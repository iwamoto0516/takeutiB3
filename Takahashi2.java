import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Takahashi2 extends Player {
    private Random random = new Random();
    // 探索の深さ：2手
    private final int MAX_DEPTH = 2; 
    //継承
    public Takahashi2(int color) {
        super(color);
    }

    @Override
    public boolean play(Board board) {
        PawnMove pawnMove = new PawnMove();

        //自分の手番と現在の自分と相手の位置の取得（条件式 ? trueの場合 : falseの場合） 
        int myRow        = (color == Board.BLACK) ? board.blackRow   : board.whiteRow;
        int myCol        = (color == Board.BLACK) ? board.blackCol   : board.whiteCol;
        int enemyRow     = (color == Board.BLACK) ? board.whiteRow   : board.blackRow;
        int enemyCol     = (color == Board.BLACK) ? board.whiteCol   : board.blackCol;
        int myGoalRow    = (color == Board.BLACK) ? 8 : 0;
        int enemyGoalRow = (color == Board.BLACK) ? 0 : 8;
        int myWalls      = (color == Board.BLACK) ? board.blackWalls : board.whiteWalls;
        //最短経路の取得
        int myDist = shortestDis(board, myRow, myCol, myGoalRow);
        int enemyDist = shortestDis(board, enemyRow, enemyCol, enemyGoalRow);

        //自分と相手の距離が同じ場合、ミニマックス（αβ枝切り）で最善手を選ぶ
        if (myDist == enemyDist) {
            List<Move> moves = generateMoves(board, color);
            Move bestMove = null;
            int bestValue = Integer.MIN_VALUE;

            //αβ枝切りの初期値
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;

            for (Move move : moves) {
                //盤面を生成
                Board clone = cloneBoard(board);
                //自分の番
                executeMove(clone, move);
                //相手の番を終えた後、評価
                int value = alphaBeta(clone, MAX_DEPTH - 1, alpha, beta, false);

                //評価が良い場合、更新
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
                alpha = Math.max(alpha, value);
            }

            //最も良い評価の手を実行
            if (bestMove != null) {
                return executeMove(board, bestMove);
            }
        }

        //距離に差がある場合やミニマックスが手を選ばなかった場合、最短経路方向へ移動
        if (myDist <= enemyDist || myWalls <= 0) {
            if (moveToShortestPath(board, pawnMove, myRow, myCol, myGoalRow)) {
                return true;
            }
        }
        //板を持っている場合、邪魔な位置に板を置く
        if (myWalls > 0) {
            if (placeBestBlockingWall(board, enemyRow, enemyCol, enemyGoalRow, enemyDist)) {
                return true;
            }
        }
        //最短経路がある場合、最短経路方向へ移動
        if (moveToShortestPath(board, pawnMove, myRow, myCol, myGoalRow)) {
            return true;
        }

        //最短経路が見つからない場合、ランダムに移動
        while (true) {                
            int direction = random.nextInt(4);
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            
            switch (direction) {
                case 0: // 上
                    row = myRow - 1;
                    col = myCol;
                    break;

                case 1: // 下
                    row = myRow + 1;
                    col = myCol;
                    break;

                case 2: // 左
                    row = myRow;
                    col = myCol - 1;
                    break;

                default: // 右
                    row = myRow;
                    col = myCol + 1;
                    break;
                }

            if (pawnMove.movePawn(board, row, col)) {
                return true;
            }
        }
    }

    //αβ枝切り付きミニマックス
    private int alphaBeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        //終局、または規定の深さに達したら盤面を評価
        if (depth == 0 || isGameOver(board)) {
            return evaluate(board);
        }
        //自分の番か相手の番かを取得
        int currentColor = maximizingPlayer ? this.color : 
                (this.color == Board.BLACK ? Board.WHITE : Board.BLACK);
        //現在の盤面で可能な行動すべてをリスト化
        List<Move> moves = generateMoves(board, currentColor);
        //移動先と置く壁がない場合、現在の盤面の点数を返す
        if (moves.isEmpty()) return evaluate(board);

        //マックスプレイヤ（自分、点数が高い手を選択）
        if (maximizingPlayer) {
            //マックスミニ値を－無限大で初期化
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                //盤面コピー
                Board clone = cloneBoard(board);
                //一手進める
                executeMove(clone, move);
                int eval = alphaBeta(clone, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; //β値（最大）がα値（最小）より大きくなったので、枝刈り
            }
            return maxEval;
        } 
        //ミニマムプレイヤ（相手、点数が低い手を選択）
        else {
            //ミニマックス値を無限大で初期化
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board clone = cloneBoard(board);
                executeMove(clone, move);
                int eval = alphaBeta(clone, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; //α値がβ値より小さくなったので、枝刈り
            }
            return minEval;
        }
    }

    //評価関数（スコアが高いほど良い）
    private int evaluate(Board board) {
        int myRow = (this.color == Board.BLACK) ? board.blackRow : board.whiteRow;
        int myCol = (this.color == Board.BLACK) ? board.blackCol : board.whiteCol;
        int enemyRow = (this.color == Board.BLACK) ? board.whiteRow : board.blackRow;
        int enemyCol = (this.color == Board.BLACK) ? board.whiteCol : board.blackCol;
        int myGoalRow = (this.color == Board.BLACK) ? 8 : 0;
        int enemyGoalRow = (this.color == Board.BLACK) ? 0 : 8;
        int myDist = shortestDis(board, myRow, myCol, myGoalRow);
        int enemyDist = shortestDis(board, enemyRow, enemyCol, enemyGoalRow);

        //完全に閉じ込めらた場合の判定（反則の手）
        if (myDist == Integer.MAX_VALUE) return Integer.MIN_VALUE + 100; //最低点を返す
        if (enemyDist == Integer.MAX_VALUE) return Integer.MAX_VALUE - 100; //最高点を返す

        //敵の残り距離が長く、自分の残り距離が短いほど高得点 (重み10倍)
        int score = (enemyDist - myDist) * 10;

        //半分より進んでいるか判定（進んでいる場合、重みを足す）
        boolean iAmHalf = (this.color == Board.BLACK ? myRow >= 4 : myRow <= 4);
        boolean enemyHalf = (this.color == Board.BLACK ? enemyRow <= 4 : enemyRow >= 4);
        if (enemyHalf) score += 3;
        if (iAmHalf) score += 3;

        //自分も相手も板がすべて無くなった場合の判定
        if (board.blackWalls == 0 && board.whiteWalls == 0) {
            if (myDist < enemyDist) {
                return Integer.MAX_VALUE - 100; //自分が確実に勝つので最高点を返す
            } else if (myDist > enemyDist) {
                return Integer.MIN_VALUE + 100; //自分が確実に負けるので最低点を返す
            }
        }

        return score;
    }

    //ゲームが終わっているか判定
    private boolean isGameOver(Board board) {
        return board.blackRow == 8 || board.whiteRow == 0;
    }

    //仮想の盤面で1手進める
    private boolean executeMove(Board board, Move move) {
        //コマを移動
        if (move.isPawnMove) {
            PawnMove pm = new PawnMove();
            return pm.movePawn(board, move.r, move.c);
        } 
        //板を置く
        else {
            WallPlace wp = new WallPlace();
            if (move.isHorizontal) return wp.placeHorizontalWall(board, move.r, move.c);
            else return wp.placeVerticalWall(board, move.r, move.c);
        }
    }

    //手の候補生成
    private class Move {
        boolean isPawnMove;
        boolean isHorizontal;
        int r, c;
    }
    private List<Move> generateMoves(Board board, int playerColor) {
        //手を保存するリストを生成
        List<Move> list = new ArrayList<>();
        PawnMove pm = new PawnMove();
        WallPlace wp = new WallPlace();
        //現在のプレイヤの位置を取得
        int pRow = (playerColor == Board.BLACK) ? board.blackRow : board.whiteRow;
        int pCol = (playerColor == Board.BLACK) ? board.blackCol : board.whiteCol;
        
        // 1. 移動候補の生成（プレイヤーの周囲2マスのみ）
        int startR = Math.max(0, pRow - 2);
        int endR = Math.min(8, pRow + 2);
        int startC = Math.max(0, pCol - 2);
        int endC = Math.min(8, pCol + 2);
        //使い回し用の下見盤面を作成
        Board checkBoard = cloneBoard(board);

        for (int r = startR; r <= endR; r++) {
            for (int c = startC; c <= endC; c++) {
                //移動を試す前に、下見盤面を今の本番の状況にリセットする
                checkBoard.blackRow = board.blackRow;
                checkBoard.blackCol = board.blackCol;
                checkBoard.whiteRow = board.whiteRow;
                checkBoard.whiteCol = board.whiteCol;
                //使い回し盤面で移動できるか確認する
                if (pm.movePawn(checkBoard, r, c)) {
                    Move m = new Move();
                    m.isPawnMove = true;
                    m.r = r;
                    m.c = c;
                    list.add(m); //成功したらリストに追加
                }
            }
        }
        
        // 2. 壁候補の生成（板が残っている場合のみ）
        int walls = (playerColor == Board.BLACK) ? board.blackWalls : board.whiteWalls;
        if (walls > 0) {
            //相手の最短経路方向を取得
            int eRow = (playerColor == Board.BLACK) ? board.whiteRow : board.blackRow;
            int eCol = (playerColor == Board.BLACK) ? board.whiteCol : board.blackCol;
            int eGoalRow = (playerColor == Board.BLACK) ? 0 : 8;
            int targetR = eRow;

            if(eGoalRow == 0 && eRow > 0) targetR = eRow - 1; //相手が白
            if(eGoalRow == 8 && eRow < 8) targetR = eRow + 1; //相手が黒

            //コマの周辺4マス以内のみ探索
            int minR = Math.max(0, targetR - 1);
            int maxR = Math.min(7, targetR + 1);
            int minC = Math.max(0, eCol - 1);
            int maxC = Math.min(7, eCol + 1);
            //使い回し用の下見盤面を作成
            Board wallCheckBoard = cloneBoard(board);

            for (int r = minR; r <= maxR; r++) {
                for (int c = minC; c <= maxC; c++) {
                    //横壁の下見
                    if (wp.placeHorizontalWall(wallCheckBoard, r, c)) {
                        Move m = new Move();
                        m.isPawnMove = false;
                        m.isHorizontal = true;
                        m.r = r;
                        m.c = c;
                        list.add(m);
                        
                        //設置できた場合、次の下見のためにその壁を消去してリセットする
                        wallCheckBoard.horizontalWall[r][c] = false;
                        if (c < 8) wallCheckBoard.horizontalWall[r][c + 1] = false;
                    }
                    
                    //縦壁の下見
                    if (wp.placeVerticalWall(wallCheckBoard, r, c)) {
                        Move m = new Move();
                        m.isPawnMove = false;
                        m.isHorizontal = false;
                        m.r = r;
                        m.c = c;
                        list.add(m);//設置できた場合、次の下見のためにその壁を消去してリセットする
                        wallCheckBoard.verticalWall[r][c] = false;
                        if (r < 8) wallCheckBoard.verticalWall[r + 1][c] = false;
                    }
                }
            }
        }
        return list;
    }
    
    //最短経路方向に進む
    private boolean moveToShortestPath(Board board, PawnMove pawnMove, int myRow, int myCol, int goalRow) {
        int bestRow = -1;
        int bestCol = -1;
        int minNextDist = Integer.MAX_VALUE;
        //自分の周囲2マスのみ探索
        int startR = Math.max(0, myRow - 2);
        int endR = Math.min(8, myRow + 2);
        int startC = Math.max(0, myCol - 2);
        int endC = Math.min(8, myCol + 2);

        for (int r = startR; r <= endR; r++) {
            for (int c = startC; c <= endC; c++) {
                Board clone = cloneBoard(board);
                //移動できるか判定
                if (pawnMove.movePawn(clone, r, c)) {
                    //移動後の盤面からゴールまでの距離
                    int dist = shortestDis(clone, r, c, goalRow);
                    if (dist < minNextDist) {
                        minNextDist = dist;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }
        //最適な移動先が見つかれば移動
        if (bestRow != -1) {
            return pawnMove.movePawn(board, bestRow, bestCol);
        }
        return false;
    }

    //相手の最短経路の距離を引き延ばせる位置に設置
    private boolean placeBestBlockingWall(Board board, int enemyRow, int enemyCol, int enemyGoalRow, int currentEnemyDist) {
        WallPlace wallPlace = new WallPlace();
        int maxEnemyDist = currentEnemyDist;
        int bestRow = -1;
        int bestCol = -1;
        boolean isHorizontal = true;
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) { 
                //横壁を試す
                Board cloneH = cloneBoard(board);
                if (wallPlace.placeHorizontalWall(cloneH, r, c)) {
                    int dist = shortestDis(cloneH, enemyRow, enemyCol, enemyGoalRow);
                    //完全に道を塞いでいない（Integer.MAX_VALUE未満）かつ、これまでで最も距離が伸びる場合
                    if (dist > maxEnemyDist && dist != Integer.MAX_VALUE) {
                        maxEnemyDist = dist;
                        bestRow = r;
                        bestCol = c;
                        isHorizontal = true;
                    }
                }
                
                //縦壁を試す
                Board cloneV = cloneBoard(board);
                if (wallPlace.placeVerticalWall(cloneV, r, c)) {
                    int dist = shortestDis(cloneV, enemyRow, enemyCol, enemyGoalRow);
                    if (dist > maxEnemyDist && dist != Integer.MAX_VALUE) {
                        maxEnemyDist = dist;
                        bestRow = r;
                        bestCol = c;
                        isHorizontal = false;
                    }
                }
            }
        }

        //最も相手を遅らせる壁が見つかれば設置
        if (bestRow != -1) {
            if (isHorizontal) {
                return wallPlace.placeHorizontalWall(board, bestRow, bestCol);
            } else {
                return wallPlace.placeVerticalWall(board, bestRow, bestCol);
            }
        }
        return false; 
    }

    //BFSで最短経路探索（板のみ考慮）
    private int shortestDis(Board board, int startRow, int startCol, int goalRow) {
        boolean[][] visited = new boolean[9][9]; 

        int[] queueRow = new int[81];
        int[] queueCol = new int[81];
        int[] queueDist = new int[81];
        int head = 0;
        int tail = 0;

        visited[startRow][startCol] = true;

        queueRow[tail] = startRow;
        queueCol[tail] = startCol;
        queueDist[tail] = 0;
        tail++;

        while (head < tail) {
            int row = queueRow[head];
            int col = queueCol[head];
            int dist = queueDist[head];
            head++;

            if (row == goalRow) return dist;

            // 1. 下への移動（rowが8未満、未訪問、移動先に横壁がない）
            if (row < 8 && !visited[row + 1][col] && !board.horizontalWall[row][col]){
                visited[row + 1][col] = true; 
                queueRow[tail] = row + 1;
                queueCol[tail] = col;
                queueDist[tail] = dist + 1;
                tail++;
            } 
            // 2. 上への移動（rowが0より大きい、未訪問、移動先に横壁がない）
            if (row > 0 && !visited[row - 1][col] && !board.horizontalWall[row - 1][col]){
                visited[row - 1][col] = true;
                queueRow[tail] = row - 1;
                queueCol[tail] = col;
                queueDist[tail] = dist + 1;
                tail++;
            } 
            // 3. 右への移動（colが8未満、未訪問、移動先に縦壁がない）
            if (col < 8 && !visited[row][col + 1] && !board.verticalWall[row][col]){
                visited[row][col + 1] = true; 
                queueRow[tail] = row;
                queueCol[tail] = col + 1;
                queueDist[tail] = dist + 1;
                tail++;
            } 
            // 4. 左への移動（colが0より大きい、未訪問、移動先に縦壁がない）
            if (col > 0 && !visited[row][col - 1] && !board.verticalWall[row][col - 1]){
                visited[row][col - 1] = true; 
                queueRow[tail] = row;
                queueCol[tail] = col - 1;
                queueDist[tail] = dist + 1;
                tail++;
            } 
        }
        return Integer.MAX_VALUE;
    }

    //盤面のコピーを作成
    private boolean[][] copyArray(boolean[][] source) {
        boolean[][] dest = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) dest[i] = source[i].clone();
        return dest;
    }
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
}
