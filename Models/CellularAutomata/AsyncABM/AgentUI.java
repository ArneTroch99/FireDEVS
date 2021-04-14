package CellularAutomata.AsyncABM;

import javafx.scene.paint.Color;
import view.CAView.CAViewUI;

public class AgentUI {
	public static void setPhaseColor() {
		CAViewUI.addPhaseColor("EMPTY", Color.WHITE);

		CAViewUI.addPhaseColor("CELL: CXCL12", Color.GREEN);
		CAViewUI.addPhaseColor("CELL: CXCR7", Color.BLUE);
		CAViewUI.addPhaseColor("CELL: CXCR4", Color.RED);

		// intermediate phase
		CAViewUI.addPhaseColor("COMING:-", Color.LIGHTGREY);
		CAViewUI.addPhaseColor("CXCL12:-", Color.MEDIUMSPRINGGREEN);
		CAViewUI.addPhaseColor("CXCR4:-", Color.PALEVIOLETRED);
		CAViewUI.addPhaseColor("CXCR7:-", Color.CORNFLOWERBLUE);
	}

}