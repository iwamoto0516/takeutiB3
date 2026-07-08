public class WallLoopChecker {

    private int[] parent;

    private static final int TOP = 100;
    private static final int BOTTOM = 101;
    private static final int LEFT = 102;
    private static final int RIGHT = 103;
    private static final int SIZE = 104; // ポスト0~99(10x10) + 特殊ノード4つ

    private int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    private void union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra != rb) {
            parent[ra] = rb;
        }
    }

    // ポスト座標(i,j) 0~9 を番号に変換
    private int post(int i, int j) {
        return i * 10 + j;
    }

    // 盤面を上下に完全分断する壁ができているか確認
    public boolean hasFullBarrier(Board board) {

        parent = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            parent[i] = i;
        }

        // 盤の外周ポストを特殊ノードに繋ぐ
        for (int k = 0; k <= 9; k++) {
            union(post(0, k), TOP);
            union(post(9, k), BOTTOM);
            union(post(k, 0), LEFT);
            union(post(k, 9), RIGHT);
        }

        // 横壁の中心点からポストの接続を反映
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board.horizontalCenter[row][col]) {
                    union(post(row + 1, col), post(row + 1, col + 1));
                    union(post(row + 1, col + 1), post(row + 1, col + 2));
                }
            }
        }

        // 縦壁の中心点からポストの接続を反映
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board.verticalCenter[row][col]) {
                    union(post(row, col + 1), post(row + 1, col + 1));
                    union(post(row + 1, col + 1), post(row + 2, col + 1));
                }
            }
        }

        // 左端と右端が壁でつながったら完全分断
        return find(LEFT) == find(RIGHT);
    }
}
