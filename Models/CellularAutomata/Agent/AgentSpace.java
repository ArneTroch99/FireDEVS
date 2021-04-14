package CellularAutomata.Agent;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Random;

import model.modeling.CAModels.TwoDimCellSpace;
import view.modeling.ViewableComponent;

public class AgentSpace extends TwoDimCellSpace {

	private final String[] statusCXCL12 = { "EMPTY",  "CXCL12" };
	private final String[] statusCXCLR4 = { "EMPTY", "CXCR4" };
	private final String[] statusCXCLR7 = { "EMPTY", "CXCR7" };

	public AgentSpace() {
		this(5, 5);
	}

	public AgentSpace(int xDim, int yDim) {
		super("Chemotaxis", xDim, yDim);

		this.numCells = xDim * yDim;
		for (int i = 0; i < xDimCellspace; i++) {
			for (int j = 0; j < yDimCellspace; j++) {
				Random randomno = new Random();
				Agent cell;
				if (i <= xDimCellspace / 3) {
					cell = new Agent(i, j, statusCXCL12[randomno.nextInt(statusCXCL12.length)], 1);
				} else if (i >= xDimCellspace / 3 * 2) {
					cell = new Agent(i, j, statusCXCLR4[randomno.nextInt(statusCXCLR4.length)], 1);
				} else {
					cell = new Agent(i, j, statusCXCLR7[randomno.nextInt(statusCXCLR7.length)], 1);
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
        preferredSize = new Dimension(1257, 950);
        ((ViewableComponent)withName("Cell: 1, 3")).setPreferredLocation(new Point(213, 563));
        ((ViewableComponent)withName("Cell: 4, 1")).setPreferredLocation(new Point(851, 212));
        ((ViewableComponent)withName("Cell: 0, 0")).setPreferredLocation(new Point(0, 35));
        ((ViewableComponent)withName("Cell: 4, 0")).setPreferredLocation(new Point(842, 38));
        ((ViewableComponent)withName("Cell: 0, 2")).setPreferredLocation(new Point(-1, 384));
        ((ViewableComponent)withName("Cell: 1, 4")).setPreferredLocation(new Point(212, 747));
        ((ViewableComponent)withName("Cell: 4, 2")).setPreferredLocation(new Point(847, 386));
        ((ViewableComponent)withName("Cell: 4, 4")).setPreferredLocation(new Point(843, 746));
        ((ViewableComponent)withName("Cell: 2, 3")).setPreferredLocation(new Point(418, 565));
        ((ViewableComponent)withName("Cell: 1, 1")).setPreferredLocation(new Point(213, 205));
        ((ViewableComponent)withName("Cell: 3, 1")).setPreferredLocation(new Point(640, 211));
        ((ViewableComponent)withName("Cell: 2, 2")).setPreferredLocation(new Point(423, 384));
        ((ViewableComponent)withName("Cell: 4, 3")).setPreferredLocation(new Point(851, 561));
        ((ViewableComponent)withName("Cell: 0, 3")).setPreferredLocation(new Point(-5, 568));
        ((ViewableComponent)withName("Cell: 0, 4")).setPreferredLocation(new Point(-7, 750));
        ((ViewableComponent)withName("Cell: 1, 0")).setPreferredLocation(new Point(216, 32));
        ((ViewableComponent)withName("Cell: 3, 2")).setPreferredLocation(new Point(638, 386));
        ((ViewableComponent)withName("Cell: 2, 1")).setPreferredLocation(new Point(424, 208));
        ((ViewableComponent)withName("Cell: 2, 4")).setPreferredLocation(new Point(417, 752));
        ((ViewableComponent)withName("Cell: 3, 4")).setPreferredLocation(new Point(638, 753));
        ((ViewableComponent)withName("Cell: 2, 0")).setPreferredLocation(new Point(426, 37));
        ((ViewableComponent)withName("Cell: 3, 0")).setPreferredLocation(new Point(636, 36));
        ((ViewableComponent)withName("Cell: 0, 1")).setPreferredLocation(new Point(0, 210));
        ((ViewableComponent)withName("Cell: 1, 2")).setPreferredLocation(new Point(211, 384));
        ((ViewableComponent)withName("Cell: 3, 3")).setPreferredLocation(new Point(638, 556));
    }
}
