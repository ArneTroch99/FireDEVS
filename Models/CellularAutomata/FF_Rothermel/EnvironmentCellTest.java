package CellularAutomata.FF_Rothermel;

import model.modeling.CAModels.TwoDimCellSpace;

public class EnvironmentCellTest extends TwoDimCellSpace {

    public EnvironmentCellTest() {
        this(10, 10);
    }

    public EnvironmentCellTest(int xDim, int yDim) {
        super("moor", xDim, yDim);
        this.numCells = xDim * yDim;

        FuelModel[][] fuelModels = ImageInterpreter.readImage(xDim, yDim);
        CellUtils.init(2, 2, 100);
        ROS_Calculator rosCalculator = new ROS_Calculator();

        for (int i = 0; i < xDimCellspace; i++) {
            for (int j = 0; j < yDimCellspace; j++) {
                EnvironmentCell cell = new EnvironmentCell(i, j);
                addCell(cell);
                if (i == xDim / 2 && j == yDim / 2) {
                    cell.setStartFire(true);
                }
                cell.setFuelmodel(fuelModels[i][j]);
                cell.setRosCalculator(rosCalculator);
            }
        }
        doNeighborToNeighborCoupling();
    }

    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    @Override
    public void layoutForSimView() {
    }
}
