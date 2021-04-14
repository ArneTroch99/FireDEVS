/* 
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation;

import GenCol.EntityInterface;
import model.modeling.ActivityInterface;

public interface CoupledSimulatorInterface extends
            coreSimulatorInterface
            ,CouplingProtocolInterface
            ,HierParent
{
    public void startActivity(ActivityInterface a);
    public void returnResultFromActivity(EntityInterface  result);
}
