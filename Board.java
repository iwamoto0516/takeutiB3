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

