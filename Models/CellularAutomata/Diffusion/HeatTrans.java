package CellularAutomata.Diffusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import GenCol.entity;
import javafx.scene.paint.Color;
import model.modeling.content;
import model.modeling.message;
import model.modeling.CAModels.TwoDimCell;

public class HeatTrans extends TwoDimCell {
	// private int[][] CAResultData;

	private ArrayList<Integer> statusChangeX, statusChangeY;

	private String[][] OutPortsNames;
	private boolean[][] outPortsCheck;

	private int[][] CAMemoryData;

	// private boolean[][] statusChanged;

	protected inputEntity value;
	private int width, height;

	// public static long extTime = 0;
	// public static long outTime = 0;
	// public static long cellExtTime = 0;

	public HeatTrans(int xsize, int ysize) {
		super(-1, -1);
		width = xsize;
		height = ysize;
		OutPortsNames = new String[width][height];

		CAMemoryData = new int[width][height];

		outPortsCheck = new boolean[width][height];

		// store the index of the changed cells
		statusChangeX = new ArrayList<Integer>();
		statusChangeY = new ArrayList<Integer>();

		addInport("inComp");

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				OutPortsNames[i][j] = "outComp:" + i + "," + j;
				addOutport(OutPortsNames[i][j]);

			}
		}

	}

	public void initialize() {
		super.initialize();
		passivate();
	}

	public void deltext(double e, message x) {
		Continue(e);
		Iterator it = x.iterator();
		while (it.hasNext()) {

			content c = (content) it.next();
			if (c.getPortName() == "inComp") {
				value = (inputEntity) c.getValue();

				if (value != null) {
					// Keep the index of input of changed cells
					statusChangeX.add(value.getI());
					statusChangeY.add(value.getJ());
					// update the memory data storage with only status changed
					// cells
					int endIndex = value.getInput().indexOf(":-");
					CAMemoryData[value.getI()][value.getJ()] = Integer
							.parseInt(value.getInput().substring(0, endIndex));
				}

			}
		}

		holdIn("busy", 0);

	}

	public void deltint() {

		passivate();

	}

	public void deltcon(double e, message x) {
		deltint();
		deltext(0, x);
	}

	public message out() {
		message m = new message();
		for (int i = 0; i < statusChangeX.size(); i++) {
			int x = statusChangeX.get(i);
			int y = statusChangeY.get(i);
			for (int dx = -1; dx < 2; dx++) {
				for (int dy = -1; dy < 2; dy++) {
					if (((x + dx) >= 0 && (x + dx) <= width) && ((y + dy) >= 0 && (y + dy) <= height)) {
						if (!outPortsCheck[stx(x + dx)][sty(y + dy)]) {
							content con1 = makeContent(OutPortsNames[stx(x + dx)][sty(y + dy)],
									new entity("" + neighborStateCount(stx(x + dx), sty(y + dy))));
							m.add(con1);
							outPortsCheck[stx(x + dx)][sty(y + dy)] = true;
						}
					}
				}
			}
		}
		statusChangeX.clear();
		statusChangeY.clear();
		outPortsCheck = new boolean[width][height];

		// outputMemoryData();

		return m;
	}

	private int neighborStateCount(int x, int y) {
		int totalData = 0;
		int count = 0;
		for (int dx = -1; dx < 2; dx++) {
			for (int dy = -1; dy < 2; dy++) {

				int inputData = CAMemoryData[stx(x + dx)][sty(y + dy)];
				totalData = totalData + inputData;
				count++;

			}
		}
		return (int)Math.round((double)totalData / count); 
	}

	private final int stx(final int x) {
		if (x >= 0) {
			if (x < width)
				return x;
			return width-1;
		}
		return 0;
	}

	private final int sty(final int y) {
		if (y >= 0) {
			if (y < height)
				return y;
			return height-1;
		}
		return 0;
	}

	public String[][] getOutPortsNames() {
		return OutPortsNames;
	}

	class Index {
		int i, j;

		public Index(int i, int j) {
			this.i = i;
			this.j = j;
		}

	}

}
