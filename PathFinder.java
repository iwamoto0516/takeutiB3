public class PathFinder {

  public boolean hasPath(Board board, int player) {

    BoardMap boardMap = new BoardMap();
    // 17×17の盤面を作成
    int[][] map = boardMap.createMap(board);

    // 探索開始位置
    int startRow;
    int startCol;

    if (player == Board.BLACK) {
        startRow = board.blackRow * 2;
        startCol = board.blackCol * 2;
    } else {
        startRow = board.whiteRow * 2;
        startCol = board.whiteCol * 2;
    }

    return true; // またはfalse
  }
}
