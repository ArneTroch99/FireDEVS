/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.modeling;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import GenCol.EntityInterface;
//Model Connections
import facade.simulation.FIllegalSimulatorStateException;
import facade.simulation.FSimulator;
import model.modeling.IODevs;
import model.modeling.digraph;
import model.simulation.CoupledSimulatorInterface;
import model.simulation.atomicSimulator;
import util.SortedArrayList;
import util.SortedEnumerableList;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;

/**
 * FCoupled Model which contains a ViewableDigraph model for the SimView
 * @author  Ranjit Singh 
 * @modified by Sungung Kim 05/29/2008
 */
public class FCoupledModel extends FModel
{
    private ViewableDigraph model;
    private SortedEnumerableList<FModel> childComponents;
    
    public FCoupledModel(ViewableDigraph model) 
    {
        this(model,null);
    }
    
    public FCoupledModel(ViewableDigraph model, FModel parent) 
    {
        super(model,parent);
        this.model = model;
        this.childComponents = createChildModels(model, parent);
    }
    
    public void setSimulator(FSimulator simulator)
    {
        childComponents.forEach((FModel m) -> {
           m.setSimulator(simulator); 
        });
    }
    
    public ViewableDigraph getModel(){
    	return model;
    }    
    
    public SortedEnumerableList<FModel> getChildren()
    {
        return childComponents;
    }
    
    public double getTimeOfNextEvent() 
    {
        return((atomicSimulator)model.getCoordinator()).getTN();
    }
    
    public double getTimeOfLastEvent() 
    {
        return((atomicSimulator)model.getCoordinator()).getTL();
    }
    
    @Override
    public List<Object> getOutputPortContents(String portName) 
    {
         if (outputPortNames.contains(portName))
            return extractEntities(model.getCoordinator()
                                   .getOutputForTimeView().valuesOnPort(portName));
        else
            throw new FIllegalModelParameterException("Invalid Output Port: " + portName);
    }
    
    @Override
    public List<Object> getInputPortContents(String portName) 
    {
        if (inputPortNames.contains(portName))
            return extractEntities(model.getCoordinator()
                                   .getInputForTimeView().valuesOnPort(portName));
        else
            throw new FIllegalModelParameterException("Invalid Input Port: " + portName);
    }
    
    @Override
    public void injectInput(String portName, EntityInterface input) 
    {
        if (inputPortNames.contains(portName))
        {
            short currentState = fSimulator.getCurrentState();
            if (currentState == FSimulator.STATE_INITIAL)
            {
                if (isRootModel())
                {
                    model.getCoordinator().simInject(0,portName,input);
                }
                else
                    throw new FModelException("Can only [Inject Input] from the Root " 
                                               + "Coupled Model.");
            }
            else
                throw new FIllegalSimulatorStateException("Can only [Inject Input] from state:"
                                                          + "{Initial}.");
        }
        else
            throw new FIllegalModelParameterException("Invalid Input Port: " + portName);
    }
    
    protected SortedEnumerableList<FModel> createChildModels(digraph model, FModel fModel)
    {
        return createChildModels(model, fModel, (IODevs c) -> {
            if (c instanceof ViewableAtomic)
            {
                return Optional.of(new FAtomicModel((ViewableAtomic)c, fModel));
            }
            else if (c instanceof ViewableDigraph)
            {
                return Optional.of(new FCoupledModel((ViewableDigraph)c, fModel));
            }
            return Optional.empty();
        });
    }
    
    protected final SortedEnumerableList<FModel> createChildModels(digraph model, FModel fModel, Function<IODevs, Optional<FModel>> FModelFactory)
    {
        SortedEnumerableList<FModel> childModels = new SortedArrayList<FModel>((FModel lhs, FModel rhs) -> {
            return SortedEnumerableList.DefaultStringComparator.compare(lhs.toString(), rhs.toString());
        });

        model.getComponents().forEach((IODevs c) -> {
            Optional<FModel> child = FModelFactory.apply(c);
            if (child.isPresent())
            {
                childModels.add(child.get());
            }
            else
            {
                throw new FModelException("Unknown Model Type: " + c.getName());
            }
        });
        
        return childModels;
    }
}