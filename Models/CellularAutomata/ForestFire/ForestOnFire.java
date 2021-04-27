package CellularAutomata.ForestFire;

import java.awt.Dimension;
import java.awt.Point;

import javafx.application.Application;
import javafx.stage.Stage;
import model.modeling.CAModels.TwoDimCellSpace;
import view.modeling.ViewableComponent;
import view.modeling.ViewableComponentUtil;

public class ForestOnFire extends TwoDimCellSpace {

	public ForestOnFire() {
		this(50, 50);
	}

	public ForestOnFire(int xDim, int yDim) {
		super("forest", xDim, yDim);
		this.numCells = xDim * yDim;
		for (int i = 0; i < xDimCellspace; i++) {
			for (int j = 0; j < yDimCellspace; j++) {
				Tree tree = new Tree(i, j);
				addCell(tree);
			}
		}
		// Do the couplings
		doNeighborToNeighborCoupling();
	}

    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    @Override
    public void layoutForSimView()
    {
        preferredSize = new Dimension(800, 700);
        ((ViewableComponent)withName("Cell: 8, 2")).setPreferredLocation(new Point(180, 345));
        ((ViewableComponent)withName("Cell: 8, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 8, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 8, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 8, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 8, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 8, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 8, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 8, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 0, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 8, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 5, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 6, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 3, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 4, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 1, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 9, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 4")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 2, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 1")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 2")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 0")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 9")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 7")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 8")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 5")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 6")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 3")).setPreferredLocation(new Point(50, 50));
        ((ViewableComponent)withName("Cell: 7, 4")).setPreferredLocation(new Point(50, 50));
    }
}
