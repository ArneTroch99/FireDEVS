package facade.modeling.CAmodeling;

import java.util.Optional;

import facade.modeling.FCoupledModel;
import facade.modeling.FModel;
import model.modeling.IODevs;
import model.modeling.digraph;
import model.modeling.CAModels.TwoDimCell;
import model.modeling.CAModels.TwoDimCellSpace;
import util.SortedEnumerableList;

public class FCASpaceModel extends FCoupledModel
{
    public FCASpaceModel(TwoDimCellSpace model)
    {
        this(model, null);
    }

    public FCASpaceModel(TwoDimCellSpace model, FModel parent)
    {
        super(model, parent);
    }

    @Override
    public TwoDimCellSpace getModel()
    {
        return (TwoDimCellSpace) super.getModel();
    }

    public int getNumCells()
    {
        return getModel().numCells;
    }

    public int getXDimSize()
    {
        return getModel().xDimCellspace;
    }

    public int getYDimSize()
    {
        return getModel().yDimCellspace;
    }

    public TwoDimCell getCell(int x, int y)
    {
        return (TwoDimCell) getModel().withId(x, y);
    }

    @Override
    protected SortedEnumerableList<FModel> createChildModels(
        digraph model,
        FModel fModel
    )
    {
        return createChildModels(model, fModel, (IODevs c) -> {
            if (c instanceof TwoDimCell)
            {
                return Optional.of(
                    new FCACellModel((TwoDimCell) c, fModel)
                );
            }
            else if (c instanceof TwoDimCellSpace)
            {
                return Optional.of(
                    new FCASpaceModel((TwoDimCellSpace) c, fModel)
                );
            }
            return Optional.empty();
        });
    }
}
