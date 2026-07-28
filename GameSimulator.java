// 何戦か自動対戦させて勝敗を集計するシミュレータ
public class GameSimulator {
 
    // ===== ここを変えるだけで対戦カードを変更できる =====
    // 指定できる名前は createPlayer() 内の switch に対応するクラス名
    // 例: "Tanaka", "Iwamoto", "Masuya2" など
    private static final String PLAYER_A_NAME = "Tanaka";
    private static final String PLAYER_B_NAME = "Masuya";
 
    private static final int TOTAL_GAMES = 100;
    private static final int MAX_MOVES = 300; // 無限ループ防止のための手数上限
    // ================================================
 
    public static void main(String[] args) {
 
        int playerAWins = 0;
        int playerBWins = 0;
        int draws = 0; // 手数上限に達した、または動けなくなった場合
 
        long totalMoves = 0;
 
        int half = TOTAL_GAMES / 2; // 前半戦数(先行後攻の切り替えポイント)
 
        for (int gameNumber = 1; gameNumber <= TOTAL_GAMES; gameNumber++) {
 
            Board board = new Board();
 
            // 前半: Aが先行(黒)/Bが後攻(白)
            // 後半: Bが先行(黒)/Aが後攻(白)
            boolean aIsBlack = gameNumber <= half;
 
            Player playerA = createPlayer(PLAYER_A_NAME, aIsBlack ? Board.BLACK : Board.WHITE);
            Player playerB = createPlayer(PLAYER_B_NAME, aIsBlack ? Board.WHITE : Board.BLACK);
 
            Player black = aIsBlack ? playerA : playerB;
            Player white = aIsBlack ? playerB : playerA;
 
            Player current = black;
            int moveCount = 0;
            boolean stuck = false;
 
            while (board.getWinner() == 0 && moveCount < MAX_MOVES) {
 
                boolean success = current.play(board);
 
                if (!success) {
                    // 動けない・置ける壁がない等で行動できなかった場合はそこで打ち切り
                    stuck = true;
                    break;
                }
 
                moveCount++;
 
                current = (current == black) ? white : black;
            }
 
            int winner = board.getWinner();
            totalMoves += moveCount;
 
            String resultLabel;
 
            if (winner == Board.BLACK) {
                if (black == playerA) {
                    playerAWins++;
                } else {
                    playerBWins++;
                }
                resultLabel = (black == playerA ? PLAYER_A_NAME : PLAYER_B_NAME) + "の勝ち(先行)";
            } else if (winner == Board.WHITE) {
                if (white == playerA) {
                    playerAWins++;
                } else {
                    playerBWins++;
                }
                resultLabel = (white == playerA ? PLAYER_A_NAME : PLAYER_B_NAME) + "の勝ち(後攻)";
            } else {
                draws++;
                resultLabel = stuck ? "引き分け(動けず終了)" : "引き分け(手数上限)";
            }
 
            String turnOrderLabel = aIsBlack
                    ? (PLAYER_A_NAME + "先行/" + PLAYER_B_NAME + "後攻")
                    : (PLAYER_B_NAME + "先行/" + PLAYER_A_NAME + "後攻");
            System.out.println("第" + gameNumber + "戦[" + turnOrderLabel + "]: " + resultLabel + "  (手数: " + moveCount + ")");
        }
 
        System.out.println();
        System.out.println("========== 集計結果 ==========");
        System.out.println("対戦カード: " + PLAYER_A_NAME + " vs " + PLAYER_B_NAME);
        System.out.println("総試合数: " + TOTAL_GAMES + " (先行後攻を" + half + "戦ごとに入れ替え)");
        System.out.println(PLAYER_A_NAME + "勝利: " + playerAWins + " (" + percentage(playerAWins) + "%)");
        System.out.println(PLAYER_B_NAME + "勝利: " + playerBWins + " (" + percentage(playerBWins) + "%)");
        System.out.println("引き分け: " + draws + " (" + percentage(draws) + "%)");
        System.out.println("平均手数: " + String.format("%.1f", (double) totalMoves / TOTAL_GAMES));
    }
 
    // 名前からプレイヤーのインスタンスを生成する
    // 新しいAIクラスを追加したらここにcaseを足すだけでよい
    private static Player createPlayer(String name, int color) {
        switch (name) {
            case "Tanaka":
                return new Tanaka(color);
            case "Iwamoto":
                return new Iwamoto(color);
            case "Masuya2":
                return new Masuya2(color);
            case "Masuya":
                return new Masuya(color);
            default:
                throw new IllegalArgumentException("未知のプレイヤー名です: " + name);
        }
    }
 
    private static double percentage(int count) {
        return Math.round((double) count / TOTAL_GAMES * 1000) / 10.0;
    }
}
 
