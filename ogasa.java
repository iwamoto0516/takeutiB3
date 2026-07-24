// ogasa.java
//
// 「相手の逃げ道の狭さ」を重視して壁を置く攻撃的プレイヤー。
//
//   基本方針貪欲法（最短経路の差に加えて壁の妨害に価値を付与した）
//  ・自分の最短距離のほうが短ければ、素直にゴールへ進む。
//  ・相手の最短距離が短い（または同じ）なら、相手を最も追い込める壁を置く。
//  ・壁の良し悪しは「相手の距離をどれだけ伸ばすか」だけでなく、
//    「相手の逃げ道をどれだけ狭くするか（ボトルネックの深刻さ・移動自由度の低下）」で評価する。
//  ・置いた結果、相手（や自分）のゴール経路が消える手は、WallPlace が失敗するため自然に除外される。
public class ogasa extends Player {

    public ogasa(int color) {
        super(color);
    }

    @Override
    public boolean play(Board board) {

        PawnMove pawnMove = new PawnMove();
        WallPlace wallPlace = new WallPlace();

        int myRow, myCol, enemyRow, enemyCol;
        int myGoalRow, enemyGoalRow, myWalls;

        if (color == Board.BLACK) {
            myRow = board.blackRow;   myCol = board.blackCol;
            enemyRow = board.whiteRow; enemyCol = board.whiteCol;
            myGoalRow = 8; enemyGoalRow = 0;
            myWalls = board.blackWalls;
        } else {
            myRow = board.whiteRow;   myCol = board.whiteCol;
            enemyRow = board.blackRow; enemyCol = board.blackCol;
            myGoalRow = 0; enemyGoalRow = 8;
            myWalls = board.whiteWalls;
        }

        int myDistance = shortestDistance(board, myRow, myCol, myGoalRow);
        int enemyDistance = shortestDistance(board, enemyRow, enemyCol, enemyGoalRow);

        // 自分のほうが近い、または壁切れなら進む
        if (myDistance < enemyDistance || myWalls <= 0) {
            return moveTowardGoal(board, pawnMove, myGoalRow);
        }

        // 相手を最も「狭く」できる壁を探して置く
        if (placeWallToTrap(board, wallPlace, enemyRow, enemyCol, enemyGoalRow, enemyDistance)) {
            return true;
        }

        // 有効な壁がなければ進む
        return moveTowardGoal(board, pawnMove, myGoalRow);
    }

    // ============================================================
    // 移動：ゴールに一番近づける手を選ぶ
    // ============================================================
    private boolean moveTowardGoal(Board board, PawnMove pawnMove, int goalRow) {

        int currentRow, currentCol;
        if (color == Board.BLACK) { currentRow = board.blackRow; currentCol = board.blackCol; }
        else                      { currentRow = board.whiteRow; currentCol = board.whiteCol; }

        int bestRow = -1, bestCol = -1, bestDistance = Integer.MAX_VALUE;

        for (int row = Math.max(0, currentRow - 2); row <= Math.min(8, currentRow + 2); row++) {
            for (int col = Math.max(0, currentCol - 2); col <= Math.min(8, currentCol + 2); col++) {
                if (row == currentRow && col == currentCol) continue;

                Board trial = cloneBoard(board);
                PawnMove trialMove = new PawnMove();
                if (!trialMove.movePawn(trial, row, col)) continue;

                int distance = shortestDistance(trial, row, col, goalRow);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestRow = row; bestCol = col;
                }
            }
        }

