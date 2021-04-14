/* Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;

import GenCol.EntityInterface;

public interface ContentInterface extends EntityInterface
{
    public PortInterface getPort();

    public String getPortName();

    public Object getValue();

    public boolean onPort(PortInterface port);
}
