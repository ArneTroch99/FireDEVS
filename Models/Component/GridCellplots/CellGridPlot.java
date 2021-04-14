
/**
 * A devs component that displays a cell-grid-view and accepts
 * messages telling it what to plot on that view.
 *
 * @author      Bernard Zeigler, Jeff Mather
 */

package Component.GridCellplots;

import java.awt.*;
import java.util.*;

import Component.UtilModels.*;
import GenCol.*;


import model.modeling.*;
import util.*;
import view.simView.*;

public class CellGridPlot extends JobQueue
{
    /**
     * The cell-grid-view on to which to plot.
     */
    protected CellGridView cellGridView;

    /**
     * The default onscreen location of the cell-grid-view.
     */
    protected Point cellGridViewLocation = new Point(500, 50);

    /**
     * The last values passed in as part of draw-i and draw-j jobs.
     */
    protected double lastI = 0, lastJ = 0;

    /**
     * What range each axis in the cell-grid-view covers.
     */
    protected double xRange = 1, yRange = 1, zRange = 1;

    /**
     * The ratio of pixels-to-seconds when the x axis is used as the time
     * axis for a time plot.
     */
    protected double timeScale;

    /**
     * The color to use in cell drawing operations where the color is
     * always the same.
     */
    protected Color defaultColor = Color.blue;

    /**
     * The time value computed for the last plot-time command carried out.
     * Is used to help determine when the plot has cycled back to the left
     * side of the view.
     */
    protected double lastTimePlotTime = -1;

    /**
     * Indicates whether the cells of a time plot which are now supposed to
     * be dimmed should not actually be dimmed.  This will be true from the
     * time the plot cycles back to the left side of the view (at which
     * point the view is cleared), until the dimming of the cells has also
     * cycled to the left side.  Dimming cells during this time would
     * leave them uncleared on the far right side of the view.
     */
    protected boolean dontDimTimePlotCells;

    /**
     * Is the corresponding variable to lastTimePlotTime for
     * pulse-plotting situations.  See that variable's description.
     */
    protected double lastPulseTime = -1;

    /**
     * Is the corresponding variable to dontDimTimePlotCells for
     * pulse-plotting situations.  See that variable's description.
     */
    protected boolean dontDimPulseLines;

    /**
     * Constructor.
     *
     * @param   name            The name to give this devs component.
     * @param   jobDueDelay     The length of time after a job arrives until
     *                          it is due.
     * @param   xLabel_         The name to give the x-axis of the cell-grid-view.
     * @param   xRange_         How large a range the x-axis should cover.
     * @param   yLabel_         The name to give the y-axis of the cell-grid-view.
     * @param   yRange_         How large a range the y-axis should cover.
     */
    public CellGridPlot(String name, double jobDueDelay,
        String xLabel, double xRange_, String yLabel, double yRange_)
    {
        super(name, jobDueDelay);

        // this class of component appears often within models, and its
        // display often doesn't add much to the understanding of a
        // visualized model, while it takes up a lot of space within
        // the sim-viewer view-canvas; therefore, it's default display-state
        // is to be hidden
        setHidden(true);

        xRange = xRange_;
        yRange = yRange_;

        // append half-range values to the axis labels
        xLabel = xLabel + " " + .5 * xRange;
        yLabel = yLabel + " " + .5 * yRange;

        // create the cell-grid-view
        cellGridView = new CellGridView(name, xLabel, yLabel);

        configureCellGridView();

        // add necessary ports
        addInport("drawString");
        addInport("drawCell");
        addInport("drawCellToScale");
        addInport("drawI");
        addInport("drawJ");
        addInport("draw2D");
        addInport("timePlot");
        addInport("pulsePlot");

        // add some test inputs
        addTestInput("drawString",
            new DrawCellEntity("this is a test", 15, 10, Color.red), 0);
        addTestInput("drawCell",
            new DrawCellEntity(15, 11, Color.black, Color.green), 0);
        addTestInput("drawCell", new DrawCellEntity(10, 10, Color.red), 0);
        addTestInput("drawI", new doubleEnt(.5), 0);
        addTestInput("drawJ", new doubleEnt(-.7), 0);
        addTestInput("timePlot", new doubleEnt(-.7), 1);
        addTestInput("timePlot", new doubleEnt(.7), 1);

        computeTimeScale();
    }

    /**
     * Constructor.
     *
     * @param   name            See other constructor.
     * @param   jobDueDelay     See other constructor.
     * @param   cellGridView_   The cell-grid-view on which to plot.
     * @param   zRange_         How large a range the z-axis should cover.
     */
    public CellGridPlot(String name, double jobDueDelay,
        CellGridView cellGridView_, double zRange_)
    {
        super(name, jobDueDelay);
        cellGridView = cellGridView_;
        zRange = zRange_;
        configureCellGridView();

        // see other constructor for why this is done
        setHidden(true);

        computeTimeScale();
    }

