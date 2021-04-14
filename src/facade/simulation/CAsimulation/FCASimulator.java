package facade.simulation.CAsimulation;

import facade.modeling.FModel;
import facade.simulation.FCoupledSimulator;
import model.modeling.CAModels.TwoDimCellSpace;
import model.simulation.realTime.TunableCoordinator.Listener;

public class FCASimulator extends FCoupledSimulator {

	public FCASimulator(TwoDimCellSpace model, FModel rootModel, Listener listener, short modelType) {
		super(model, rootModel, listener, modelType);

	}
}
