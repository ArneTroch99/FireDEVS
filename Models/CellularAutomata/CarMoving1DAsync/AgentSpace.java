package CellularAutomata.CarMoving1DAsync;

import java.awt.Dimension;
import java.awt.Point;

import model.modeling.CAModels.TwoDimCellSpace;
import view.modeling.ViewableComponent;

public class AgentSpace extends TwoDimCellSpace
{

    public AgentSpace()
    {
        this(8, 1);
    }

    public AgentSpace(int xDim, int yDim)
    {
        super("Cars Moving", xDim, yDim);

        this.numCells = xDim * yDim;
        for (int i = 0; i < xDimCellspace; i++)
        {
            for (int j = 0; j < yDimCellspace; j++)
            {
                Agent cell;
                if (i == 0)
                {
                    cell = new Agent(i, j, "Car2", 2);
                }
                else if (i == 1)
                {
                    cell = new Agent(i, j, "Car1", 1);
                }
                else if (i == 2)
                {
                    cell = new Agent(i, j, "Car3", 3);
                }
                else
                {
                    cell = new Agent(i, j, "EMPTY", 1);
                }
                addCell(cell);
                cell.initialize();
            }
        }

        doNeighborToNeighborCoupling();

    }

    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    public void layoutForSimView()
    {
        preferredSize = new Dimension(1579, 332);
        ((ViewableComponent)withName("Cell: 1, 0")).setPreferredLocation(new Point(148, 52));
        ((ViewableComponent)withName("Cell: 3, 0")).setPreferredLocation(new Point(527, 52));
        ((ViewableComponent)withName("Cell: 5, 0")).setPreferredLocation(new Point(902, 52));
        ((ViewableComponent)withName("Cell: 7, 0")).setPreferredLocation(new Point(1271, 52));
        ((ViewableComponent)withName("Cell: 0, 0")).setPreferredLocation(new Point(-45, 52));
        ((ViewableComponent)withName("Cell: 2, 0")).setPreferredLocation(new Point(338, 52));
        ((ViewableComponent)withName("Cell: 6, 0")).setPreferredLocation(new Point(1084, 52));
        ((ViewableComponent)withName("Cell: 4, 0")).setPreferredLocation(new Point(712, 52));
    }
}
