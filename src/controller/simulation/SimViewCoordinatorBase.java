/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package controller.simulation;

import model.modeling.*;
import model.simulation.*;
import view.modeling.*;

/**
 * Houses implementation common to SimViewCoordinator and 
 * SimViewCoupledCoordinator.
 */
public class SimViewCoordinatorBase
{
    /**
     * Tries to attach the given listener to the given simulator.
     *
     * @param   listener    The listener to attach.
     * @param   simulator   The simulator to which to attach the listener.
     */
    protected void setListenerIntoSimulator(Object listener,
        coupledSimulator simulator)
    {
        if (listener == null) return;

        // if the created simulator is a viewable-atomic-simulator
        if (simulator instanceof ViewableAtomicSimulator) {
            // if the listener is also suitable for listening to the
            // created simulator
            if (listener instanceof ViewableAtomicSimulator.Listener) {
                // pass the listener to the simulator
                ((ViewableAtomicSimulator)simulator).setListener(
                    (ViewableAtomicSimulator.Listener)listener);
            }

            // if the listener is also a time-scale-keeper
            if (listener instanceof ViewableAtomicSimulator.TimeScaleKeeper) {
                // pass the listener to the simulator
                ((ViewableAtomicSimulator)simulator).setTimeScaleKeeper(
                    (ViewableAtomicSimulator.TimeScaleKeeper)listener);
            }
        }
    }

    /**
     * Creates a simulator for the given devs component.
     *
     * @param   devs    The component for which to create a simulator.
     */    
    protected coupledSimulator createSimulator(IOBasicDevs devs)
    {
        // if the given component is a viewable-atomic
        if (devs instanceof ViewableAtomic) {
            // create a viewable-atomic-simulator for it
            return new ViewableAtomicSimulator((ViewableAtomic)devs);
        }

        return null;
    }
}