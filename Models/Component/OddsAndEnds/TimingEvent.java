/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.OddsAndEnds;

import Component.BasicProcessor.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.simView.*;


public class TimingEvent extends proc {
  protected boolean bIn1;
  protected boolean bIn2;

public TimingEvent(String name, double ProcessingTime){
  super(name, ProcessingTime);
  addInport("in1");
  addInport("in2");
  addOutport("out");
}

public TimingEvent(){
  super("TimingEvent", 10);
  addTestInput("in1", new entity("job1"));
  addTestInput("in2", new entity("job2"));
  addTestInput("in1", new entity("job3"), 5);
  addTestInput("in1", new entity("job3"), 6);
  addTestInput("in2", new entity("job4"), 5);

  initialize( );
}

/*initialization*/
public void initialize( ){
  bIn1 = false;
  bIn2 = false;
  super.initialize( );
}

/*External Transition Function*/
public void  deltext(double e, message x){

  Continue(e);
  if (phaseIs("passive")){
    for (int i=0; i<x.size(); i++){
      if (messageOnPort(x, "in1",i))
        bIn1 = true;
      if (messageOnPort(x, "in2", i))
        bIn2 = true;
    }
    holdIn("active", processing_time);
  }
  else
  {
    if (e == 0)
    {
      for (int i=0; i<x.size(); i++){
        if (messageOnPort(x, "in1",i))
          bIn1 = true;
        if (messageOnPort(x, "in2", i))
          bIn2 = true;
      }
    }
  }
}

/*Internal Transition Function*/
public void  deltint( ){
  bIn1 = bIn2 = false;
  passivate( );
}

/*Output Function*/
public message out( ){

  String str;

  if ( bIn1 & bIn2 ) str = "T";
  else  str = "F";

  message m = new message( );

  if (phaseIs("active")){
      m.add(makeContent("out", new entity(str)));
  }

  return m;
}
}