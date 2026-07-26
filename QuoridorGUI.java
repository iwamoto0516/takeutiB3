import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Board / PawnMove / WallPlace のルールをそのまま呼び出すだけのGUI
// 移動判定・壁設置判定のロジックは一切ここでは実装しない(既存クラスに完全に委ねる)
public class QuoridorGUI extends JFrame {

    private static final int CELL = 50; // 1マスのピクセルサイズ
    private static final int GAP = 14;   // 壁(隙間)のピクセルサイズ
    private static final int MARGIN = 20;

    private Board board = new Board();
    private final PawnMove pawnMove = new PawnMove();
    private final WallPlace wallPlace = new WallPlace();

    private final BoardPanel boardPanel = new BoardPanel();
    private final JLabel statusLabel = new JLabel();

    private final JComboBox<String> modeSelect = new JComboBox<>(new String[]{
            "人 vs 人", "人(黒) vs AI(白)", "AI(黒) vs 人(白)", "AI vs AI"
    });
    private final JComboBox<String> aiTypeSelect = new JComboBox<>(new String[]{
            "Tanaka", "Random"
    });

    private Player blackController; // nullなら人間が操作
    private Player whiteController; // nullなら人間が操作
    private boolean inputLocked = false; // AI思考中はクリックを無視する

    public QuoridorGUI() {
        setTitle("Quoridor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton resetButton = new JButton("リセット");
        resetButton.addActionListener(e -> {
            board = new Board();
            applyModeSelection();
            refresh();
            maybeTriggerAI();
        });

        modeSelect.addActionListener(e -> resetButton.doClick());
        aiTypeSelect.addActionListener(e -> resetButton.doClick());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("対戦モード:"));
        topPanel.add(modeSelect);
        topPanel.add(new JLabel("AI:"));
        topPanel.add(aiTypeSelect);
        topPanel.add(statusLabel);
        topPanel.add(resetButton);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);

