package CellularAutomata.Chemotaxis;

import java.util.ArrayList;
import java.util.Random;

import GenCol.entity;
import model.modeling.message;
import model.modeling.CAModels.TwoDimCell;

public class ChemotaxisGridCell extends TwoDimCell {
	protected entity msg;
	// 8 neighbors and 1 itself
	protected String[] goingDirections = { "Stay", "Stay", "Stay", "Stay", "Stay", "Stay", "Stay", "Stay", "Stay" };
	protected String[] neighborIn = { "inN", "inNE", "inE", "inSE", "inS", "inSW", "inW", "inNW" };
	protected String[] neighborOut = { "outN", "outNE", "outE", "outSE", "outS", "outSW", "outW", "outNW" };
	protected String[] comingDirections = { "No", "No", "No", "No", "No", "No", "No", "No" };
	protected ArrayList<Integer> comingShuffle = new ArrayList<Integer>();

	private enum StatusType {
		EMPTY, WANTMOVE, MOVING, MOVED, CXCL12, CXCR4, CXCR7
	};

	// status is the phase
	private String initialStatus, status;
	private String goDirection = "Stay";
	private String comeDirection = "No";
	private String finalDirection = "No";
	// protected boolean clocked = true;

	public ChemotaxisGridCell() {
		this(0, 0);
	}

	public ChemotaxisGridCell(int xcoord, int ycoord) {
		super(xcoord, ycoord);
	}

	/**
	 * Initialization method
	 */
	public void initialize() {
		super.initialize();
		status = initialStatus;
		
		if (status == "CXCL12") {
			holdIn("CXCL12", INFINITY);
		} else if (status == "CXCR4") {
			holdIn("CXCR4", INFINITY);
		} else if (status == "CXCR7") {
			holdIn("CXCR7", INFINITY);
		} else {
			holdIn("EMPTY", 1);
		}

		// Define the Phase Color for CA Display
		ChemotaxisUI.setPhaseColor();
	}

	/**
	 * External Transition Function
	 */

	public void deltext(double e, message x) {

		Continue(e);
		for (int i = 0; i < x.getLength(); i++) {
			for (int j = 0; j < neighborIn.length; j++) {
				if (somethingOnPort(x, neighborIn[j])) {
					msg = x.getValOnPort(neighborIn[j], i);
					if (msg != null && msg.toString().contains("EMPTY")) {
						if (!phaseIs("EMPTY"))
							goingDirections[j] = neighborOut[j];
					} else if (msg != null && msg.toString().contains("WANTMOVE")) {
						comingDirections[j] = "WANTMOVE";
					} else if (msg != null && msg.toString().contains("COMING")) {
						finalDirection = neighborOut[j];
					} else if (msg != null && msg.toString() == "CXCL12") {
						phase = "CXCL12";
						status = "CXCL12";
					} else if (msg != null && msg.toString() == "CXCR4") {
						phase = "CXCR4";
						status = "CXCR4";
					} else if (msg != null && msg.toString() == "CXCR7") {
						phase = "CXCR7";
						status = "CXCR7";
					}
				}
			}
		}

		Random r = new Random();

		goDirection = goingDirections[r.nextInt(9)];

		for (int i = 0; i < comingDirections.length; i++) {
			if (comingDirections[i] != "No") {
				comingShuffle.add(i);
			}
		}
		int pickCome = 0;
		if (comingShuffle.size() > 0) {
			pickCome = r.nextInt(comingShuffle.size());
			comeDirection = neighborIn[comingShuffle.get(pickCome)];
		}

		if (goDirection == "Stay" && comeDirection == "No" && finalDirection == "No") {
			holdIn(phase, 1);
		} else if (goDirection != "Stay") {
			holdIn(status + ":- WANTMOVE " + goDirection, 0);
		} else if (comeDirection != "No") {
			holdIn("COMING:- " + comeDirection, 0);
		} else if (finalDirection != "No") {
			holdIn(status + ":- MOVING " + finalDirection, 0);
		}

		// reset the direction array
		for (int i = 0; i < goingDirections.length; i++) {
			goingDirections[i] = "Stay";
		}
		for (int i = 0; i < comingDirections.length; i++) {
			comingDirections[i] = "No";
		}
		comingShuffle.clear();

		// System.out.println(getXcoord() + ", " + getYcoord() + ": " + phase);

	}

	/*
	 * Internal Transition Function
	 */

	public void deltint() {

		if (status == "EMPTY") {
			holdIn("EMPTY", 1);
		} else if (phase.contains("WANTMOVE")) {
			holdIn(status, INFINITY);

		} else {
			holdIn(phase, INFINITY);
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

		message m = new message();

		if (phaseIs("EMPTY")) {
			for (int i = 0; i < neighborOut.length; i++) {
				m.add(makeContent(neighborOut[i], new entity(phase + " " + getXcoord() + ", " + getYcoord())));
			}
		} else if (phase.contains("WANTMOVE")) {
			for (int i = 0; i < neighborOut.length; i++) {
				if (goDirection == neighborOut[i]) {
					m.add(makeContent(neighborOut[i], new entity(phase)));
					break;
				}
			}
		} else if (phase.contains("COMING")) {
			for (int i = 0; i < neighborOut.length; i++) {
				if (comeDirection == neighborIn[i]) {
					m.add(makeContent(neighborOut[i], new entity(phase)));
					break;
				}
			}
		} else if (phase.contains("MOVING")) {
			for (int i = 0; i < neighborOut.length; i++) {
				if (finalDirection == neighborOut[i]) {
					m.add(makeContent(neighborOut[i], new entity(status)));
					status = "EMPTY";
					break;
				}
			}

		}

		goDirection = "Stay";
		comeDirection = "No";
		finalDirection = "No";

		return m;

	}

	public String getStatus() {
		return status;
	}

	public void setInitialStatus(String status) {
		this.initialStatus = status;
	}

}
