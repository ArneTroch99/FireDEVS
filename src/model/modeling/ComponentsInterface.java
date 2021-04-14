/* Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;

import GenCol.ensembleInterface;

public interface ComponentsInterface<T> extends ensembleInterface<T>
{
    public componentIterator cIterator();
    public boolean containsName(String name);
}