        applyModeSelection();
        refresh();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        maybeTriggerAI();
    }

    // プルダウンの選択内容から、黒・白それぞれの操作主体(人間 or AI)を決める
    private void applyModeSelection() {

        String mode = (String) modeSelect.getSelectedItem();
        String aiType = (String) aiTypeSelect.getSelectedItem();

        blackController = null;
        whiteController = null;

        if ("人(黒) vs AI(白)".equals(mode)) {
            whiteController = createAI(aiType, Board.WHITE);
        } else if ("AI(黒) vs 人(白)".equals(mode)) {
            blackController = createAI(aiType, Board.BLACK);
        } else if ("AI vs AI".equals(mode)) {
            blackController = createAI(aiType, Board.BLACK);
            whiteController = createAI(aiType, Board.WHITE);
        }
        // "人 vs 人" の場合は両方null(人間)のまま
    }

    private Player createAI(String type, int color) {
        if ("Random".equals(type)) {
            return new RandomPlayer(color);
        } else {
            return new Tanaka(color);
        }
    }

    // 現在の手番がAIなら、バックグラウンドで着手させる(GUIが固まらないようSwingWorkerを使う)
    private void maybeTriggerAI() {

        if (board.getWinner() != 0) {
            return;
        }

        Player controller = (board.turn == Board.BLACK) ? blackController : whiteController;

        if (controller == null) {
            return; // 人間の手番。クリックを待つ
        }

        inputLocked = true;
        statusLabel.setText(statusLabel.getText() + "  (AI思考中...)");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return controller.play(board);
            }

            @Override
            protected void done() {
                inputLocked = false;
                refresh();

                // AI vs AI のときも見やすいように、少し間をおいて次の手番をチェックする
                Timer timer = new Timer(300, e -> maybeTriggerAI());
                timer.setRepeats(false);
                timer.start();
            }
        };
        worker.execute();
    }

    // 画面表示を最新の盤面状態に合わせて更新する
    private void refresh() {

        int winner = board.getWinner();

        if (winner == Board.BLACK) {
            statusLabel.setText("黒の勝ち！");
        } else if (winner == Board.WHITE) {
            statusLabel.setText("白の勝ち！");
        } else {
            String turnText = (board.turn == Board.BLACK) ? "黒" : "白";
            statusLabel.setText("手番: " + turnText
                    + "   黒の壁: " + board.blackWalls
                    + "   白の壁: " + board.whiteWalls);
        }

        boardPanel.repaint();
    }

    // 17分割(0〜16)の座標系での、各スロットの開始ピクセル位置とサイズを計算するヘルパー
    // 偶数インデックス = マス(CELLサイズ)、奇数インデックス = 壁の隙間(GAPサイズ)
    private static int slotStart(int index) {
        int pos = MARGIN;
        for (int i = 0; i < index; i++) {
            pos += (i % 2 == 0) ? CELL : GAP;
        }
        return pos;
    }

    private static int slotSize(int index) {
        return (index % 2 == 0) ? CELL : GAP;
    }

    // ピクセル座標から17分割のスロット番号(0〜16)を求める。範囲外なら-1
    static int pixelToSlot(int pixel) {
        int pos = MARGIN;
        for (int i = 0; i < 17; i++) {
            int size = (i % 2 == 0) ? CELL : GAP;
            if (pixel >= pos && pixel < pos + size) {
                return i;
            }
            pos += size;
        }
        return -1;
    }

    private class BoardPanel extends JPanel {

        BoardPanel() {
            int size = slotStart(16) + slotSize(16) + MARGIN - MARGIN + MARGIN;
            setPreferredSize(new Dimension(size, size));
            setBackground(new Color(0xF4F3EE));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleClick(e.getX(), e.getY());
                }
            });
        }

        private void handleClick(int x, int y) {

            if (inputLocked || board.getWinner() != 0) {
                return;
            }

            int slotRow = pixelToSlot(y);
            int slotCol = pixelToSlot(x);

            if (slotRow == -1 || slotCol == -1) {
                return;
            }

            boolean rowIsCell = (slotRow % 2 == 0);
            boolean colIsCell = (slotCol % 2 == 0);

            if (rowIsCell && colIsCell) {
                // マスをクリック → 駒を移動(判定はPawnMoveに完全に委ねる)
                int row = slotRow / 2;
                int col = slotCol / 2;
                boolean moved = pawnMove.movePawn(board, row, col);
                if (!moved) {
                    Toolkit.getDefaultToolkit().beep();
                }
                refresh();
                maybeTriggerAI();

            } else if (!rowIsCell && colIsCell) {
                // 横の隙間をクリック → 横壁(判定はWallPlaceに完全に委ねる)
                int row = (slotRow - 1) / 2;
                int col = slotCol / 2;
                boolean placed = wallPlace.placeHorizontalWall(board, row, col);
                if (!placed) {
                    Toolkit.getDefaultToolkit().beep();
                }
                refresh();
                maybeTriggerAI();

            } else if (rowIsCell && !colIsCell) {
                // 縦の隙間をクリック → 縦壁(判定はWallPlaceに完全に委ねる)
                int row = slotRow / 2;
                int col = (slotCol - 1) / 2;
                boolean placed = wallPlace.placeVerticalWall(board, row, col);
                if (!placed) {
                    Toolkit.getDefaultToolkit().beep();
                }
                refresh();
                maybeTriggerAI();
            }
            // 隙間同士(壁の交差点)をクリックした場合は何もしない
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // マス
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {

                    int x = slotStart(col * 2);
                    int y = slotStart(row * 2);

                    if (row == board.blackRow && col == board.blackCol) {
                        g2.setColor(new Color(0x2C2C2A));
                    } else if (row == board.whiteRow && col == board.whiteCol) {
                        g2.setColor(Color.WHITE);
                    } else {
                        g2.setColor(Color.WHITE);
                    }
                    g2.fillRoundRect(x, y, CELL, CELL, 6, 6);

                    g2.setColor(new Color(0xD3D1C7));
                    g2.drawRoundRect(x, y, CELL, CELL, 6, 6);

                    if (row == board.whiteRow && col == board.whiteCol) {
                        g2.setColor(new Color(0x5F5E5A));
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(x + 1, y + 1, CELL - 2, CELL - 2, 6, 6);
                    }
                }
            }

            // 横壁
            g2.setColor(new Color(0x378ADD));
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 9; col++) {
                    if (board.horizontalWall[row][col]) {
                        int x = slotStart(col * 2);
                        int y = slotStart(row * 2 + 1);
                        g2.fillRoundRect(x, y, CELL, GAP, 3, 3);
                    }
                }
            }

            // 縦壁
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board.verticalWall[row][col]) {
                        int x = slotStart(col * 2 + 1);
                        int y = slotStart(row * 2);
                        g2.fillRoundRect(x, y, GAP, CELL, 3, 3);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuoridorGUI::new);
    }
}
