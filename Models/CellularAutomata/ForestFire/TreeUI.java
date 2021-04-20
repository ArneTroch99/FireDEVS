package CellularAutomata.ForestFire;

import javafx.scene.paint.Color;
import view.CAView.CAViewUI;

public class TreeUI {
	public static void setPhaseColor(){
		CAViewUI.addPhaseColor("tree", Color.GREEN);
		CAViewUI.addPhaseColor("passive", Color.WHITE);
		CAViewUI.addPhaseColor("fire", Color.RED);

	}
	
}
