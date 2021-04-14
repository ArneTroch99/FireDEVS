package CellularAutomata.Agent;

import javafx.scene.paint.Color;
import view.CAView.CAViewUI;

public class AgentUI {
	public static void setPhaseColor() {
		CAViewUI.addPhaseColor("EMPTY", Color.WHITE);

		CAViewUI.addPhaseColor("Has Agent: CXCL12", Color.GREEN);
		CAViewUI.addPhaseColor("Has Agent: CXCR7", Color.BLUE);
		CAViewUI.addPhaseColor("Has Agent: CXCR4", Color.RED);

		// intermediate phase
		CAViewUI.addPhaseColor("COMING:-", Color.LIGHTGREY);
		CAViewUI.addPhaseColor("CXCL12:-", Color.MEDIUMSPRINGGREEN);
		CAViewUI.addPhaseColor("CXCR4:-", Color.PALEVIOLETRED);
		CAViewUI.addPhaseColor("CXCR7:-", Color.CORNFLOWERBLUE);
	}

}
