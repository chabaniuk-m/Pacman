package game.gui.board;

import game.gui.hero.Ghost;
import game.logic.board.Cell;

import javax.swing.*;

public class CellUI {
    public static int cellSize;
    private final Cell cell;
    private JLabel label;
    private Ghost.Color fruitColor;

    private CellUI(Cell cell) {
        this.cell = cell;
    }

    public static CellUI free() {
        return new CellUI(Cell.free());
    }

    public void addFruit(Ghost.Color fruitColor) {
        this.fruitColor = fruitColor;
        cell.setFruit(fruitColor.getFruitScore());
        label.setIcon(fruitColor.getFruitIcon(cellSize));
    }

    public static CellUI wall() {
        return new CellUI(Cell.wall());
    }

    public void setLabel(JLabel lbl) {
        label = lbl;
    }

    public int eatPoint() {
        if (fruitColor != null && cell.hasPoint()) {
            // fruit will be eaten, but cell still has a point
            label.setIcon(fruitColor.getDotIcon(cellSize));
            fruitColor = null;
        } else if (cell.hasPoint()) {
            label.setIcon(null);
        }
        return cell.eatPoint();
    }

    public boolean isFree() {
        return cell.isFree();
    }

    public boolean isWall() {
        return cell.isWall();
    }

    public void removeLabel() {
        if (label != null) {
            label.setVisible(false);
        }
    }
}