    /**
     * A convenience constructor.
     */
    public CellGridPlot(String name, double jobDueDelay,
        CellGridView cellGridView)
    {
        this(name, jobDueDelay, cellGridView, 1.0);
    }

    /**
     * A convenience constructor.
     */
    public CellGridPlot(String name, double delay,
        double xRange, double yRange)
    {
        this(name, delay, "X", xRange, "Y", yRange);
    }

    /**
     * A convenience constructor.
     */
    public CellGridPlot(String name, double delay,
        double xRange, double yRange, double zRange_)
    {
        this(name, delay, "X", xRange, "Y", yRange);
        zRange = zRange_;
    }

    /**
     * A convenience constructor.
     */
    public CellGridPlot(String name, double delay,
        String yLabel, double yRange)
    {
        this(name, delay, "time", 100, yLabel, yRange);
    }

    /**
     * A convenience constructor.
     */
    public CellGridPlot(String name, double delay, double yRange)
    {
        this(name, delay, "Y", yRange);
    }

    /**
     * A convenience constructor.
     */
    public CellGridPlot()
    {
        this("CellGridPlot", 10, 2, 2);
    }

    /**
     * Sets the location of this component's cell-grid-view onscreen.
     *
     * @param   x, y    The location onscreen.
     */
    public void setCellGridViewLocation(int x, int y)
    {
        cellGridViewLocation.setLocation(x, y);
        cellGridView.setLocation(x, y);
    }

    /**
     * Sets the size of the cell space to be displayed by the cell-grid-view.
     *
     * @param       width, height       The size of the cell space (in cells).
     */
    public void setSpaceSize(int width, int height)
    {
        cellGridView.setSpaceSize(new Dimension(width, height));
        computeTimeScale();
        cellGridView.setXScale(xRange);
        cellGridView.setYScale(yRange);
    }

    /**
     * See member variable for description of variable computed.
     */
    protected void computeTimeScale()
    {
        timeScale = 2.2 * cellGridView.getSpaceSize().width;
    }

    /**
     * Sets how large each cell should be.
     *
     * @param       cellSize_           The size of each cell (in pixels).
     */
    public void setCellSize(int size)
    {
        cellGridView.setCellSize(size);
    }

    /**
     * See member variable accessed.
     */
    public double getTimeScale() {return timeScale;}

    /**
     * Sets the ratio of pixels-to-seconds when the x-axis is used as the time
     * axis for a time plot.
     *
     * @param   timeScale_      The ratio of pixels to seconds for the x-axis.
     */
    public void setTimeScale(double timeScale_)
    {
        timeScale = timeScale_;
        xRange = timeScale;
        cellGridView.setXLabel("time " + 0.5 * xRange);
    }

    /**
     * Sets the color to use in cell drawing operations where the color is
     * always the same.
     *
     * @param   color       The color to use.
     */
    public void setDefaultColor(Color color)
    {
        defaultColor = color;
    }

    /**
     * Returns a color based on where the given value lies within the
     * cell-grid-view's z-range.
     *
     * @param   value       The value to map to a color.
     * @return              The color mapped to.
     */
    public Color mapValueToColor(double value)
    {
        value *= 2;
        if (value > zRange) return Color.red;
        else if (value > .8 * zRange) return Color.pink;
        else if (value > .6 * zRange) return Color.orange;
        else if (value > .4 * zRange) return Color.yellow;
        else if (value > .2 * zRange) return Color.green;
        else if (value > 0) return Color.green;
        else if (value > -.2 * zRange) return Color.cyan;
        else if (value > -.4 * zRange) return Color.blue;
        else if (value > -.6 * zRange) return Color.magenta;
        else if (value > -.8 * zRange) return Color.gray;
        else return Color.black;
    }

    /**
     * Configures this component's cell-grid-view according to current
     * member variable values.
     */
    protected void configureCellGridView()
    {
        // configure the cell-grid-view
        cellGridView.setXScale(xRange);
        cellGridView.setYScale(yRange);

        // only set the view visible if it isn't already so; otherwise, this
        // view might be brought to the front of the screen many, many
        // times (if there are many plots sharing this view)
        if (!cellGridView.isVisible()) cellGridView.setVisible(true);
    }

