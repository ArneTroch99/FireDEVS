/**
 * This program implements a 2-D cell for the dynamic forest fire model
 * with an optimized cell space
 * author: Lewis Ntaimo
 * Date:   April 15, 2003
 * Revision:
 *
 */
package model.modeling.CAModels;

import GenCol.*;
import model.modeling.*;
import util.rand;
import util.statistics.*;
import view.modeling.*;
import view.simView.*;

public abstract class TwoDimCell extends ViewableAtomic implements Cell {
	protected int numCells; // Total number of cells in the cell space
	protected int xcoord; // The x-coordinate of this cell in the cell space
	protected int ycoord; // The y-coordinate of this cell in the cell space
	protected Pair id; // Unique cell id: equals cell pos in cell space
	protected boolean coupled; // This is true if a cell has all the couplings
								// done

	protected rand r;
	protected int numTransitions, numTransAll;
	boolean initial;

	protected Pair inpair;

	/**
	 * Default constructor
	 */
	public TwoDimCell() {
		this(new Pair(new Integer(0), new Integer(0)));
	}

	public TwoDimCell(int xcoord, int ycoord) {
		this(new Pair(new Integer(xcoord), new Integer(ycoord)));
	}

	/**
	 * Default constructor
	 */
	public TwoDimCell(Pair cellId) {
		super("Cell: " + cellId.getKey() + ", " + cellId.getValue());
		id = cellId;
		Integer x = (Integer) cellId.getKey();
		Integer y = (Integer) cellId.getValue();
		int xcoord = x.intValue();
		int ycoord = y.intValue();
		// super("Cell_"+ xcoord + "_"+ ycoord);

		// id = new Pair(new Integer(xcoord), new Integer(ycoord));
		this.xcoord = xcoord;
		this.ycoord = ycoord;
		coupled = false;
		addInport("start");
		addInport("stop");
		addInport("inN");
		addInport("inNE");
		addInport("inE");
		addInport("inSE");
		addInport("inS");
		addInport("inSW");
		addInport("inW");
		addInport("inNW");
		addOutport("outN");
		addOutport("outNE");
		addOutport("outE");
		addOutport("outSE");
		addOutport("outS");
		addOutport("outSW");
		addOutport("outW");
		addOutport("outNW");
		addOutport("outCoord");

		// Add test ports

		addTestInput("start", new entity(""));
		addTestInput("inN", new Pair(new Integer(xcoord), new Integer(ycoord + 1)));
		addTestInput("inNE", new Pair(new Integer(xcoord + 1), new Integer(ycoord + 1)));
		addTestInput("inE", new Pair(new Integer(xcoord + 1), new Integer(ycoord)));
		addTestInput("inSE", new Pair(new Integer(xcoord + 1), new Integer(ycoord - 1)));
		addTestInput("inS", new Pair(new Integer(xcoord), new Integer(ycoord - 1)));
		addTestInput("inSW", new Pair(new Integer(xcoord - 1), new Integer(ycoord - 1)));
		addTestInput("inW", new Pair(new Integer(xcoord - 1), new Integer(ycoord - 1)));
		addTestInput("inNW", new Pair(new Integer(xcoord - 1), new Integer(ycoord + 1)));

	}

	public void initialize() {
		super.initialize();
		// initial = false;
		// numTransitions = 0;
		// passivate();
	}

	public void deltext(double e, message x) {
		Continue(e);
		for (int i = 0; i < x.getLength(); i++) {
			if (somethingOnPort(x, "start")) {
				holdIn("active", 0);
			} else if (somethingOnPort(x, "stop")) {
				passivate();
			}

			// Get notification message from neighbor cells
			if (somethingOnPort(x, "inN")) {
				inpair = (Pair) x.getValOnPort("inN", i);
			} else if (somethingOnPort(x, "inNE")) {
				inpair = (Pair) x.getValOnPort("inNE", i);
			} else if (somethingOnPort(x, "inE")) {
				inpair = (Pair) x.getValOnPort("inE", i);
			} else if (somethingOnPort(x, "inSE")) {
				inpair = (Pair) x.getValOnPort("inSE", i);
			} else if (somethingOnPort(x, "inS")) {
				inpair = (Pair) x.getValOnPort("inS", i);
			} else if (somethingOnPort(x, "inSW")) {
				inpair = (Pair) x.getValOnPort("inSW", i);
			} else if (somethingOnPort(x, "inW")) {
				inpair = (Pair) x.getValOnPort("inW", i);
			} else if (somethingOnPort(x, "inNW")) {
				inpair = (Pair) x.getValOnPort("inNW", i);
			}

		}
	}

