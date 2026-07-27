import java.util.List;
import java.util.ArrayList;

class MCTS2Node {
    Board state;
    MCTS2Node parent;
    List<MCTS2Node> children = new ArrayList<>();

    double w = 0.0;   // 累計価値
    int n = 0;        // 試行回数

    int moveType;     // 0:移動, 1:横壁, 2:縦壁
    int moveR, moveC; // 手の内容

    int playerToMove; // この局面で手番

    boolean isExpanded = false;

    MCTS2Node(Board state, MCTS2Node parent) {
        this.state = state;
        this.parent = parent;
        this.playerToMove = state.turn;
    }

    @Override
    public String toString() {
        return "MCTS2Node{" +
               "w=" + w +
               ", n=" + n +
               ", moveType=" + moveType +
               ", moveR=" + moveR +
               ", moveC=" + moveC +
               '}';
    }
}

public class MCTS2 {

    private static final double C_UCB = 1.4;

    private PawnMove pawnMove = new PawnMove();
    private WallPlace wallPlace = new WallPlace();

    // --- Masuya ラッパー ---
    private Masuya helper = new Masuya(Board.BLACK);

    private List<int[]> getLegalPawnMoves(Board board) {
        return helper.getLegalPawnMoves(board);
    }

    private List<int[]> getLegalHorizontalWalls(Board board) {
        return helper.getLegalHorizontalWalls(board);
    }

    private List<int[]> getLegalVerticalWalls(Board board) {
        return helper.getLegalVerticalWalls(board);
    }

    private int shortestPath(Board board, int color) {
        return helper.shortestPath(board, color);
    }

    // --- UCB1 による子ノード選択 ---
    private MCTS2Node selectChild(MCTS2Node node) {
        MCTS2Node best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double lnT = Math.log(Math.max(1, node.n));

        for (MCTS2Node child : node.children) {
            double q = (child.n == 0) ? 0.0 : child.w / child.n;
            double u = C_UCB * Math.sqrt(lnT / (child.n + 1));
            double score = q + u;
            if (score > bestScore) {
                bestScore = score;
                best = child;
            }
        }
        return best;
    }

    // --- 展開：方針に応じて移動だけ or 壁だけ ---
    private void expand(MCTS2Node node, boolean runMode) {
        if (node.isExpanded) return;

        Board board = node.state;

        if (runMode) {
            // 走りモード：移動手だけ展開
            for (int[] mv : getLegalPawnMoves(board)) {
                Board tmp = new Board(board);
                pawnMove.movePawn(tmp, mv[0], mv[1]);

                MCTS2Node child = new MCTS2Node(tmp, node);
                child.moveType = 0;
                child.moveR = mv[0];
                child.moveC = mv[1];
                node.children.add(child);
            }
        } else {
            // 妨害モード：壁手だけ展開
            for (int[] w : getLegalHorizontalWalls(board)) {
                Board tmp = new Board(board);
                wallPlace.placeHorizontalWall(tmp, w[0], w[1]);

                MCTS2Node child = new MCTS2Node(tmp, node);
                child.moveType = 1;
                child.moveR = w[0];
                child.moveC = w[1];
                node.children.add(child);
            }

            for (int[] w : getLegalVerticalWalls(board)) {
                Board tmp = new Board(board);
                wallPlace.placeVerticalWall(tmp, w[0], w[1]);

                MCTS2Node child = new MCTS2Node(tmp, node);
                child.moveType = 2;
                child.moveR = w[0];
                child.moveC = w[1];
                node.children.add(child);
            }
        }

        node.isExpanded = true;
    }

    // --- 移動フェーズ用：プレイアウトなしで最短距離評価 ---
    private double evaluateRun(Board board, int rootPlayer) {
        int myColor = rootPlayer;
        int enemyColor = (myColor == Board.BLACK ? Board.WHITE : Board.BLACK);

        int myDist = shortestPath(board, myColor);
        int enemyDist = shortestPath(board, enemyColor);

        // ゴールボーナス／ペナルティ
        if (myDist == 0) return 100.0;
        if (enemyDist == 0) return -100.0;

        // シンプルに「敵よりどれだけ近いか」
        return (enemyDist - myDist);
    }

