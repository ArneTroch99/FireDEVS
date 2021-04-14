/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.OddsAndEnds;


import java.lang.*;

import GenCol.*;


import model.modeling.*;
import model.simulation.*;

import view.modeling.ViewableAtomic;
import view.simView.*;


public class storageP extends ViewableAtomic{
protected double store;
protected double response_time;

  public storageP(){
    super("storageP");
addInport("in");
addInport("query");
addOutport("out");
addTestInput("in",new doubleEnt(1));
addTestInput("in",new doubleEnt(2));
addTestInput("query",new entity());
}

public storageP(String name,double Response_time){
   super(name);
   addInport("query");
   response_time = Response_time;
}

public void initialize(){
     phase = "passive";
     sigma = INFINITY;
     store = 0;
     response_time = 10;
     super.initialize();
 }


public void  deltext(double e,message x){
    Continue(e);
 if (phaseIs("passive"))
{
 for (int i=0; i< x.getLength();i++)
  if (messageOnPort(x,"in",i))
      {
      entity val = x.getValOnPort("in",i);
      doubleEnt f = (doubleEnt)val;
      store = f.getv();
      }
 for (int i=0; i< x.getLength();i++)
  if (messageOnPort(x,"query",i))
        holdIn("respond", response_time);
}
}

public void  deltint( ){
    passivate();
}


public message    out( )
{
message   m = new message();
if (phaseIs("respond")){
content   con = makeContent("out",new doubleEnt(store));
m.add(con);
}
return m;
}

 public void showState(){
  super.showState();
  System.out.println("store: " + store);
 }



}

