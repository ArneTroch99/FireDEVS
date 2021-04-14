package CellularAutomata.HeatDiffusion;

import model.modeling.content;
import model.modeling.message;
import model.modeling.CAModels.TwoDimCell;
import model.modeling.CAModels.TwoDimCellSpace;

import java.util.Iterator;

import GenCol.entity;

public class HeatCell extends TwoDimCell {
	protected double HeatValue = 0;
	protected entity value;
	public int isSource = 0;
	// protected boolean clocked = true;
	private String prePhase = "0:-";

	private double valueN = 0;
	private double valueS = 0;
	private double valueW = 0;
	private double valueE = 0;
	private double preValue = 0;
	
	private double dx = 0.0526;
	private double dy = 0.0526;
	private double dt = 0.0013;

	public HeatCell() {
		this(0, 0);
	}

	public HeatCell(int xcoord, int ycoord) {
		super(xcoord, ycoord);
	}

	/**
	 * Initialization method
	 */
	public void initialize() {
		super.initialize();
		if (isSource == 1) {
			HeatValue = 100;
			holdIn(HeatValue + ":-", 1);
		} else {
			HeatValue = 0;
			holdIn(HeatValue + ":-", 1);
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
		if(isSource == 0) {
		Iterator it = x.iterator();
		while (it.hasNext()) {
			content c = (content) it.next();
			if (c.getPortName() == "inN") {
				value = (entity) c.getValue();
				if (value != null) {
					valueN = Double.parseDouble(value.getName());
				}
			} else if (c.getPortName() == "inS") {
				value = (entity) c.getValue();
				if (value != null) {
					valueS = Double.parseDouble(value.getName());
				}
			} else if (c.getPortName() == "inW") {
				value = (entity) c.getValue();
				if (value != null) {
					valueW = Double.parseDouble(value.getName());
				}
			} else if (c.getPortName() == "inE") {
				value = (entity) c.getValue();
				if (value != null) {
					valueE = Double.parseDouble(value.getName());
				}
			}
		}

		if (HeatValue != newHeatValue()) {
			preValue = HeatValue;
			HeatValue = newHeatValue();
			holdIn(HeatValue + ":-", 1);
		} else {
			preValue = HeatValue;
			holdIn(phase, 1);
		}
		}

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

		if (HeatValue != preValue && HeatValue != 0) {
			m.add(makeContent("outN", new entity(HeatValue + "")));

			m.add(makeContent("outE", new entity(HeatValue + "")));

			m.add(makeContent("outS", new entity(HeatValue + "")));

			m.add(makeContent("outW", new entity(HeatValue + "")));

		}

		return m;
	} 

	// use FTCS scheme to solve PDE
	private double newHeatValue() {
		double coef = 4.0;
		if (xcoord == 0 || xcoord == getWidth() - 1) {
			coef--;
		}
		if (ycoord == 0 || ycoord == getHeight() - 1) {
			coef--;
		}
		return (HeatValue + (dt/(dx*dx))/2 * (valueN + valueS + valueE + valueW - coef * HeatValue));
	}

}
