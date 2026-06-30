// 簡易的な盤面表示
public class BoardPrinter {

    // 盤面を表示
    public static void printBoard(Board board) {

        // 列番号
        System.out.print("    ");
        for (int col = 0; col < 9; col++) {
            System.out.printf("%-4d", col);
        }
        System.out.println();

        for (int row = 0; row < 9; row++) {

            // 駒の行
            System.out.printf("%-3d ", row);

            for (int col = 0; col < 9; col++) {

                char cell = '.';

                if (row == board.blackRow && col == board.blackCol) {
                    cell = 'B';
                } else if (row == board.whiteRow && col == board.whiteCol) {
                    cell = 'W';
                }

                System.out.print(cell);

                // 縦壁
                if (col < 8) {
                    if (board.verticalWall[row][col]) {
                        System.out.print(" | ");
                    } else {
                        System.out.print("   ");
                    }
                }
            }

            System.out.println();

            // 最後の行の下には横壁を表示しない
            if (row == 8) {
                continue;
            }

            // 横壁の行
            System.out.print("  ");

            for (int col = 0; col < 9; col++) {

                if (board.horizontalWall[row][col]) {
                    System.out.print("+---");
                } else {
                    System.out.print("+   ");
                }
            }

            System.out.println("+");
        }

        System.out.println();
        System.out.println("黒の壁: " + board.blackWalls);
        System.out.println("白の壁: " + board.whiteWalls);
        System.out.println();
    }
}