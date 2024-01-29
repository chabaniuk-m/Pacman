package game.gui.hero;

import game.gui.board.BoardFrame;
import game.gui.utils.UIUtils;
import game.logic.Direction;
import game.logic.Utils;
import game.logic.board.Cell;
import game.logic.exception.GameException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static game.logic.Direction.*;

public class Ghost extends Hero {
    public enum Color {
        BLUE,           // eggplant
        GREEN,          // banana
        MAG,            // cherry
        ORANGE,         // melon
        PINK,           // apple
        RED;            // chilly pepper

        // values() method return array of values preserving ⬆️ the order
        static final ImageIcon[] tinyIcons;
        static final ImageIcon[] smallIcons;
        static final ImageIcon[] mediumIcons;
        static final ImageIcon[] largeIcons;

        static {
            tinyIcons = new ImageIcon[7];
            smallIcons = new ImageIcon[7];
            mediumIcons = new ImageIcon[7];
            largeIcons = new ImageIcon[7];
            loadAllIcons();
        }

        static void loadAllIcons() {
            ImageIcon[] original = new ImageIcon[7];

            original[0] = UIUtils.loadIcon("/images/fruit/eggplant.png");
            original[1] = UIUtils.loadIcon("/images/fruit/banana.png");
            original[2] = UIUtils.loadIcon("/images/fruit/cherry.png");
            original[3] = UIUtils.loadIcon("/images/fruit/watermelon.png");
            original[4] = UIUtils.loadIcon("/images/fruit/apple.png");
            original[5] = UIUtils.loadIcon("/images/fruit/pepper.png");
            original[6] = UIUtils.loadIcon("/images/board/dot.png");

            resizeAll(original, tinyIcons, Cell.TINY_SIZE);
            resizeAll(original, smallIcons, Cell.SMALL_SIZE);
            resizeAll(original, mediumIcons, Cell.MEDIUM_SIZE);
            resizeAll(original, largeIcons, Cell.LARGE_SIZE);
        }

        static void resizeAll(ImageIcon[] original, ImageIcon[] icons, int size) {
            for (int i = 0; i < 7; i++) {
                icons[i] = resized(original[i], size);
            }
        }

