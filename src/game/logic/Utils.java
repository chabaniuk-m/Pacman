package game.logic;

import java.util.Random;

// static utility functions for PacmanGame
public class Utils {

    // TODO: remove labels from BoardFrame
    private final static Random random = new Random();
    public static void sleep(double seconds) {
        try {
            Thread.sleep((long)(seconds * 1000));
        } catch (InterruptedException e) {
            // TODO: change to custom exception
            throw new RuntimeException(e);
        }
    }

    /**
     * @param chance from 0 to 1
     * @return true or false
     */
    public static boolean success(double chance) {
        return random.nextDouble() < chance;
    }

    public static double dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