    // --- 壁プレイアウト（妨害モード用） ---
    private double wallPlayout(Board board, int rootPlayer) {

        Board tmp = new Board(board);
        int myColor    = tmp.turn;
        int enemyColor = (myColor == Board.BLACK ? Board.WHITE : Board.BLACK);

        WallPlace wp = new WallPlace();

        for (int step = 0; step < 200; step++) {

            int myWalls = (myColor == Board.BLACK ? tmp.blackWalls : tmp.whiteWalls);
            if (myWalls <= 0) break;

            List<int[]> hw = getLegalHorizontalWalls(tmp);
            List<int[]> vw = getLegalVerticalWalls(tmp);

            List<int[]> allWalls = new ArrayList<>();
            for (int[] w : hw) allWalls.add(new int[]{1, w[0], w[1]});
            for (int[] w : vw) allWalls.add(new int[]{2, w[0], w[1]});

            if (allWalls.isEmpty()) break;

            int[] w = allWalls.get((int)(Math.random() * allWalls.size()));

            Board tmp2 = new Board(tmp);
            boolean ok = false;

            if (w[0] == 1) ok = wp.placeHorizontalWall(tmp2, w[1], w[2]);
            if (w[0] == 2) ok = wp.placeVerticalWall(tmp2, w[1], w[2]);

            if (!ok) continue;

            tmp = tmp2;

            tmp.turn = enemyColor;
            int t = myColor;
            myColor = enemyColor;
            enemyColor = t;
        }

        int rootColor   = rootPlayer;
        int rootEnemy   = (rootColor == Board.BLACK ? Board.WHITE : Board.BLACK);

        int enemyDistBefore = shortestPath(board, rootEnemy);
        int enemyDistAfter  = shortestPath(tmp, rootEnemy);

        // ゴールボーナス／ペナルティも見る
        int myDistAfter = shortestPath(tmp, rootColor);

        if (myDistAfter == 0) return 100.0;
        if (enemyDistAfter == 0) return -100.0;

        // 敵の距離が伸びた分だけ価値
        return enemyDistAfter - enemyDistBefore;
    }

    // --- 価値の伝播 ---
    private void backpropagate(MCTS2Node node, double value, int rootPlayer) {
        MCTS2Node cur = node;
        while (cur != null) {
            cur.n += 1;
            cur.w += value;
            cur = cur.parent;
        }
    }

    // --- 1回の MCTS 反復 ---
    private void mctsIteration(MCTS2Node root) {
        MCTS2Node node = root;

        // 選択フェーズ
        while (node.isExpanded && !node.children.isEmpty()) {
            node = selectChild(node);
        }

        // 方針決定
        Board s = node.state;
        int myColor    = s.turn;
        int enemyColor = (myColor == Board.BLACK ? Board.WHITE : Board.BLACK);

        int myDist    = shortestPath(s, myColor);
        int enemyDist = shortestPath(s, enemyColor);
        int myWalls   = (myColor == Board.BLACK ? s.blackWalls : s.whiteWalls);

        boolean runMode = (myDist < enemyDist || myWalls == 0);

        // 展開フェーズ
        expand(node, runMode);
        if (node.children.isEmpty()) return;

        // 新しく展開された子から1つ選んで評価／プレイアウト
        MCTS2Node leaf = node.children.get((int)(Math.random() * node.children.size()));

        int rootPlayer = root.playerToMove;

        double value;
        if (runMode) {
            // 移動手 → プレイアウトなし評価
            value = evaluateRun(leaf.state, rootPlayer);
        } else {
            // 壁手 → 壁プレイアウト
            value = wallPlayout(leaf.state, rootPlayer);
        }

        // 逆伝播
        backpropagate(leaf, value, root.playerToMove);
    }

    // --- 外から呼ぶ入口 ---
    public void playWithMCTS2(Board board, int iterations) {
        MCTS2Node root = new MCTS2Node(new Board(board), null);

        for (int i = 0; i < iterations; i++) {
            mctsIteration(root);
        }

        MCTS2Node best = null;
        int bestN = -1;
        for (MCTS2Node child : root.children) {
            if (child.n > bestN) {
                bestN = child.n;
                best = child;
            }
        }
        System.out.println(best);

        if (best != null) {
            switch (best.moveType) {
                case 0:
                    pawnMove.movePawn(board, best.moveR, best.moveC);
                    break;
                case 1:
                    wallPlace.placeHorizontalWall(board, best.moveR, best.moveC);
                    break;
                case 2:
                    wallPlace.placeVerticalWall(board, best.moveR, best.moveC);
                    break;
            }
        }
    }
}
