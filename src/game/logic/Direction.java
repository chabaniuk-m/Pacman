package game.logic;

import game.logic.exception.GameException;

public enum Direction {
    LEFT,
    UP,
    RIGHT,
    DOWN;

    public static Direction of(int n) {
        return switch (n) {
            case 0 -> RIGHT;
            case 1 -> LEFT;
            case 2 -> UP;
            case 3 -> DOWN;
            default -> throw new GameException("cannot create direction of number " + n);
        };
    }

    public int orderIndex() {
        return switch (this) {
            case LEFT -> 0;
            case UP -> 1;
            case RIGHT -> 2;
            case DOWN -> 3;
        };
    }

    public boolean isOpposite(Direction direction) {
        return switch (this) {
            case RIGHT -> direction == LEFT;
            case LEFT -> direction == RIGHT;
            case UP -> direction == DOWN;
            case DOWN -> direction == UP;
        };
    }

    @Override
    public String toString() {
        return switch (this) {

            case LEFT -> "left";
            case UP -> "up";
            case RIGHT -> "right";
            case DOWN -> "down";
        };
    }

    public Direction getOpposite() {
        return switch (this) {

            case LEFT -> RIGHT;
            case UP -> DOWN;
            case RIGHT -> LEFT;
            case DOWN -> UP;
        };
    }
}
