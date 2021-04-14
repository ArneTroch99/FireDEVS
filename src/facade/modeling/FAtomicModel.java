/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.modeling;

//Intra-Facade Connections

//Collection Connections

//Standard API Imports
import java.util.List;

import GenCol.EntityInterface;
//Model Connections
import facade.simulation.FIllegalSimulatorStateException;
import facade.simulation.FSimulator;
import model.simulation.atomicSimulator;
import view.modeling.ViewableAtomic;


/**
 * FAtomice Model which contains a ViewableAtomice model for SimView
 * @author  Ranjit Singh
 * @modified by Sungung Kim 05/29/2008
 */
public class FAtomicModel extends FModel
{
    private ViewableAtomic model;
    
    /** Constructs this AtomicModel wrapper for a specific
     * atomic model.
     */    
    public FAtomicModel(ViewableAtomic model) 
    {
        this(model, null);
    }
    
    public FAtomicModel(ViewableAtomic model, FModel parent) 
    {
        super(model, parent);
        this.model = model;
    }
    
    public ViewableAtomic getModel(){
    	return model;
    }  
    
    public double getSigma()
    {
        return model.getSigma();
    }
    
    public void setSigma(double sigma)
    {
        model.setSigma(sigma);
    }
    
    public String getPhase()
    {
        return model.getPhase();
    }
    
    public double getTimeOfNextEvent() 
    {
        return ((atomicSimulator)model.getSimulator()).getTN();
    }
    
    public double getTimeOfLastEvent() 
    {
        return ((atomicSimulator)model.getSimulator()).getTL();
    }
    
    public Object getStateValue(String name)
    {
        return model.getStateValue(name);
    }
    
    //returns list of entities
    @Override
    public List<Object> getInputPortContents(String portName) 
    {
        if (inputPortNames.contains(portName))
            return extractEntities(((atomicSimulator)model.getSimulator())
                                     .getInputForTimeView().valuesOnPort(portName));
        else
            throw new FIllegalModelParameterException("Invalid Input Port: " + portName);
    }
    
    //returns list of entities
    @Override
    public List<Object> getOutputPortContents(String portName) 
    {     
        if (outputPortNames.contains(portName))
            return FModel.extractEntities(
                    ((atomicSimulator)model.getSimulator())
                                     .getOutputForTimeView().valuesOnPort(portName));
        else
            throw new FIllegalModelParameterException("Invalid Output Port: " + portName);
    }

    @Override
    public void injectInput(String portName, EntityInterface input) 
    {
        if (inputPortNames.contains(portName))
        {
            short currentState = fSimulator.getCurrentState();
           
            if (currentState == FSimulator.STATE_INITIAL || 
            	currentState == FSimulator.STATE_PAUSE   ||
            	currentState == FSimulator.STATE_END)
            {
                ((atomicSimulator)model.getSimulator()).simInject(0,portName,input);
            }
            else
                throw new FIllegalSimulatorStateException("Can only [Inject Input] from state:"
                                                          + "{Initial}.");
        }
        else
            throw new FIllegalModelParameterException("Invalid Input Port: " + portName);
    }

}
