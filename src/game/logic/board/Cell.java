package game.logic.board;

import game.logic.exception.GameException;

public class Cell {
    public static final int TINY_SIZE;
    public static final int SMALL_SIZE;
    public static final int MEDIUM_SIZE;
    public static final int LARGE_SIZE;
    public static final int POINT_VALUE;
    public static final int EGGPLANT_VALUE;
    public static final int BANANA_VALUE;
    public static final int CHERRY_VALUE;
    public static final int MELON_VALUE;
    public static final int APPLE_VALUE;
    public static final int PEPPER_VALUE;
    private int fruitValue;

    private boolean hasPoint;
    private final boolean isFree;

    {
        fruitValue = 0;
        hasPoint = true;
    }

    static {
        POINT_VALUE = 5;

        TINY_SIZE = 6;                  // 76 - 100
        SMALL_SIZE = 8;                 // 51 - 75
        MEDIUM_SIZE = 12;               // 26 - 50
        LARGE_SIZE = 25;                // 10 - 25

        EGGPLANT_VALUE = 50;
        BANANA_VALUE = 55;
        CHERRY_VALUE = 60;
        MELON_VALUE = 65;
        APPLE_VALUE = 70;
        PEPPER_VALUE = 75;
    }

    private Cell(boolean isFree) {
        this.isFree = isFree;
    }

    public static Cell free() {
        return new Cell(true);
    }

    public static Cell wall() {
        return new Cell(false);
    }

    public boolean hasPoint() {
        return hasPoint;
    }

    public boolean isFree() {
        return isFree;
    }

    public boolean isWall() {
        return !isFree;
    }

    /**
     * @return number of points earned from crossing this cell
     * @throws GameException attempt to get point from wall
     */
    public int eatPoint() {
        if (isWall()) {
            throw new GameException("Cannot get point from wall");
        }
        int v = 0;
        // TODO: fruit must be displayed over the point, so second will not disappear
        if (fruitValue > 0) {
            v = fruitValue;
            fruitValue = 0;
        } else if (hasPoint) {
            v = POINT_VALUE;
            hasPoint = false;
        }
        return v;
    }

    public void setFruit(int fruitScore) {
        this.fruitValue = fruitScore;
    }
}
