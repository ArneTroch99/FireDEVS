package CellularAutomata.FF_Rothermel;

import javafx.scene.paint.Color;
import view.CAView.CAViewUI;

public class EnvironmentCellUI {
    public static void setPhaseColor(){
        CAViewUI.addPhaseColor("BURNED", Color.GRAY);
        CAViewUI.addPhaseColor("UNBURNED", Color.WHITE);
        CAViewUI.addPhaseColor("BURNING", Color.RED);
        CAViewUI.addPhaseColor("UNBURNABLE", Color.BLACK);
        CAViewUI.addPhaseColor("START", Color.GREEN);
    }
}
