// 壁の設置
public class WallPlace {
  // 横壁の設置
  public boolean placeHorizontalWall(Board board,int row, int col) {

    // 手持ちの壁があるかを確認
    if(board.turn == Board.BLACK && board.blackWalls <= 0) {
      return false;
    }
    if(board.turn == Board.WHITE && board.whiteWalls <= 0) {
      return false;
    }

    // 設置する場所が盤面内か確認する
    if(row < 0 || row >= 8 || col < 0 || col >= 8) {
      return false;
    }

    // 交差する壁があるか確認
    if(board.verticalCenter[row][col]) {
      return false;
    }

    // 設置する場所に壁がないか確認
    if(board.horizontalWall[row][col] || board.horizontalWall[row][col+1]) {
      return false;
    }

    // 壁の設置
    board.horizontalWall[row][col] = true;
    board.horizontalWall[row][col+1] = true;
    board.horizontalCenter[row][col] = true;

    // 経路確認
    BoardMap boardmap = new BoardMap();
    boardmap.createMap(board);
    Heiro heiro = new Heiro();
    boolean ok = heiro.HeiroCheck(boardmap);
    if(!ok) {
      // もとに戻す
      board.horizontalWall[row][col] = false;
      board.horizontalWall[row][col+1] = false;
      board.horizontalCenter[row][col] = false;
      return false;
    }
    
    // 手持ちの壁の枚数の更新と手番交代
    if(board.turn == Board.BLACK) {
      board.blackWalls--;
      board.turn = Board.WHITE;
    } else {
      board.whiteWalls--;
      board.turn = Board.BLACK;
    }
    return true;
  }

  // 縦壁の設置
  public boolean placeVerticalWall(Board board,int row, int col) {

    // 手持ちの壁があるかを確認
    if(board.turn == Board.BLACK && board.blackWalls <= 0) {
      return false;
    }
    if(board.turn == Board.WHITE && board.whiteWalls <= 0) {
      return false;
    }

    // 設置する場所が盤面内か確認する
    if(row < 0 || row >= 8 || col < 0 || col >= 8) {
      return false;
    }

    // 交差する壁があるか確認
    if(board.horizontalCenter[row][col]) {
      return false;
    }

    //設置する場所に壁がないか確認
    if(board.verticalWall[row][col] || board.verticalWall[row+1][col]) {
      return false;
    }

    // 壁の設置
    board.verticalWall[row][col] = true;
    board.verticalWall[row+1][col] = true;
    board.verticalCenter[row][col] = true;

    // 経路確認
    BoardMap boardmap = new BoardMap();
    boardmap.createMap(board);
    Heiro heiro = new Heiro();
    boolean ok = heiro.HeiroCheck(boardmap);
    if(!ok) {
      // もとに戻す
      board.verticalWall[row][col] = false;
      board.verticalWall[row+1][col] = false;
      board.verticalCenter[row][col] = false;
      return false;
    }
    
    // 手持ちの壁の枚数の更新と手番交代
    if(board.turn == Board.BLACK) {
      board.blackWalls--;
      board.turn = Board.WHITE;
    } else {
      board.whiteWalls--;
      board.turn = Board.BLACK;
    }
    return true;
  }

}
