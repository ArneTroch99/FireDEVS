/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.MultiProcessors;

import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.simView.*;


public class procName extends proc {//ViewableAtomic{//

public procName(){
super("procName",20);
//addInport("none");
addInport("inName");
addOutport("outName");
addTestInput("inName",new Pair(new procName("procName",20),
                 new entity("job1")));
addTestInput("inName",new Pair(new procName("other",20),
                 new entity("job2")));
addTestInput("none",new entity("job"));
}


public procName(String  name,double  Processing_time){
super(name, Processing_time);
//addInport("none");
addInport("inName");
addOutport("outName");
processing_time = Processing_time;
}

public void initialize(){
job = new job("nullJob");
super.initialize();
 }



public void  deltext(double e,message   x)
{

    Continue(e);
if (phaseIs("passive"))
{
  for (int i=0; i< x.size();i++)
	 
    if (messageOnPort(x,"inName",i))
     {
    entity   ent =
     x.getValOnPort("inName",i);
    Pair   pr = (Pair)ent;
    entity en = (entity)pr.getKey();
    procName   pn = (procName  )en;

   if (this.equals(pn))
      {
      job = (entity)pr.getValue();
      holdIn("busy",processing_time);
      }
    }
}
}

public message    out( )
{
message   m = new message();
if (phaseIs("busy")){
content con = makeContent("outName",new Pair(this,job));
m.add(con);
}
return m;
}

}