    /**
     * Performs additional work beyond that done in the parent class method.
     */
    protected void deltextHook1(message message)
    {
        // for each content in the given message
        doubleEnt dueTime = new doubleEnt(clock + jobDueDelay);
        for (int i = 0; i < message.getLength(); i++) {
            // if this content is on the draw-string port
            if (messageOnPort(message, "drawString", i)) {
                // add this content to the jobs that have arrived
                DrawCellEntity entity = (DrawCellEntity)
                    message.getValOnPort("drawString", i);
                arrived.put(dueTime, entity);

                // tell the cell-grid-view to draw the entity's name
                cellGridView.drawString(
                    (int)Math.rint(entity.i), (int)Math.rint(entity.j),
                    entity.getName(), entity.color);

                // remember the operation that must be performed to dim what
                // was drawn just above
                entity.setName("drawString");
            }

            // else, if this content is on the draw-cell port
            else if (messageOnPort(message, "drawCell", i)) {
                // add this content to the jobs that have arrived
                DrawCellEntity entity = (DrawCellEntity)
                    message.getValOnPort("drawCell", i);
                arrived.put(dueTime, entity);

                // tell the cell-grid-view to draw the cell specified by the job
                cellGridView.drawCell(
                    (int)Math.rint(entity.i), (int)Math.rint(entity.j),
                    entity.color);

                // remember the operation that must be performed to dim what
                // was drawn just above
                entity.setName("drawCell");
            }

            // else, if this content is on the draw-cell-to-scale port
            else if (messageOnPort(message, "drawCellToScale", i)) {
                // add this content to the jobs that have arrived
                EntityInterface inEntity = message.getValOnPort("drawCellToScale", i);
                DrawCellEntity entity = (DrawCellEntity)inEntity;
              //  DrawCellEntity entity = DrawCellEntity.toObject(inEntity);
                arrived.put(dueTime, entity);

                // tell the cell-grid-view to draw-to-scale the cell
                // specified by the job
                cellGridView.drawCellToScale(entity.i, entity.j, entity.color);

                // remember the operation that must be performed to dim what
                // was drawn just above
                entity.setName("drawCellToScale");
            }

            // else, if this content is on the draw-2d port
            else if (messageOnPort(message, "draw2D", i)) {
                // add this content to the jobs that have arrived
                DrawCellEntity entity = (DrawCellEntity)
                    message.getValOnPort("draw2D", i);
                arrived.put(dueTime, entity);

                // tell the cell-grid-view to draw-to-scale the cell
                // specified by the job using a color mapped from
                // the job's value for the cell
                entity.color = mapValueToColor(entity.k);
                cellGridView.drawCellToScale(entity.i, entity.j, entity.color);

                // remember the operation that must be performed to dim what
                // was drawn just above
                entity.setName("drawCellToScale");
            }

            // else, if this content is on the draw-i port
            else if (messageOnPort(message, "drawI", i)) {
                // store the i specified by the cell as the last one received
                doubleEnt entity = (doubleEnt)message.getValOnPort("drawI", i);
                lastI = entity.getv();

                // add this content to the jobs that have arrived
                arrived.put(dueTime, new DrawCellEntity("drawCellToScale",
                    lastI, lastJ));

                // tell the cell-grid-view to draw-to-scale the cell of the last
                // i and j received
                cellGridView.drawCellToScale(lastI, lastJ, Color.black);
            }

            // else, if this content is on the draw-j port
            else if (messageOnPort(message, "drawJ", i)) {
                // store the j specified by the cell as the last one received
                doubleEnt entity = (doubleEnt)message.getValOnPort("drawJ", i);
                lastJ = entity.getv();

                // add this content to the jobs that have arrived
                arrived.put(dueTime,
                    new DrawCellEntity("drawCellToScale", lastI, lastJ));

                // tell the cell-grid-view to draw-to-scale the cell of the last
                // i and j received
                cellGridView.drawCellToScale(lastI, lastJ, Color.blue);
            }

            // else, if this content is on the time-plot port
            else if (messageOnPort(message, "timePlot", i)) {
                // add this content to the jobs that have arrived
                doubleEnt entity = (doubleEnt)message.getValOnPort(
                    "timePlot", i);
                double time = (-.5 + (clock / timeScale) % 1) * timeScale;
                double value = entity.getv();
                arrived.put(dueTime,
                    new DrawCellEntity("drawCellToTimeScale", time, value,
                        null, timeScale));

                // if the plot is cycling back to the left side of the
                // cell-grid-view
                if (time < 0 && lastTimePlotTime > 0) {
                    // clear the grid
                    dontDimTimePlotCells = true;
                    cellGridView.clearGrid();
                }

                // remember the new current time value for the next pass
                // through here
                lastTimePlotTime = time;

                // tell the cell-grid-view to draw-to-time-scale the cell
                // whose i value is given by the current clock value,
                // and whose j value is specified by the job
                cellGridView.drawCellToTimeScale(time, timeScale,
                    value, defaultColor);
            }

            // else, if this content is on the pulse-plot port
            else if (messageOnPort(message, "pulsePlot", i)) {
                // add this content to the jobs that have arrived
                doubleEnt entity = (doubleEnt)message.getValOnPort(
                    "pulsePlot", i);
                double time = (-.5 + (clock / timeScale) % 1) * timeScale;
                double value = entity.getv();
                arrived.put(dueTime,
                    new DrawCellEntity("drawPulseToTimeScale", time, value,
                        null, timeScale));

                // if the plot is cycling back to the left side of the
                // cell-grid-view
                if (time < 0 && lastPulseTime > 0) {
                    // clear the grid
                    dontDimPulseLines = true;
                    cellGridView.clearGrid();
                }

                // remember the new current time value for the next pass
                // through here
                lastPulseTime = time;

                // tell the cell-grid-view to draw-to-time-scale the pulse
                // whose i value is given by the current clock value,
                // and whose j value is specified by the job
                cellGridView.drawPulseToTimeScale(time, timeScale,
                    value, defaultColor);
            }
        }
    }

