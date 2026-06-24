import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Quoridor q = new Quoridor();
        Scanner sc = new Scanner(System.in);

        while(q.getWinner() == 0) {

            q.printBoard();

            if(q.turn == Quoridor.BLACK) {
                System.out.println("黒の番");
            } else {
                System.out.println("白の番");
            }

            System.out.println("1: 駒を移動");
            System.out.println("2: 横壁を設置");
            System.out.println("3: 縦壁を設置");

            int choice = sc.nextInt();

            boolean success = false;

            if(choice == 1) {

                System.out.print("行: ");
                int row = sc.nextInt();

                System.out.print("列: ");
                int col = sc.nextInt();

                success = q.movePawn(row, col);

            } else if(choice == 2) {

                System.out.print("壁の行: ");
                int row = sc.nextInt();

                System.out.print("壁の列: ");
                int col = sc.nextInt();

                success = q.placeHorizontalWall(row, col);

            } else if(choice == 3) {

                System.out.print("壁の行: ");
                int row = sc.nextInt();

                System.out.print("壁の列: ");
                int col = sc.nextInt();

                success = q.placeVerticalWall(row, col);
            }

            if(!success) {
                System.out.println("その操作はできません！");
            }
        }

        if(q.getWinner() == Quoridor.BLACK) {
            System.out.println("黒の勝ち！");
        } else {
            System.out.println("白の勝ち！");
        }

        sc.close();
    }
}