/*  
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 
/**
 * Utility methods involving (or used by) atomic simulators.
 *
 * @author  Jeff Mather
 */

package model.simulation;

import java.util.*;

import GenCol.*;


import model.modeling.*;


public class AtomicSimulatorUtil
{
    /**
     * Returns a list of the ports (along with their components) to which
     * the given port is a source.
     *
     * @param   portName            The source port name on the source component.
     * @param   sourceComponentName
     *                              The source component name.
     * @param   internalCouplings   The source component's internal couplings
     *                              collection.
     * @param   externalCouplings   The source component's external couplings
     *                              collection (if it has one).
     * @param   modelToSimMap       The source component's mapping of
     *                              (names of) components to their simulators.
     * @param   internalModelToSimMap
     *                              The source component's mapping of
     *                              (names of) internal components to their
     *                              simulators (if it has one).
     * @param   rootSim             The root simulator (or coordinator) of the
     *                              simulator making this call (if any).
     * @return                      The list of couplings to the source port's
     *                              destination ports.
     */
    static public List<Object> getCouplingsToSourcePort(String portName,
        String sourceComponentName, couprel internalCouplings,
        couprel externalCouplings, Function modelToSimMap,
        Function internalModelToSimMap, atomicSimulator rootSim)
    {
        // ask for all the couplings to the source port in the
        // given internal couplings set (note: I would have thought that
        // internalCouplings would have matched up with internalModelToSimMap
        // (and externalCouplings with modelToSimMap) in the calls below,
        // but apparently the reverse is true, and I'm not sure why)
        Set couplings = internalCouplings.translate(
            sourceComponentName, portName);
        List<Object> resolvedCouplings = resolveCouplings(couplings, modelToSimMap,
            rootSim);

        // if an external couplings set was given
        if (externalCouplings != null) {
            // ask for all the couplings to the source port in the
            // given external couplings set
            couplings = externalCouplings.translate(
                sourceComponentName, portName);
            resolvedCouplings.addAll(resolveCouplings(couplings,
                internalModelToSimMap, rootSim));
        }

        return resolvedCouplings;
    }

    /**
     * Resolves the component name in each of the couplings in the given set
     * to its devs component, and returns a new list of resolved such couplings.
     *
     * @param   couplings   The set of couplings, each containing an unresolved
     *                      component name.
     * @param   modelToSimMap       A mapping of (names of) components to
     *                              their simulators.
     * @param   rootSim             The root simulator (or coordinator) of the
     *                              simulator making this call (if any).
     * @return              A new list of couplings, corresponding to the given
     *                      one, except each coupling contains the actual
     *                      destination component, rather than just that
     *                      component's name.
     */
    static protected List<Object> resolveCouplings(Set couplings,
        Function modelToSimMap, atomicSimulator rootSim)
    {
        // for each of the couplings to the source port
        List<Object> resolvedCouplings = new ArrayList();
        Iterator i = couplings.iterator();
        while (i.hasNext()) {
            Pair coupling = (Pair)i.next();

            // resolve the destination component name to its actual component
            EntityInterface component = getComponentWithName(
                (String)coupling.getKey(), modelToSimMap, rootSim);

            // package the destination component with the destination
            // port-name, and add the result to our list of resolved
            // couplings
            resolvedCouplings.add(new Pair(component, coupling.getValue()));
        }

        return resolvedCouplings;
    }

    /**
     * Queries the given sources to find the devs component with the given name.
     *
     * @param   name                The name of the component to find.
     * @param   modelToSimMap       A mapping of (names of) components to
     *                              their simulators, which is needed
     *                              because the desired component will be
     *                              obtained via its simulator.
     * @param   rootSim             The root simulator (or coordinator) of the
     *                              simulator making this call (if any), which
     *                              will be consulted if the above mapping
     *                              fails to produce a value.
     * @return                      The devs component with the given name.
     */
    static public EntityInterface getComponentWithName(String name,
        Function modelToSimMap, atomicSimulator rootSim)
    {
        // map the component name to its simulator
        atomicSimulator simulator = null;
        if (modelToSimMap != null) {
            simulator = (atomicSimulator)modelToSimMap.get(name);
        }

        // if the mapping produced no result, but we were given a root simulator
        if (simulator == null && rootSim != null) {
            // use the given root simulator as the component's simulator
            simulator = rootSim;
        }

        // else if we don't have the simulator for the component of the
        // given name, we can't find the component itself, so quit
        else if (simulator == null) return null;

        // get the actual component from the simulator
        EntityInterface model = simulator.getModel();
        if (simulator instanceof coordinator) {
            model = ((coordinator)simulator).getCoupled();
        }

        return model;
    }
}