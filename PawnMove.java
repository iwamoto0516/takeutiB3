public class PawnMove {

  // 盤面内か確認する
  private boolean isInsideBoard(int row, int col) {
    return !(row < 0 || row > 8 || col < 0 || col > 8);
  }

  // 駒を移動し、手番を交代する
  private void moveCurrentPawn(Board board, int row, int col) {

    if (board.turn == Board.BLACK) {
        board.blackRow = row;
        board.blackCol = col;
        board.turn = Board.WHITE;
    } else {
        board.whiteRow = row;
        board.whiteCol = col;
        board.turn = Board.BLACK;
    }
  }

  // 駒を移動する
  public boolean movePawn(Board board, int row, int col) {


  //現在の手番の座標
  int currentRow; 
  int currentCol;
  // 相手の駒の座標
  int enemyRow;
  int enemyCol;

    // 現在の手番の駒を取得
    if(board.turn == Board.BLACK) {
      currentRow = board.blackRow;
      currentCol = board.blackCol;
      enemyRow = board.whiteRow;
      enemyCol = board.whiteCol;
    } else {
      currentRow = board.whiteRow;
      currentCol = board.whiteCol;
      enemyRow = board.blackRow;
      enemyCol = board.blackCol;
    }
    

    // 盤面内か確認する
  if(!isInsideBoard(row, col)) {
      return false; // 盤面外だった場合,false
    }

    // 1マス移動か確認(それぞれの絶対値を足す)
    int distance = Math.abs(row - currentRow) + Math.abs(col - currentCol);

    boolean jumpMove = false;
    boolean diagonalMove = false;

    // 相手のいるマスには移動できない
    if(row == enemyRow && col == enemyCol) {
      return false;
    }

    if(isNormalMove(board,currentRow, currentCol, row, col)) {
      moveCurrentPawn(board,row,col);
      return true;
    } else if(distance == 2) {
      // ジャンプ移動できるか判定
      jumpMove = isJumpMove(board,currentRow,currentCol,enemyRow,enemyCol,row,col);
      // 斜め移動できるか判定
      diagonalMove = isDiagonalMove(board,currentRow,currentCol,enemyRow,enemyCol,row,col);
      if(!jumpMove && !diagonalMove) {
        return false;
      }
      moveCurrentPawn(board,row,col);
      return true;
    }
    return false;
  }

    // 1マス移動できるか判定
  private boolean isNormalMove(Board board,int currentRow, int currentCol, int row, int col) {
    int distance = Math.abs(row - currentRow) + Math.abs(col - currentCol);
    if (distance != 1) {
      return false;
    }
    // 下
    if (row == currentRow + 1) {
      return !board.horizontalWall[currentRow][currentCol];
    }
    // 上
    if (row == currentRow - 1) {
      return !board.horizontalWall[currentRow - 1][currentCol];
    }
    // 右
    if (col == currentCol + 1) {
      return !board.verticalWall[currentRow][currentCol];
    }
    // 左
    if (col == currentCol - 1) {
      return !board.verticalWall[currentRow][currentCol - 1];
    }
    return false;
  }
    // ジャンプ移動できるか判定
  private boolean isJumpMove(Board board,int currentRow, int currentCol,
                            int enemyRow, int enemyCol,
                             int row, int col) {
    // 下方向にジャンプ
    if (enemyRow == currentRow + 1 &&
        enemyCol == currentCol &&
        row == currentRow + 2 &&
        col == currentCol) {
        if (board.horizontalWall[currentRow][currentCol]) {
            return false;
        }
        if (board.horizontalWall[currentRow + 1][currentCol]) {
            return false;
        }
        return true;
    }

    // 上方向にジャンプ
    else if (enemyRow == currentRow - 1 &&
             enemyCol == currentCol &&
             row == currentRow - 2 &&
             col == currentCol) {
        if (board.horizontalWall[currentRow - 1][currentCol]) {
            return false;
        }
        if (board.horizontalWall[currentRow - 2][currentCol]) {
            return false;
        }
        return true;
    }

    // 右方向にジャンプ
    else if (enemyRow == currentRow &&
             enemyCol == currentCol + 1 &&
             row == currentRow &&
             col == currentCol + 2) {
        if (board.verticalWall[currentRow][currentCol]) {
            return false;
        }
        if (board.verticalWall[currentRow][currentCol + 1]) {
            return false;
        }
        return true;
    }

    // 左方向にジャンプ
    else if (enemyRow == currentRow &&
             enemyCol == currentCol - 1 &&
             row == currentRow &&
             col == currentCol - 2) {
        if (board.verticalWall[currentRow][currentCol - 1]) {
            return false;
        }
        if (board.verticalWall[currentRow][currentCol - 2]) {
            return false;
        }
        return true;
    }
    return false;
  }

  // 斜め移動できるか判定
  private boolean isDiagonalMove(Board board,int currentRow, int currentCol,
                                 int enemyRow, int enemyCol,
                                 int row, int col) {

    // 下方向への斜め移動
    if(enemyRow == currentRow + 1 &&
       enemyCol == currentCol) {

        if(board.horizontalWall[currentRow][currentCol]) {
            return false;
        }

        if(board.horizontalWall[currentRow + 1][currentCol]) {

            // 左下
            if(row == currentRow + 1 && col == currentCol - 1) {
                return !board.verticalWall[currentRow + 1][currentCol - 1];
            }

            // 右下
            if(row == currentRow + 1 && col == currentCol + 1) {
                return !board.verticalWall[currentRow + 1][currentCol];
            }
        }
    }

    // 上方向への斜め移動
    else if(enemyRow == currentRow - 1 &&
            enemyCol == currentCol) {

        if(board.horizontalWall[currentRow - 1][currentCol]) {
            return false;
        }

        if(board.horizontalWall[currentRow - 2][currentCol]) {

            if(row == currentRow - 1 && col == currentCol - 1) {
                return !board.verticalWall[currentRow - 1][currentCol - 1];
            }

            if(row == currentRow - 1 && col == currentCol + 1) {
                return !board.verticalWall[currentRow - 1][currentCol];
            }
        }
    }

    // 右方向への斜め移動
    else if(enemyRow == currentRow &&
            enemyCol == currentCol + 1) {

        if(board.verticalWall[currentRow][currentCol]) {
            return false;
        }

        if(board.verticalWall[currentRow][currentCol + 1]) {

            if(row == currentRow - 1 && col == currentCol + 1) {
                return !board.horizontalWall[currentRow - 1][currentCol + 1];
            }

            if(row == currentRow + 1 && col == currentCol + 1) {
                return !board.horizontalWall[currentRow][currentCol + 1];
            }
        }
    }

    // 左方向への斜め移動
    else if(enemyRow == currentRow &&
            enemyCol == currentCol - 1) {

        if(board.verticalWall[currentRow][currentCol - 1]) {
            return false;
        }

        if(board.verticalWall[currentRow][currentCol - 2]) {

            if(row == currentRow - 1 && col == currentCol - 1) {
                return !board.horizontalWall[currentRow - 1][currentCol - 1];
            }

            if(row == currentRow + 1 && col == currentCol - 1) {
                return !board.horizontalWall[currentRow][currentCol - 1];
            }
        }
    }

    return false;
  }


}
