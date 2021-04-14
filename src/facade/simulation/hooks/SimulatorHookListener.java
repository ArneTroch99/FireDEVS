/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package facade.simulation.hooks;

/**
 * Simulator hook listener: this send the data to the TimeView
 * @author  Ranjit Singh
 */
public interface SimulatorHookListener 
{
    public void postComputeInputOutputHook();
//    public void CAPostComputeInputOutputHook();
    public void simulatorStateChangeHook();
}