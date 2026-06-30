// ゲーム全体の進行
public class QuoridorGame {

    private Board board;

    private Player blackPlayer;
    private Player whitePlayer;

    private Player currentPlayer;

    public QuoridorGame() {

        board = new Board();

        blackPlayer = new HumanPlayer(Board.BLACK);
        whitePlayer = new HumanPlayer(Board.WHITE);

        currentPlayer = blackPlayer;
    }

    public void start() {

        while (board.getWinner() == 0) {

            BoardPrinter.printBoard(board);

            if (currentPlayer.getColor() == Board.BLACK) {
                System.out.println("黒の番");
            } else {
                System.out.println("白の番");
            }

            boolean success = currentPlayer.play(board);

            if (!success) {
                System.out.println("その操作はできません！");
                continue;
            }

            if (currentPlayer == blackPlayer) {
                currentPlayer = whitePlayer;
            } else {
                currentPlayer = blackPlayer;
            }
        }

        if (board.getWinner() == Board.BLACK) {
            System.out.println("黒の勝ち！");
        } else {
            System.out.println("白の勝ち！");
        }
    }
}
