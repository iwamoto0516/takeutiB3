import java.util.*;

public class Masuya2 extends Player {

    private PawnMove pawnMove = new PawnMove();
    private WallPlace wallPlace = new WallPlace();

    public Masuya2(int color) {
        super(color);
    }

    @Override
    public boolean play(Board board) {
        MCTS3 mcts = new MCTS3();
        int type = 0;

        int myColor = this.color;  // Masuya は Player を継承しているので自分の色が入ってる
        int enemyColor = (myColor == Board.BLACK ? Board.WHITE : Board.BLACK);
        int myWalls    = (myColor == Board.BLACK ? board.blackWalls : board.whiteWalls);
        int enemyWalls = (enemyColor == Board.BLACK ? board.blackWalls : board.whiteWalls);
        int myDist    = shortestPath(board, myColor);
        int enemyDist = shortestPath(board, enemyColor);


        if (myWalls == 0 & enemyWalls == 0) {
            type = 0;
        } else if (myDist == enemyDist & myWalls != 0) {
            type = (Math.random() < 0.5 ? 0 : 2);
        } else if (myDist < enemyDist & myWalls != 0) {
            type = (Math.random() < 0.7 ? 0 : 2);
        } else if (myDist > enemyDist & myWalls != 0) {
            type = (Math.random() < 0.2 ? 0 : 2);
        } else if (myDist < enemyDist & enemyWalls == 0) {
            type = 0;
        } else {
            type = 0;
        }

        mcts.playWithMCTS3(board, 500, type);  // 探索回数は200〜800が現実的
    return true;
    }


    // -------------------------
    // 合法手の列挙
    // -------------------------

    public List<int[]> getLegalPawnMoves(Board board) {
        List<int[]> moves = new ArrayList<>(); //リストの宣言

        int row = (board.turn == Board.BLACK) ? board.blackRow : board.whiteRow;
        int col = (board.turn == Board.BLACK) ? board.blackCol : board.whiteCol;

        int[][] dirs = { //ここで１マス（下、上、右、左）とジャンプマス（下、上、右、左、斜め右下、斜め左下、斜め右上、斜め左上）へ行くための数値を宣言
            {1,0},{-1,0},{0,1},{0,-1},
            {2,0},{-2,0},{0,2},{0,-2},
            {1,1},{1,-1},{-1,1},{-1,-1}
        };

        for (int[] d : dirs) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (nr < 0 || nr > 8 || nc < 0 || nc > 8) continue; //移動するマスが範囲外の場合スキップ

            Board tmp = new Board(board); //現在の盤面をコピー
            if (pawnMove.movePawn(tmp, nr, nc)) { //その盤面において実際に移動可能かを確認
                moves.add(new int[]{nr, nc}); //移動できるならばmoveリストに追加(移動した行、移動した列)            
            }
        }

        return moves;
    }

    public List<int[]> getLegalHorizontalWalls(Board board) {
        List<int[]> list = new ArrayList<>(); //リストの宣言

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Board tmp = new Board(board); //１試行ごとに盤面をリセットするため２重for文の中
                if (wallPlace.placeHorizontalWall(tmp, r, c)) { //横壁がおけるのか？
                    list.add(new int[]{r, c});
                }
            }
        }
        return list;
    }

    public List<int[]> getLegalVerticalWalls(Board board) {
        List<int[]> list = new ArrayList<>();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Board tmp = new Board(board);
                if (wallPlace.placeVerticalWall(tmp, r, c)) { //縦壁がおけるのか？
                    list.add(new int[]{r, c});
                }
            }
        }
        return list;
    }

    // -------------------------
    // 評価関数
    // -------------------------

    public double evaluate(Board board, int myColor, int enemyColor) {
        int myDist = shortestPath(board, myColor);
        int enemyDist = shortestPath(board, enemyColor);

        return (enemyDist - myDist) * 1.0;
    }

    // -------------------------
    // 最短距離 BFS
    // -------------------------

    public int shortestPath(Board board, int color) {

        int startRow = (color == Board.BLACK) ? board.blackRow : board.whiteRow;
        int startCol = (color == Board.BLACK) ? board.blackCol : board.whiteCol;

        boolean[][] visited = new boolean[9][9];
        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{startRow, startCol, 0});
        visited[startRow][startCol] = true;

        while (!q.isEmpty()) {
            int[] s = q.poll();
            int r = s[0], c = s[1], d = s[2];

            // ゴール判定（相手の駒は無視）
            if (color == Board.BLACK && r == 8) return d;
            if (color == Board.WHITE && r == 0) return d;

            // 4方向だけで十分
            int[][] dirs = {
                {1,0},{-1,0},{0,1},{0,-1}
            };

            for (int[] dir : dirs) {
                int nr = r + dir[0];
                int nc = c + dir[1];

                if (nr < 0 || nr > 8 || nc < 0 || nc > 8) continue;
                if (visited[nr][nc]) continue;

                // 壁チェック（movePawn を使わない）
                if (!canMove(board, r, c, nr, nc)) continue;

                visited[nr][nc] = true;
                q.add(new int[]{nr, nc, d + 1});
            }
        }

        return 999;
    }

    public  boolean canMove(Board board, int r, int c, int nr, int nc) {
        // 下
        if (nr == r + 1 && nc == c) {
            return !board.horizontalWall[r][c];
        }
        // 上
        if (nr == r - 1 && nc == c) {
            return !board.horizontalWall[r - 1][c];
        }
        // 右
        if (nr == r && nc == c + 1) {
            return !board.verticalWall[r][c];
        }
        // 左
        if (nr == r && nc == c - 1) {
            return !board.verticalWall[r][c - 1];
        }
        return false;
    }
}
