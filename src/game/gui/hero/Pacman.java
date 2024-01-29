package game.gui.hero;

import game.gui.utils.UIUtils;
import game.gui.board.BoardFrame;
import game.logic.Direction;
import game.logic.exception.AudioLoadingException;
import game.logic.exception.GameException;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Pacman extends Hero {

    /**
     * GIFs & images used to display pacman state
     */
    public enum Picture {
        CIRCLE_DOWN(icon("/images/pacman/circle-left.png")),
        CIRCLE_LEFT(icon("/images/pacman/circle-right.png")),
        CIRCLE_RIGHT(icon("/images/pacman/circle-up.png")),
        CIRCLE_UP(icon("/images/pacman/circle-down.png")),
        DIE_LEFT(icon("/images/pacman/die-left.png")),
        DIE_RIGHT(icon("/images/pacman/die-right.png")),
        DIE_UP(icon("/images/pacman/die-up.png")),
        DIE_DOWN(icon("/images/pacman/die-down.png")),
        LEFT(icon("/images/pacman/left.png")),
        RIGHT(icon("/images/pacman/right.png")),
        UP(icon("/images/pacman/up.png")),
        CONFETTI(icon("/images/pacman/confetti.png")),
        DOWN(icon("/images/pacman/down.png"));

        private final ImageIcon icon;

        Picture(ImageIcon icon) {
            this.icon = icon;
        }

        /**
         * Reads content of a file and converts it to a ImageIcon instance
         * @param fileName path to the file
         * @throws NullPointerException cannot find a file
         */
        private static ImageIcon icon(String fileName) {
            try {
                return new ImageIcon(Objects.requireNonNull(Picture.class.getResource(fileName)));
            } catch (Exception e) {
                throw new RuntimeException("Problem with reading a file");
            }
        }

        public ImageIcon getIcon() {
            return icon;
        }

        public ImageIcon getScaled(int size) {
            return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_DEFAULT));
        }
    }

    /**
     * in milliseconds
     */
    private final AtomicBoolean isCircle;
    private final AtomicBoolean isDirectionJustChangedToOpposite;
    private final int speedup;
    private static final int SPEEDUP_TIME;          // milliseconds

