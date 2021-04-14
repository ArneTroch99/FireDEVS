/* Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package model.modeling;

import java.util.Set;

import GenCol.Pair;
import GenCol.Relation;
import GenCol.entity;

public class couprel extends Relation<Object, Object>
{
    public synchronized Object add(Object key, Object value)
    {
        return super.put(key, value);
    }

    public synchronized void add(entity c1, port p1, entity c2, port p2)
    {
        Pair<String, String> coup1 = new Pair<String, String>(c1.getName(), p1.getName());
        Pair<String, String> coup2 = new Pair<String, String>(c2.getName(), p2.getName());
        this.add(coup1, coup2);
    }

    public synchronized void remove(entity c1, port p1, entity c2, port p2)
    {
        Pair<String, String> coup1 = new Pair<String, String>(c1.getName(), p1.getName());
        Pair<String, String> coup2 = new Pair<String, String>(c2.getName(), p2.getName());
        super.remove(coup1, coup2);
    }

    public synchronized Set<Object> translate(String srcName, String ptName)
    {
        Pair<String, String> cp = new Pair<String, String>(srcName, ptName);
        return super.getSet(cp);
    }

    public synchronized Set<Object> assocPair(Pair<Object, Object> cpr)
    {
        return super.getSet(cpr);
    }
}