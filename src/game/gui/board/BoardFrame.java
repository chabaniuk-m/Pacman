package game.gui.board;

import game.gui.GameView;
import game.gui.hero.Ghost;
import game.gui.utils.KeyPressedListener;
import game.gui.utils.UIUtils;
import game.gui.hero.Pacman;
import game.logic.AppController;
import game.logic.Direction;
import game.logic.board.Cell;
import game.logic.exception.AudioLoadingException;
import game.logic.exception.GameException;
import game.logic.exception.ImageProcessingException;
import game.logic.player.PlayersManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BoardFrame extends JFrame {
    public final static String GAME_BACKGROUND_AUDIO_FILE_LOCATION;
    private final Object gameMutex;
    private final AtomicBoolean isRoundStarted;
    private final Random random;
    private static ImageIcon pacmanLive;
    /**
     * horizontal
     */
    private Pacman pacman;
    public GhostCage ghostCage;
    private JLabel pacmanLbl;
    private final JLabel winLoseLbl;
    private static final int PACMAN_ICON_SIZE;
    final int height;
    final int width;
    private final int frameHeight;
    private final int frameWidth;
    final int cellSize;
    private int score;
    private boolean isGameStarted;
    private boolean isGameEnded;
    private boolean isPaused;
    private JLabel scoreLbl;               // Score: lbl,       <name-lbl>
    /**
     * apple - double money
     * chilly pepper - increase velocity
     * melon - invisible
     * eggplant - eat monsters
     * banana - +1 live
     * cherry - -1 ghost for 30 sec
     */
    private JLabel power;               // pman, pman...     power
    private int lives;                  //
    private final ArrayList<JLabel> liveLabels = new ArrayList<>(8);
    private final String nickname;
    private final GameTable table;
    private JLabel stopWatch;
    private final Object stopWatchMutex = new Object();

    {
        gameMutex = new Object();
        isRoundStarted = new AtomicBoolean();
        score = 0;
        lives = 0;
        isGameStarted = false;
        isGameEnded = false;
        random = new Random();
    }

    static {
        pacmanLive = UIUtils.loadIcon("/images/pacman/right.png");
        GAME_BACKGROUND_AUDIO_FILE_LOCATION = "/audio/game-back.wav";
        PACMAN_ICON_SIZE = 20;
        var resizedImage = pacmanLive.getImage()
                .getScaledInstance(PACMAN_ICON_SIZE, PACMAN_ICON_SIZE, Image.SCALE_SMOOTH);
        pacmanLive = new ImageIcon(resizedImage);
    }

    public BoardFrame(int width, int height, String nickname) {
        this.width = width;
        this.height = height;
        this.nickname = nickname;

        int k = Math.max(height, width - 10);
        if (k > 75) {
            cellSize = Cell.TINY_SIZE;
        } else if (k > 50) {
            cellSize = Cell.SMALL_SIZE;
        } else if (k > 25) {
            cellSize = Cell.MEDIUM_SIZE;
        } else {
            cellSize = Cell.LARGE_SIZE;
        }

        table = new GameTable(this);

        // frame
        addWindowListener(new GameView.PacmanWindowListener(this));
        addKeyListener(AppController.getInstance());
        setResizable(false);
        setLayout(null);
        System.out.printf("""
                width = %d
                height = %d
                """, width, height);
        frameHeight = height * cellSize + 100;
        frameWidth = width * cellSize;
        UIUtils.setFrameDimension(this,
                frameHeight + GameView.FRAME_MENU_HEIGHT,
                frameWidth + GameView.FRAME_MENU_HEIGHT - 10);
        try {
            GameView.setAppIcon(this);
        } catch (ImageProcessingException e) {
            System.out.println("Failed to set icon to board frame");
        }
        setTitle("Pacman - Game");
        getContentPane().setBackground(Color.BLACK);

        addLabelComponents();

        addKeyListener((KeyPressedListener) e -> {
            switch (e.getKeyCode()) {
                case 10 -> startNewRound();
                case 80 -> pauseResume();
            }
        });

        winLoseLbl = new JLabel();
        winLoseLbl.setBounds((getWidth() - 240) / 2, (getHeight() - 100) / 2 - 25, 240, 100);
        winLoseLbl.setHorizontalAlignment(SwingConstants.CENTER);
        winLoseLbl.setFont(new Font("Arial", Font.PLAIN, 48));
        add(winLoseLbl);

        beginGameLoop();
    }

    private void startNewRound() {
        if (!isRoundStarted.get()) {
            isRoundStarted.set(true);
            synchronized (gameMutex) {
                gameMutex.notify();
            }
            winLoseLbl.setVisible(false);
        }
    }

    private void beginGameLoop() {
        new Thread(() -> {
            try {
                do {

                    prepareGhostCage();
                    preparePacman();

                    isRoundStarted.set(false);

                    /*
                     * Waiting while user starts the round by pressing Enter
                     */
                    synchronized (gameMutex) {
                        gameMutex.wait();
                    }
                    removeOneLive();
                    pacman.start();
                    ghostCage.start();
                    synchronized (stopWatchMutex) {
                        stopWatchMutex.notify();
                    }
                    /*
                     * Waiting till users ends the round by collecting all
                     * points or dying from a ghost
                     */
                    synchronized (gameMutex) {
                        gameMutex.wait();
                    }
                    if (table.allPointsCollected()) {
                        displayWin();
                        break;
                    } else if (lives == 0) {
                        displayLose();
                        break;
                    }
                    clearGameBoard();
                    // otherwise prepare for the next round
                } while (lives > 0);
            } catch (InterruptedException e) {
                throw new GameException();
            }
        }).start();
    }

    public void win() {
        displayWin();
        pause();
    }

    private void clearGameBoard() {
        // TODO: clear the board from ghosts and pacman
        System.out.println("Clearing the board");
    }

    private void displayLose() {
        displayWinLoseText("You lose!", Color.RED);
    }

    private void displayWin() {
        displayWinLoseText("You won!", Color.GREEN);
    }

    private void displayWinLoseText(String text, Color color) {
        winLoseLbl.setText(text);
        winLoseLbl.setForeground(color);
        winLoseLbl.setVisible(true);
    }

    private void removeOneLive() {
        lives -= 1;
        var lbl = liveLabels.get(lives);
        lbl.setVisible(false);
    }

    private void pauseResume() {
        if (isRoundStarted.get()) {
            if (isPaused) {
                resume();
            } else {
                pause();
            }
        }
    }

    private void resume() {
        pacman.resume();
        ghostCage.resume();
        synchronized (stopWatchMutex) {
            isPaused = false;
            stopWatchMutex.notify();
        }
    }

    private void pause() {
        pacman.pause();
        ghostCage.pause();
        synchronized (stopWatchMutex) {
            isPaused = true;
        }
    }

    public String getPlayNick() {
        return nickname;
    }

    public int getScore() {
        return score;
    }

    private int getNumberOfGhosts() {
        // TODO: respectively to width & height
        int lambda = width * height;
        if (lambda < 150) {
            return 2;
        }
        if (lambda < 250) {
            return 3;
        }
        if (lambda < 500) {
            return 4;
        }
        if (lambda < 800) {
            return 5;
        }
        if (lambda < 1500) {
            return 6;
        }
        if (lambda < 3000) {
            return 7;
        }
        if (lambda < 5000) {
            return 8;
        }
        if (lambda < 8000) {
            return 9;
        }
        return 10;
    }

    public int getMaxPacmanSpeed() {
        return pacman.getMaxSpeed();
    }

    public Point getPacmanCoordinates() {
        return pacman.getCoordinates();
    }

    private final AtomicInteger coinsMultiplier = new AtomicInteger(1);
    private final Object doubleCoinsMutex = new Object();

    public void doubleCoins() {
        new Thread(() -> {
            try {
                synchronized (doubleCoinsMutex) {
                    if (coinsMultiplier.get() == 1) {
                        addPower("Double Coins");
                    }
                    coinsMultiplier.set(coinsMultiplier.get() * 2);
                }
                UIUtils.playSound("/audio/coins.wav", false);
                Thread.sleep(18_000);
                synchronized (doubleCoinsMutex) {
                    coinsMultiplier.set(coinsMultiplier.get() / 2);
                    if (coinsMultiplier.get() == 1) {
                        removePower("Double Coins");
                    }
                }
            } catch (Exception e) {
                throw new GameException(e);
            }
        }).start();
    }

    public void pacmenMoveToNextCell(int x, int y) {
        System.out.println("Pacman moved to next cell");
        Point point = table.getBoardCoordinates(x, y);
        int points = table.eatPointAt(point.y, point.x) * coinsMultiplier.get();
        // TODO: if double point power is on...
        if (updateScore(points)) {
            UIUtils.startChomp();
        } else {
            UIUtils.stopChomp();
        }
    }

    private void prepareGhostCage() {
        var point = table.getCageCoordinates();
        ghostCage = new GhostCage(point.x, point.y, getNumberOfGhosts());
    }

    private void preparePacman() {

        // TODO: refactor pacman hero
        pacmanLbl = new JLabel();
        pacman = table.createPacman(pacmanLbl);

    }

    private boolean canMove(Direction direction, int i, int j) {
        var d = pacman.getDirection();
        boolean isLeftUpDirection = d == Direction.LEFT || d == Direction.UP;
        switch (direction) {
            case RIGHT -> {
                if (!pacman.isExactlyInCell()) {
                    j += 1;
                }
                return j < width && table.isFreeAt(i, j);
            }
            case LEFT -> {
                if (!pacman.isExactlyInCell() && !isLeftUpDirection) {
                    j -= 1;
                }
                return j >= 0 && table.isFreeAt(i, j);
            }
            case UP -> {
                if (!pacman.isExactlyInCell() && !isLeftUpDirection) {
                    i -= 1;
                }
                return i >= 0 && table.isFreeAt(i, j);
            }
            case DOWN -> {
                if (!pacman.isExactlyInCell()) {
                    i += 1;
                }
                return i < height && table.isFreeAt(i, j);
            }
        }

        throw new GameException();
    }

    private void addLabelComponents() {
        JLabel text = new JLabel("Score:");
        text.setBounds(20, 20, 50, 30);
        text.setHorizontalAlignment(JLabel.RIGHT);
        text.setForeground(Color.WHITE);
        add(text);

        scoreLbl = new JLabel("0");
        scoreLbl.setBounds(75, 20, 80, 30);
        scoreLbl.setHorizontalAlignment(JLabel.LEFT);
        scoreLbl.setForeground(Color.WHITE);
        add(scoreLbl);

        JLabel nick = new JLabel(nickname);
        nick.setBounds(frameWidth - 230, 20, 200, 30);
        nick.setHorizontalAlignment(JLabel.RIGHT);
        nick.setForeground(Color.WHITE);
        add(nick);

        power = new JLabel("no power");
        power.setBounds(frameWidth - 420, frameHeight - 40, 400, 30);
        power.setHorizontalAlignment(JLabel.RIGHT);
        power.setForeground(Color.WHITE);
        add(power);

        addStopWatch();

        addLive();
        addLive();
        addLive();
        addLive();
        addLive();
        removeOneLive();
        removeOneLive();
    }

    private void addStopWatch() {
        stopWatch = new JLabel("00:00.0");
        stopWatch.setBounds((frameWidth - 100) / 2, 20, 100, 30);
        stopWatch.setHorizontalAlignment(SwingConstants.CENTER);
        stopWatch.setForeground(Color.WHITE);
        add(stopWatch);
        new Thread(() -> {
            try {
                synchronized (stopWatchMutex) {
                    stopWatchMutex.wait();
                }
                while (true) {
                    Thread.sleep(100);
                    synchronized (stopWatchMutex) {
                        if (isPaused) {
                            System.out.println("STOPWATCH: waiting...");
                            stopWatchMutex.wait();
                        }
                        String text = stopWatch.getText();
                        String[] arr = text.split(":");
                        int minutes = Integer.parseInt(arr[0]);
                        int seconds = Integer.parseInt(arr[1].replace(".", ""));
                        int time = minutes * 600 + seconds + 1;
                        System.out.println("time = " + time);
                        minutes = time / 600;
                        time %= 600;
                        int secs = time;
                        String m = "" + minutes;
                        m = m.length() == 1 ? "0" + m : m;
                        String s = "" + secs / 10;
                        s = s.length() == 1 ? "0" + s : s;
                        String ms = ""  + secs % 10;
                        System.out.printf("⌚ Update time: m = %s, s = %s, ms = %s\n", m, s, ms);
                        stopWatch.setText(String.format("%s:%s.%s", m, s, ms));
                    }
                }
            } catch (InterruptedException e) {
                throw new GameException(e);
            }
        }).start();
    }

    public void changeDirection(Direction newDirection) {
        synchronized (pacman.mutex) {
            if (!isPaused && !isGameEnded) {
                var pacmanDirection = pacman.getDirection();
                System.out.println("Pacman's direction is " + pacmanDirection);
                if (newDirection != pacmanDirection) {
                    System.out.println("Direction is changing to " + newDirection);
                    synchronized (pacman.mutex) {
                        Point pacmanCoordinates = pacman.getCoordinates();
                        System.out.println("pacmanCoordinates = " + pacmanCoordinates);
                        var point = table.getBoardCoordinates(pacmanCoordinates.x, pacmanCoordinates.y);
                        System.out.println("respective point on board: " + point);
                        int j = point.x;
                        int i = point.y;
                        if (pacmanDirection.isOpposite(newDirection)) {
                            System.out.println("Direction is opposite");
                            pacman.setTarget(table.findTarget(newDirection, j, i));
                            pacman.setOppositeDirection(newDirection);
                            pacman.mutex.notify();
                        } else {
                            System.out.println("Direction is not opposite");
                            if (!pacman.isExactlyInCell()) {
                                switch (pacmanDirection) {
                                    case RIGHT -> j += 1;
                                    case DOWN -> i += 1;
                                }
                            }
                            System.out.printf("Checking if pacman can move from point: i = %d, j = %d\n", i, j);
                            if (canMove(newDirection, i, j)) {
                                System.out.println("Change pacman direction and notify it");
                                if (pacman.isFrozen()) {
                                    pacman.setTarget(table.findTarget(newDirection, j, i));
                                    pacman.setDirection(newDirection);
                                } else {
                                    pacman.setNewTarget(table.findTarget(newDirection, j, i));
                                    pacman.setNewDirection(newDirection);
                                }
                                pacman.mutex.notify();
                            } else {
                                System.out.println("Can not move in that direction");
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean updateScore(int toAdd) {
        if (toAdd == 0) {
            return false;
        }
        score += toAdd;
        scoreLbl.setText(String.valueOf(score));
        return true;
    }

    private void addLive() {
        JLabel lbl = new JLabel(pacmanLive);
        lbl.setBounds(20 + lives * (10 + PACMAN_ICON_SIZE),
                frameHeight - PACMAN_ICON_SIZE - 20,
                PACMAN_ICON_SIZE,
                PACMAN_ICON_SIZE);
        liveLabels.add(lbl);
        add(lbl);
        ++lives;
    }

    private final AtomicBoolean isInvisible = new AtomicBoolean();
    private final AtomicInteger invisibleTimer = new AtomicInteger(0);

    void playInvisibleSound() {
        UIUtils.startInvisibleSound("/audio/invisible.wav");
        new Thread(() -> {
            try {
                Thread.sleep(28_800);
                while (true) {
                    synchronized (invisibleTimer) {
                        if (!isInvisible.get()) {
                            UIUtils.stopInvisibleSound();
                            return;
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void makePacmanInvisible() {
        if (invisibleTimer.get() > 200) {
            synchronized (invisibleTimer) {
                invisibleTimer.set(invisibleTimer.get() + 28_900);
            }
        }
        invisibleTimer.set(28_900);

        new Thread(() -> {
            synchronized (invisibleTimer) {
                isInvisible.set(true);
                addPower("Invisible");
            }
            playInvisibleSound();
            while (invisibleTimer.get() > 0) {
                synchronized (invisibleTimer) {
                    invisibleTimer.set(invisibleTimer.get() - 50);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new GameException(e);
                }
            }
            synchronized (invisibleTimer) {
                isInvisible.set(false);
                removePower("Invisible");
            }
        }).start();
    }

    public void killPacman() {
        if (isInvisible.get()) {
            return;
        }
        try {
            UIUtils.stopChomp();
            ghostCage.removeGhosts();
            Thread.sleep(200);
            UIUtils.stopChomp();
            pacman.die();
            // wait some time before next round
            Thread.sleep(1000);
            UIUtils.stopChomp();
        } catch (InterruptedException e) {
            throw new GameException(e);
        }

        synchronized (gameMutex) {
            gameMutex.notify();
        }
    }

    public void endGame() {
        // TODO: actually en the game
        synchronized (pacman.mutex) {
            pacman.stop();
        }
        PlayersManager.getInstance().updatePlayer(nickname, score);
    }

    public ArrayList<Direction> getAvailableDirectionsFrom(int x, int y) {
        return table.possibleDirectionsFrom(x, y);
    }

    public void addFruit(Ghost.Color color, int x, int y) {
        Point point = table.getBoardCoordinates(x, y);
        table.addFruitAt(color, point.y, point.x);
    }

    private final ArrayList<String> powers = new ArrayList<>();

    public void addPower(String text) {
        powers.add(text);
        power.setText(String.join(" | ", powers));
    }

    public void removePower(String text) {
        powers.remove(text);
        if (powers.isEmpty()) {
            power.setText("no power");
        } else {
            power.setText(String.join(" | ", powers));
        }
    }

    public void speedUpPacman() {
        pacman.speedup();
    }

    public void addOneLive() {
        if (lives < 5) {
            try {
                UIUtils.playSound("/audio/heal-up.wav", false);
            } catch (AudioLoadingException e) {
                throw new GameException(e);
            }
            liveLabels.get(lives).setVisible(true);
            lives += 1;
            System.out.printf("""
                    ONE LIVE IS ADDED (lives count = %s)
                    """, lives);
        }
    }

    /**
     * Manages all Ghosts
     */
    public class GhostCage {
        ArrayList<Ghost> ghosts = new ArrayList<>(10);

        /**
         * uses to add new ghost to the game
         */
        static final Deque<Ghost.Color> newGhostQueue;

        public final Object spawnerMutex = new Object();
        private final Object ghostQueueMutex = new Object();
        int cageX;
        int cageY;

        final Deque<Ghost.Color> spawnQueue;

        {
            spawnQueue = new LinkedList<>();
        }

        static {
            newGhostQueue = new LinkedList<>();
            refillNewGhostQueue();
        }

        static void refillNewGhostQueue() {
            ArrayList<Ghost.Color> list = new ArrayList<>() {{
               addAll(Arrays.asList(Ghost.Color.values())) ;
            }};
            Collections.shuffle(list);
            newGhostQueue.addAll(list);
        }

        /**
         * (cage is drawn in GameTable)
         * @param x - pixel coordinate of cage
         * @param y - pixel coordinate of cage
         */
        GhostCage(int x, int y, int numberOfGhost) {
            cageX = x;
            cageY = y;
            assert numberOfGhost > 0;
            for (int k = 0; k < numberOfGhost; ++k) {
                addGhostToSpawnQueue();
            }
            addNewGhost();
        }

        private void addNewGhost() {
            var label = new JLabel();
            label.setBounds(cageX, cageY, cellSize, cellSize);
            add(label);
            ghosts.add(new Ghost(spawnQueue.poll(), Direction.UP, cellSize, label, BoardFrame.this));
        }

        private void addGhostToSpawnQueue() {
            if (newGhostQueue.isEmpty()) {
                refillNewGhostQueue();
            }
            synchronized (ghostQueueMutex) {
                spawnQueue.addLast(newGhostQueue.poll());
                ghostQueueMutex.notify();
            }
        }

        /**
         * spawn ghosts in separate thread
         */
        private void startSpawnGhosts() {
            new Thread(() -> {
                try {
                    while (!isGameEnded) {
                        synchronized (spawnerMutex) {
                            ghosts.get(ghosts.size() - 1).start();
                            spawnerMutex.wait();                    // waiting the ghost to leave the cage
                        }
                        Thread.sleep(random.nextInt(200, 1000));
                        // put new ghost in the cage
                        synchronized (ghostQueueMutex) {
                            if (spawnQueue.isEmpty()) {
                                ghostQueueMutex.wait();                // waiting till new ghost is added to spawn queue
                            }
                            addNewGhost();
                        }
                    }
                } catch (InterruptedException e) {
                    throw new GameException(e);
                }
            }).start();
        }

        public void start() {
            System.out.println("Ghost cage started");
            startSpawnGhosts();
        }

        public void pause() {
            ghosts.forEach(Ghost::pause);
            System.out.println("Pause " + ghosts.size() + " ghosts");
        }

        public void resume() {
            ghosts.forEach(Ghost::resume);
        }

        public void removeGhosts() {
            System.out.println("Start removing ghosts");
            ghosts.forEach(Ghost::endTheRound);
            System.out.println(ghosts.size() + " removed from board");
            ghosts.clear();
        }
    }
}