	public void deltint() {
		if (initial) {
			Integer my_int = (Integer) id.getKey();
			r = new rand(my_int.intValue());
			initial = false;
		}
		numTransitions++;
		passivate();
	}

	public message out() {
		message m = new message();
		m.add(makeContent("outCoord", new Pair(new Integer(xcoord), new Integer(ycoord))));
		return m;
	}

	public String getTooltipText() {
		return super.getTooltipText() + "\n" + " Cell (" + xcoord + ", " + ycoord + ")";
	}

	/**
	 * This methods sets the position for the cell
	 **/

	public int[] pairToArray(Pair inpair) {
		Integer i = (Integer) inpair.getKey();
		Integer j = (Integer) inpair.getValue();
		int[] arr = { i.intValue(), j.intValue() };
		return arr;
	}

	public void setCoordNPos(int xcoord, int ycoord) {
		this.xcoord = xcoord;
		this.ycoord = ycoord;
	}

	public void setCoordNPos(Pair inpair) {
		int[] arr = pairToArray(inpair);
		this.xcoord = arr[0];
		this.ycoord = arr[1];
	}

	/**
	 * This sets old to true: this implies that this cell has all the neighbor
	 * couplings done
	 */
	public void setCouplingsDone() {
		coupled = true;
	}

	/**
	 * This method returns this cells id
	 * 
	 * @return id
	 */
	public Pair getId() {
		return new Pair(new Integer(xcoord), new Integer(ycoord));
	}

	/**
	 * This method returns this cells x coordinate
	 * 
	 * @return xcoord
	 */
	public int getXcoord() {
		return xcoord;
	}

	/**
	 * This method returns this cells y coordinate
	 * 
	 * @return ycoord
	 */
	public int getYcoord() {
		return ycoord;
	}

	// added by Chao for read neighbor's phase
	private TwoDimCellSpace getParentSpace() {
		return (TwoDimCellSpace) this.getParent();

	}

	public int getWidth() {
		return getParentSpace().xDimCellspace;

	}

	public int getHeight() {
		return getParentSpace().yDimCellspace;

	}

	public TwoDimCell getNeighbor(int x, int y) {
		return getParentSpace().getCell(x, y);

	}
	
	public String getNeighborPhase(int x, int y) {
		return getNeighbor(x,y).getFormattedPhase();
	}

	public Pair neighborId(int i, int j) {
		int xc = xcoord + i;
		int yc = ycoord + j;
		return new Pair(new Integer(xc), new Integer(yc));
	}

	public boolean isNorthNeighbor(int i, int j) {
		return xcoord == i && ycoord == j - 1;
	}

	public boolean isSouthNeighbor(int i, int j) {
		return xcoord == i && ycoord == j + 1;
	}

	public boolean isEastNeighbor(int i, int j) {
		return xcoord == i - 1 && ycoord == j;
	}

	public boolean isWestNeighbor(int i, int j) {
		return xcoord == i + 1 && ycoord == j;
	}

	public boolean isMooreNeighbor(int i, int j) {
		return isNorthNeighbor(i, j) || isSouthNeighbor(i, j) || isEastNeighbor(i, j) || isWestNeighbor(i, j);
	}

	public boolean isMooreNeighbor(Pair cellId) {
		Integer i = (Integer) cellId.getKey();
		Integer j = (Integer) cellId.getValue();
		int ii = i.intValue();
		int jj = j.intValue();
		return isMooreNeighbor(ii, jj);
	}

	public boolean isClose(Pair cellId) {
		Integer i = (Integer) cellId.getKey();
		Integer j = (Integer) cellId.getValue();
		int ii = i.intValue();
		int jj = j.intValue();
		return (ii - xcoord) * (ii - xcoord) + (jj - ycoord) * (jj - ycoord) < 5;
	}

