package game.gui;

import game.Main;
import game.gui.board.BoardFrame;
import game.gui.utils.KeyPressedListener;
import game.gui.utils.UIUtils;
import game.logic.AppController;
import game.logic.Utils;
import game.logic.exception.AudioLoadingException;
import game.logic.exception.ImageProcessingException;
import game.logic.player.PlayersManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;

/*
 * View component in MVC pattern.
 * Also implements singleton pattern
 */
public class GameView {
    // singleton
    private static GameView instance;
    private final AppController controller;
    private final static String MENU_AUDIO_FILE_LOCATION;
    private final static String CUTSCENE_AUDIO_FILE_LOCATION;
    private final static String BUTTON_CLICK_SOUND_FILE_LOCATION;
    private final static String BACK_BUTTON_ICON_FILE_LOCATION;
    private final static String APP_ICON_LOCATION;
    private final static String MENU_BACKGROUND_IMAGE_LOCATION;
    public final static int FRAME_MENU_HEIGHT;
    private JFrame currentWindow;

    /**
     * Height of a frame
     */
    private int height;
    /**
     * Width of a frame
     */
    private int width;

    // block of static initialization
    static {
        instance = null;
        MENU_AUDIO_FILE_LOCATION = "/audio/menu.wav";
        CUTSCENE_AUDIO_FILE_LOCATION = "/audio/hints-back.wav";
        BUTTON_CLICK_SOUND_FILE_LOCATION = "/audio/button-click.wav";
        BACK_BUTTON_ICON_FILE_LOCATION = "/images/back.png";
        APP_ICON_LOCATION = "/images/logo.png";
        MENU_BACKGROUND_IMAGE_LOCATION = "/images/menu-background.jpg";
        FRAME_MENU_HEIGHT = 23;
    }

    // block of a non-static initialization
    {
        controller = AppController.getInstance();
    }