    /**
     * Performs additional work beyond that done in the parent class method.
     */
    protected void deltintHook1()
    {
        // for each job that is due
        Iterator i = due.iterator();
        while (i.hasNext()) {
            // if this job is a draw-cell-to-scale job
            DrawCellEntity entity = (DrawCellEntity)i.next();
            String name = entity.getName();
            if (name.startsWith("drawCellToScale")) {
                // dim the cell that was drawn when the job arrived
                cellGridView.drawCellToScale(entity.i, entity.j, entity.dimTo);
            }

            // else, if this job is a draw-cell-to-time-scale job
            else if (name.startsWith("drawCellToTimeScale")) {
                // if we currently aren't supposed to skip dimming time plot
                // cells, or the cell to be dimmed now is on the left side of
                // the view
                if (!dontDimTimePlotCells || entity.i < 0) {
                    // dim the cell that was drawn when the job arrived
                    cellGridView.drawCellToTimeScale(entity.i, entity.timeScale,
                        entity.j, entity.dimTo);
                }

                // else, if we currently aren't supposed to dim time plot
                // cells, yet now the cell to be dimmed is on the left side
                // of the view
                else if (dontDimTimePlotCells && entity.i < 0) {
                    // it's ok to dim time plot cells again
                    dontDimTimePlotCells = false;
                }
            }

            // else, if this job is a draw-pulse-to-time-scale job
            else if (name.startsWith("drawPulseToTimeScale")) {
                // if we currently aren't supposed to skip dimming pulse lines,
                // or the pulse line to be dimmed now is on the left side of
                // the view
                if (!dontDimPulseLines || entity.i < 0) {
                    // dim the pulse line that was drawn when the job arrived
                    cellGridView.drawPulseToTimeScale(entity.i, entity.timeScale,
                        entity.j, entity.dimTo);
                }

                // else, if we currently aren't supposed to dim pulse lines, yet
                // now the pulse line to be dimmed is on the left side of the
                // view
                else if (dontDimPulseLines && entity.i < 0) {
                    // it's ok to dim pulse lines again
                    dontDimPulseLines = false;
                }
            }

            // else, if this job is a draw-cell job
            else if (name.startsWith("drawCell")) {
                // dim the cell that was drawn when the job arrived
                cellGridView.drawCell(
                    (int)Math.rint(entity.i), (int)Math.rint(entity.j),
                    entity.dimTo);
            }

            // else, if the job is a draw-string job
            else if (name.startsWith("drawString")) {
                // dim the string that was drawn when the job arrived
                cellGridView.drawString(
                    (int)Math.rint(entity.i), (int)Math.rint(entity.j),
                    name, entity.dimTo);
            }
        }
    }

    /**
     * See parent method.
     */
    public message out()
    {
        // the parent class's outputting of finished jobs is not desired here
        return new message();
    }

    /**
     * See parent method.
     */
    public String stringState()
    {
        String graphInfo =
            "XRange,YRange : " + xRange + "," + yRange + "\n" +
            "lastI,lastJ : " + lastI + "," + lastJ;
        return super.stringState() + "\n" + graphInfo;
    }

    /**
     * See member variable accessed.
     */
    public CellGridView getCellGridView() {return cellGridView;}
    public void setCellGridView(CellGridView view)
    {
        cellGridView = view;
        configureCellGridView();
    }
}