	/**
	 * This method returns true if all the couplings are done for this cell
	 * 
	 * @return coupled
	 **/

	public boolean isAllCoupled() {
		return coupled;
	}

	/**
	 * This method does a neighbor-to-neighbor 2D coupling of cells in a 2D space
	 * where each cell has 8 neighbors except the border cells.
	 * 
	 * @param otherID
	 *            neighbor ID
	 * @return returns true if otherID is this cell's neighbor
	 */
	/*
	 * public boolean neighbor(int otherId){ int idW = otherId+1; int idE =
	 * otherId-1; int idN = otherId+xDim; int idS = otherId-xDim; int idNW =
	 * otherId+xDim+1; int idNE = otherId+xDim-1; int idSW = otherId-xDim+1; int
	 * idSE = otherId-xDim-1; if ( (id+1)%xDim == 0 ) // RHS edge cells return
	 * (idW==id || idN==id || idS==id || idNW==id || idSW==id); else if (id%xDim ==
	 * 0) // LHS edge cells return (idE==id|| idN==id || idS==id || idNE==id ||
	 * idSE==id); else // all other cells return (idW==id || idE==id|| idN==id ||
	 * idS==id || idNW==id || idNE==id || idSW==id || idSE==id); }
	 */
	/**
	 * propagate message m using port type pn to Moore neighbors
	 * 
	 * @return message
	 **/

	public message propagate(message m, String pn) {

		m.add(makeContent(pn + "outN", new Pair(new Integer(xcoord), new Integer(ycoord))));

		// m.add(makeContent(pn+""outNE",new Pair(new Integer(xcoord), new
		// Integer(ycoord))));

		m.add(makeContent(pn + "outE", new Pair(new Integer(xcoord), new Integer(ycoord))));

		// m.add(makeContent(pn+"outSE",new Pair(new Integer(xcoord), new
		// Integer(ycoord))));

		m.add(makeContent(pn + "outS", new Pair(new Integer(xcoord), new Integer(ycoord))));

		// m.add(makeContent(pn+"outSW",new Pair(new Integer(xcoord), new
		// Integer(ycoord))));

		m.add(makeContent(pn + "outW", new Pair(new Integer(xcoord), new Integer(ycoord))));

		// m.add(makeContent(pn+"outNW",new Pair(new Integer(xcoord), new
		// Integer(ycoord))));

		return m;
	}

	/**
	 * propagate message m using port type pn to neighbors
	 * 
	 * @return message
	 **/

	public message propagateMealy(message m, String pn) {

		m.add(makeContent(pn + "outN", new Pair(new Integer(xcoord), new Integer(ycoord))));

		m.add(makeContent(pn + "outNE", new Pair(new Integer(xcoord), new Integer(ycoord))));

		m.add(makeContent(pn + "outE", new Pair(new Integer(xcoord), new Integer(ycoord))));

		m.add(makeContent(pn + "outSE", new Pair(new Integer(xcoord), new Integer(ycoord))));

		m.add(makeContent(pn + "outS", new Pair(new Integer(xcoord), new Integer(ycoord))));

		m.add(makeContent(pn + "outSW", new Pair(new Integer(xcoord), new Integer(ycoord))));

		m.add(makeContent(pn + "outW", new Pair(new Integer(xcoord), new Integer(ycoord))));

		m.add(makeContent(pn + "outNW", new Pair(new Integer(xcoord), new Integer(ycoord))));

		return m;
	}

	/**
	 * is there something on port type pn in Mealy neighborhood
	 * 
	 * @return boolean
	 **/

	public boolean somethingOnPortType(message x, String port) {
		return somethingOnPort(x, port + "inN") || somethingOnPort(x, port + "inNE") || somethingOnPort(x, port + "inE")
				|| somethingOnPort(x, port + "inSE") || somethingOnPort(x, port + "inS")
				|| somethingOnPort(x, port + "inSW") || somethingOnPort(x, port + "inW")
				|| somethingOnPort(x, port + "inNW");
	}

	public boolean somethingOnPort(message x, String port) {
		for (int i = 0; i < x.getLength(); i++)
			if (messageOnPort(x, port, i))
				return true;
		return false;

	}

} // End class TwoDimCell
