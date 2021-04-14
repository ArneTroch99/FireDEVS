/* 
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation;

public interface HierParent
{
    public void setParent(CoupledCoordinatorInterface p); // bpz

    public void setRootParent(CoordinatorInterface p);

    public CoupledCoordinatorInterface getParent();

    public CoordinatorInterface getRootParent();
}
