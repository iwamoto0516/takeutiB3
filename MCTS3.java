import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Collections;

class MCTS3Node {
    Board state;
    MCTS3Node parent;
    List<MCTS3Node> children = new ArrayList<>();

    double w = 0.0;   // 累計価値
    int n = 0;        // 試行回数

    int moveType;     // 0:移動, 1:横壁, 2:縦壁
    int moveR, moveC; // 手の内容

    int playerToMove; // この局面で手番

    boolean isExpanded = false;

    MCTS3Node(Board state, MCTS3Node parent) {
        this.state = state;
        this.parent = parent;
        this.playerToMove = state.turn;
    }

    @Override
    public String toString() {
        return "MCTS3Node{" +
               "w=" + w +
               ", n=" + n +
               ", moveType=" + moveType +
               ", moveR=" + moveR +
               ", moveC=" + moveC +
               '}';
    }
}

public class MCTS3 {

    private static final double C_UCB = 1.4;

    private PawnMove pawnMove = new PawnMove();
    private WallPlace wallPlace = new WallPlace();

    // --- Masuya ラッパー ---
    private Masuya2 helper = new Masuya2(Board.BLACK);

    private List<int[]> getLegalPawnMoves(Board board) {
        return helper.getLegalPawnMoves(board);
    }

    private List<int[]> getLegalHorizontalWalls(Board board) {
        return helper.getLegalHorizontalWalls(board);
    }

    private List<int[]> getLegalVerticalWalls(Board board) {
        return helper.getLegalVerticalWalls(board);
    }

    private int shortestPath(Board board, int color) {
        return helper.shortestPath(board, color);
    }

    private boolean canMove(Board board, int r, int c, int nr, int nc) {
        return helper.canMove(board, r, c, nr, nc);
    }

    // --- UCB1 による子ノード選択 ---
    private MCTS3Node selectChild(MCTS3Node node) {
        MCTS3Node best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double lnT = Math.log(Math.max(1, node.n));

        for (MCTS3Node child : node.children) {

            if (child.n == 0) {
                return child;   // 未訪問の子は即選択
            }

            double q = child.w / child.n;
            double u = C_UCB * Math.sqrt(lnT / child.n);
            double score = q + u;
            if (score > bestScore) { 
                bestScore = score;
                best = child;
            }
        }
        return best;
    }

    // --- 展開：方針に応じて移動だけ or 壁だけ ---
    private int expand(MCTS3Node node, int type) {
        if (node.isExpanded) return 0;
        Board board = node.state;

        int myColor = board.turn;
        int enemyColor = (myColor == Board.BLACK ? Board.WHITE : Board.BLACK);

        // 敵の最短経路を取得
        List<int[]> enemyPath = getShortestPathCells(board, enemyColor);
        
        //移動手だけ展開(node1)
        if (type == 1) {
            for (int[] mv : getLegalPawnMoves(board)) {
                Board tmp = new Board(board);
                pawnMove.movePawn(tmp, mv[0], mv[1]);

                MCTS3Node child = new MCTS3Node(tmp, node);
                child.moveType = 0;
                child.moveR = mv[0];
                child.moveC = mv[1];
                node.children.add(child);
            }
        } else {     
            //壁手だけ展開(node2)
            for (int[] w : getLegalHorizontalWalls(board)) {
                if (!affectEnemyPath(w[0], w[1], 1, enemyPath)) continue;
                
                Board tmp = new Board(board);
                wallPlace.placeHorizontalWall(tmp, w[0], w[1]);

                MCTS3Node child = new MCTS3Node(tmp, node);
                child.moveType = 1;
                child.moveR = w[0];
                child.moveC = w[1];
                node.children.add(child);
            }
            for (int[] w : getLegalVerticalWalls(board)) {
                if (!affectEnemyPath(w[0], w[1], 2, enemyPath)) continue;
                
                Board tmp = new Board(board);
                wallPlace.placeVerticalWall(tmp, w[0], w[1]);

                MCTS3Node child = new MCTS3Node(tmp, node);
                child.moveType = 2;
                child.moveR = w[0];
                child.moveC = w[1];
                node.children.add(child);
            }

            if (node.children.isEmpty()) {
                // 移動展開に切り替え
                return -1;
            }               
        }

        node.isExpanded = true;
        return 0;
    }

   

