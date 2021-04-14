/*
 *
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

/**
 * A display of a grid of cells.
 *
 * @author      Bernard Zeigler, Jeff Mather
 */

package Component.GridCellplots;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import util.*;


public class CellGridView extends JFrame
{
    /**
     * The color of each cell in the grid.
     */
    protected Color[][] grid;

    /**
     * The actual panel on which the grid is drawn.
     */
    protected GridPanel gridPanel;

    /**
     * The size of the cell space being depicted (in cells).
     */
    protected Dimension spaceSize = new Dimension(40, 40);

    /**
     * The size of each cell (in pixels).
     */
    protected int cellSize = 10;

    /**
     * The scale factors to use when depicting a range that is larger than
     * the space-size.
     */
    protected double xScaleFactor = 1, yScaleFactor = 1;

    /**
     * The font used to draw labels on the grid, as well as its associated
     * metrics object and aspects of its size.
     */
    protected Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
    protected FontMetrics labelFontMetrics = getFontMetrics(labelFont);
    protected int labelFontAscent = labelFontMetrics.getAscent();

    /**
     * The axes labels.
     */
    protected String xLabel = "X", yLabel = "Y";

    /**
     * The width of the border around the grid.
     */
    protected final int borderWidth = 5;

    /**
     * Constructor.
     *
     * @param   title               The title to display in this view's
     *                              title bar.
     * @param   spaceSize_          The size of the cell space being depicted
     *                              (in cells).
     * @param   cellSize_           The size of each cell (in pixels).
     * @param   xLabel_, yLabel_    The axes labels.
     */
    public CellGridView(String title, Dimension spaceSize_, int cellSize_,
        String xLabel_, String yLabel_)
    {
        super(title);

        if (xLabel_ != null) xLabel = xLabel_;
        if (yLabel_ != null) yLabel = yLabel_;

        if (spaceSize_ != null) spaceSize = spaceSize_;
        if (cellSize_ > 0) cellSize = cellSize_;

        createGrid();

        // set the properties of this view's content pane
        Container pane = getContentPane();
        pane.setBackground(Color.white);
        pane.setLayout(new BorderLayout());

        // add the main panel
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(
            borderWidth, borderWidth, borderWidth, borderWidth));
        pane.add(main, BorderLayout.CENTER);

