/* 
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package model.modeling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("serial")
public class ports extends HashSet<port> implements PortsInterface
{
    @Override
    public List<String> getNameList()
    {
        List<String> names = new ArrayList<String>(super.size());
        super.forEach((port p) -> {
            names.add(p.getName());
        });
        
        return names;
    }
}