        if (bestRow == -1) return false;
        return pawnMove.movePawn(board, bestRow, bestCol);
    }

    // ============================================================
    // 壁：相手の「逃げ道の狭さ」を最大化する場所を総当たりで探す
    //
    // 各壁候補について、置いた後の盤面で
    //   ・相手のゴールまでの最短距離   (伸びるほど良い)
    //   ・相手経路のボトルネック深刻度 (急所を作れるほど良い)
    //   ・相手駒の移動自由度の低下     (囲めるほど良い)
    // を合成したスコアを計算し、最大の手を選ぶ。
    // WallPlace が成功した手だけを対象にするため、
    // 「相手を完全に閉じ込める反則手」は自動的に除外される。
    // ============================================================
    private boolean placeWallToTrap(Board board, WallPlace wallPlace,
                                    int enemyRow, int enemyCol, int enemyGoalRow,
                                    int currentEnemyDistance) {

        int bestRow = -1, bestCol = -1;
        boolean bestIsHorizontal = true;
        int bestScore = Integer.MIN_VALUE;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                // --- 横壁を試す ---
                Board trialH = cloneBoard(board);
                if (new WallPlace().placeHorizontalWall(trialH, row, col)) {
                    int score = trapScore(trialH, enemyRow, enemyCol, enemyGoalRow, currentEnemyDistance);
                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = row; bestCol = col; bestIsHorizontal = true;
                    }
                }

                // --- 縦壁を試す ---
                Board trialV = cloneBoard(board);
                if (new WallPlace().placeVerticalWall(trialV, row, col)) {
                    int score = trapScore(trialV, enemyRow, enemyCol, enemyGoalRow, currentEnemyDistance);
                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = row; bestCol = col; bestIsHorizontal = false;
                    }
                }
            }
        }

        // 何も相手を追い込めない（スコアが伸びない）なら壁を置かない
        if (bestRow == -1 || bestScore <= 0) return false;

        if (bestIsHorizontal) return wallPlace.placeHorizontalWall(board, bestRow, bestCol);
        else                  return wallPlace.placeVerticalWall(board, bestRow, bestCol);
    }

    // 壁を1枚置いた後の盤面が、相手をどれだけ追い込めているかの合成スコア
    private int trapScore(Board board, int enemyRow, int enemyCol,
                          int enemyGoalRow, int baseEnemyDistance) {

        int dist = shortestDistance(board, enemyRow, enemyCol, enemyGoalRow);
        if (dist == Integer.MAX_VALUE) return Integer.MIN_VALUE; // 経路消滅は不可（本来 WallPlace が弾く）

        // (1) 相手の距離の伸び：素直な妨害効果
        int stretch = (dist - baseEnemyDistance) * 10;

        // (2) 逃げ道の狭さ：相手の最短経路上のボトルネック深刻度
        int bottleneck = bottleneckSeverity(board, enemyRow, enemyCol, enemyGoalRow) * 5;

        // (3) 相手駒の移動自由度の低下（最大4方向、少ないほど加点）
        int mob = (4 - mobility(board, enemyRow, enemyCol)) * 3;

        return stretch + bottleneck + mob;
    }

    // ============================================================
    // 逃げ道の狭さ（1）：移動自由度
    //  相手駒が今いるマスから壁に阻まれず動ける方向数（0〜4）
    // ============================================================
    private int mobility(Board board, int row, int col) {
        int cnt = 0;
        if (row < 8 && !board.horizontalWall[row][col])     cnt++; // 下
        if (row > 0 && !board.horizontalWall[row - 1][col]) cnt++; // 上
        if (col < 8 && !board.verticalWall[row][col])       cnt++; // 右
        if (col > 0 && !board.verticalWall[row][col - 1])   cnt++; // 左
        return cnt;
    }

    // ============================================================
    // 逃げ道の狭さ（2・核心）：ボトルネック深刻度
    //
    //  相手の最短経路を1本復元し、その経路上の各リンク（隣接マス間の通行）を
    //  1本ずつ仮に塞いで最短距離を測り直す。距離が大きく伸びるリンクがあるほど
    //  「そこが急所＝逃げ道が狭い」と判断し、その最大増加量を返す。
    //  塞ぐと経路が消えるリンクは最重要の急所とみなし大きな値を与える。
    // ============================================================
    private int bottleneckSeverity(Board board, int startRow, int startCol, int goalRow) {

        int base = shortestDistance(board, startRow, startCol, goalRow);
        if (base == Integer.MAX_VALUE) return 0;

        int[][] path = recoverShortestPath(board, startRow, startCol, goalRow);
        if (path == null || path.length < 2) return 0;

        int worst = 0;
        for (int i = 0; i + 1 < path.length; i++) {
            int r = path[i][0],  c = path[i][1];
            int nr = path[i + 1][0], nc = path[i + 1][1];

            // このリンクを一時的に塞ぐ
            WallEdge saved = setLink(board, r, c, nr, nc, true);
            int after = shortestDistance(board, startRow, startCol, goalRow);
            restoreLink(board, saved); // 元に戻す

            if (after == Integer.MAX_VALUE) worst = Math.max(worst, 20);
            else                            worst = Math.max(worst, after - base);
        }
        return worst;
    }

    // リンク（隣接する2マス間の通行）を塞ぐ／戻すためのヘルパー
    // Board の壁配列 horizontalWall[r][c] / verticalWall[r][c] を直接操作する。
    private static class WallEdge {
        boolean horizontal; int r; int c; boolean prev;
    }

    // (r,c) と (nr,nc) の間の通行を block する。変更前の値を保持して返す。
    private WallEdge setLink(Board board, int r, int c, int nr, int nc, boolean block) {
        WallEdge e = new WallEdge();
        if (nr == r + 1 && nc == c) {           // 下方向
            e.horizontal = true;  e.r = r;      e.c = c;
        } else if (nr == r - 1 && nc == c) {    // 上方向
            e.horizontal = true;  e.r = r - 1;  e.c = c;
        } else if (nc == c + 1 && nr == r) {    // 右方向
            e.horizontal = false; e.r = r;      e.c = c;
        } else {                                // 左方向
            e.horizontal = false; e.r = r;      e.c = c - 1;
        }
        if (e.horizontal) { e.prev = board.horizontalWall[e.r][e.c]; board.horizontalWall[e.r][e.c] = block; }
        else              { e.prev = board.verticalWall[e.r][e.c];   board.verticalWall[e.r][e.c]   = block; }
        return e;
    }

    private void restoreLink(Board board, WallEdge e) {
        if (e.horizontal) board.horizontalWall[e.r][e.c] = e.prev;
        else              board.verticalWall[e.r][e.c]   = e.prev;
    }

    // 最短経路を1本だけ復元する（BFS で距離マップを作り、ゴールから逆にたどる）
    private int[][] recoverShortestPath(Board board, int startRow, int startCol, int goalRow) {

        int[][] dist = new int[9][9];
        for (int[] row : dist) java.util.Arrays.fill(row, -1);

        int[] qr = new int[81], qc = new int[81];
        int head = 0, tail = 0;
        dist[startRow][startCol] = 0;
        qr[tail] = startRow; qc[tail] = startCol; tail++;

        int gr = -1, gc = -1;
        while (head < tail) {
            int row = qr[head], col = qc[head]; head++;
            if (row == goalRow) { gr = row; gc = col; break; }

            if (row < 8 && dist[row + 1][col] < 0 && !board.horizontalWall[row][col]) {
                dist[row + 1][col] = dist[row][col] + 1; qr[tail] = row + 1; qc[tail] = col; tail++;
            }
            if (row > 0 && dist[row - 1][col] < 0 && !board.horizontalWall[row - 1][col]) {
                dist[row - 1][col] = dist[row][col] + 1; qr[tail] = row - 1; qc[tail] = col; tail++;
            }
            if (col < 8 && dist[row][col + 1] < 0 && !board.verticalWall[row][col]) {
                dist[row][col + 1] = dist[row][col] + 1; qr[tail] = row; qc[tail] = col + 1; tail++;
            }
            if (col > 0 && dist[row][col - 1] < 0 && !board.verticalWall[row][col - 1]) {
                dist[row][col - 1] = dist[row][col] + 1; qr[tail] = row; qc[tail] = col - 1; tail++;
            }
        }

        if (gr < 0) return null;

        // ゴールから、距離が1小さい隣接マスへ逆にたどる
        java.util.List<int[]> rev = new java.util.ArrayList<>();
        int r = gr, c = gc;
        rev.add(new int[]{r, c});
        while (!(r == startRow && c == startCol)) {
            boolean moved = false;
            // 下から来た？（上のマス r-1 が dist-1）
            if (r > 0 && dist[r - 1][c] == dist[r][c] - 1 && !board.horizontalWall[r - 1][c]) {
                r = r - 1; moved = true;
            } else if (r < 8 && dist[r + 1][c] == dist[r][c] - 1 && !board.horizontalWall[r][c]) {
                r = r + 1; moved = true;
            } else if (c > 0 && dist[r][c - 1] == dist[r][c] - 1 && !board.verticalWall[r][c - 1]) {
                c = c - 1; moved = true;
            } else if (c < 8 && dist[r][c + 1] == dist[r][c] - 1 && !board.verticalWall[r][c]) {
                c = c + 1; moved = true;
            }
            if (!moved) break; // 念のため（通常起きない）
            rev.add(new int[]{r, c});
        }

        // start -> goal の順に並べ替え
        int[][] path = new int[rev.size()][2];
        for (int i = 0; i < rev.size(); i++) path[i] = rev.get(rev.size() - 1 - i);
        return path;
    }

    // ============================================================
    // BFS 最短距離（壁のみ考慮し相手駒は無視）
    // ============================================================
    private int shortestDistance(Board board, int startRow, int startCol, int goalRow) {

        boolean[][] visited = new boolean[9][9];
        int[] queueRow = new int[81], queueCol = new int[81], queueDist = new int[81];
        int head = 0, tail = 0;

        visited[startRow][startCol] = true;
        queueRow[tail] = startRow; queueCol[tail] = startCol; queueDist[tail] = 0; tail++;

        while (head < tail) {
            int row = queueRow[head], col = queueCol[head], dist = queueDist[head];
            head++;

            if (row == goalRow) return dist;

            if (row < 8 && !visited[row + 1][col] && !board.horizontalWall[row][col]) {
                visited[row + 1][col] = true;
                queueRow[tail] = row + 1; queueCol[tail] = col; queueDist[tail] = dist + 1; tail++;
            }
            if (row > 0 && !visited[row - 1][col] && !board.horizontalWall[row - 1][col]) {
                visited[row - 1][col] = true;
                queueRow[tail] = row - 1; queueCol[tail] = col; queueDist[tail] = dist + 1; tail++;
            }
            if (col < 8 && !visited[row][col + 1] && !board.verticalWall[row][col]) {
                visited[row][col + 1] = true;
                queueRow[tail] = row; queueCol[tail] = col + 1; queueDist[tail] = dist + 1; tail++;
            }
            if (col > 0 && !visited[row][col - 1] && !board.verticalWall[row][col - 1]) {
                visited[row][col - 1] = true;
                queueRow[tail] = row; queueCol[tail] = col - 1; queueDist[tail] = dist + 1; tail++;
            }
        }
        return Integer.MAX_VALUE;
    }

    // ============================================================
    // 盤面コピー
    // ============================================================
    private Board cloneBoard(Board board) {
        Board copy = new Board();
        copy.blackRow = board.blackRow;   copy.blackCol = board.blackCol;
        copy.whiteRow = board.whiteRow;   copy.whiteCol = board.whiteCol;
        copy.blackWalls = board.blackWalls; copy.whiteWalls = board.whiteWalls;
        copy.turn = board.turn;
        copy.horizontalWall   = copyArray(board.horizontalWall);
        copy.verticalWall     = copyArray(board.verticalWall);
        copy.horizontalCenter = copyArray(board.horizontalCenter);
        copy.verticalCenter   = copyArray(board.verticalCenter);
        return copy;
    }

    private boolean[][] copyArray(boolean[][] source) {
        boolean[][] dest = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) dest[i] = source[i].clone();
        return dest;
    }
}
