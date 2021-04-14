package CellularAutomata.CarMoving2DAsync;

import javafx.scene.paint.Color;
import view.CAView.CAViewUI;

public class AgentUI {
    public static void setPhaseColor() {
        CAViewUI.addPhaseColor("EMPTY", Color.WHITE);

        CAViewUI.addPhaseColor("Agent: Car1", Color.GREEN);
        CAViewUI.addPhaseColor("Agent: Car2", Color.BLUE);
        CAViewUI.addPhaseColor("Agent: Car3", Color.RED);


        // intermediate phase
        CAViewUI.addPhaseColor("COMING:-", Color.LIGHTGREY);
        CAViewUI.addPhaseColor("Car1:-", Color.MEDIUMSPRINGGREEN);
        CAViewUI.addPhaseColor("Car2:-", Color.CORNFLOWERBLUE);
        CAViewUI.addPhaseColor("Car3:-", Color.PALEVIOLETRED);

    }

}