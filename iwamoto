// 最短距離(BFS)をもとに行動するプレイヤー
// ・自分の最短距離の方が短ければ移動する
// ・相手の最短距離の方が短い(または同じ)場合は、相手の最短距離を一番伸ばせる壁を置く
// ・有効な壁が見つからない場合は移動する

import java.util.ArrayList;

public class Iwamoto extends Player {

    // 自分の色を取得
    public Iwamoto(int color) {
        super(color);
    }

    @Override
    public boolean play(Board board) {
        
        ArrayList<Move> moves = generateMoves(board);

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (Move move : moves) {

        Board next = cloneBoard(board);
        applyMove(next, move);

        int score = alphaBeta(next,
                            2,
                            Integer.MIN_VALUE,
                            Integer.MAX_VALUE,
                            false);

        if (score > bestScore) {
            bestScore = score;
            bestMove = move;
        }
    }

    if (bestMove == null) {
        return false;
    }

    applyMove(board, bestMove);
    return true;
 }

    // 現在位置から移動できるマスをすべて試し、ゴールまでの距離が一番縮まる手を選んで移動する
    private boolean moveTowardGoal(Board board, PawnMove pawnMove, int goalRow) {

        int currentRow;
        int currentCol;

        if (color == Board.BLACK) {
            currentRow = board.blackRow;
            currentCol = board.blackCol;
        } else {
            currentRow = board.whiteRow;
            currentCol = board.whiteCol;
        }

        int bestRow = -1;
        int bestCol = -1;
        int bestDistance = Integer.MAX_VALUE;

        // 通常移動・ジャンプ・斜め移動の可能性がある範囲を全探索
        // 現在の位置から前後左右2マス以内の範囲をすべて調べる
        // min,maxをそれぞれ8,0でとることで範囲外まで調べるのを防ぐ
        for (int row = Math.max(0, currentRow - 2); row <= Math.min(8, currentRow + 2); row++) {
            for (int col = Math.max(0, currentCol - 2); col <= Math.min(8, currentCol + 2); col++) {

                if (row == currentRow && col == currentCol) {
                    continue;
                }

                Board trial = cloneBoard(board); // 盤面を複製
                PawnMove trialMove = new PawnMove();

                if (!trialMove.movePawn(trial, row, col)) { // その移動が合法か試す
                    continue;
                }

                int distance = shortestDistance(trial, row, col, goalRow); // 移動後の距離

                if (distance < bestDistance) { // 一番近くなる手だった場合記録
                    bestDistance = distance;
                    bestRow = row;
                    bestCol = col;
                }
            }
        }

        if (bestRow == -1) {
            return false; // 動けるマスがない
        }

        return pawnMove.movePawn(board, bestRow, bestCol); // 実際の盤面に反映
    }

