/**
 * An entity that describes a cell to be drawn, including that cell's
 * coordinates within a space, and the color with which it should be drawn.
 *
 * @author      Bernard Zeigler, Jeff Mather
 */

package Component.GridCellplots;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

import GenCol.*;


import model.modeling.*;


public class DrawCellEntity extends entity
{
    /**
     * The cell coordinates within a cell space.
     */
    public double i, j, k;

    /**
     * The color in which to draw the cell normally, and also when it should
     * appeared as being dimmed.
     */
    public Color color, dimTo;

    /**
     * Formats decimal numbers to three places.
     */
    protected DecimalFormat decimalFormat = new DecimalFormat("0.000");

    /**
     * The time-scale-factor to be used when drawing the cell.
     */
    public double timeScale;

    /**
     * Constructs a draw-cell-entity using the given values.
     *
     * @param   name        The name to give this entity.
     * @param   i_, j_, k_  The cell's coordinates within a cell space.
     *                      (k_ appears only in other constructors).
     * @param   color_      The color in which to draw the cell.
     * @param   dimTo_      The color in which to draw the cell
     *                      when it should appeared dimmed (this appears
     *                      only in other constructors).
     */
    public DrawCellEntity(String name_, double i_, double j_, Color color_,
        double timeScale_)
    {
        super(name_);
        i = i_;
        j = j_;
        if (color_ != null) color = color_;
        dimTo = Color.lightGray;
        if (timeScale_ != 0) timeScale = timeScale_;

        // make the name of this entity more informational
        name = name + " :i,j " + decimalFormat.format(i) + ", "
            + decimalFormat.format(j);
    }

    /**
     * A convenience constructor.
     */
    public DrawCellEntity(String name, double i, double j, Color color)
    {
        this(name, i, j, color,0);//Color.black, 0);//bpz
    }

    /**
     * A convenience constructor.
     */
    public DrawCellEntity(String name, double i, double j, double k_)
    {
        this(name, i, j, Color.black);
        k = k_;
    }

    /**
     * A convenience constructor.
     */
    public DrawCellEntity(String name, double i, double j)
    {
        this(name, i, j, Color.black);
    }

    /**
     * A convenience constructor.
     */
    public DrawCellEntity(double i, double j, Color color)
    {
        this("drawCell", i, j, color);
    }

    /**
     * A convenience constructor.
     */
    public DrawCellEntity(double i, double j, Color color, Color dimTo_)
    {
        this("drawCell", i, j, color);
        dimTo = dimTo_;
    }

    /**
     * See parent member variable accessed.
     */
    public void setName(String name_) {name = name_;}

    /**
     * Creates a draw-cell-entity from the given string representation.
     *
     * @param   stringRep   A string holding the data with which to initialize
     *                      the new draw-cell-entity.
     * @return              The newly-created draw-cell-entity.
     */
    static public DrawCellEntity toObject(String stringRep)
    {
        // detm what the i portion is of the given string representation
        int jIndex = stringRep.indexOf("j ");
        int commaIndex = stringRep.indexOf(", ");
        String iString = stringRep.substring(jIndex + 1, commaIndex);

        // detm what the j portion is of the given string representation
        String jString = stringRep.substring(commaIndex + 1,
            stringRep.length());

        // create and return the new draw-cell-entity
        return new DrawCellEntity("drawCellToScale",
            Double.parseDouble(iString), Double.parseDouble(jString));
    }

    /**
     * A convienence method.
     */
    static public DrawCellEntity toObject(entity entity)
    {
        return toObject(entity.getName());
    }
}



