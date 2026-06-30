import java.util.Scanner;

public class HumanPlayer extends Player {

    private Scanner sc = new Scanner(System.in);
    private PawnMove pawnMove = new PawnMove();
    private WallPlace wallMove = new WallPlace();

    public HumanPlayer(int color) {
        super(color);
    }

    @Override
    public boolean play(Board board) {

        System.out.println("1: 駒を移動");
        System.out.println("2: 横壁を設置");
        System.out.println("3: 縦壁を設置");

        System.out.print("操作(1～3、qで終了): ");

        if (sc.hasNextInt()) {

            int choice = sc.nextInt();

            if (choice == 1) {

                System.out.print("行: ");
                int row = sc.nextInt();

                System.out.print("列: ");
                int col = sc.nextInt();

                return pawnMove.movePawn(board,row, col);

            } else if (choice == 2) {

                System.out.print("壁の行: ");
                int row = sc.nextInt();

                System.out.print("壁の列: ");
                int col = sc.nextInt();

                return wallMove.placeHorizontalWall(board,row, col);

            } else if (choice == 3) {

                System.out.print("壁の行: ");
                int row = sc.nextInt();

                System.out.print("壁の列: ");
                int col = sc.nextInt();

                return wallMove.placeVerticalWall(board,row, col);

            } else {
                System.out.println("1～3を入力してください。");
                return false;
            }

        } else {

            String input = sc.next();

            if (input.equalsIgnoreCase("q")) {
                System.out.println("ゲームを終了します。");
                System.exit(0);
            } else {
                System.out.println("入力が正しくありません。");
                return false;
            }
        }

        return false;
    }
}

