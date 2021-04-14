/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.modeling;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import GenCol.Bag;
import GenCol.EntityInterface;
import GenCol.ensembleBag;
import GenCol.entity;
//Model Connections
import facade.simulation.FSimulator;
import model.modeling.TestInput;
import model.modeling.atomic;
import model.modeling.devs;
import model.modeling.port;
import model.modeling.ports;
import util.SortedArrayList;
import util.SortedEnumerableList;
import util.tracking.TrackingType;
/**
 * Interface of the Facade model
 * @author  Ranjit Singh 
 * @modified Sungung Kim
 */
public abstract class FModel
{
	public static final short ATOMIC    = 0;
    public static final short COUPLED   = 1;
    
    private devs model;
    private FModel parent;
    protected FSimulator fSimulator;
    protected SortedEnumerableList<String> inputPortNames;
    protected SortedEnumerableList<String> outputPortNames;
    protected SortedEnumerableList<String> stateNames;
    protected SortedEnumerableList<String> timeDimensionNames;
    protected HashMap<String, Field> states;
    
    public static final double INFINITY = Double.MAX_VALUE;
    
    public FModel(devs model, FModel parent)
    {
        this.model      = model;
        this.parent     = parent;
        
        this.inputPortNames  = SortedArrayList.MakeSortedStringArrayList(
            extractPortNames(model.getMessageHandler().getInports())
        );

        this.outputPortNames = SortedArrayList.MakeSortedStringArrayList(
            extractPortNames(model.getMessageHandler().getOutports())
        );
        
        this.states = new HashMap<String, Field>();

        this.stateNames = new SortedArrayList<String>((String lhs, String rhs) -> {
            if (lhs.equals("Phase"))
            {
                return -1;
            }
            else if (lhs.equals("Sigma"))
            {
                if (rhs.equals("Phase"))
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
            }
            else if (rhs.equals("Phase"))
            {
                return 1;
            }
            else if (rhs.equals("Sigma"))
            {
                return 1;
            }
            return SortedEnumerableList.DefaultStringComparator.compare(lhs, rhs);
        });

        stateNames.add("Phase");
        stateNames.add("Sigma");
        
        if (model instanceof atomic)
        {
            Set<String> states = ((atomic) model).getStateNames();
            this.stateNames.addAll(states);
        }
        
        this.timeDimensionNames = new SortedArrayList<String>((String lhs, String rhs) -> {
            if (lhs.equals("tL"))
            {
                return -1;
            }
            else if (rhs.equals("tL"))
            {
                return 1;
            }
            return lhs.compareTo(rhs);
        });
        timeDimensionNames.add("tL");
        timeDimensionNames.add("tN");
    }
    
    public void setSimulator(FSimulator simulator)
    {
        this.fSimulator = simulator;
    }
    
    public abstract void injectInput(String portName, EntityInterface input);
    public abstract List<Object> getOutputPortContents(String portName);
    public abstract List<Object> getInputPortContents(String portName);
    public abstract double getTimeOfLastEvent();
    public abstract double getTimeOfNextEvent();
    public devs getModel(){return model;}
    
    public String getName()
    {
        return model.getName();
    }
   
    public SortedEnumerableList<String> getInputPortNames()
    {
        return inputPortNames;
    }
    
    public SortedEnumerableList<String> getOutputPortNames()
    {
        return outputPortNames;
    }
    
    public SortedEnumerableList<String> getStateNames()
    {
        return this.stateNames;
    }
    
    public SortedEnumerableList<String> getTimeDimensionNames()
    {
        return this.timeDimensionNames;
    }
    
    public boolean isRootModel()
    {
        return getParent() == null;
    }
    
    public FModel getParent()
    {
        return parent;
    }
    
    public String toString()
    {
        return getName();
    }
    
    public Object getStateValue(String name)
    {
        return ((atomic) model).getStateValue(name);
    }
    
    public boolean isTaggedState(String name)
    {
        try
        {
            return ((atomic) model).isStateTagged(name);
        }
        catch (ClassCastException e)
        {
            return false;
        }
    }
    
    public boolean isStateDefaultTracked(String name, TrackingType setting)
    {
        try
        {
            return ((atomic) model).isStateDefaultTracked(name, setting);
        }
        catch (ClassCastException e)
        {
            return false;
        }
    }
    
    //returns list of entities // if nothing, empty list
    public List<entity> getInputPortTestValues(String portName)
    {
        if (inputPortNames.contains(portName))
        {
            List<entity> list = new Vector<entity>();
            List<TestInput> inputsForPort = model.getTestInputsForPort(portName);
            if (inputsForPort != null)
            {
                inputsForPort.forEach((TestInput input) -> {
                    list.add(input.getValue());
                });                    
            }
            return list;
        }
        else
            throw new FIllegalModelParameterException("Invalid Input Port: " + portName);
    }
  
    protected static List<String> extractPortNames(ports portSet)
    {
        List<String> names = new Vector<String>();

        portSet.forEach((port p) -> {
            names.add(p.getName());  
        });

        return names;
    }  
    
    protected static <T> List<T> extractEntities(ensembleBag<T> eBag)
    {
        return Bag.toVector(eBag);
    }
}
