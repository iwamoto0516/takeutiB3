// 盤面のデータの管理
public class Board {

  // プレイヤーを表す
  public static final int BLACK = 1;
  public static final int WHITE = 2;

  // 黒駒の座標
  int blackRow;
  int blackCol;

  // 白駒の座標
  int whiteRow;
  int whiteCol;

  // 壁の残りの枚数
  int blackWalls;
  int whiteWalls;

  int turn; // 現在の手番
  boolean[][] horizontalWall; // 横壁の情報
  boolean[][] verticalWall; // 縦壁の情報
  boolean[][] horizontalCenter; // 横壁の中心点
  boolean[][] verticalCenter; // 縦壁の中心点

  public Board() {
    // 駒の初期位置
    blackRow = 0;
    blackCol = 4;
    whiteRow = 8;
    whiteCol = 4;

    // 壁の枚数
    blackWalls = 10;
    whiteWalls = 10;

    // 手番
    turn = BLACK;

    // 壁の配置情報
    horizontalWall = new boolean[8][9];
    verticalWall = new boolean[9][8];
    horizontalCenter = new boolean[8][8];
    verticalCenter = new boolean[8][8];
  }

  public Board(Board other) {
    this.blackRow = other.blackRow;
    this.blackCol = other.blackCol;
    this.whiteRow = other.whiteRow;
    this.whiteCol = other.whiteCol;

    this.blackWalls = other.blackWalls;
    this.whiteWalls = other.whiteWalls;

    this.turn = other.turn;

    this.horizontalWall = new boolean[8][9];
    this.verticalWall = new boolean[9][8];
    this.horizontalCenter = new boolean[8][8];
    this.verticalCenter = new boolean[8][8];

    for (int r = 0; r < 8; r++) {
        for (int c = 0; c < 9; c++) {
            this.horizontalWall[r][c] = other.horizontalWall[r][c];
        }
    }
    for (int r = 0; r < 9; r++) {
        for (int c = 0; c < 8; c++) {
            this.verticalWall[r][c] = other.verticalWall[r][c];
        }
    }
    for (int r = 0; r < 8; r++) {
        for (int c = 0; c < 8; c++) {
            this.horizontalCenter[r][c] = other.horizontalCenter[r][c];
            this.verticalCenter[r][c] = other.verticalCenter[r][c];
        }
    }
  }

  // ゴール判定
  public int getWinner() {
      if(blackRow == 8) {
        return BLACK;
      }
      if(whiteRow == 0) {
        return WHITE;
      }
      return 0;
    }
}

