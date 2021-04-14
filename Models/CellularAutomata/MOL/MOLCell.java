package CellularAutomata.MOL;

import model.modeling.content;
import model.modeling.message;
import model.modeling.CAModels.TwoDimCell;
import model.modeling.CAModels.TwoDimCellSpace;

import java.util.Iterator;

import GenCol.entity;

public class MOLCell extends TwoDimCell {
	protected double HeatValue = 0;
	protected entity value;
	public int isSource = 0;
	public int isBorder = 0;
	// protected boolean clocked = true;
	private String prePhase = "0:-";

	private double valueNW = 0.0;
	private double valueSW = 0.0;
	private double valueW = 0.0;
	private double preValue = 0.0;

	private double dx = 0.0526;
	private double dt = 0.0013;

	public MOLCell() {
		this(0, 0);
	}

	public MOLCell(int xcoord, int ycoord) {
		super(xcoord, ycoord);
	}

	/**
	 * Initialization method
	 */
	public void initialize() {
		super.initialize();
		if (isSource == 1) { 
			HeatValue = 100 * Math.sin(Math.PI * (ycoord) * dx);
			holdIn(HeatValue + ":-", 1);
		} else {
			HeatValue = 0.0;
			holdIn(HeatValue + ":-", 1);
		}

		// Define the Phase Color for CA Display
		MOLUI.setPhaseColor();
	}

	/**
	 * External Transition Function
	 */

	public void deltext(double e, message x) {
		// long startTime = System.currentTimeMillis();
		Continue(e);
		if (isBorder == 0) {
			Iterator it = x.iterator();
			while (it.hasNext()) {
				content c = (content) it.next();
				if (c.getPortName() == "inNW") {
					value = (entity) c.getValue();
					if (value != null) {
						valueNW = Double.parseDouble(value.getName());
					}
				} else if (c.getPortName() == "inSW") {
					value = (entity) c.getValue();
					if (value != null) {
						valueSW = Double.parseDouble(value.getName());
					}
				} else if (c.getPortName() == "inW") {
					value = (entity) c.getValue();
					if (value != null) {
						valueW = Double.parseDouble(value.getName());
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
			m.add(makeContent("outNE", new entity(HeatValue + "")));

			m.add(makeContent("outE", new entity(HeatValue + "")));

			m.add(makeContent("outSE", new entity(HeatValue + "")));

		}

		return m;
	}

	// use FTCS scheme to solve PDE
	private double newHeatValue() {
		return (double) (valueW + dt * (valueNW + valueSW - 2 * valueW) / (dx * dx));
	}

}