        static ImageIcon resized(ImageIcon icon, int size) {
            return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_FAST));
        }

        @Override
        public String toString() {
            return switch (this) {
                case PINK -> "pink-";
                case GREEN -> "green-";
                case RED -> "red-";
                case BLUE -> "blue-";
                case MAG -> "mag-";
                case ORANGE -> "orange-";
            };
        }

        public int getFruitScore() {
            return switch (this) {
                case BLUE -> Cell.EGGPLANT_VALUE;
                case GREEN -> Cell.BANANA_VALUE;
                case MAG -> Cell.CHERRY_VALUE;
                case ORANGE -> Cell.MELON_VALUE;
                case PINK -> Cell.APPLE_VALUE;
                case RED -> Cell.PEPPER_VALUE;
            };
        }

        public int getFruitSpawnGap() {
            return switch (this) {

                case BLUE -> 8_000;
                case GREEN -> 20_000;
                case MAG, RED, PINK -> 5_000;
                case ORANGE -> 10_000;
            };
        }

        static Color of(int fruitScore) {
            if (fruitScore == Cell.EGGPLANT_VALUE) {
                return BLUE;
            }
            if (fruitScore == Cell.BANANA_VALUE) {
                return GREEN;
            }
            if (fruitScore == Cell.CHERRY_VALUE) {
                return MAG;
            }
            if (fruitScore == Cell.MELON_VALUE) {
                return ORANGE;
            }
            if (fruitScore == Cell.APPLE_VALUE) {
                return PINK;
            }
            if (fruitScore == Cell.PEPPER_VALUE) {
                return RED;
            }
            throw new GameException("Invalid fruitScore");
        }

        public ImageIcon getFruitIcon(int size) {
            int index = switch (this) {
                case BLUE -> 0;
                case GREEN -> 1;
                case MAG -> 2;
                case ORANGE -> 3;
                case PINK -> 4;
                case RED -> 5;
            };
            if (size == Cell.TINY_SIZE) {
                return tinyIcons[index];
            }
            if (size == Cell.SMALL_SIZE) {
                return smallIcons[index];
            }
            if (size == Cell.MEDIUM_SIZE) {
                return mediumIcons[index];
            }
            if (size == Cell.LARGE_SIZE) {
                return largeIcons[index];
            }
            else  throw new GameException("invalid size");
        }

        public ImageIcon getDotIcon(int size) {
            if (size == Cell.TINY_SIZE) {
                return tinyIcons[6];
            }
            if (size == Cell.SMALL_SIZE) {
                return smallIcons[6];
            }
            if (size == Cell.MEDIUM_SIZE) {
                return mediumIcons[6];
            }
            if (size == Cell.LARGE_SIZE) {
                return largeIcons[6];
            }
            else  throw new GameException("invalid size");
        }

        public int orderIndex() {
            return switch (this) {
                case BLUE -> 0;
                case GREEN -> 1;
                case MAG -> 2;
                case ORANGE -> 3;
                case PINK -> 4;
                case RED -> 5;
            };
        }
    }

    /**
     * Utility class created for optimization of process, that changes
     * icon of ghost depending on its direction.
     * At the beginning it reads all icons convert them to proper sizes
     * and saves them for later use
     */
    public static class IconManager {
        // order of colors: blue -> green -> mag -> orange -> pink -> red (alpha)
        static final Image[] originalImages;
        static final String iconFilesDirectory;
        static final ImageIcon[] tinyIcons;
        static final ImageIcon[] smallIcons;
        static final ImageIcon[] mediumIcons;
        static final ImageIcon[] largeIcons;

        static {
            iconFilesDirectory = "/images/ghost/";
            originalImages = new Image[24];         // 24 - 6 ghosts x 4 icons per ghost
            readOriginalImages();

            tinyIcons = new ImageIcon[24];
            smallIcons = new ImageIcon[24];
            mediumIcons = new ImageIcon[24];
            largeIcons = new ImageIcon[24];

            readIconsOfSize(tinyIcons, Cell.TINY_SIZE);
            readIconsOfSize(smallIcons, Cell.SMALL_SIZE);
            readIconsOfSize(mediumIcons, Cell.MEDIUM_SIZE);
            readIconsOfSize(largeIcons, Cell.LARGE_SIZE);
        }

        static void readOriginalImages() {
            int index = 0;
            for (var color : Color.values()) {
                for (var dir : Direction.values()) {
                    originalImages[index++] = UIUtils
                            .loadIcon(iconFilesDirectory + color + dir + ".png").getImage();
                }
            }
        }

        static void readIconsOfSize(ImageIcon[] arr, int size) {
            for (int i = 0; i < 24; i++) {
                arr[i] = new ImageIcon(originalImages[i]
                        .getScaledInstance(size, size, Image.SCALE_FAST));
            }
        }

        // central method
        public static ImageIcon getIcon(Color ghostColor, Direction direction, int size) {
            int index = 4 * ghostColor.orderIndex() + direction.orderIndex();
            if (size == Cell.TINY_SIZE) {
                return tinyIcons[index];
            }
            if (size == Cell.SMALL_SIZE) {
                return smallIcons[index];
            }
            if (size == Cell.MEDIUM_SIZE) {
                return mediumIcons[index];
            }
            if (size == Cell.LARGE_SIZE) {
                return largeIcons[index];
            }
            throw new GameException("Invalid size provided");
        }
    }

    private static final int LOOK_AROUND_NAP;           // milliseconds
    private final Color color;
    private final AtomicReference<Object> spawnerMutex;

    static {
        LOOK_AROUND_NAP = 800;
    }

    public Ghost(Color color, Direction direction, int cellSize, JLabel label, BoardFrame boardFrame) {
        super(240, cellSize, label, direction, boardFrame);
        this.color = color;
        spawnerMutex = new AtomicReference<>();
        adjustIcon(Direction.LEFT);
    }

    private void checkPacmanPosition() {
        int x = label.getX();
        int y = label.getY();
        var point = boardFrame.getPacmanCoordinates();
        int pX = point.x;
        int pY = point.y;
        double dist = Utils.dist(x, y, pX, pY);
        if (dist <= DEATH_RADIUS) {
            boardFrame.killPacman();
        }
    }

    @Override
    public void start() {
        final int lookAroundNap = 200;         // milliseconds
        final int fruitSpawnTime = color.getFruitSpawnGap();
        this.spawnerMutex.set(boardFrame.ghostCage.spawnerMutex);
        new Thread(() -> {
            try {
                // look around before moving out of the cage

                System.out.println("""
                        
                        GHOST SPAWNED
                                                
                        """);

                Direction[] eyeMovements = {
                    RIGHT, UP, DOWN, LEFT, DOWN, RIGHT, UP
                };

                for (var d : eyeMovements) {
                    synchronized (mutex) {
                        if (isEndOfRound.get()) {
                            return;
                        }
                        if (isPaused.get()) {
                            System.out.println("PAUSED... (ghost)");
                            mutex.wait();
                        }
                        adjustIcon(d);
                    }
                    Thread.sleep(LOOK_AROUND_NAP);
                }


                System.out.println("""
                        
                        GHOST FINISHED MOVING EYES
                        
                        """);

                direction.set(UP);
                int k = 0;

                // move out of the cage
                int n = 2 * cellSize;
                for (int i = 0; i < n; i++) {
                    // if ghost is only started moving out of the cage it is not necessary to check pacman postion
                    boolean checkPacman = i >= n - DEATH_RADIUS;
                    // move 1 pixel out of the cage
                    synchronized (mutex) {
                        if (isEndOfRound.get()) {
                            return;
                        }
                        if (isPaused.get()) {
                            freeze();
                        }
                        label.setBounds(label.getX(), label.getY() - 1, label.getWidth(), label.getHeight());
                    }
                    ++k;
                    // check can kill pacman
                    if (checkPacman && k >= DEATH_RADIUS) {
                        k = 0;
                        checkPacmanPosition();
                    }
                    Thread.sleep(speed);
                }

                // notify spawner, that ghost moved out of the cage
                synchronized (spawnerMutex.get()) {
                    spawnerMutex.get().notify();
                }

                // ghost moved out of the cage & notified spawner about it

                isExactlyInCell = true;

                int count = 0;      // how many pixels of cell are passed
                int timer = fruitSpawnTime;
                while (!isEndOfRound.get()) {
                    // all about movement to next pixel
                    synchronized (mutex) {
                        if (isPaused.get()) {
                            freeze();
                        }
                        if (isExactlyInCell) {
                            // leave fruit
                            if (timer <= 0) {
                                if (Utils.success(0.25)) {
                                    boardFrame.addFruit(color, label.getX(), label.getY());
                                }
                                timer = fruitSpawnTime;
                            }
                            // decide about next (random) direction
                            ArrayList<Direction> availableDirections = boardFrame.getAvailableDirectionsFrom(label.getX(), label.getY());
                            assert availableDirections.size() != 0;
                            // DANGEROUS ZONE ⬇️
                            boolean chosen = false;
                            Direction dir = direction.get();
                            if (availableDirections.contains(dir)) {
                                if (Utils.success(0.8) || availableDirections.size() == 1) {
                                    chosen = true;
                                }
                            }
                            if (!chosen) {
                                availableDirections.remove(dir);
                                if (availableDirections.contains(dir.getOpposite()) && Utils.success(0.06) ||
                                    availableDirections.size() == 1) {
                                    direction.set(dir.getOpposite());
                                    chosen = true;
                                }
                            }
                            if (!chosen) {
                                availableDirections.remove(dir.getOpposite());
                                direction.set(availableDirections.size() == 1 ?
                                              availableDirections.get(0) :
                                              (Utils.success(0.5) ?
                                               availableDirections.get(0) :
                                               availableDirections.get(1)));
                            }
                            // END OF DANGEROUS ZONE
                            isExactlyInCell = false;
                        }
                        // move to next pixel
                        switch (direction.get()) {
                            case RIGHT -> {
                                adjustIcon();
                                label.setBounds(label.getX() + 1, label.getY(), label.getWidth(), label.getHeight());
                            }
                            case LEFT -> {
                                adjustIcon();
                                label.setBounds(label.getX() - 1, label.getY(), label.getWidth(), label.getHeight());
                            }
                            case UP -> {
                                adjustIcon();
                                label.setBounds(label.getX(), label.getY() - 1, label.getWidth(), label.getHeight());
                            }
                            case DOWN -> {
                                adjustIcon();
                                label.setBounds(label.getX(), label.getY() + 1, label.getWidth(), label.getHeight());
                            }
                        }
                        ++k;
                        timer -= speed;
                        // check can kill pacman
                        if (k >= DEATH_RADIUS) {
                            k = 0;
                            checkPacmanPosition();
                        }
                        count += 1;
                        if (count == cellSize) {
                            count = 0;
                            isExactlyInCell = true;
                        }
                    }
                    // simulate speed
                    Thread.sleep(speed);
                }
                System.out.println(this + ": round is ended");

            } catch (InterruptedException e) {
                throw new GameException(e);
            }
        }).start();
    }

    private int minimumMovesToCatchPacman(int x, int y, int pX, int pY) {
        // TODO: impl
        // assume that pacman will move from its position towards this ghost & ghost also moves towards pacman
        // порахувати відстань, яку пройде привид, за час за який вони зустрінуться
        // рахуючись на зустріч один одному
        int distance = Math.abs(x - pX) + Math.abs(y - pY);
        double ghostSpeed = 1d / speed;
        double pacmanSpeed = 1d / boardFrame.getMaxPacmanSpeed();
        double time = distance / (ghostSpeed + pacmanSpeed);
        return (int) (ghostSpeed * time);
    }

    public void pause() {
        synchronized (mutex) {
            isPaused.set(true);
        }
    }

    public void endTheRound() {
        synchronized (mutex) {
            System.out.println("GHOST: is end of round");
            isEndOfRound.set(true);
            disappear();
        }
    }

    private void disappear() {
        label.setIcon(null);
        System.out.println("GHOST (" + color + "): disappeared");
    }

    public void resume() {
        synchronized (mutex) {
            isPaused.set(false);
            System.out.println("NOTIFY GHOST TO RESUME");
            mutex.notify();
        }
    }

    private void freeze() {
        isFrozen = true;
        isExactlyInCell = true;
        try {
            mutex.wait();
        } catch (InterruptedException e) {
            throw new GameException(e);
        }
    }

    private void adjustIcon() {
        adjustIcon(direction.get());
    }

    private void adjustIcon(Direction direction) {
        label.setIcon(IconManager.getIcon(color, direction, cellSize));
    }

    @Override
    public String toString() {
        return "GHOST (" + color + ")";
    }

    public static void main(String[] args) {

    }
}
