package CellularAutomata.GameOfLife;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Random;

import Component.BasicProcessor.genr;
import javafx.application.Application;
import javafx.stage.Stage;
import model.modeling.CAModels.TwoDimCellSpace;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
import view.modeling.ViewableComponentUtil;

public class GameOfLife extends TwoDimCellSpace {

	// A b-heptomino looks like this:
	// X
	// XXX
	// X XX
	public static final int[][] b_heptomino = new int[][] { { 0, 1, 1 }, { 1, 1, 0 }, { 0, 1, 1 }, { 0, 0, 1 } };

	public GameOfLife() {
		this(10, 10);
	}

	public GameOfLife(int xDim, int yDim) {
		super("Game of Life", xDim, yDim);

		this.numCells = xDim * yDim;
		for (int i = 0; i < xDimCellspace; i++) {
			for (int j = 0; j < yDimCellspace; j++) {
				Life life = new Life(i, j);
				addCell(life);

				Random randomno = new Random();
				life.isLife = randomno.nextInt(2);
				life.initialize();

			}
		}

		// for(int x=0;x<b_heptomino.length;x++)
		// for(int y=0;y<b_heptomino[x].length;y++){
		// Life l = (Life)(getCell(x+xDim/2-b_heptomino.length/2,
		// y+yDim/2-b_heptomino[x].length/2));
		// l.isLife = b_heptomino[x][y];
		// l.initialize();
		// }

		// Do the couplings

		doNeighborToNeighborCoupling();

	}

}