//    public static final int SPEEDUP;
    private static final String DIE_AUDIO_FILE_LOCATION;
    private final ImageIcon CIRCLE_DOWN;
    private final ImageIcon CIRCLE_LEFT;
    private final ImageIcon CIRCLE_RIGHT;
    private final ImageIcon CIRCLE_UP;
    private final ImageIcon DIE_LEFT;
    private final ImageIcon DIE_RIGHT;
    private final ImageIcon DIE_UP;
    private final ImageIcon DIE_DOWN;
    private final ImageIcon LEFT;
    private final ImageIcon RIGHT;
    private final ImageIcon UP;
    private final ImageIcon CONFETTI;
    private final ImageIcon DOWN;

    {
        // TODO: maybe change value
        isCircle = new AtomicBoolean(true);
        isDirectionJustChangedToOpposite = new AtomicBoolean();
    }

    static {
        SPEEDUP_TIME = 10_000;              // 10 seconds
        DIE_AUDIO_FILE_LOCATION = "/audio/death.wav";
    }

    /**
     * Create a pacman and place its icon on a board
     *
     * @param cellSize  size of a pacman icon
     * @param direction where pacman will start moving
     * @param label     component that is already added to the board
     *                  and will represent a pacman and it's movement
     * @param board
     */
    public Pacman(int cellSize, Direction direction, JLabel label, BoardFrame board) {
        super(300, cellSize, label, direction, board);

        // 40% faster
        speedup = speed * 2 / 5;

        // prepare scaled images
        {
            CIRCLE_DOWN = Picture.CIRCLE_DOWN.getScaled(cellSize);
            CIRCLE_LEFT = Picture.CIRCLE_LEFT.getScaled(cellSize);
            CIRCLE_RIGHT = Picture.CIRCLE_RIGHT.getScaled(cellSize);
            CIRCLE_UP = Picture.CIRCLE_UP.getScaled(cellSize);
            DOWN = Picture.DOWN.getScaled(cellSize);
            LEFT = Picture.LEFT.getScaled(cellSize);
            RIGHT = Picture.RIGHT.getScaled(cellSize);
            UP = Picture.UP.getScaled(cellSize);
            DIE_DOWN = Picture.DIE_DOWN.getScaled(cellSize);
            DIE_LEFT = Picture.DIE_LEFT.getScaled(cellSize);
            DIE_RIGHT = Picture.DIE_RIGHT.getScaled(cellSize);
            DIE_UP = Picture.DIE_UP.getScaled(cellSize);
            CONFETTI = Picture.CONFETTI.getScaled(cellSize);
        }

        label.setIcon(pacmanIcon());
    }

    boolean isFast = false;

    public int getMaxSpeed() {
        synchronized (mutex) {
            if (isFast) {
                return speed;
            } else {
                return speed + speedup;
            }
        }
    }

    private final AtomicLong timer = new AtomicLong(0);
    public void speedup() {
        if (timer.get() > 200) {
            synchronized (timer) {
                timer.set(timer.get() + 10_000);
            }
            return;
        }
        timer.set(10_000);
        new Thread(() -> {
            try {
                synchronized (mutex) {
                    UIUtils.playSound("/audio/speedup.wav", false);
                    boardFrame.addPower("Speed Up");
                    speedUp();
                }
                while (timer.get() > 0) {
                    Thread.sleep(50);
                    synchronized (mutex) {
                        if (!isPaused.get()) {
                            synchronized (timer) {
                                timer.set(timer.get() - 50);
                            }
                        }
                    }
                }
                synchronized (mutex) {
                    boardFrame.removePower("Speed Up");
                    slowDown();
                }
            } catch (InterruptedException | AudioLoadingException e) {
                throw new GameException(e);
            }
        }).start();
    }

    private void speedUp() {
        if (!isFast) {
            synchronized (mutex) {
                speed -= speedup;
            }
            isFast = true;
            // slow down after SPEEDUP_TIME
            new Thread(() -> {
                try {
                    Thread.sleep(SPEEDUP_TIME);
                    slowDown();
                } catch (InterruptedException e) {
                    throw new GameException(e);
                }
            }).start();
        }
    }

    private void slowDown() {
        if (isFast) {
            synchronized (mutex) {
                speed += speedup;
            }
            isFast = false;
        }
    }

    public int getSpeed() {
        return speed;
    }

    public Direction getDirection() {
        return direction.get();
    }

    public Point getCoordinates() {
        return new Point(label.getX(), label.getY());
    }

    public void setTarget(Point point) {
        target.set(point);
    }

    public void setNewTarget(Point point) {
        newTarget.set(point);
    }

    public void setNewDirection(Direction direction) {
        newDirection.set(direction);
    }

    public void setDirection(Direction direction) {
        this.direction.set(direction);
    }

    @Override
    public void start() {
        new Thread(() -> {
            int count = 0;
            try {
                while (true) {
                    // all about movement to next pixel
                    synchronized (mutex) {
                        if (isEndOfRound.get()) {
                            return;
                        }
                        if (isPaused.get()) {
                            freeze();
                        }
                        if (isDirectionJustChangedToOpposite.get()) {
                            isDirectionJustChangedToOpposite.set(false);
                            count = cellSize - count;
                        }
                        // move to next pixel
                        switch (direction.get()) {
                            case RIGHT -> {
                                if (label.getX() == target.get().x) {
                                    count -= 1;
                                    freeze();
                                } else {
                                    nextIcon();
                                    label.setBounds(label.getX() + 1, label.getY(), label.getWidth(), label.getHeight());
                                }
                            }
                            case LEFT -> {
                                if (label.getX() == target.get().x) {
                                    count -= 1;
                                    freeze();
                                } else {
                                    nextIcon();
                                    label.setBounds(label.getX() - 1, label.getY(), label.getWidth(), label.getHeight());
                                }
                            }
                            case UP -> {
                                if (label.getY() == target.get().y) {
                                    count -= 1;
                                    freeze();
                                } else {
                                    nextIcon();
                                    label.setBounds(label.getX(), label.getY() - 1, label.getWidth(), label.getHeight());
                                }
                            }
                            case DOWN -> {
                                if (label.getY() == target.get().y) {
                                    count -= 1;
                                    freeze();
                                } else {
                                    nextIcon();
                                    label.setBounds(label.getX(), label.getY() + 1, label.getWidth(), label.getHeight());
                                }
                            }
                        }
                        count += 1;
                        if (count == cellSize) {
                            isExactlyInCell = true;
                            boardFrame.pacmenMoveToNextCell(label.getX(), label.getY());
                            if (newTarget.get() != null) {
                                // TODO: remove assert
                                assert newDirection.get() != null;
                                direction.set(newDirection.get());
                                target.set(newTarget.get());
                                newTarget.set(null);
                                newDirection.set(null);
                            }
                            count = 0;
                        } else {
                            isExactlyInCell = false;
                        }
                    }
                    // simulate speed
                    Thread.sleep(speed);
                }
            } catch (InterruptedException e) {
                throw new GameException(e);
            }
        }).start();
    }

    public void die() {
        System.out.println("""
                PACMAN DIES
                """);
        synchronized (mutex) {
            isEndOfRound.set(true);
            displayDeath();
        }
        try {
            UIUtils.playSound(DIE_AUDIO_FILE_LOCATION, false);
        } catch (AudioLoadingException e) {
            throw new GameException(e);
        }
    }

    private void displayDeath() {
        new Thread(() -> {
            try {
                label.setIcon(switch (direction.get()) {
                    case RIGHT -> RIGHT;
                    case LEFT -> LEFT;
                    case UP -> UP;
                    case DOWN -> DOWN;
                });
                Thread.sleep(200);
                label.setIcon(switch (direction.get()) {
                    case LEFT -> DIE_LEFT;
                    case UP -> DIE_UP;
                    case RIGHT -> DIE_RIGHT;
                    case DOWN -> DIE_DOWN;
                });
                Thread.sleep(100);
                label.setIcon(null);
                Thread.sleep(100);
                int time = 1100;
                int n = 25;
                for (int k = 3; k < n + 3; k++) {
                    int size = (int) (k * 1d / (n + 2) * cellSize) + 1;
                    label.setHorizontalAlignment(0);
                    label.setVerticalAlignment(0);
                    label.setIcon(new ImageIcon(CONFETTI.getImage().getScaledInstance(size, size, Image.SCALE_FAST)));
                    Thread.sleep(time / n);
                }
                Thread.sleep(200);
                label.setIcon(null);
            } catch (InterruptedException e) {
                throw new GameException(e);
            }
        }).start();
    }

    public void setOppositeDirection(Direction newDirection) {
        // TODO: remove assert
        System.out.println("Pacman changes its direction to opposite");
        assert newDirection.isOpposite(direction.get());
        isDirectionJustChangedToOpposite.set(true);
        direction.set(newDirection);
    }

    private void freeze() {
        isFrozen = true;
        isExactlyInCell = true;
        isCircle.set(true);
        UIUtils.stopChomp();
        label.setIcon(switch (direction.get()) {
            case RIGHT -> RIGHT;
            case LEFT -> LEFT;
            case UP -> UP;
            case DOWN -> DOWN;
        });
        try {
//            synchronized (mutex) {
                mutex.wait();
//            }
            isFrozen = false;
        } catch (InterruptedException e) {
            throw new GameException(e);
        }
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void pause() {
        synchronized (mutex) {
            isPaused.set(true);
        }
    }

    public void resume() {
        synchronized (mutex) {
            isPaused.set(false);
            mutex.notify();
        }
    }

    public void stop() {
        synchronized (mutex) {
            isEndOfRound.set(true);
        }
    }

    public boolean isExactlyInCell() {
        return isExactlyInCell;
    }

    private void nextIcon() {
        label.setIcon(pacmanIcon());
    }

    private ImageIcon pacmanIcon() {
        isCircle.set(!isCircle.get());
        return isCircle.get() ?
                switch (direction.get()) {
                    case RIGHT -> CIRCLE_RIGHT;
                    case LEFT -> CIRCLE_LEFT;
                    case UP -> CIRCLE_UP;
                    case DOWN -> CIRCLE_DOWN;
                } :
                switch (direction.get()) {
                    case RIGHT -> RIGHT;
                    case LEFT -> LEFT;
                    case UP -> UP;
                    case DOWN -> DOWN;
                };
    }
}