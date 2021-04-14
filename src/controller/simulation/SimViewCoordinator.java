/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package controller.simulation;



import model.modeling.*;
import model.simulation.*;
import model.simulation.realTime.*;
import view.modeling.*;

/**
 * A tunable coordinator specialized to inter-operate with a SimView.
 * Then this coordinator is extended into the facade simulator
 */
public class SimViewCoordinator extends TunableCoordinator
{
    /**
     * Holds part of the implementation for this coordinator.
     */    
    protected SimViewCoordinatorBase base;
    protected DynamicStructureViewer dsViewer;

    /**
     * Constructor.
     *
     * See parent constructor for parameter descriptions.
     */
    public SimViewCoordinator(digraph digraph, Listener listener)
    {
        super(digraph, listener);
        if(listener instanceof DynamicStructureViewer)
        	   dsViewer = (DynamicStructureViewer)listener;
    }

    /**
     * See parent method.
     */
    protected void constructorHook1() {base = new SimViewCoordinatorBase();}

    /**
     * See parent method.
     */
    protected TunableCoupledCoordinator addCoordinatorHook1(digraph digraph)
    {
        return new SimViewCoupledCoordinator(digraph,
            (TunableCoupledCoordinator.Listener)listener);
    }

    /**
     * See parent method.
     */
    protected void addSimulatorHook1(coupledSimulator simulator)
    {
        base.setListenerIntoSimulator(listener, simulator);
    }

    /**
     * See parent method.
     */
    protected coupledSimulator createSimulatorHook1(IOBasicDevs devs)
    {
        return base.createSimulator(devs);
    }
    
    // for variable structure  Xiaolin Hu, Febrary 2, 2003
    public void addCoupling(String src, String p1, String dest, String p2){
      IODevs srcModel = myCoupled.withName(src);
      if(srcModel==null){
        if(src.compareTo(myCoupled.getName())==0) srcModel=(IODevs)myCoupled;
        else return;
      }
      IODevs destModel = myCoupled.withName(dest);
      if(destModel==null){
        if(dest.compareTo(myCoupled.getName())==0) destModel=(IODevs)myCoupled;
        else return;
      }
      super.addCoupling(src,p1,dest,p2);
      if(dsViewer!=null)  dsViewer.couplingAdded(srcModel,p1,destModel,p2);
    }

    public void removeCoupling(String src, String p1, String dest, String p2){
      IODevs srcModel = myCoupled.withName(src);
      if(srcModel==null){
        if(src.compareTo(myCoupled.getName())==0) srcModel=(IODevs)myCoupled;
        else return;
      }
      IODevs destModel = myCoupled.withName(dest);
      if(destModel==null){
        if(dest.compareTo(myCoupled.getName())==0) destModel=(IODevs)myCoupled;
        else return;
      }
      super.removeCoupling(src,p1,dest,p2);

      if(dsViewer!=null)  dsViewer.couplingRemoved(srcModel,p1,destModel,p2);


    }

public void setNewSimulator(IOBasicDevs iod){
 
	super.setNewSimulator(iod);
	if(dsViewer!=null)  
    	 dsViewer.modelAdded((ViewableComponent)iod,(ViewableDigraph)myCoupled);
}

public void removeModel(IODevs model){
  super.removeModel(model);
  if(dsViewer!=null)  
	  dsViewer.modelRemoved((ViewableComponent)model,(ViewableDigraph)myCoupled);
}

}