/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.BasicProcessor;


import java.util.Date;

import GenCol.*;


import model.modeling.*;
import model.simulation.*;

import view.simView.*;


public class procQ extends proc{
  protected Queue q;


public procQ(String  name,double  Processing_time){
super(name,Processing_time);
q = new Queue();
}

public procQ(){
super("procQ",20);
addTestInput("in",new entity("job1"));
addTestInput("in",new entity("job2"),20);
addTestInput("in",new entity("job2"),10);
addTestInput("none",new entity("job"));
initialize();
}

public void initialize(){
q = new Queue();
super.initialize();

 }


 public void  deltext(double e, message   x){
  Continue(e);
  if (phaseIs("passive")){
    for (int i=0; i< x.size(); i++)
      if (messageOnPort(x, "in", i)){
        job = x.getValOnPort("in", i);
        holdIn("busy", processing_time);
        q.add(job);
      }

    job = (entity)q.first();  // this makes sure the processed job is the one at
                  //the front
     }

   else if (phaseIs("busy")){
   for (int i=0; i< x.size();i++)
      if (messageOnPort(x, "in", i))
      {
      entity jb = x.getValOnPort("in", i);
      q.add(jb);
      }
   }
}

public void  deltint( ){
   // take some time
   try {
     System.out.println("enter time: " + (new Date()));
     Thread.sleep(2000);
   }
   catch (Exception e){

   }
q.remove();

if(!q.isEmpty()){
  job = (entity)q.first();
   holdIn("busy", processing_time);
}
else passivate();

System.out.println("exist time: " + (new Date()));
}

public void deltcon(double e,message x)
{
  deltint();
  deltext(0,x);
}

/*Output Function*/
public message out( ){
  message m = new message( );

  if ((phaseIs("busy")) | (phaseIs("busy"))){
      System.out.println("out: -->" + job);
      m.add(makeContent("out", job));
  }

  return m;
}
 public void showState(){
    super.showState();
    System.out.println("Queue length: " + q.size() + q.toString());
 }

 public String getTooltipText(){
   return
   super.getTooltipText()
   +"\n"+"queue length: " + q.size()
    +"\n"+"queue itself: " + q.toString();
  }
}