        // add the grid panel
        gridPanel = new GridPanel();
        main.add(gridPanel, BorderLayout.CENTER);
    }

    /**
     * A convenience constructor.
     */
    public CellGridView(String title, Dimension spaceSize, int cellSize)
    {
        this(title, spaceSize, cellSize, null, null);
    }

    /**
     * A convenience constructor.
     */
    public CellGridView(String title, String xLabel, String yLabel)
    {
        this(title, null, 0, xLabel, yLabel);
    }

    /**
     * A convenience constructor.
     */
    public CellGridView(String title)
    {
        this(title, null, 0);
    }

    /**
     * See parent method.
     */
    public void addNotify()
    {
        super.addNotify();

        adjustSizeToHoldGridPanel();
    }

    /**
     * Adjusts the size of this view's frame to just contain the grid
     * panel and its border.
     */
    protected void adjustSizeToHoldGridPanel()
    {
        // adjust the size of this view to just contain the grid and
        // its border
        Insets insets = getInsets();
        setSize(gridPanel.getPreferredSize().width
            + insets.left + insets.right + borderWidth * 2,
            gridPanel.getPreferredSize().height
            + insets.top + insets.bottom + borderWidth * 2);
    }

    /**
     * Returns the given x limited to the cell-space's width (and zero).
     *
     * @param   x       The cell x to limit.
     */
    protected int forceXInBounds(int x)
    {
        x = (x < 0) ? 0 : x;
        x = (x >= spaceSize.width) ? spaceSize.width - 1 : x;
        return x;
    }

    /**
     * Returns the given y limited to the cell-space's height (and zero).
     *
     * @param   y       The cell y to limit.
     */
    protected int forceYInBounds(int y)
    {
        y = (y < 0) ? 0 : y;
        y = (y >= spaceSize.height) ? spaceSize.height - 1 : y;
        return y;
    }

    /**
     * Sets the x scale factor to use when depicting a range that is larger
     * than the space-size.
     *
     * @param   range       The x-range the cell space is supposed to depict.
     */
    public void setXScale(double range)
    {
        xScaleFactor = spaceSize.width / range;
    }

    /**
     * Sets the y scale factor to use when depicting a range that is larger
     * than the space-size.
     *
     * @param   range       The y-range the cell space is supposed to depict.
     */
    public void setYScale(double range)
    {
        yScaleFactor = spaceSize.height / range;
    }

    /**
     * Returns the given x scaled according to the current x-scale-factor.
     *
     * @param   x       The x to scale.
     * @return          The x-value, scaled.
     */
    protected int scaleX(double x)
    {
        int centerX = (int)Math.rint(spaceSize.width / 2.0);
        int scaled = centerX + (int)Math.rint(x * xScaleFactor);
        return forceXInBounds(scaled);
    }

    /**
     * Returns the given time value scaled according to the current
     * time-scale-factor.
     *
     * @param   time        The time value to scale.
     * @return              The time value, scaled.
     */
    protected int scaleTime(double time, double timeScale)
    {
        setXScale(timeScale);
        return scaleX(time);
    }

    /**
     * Returns the given y scaled according to the current y-scale-factor.
     *
     * @param   y       The y to scale.
     * @return          The y-value, scaled.
     */
    protected int scaleY(double y)
    {
        int centerY = (int)Math.rint(spaceSize.height / 2.0);
        int scaled = centerY + (int)Math.rint(y * yScaleFactor);
        return forceYInBounds(scaled);
    }

    /**
     * Draws a cell at the given cell location using the given color.
     *
     * @param   cellX, cellY        The cell location.
     * @param   color               The color to fill the cell with.
     */
    public void drawCell(int cellX, int cellY, Color color)
    {
        // detm the pixel location of the cell
        int x = (forceXInBounds(cellX) * cellSize) + 1;
        int y = (forceYInBounds(cellY) * cellSize) + 1;

        fillCellOnSwingThread(x, y, color);
    }

    /**
     * Draws a cell at the location determined by scaling the given
     * cell location by the current scale factors.
     *
     * @param   cellX, cellY        The cell location to scale.
     * @param   color               The color to fill the cell with.
     */
    public void drawCellToScale(double cellX, double cellY, Color color)
    {
        // detm the scaled pixel location of the cell
        int x = (scaleX(cellX) * cellSize) + 1;
        int y = (scaleY(-cellY) * cellSize) + 1;

        fillCellOnSwingThread(x, y, color);
    }

    /**
     * This is just like drawCellToScale(), except that the given cell x-value
     * is treated as a time value and is scaled by the given time-scale,
     * rather than the current x-scale-factor.
     *
     * @param   timeScale           The factor by which to scale the given
     *                              cell x-value.
     *
     * See drawCellToScale() for the other parameter explanations.
     */
    public void drawCellToTimeScale(double cellX, double timeScale,
        double cellY, Color color)
    {
        // detm the scaled pixel location of the cell
        int x = (scaleTime(cellX, timeScale) * cellSize) + 1;
        int y = (scaleY(-cellY) * cellSize) + 1;

        fillCellOnSwingThread(x, y, color);
    }

    /**
     * This is just like drawCellToTimeScale(), except that a column is drawn
     * at the scaled location, rather than just a cell.
     *
     * See drawCellToTimeScale() for the parameter explanations.
     */
    public void drawPulseToTimeScale(double cellX, double timeScale,
        double cellY, Color color)
    {
        // detm the scaled pixel location of the cell
        int x = (scaleTime(cellX, timeScale) * cellSize) + 1;
        int y = (scaleY(-cellY) * cellSize) + 1;

        fillColumn(x, y, color);
    }

    /**
     * Fills the cell at the given pixel location with the given color.
     * Note that when filling a cell, neither its starting row or column
     * are filled; this keeps the cell from obliterating the axis lines and
     * provides a border between adjacent cells.
     *
     * @param   pixelX, pixelY      The pixel location of the upper-left-hand
     *                              corner of the cell to fill.
     * @param   color               The fill color.
     */
    protected void fillCell(int pixelX, int pixelY, Color color)
    {
        // fill in the cell's rectangle
        Graphics g = gridPanel.getGraphics();
        g.setColor(color);
        g.fillRect(pixelX, pixelY, cellSize - 1, cellSize - 1);

        // remember the cell's new color
        grid[pixelX / cellSize][pixelY / cellSize] = color;
    }

    /**
     * Makes a call to the fillCell() method on the swing thread.
     *
     * See fillCell() for parameter descriptions.
     */
    protected void fillCellOnSwingThread(final int pixelX, final int pixelY,
        final Color color)
    {
        // run this code on the swing thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fillCell(pixelX, pixelY, color);
            }
        });
    }

    /**
     * Fills the vertical column of cells that starts at the given
     * pixel location and extends to the x-axis.
     *
     * @param   pixelX, pixelY      The pixel location of the upper-left-hand
     *                              (or bottom-left-hand, if cellY is negative)
     *                              corner of the column to fill.
     * @param   color               The fill color.
     */
    protected void fillColumn(final int pixelX, final int pixelY,
        final Color color)
    {
        // run this code on the swing thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // fill in the column from the x-axis to the cell's rectangle
                Graphics g = gridPanel.getGraphics();
                g.setColor(color);

                // if the cell-y value is above the x-axis
                int gridHeight = spaceSize.height * cellSize;
                int toZero = (gridHeight - pixelY) - gridHeight / 2;
                if (toZero >= 0) {
                    // fill in the cells from the given cell down to the
                    // x-axis
                    for (int i = 0; i <= toZero / cellSize; i++) {
                        fillCell(pixelX, pixelY + i * cellSize, color);
                    }
                }

                // otherwise
                else {
                    // fill in the cells from the given cell up to the
                    // x-axis
                    for (int i = 0; i <= -toZero / cellSize; i++) {
                        fillCell(pixelX, pixelY - i * cellSize, color);
                    }
                }
            }
        });
    }

    /**
     * Draws a string at the given cell location using the given color.
     *
     * @param   cellX, cellY        The cell location at which to draw
     *                              the string.
     * @param   string              The text string to draw.
     * @param   color               The color with which to draw the string.
     */
    public void drawString(int cellX, int cellY, final String string,
        final Color color)
    {
        // detm the pixel location of the cell
        final int x = forceXInBounds(cellX) * cellSize;
        final int y = forceYInBounds(-cellY) * cellSize;

        // run this code on the swing thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // draw the given string with its starting point centered
                // within the cell
                Graphics g = gridPanel.getGraphics();
                g.setColor(color);
                g.drawString(string, x + cellSize / 2, y + cellSize / 2);
            }
        });
    }

    /**
     * The panel on which the actual grid is drawn.
     */
    protected class GridPanel extends JPanel
    {
        /**
         * Constructor.
         */
        public GridPanel()
        {
            setBackground(Color.white);
        }

        /**
         * See parent method.
         */
        public Dimension getPreferredSize()
        {
            return new Dimension(spaceSize.width * cellSize,
                spaceSize.height * cellSize);
        }

        /**
         * Paints this view's static graphical elements, such as its border
         * and axes labels.
         */
        public void paint(Graphics g)
        {
            super.paint(g);

            // draw and label the 0,0 cell
            int width = spaceSize.width * cellSize, halfX = width / 2;
            int height = spaceSize.height * cellSize, halfY = height / 2;
            g.drawRect(halfX, halfY, cellSize, cellSize);
            g.drawString("0,0", halfX + 4, halfY - 4);

            // label the axes
            g.drawString(xLabel,
                width - labelFontMetrics.stringWidth(xLabel) - 4,
                halfY - 4);
            g.drawString(yLabel, halfX + 4, labelFontAscent + 4);

            // draw the two lines that form the four quadrants
            g.drawLine(halfX, 0, halfX, height);
            g.drawLine(0, halfY, width, halfY);

            // for each cell in the grid
            for (int i = 0; i < spaceSize.width; i++) {
                for (int j = 0; j < spaceSize.height; j++) {
                    // if this cell has had its color set
                    if (grid[i][j] != null) {
                        // fill in this cell with its color
                        g.setColor(grid[i][j]);
                        g.fillRect(i * cellSize + 1, j * cellSize + 1,
                            cellSize - 1, cellSize - 1);
                    }
                }
            }
        }
    }

    /**
     * See member variable accessed.
     */
    public Dimension getSpaceSize() {return spaceSize;}
    public void setSpaceSize(Dimension size)
    {
        spaceSize = size;

        createGrid();

        adjustSizeToHoldGridPanel();
    }

    /**
     * See member variable accessed.
     */
    public int getCellSize() {return cellSize;}
    public void setCellSize(int size)
    {
        cellSize = size;
        adjustSizeToHoldGridPanel();
    }

    /**
     * See member variable accessed.
     */
    public String getXLabel() {return xLabel;}
    public void setXLabel(String label) {xLabel = label;}

    /**
     * Creates the grid associated with the grid member variable.
     */
    protected void createGrid()
    {
        // create the grid data structure
        grid = new Color[spaceSize.width][spaceSize.height];
    }

    /**
     * Clears the grid.
     */
    public void clearGrid()
    {
        // for each cell in the grid
        for (int i = 0; i < spaceSize.width; i++) {
            for (int j = 0; j < spaceSize.height; j++) {
                // clear this cell's color value
                grid[i][j] = null;
            }
        }

        // get the whole grid repainted
        repaint();
    }
}



