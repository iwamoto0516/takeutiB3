import java.util.Queue;
import java.util.LinkedList;

public class Heiro {
    public boolean HeiroCheck(BoardMap boardmap) {
        //ゴールに到達できるか
        boolean blackOK = canReachGoal(boardmap, boardmap.blackRow, boardmap.blackCol, 16);
        System.out.println(blackOK);

        boolean whiteOK = canReachGoal(boardmap, boardmap.whiteRow, boardmap.whiteCol, 0);
        System.out.println(whiteOK);

        
        System.out.printf("(%d,%d)%n", boardmap.blackCol, boardmap.blackRow);

        System.out.printf("(%d,%d)%n", boardmap.whiteCol, boardmap.whiteRow);

        
        return blackOK && whiteOK;
    }
    
    //ゴールの探索（BFS）
    private boolean canReachGoal(BoardMap map, int startR, int startC, int goalRow) {
        boolean[][] visited = new boolean[17][17]; //もう調べた場所を記録する2次元配列（17*17サイズのマップに合わせている）
        Queue<int[]> queue = new LinkedList<>();  //キューの宣言

        queue.add(new int[]{startR, startC}); //スタート地点のキューを入れる
        
        visited[startR][startC] = true; //スタート地点を調査済みのしるしであるTrueにする

        //ループで使用する上下左右に進むための方向ベクトルdir（二次元配列）
        int[][] dirs = {
            {2, 0},  // 下
            {-2, 0}, // 上
            {0, 2},  // 右
            {0, -2}  // 左
        };
        while (!queue.isEmpty()) { //キューが空でないなら真

            int[] cur = queue.poll(); //キューの先頭の要素をとってくる
            System.out.print("(" + cur[0] + ", " + cur[1] + ")");
            int r = cur[0]; //取ってきた行
            int c = cur[1]; //取ってきた列

            // ゴールに到達したらOK
            if (r == goalRow) {
                return true;
            }

            // 4方向へ探索
            for (int i = 0; i < 4; i++) {
                int[] d = dirs[i];
                int nr = r + d[0];
                int nc = c + d[1];

                // 範囲外
                if (nr < 0 || nr >= 17 || nc < 0 || nc >= 17) {
                    continue;
                }

                // 壁なら進めない
                if (d == dirs[0]) {
                    if (map.map[nr-1][nc] == BoardMap.WALL) {
                        continue;
                    }
                } else if (d == dirs[1]) {
                    if (map.map[nr+1][nc] == BoardMap.WALL) {
                        continue;
                    }
                } else if (d == dirs[2]) {
                    if (map.map[nr][nc-1] == BoardMap.WALL) {
                        continue;
                    }
                } else {
                    if (map.map[nr][nc+1] == BoardMap.WALL) {
                        continue;
                    }
                }

                // すでに訪問済み
                if (visited[nr][nc]) {
                    continue;
                }

                //範囲がでも壁でも訪問済みでもないマスを進み調査済みとする
                System.out.print("(" + nr + ", " + nc + ")");
                visited[nr][nc] = true; 
                queue.add(new int[]{nr, nc});
            }
        }

        //この時点でキューには何も残ってない
        // 最後まで到達できなかった
        return false;
    }
}