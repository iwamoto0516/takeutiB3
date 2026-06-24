// 現在の盤面、駒の位置、壁の位置、手番の管理
public class Quoridor {

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

  public Quoridor() {
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

  // 駒を移動する
  public boolean movePawn(int row, int col) {

  //現在の手番の座標
  int currentRow; 
  int currentCol;
  // 相手の駒の座標
  int enemyRow;
  int enemyCol;

    // 現在の手番の駒を取得
    if(turn == BLACK) {
      currentRow = blackRow;
      currentCol = blackCol;
      enemyRow = whiteRow;
      enemyCol = whiteCol;
    } else {
      currentRow = whiteRow;
      currentCol = whiteCol;
      enemyRow = blackRow;
      enemyCol = blackCol;
    }

    // 盤面内か確認する
    if(row < 0 || row > 8 || col < 0 || col > 8) {
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

    if(distance == 1) {
      // 前方(後方)壁がないか確認
      if(row == currentRow + 1) {
        if(horizontalWall[currentRow][currentCol]) {
          return false;
        }
      }

      // 後方(前方)に壁がないか確認
      if(row == currentRow - 1) {
        if(horizontalWall[currentRow - 1][currentCol]) {
          return false;
        }
      }

      // 左(右)に壁がないか確認
      if(col == currentCol + 1) {
        if(verticalWall[currentRow][currentCol]) {
          return false;
        }
      }

      // 右(左)に壁がないか確認
      if(col == currentCol - 1) {
        if(verticalWall[currentRow][currentCol - 1]) {
          return false;
        }
      }

    } else if(distance == 2) {

      // 下方向にジャンプ
      if(enemyRow == currentRow + 1 && 
        enemyCol == currentCol && 
        row == currentRow + 2 && 
        col == currentCol) {

          // 相手との間の壁チェック
          if(horizontalWall[currentRow][currentCol]) {
            return false;
          }

          // 相手の後ろに壁がないかチェック
          if(horizontalWall[currentRow + 1][currentCol]) {
            return false;
          }
          
          jumpMove = true;
      }

  // 上方向にジャンプ
  else if(enemyRow == currentRow - 1 &&
          enemyCol == currentCol &&
          row == currentRow - 2 &&
          col == currentCol) {

          // 相手との間の壁チェック
          if(horizontalWall[currentRow - 1][currentCol]) {
            return false;
          }

          // 相手の後ろに壁がないかチェック
          if(horizontalWall[currentRow - 2][currentCol]) {
            return false;
          }
          jumpMove = true;
        }

  // 右方向にジャンプ
  else if(enemyRow == currentRow &&
          enemyCol == currentCol + 1 &&
          row == currentRow &&
          col == currentCol + 2) {

          // 相手との間の壁チェック
          if(verticalWall[currentRow][currentCol]) {
            return false;
          }

          // 相手の後ろに壁がないかチェック
          if(verticalWall[currentRow][currentCol + 1]) {
            return false;
          }
          jumpMove = true;
        }

  // 左方向にジャンプ
  else if(enemyRow == currentRow &&
          enemyCol == currentCol - 1 &&
          row == currentRow &&
          col == currentCol - 2) {

          // 相手との間の壁チェック
          if(verticalWall[currentRow][currentCol - 1]) {
            return false;
          }

          // 相手の後ろに壁がないかチェック
          if(verticalWall[currentRow][currentCol - 2]) {
            return false;
          }
          jumpMove = true;
        }
      // 下方向への斜め移動
      if(enemyRow == currentRow + 1 &&
         enemyCol == currentCol) {
          // 相手との間に壁があるなら不可
          if(horizontalWall[currentRow][currentCol]) {
            return false;
          }
          // 相手の後ろに壁があることが条件
          if(horizontalWall[currentRow + 1][currentCol]) {
            // 左下
            if(row == currentRow + 1 && col == currentCol - 1) {
              // 相手の左側に壁がない
              if(!verticalWall[currentRow + 1][currentCol - 1]) {
                diagonalMove = true;
              }
            } else if(row == currentRow + 1 && col == currentCol + 1) {
              if(!verticalWall[currentRow + 1][currentCol]) {
                diagonalMove = true;
              }
            }
          }
      }
      
      //上方向への斜め移動
      else if(enemyRow == currentRow - 1 &&
              enemyCol == currentCol) {
          if(horizontalWall[currentRow - 1][currentCol]) {
            return false;
          }
          if(horizontalWall[currentRow - 2][currentCol]) {
            if(row == currentRow - 1 && col == currentCol - 1) {
              if(!verticalWall[currentRow - 1][currentCol - 1]) {
                diagonalMove = true;
              }
            } else if(row == currentRow - 1 && col == currentCol + 1) {
              if(!verticalWall[currentRow - 1][currentCol]) {
                diagonalMove = true;
              }
            }
          }
      }

      // 右方向への斜め移動
      else if(enemyRow == currentRow &&
              enemyCol == currentCol + 1) {
          if(verticalWall[currentRow][currentCol]) {
            return false;
          }
          if(verticalWall[currentRow][currentCol + 1]) {
            if(row == currentRow - 1 && col == currentCol + 1) {
              if(!horizontalWall[currentRow - 1][currentCol + 1]) {
                diagonalMove = true;
              }
            } else if(row == currentRow + 1 && col == currentCol + 1) {
              if(!horizontalWall[currentRow][currentCol + 1]) {
                diagonalMove = true;
              }
            }
          }
      }

      // 左方向への斜め移動
      else if(enemyRow == currentRow &&
              enemyCol == currentCol - 1) {
          if(verticalWall[currentRow][currentCol - 1]) {
            return false;
          }
          if(verticalWall[currentRow][currentCol - 2]){
            if(row == currentRow - 1 && col == currentCol - 1) {
              if(!horizontalWall[currentRow - 1][currentCol - 1]) {
                diagonalMove = true;
              }
            } else if(row == currentRow + 1 && col == currentCol - 1) {
              if(!horizontalWall[currentRow][currentCol - 1]) {
                diagonalMove = true;
              }
            }
          }
      }
      

       if(!jumpMove && !diagonalMove) {
          return false;
        }
      } else {
        return false;
      }

      // 駒の移動
      if(turn == BLACK) {
        blackRow = row;
        blackCol = col;
        turn = WHITE;
      } else {
        whiteRow = row;
        whiteCol = col;
        turn = BLACK;
      }
    return true;
  }

  // 横壁の設置
  public boolean placeHorizontalWall(int row, int col) {

    // 手持ちの壁があるかを確認
    if(turn == BLACK && blackWalls <= 0) {
      return false;
    }
    if(turn == WHITE && whiteWalls <= 0) {
      return false;
    }

    // 設置する場所が盤面内か確認する
    if(row < 0 || row >= 8 || col < 0 || col >= 8) {
      return false;
    }

    // 交差する壁があるか確認
    if(verticalCenter[row][col]) {
      return false;
    }

    // 設置する場所に壁がないか確認
    if(horizontalWall[row][col] || horizontalWall[row][col+1]) {
      return false;
    }

    // 壁の設置
    horizontalWall[row][col] = true;
    horizontalWall[row][col+1] = true;
    horizontalCenter[row][col] = true;

    // 手持ちの壁の枚数の更新と手番交代
    if(turn == BLACK) {
      blackWalls--;
      turn = WHITE;
    } else {
      whiteWalls--;
      turn = BLACK;
    }
    return true;
  }

  // 縦壁の設置
  public boolean placeVerticalWall(int row, int col) {

    // 手持ちの壁があるかを確認
    if(turn == BLACK && blackWalls <= 0) {
      return false;
    }
    if(turn == WHITE && whiteWalls <= 0) {
      return false;
    }

    // 設置する場所が盤面内か確認する
    if(row < 0 || row >= 8 || col < 0 || col >= 8) {
      return false;
    }

    // 交差する壁があるか確認
    if(horizontalCenter[row][col]) {
      return false;
    }

    //設置する場所に壁がないか確認
    if(verticalWall[row][col] || verticalWall[row+1][col]) {
      return false;
    }

    // 壁の設置
    verticalWall[row][col] = true;
    verticalWall[row+1][col] = true;
    verticalCenter[row][col] = true;

    // 手持ちの壁の枚数の更新と手番交代
    if(turn == BLACK) {
      blackWalls--;
      turn = WHITE;
    } else {
      whiteWalls--;
      turn = BLACK;
    }
    return true;
  }

  // 盤面表示
  public void printBoard() {

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

            if (row == blackRow && col == blackCol) {
                cell = 'B';
            } else if (row == whiteRow && col == whiteCol) {
                cell = 'W';
            }

            System.out.print(cell);

            // 縦壁
            if (col < 8) {
                if (row < 9 && verticalWall[row][col]) {
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

            if (horizontalWall[row][col]) {
                System.out.print("+---");
            } else {
                System.out.print("+   ");
            }
        }
        System.out.println("+");
    }

    System.out.println();
    System.out.println("黒の壁: " + blackWalls);
    System.out.println("白の壁: " + whiteWalls);
    System.out.println();
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
