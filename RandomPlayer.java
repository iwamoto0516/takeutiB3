import java.util.Random;

public class RandomPlayer extends Player {

    private Random random = new Random();

    public RandomPlayer(int color) {
        super(color);
    }

    @Override
    public boolean play(Board board) {

        PawnMove pawnMove = new PawnMove();
        WallPlace wallPlace = new WallPlace();

        while (true) {

            int action = random.nextInt(3); // 0:移動 1:横壁 2:縦壁

            int row = random.nextInt(9);
            int col = random.nextInt(9);

            switch (action) {

                case 0:

                    int currentRow;
                    int currentCol;

                    if (board.turn == Board.BLACK) {
                        currentRow = board.blackRow;
                        currentCol = board.blackCol;
                    } else {
                        currentRow = board.whiteRow;
                        currentCol = board.whiteCol;
                    }

                    int direction = random.nextInt(4);

                    switch (direction) {
                        case 0: // 上
                            row = currentRow - 1;
                            col = currentCol;
                            break;

                        case 1: // 下
                            row = currentRow + 1;
                            col = currentCol;
                            break;

                        case 2: // 左
                            row = currentRow;
                            col = currentCol - 1;
                            break;

                        default: // 右
                            row = currentRow;
                            col = currentCol + 1;
                            break;
                    }

                    if (pawnMove.movePawn(board, row, col)) {
                        return true;
                    }
                    break;
                case 1:
                    row = random.nextInt(8);
                    col = random.nextInt(8);

                    if (wallPlace.placeHorizontalWall(board, row, col)) {
                        return true;
                    }
                    break;

                case 2:
                    row = random.nextInt(8);
                    col = random.nextInt(8);

                    if (wallPlace.placeVerticalWall(board, row, col)) {
                        return true;
                    }
                    break;
            }
        }
    }
}