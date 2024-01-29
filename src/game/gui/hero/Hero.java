package game.gui.hero;

import game.gui.board.BoardFrame;
import game.logic.Direction;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Hero {

    /**
     * distance at which ghost can kill pacman (pixels)
     */
    protected static final int DEATH_RADIUS;
    protected final int timeToPassOneCell;
    protected final int cellSize;
    protected final JLabel label;
    protected final BoardFrame boardFrame;
    /**
     * time to go through 1 pixel
     */
    protected int speed;
    protected final AtomicReference<Direction> direction;
    protected final AtomicBoolean isEndOfRound;
    protected final AtomicBoolean isPaused;
    protected final AtomicReference<Direction> newDirection;
    protected final AtomicReference<Point> target;
    protected final AtomicReference<Point> newTarget;
    public final Object mutex;
    protected boolean isFrozen;
    protected boolean isExactlyInCell;

    {
        direction = new AtomicReference<>();
        isEndOfRound = new AtomicBoolean();
        isPaused = new AtomicBoolean();
        newDirection = new AtomicReference<>();
        target = new AtomicReference<>();
        newTarget = new AtomicReference<>();
        mutex = new Object();
    }

    static {
        DEATH_RADIUS = 4;
    }

    public Hero(int timeToPassOneCell, int cellSize, JLabel label, Direction direction, BoardFrame boardFrame) {
        this.timeToPassOneCell = timeToPassOneCell;
        this.cellSize = cellSize;
        boardFrame.getContentPane().setComponentZOrder(label, 0);
        this.label = label;
        this.boardFrame = boardFrame;
        this.direction.set(direction);
        this.speed = timeToPassOneCell / cellSize;
    }

    public abstract void start();
}
