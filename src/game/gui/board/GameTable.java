package game.gui.board;

import game.gui.hero.Ghost;
import game.gui.hero.Pacman;
import game.gui.utils.UIUtils;
import game.logic.Direction;
import game.logic.board.Cell;
import game.logic.exception.GameException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameTable extends AbstractTableModel {
    private Random random;
    private int cageX;
    private int cageY;
    /////////////////////////////////////
//    private ImageIcon straightLineHorizontal;
//    private ImageIcon straightLineVertical;
//    private ImageIcon angleLineRightDown;
//    private static ImageIcon angleLineLeftDown;
//    private ImageIcon angleLineLeftUp;
//    private ImageIcon angleLineRightUp;
    //////////////////////////////////////
    private ImageIcon borderRight;
    private ImageIcon borderLeft;
    private ImageIcon borderUp;
    private ImageIcon borderDown;
    private ImageIcon cornerLeftUp;
    private ImageIcon cornerLeftDown;
    private ImageIcon cornerRightUp;
    private ImageIcon cornerRightDown;
    private ImageIcon dotIcon;
    // new icons
    private ImageIcon gate;
    private ImageIcon verticalWall;
    private ImageIcon horizontalWall;
    private ImageIcon wallOne;
    private ImageIcon wall1Left;
    private ImageIcon wall1Up;
    private ImageIcon wall1Right;
    private ImageIcon wall1Down;
    private ImageIcon wall2LeftDown;
    private ImageIcon wall2LeftUp;
    private ImageIcon wall2RightDown;
    private ImageIcon wall2RightUp;
    private ImageIcon wall3Left;
    private ImageIcon wall3Up;
    private ImageIcon wall3Right;
    private ImageIcon wall3Down;
    private ImageIcon wall4;
    private final BoardFrame boardFrame;
    private final CellUI[][] board;
    private int pointsCount;

    {
        random = new Random();
        pointsCount = 0;
    }

    public GameTable(BoardFrame boardFrame) {
        this.boardFrame = boardFrame;
        board = new CellUI[boardFrame.height][];
        CellUI.cellSize = boardFrame.cellSize;
        for (int i = 0; i < boardFrame.height; i++) {
            board[i] = new CellUI[boardFrame.width];
        }
        loadIcons();
        createMaze();
        addFruits();
    }

    // only for test
    private void addFruits() {
        for (var c : Ghost.Color.values()) {
            addFruit(c);
        }
    }

    private void addFruit(Ghost.Color c) {
        int i, j;
        do {
            i = random.nextInt(1, boardFrame.height - 1);
            j = random.nextInt(1, boardFrame.width - 1);
        } while (!getValueAt(i, j).isFree());

        board[i][j].addFruit(c);
    }

    public void addFruitAt(Ghost.Color color, int i, int j) {
        board[i][j].addFruit(color);
    }

    private void loadIcons() {
        //////////////////////////
//        straightLineHorizontal = UIUtils.loadIcon("/images/board/double-straight-big.png");
//        var resizedImage = straightLineHorizontal.getImage()
//                .getScaledInstance(boardFrame.cellSize, boardFrame.cellSize, Image.SCALE_FAST);
//        straightLineHorizontal = new ImageIcon(resizedImage);
//
//        straightLineVertical = UIUtils.rotateImage(
//                UIUtils.toBufferedImage(straightLineHorizontal.getImage()), 90);
//
//        angleLineRightDown = UIUtils.loadIcon("/images/board/double-angle.png");
//        resizedImage = angleLineRightDown.getImage()
//                .getScaledInstance(boardFrame.cellSize, boardFrame.cellSize, Image.SCALE_FAST);
//        angleLineRightDown = new ImageIcon(resizedImage);
//
//        angleLineLeftDown = UIUtils.rotateImage(
//                UIUtils.toBufferedImage(angleLineRightDown.getImage()), 90);
//        angleLineLeftUp = UIUtils.rotateImage(
//                UIUtils.toBufferedImage(angleLineRightDown.getImage()), 180);
//        angleLineRightUp = UIUtils.rotateImage(
//                UIUtils.toBufferedImage(angleLineRightDown.getImage()), 270);
        //////////////////////////////
        borderUp = load("/images/board/wall/up.png");
        borderRight = load("/images/board/wall/right.png");
        borderDown = load("/images/board/wall/down.png");
        borderLeft = load("/images/board/wall/left.png");

        cornerLeftUp = load("/images/board/wall/corner-left-up.png");
        cornerRightUp = load("/images/board/wall/corner-right-up.png");
        cornerRightDown = load("/images/board/wall/corner-right-down.png");
        cornerLeftDown = load("/images/board/wall/corner-left-down.png");

        dotIcon = load("/images/board/dot.png");
        wallOne = load("/images/board/wall/wallOne.png");

        gate = load("/images/board/wall/gate.png");
        verticalWall = load("/images/board/wall/wall-vertical.png");
        horizontalWall = load("/images/board/wall/wall-horizontal.png");

        wall1Left = load("/images/board/wall/wall-1-left.png");
        wall1Up = load("/images/board/wall/wall-1-up.png");
        wall1Right = load("/images/board/wall/wall-1-right.png");
        wall1Down = load("/images/board/wall/wall-1-down.png");

        wall2LeftDown = load("/images/board/wall/wall-2-left-down.png");
        wall2LeftUp = load("/images/board/wall/wall-2-left-up.png");
        wall2RightDown = load("/images/board/wall/wall-2-right-down.png");
        wall2RightUp = load("/images/board/wall/wall-2-right-up.png");

        wall3Left = load("/images/board/wall/wall-3-left.png");
        wall3Up = load("/images/board/wall/wall-3-up.png");
        wall3Right = load("/images/board/wall/wall-3-right.png");
        wall3Down = load("/images/board/wall/wall-3-down.png");

        wall4 = load("/images/board/wall/wall-4.png");
    }

    private ImageIcon load(String iconFileLocation) {
        return new ImageIcon(UIUtils.loadIcon(iconFileLocation) .getImage()
                .getScaledInstance(boardFrame.cellSize, boardFrame.cellSize, Image.SCALE_FAST));
    }

    private void createMaze() {
        setBorder();
        addGhostsCage();
        // TODO: add maze itself
        addWallBlocks();
        fillUnSetCells();
        countPoints();

        // TODO: remove print
        for (int i = 0; i < boardFrame.height; i++) {
            for (int j = 0; j < boardFrame.width; j++) {
                System.out.print(board[i][j].isWall() ? "W " : "F ");
            }
            System.out.println();
        }
    }

    private void addWallBlocks() {
        WallFactory factory = new WallFactory();
        int n = 0;
        while (n < 5) {

            if (!factory.getRandomBlock().draw()) {
                n += 1;
            } else {
                n = 0;
            }
        }
    }

    private void countPoints() {
        pointsCount = 0;
        for (int i = 1; i < boardFrame.height - 1; ++i) {
            for (int j = 1; j < boardFrame.width - 1; ++j) {
                if (isFreeAt(i, j)) {
                    pointsCount += 1;
                }
            }
        }
    }

    private void displayBorder() {
        addCellIconAt(cornerRightDown, 0, 0);
        addCellIconAt(cornerLeftDown, 0, boardFrame.width - 1);
        addCellIconAt(cornerLeftUp, boardFrame.height - 1, boardFrame.width - 1);
        addCellIconAt(cornerRightUp, boardFrame.height - 1, 0);
        for (int i = 1; i < boardFrame.height - 1; ++i) {
            addCellIconAt(borderRight, i, 0);
            addCellIconAt(borderLeft, i, boardFrame.width - 1);
        }
        for (int j = 1; j < boardFrame.width - 1; ++j) {
            addCellIconAt(borderDown, 0, j);
            addCellIconAt(borderUp, boardFrame.height - 1, j);
        }
    }

    Pacman createPacman(JLabel emptyPacmanLabel) {

        // find random point on the board, where we can place a pacman
        int i, j;
        do {
            i = random.nextInt(1, boardFrame.height - 1);
            j = random.nextInt(1, boardFrame.width - 1);
        } while (!getValueAt(i, j).isFree());

        // random position & direction for pacman
        var point = getFrameCoordinates(i, j);
        emptyPacmanLabel.setBounds(point.x, point.y, boardFrame.cellSize, boardFrame.cellSize);
        boardFrame.add(emptyPacmanLabel);
        var dir = randomDirectionFrom(i, j);
        Pacman pacman = new Pacman(boardFrame.cellSize, dir, emptyPacmanLabel, boardFrame);
        board[i][j].eatPoint();
        pacman.setTarget(findTarget(dir, j, i));

        return pacman;
    }

    public ArrayList<Direction> possibleDirectionsFrom(int x, int y) {
        var point = getBoardCoordinates(x, y);
        return directionFrom(point.y, point.x);
    }

    private ArrayList<Direction> directionFrom(int i, int j) {
        return new ArrayList<>() {{
            if (board[i][j + 1].isFree()) {
                add(Direction.RIGHT);
            }
            if (board[i][j - 1].isFree()) {
                add(Direction.LEFT);
            }
            if (board[i + 1][j].isFree()) {
                add(Direction.DOWN);
            }
            if (board[i - 1][j].isFree()) {
                add(Direction.UP);
            }
        }};
    }

    private Direction randomDirectionFrom(int i, int j) {
        ArrayList<Direction> availableDirection =  directionFrom(i, j);
        return availableDirection.get(random.nextInt(availableDirection.size()));
    }

    Point findTarget(Direction direction, int x, int y) {
        switch (direction) {

            case RIGHT -> {
                do {
                    x += 1;
                } while (x < boardFrame.width && board[y][x].isFree());

                return getFrameCoordinates(y, x - 1);
            }
            case LEFT -> {
                do {
                    x -= 1;
                } while (x >= 0 && board[y][x].isFree());

                return getFrameCoordinates(y, x + 1);
            }
            case UP -> {
                do {
                    y -= 1;
                } while (y >= 0 && board[y][x].isFree());

                return getFrameCoordinates(y + 1, x);
            }
            case DOWN -> {
                do {
                    y += 1;
                } while (y < boardFrame.height && board[y][x].isFree());

                return getFrameCoordinates(y - 1, x);
            }
        }

        throw new GameException();
    }

    // setting border and filling other cells with free cell
    private void setBorder() {
        for (int j = 0; j < boardFrame.width; ++j) {
            board[0][j] = CellUI.wall();
            board[boardFrame.height - 1][j] = CellUI.wall();
        }
        for (int i = 1; i < boardFrame.height - 1; ++i) {
            board[i][0] = CellUI.wall();
            board[i][boardFrame.width - 1] = CellUI.wall();
        }
        displayBorder();
    }

    private void addGhostsCage() {
        // cage itself is 3x3, but it has padding around it so 5x5
        // 10x10 -> 2-3, 2-3   from 2 -> width - 6

        int beginI, beginJ;
        beginI = random.nextInt(2, boardFrame.height - 6);
        beginJ = random.nextInt(2, boardFrame.width - 6);

        for (int i = beginI; i < beginI + 5; ++i) {
            addFreeAt(i, beginJ + 4);
        }
        for (int j = beginJ + 1; j < beginJ + 4; ++j) {
            addFreeAt(beginI, j);
            addFreeAt(beginI + 4, j);
        }

        int i = beginI + 1;
        int j = beginJ + 1;

        addWallAt(wall1Up, i, j);
        addWallAt(verticalWall, i + 1, j);
        addWallAt(wall2RightUp, i + 2, j);
        addWallAt(horizontalWall, i + 2, j + 1);
        addWallAt(wall2LeftUp, i + 2, j + 2);
        addWallAt(verticalWall, i + 1, j + 2);
        addWallAt(wall1Up, i, j + 2);
        addWallAt(gate, i, j + 1);
        addWallAt(null, i + 1, j + 1);

        var point = getFrameCoordinates(i + 1, j + 1);
        cageX = point.x;
        cageY = point.y;
    }

    Point getCageCoordinates() {
        return new Point(cageX, cageY);
    }

    private void fillUnSetCells() {
        for (int i = 1; i < boardFrame.height; ++i) {
            for (int j = 1; j < boardFrame.width; ++j) {
                if (board[i][j] == null) {
                    addFreeAt(i, j);
                }
            }
        }
    }

    private void addCellIconAt(ImageIcon icon, int i, int j) {
        var point = getFrameCoordinates(i, j);
        var lbl = new JLabel(icon);
        lbl.setBounds(point.x, point.y, boardFrame.cellSize, boardFrame.cellSize);
        boardFrame.add(lbl);
        board[i][j].setLabel(lbl);
    }

    /**
     * @return Where board[i][j] == null
     */
    private boolean isUnsetAt(int i, int j) {
        return board[i][j] == null;
    }

    private void addFreeAt(int i, int j) {
        assert !isWallAt(i, j);
        if (isWallAt(i, j)) {
            throw new GameException("Trying to paint free at wall!!!");
        }
        if (board[i][j] != null) {
            board[i][j].removeLabel();
        }
        board[i][j] = CellUI.free();
        addCellIconAt(dotIcon, i, j);
    }

    private void addWallAt(ImageIcon icon, int i, int j) {
        if (board[i][j] != null) {
            board[i][j].removeLabel();
        }
        board[i][j] = CellUI.wall();
        addCellIconAt(icon, i, j);
    }

    Point getFrameCoordinates(int i, int j) {
        return new Point(j * boardFrame.cellSize, 50 + i * boardFrame.cellSize);
    }

    Point getBoardCoordinates(int x, int y) {
        return new Point((int) (x * 1d / boardFrame.cellSize), (int) ((y - 50) * 1d / boardFrame.cellSize));
    }

    int eatPointAt(int i, int j) {
        assert getValueAt(i, j).isFree();
        int value = getValueAt(i, j).eatPoint();
        if (value > 10) {         // fruit
            // TODO: if cell has eaten point -> display it
            System.out.println("Pacman have eaten a fruit!");
            if (value == Cell.PEPPER_VALUE) {
                boardFrame.speedUpPacman();
            } else if (value == Cell.APPLE_VALUE) {
                boardFrame.doubleCoins();
            } else if (value == Cell.BANANA_VALUE) {
                boardFrame.addOneLive();
            } else if (value == Cell.MELON_VALUE) {
                boardFrame.makePacmanInvisible();
            }
        }
        if (allPointsCollected()) {
            boardFrame.win();
        }
        return value;
    }

    boolean isWallAt(int i, int j) {
        var v = getValueAt(i, j);
        return v != null && v.isWall();
    }

    boolean isFreeAt(int i, int j) {
        var v = getValueAt(i, j);
        return v != null && v.isFree();
    }

    @Override
    public int getRowCount() {
        return boardFrame.height;
    }

    @Override
    public int getColumnCount() {
        return boardFrame.width;
    }

    @Override
    public CellUI getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || boardFrame.height <= rowIndex) {
            throw new GameException("row index " + rowIndex + " is out of bound for "
                    + "row count " + boardFrame.height);
        } else if (columnIndex < 0 || boardFrame.width <= columnIndex) {
            throw new GameException("column index " + columnIndex + " is out of bound for "
                    + "column count " + boardFrame.width);
        }
        return board[rowIndex][columnIndex];
    }

    public boolean allPointsCollected() {
        return pointsCount == 0;
    }

    public class WallFactory {
        private static Random random;
        private static ArrayList<WallBlock> allTypes;

        {
            random = new Random();
            allTypes = new ArrayList<>(40);
            addAllTypes(0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
        }

        void addAllTypes(int... types) {
            for (var t : types) {
                allTypes.add(new WallBlock(t));
            }
        }

        WallBlock getRandomBlock() {
            int f, t;
            f = 0;
            t = allTypes.size();
            return allTypes.get(random.nextInt(f, t));
        }

        public class WallBlock {
            private final int type;

            private WallBlock(int type) {
                this.type = type;
            }

            /**
             * Trys to paint wall block on a board
             * @return whether block is painted on a board
             */
            public boolean draw() {
                return switch (type) {
                    case 0 -> one();
                    case 1 -> two();
                    case 2 -> twov();
                    case 3 -> drawBlockOfType1();
                    case 4 -> drawBlockOfType2();
                    case 5 -> drawBlockOfType3();
                    case 6 -> drawBlockOfType4();
                    case 7 -> drawBlockOfType5();
                    case 8 -> drawBlockOfType6();
                    case 9 -> drawBlockOfType7();
                    case 10 -> drawBlockOfType8();
                    case 11 -> drawBlockOfType9();
                    case 12 -> drawBlockOfType10();
                    case 13 -> drawBlockOfType11();
                    case 14 -> drawBlockOfType12();
                    default -> throw new GameException("unknown wall block type " + type);
                };
            }

            private boolean twov() {
                /*
                 * 0 0 0
                 * 0 1 0
                 * 0 1 0
                 * 0 0 0
                 */
                int w = 3;
                int h = 4;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Up, i + 1, j + 1);
                addFreeAt(i + 1, j + 2);
                // row 2
                addFreeAt(i + 2, j);
                addWallAt(wall1Down, i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                // row 3
                addFreeAt(i + 3, j);
                addFreeAt(i + 3, j + 1);
                addFreeAt(i + 3, j + 2);

                return true;
            }

            private boolean one() {
                /*
                 * 0 0 0
                 * 0 1 0
                 * 0 0 0
                 */
                int w = 3;
                int h = 3;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wallOne, i + 1, j + 1);
                addFreeAt(i + 1, j + 2);
                // row 2
                addFreeAt(i + 2, j);
                addFreeAt(i + 2, j + 1);
                addFreeAt(i + 2, j + 2);

                return true;
            }

            private boolean two() {
                /*
                 * 0 0 0 0
                 * 0 1 1 0
                 * 0 0 0 0
                 */
                int w = 4;
                int h = 3;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Left, i + 1, j + 1);
                addWallAt(wall1Right, i + 1, j + 2);
                addFreeAt(i + 1, j + 3);
                // row 2
                addFreeAt(i + 2, j);
                addFreeAt(i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                addFreeAt(i + 2, j + 3);

                return true;
            }

            Point findWhereToBuildBlock(int w, int h) {
                // TODO: optimize
                for (int i = 1; i < boardFrame.height - h; ++i) {
                    for (int j = 1; j < boardFrame.width - w; ++j) {
                        boolean isPossibleToBuildBlock = true;
                        block_check:
                        for (int k = i; k < i + h; ++k) {
                            for (int s = j; s < j + w; ++s) {
                                if (!(isUnsetAt(k, s) || isFreeAt(k, s))) {
                                    isPossibleToBuildBlock = false;
                                    break block_check;
                                }
                            }
                        }
                        if (isPossibleToBuildBlock) {
                            return new Point(j, i);
                        }
                    }
                }
                return null;
            }

            boolean drawBlockOfType1() {
                /*
                 * 0 0 0 0 0
                 * 0 1 1 1 0
                 * 0 0 0 0 0
                 */
                int w = 5;
                int h = 3;
                Point point = findWhereToBuildBlock(w, h);

                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                addFreeAt(i, j + 4);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Left, i + 1, j + 1);
                addWallAt(horizontalWall, i + 1, j + 2);
                addWallAt(wall1Right, i + 1, j + 3);
                addFreeAt(i + 1, j + 4);
                // row 2
                addFreeAt(i + 2, j);
                addFreeAt(i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                addFreeAt(i + 2, j + 3);
                addFreeAt(i + 2, j + 4);

                return true;
            }
            boolean drawBlockOfType2() {
                /*
                 * 0 0 0
                 * 0 1 0
                 * 0 1 0
                 * 0 1 0
                 * 0 0 0
                 */
                int w = 3;
                int h = 5;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Up, i + 1, j + 1);
                addFreeAt(i + 1, j + 2);
                // row 2
                addFreeAt(i + 2, j);
                addWallAt(verticalWall, i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                // row 3
                addFreeAt(i + 3, j);
                addWallAt(wall1Down, i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                // row 4
                addFreeAt(i + 4, j);
                addFreeAt(i + 4, j + 1);
                addFreeAt(i + 4, j + 2);

                return true;
            }
            boolean drawBlockOfType3() {
                /*
                 * 0 0 0 0
                 * 0 1 1 0
                 * 0 0 1 0
                 * 0 0 0 0
                 */
                int w = 4;
                int h = 4;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Left, i + 1, j + 1);
                addWallAt(wall2LeftDown, i + 1, j + 2);
                addFreeAt(i + 1, j + 3);
                // row 2
                addFreeAt(i + 2, j);
                addFreeAt(i + 2, j + 1);
                addWallAt(wall1Down, i + 2, j + 2);
                addFreeAt(i + 2, j + 3);
                // row 3
                addFreeAt(i + 3, j);
                addFreeAt(i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                addFreeAt(i + 3, j + 3);

                return true;
            }
            boolean drawBlockOfType4() {

                /*
                 * 0 0 0 0
                 * 0 1 1 0
                 * 0 1 0 0
                 * 0 0 0 0
                 */
                int w = 4;
                int h = 4;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall2RightDown, i + 1, j + 1);
                addWallAt(wall1Right, i + 1, j + 2);
                addFreeAt(i + 1, j + 3);
                // row 2
                addFreeAt(i + 2, j);
                addWallAt(wall1Down, i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                addFreeAt(i + 2, j + 3);
                // row 3
                addFreeAt(i + 3, j);
                addFreeAt(i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                addFreeAt(i + 3, j + 3);

                return true;
            }
            boolean drawBlockOfType5() {
                /*
                 * 0 0 0 0
                 * 0 1 0 0
                 * 0 1 1 0
                 * 0 0 0 0
                 */
                int w = 4;
                int h = 4;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Up, i + 1, j + 1);
                addFreeAt(i + 1, j + 2);
                addFreeAt(i + 1, j + 3);
                // row 2
                addFreeAt(i + 2, j);
                addWallAt(wall2RightUp, i + 2, j + 1);
                addWallAt(wall1Right, i + 2, j + 2);
                addFreeAt(i + 2, j + 3);
                // row 3
                addFreeAt(i + 3, j);
                addFreeAt(i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                addFreeAt(i + 3, j + 3);

                return true;
            }

            boolean drawBlockOfType6() {
                /*
                 * 0 0 0 0
                 * 0 0 1 0
                 * 0 1 1 0
                 * 0 0 0 0
                 */
                int w = 4;
                int h = 4;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                // row 1
                addFreeAt(i + 1, j);
                addFreeAt(i + 1, j + 1);
                addWallAt(wall1Up, i + 1, j + 2);
                addFreeAt(i + 1, j + 3);
                // row 2
                addFreeAt(i + 2, j);
                addWallAt(wall1Left, i + 2, j + 1);
                addWallAt(wall2LeftUp, i + 2, j + 2);
                addFreeAt(i + 2, j + 3);
                // row 3
                addFreeAt(i + 3, j);
                addFreeAt(i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                addFreeAt(i + 3, j + 3);

                return true;
            }
            boolean drawBlockOfType7() {
                /*
                 * 0 0 0 0 0
                 * 0 1 1 1 0
                 * 0 0 0 1 0
                 * 0 0 0 0 0
                 */
                int w = 5;
                int h = 4;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                addFreeAt(i, j + 4);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Left, i + 1, j + 1);
                addWallAt(horizontalWall, i + 1, j + 2);
                addWallAt(wall2LeftDown, i + 1, j + 3);
                addFreeAt(i + 1, j + 4);
                // row 2
                addFreeAt(i + 2, j);
                addFreeAt(i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                addWallAt(wall1Down, i + 2, j + 3);
                addFreeAt(i + 2, j + 4);
                // row 3
                addFreeAt(i + 3, j);
                addFreeAt(i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                addFreeAt(i + 3, j + 3);
                addFreeAt(i + 3, j + 4);

                return true;
            }
            boolean drawBlockOfType8() {
                /*
                 * 0 0 0 0 0
                 * 0 1 1 1 0
                 * 0 0 0 1 0
                 * 0 0 0 1 0
                 * 0 0 0 0 0
                 */
                int w = 5;
                int h = 5;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                addFreeAt(i, j + 4);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Left, i + 1, j + 1);
                addWallAt(horizontalWall, i + 1, j + 2);
                addWallAt(wall2LeftDown, i + 1, j + 3);
                addFreeAt(i + 1, j + 4);
                // row 2
                addFreeAt(i + 2, j);
                addFreeAt(i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                addWallAt(verticalWall, i + 2, j + 3);
                addFreeAt(i + 2, j + 4);
                // row 3
                addFreeAt(i + 3, j);
                addFreeAt(i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                addWallAt(wall1Down, i + 3, j + 3);
                addFreeAt(i + 3, j + 4);
                // row 4
                addFreeAt(i + 4, j);
                addFreeAt(i + 4, j + 1);
                addFreeAt(i + 4, j + 2);
                addFreeAt(i + 4, j + 3);
                addFreeAt(i + 4, j + 4);

                return true;
            }
            boolean drawBlockOfType9() {
                /*
                 * 0 0 0 0 0
                 * 0 1 1 1 0
                 * 0 1 0 0 0
                 * 0 1 0 0 0
                 * 0 0 0 0 0
                 */
                int w = 5;
                int h = 5;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                addFreeAt(i, j + 4);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall2RightDown, i + 1, j + 1);
                addWallAt(horizontalWall, i + 1, j + 2);
                addWallAt(wall1Right, i + 1, j + 3);
                addFreeAt(i + 1, j + 4);
                // row 2
                addFreeAt(i + 2, j);
                addWallAt(verticalWall, i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                addFreeAt(i + 2, j + 3);
                addFreeAt(i + 2, j + 4);
                // row 3
                addFreeAt(i + 3, j);
                addWallAt(wall1Down, i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                addFreeAt(i + 3, j + 3);
                addFreeAt(i + 3, j + 4);
                // row 4
                addFreeAt(i + 4, j);
                addFreeAt(i + 4, j + 1);
                addFreeAt(i + 4, j + 2);
                addFreeAt(i + 4, j + 3);
                addFreeAt(i + 4, j + 4);

                return true;
            }
            boolean drawBlockOfType10() {
                /*
                 * 0 0 0 0 0 0
                 * 0 1 1 1 1 0
                 * 0 1 0 0 0 0
                 * 0 1 0 0 0 0
                 * 0 1 0 0 0 0
                 * 0 1 0 0 0 0
                 * 0 1 0 0 0 0
                 * 0 1 1 1 1 0
                 * 0 0 0 0 0 0
                 */
                int w = 6;
                int h = 9;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                addFreeAt(i, j + 4);
                addFreeAt(i, j + 5);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall2RightDown, i + 1, j + 1);
                addWallAt(horizontalWall, i + 1, j + 2);
                addWallAt(horizontalWall, i + 1, j + 3);
                addWallAt(wall1Right, i + 1, j + 4);
                addFreeAt(i + 1, j + 5);
                // rows 2-6
                for (int r = 2; r <= 6; r++) {
                    addFreeAt(i + r, j);
                    addWallAt(verticalWall, i + r, j + 1);
                    addFreeAt(i + r, j + 2);
                    addFreeAt(i + r, j + 3);
                    addFreeAt(i + r, j + 4);
                    addFreeAt(i + r, j + 5);
                }
                // row 7
                addFreeAt(i + 7, j);
                addWallAt(wall2RightUp, i + 7, j + 1);
                addWallAt(horizontalWall, i + 7, j + 2);
                addWallAt(horizontalWall, i + 7, j + 3);
                addWallAt(wall1Right, i + 7, j + 4);
                addFreeAt(i + 7, j + 5);
                // row 8
                addFreeAt(i + 8, j);
                addFreeAt(i + 8, j + 1);
                addFreeAt(i + 8, j + 2);
                addFreeAt(i + 8, j + 3);
                addFreeAt(i + 8, j + 4);
                addFreeAt(i + 8, j + 5);

                return true;
            }
            boolean drawBlockOfType11() {
                /*
                 * 0 0 0 0 0 0
                 * 0 1 1 1 1 0
                 * 0 0 0 1 0 0
                 * 0 0 0 1 0 0
                 * 0 0 0 1 0 0
                 * 0 0 0 1 0 0
                 * 0 0 0 1 0 0
                 * 0 1 1 1 1 0
                 * 0 0 0 0 0 0
                 */
                int w = 6;
                int h = 9;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                addFreeAt(i, j + 4);
                addFreeAt(i, j + 5);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall1Left, i + 1, j + 1);
                addWallAt(horizontalWall, i + 1, j + 2);
                addWallAt(wall3Down, i + 1, j + 3);
                addWallAt(wall1Right, i + 1, j + 4);
                addFreeAt(i + 1, j + 5);
                // rows 2-5
                for (int r = 2; r <= 5; r++) {
                    addFreeAt(i + r, j);
                    addFreeAt(i + r, j + 1);
                    addFreeAt(i + r, j + 2);
                    addWallAt(verticalWall, i + r, j + 3);
                    addFreeAt(i + r, j + 4);
                    addFreeAt(i + r, j + 5);
                }
                // row 6
                addFreeAt(i + 6, j);
                addFreeAt(i + 6, j + 1);
                addFreeAt(i + 6, j + 2);
                addWallAt(verticalWall, i + 6, j + 3);
                addFreeAt(i + 6, j + 4);
                addFreeAt(i + 6, j + 5);
                // row 7
                addFreeAt(i + 7, j);
                addWallAt(wall1Left, i + 7, j + 1);
                addWallAt(horizontalWall, i + 7, j + 2);
                addWallAt(wall3Up, i + 7, j + 3);
                addWallAt(wall1Right, i + 7, j + 4);
                addFreeAt(i + 7, j + 5);
                // row 8
                addFreeAt(i + 8, j);
                addFreeAt(i + 8, j + 1);
                addFreeAt(i + 8, j + 2);
                addFreeAt(i + 8, j + 3);
                addFreeAt(i + 8, j + 4);
                addFreeAt(i + 8, j + 5);

                return true;
            }
            boolean drawBlockOfType12() {
                /*
                 * 0 0 0 0 0 0 0 0 0 0
                 * 0 1 1 1 1 1 1 1 1 0
                 * 0 1 0 0 0 0 0 0 1 0
                 * 0 1 0 0 0 0 0 0 1 0
                 * 0 0 0 0 0 0 0 0 0 0
                 */
                int w = 10;
                int h = 5;
                Point point = findWhereToBuildBlock(w, h);
                if (point == null) {
                    return false;
                }

                int i = point.y;
                int j = point.x;
                // row 0
                addFreeAt(i, j);
                addFreeAt(i, j + 1);
                addFreeAt(i, j + 2);
                addFreeAt(i, j + 3);
                addFreeAt(i, j + 4);
                addFreeAt(i, j + 5);
                addFreeAt(i, j + 6);
                addFreeAt(i, j + 7);
                addFreeAt(i, j + 8);
                addFreeAt(i, j + 9);
                // row 1
                addFreeAt(i + 1, j);
                addWallAt(wall2RightDown, i + 1, j + 1);
                addWallAt(horizontalWall, i + 1, j + 2);
                addWallAt(horizontalWall, i + 1, j + 3);
                addWallAt(horizontalWall, i + 1, j + 4);
                addWallAt(horizontalWall, i + 1, j + 5);
                addWallAt(horizontalWall, i + 1, j + 6);
                addWallAt(horizontalWall, i + 1, j + 7);
                addWallAt(wall2LeftDown, i + 1, j + 8);
                addFreeAt(i + 1, j + 9);
                // rows 2-3
                addFreeAt(i + 2, j);
                addWallAt(verticalWall, i + 2, j + 1);
                addFreeAt(i + 2, j + 2);
                addFreeAt(i + 2, j + 3);
                addFreeAt(i + 2, j + 4);
                addFreeAt(i + 2, j + 5);
                addFreeAt(i + 2, j + 6);
                addFreeAt(i + 2, j + 7);
                addWallAt(verticalWall, i + 2, j + 8);
                addFreeAt(i + 2, j + 9);
                // row 3
                addFreeAt(i + 3, j);
                addWallAt(wall1Down, i + 3, j + 1);
                addFreeAt(i + 3, j + 2);
                addFreeAt(i + 3, j + 3);
                addFreeAt(i + 3, j + 4);
                addFreeAt(i + 3, j + 5);
                addFreeAt(i + 3, j + 6);
                addFreeAt(i + 3, j + 7);
                addWallAt(wall1Down, i + 3, j + 8);
                addFreeAt(i + 3, j + 9);
                // row 4
                addFreeAt(i + 4, j);
                addFreeAt(i + 4, j + 1);
                addFreeAt(i + 4, j + 2);
                addFreeAt(i + 4, j + 3);
                addFreeAt(i + 4, j + 4);
                addFreeAt(i + 4, j + 5);
                addFreeAt(i + 4, j + 6);
                addFreeAt(i + 4, j + 7);
                addFreeAt(i + 4, j + 8);
                addFreeAt(i + 4, j + 9);

                return true;
            }
            boolean drawBlockOfType13() {return false;}
            boolean drawBlockOfType14() {return false;}
            boolean drawBlockOfType15() {return false;}
        }
    }
}