    private void setAppBackground(JFrame frame) throws ImageProcessingException {
        try {
            ImageIcon icon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(GameView.class.getResourceAsStream(MENU_BACKGROUND_IMAGE_LOCATION))
            ));
            JLabel bg = new JLabel(icon);
            bg.setBounds(0, 0, width, height);
            frame.add(bg);
        } catch (IOException | NullPointerException e) {
            // TODO: add parameters
            throw new ImageProcessingException();
        }
    }

    public static void setAppIcon(JFrame frame) throws ImageProcessingException {
        try {
            ImageIcon logo = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(Main.class.getResource(APP_ICON_LOCATION))
            ));
            frame.setIconImage(logo.getImage());
        } catch (IOException | NullPointerException e) {
            // TODO: add parameters to the constructor
            throw new ImageProcessingException();
        }
    }

    private GameView() {}

    // singleton
    public static GameView getInstance() {
        if (instance == null) {
            // TODO: instantiate an instance via init blocks
            instance = new GameView();
        }
        return instance;
    }

    public void openGame() {
        // TODO: generate map and set properties of it & and start it
        currentWindow = MenuFrame.getInstance(this);            // first time becomes visible by itself
    }

    public void backToMenu() {
        // TODO: backToMenu()
        SizeFrame.getInstance(this).setVisible(false);
        if (!(currentWindow instanceof MenuFrame)) {
            openMenu();
        }
    }

    /*
     * Hints and Scores can be opened only from menu window
     */
    private void openHints() {
        openInfoWindow(HintsFrame.getInstance(this));
    }

    private void openScores() {
        openInfoWindow(ScoresFrame.getInstance(this));
    }

    private void openInfoWindow(JFrame frame) {
        currentWindow.setVisible(false);
        currentWindow = frame;
        currentWindow.setVisible(true);
        try {
            UIUtils.playSound(CUTSCENE_AUDIO_FILE_LOCATION, true);
        } catch (AudioLoadingException e) {
            System.out.println("WARNING: failed to set hints background music because of " +
                    "AudioLoadingException: " + e.getMessage());
        }
    }

    private void openMenu() {
        if (currentWindow != null) {
            currentWindow.setVisible(false);
        }
        try {
            UIUtils.playSound(MENU_AUDIO_FILE_LOCATION, true);
        } catch (AudioLoadingException e) {
            System.out.println("WARNING: failed to set menu background music: " + e.getMessage());
        }
        currentWindow = MenuFrame.getInstance(this);
        currentWindow.setVisible(true);
    }

    private void openSize() {
        SizeFrame.getInstance(this).setVisible(true);
    }

    private void startNewGame(String nickname, int width, int height) {
        currentWindow.setVisible(false);
        currentWindow = new BoardFrame(width, height, nickname);
        try {
            UIUtils.playSound(BoardFrame.GAME_BACKGROUND_AUDIO_FILE_LOCATION, true);
        } catch (AudioLoadingException e) {
            System.out.println("Cannot play back music");
        }
        currentWindow.setVisible(true);
    }

    private void playButtonClickSound() {
        try {
            UIUtils.playSound(BUTTON_CLICK_SOUND_FILE_LOCATION, false);
        } catch (AudioLoadingException ex) {
            System.out.println("Cannot play button click sound because of AudioLoadingException: " +
                    ex.getMessage());
        }
    }

    private void setUpFrame(JFrame frame, String title, int w, int h) {
        width = w - FRAME_MENU_HEIGHT;
        height = h;

        UIUtils.setFrameDimension(frame, height, width);
        frame.setTitle(title);
        frame.setResizable(false);
        frame.setLayout(null);
    }

    // 👇 all inner class implement singleton pattern ⬇️

    /*
     * represents menu window
     * with navigation buttons
     */
    private class MenuFrame extends JFrame {
        // TODO: fully implement this class
        private static MenuFrame instance;

        public static MenuFrame getInstance(GameView view) {
            if (instance == null) {
                instance = view.new MenuFrame();
            }
            return instance;
        }

        private MenuFrame() {
            super();
            addWindowListener(new PacmanWindowListener());
            addKeyListener(controller);

            setUpFrame(this, "Menu", 500, 500);

            // add buttons: new game, high scores, hints, exit
            JButton newGameBtn = menuButton("New Game", 0);
            newGameBtn.addActionListener(e -> {
                playButtonClickSound();
                startNewGame();
            });           // function reference

            JButton highScoresBtn = menuButton("High Scores", 1);
            // lambda statement
            highScoresBtn.addActionListener(e -> {
                playButtonClickSound();
                showHighScores();
            });
            JButton hintsBtn = menuButton("Hints", 2);
            // lambda statement
            hintsBtn.addActionListener(e -> {
                playButtonClickSound();
                showHints();
            });

            JButton exitBtn = menuButton("Exit", 3);
            // lambda statement
            exitBtn.addActionListener(e -> {
                playButtonClickSound();
                setVisible(false);
                Utils.sleep(0.2);
                UIUtils.exit();
            });

            addKeyListener((KeyPressedListener) e -> {
                switch (e.getKeyCode()) {
                    case 78 -> startNewGame();
                    case 83 -> showHighScores();
                    case 72 -> showHints();
                    case 69 -> {
                        setVisible(false);
                        UIUtils.exit();
                    }
                }
            });

            try {       // trying to set menu background
                setAppBackground(this);
            } catch (ImageProcessingException e) {
                Utils.sleep(0.2);
                UIUtils.processException(this, e);
            }

            setVisible(true);

            try {       // trying to set application icon
                setAppIcon(this);
            } catch (ImageProcessingException e) {
                Utils.sleep(0.2);
                UIUtils.processException(this, e);
            }

            try {       // trying to start playing background menu music
                UIUtils.playSound(MENU_AUDIO_FILE_LOCATION, true);
            } catch (AudioLoadingException e) {
                Utils.sleep(0.2);
                UIUtils.processException(this, e);
            }


        }

        JButton menuButton(String text, int n) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.PLAIN, 24));
            btn.setBounds(140, 110 + n * 70, 180, 30);
            btn.setFocusable(false);
            add(btn);
            return btn;
        }

        void startNewGame() {
            System.out.println("New game is starting...");
            openSize();
        }

        void showHighScores() {
            System.out.println("Look at high scores");
            openScores();
        }

        void showHints() {
            System.out.println("Look at hints");
            openHints();
        }
    }

    private class ScoresFrame extends JFrame {
        // TODO: fully implement this class
        private static ScoresFrame instance;
        private JList<String> uiList;

        public static ScoresFrame getInstance(GameView view) {
            if (instance == null) {
                instance = view.new ScoresFrame();
            } else {
                instance.refresh();
            }
            return instance;
        }

        private ScoresFrame() {

            // TODO: add ability to mutate & check prev mute state
            addWindowListener(new PacmanWindowListener());

            setUpFrame(this, "Best Scores", 500, 500);

            addScores();

            try {
                setAppIcon(this);
            } catch (ImageProcessingException e) {
                System.out.println("WARNING: failed to set frame icon because of " +
                        "ImageProcessingException: " + e.getMessage());
            }

            try {
                add(backToMenuButton());
            } catch (ImageProcessingException e) {
                System.out.println("Failed to add icon to 'go back to menu' button");
            }
        }

        private void refresh() {
            remove(uiList);
            addScores();
        }

        private void addScores() {
            var manager = PlayersManager.getInstance();
            uiList = new JList<>(manager.getScoreBoard());
            uiList.setBounds(80, 50, 300, 400);
            uiList.setFont(new Font("Arial", Font.PLAIN, 22));
            uiList.setAlignmentX(Component.CENTER_ALIGNMENT);
            uiList.addKeyListener(AppController.getInstance());
            add(uiList);
        }

        static JLabel backToMenuButton() throws ImageProcessingException {
            JLabel btn;
            try {
                ImageIcon backIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(
                        ScoresFrame.class.getResourceAsStream(BACK_BUTTON_ICON_FILE_LOCATION)
                )));
                btn = new JLabel(backIcon);
                btn.setBounds(10, 10, 30, 30);
            } catch (IOException | NullPointerException e) {
                btn = new JLabel("Open Menu");
                btn.setFont(new Font("Arial", Font.PLAIN, 20));
                btn.setBounds(10, 10, 140, 25);
                btn.setFocusable(false);
            }
            btn.addMouseListener(new BackToMenuMouseListener());
            System.out.println("back to menu button is added: " + btn);
            return btn;
        }
    }

    /*
     * represents window with hints
     * also implements singleton pattern
     */
    private class HintsFrame extends JFrame {
        // TODO: fully implement this class
        private static HintsFrame instance;

        public static HintsFrame getInstance(GameView view) {
            if (instance == null) {
                instance = view.new HintsFrame();
            }
            return instance;
        }

        private HintsFrame() {

            // TODO: add ability to mutate & check prev mute state
            super();
            addWindowListener(new PacmanWindowListener());
            addKeyListener(AppController.getInstance());

            setUpFrame(this, "Hints", 500, 500);

            addHintsText();

            try {
                setAppIcon(this);
            } catch (ImageProcessingException e) {
                System.out.println("WARNING: failed to set frame icon because of " +
                        "ImageProcessingException: " + e.getMessage());
            }

            try {
                add(ScoresFrame.backToMenuButton());
            } catch (ImageProcessingException e) {
                System.out.println("Failed to add icon to 'go back to menu' button");
            }
        }

        private void addHintsText() {
            String hints = """
                    GAMEPLAY:
                     1. Game won't start until you press Enter
                     2. To pause (freeze) the game press 'p'
                     3. You can control pacman via arrow or
                        WASD keys
                     
                    SHORTCUTS:
                     1. Press Ctrl + Shift + Q at any time in
                        the program to go back to menu. Don't
                        worry your progress will be saved.
                     2. Board Size window: you can press
                        Enter to immediately start the game
                     3. Menu window: each button has respective
                        shortcut:
                         - New Game - 'n'
                         - High Scores - 's'
                         - Hints - 'h'
                         - Exit - 'e'
                         
                    GENERAL:
                     1. No matter how you close the program
                        your progress will be saved
                    
                    """;
            JTextArea text = new JTextArea(24, 20);
            text.addKeyListener(AppController.getInstance());
            text.setFont(new Font("Arial", Font.PLAIN, 20));
            text.setEditable(false);
            text.setText(hints);
            JScrollPane pane = new JScrollPane(text);
            pane.setBounds(15, 50, 440, 400);
            add(pane);
        }
    }

    /*
     * When this window opens, menu window leaves opened as well
     */
    private class SizeFrame extends JFrame {
        static SizeFrame instance;
        JFormattedTextField nicknameFld;
        JFormattedTextField widthFld;
        JFormattedTextField heightFld;

        static SizeFrame getInstance(GameView view) {
            if (instance == null) {
                instance = view.new SizeFrame();
            }
            return instance;
        }

        private SizeFrame() {
            super();

            setUpFrame(this, "Board Size", 400, 340);
            addKeyListener(AppController.getInstance());

            JLabel lbl = new JLabel("Choose size of a Board");
            lbl.setBounds(80, 20, 240, 40);
            lbl.setFont(new Font("Arial", Font.PLAIN, 20));
            add(lbl);

            lbl = new JLabel("Nickname: ");
            lbl.setBounds(10, 70, 120, 40);
            lbl.setHorizontalAlignment(JLabel.RIGHT);
            lbl.setFont(new Font("Arial", Font.PLAIN, 20));
            add(lbl);

            nicknameFld = new JFormattedTextField();
            nicknameFld.setFont(new Font("Arial", Font.PLAIN, 16));
            nicknameFld.setBounds(130, 84, 200, 20);
            nicknameFld.setBorder(BorderFactory.createCompoundBorder(
                    nicknameFld.getBorder(),
                    BorderFactory.createEmptyBorder(0, 0, 1, 0)));
            nicknameFld.addKeyListener(new RestrictLength(nicknameFld, 16));
            nicknameFld.addKeyListener((KeyPressedListener) e -> {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    play();
                }
            });
            nicknameFld.addKeyListener(AppController.getInstance());
            // TODO: maybe remove
            nicknameFld.setText(PlayersManager.getInstance().getRandomPlayer());
            add(nicknameFld);


            lbl = new JLabel("Width: ");
            lbl.setHorizontalAlignment(JLabel.RIGHT);
            lbl.setFont(new Font("Arial", Font.PLAIN, 20));
            lbl.setBounds(10, 120, 120, 30);
            add(lbl);

            widthFld = new JFormattedTextField();
            widthFld.setFont(new Font("Arial", Font.PLAIN, 16));
            widthFld.setBounds(130, 128, 45, 20);
            widthFld.setBorder(BorderFactory.createCompoundBorder(
                    widthFld.getBorder(),
                    BorderFactory.createEmptyBorder(0, 0, 1, 0)));
            widthFld.addKeyListener(new SizeKeyListener(widthFld));
            widthFld.addKeyListener((KeyPressedListener) e -> {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    play();
                }
            });
            widthFld.addKeyListener(AppController.getInstance());
            // TODO: maybe remove
            widthFld.setText("35");
            add(widthFld);

            lbl = new JLabel("Height: ");
            lbl.setHorizontalAlignment(JLabel.RIGHT);
            lbl.setFont(new Font("Arial", Font.PLAIN, 20));
            lbl.setBounds(10, 170, 120, 30);
            add(lbl);

            heightFld = new JFormattedTextField();
            heightFld.setFont(new Font("Arial", Font.PLAIN, 16));
            heightFld.setBounds(130, 178, 45, 20);
            heightFld.setBorder(BorderFactory.createCompoundBorder(
                    heightFld.getBorder(),
                    BorderFactory.createEmptyBorder(0, 0, 1, 0)));
            heightFld.addKeyListener(new SizeKeyListener(heightFld));
            heightFld.addKeyListener((KeyPressedListener) e -> {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    play();
                }
            });
            heightFld.addKeyListener(AppController.getInstance());
            // TODO: maybe remove
            heightFld.setText("24");
            add(heightFld);

            var btn = new JButton("Play!");
            btn.addActionListener(e -> {
                playButtonClickSound();
                if (validateInputFields()) {
                    play();
                } else {
                    JOptionPane.showMessageDialog(this, "Width or height was" +
                            "invalid, so they have been changed to the nearest acceptable values");
                }
            });
            btn.setFont(new Font("Arial", Font.PLAIN, 20));
            btn.setBounds(150, 230, 80, 34);
            btn.setFocusable(false);
            add(btn);

            try {
                setAppIcon(this);
            } catch (ImageProcessingException e) {
                System.out.println("WARNING: failed to set frame icon because of " +
                        "ImageProcessingException: " + e.getMessage());
            }
        }

        private boolean validateInputFields() {
            // TODO: check if board size is valid, identify user by nickname
            var heightText = heightFld.getText();
            boolean isValid = true;
            if (heightText.isEmpty() || Integer.parseInt(heightText) < 10) {
                heightFld.setText("10");
                isValid = false;
            } else if (Integer.parseInt(heightText) > 100) {
                heightFld.setText("100");
                isValid = false;
            }
            var widthText = widthFld.getText();
            if (widthText.isEmpty() || Integer.parseInt(widthText) < 10) {
                widthFld.setText("10");
                isValid = false;
            } else if (Integer.parseInt(widthText) > 100) {
                widthFld.setText("100");
                isValid = false;
            }
            return isValid;
        }

        void play() {
            // TODO: impl
            System.out.println("New game is starting...");
            int height = Integer.parseInt(heightFld.getText());
            int width = Integer.parseInt(widthFld.getText());
            var manager = PlayersManager.getInstance();
            String nickname = nicknameFld.getText();
            if (manager.isNewPlayer(nickname)) {
                int res = JOptionPane.showConfirmDialog(this, "There is no player with" +
                        "this nickname. Do you want to create a new one?", "New Nickname", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    System.out.println("Creating new player...");
                    manager.addPlayer(nickname);
                    setVisible(false);
                    GameView.getInstance().startNewGame(nickname, width, height);
                }
            } else {
                setVisible(false);
                GameView.getInstance().startNewGame(nickname, width, height);
            }
        }



        class RestrictLength implements KeyListener {
            private final int maxLength;
            private final JFormattedTextField field;

            RestrictLength(JFormattedTextField field, int maxLength) {
                this.field = field;
                this.maxLength = maxLength;
            }

            @Override
            public void keyTyped(KeyEvent e) {
                var text = field.getText();
                int len = text.length();
                char lastChar = e.getKeyChar();
                if (len == maxLength ||
                    Character.isWhitespace(lastChar) ||
                    Pattern.matches("\\p{Punct}", String.valueOf(lastChar))
                    && lastChar != '_') {
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        }

        class SizeKeyListener implements KeyListener {

            private final JFormattedTextField field;

            SizeKeyListener(JFormattedTextField field) {
                this.field = field;
            }

            @Override
            public void keyTyped(KeyEvent e) {
                var text = field.getText();
                var len = text.length();
                var c = e.getKeyChar();
                if (len == 0 && c == '0' ||
                    !Character.isDigit(c) ||
                    len >= 3) {
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        }
    }

    static class BackToMenuMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            System.out.println("Going back to menu");
            try {
                UIUtils.playSound(BUTTON_CLICK_SOUND_FILE_LOCATION, false);
            } catch (AudioLoadingException ex) {
                System.out.println("Cannot play button click sound because of AudioLoadingException: " +
                        ex.getMessage());
            }
            GameView.getInstance().openMenu();
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public static class PacmanWindowListener implements WindowListener {

        private final BoardFrame boardFrame;

        public PacmanWindowListener() {
            this(null);
        }

        public PacmanWindowListener(BoardFrame boardFrame) {
            this.boardFrame = boardFrame;
        }

        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {
            // closing board frame (game window)
            if (boardFrame != null) {
                boardFrame.endGame();
            }
            UIUtils.exit();
        }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowIconified(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }
    }
}
