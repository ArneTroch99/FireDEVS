package CellularAutomata.GameOfLife;

import model.modeling.message;
import model.modeling.CAModels.TwoDimCell;
import util.rand;

import java.awt.Color;
import java.util.Random;

import Component.GridCellplots.DrawCellEntity;
import GenCol.Pair;
import GenCol.entity;

public class LifeDeterministic extends TwoDimCell {
	protected int lifeCount = 0;
	protected entity value;
	public int isLife = 0;
	// protected boolean clocked = true;

	public LifeDeterministic() {
		this(0, 0);
	}

	public LifeDeterministic(int xcoord, int ycoord) {
		super(xcoord, ycoord);
	}

	/**
	 * Initialization method
	 */
	public void initialize() {
		super.initialize();

		if (isLife == 0) {
			holdIn("die", 1);
		} else {
			holdIn("life", 1);
		}

		// Define the Phase Color for CA Display
		//System.out.println("initial phase of cell: " + this.getClass().getName() + " " + "'" + isLife + "'" + " with color associated with " + "'" + this.getPhase() +"'");
		GameOfLifeUI.setPhaseColor();
	}

	//TODO method for querying (x and y coordinate)
	
	/**
	 * External Transition Function
	 */

	public void deltext(double e, message x) {

		Continue(e);
		for (int i = 0; i < x.getLength(); i++) {
			if (somethingOnPort(x, "inN")) {
				value = x.getValOnPort("inN", i);
				if (value != null && value.toString() == "life") {
					lifeCount++;
				}
			}
			if (somethingOnPort(x, "inNE")) {
				value = x.getValOnPort("inNE", i);
				if (value != null && value.toString() == "life") {
					lifeCount++;
				}
			}
			if (somethingOnPort(x, "inE")) {
				value = x.getValOnPort("inE", i);
				if (value != null && value.toString() == "life") {
					lifeCount++;
				}
			}
			if (somethingOnPort(x, "inSE")) {
				value = x.getValOnPort("inSE", i);
				if (value != null && value.toString() == "life") {
					lifeCount++;
				}
			}
			if (somethingOnPort(x, "inS")) {
				value = x.getValOnPort("inS", i);
				if (value != null && value.toString() == "life") {
					lifeCount++;
				}
			}
			if (somethingOnPort(x, "inSW")) {
				value = x.getValOnPort("inSW", i);
				if (value != null && value.toString() == "life") {
					lifeCount++;
				}
			}
			if (somethingOnPort(x, "inW")) {
				value = x.getValOnPort("inW", i);
				if (value != null && value.toString() == "life") {
					lifeCount++;
				}
			}
			if (somethingOnPort(x, "inNW")) {
				value = x.getValOnPort("inNW", i);
				if (value != null && value.toString() == "life") {
					lifeCount++;
				}
			}
			
			System.out.println("initialize SATE of Cell Ext.: " );

		}
		if (phaseIs("life")) {
			lifeCount++;
		}
		if (lifeCount <= 2 || lifeCount >= 5) {
			holdIn("die", 1);
		} else if (lifeCount == 3) {
			holdIn("life", 1);
		}
		else{
			holdIn(phase,1);
		}
		lifeCount = 0;

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

		if (phaseIs("life") || phaseIs("die")) {
			m.add(makeContent("outN", new entity(phase)));

			m.add(makeContent("outNE", new entity(phase)));

			m.add(makeContent("outE", new entity(phase)));

			m.add(makeContent("outSE", new entity(phase)));

			m.add(makeContent("outS", new entity(phase)));

			m.add(makeContent("outSW", new entity(phase)));

			m.add(makeContent("outW", new entity(phase)));

			m.add(makeContent("outNW", new entity(phase)));
		}

		return m;
	}

}
