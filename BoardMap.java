public class BoardMap {
  public static final int EMPTY = 0;
  public static final int WALL = 1;
  public static final int BLACK = 2;
  public static final int WHITE = 3;

  public int blackCol = 0;
  public int blackRow = 0;
  public int whiteCol = 0;
  public int whiteRow = 0;
  
  public int[][] createMap(Board board) {
    int[][] map = new int[17][17];
    // 横壁
    for (int row = 0; row < 8; row++) {
        for (int col = 0; col < 8; col++) {

            if (board.horizontalCenter[row][col]) {
                map[row * 2 + 1][col * 2] = WALL;
                map[row * 2 + 1][col * 2 + 1] = WALL;
                map[row * 2 + 1][col * 2 + 2] = WALL;
            }
        }
    }

    // 縦壁
    for (int row = 0; row < 8; row++) {
        for (int col = 0; col < 8; col++) {

            if (board.verticalCenter[row][col]) {
                map[row * 2][col * 2 + 1] = WALL;
                map[row * 2 + 1][col * 2 + 1] = WALL;
                map[row * 2 + 2][col * 2 + 1] = WALL;
            }
        }
    }

    // 黒駒
    map[board.blackRow * 2][board.blackCol * 2] = BLACK;
    blackCol = board.blackCol * 2;
    blackRow = board.blackRow * 2;

    // 白駒
    map[board.whiteRow * 2][board.whiteCol * 2] = WHITE;
    whiteCol = board.whiteCol * 2;
    whiteRow = board.whiteRow * 2;

    return map;
  }
}

