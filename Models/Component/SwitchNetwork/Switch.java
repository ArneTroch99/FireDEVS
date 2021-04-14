/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.SwitchNetwork;


import java.lang.*;

import Component.BasicProcessor.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.modeling.ViewableAtomic;
import view.simView.*;


public class Switch extends  ViewableAtomic{//switch is a reserved work
    protected entity job;
    protected double processing_time;
    protected boolean sw;
    protected String input;

//public Switch(){
//    this("Switch",10);
//}

public Switch(String  name,double  Processing_time){//switch is reserved word
super(name);
addInport("in");
addOutport("out");
addInport("in1");
addOutport("out1");
processing_time = Processing_time;
addTestInput("in",new entity("packet"));
addTestInput("in1",new entity("packet"));
}

public void initialize(){
     phase = "passive";
     sigma = INFINITY;
     job = new entity("job");
     sw = false;
     input = new String("in");
     super.initialize();
 }




public void  deltext(double e,message x)
{
    Continue(e);

if (phaseIs("passive"))
{

 for (int i=0; i< x.getLength();i++)
  if (messageOnPort(x,"in",i))
      {
      job = x.getValOnPort("in",i);
      input = "in";
      holdIn("busy",processing_time);
      }
 for (int i=0; i< x.getLength();i++)
  if (messageOnPort(x,"in1",i))
      {
      job = x.getValOnPort("in1",i);
      input = "in1";
      holdIn("busy",processing_time);
      }
  }
       sw = !sw;
}

public void  deltint( )
{
passivate();
}

public message    out( )
{
message   m = new message();
if (phaseIs("busy")){
    content   con;
 if (!sw && input.equals("in"))
      con = makeContent("out",job);
 else if (!sw && input.equals("in1"))
     con = makeContent("out1",job);
 else if (sw && input.equals("in"))
      con = makeContent("out1",job);
 else //if (sw && input.equals("in1"))
      con = makeContent("out",job);
 m.add(con);
}
return m;
}

 public void showState(){
   super.showState();
   System.out.println("job: " + job.getName());
   System.out.println("input: " + input);
   System.out.println("sw: " + sw);
 }

}

