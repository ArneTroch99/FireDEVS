/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.simulation;

/**
 * FCoupled Coordinator: this class does not contain real code, it is for hooking purpose
 * @modified Sungung Kim 05/29/2008
 */

import model.modeling.coupledDevs;
import model.modeling.digraph;
import model.simulation.realTime.TunableCoupledCoordinator;
import model.simulation.realTime.TunableCoupledCoordinator.Listener;
import view.modeling.ViewableDigraph;
import view.simView.*;
import facade.modeling.FCoupledModel;
import facade.modeling.FModel;
import facade.simulation.hooks.SimulatorHookListener;

public class FCoupledCoordinator implements FSimulator
{
    private FCoupledCoordinator.FCoupledCoordX simulator;
    private FModel rootModel;
    private short currentState;
    private short modelType;
    private SimulatorHookListener Flistener;
    
    public FCoupledCoordinator(ViewableDigraph model, Listener listener, short modelType)
    {
        simulator = new FCoupledCoordinator.FCoupledCoordX(model, listener);
        rootModel = new FCoupledModel(model);
        this.modelType = modelType;
    }
      
    public void step()                          {}
    public void step(int n)                     {}
    public void run()                           {}
    public void requestPause()                  {}  
    public void reset()                         {}
    public void setRTMultiplier(double factor)  {}
    public double getRTMultiplier()             {return 0;}
    public double getTimeOfLastEvent()          {return 0;}
    public double getTimeOfNextEvent()          {return 0;}
    public short  getCurrentState()             {return currentState;}
    public FModel getRootModel()                {return rootModel;}
    
    public void setSimulatorHookListener(SimulatorHookListener Flistener) 
    {
            this.Flistener = Flistener;
    }
    
    public void setCurrentState(short newState)
    {
        currentState = newState;
        if (Flistener != null)
            Flistener.simulatorStateChangeHook();
    }
    
    //----------------------------------------------------------------------
    public class FCoupledCoordX extends TunableCoupledCoordinator 
    {
        
               
        public FCoupledCoordX(digraph coupledModel, Listener listener)
        {
            super(coupledModel, listener);
            
            setCurrentState(STATE_INITIAL);
        }
       
    }
}