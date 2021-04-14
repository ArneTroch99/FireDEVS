/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

/*
/*  corresponds to Hashtable except that
/*  iteration is through Pairs rather than entries
*/

package GenCol;


import java.util.*;


public class Function extends Hashtable implements FunctionInterface{

public Function(){
super();
}

//inherit contains(Object), containsKey(Object), containsValue(Object)

public boolean contains(Object key, Object value){
if (!containsKey(key))return false;
else return  get(key).equals(value);
}
             /*
public Set keySet(){
return keySet();
}              */


public Set valueSet(){    //values() is a general Collection
Iterator it = iterator();
HashSet hs = new HashSet();
while (it.hasNext())
  hs.add(((Pair)it.next()).getValue());
return hs;
}

//public void replace(Object key, Object value)
public Object replace(Object key, Object value) //Java 1.8, March 31
{

   FunctionIterator itr = (FunctionIterator)iterator();
   while(itr.hasNext())
       {
   Pair p = (Pair)itr.next();
   if(p.getKey().equals(key)) {
     this.remove(key);
     break;
     }
   }

   return put(key, value);

}                                     /*
public void replace(String name, entity value)
{

   FunctionIterator itr = (FunctionIterator)iterator();
   while(itr.hasNext())
       {
   Pair p = itr.Next();
   if(p.getKey().equals(name)) {
     this.remove(name);
     break;
     }
   }

   put(name, value);

}                                       */

   /*
protected Pair nextKeyName(String name)
{
FunctionIterator itr = (FunctionIterator)iterator();
    while(itr.hasNext())
      {
        Pair p = itr.Next();
        if(p.getKey().equals(name))
        return(p);
      }
      return null;

}     */

public Object assoc(String name){
return get(name);

/*Pair pr = nextKeyName(name);
   if ( pr != null )
      return (pr.getVal());
   else
      return null;*/
}

public Iterator iterator(){
return new FunctionIterator(this);

}

public void print(){
System.out.println(toString());
}
}