    // --- プレイアウト ---
    private double playout(Board board, int rootPlayer) {
        Board tmp = new Board(board);
        int myColor    = tmp.turn;
        int enemyColor = (myColor == Board.BLACK ? Board.WHITE : Board.BLACK);
        int myColor0    = myColor;
        int enemyColor0 = enemyColor;

        WallPlace wp = new WallPlace();
        PawnMove pm = new PawnMove();
        
        double value = 0;

        for (int step = 0; step < 70; step++) {

            int myWalls = (myColor == Board.BLACK ? tmp.blackWalls : tmp.whiteWalls);

            // --- 30% 移動 / 70% 壁 ---
            boolean doMove = Math.random() < 0.5;

            // --- 移動プレイアウト ---
            if (doMove || myWalls <= 0) {
                List<int[]> moves = getLegalPawnMoves(tmp);
                if (moves.isEmpty()) break;

                int bestDist = Integer.MAX_VALUE;
                int[] bestMove = null;

                //最短経路が縮む手を探す
                for (int[] mv : moves) {
                    Board tmp2 = new Board(tmp);
                    pm.movePawn(tmp2, mv[0], mv[1]);
                    int dist = shortestPath(tmp2, myColor);

                    if (dist < bestDist) {
                        bestDist = dist;
                        bestMove = mv;
                    }
                }
                
                //見つかった最良手で移動
                if (bestMove != null) {
                    pm.movePawn(tmp, bestMove[0], bestMove[1]);
                } else {
                    //ランダム                
                    int[] mv = moves.get((int)(Math.random() * moves.size()));
                    pm.movePawn(tmp, mv[0], mv[1]);

                }

                // ★ 手番を切り替える
                tmp.turn = (tmp.turn == Board.BLACK ? Board.WHITE : Board.BLACK);

                // ★ myColor / enemyColor を更新する
                myColor = tmp.turn;
                enemyColor = (myColor == Board.BLACK ? Board.WHITE : Board.BLACK);
                if (shortestPath(tmp, myColor) == 0) {
                    return 1;
                } else if (shortestPath(tmp, enemyColor) == 0) {
                    return -1;
                }
                
                continue;
            }

            // --- 壁プレイアウト ---
            List<int[]> hw = getLegalHorizontalWalls(tmp);
            List<int[]> vw = getLegalVerticalWalls(tmp);

            List<int[]> allWalls = new ArrayList<>();
            for (int[] w : hw) allWalls.add(new int[]{1, w[0], w[1]});
            for (int[] w : vw) allWalls.add(new int[]{2, w[0], w[1]});

            if (allWalls.isEmpty()) break;

            //ここから訂正
            int before = shortestPath(tmp, enemyColor);
            boolean placed = false;

            //敵の距離を延ばす壁を優先
            for (int[] w : allWalls) {
                Board tmp2 = new Board(tmp);
                boolean ok = (w[0] == 1) ? wp.placeHorizontalWall(tmp2, w[1], w[2]) : wp.placeVerticalWall(tmp2, w[1], w[2]);

                if (!ok) continue;

                int after = shortestPath(tmp2, enemyColor);

                if (after > before) {
                    tmp = tmp2;
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                int[] w = allWalls.get((int)(Math.random() * allWalls.size()));

                Board tmp2 = new Board(tmp);
                boolean ok = false;

                if (w[0] == 1) ok = wp.placeHorizontalWall(tmp2, w[1], w[2]);
                if (w[0] == 2) ok = wp.placeVerticalWall(tmp2, w[1], w[2]);
                //if (!ok) continue;
                tmp = tmp2;            
            }        
        }

        // --- 評価 ---
        int rootColor = rootPlayer;
        int enemyDistBefore = shortestPath(board, enemyColor);
        int enemyDistAfter  = shortestPath(tmp, enemyColor);
        int myDistBefore    = shortestPath(board, rootColor);
        int myDistAfter     = shortestPath(tmp, rootColor);

        if (myDistAfter > enemyDistAfter) {
            value = -1; 
        } else if (myDistAfter < enemyDistAfter) {
            value = 1;
        } else {
            value = 0;
        }
        return value;
    }

    // --- 価値の伝播 ---
    private void backpropagate(MCTS3Node node, double value, int rootPlayer) {
        MCTS3Node cur = node;
        while (cur != null) {
            cur.n += 1;

            if (cur.playerToMove == rootPlayer) {
                cur.w += value;
            } else {
                cur.w -= value;
            }

            cur = cur.parent;
        }
    }

    // --- 1回の MCTS 反復 ---
    private boolean mctsIteration(Board board, MCTS3Node root, int type) {

        MCTS3Node node = root;
        // 選択フェーズ

        while (node.isExpanded && !node.children.isEmpty()) {
            node = selectChild(node);
        }
        // 展開フェーズ
        int a = expand(node, type);
        if (a == -1) {
            //System.out.println("だから言ったじゃん");
            return true;
        }
        // --- 子がないなら終了 ---
        if (node.children.isEmpty()) return false;
        
        // 新しく展開された子から1つ選んで評価／プレイアウト
        MCTS3Node leaf = node.children.get((int)(Math.random() * node.children.size()));

        double value = playout(leaf.state, root.playerToMove);

        // 逆伝播
        backpropagate(leaf, value, root.playerToMove);

        return false;
    }

    private void applyShortestMove(Board board, int player) {
        List<int[]> moves = getLegalPawnMoves(board);
        int bestDist = Integer.MAX_VALUE;
        int bestR = -1, bestC = -1;
        for (int[] mv : moves) {
            Board tmp = new Board(board);
            pawnMove.movePawn(tmp, mv[0], mv[1]);
            int dist = shortestPath(tmp, player);
            if (dist < bestDist) {
                bestDist = dist;
                bestR = mv[0];
                bestC = mv[1];
            }
        }
        pawnMove.movePawn(board, bestR, bestC);
    }

    //その手が敵にとって悪影響のある手か？
    private boolean affectEnemyPath(int r, int c, int type, List<int[]> path) {
        for (int[] cell : path) {
            int pr = cell[0];
            int pc = cell[1];

            //横壁がこのセルの上下を遮るか？
            if (type == 1) {
                if (pr == r && pc == c) return true;
                if (pr == r - 1 && pc == c - 1) return true;
                if (pr == r && pc == c - 1) return true;
                if (pr == r - 1 && pc == c) return true;
            }

            //縦壁がこのセルの左右を遮るか？
            if (type == 2) {
                if (pr == r && pc == c) return true;
                if (pr == r - 1 && pc == c - 1) return true;
                if (pr == r && pc == c - 1) return true;
                if (pr == r - 1 && pc == c) return true;
            }
        }
        return false;
    }

    private List<int[]> getShortestPathCells(Board board, int color) {
        int startRow = (color == Board.BLACK) ? board.blackRow : board.whiteRow;
        int startCol = (color == Board.BLACK) ? board.blackCol : board.whiteCol;

        // BFS 用
        boolean[][] visited = new boolean[9][9];
        int[][][] parent = new int[9][9][2];  // 経路復元用

        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        int goalR = (color == Board.BLACK ? 8 : 0);

        while (!q.isEmpty()) {
            int[] s = q.poll();
            int r = s[0], c = s[1];

            // ゴール到達
            if (r == goalR) {
                return reconstructPath(parent, startRow, startCol, r, c);
            }

            int[][] dirs = {
                {1,0},{-1,0},{0,1},{0,-1}
            };

            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];

                if (nr < 0 || nr > 8 || nc < 0 || nc > 8) continue;
                if (visited[nr][nc]) continue;

                if (!canMove(board, r, c, nr, nc)) continue;

                visited[nr][nc] = true;
                parent[nr][nc][0] = r;
                parent[nr][nc][1] = c;
                q.add(new int[]{nr, nc});
            }
        }
        return new ArrayList<>();  // 到達不可
    }

    private List<int[]> reconstructPath(int[][][] parent, int sr, int sc, int gr, int gc) {
        List<int[]> path = new ArrayList<>();

        int r = gr, c = gc;

        while (!(r == sr && c == sc)) {
            path.add(new int[]{r, c});
            int pr = parent[r][c][0];
            int pc = parent[r][c][1];
            r = pr;
            c = pc;
        }

        path.add(new int[]{sr, sc});
        Collections.reverse(path);
        return path;
    }
    // --- 外から呼ぶ入口 ---
    public void playWithMCTS3(Board board, int iterations, int type) {
        MCTS3Node root = new MCTS3Node(new Board(board), null);

        if (type == 0) {
            //System.out.println("正しいよ");
            applyShortestMove(board, root.playerToMove);
            return;
        } 

        for (int i = 0; i < iterations; i++) {
            if (mctsIteration(board, root, type)) {
                applyShortestMove(board, root.playerToMove);
                return;
            } 
        }
        

        List<MCTS3Node> allChildren = root.children;

        MCTS3Node best = null;
        int bestN = -1;
        for (MCTS3Node child : allChildren) {
            //System.out.println("子供" + child);
            if (child.n > bestN) {
                bestN = child.n;
                best = child;
            }
        }
        //System.out.println(best);

        if (best != null) {
            switch (best.moveType) {
                case 0:
                    pawnMove.movePawn(board, best.moveR, best.moveC);
                    break;
                case 1:
                    wallPlace.placeHorizontalWall(board, best.moveR, best.moveC);
                    break;
                case 2:
                    wallPlace.placeVerticalWall(board, best.moveR, best.moveC);
                    break;
            }
        }
    }
}
