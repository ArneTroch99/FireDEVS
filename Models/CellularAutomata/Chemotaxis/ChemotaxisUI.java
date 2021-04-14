package CellularAutomata.Chemotaxis;

import javafx.scene.paint.Color;
import view.CAView.CAViewUI;

public class ChemotaxisUI {
	public static void setPhaseColor(){
		CAViewUI.addPhaseColor("EMPTY", Color.WHITE);
		
		CAViewUI.addPhaseColor("CXCL12", Color.GREEN);		
		CAViewUI.addPhaseColor("CXCR7", Color.BLUE);
		CAViewUI.addPhaseColor("CXCR4", Color.RED);
		
		//intermediate phase
		CAViewUI.addPhaseColor("COMING:-", Color.LIGHTGREY);
		CAViewUI.addPhaseColor("CXCL12:-", Color.MEDIUMSPRINGGREEN);
		CAViewUI.addPhaseColor("CXCR4:-", Color.PALEVIOLETRED);
		CAViewUI.addPhaseColor("CXCR7:-", Color.CORNFLOWERBLUE);
	}
	
//TODO get color of phase	
/*	public static void getPhaseColor(){
		..; 
	}*/
	
}