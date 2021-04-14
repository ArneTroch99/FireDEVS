/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package GenCol;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

@SuppressWarnings("serial")
public class Queue<T> extends LinkedList<T>
{
    public T remove()
    {
        if (size() > 0)
        {
            return remove(0);
        }
        return null;
    }

    public T first()
    {
        return get(0);
    }

    static public <T> Queue<T> set2Queue(Set<T> s)
    {
        Queue<T> q = new Queue<T>();

        q.addAll(s);

        return q;
    }

    public Bag<T> Queue2Bag()
    {
        Bag<T> b = new Bag<T>();
        Iterator<T> it = iterator();
        while (it.hasNext())
        {
            b.add(it.next());
        }
        return b;
    }
}
