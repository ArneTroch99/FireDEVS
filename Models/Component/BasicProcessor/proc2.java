/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.BasicProcessor;


import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.modeling.ViewableAtomic;
import view.simView.*;

public class proc2 extends ViewableAtomic{//ViewableAtomic is used instead
                                    //of atomic due to its
                                    //graphics capability
    protected entity job;
    protected double processing_time;

public proc2(){
this("proc2",0);
}

public proc2(String  name,double  Processing_time){
super(name);
addInport("in");
addOutport("out");
addInport("none"); //allows testing for null input
                     //which should cause only "continue"
processing_time = Processing_time;
addTestInput("in",new entity("job1"));
addTestInput("in",new entity("job2"),20);
addTestInput("none",new entity("job"));
}

public void initialize(){
     phase = "passive";
     sigma = INFINITY;
     job = new entity("job");
     super.initialize();
 }

public void  deltext(double e,message   x)
{
Continue(e);

if (phaseIs("passive"))
 for (int i=0; i< x.getLength();i++) 
  if (messageOnPort(x,"in",i))
      {
      job = x.getValOnPort("in",i);
      if(job.getName().equals("job0"))
    	  holdIn("busy",processing_time);
      }
}

public void  deltint( )
{
passivate();
job = new entity("none");
}

public void deltcon(double e,message x)
{
   deltint();
   deltext(0,x);
}

public message    out( )
{
message   m = new message();
if (phaseIs("busy")) {
m.add(makeContent("out",job));
}
return m;
}

  public void showState(){
  super.showState();
  //System.out.println("job: " + job.getName());
 }

 public String getTooltipText(){
   return
   super.getTooltipText()
    +"\n"+"job: " + job.getName();
  }
}
