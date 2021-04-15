package CellularAutomata.ForestFire;

import model.modeling.message;
import model.modeling.CAModels.TwoDimCell;
import util.rand;

import java.awt.Color;
import java.util.Random;

import Component.GridCellplots.DrawCellEntity;
import GenCol.Pair;
import GenCol.entity;

public class Tree extends TwoDimCell {

	public Tree() {
		this(0, 0);
	}

	public Tree(int xcoord, int ycoord) {
		super(xcoord, ycoord);
		addTestInput("inN", new Pair("status", "fire"));

		// Add ports not in TwoDimCell

	} // End cell constructor

	/**
	 * Initialization method
	 */
	public void initialize() {
		super.initialize();
		if (new Random().nextInt(10) > 1) {
			holdIn("tree", 1);
			// System.out.println(phase);
		} else {
			passivate();
			// holdIn("passive", INFINITY);
		}
		// Define the Phase Color for CA Display
		TreeUI.setPhaseColor();		
	}

	/**
	 * External Transition Function
	 */

	public void deltext(double e, message x) {

		Continue(e);
		// System.out.println(phase + " on " + xcoord + "," + ycoord);
		if (phaseIs("tree")) {
			for (int i = 0; i < x.getLength(); i++) {
				// if (somethingOnPort(x, "start")) {
				// holdIn("active", 0);
				// } else if (somethingOnPort(x, "stop")) {
				// passivate();
				// }

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

				if (inpair != null && inpair.getValue().toString() == "fire") {
					// System.out.println("Catching fire");
					if (new Random().nextInt(10) < 1) {
						holdIn("fire", new Random().nextInt(3) + 3);
						System.out.println("Catching fire on " + xcoord + "," + ycoord);
					}
				}
			}
		}
	}

	/*
	 * Internal Transition Function
	 */

	public void deltint() {
		// System.out.println(phase);
		if (phaseIs("tree")) {
			// 0.2% chance to catch a fire itself
			if (new Random().nextInt(1000) < 500) {
				holdIn("fire", new Random().nextInt(5) + 3);
			}
		} else if (phaseIs("fire")) {
			if (new Random().nextInt(10) < 2) {
				holdIn("tree", new Random().nextInt(10) + 3);

			} else if (new Random().nextInt(10) < 1) {
				passivate();
				// ? should I do that
				// holdIn("passive", 10);
			} else
				holdIn("fire", new Random().nextInt(15) + 3);
		}
	}

	public void deltcon(double e, message x) {
		deltint();
		deltext(0, x);
	}

	/*
	 * Message out Function
	 */
	public message out() {
		message m = super.out();
		if (phaseIs("fire")) {
			m.add(makeContent("outN", new Pair("status", "fire")));

			m.add(makeContent("outNE", new Pair("status", "fire")));

			m.add(makeContent("outE", new Pair("status", "fire")));

			m.add(makeContent("outSE", new Pair("status", "fire")));

			m.add(makeContent("outS", new Pair("status", "fire")));

			m.add(makeContent("outSW", new Pair("status", "fire")));

			m.add(makeContent("outW", new Pair("status", "fire")));

			m.add(makeContent("outNW", new Pair("status", "fire")));

		}

		return m;
	}

}
