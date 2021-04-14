/*
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;

import GenCol.ensembleSet;

@SuppressWarnings("serial")
public class Components extends ensembleSet<IODevs> implements ComponentsInterface<IODevs>
{
    @Override
    public componentIterator cIterator()
    {
        return new componentIterator(this);
    }

    @Override
    public boolean containsName(String name)
    {
        return this.findIf((IODevs c) -> {
            if (c.getName().equals(name))
            {
                return true;
            }
            return false;
        }) != null;
    }
}