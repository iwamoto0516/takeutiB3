// プレイヤーを管理
public abstract class Player {

    protected int color;

    public Player(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public abstract boolean play(Board board);
}