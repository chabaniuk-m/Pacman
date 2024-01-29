package game.logic;

import game.gui.GameView;
import game.gui.utils.UIUtils;
import game.gui.board.BoardFrame;
import game.logic.player.PlayersManager;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/*
 * Controller in MVC pattern.
 * Also implements singleton pattern
 */
public class AppController implements KeyListener {

    // singleton
    private static AppController instance;

    private boolean isShiftDown;
    private boolean isCtrlDown;

    // block of a static initialization
    static {
        instance = null;
    }

    // block of a non-static initialization
    {
        isShiftDown = false;
        isCtrlDown = false;
    }

    public static AppController getInstance() {
        if (instance == null) {
            instance = new AppController();
        }
        return instance;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char key = e.getKeyChar();
        if (key == 'm' && !(e.getSource() instanceof JFormattedTextField)) {
            // TODO: mute
            if (UIUtils.isMutated()) {
                System.out.println("Unmute all sounds music");
                UIUtils.unmute();
            } else {
                System.out.println("Mute all sounds music");
                UIUtils.mute();
            }
        }

    }

    @Override
    public void keyPressed(KeyEvent e) {char key = e.getKeyChar();
        int code = e.getKeyCode();
        if (e.getSource() instanceof BoardFrame board) {
            switch (code) {
                // arrow or WASD
                case 37, 65 -> board.changeDirection(Direction.LEFT);
                case 38, 87 -> board.changeDirection(Direction.UP);
                case 39, 68 -> board.changeDirection(Direction.RIGHT);
                case 40, 83 -> board.changeDirection(Direction.DOWN);
            }
        }
        if (code == KeyEvent.VK_SHIFT) {
            if (!isShiftDown) {
                isShiftDown = true;
            }
        } else if (code == KeyEvent.VK_CONTROL) {
            if (!isCtrlDown) {
                isCtrlDown = true;
            }
        } else if (code == 81) {        // Q pressed
            if (isCtrlDown && isShiftDown) {
                if (e.getSource() instanceof BoardFrame board) {
                    board.endGame();
                }
                UIUtils.stopChomp();
                GameView.getInstance().backToMenu();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_SHIFT) {
            if (isShiftDown) {
                isShiftDown = false;
            }
        } else if (code == KeyEvent.VK_CONTROL) {
            if (isCtrlDown) {
                isCtrlDown = false;
            }
        }
    }
}
