package CellularAutomata.Diffusion;

import model.modeling.content;
import model.modeling.message;
import model.modeling.CAModels.TwoDimCell;
import model.modeling.CAModels.TwoDimCellSpace;

import java.util.Iterator;

import GenCol.entity;

public class HeatCell extends TwoDimCell {
	protected int HeatValue = 0;
	protected entity value;
	public int isSource = 0;
	// protected boolean clocked = true;
	private String prePhase = "0:-";

	public HeatCell() {
		this(0, 0);
	}

	public HeatCell(int xcoord, int ycoord) {
		super(xcoord, ycoord);
		addInport("inTrans");
		addOutport("outTrans");
	}

	/**
	 * Initialization method
	 */
	public void initialize() {
		super.initialize();
		if (isSource == 1) {
			holdIn("1000:-", 1);
		} else {
			holdIn("0:-", 1);
		}

		// Define the Phase Color for CA Display
		HeatUI.setPhaseColor();
	}

	/**
	 * External Transition Function
	 */

	public void deltext(double e, message x) {
		// long startTime = System.currentTimeMillis();
		Continue(e);
		Iterator it = x.iterator();
		while (it.hasNext()) {
			content c = (content) it.next();
			if (c.getPortName() == "inTrans") {
				value = (entity) c.getValue();
				if (value != null) {
					HeatValue = Integer.parseInt(value.getName());
				}
			}
		}

		if (HeatValue != 0) {
			holdIn(HeatValue+":-", 1);
		} else {
			holdIn(phase, 1);
		}
		HeatValue = 0;

		// long endTime = System.currentTimeMillis();
		// Life3Trans.cellExtTime += endTime - startTime;

	}

	/*
	 * Internal Transition Function
	 */

	public void deltint() {

		holdIn(phase, INFINITY);
	}

	public void deltcon(double e, message x) {
		deltint();
		deltext(0, x);
	}

	/*
	 * Message out Function
	 */
	public message out() {

		message m = new message();

		if (!phase.equals(prePhase)) {
			m.add(makeContent("outTrans", new inputEntity(getXcoord(), getYcoord(), phase)));
			prePhase = phase;
		}

		return m;
	}

}