    // 相手の最短距離を一番伸ばせる壁を探して設置する
    // 8x8=64箇所全ての「横壁」「縦壁」の置き場所を総当たり
    private boolean placeWallToBlock(Board board, WallPlace wallPlace,
                                      int enemyRow, int enemyCol, int enemyGoalRow,
                                      int currentEnemyDistance) {

        int bestRow = -1;
        int bestCol = -1;
        boolean bestIsHorizontal = true;
        int bestEnemyDistance = currentEnemyDistance;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                // 横壁を試す
                Board trialH = cloneBoard(board);
                WallPlace trialWallPlaceH = new WallPlace();

                if (trialWallPlaceH.placeHorizontalWall(trialH, row, col)) {
                    int distance = shortestDistance(trialH, enemyRow, enemyCol, enemyGoalRow);
                    if (distance > bestEnemyDistance) { // 一番遠くなるての場合、記録
                        bestEnemyDistance = distance;
                        bestRow = row;
                        bestCol = col;
                        bestIsHorizontal = true;
                    }
                }

                // 縦壁を試す
                Board trialV = cloneBoard(board);
                WallPlace trialWallPlaceV = new WallPlace();

                if (trialWallPlaceV.placeVerticalWall(trialV, row, col)) {
                    int distance = shortestDistance(trialV, enemyRow, enemyCol, enemyGoalRow);
                    if (distance > bestEnemyDistance) {
                        bestEnemyDistance = distance;
                        bestRow = row;
                        bestCol = col;
                        bestIsHorizontal = false;
                    }
                }
            }
        }

        if (bestRow == -1) {
            return false; // 相手の距離を伸ばせる壁がない
        }

        if (bestIsHorizontal) {
            return wallPlace.placeHorizontalWall(board, bestRow, bestCol);
        } else {
            return wallPlace.placeVerticalWall(board, bestRow, bestCol);
        }
    }

    // BFSで指定した駒からゴール行までの最短距離を求める(壁のみを考慮し、相手の駒は無視する)
    private int shortestDistance(Board board, int startRow, int startCol, int goalRow) {

        boolean[][] visited = new boolean[9][9]; // もう調べたかを記録
        // 配列をキューとして使っている(9x9=81マス)
        int[] queueRow = new int[81]; // 行
        int[] queueCol = new int[81]; // 列
        int[] queueDist = new int[81]; // その地点までの距離
        int head = 0;
        int tail = 0;

        visited[startRow][startCol] = true; // スタート地点を訪問済みにする
        // スタート地点の座標とそこまでの距離0をキューに入れる
        queueRow[tail] = startRow;
        queueCol[tail] = startCol;
        queueDist[tail] = 0;
        tail++; // tailを一つ進めて、次に追加する場所を進めておく

        while (head < tail) { // キューが空になるまで

            // キューの先頭から行・列・距離を取り出す
            int row = queueRow[head];
            int col = queueCol[head];
            int dist = queueDist[head];
            head++; // 取り出したことにし、次回は次の要素を見る

            if (row == goalRow) {
                return dist;
            }

            // 下
            // 盤面の下端を超えていないかつ、そのマスがまだ未訪問かつ、下方向に壁がない場合調べる
            if (row < 8 && !visited[row + 1][col] && !board.horizontalWall[row][col]) {
                visited[row + 1][col] = true; // そのマスを訪問済みとする
                // 「行+1、列そのまま、距離+1」としてキューに追加する
                queueRow[tail] = row + 1;
                queueCol[tail] = col;
                queueDist[tail] = dist + 1;
                tail++;
            }

            // 上
            if (row > 0 && !visited[row - 1][col] && !board.horizontalWall[row - 1][col]) {
                visited[row - 1][col] = true;
                queueRow[tail] = row - 1;
                queueCol[tail] = col;
                queueDist[tail] = dist + 1;
                tail++;
            }

            // 右
            if (col < 8 && !visited[row][col + 1] && !board.verticalWall[row][col]) {
                visited[row][col + 1] = true;
                queueRow[tail] = row;
                queueCol[tail] = col + 1;
                queueDist[tail] = dist + 1;
                tail++;
            }

            // 左
            if (col > 0 && !visited[row][col - 1] && !board.verticalWall[row][col - 1]) {
                visited[row][col - 1] = true;
                queueRow[tail] = row;
                queueCol[tail] = col - 1;
                queueDist[tail] = dist + 1;
                tail++;
            }
        }

        return Integer.MAX_VALUE; // 経路なし
    }

    // 盤面の状態をコピーする(仮の手を試すために使う)
    private Board cloneBoard(Board board) {

        Board copy = new Board();

        copy.blackRow = board.blackRow;
        copy.blackCol = board.blackCol;
        copy.whiteRow = board.whiteRow;
        copy.whiteCol = board.whiteCol;
        copy.blackWalls = board.blackWalls;
        copy.whiteWalls = board.whiteWalls;
        copy.turn = board.turn;
        copy.horizontalWall = copyArray(board.horizontalWall);
        copy.verticalWall = copyArray(board.verticalWall);
        copy.horizontalCenter = copyArray(board.horizontalCenter);
        copy.verticalCenter = copyArray(board.verticalCenter);

        return copy;
    }

    private boolean[][] copyArray(boolean[][] source) {
        boolean[][] dest = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            dest[i] = source[i].clone();
        }
        return dest;
    }
    
    //  追加分
    private int evaluate(Board board) {

    int myRow,myCol,myGoal;
    int enemyRow,enemyCol,enemyGoal;

    if(color==Board.BLACK){
        myRow=board.blackRow;
        myCol=board.blackCol;
        myGoal=8;

        enemyRow=board.whiteRow;
        enemyCol=board.whiteCol;
        enemyGoal=0;
    }else{
        myRow=board.whiteRow;
        myCol=board.whiteCol;
        myGoal=0;

        enemyRow=board.blackRow;
        enemyCol=board.blackCol;
        enemyGoal=8;
    }

    int myDist = shortestDistance(board,myRow,myCol,myGoal);
    int enemyDist = shortestDistance(board,enemyRow,enemyCol,enemyGoal);

    int score = (enemyDist - myDist) * 100;

    if (color == Board.BLACK) {
    score += (board.blackWalls - board.whiteWalls) * 5;
    } else {
    score += (board.whiteWalls - board.blackWalls) * 5;
    }

    return score;
 }

 class Move {
    boolean wall;
    boolean horizontal;
    int row;
    int col;
 }

 private int alphaBeta(Board board,
                      int depth,
                      int alpha,
                      int beta,
                      boolean maximizing){


    int winner = board.getWinner();

    if (winner != 0) {
        if (winner == color) {
            return 1000000;
        } else {
            return -1000000;
        }
    }
    if(depth==0){
        return evaluate(board);
    }

    if(maximizing){

        int value = Integer.MIN_VALUE;
        ArrayList<Move> moves = generateMoves(board);
        
        if (moves.isEmpty()) {
            return evaluate(board);
        }

        // 全合法手
        for(Move move : moves){

            Board next = cloneBoard(board);
            applyMove(next, move);

            value = Math.max(value,
                    alphaBeta(next,
                              depth-1,
                              alpha,
                              beta,
                              false));

            alpha = Math.max(alpha,value);

            if(alpha>=beta){
                break;
            }
        }

        return value;
    }
    else{
        int value = Integer.MAX_VALUE;
        ArrayList<Move> moves = generateMoves(board);

        if (moves.isEmpty()) {
            return evaluate(board);
        }

        for (Move move : moves) {

        Board next = cloneBoard(board);
        applyMove(next, move);

        value = Math.min(value,
            alphaBeta(next,
                      depth - 1,
                      alpha,
                      beta,
                      true));

    beta = Math.min(beta, value);

    if (alpha >= beta) {
        break;
    }
  }

     

        return value;
    }
 }
 
  private ArrayList<Move> generateMoves(Board board) {

    ArrayList<Move> moves = new ArrayList<>();

    int currentRow, currentCol;

    if (board.turn == Board.BLACK) {
        currentRow = board.blackRow;
        currentCol = board.blackCol;
    } else {
        currentRow = board.whiteRow;
        currentCol = board.whiteCol;
    }

    // ===== 駒移動 =====
    for (int row = Math.max(0, currentRow - 2);
         row <= Math.min(8, currentRow + 2);
         row++) {

        for (int col = Math.max(0, currentCol - 2);
             col <= Math.min(8, currentCol + 2);
             col++) {

            if (row == currentRow && col == currentCol)
                continue;

            Board trial = cloneBoard(board);
            PawnMove pawn = new PawnMove();

            if (pawn.movePawn(trial, row, col)) {

                Move move = new Move();
                move.wall = false;
                move.row = row;
                move.col = col;

                moves.add(move);
            }
        }
    }
    int walls;

    if (board.turn == Board.BLACK) {
        walls = board.blackWalls;
    } else {
        walls = board.whiteWalls;
    }

    if(walls > 0){
    // ===== 横壁 =====
    for (int row = 0; row < 8; row++) {
        for (int col = 0; col < 8; col++) {

            Board trial = cloneBoard(board);
            WallPlace wall = new WallPlace();

            if (wall.placeHorizontalWall(trial, row, col)) {

                Move move = new Move();
                move.wall = true;
                move.horizontal = true;
                move.row = row;
                move.col = col;

                moves.add(move);
            }
        }
    }

    // ===== 縦壁 =====
    for (int row = 0; row < 8; row++) {
        for (int col = 0; col < 8; col++) {

            Board trial = cloneBoard(board);
            WallPlace wall = new WallPlace();

            if (wall.placeVerticalWall(trial, row, col)) {

                Move move = new Move();
                move.wall = true;
                move.horizontal = false;
                move.row = row;
                move.col = col;

                moves.add(move);
            }
        }
    }
  }
    return moves;
 }


 private void applyMove(Board board, Move move) {

    if (move.wall) {

        WallPlace wall = new WallPlace();

        if (move.horizontal) {
            wall.placeHorizontalWall(board, move.row, move.col);
        } else {
            wall.placeVerticalWall(board, move.row, move.col);
        }

    } else {

        PawnMove pawn = new PawnMove();
        pawn.movePawn(board, move.row, move.col);

    }
 }
}
