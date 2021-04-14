/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.simulation;

//Intra-Facade Connections
import facade.modeling.FModel;
import facade.simulation.hooks.SimulatorHookListener;

/**
 * FSimulator: provides the interface of facade simulator
 * @author  Ranjit Singh 
 * @modified Sungung Kim 05/29/2008
 */
public interface FSimulator
{   
    public static final short STATE_INITIAL    = 0;
    public static final short STATE_SIMULATING = 1;
    public static final short STATE_PAUSE      = 2;
    public static final short STATE_END        = 3;
    
    public void run();
    public void step();
    public void step(int n);
    public void requestPause();
    public void reset();
    public void setRTMultiplier(double factor);
    public short getCurrentState();
    //public void end();
    
    public double getRTMultiplier();
    public double getTimeOfLastEvent();
    public double getTimeOfNextEvent();
    
    public FModel getRootModel();
    
    //Example of how hooks work
    //Any specialized hooks? (For atomic or coupled only?)
    public void setSimulatorHookListener(SimulatorHookListener listener);
}
