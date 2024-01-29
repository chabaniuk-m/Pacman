package game;

import game.gui.GameView;

/*
 * map must be dynamically generated (from 10x10 to 100x100)
 * pacman is 1 cell. There must be a window with monsters in the center
 * GIFs are forbidden
 * go through walls - 1 teleport or 0
 * no super dots
 * monsters can leave boosts
 */

public class Main {

    public static void main(String[] args) {

        GameView game = GameView.getInstance();
        game.openGame();
    }
}
