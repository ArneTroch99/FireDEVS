/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package Component.SimTrip;



import java.lang.*;

import Component.BasicProcessor.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.modeling.ViewableAtomic;
import view.simView.*;


public class simulator extends ViewableAtomic{


  protected double tN,tL;
  protected ViewableAtomic myModel;
  protected message output;


public simulator(String name,ViewableAtomic model){
   super(name);
    myModel= model;
   addInport("applyDelt");
   addInport("nextTN");
   addInport("getOut");
   addOutport("outTN");
   addOutport("sendOut");

addTestInput("nextTN",new entity(""));
addTestInput("applyDelt",new Pair(new doubleEnt(0),new message()));
addTestInput("getOut",new doubleEnt(3000));
addTestInput("applyDelt",new Pair(new doubleEnt(3000),
                         new message()));

message m = new message();
m.add(makeContent("stop",new entity("val")));
m.add(makeContent("start",new entity("val")));
addTestInput("applyDelt",new Pair(new doubleEnt(5000),m));

addTestInput("getOut",new doubleEnt(6000));
addTestInput("applyDelt",new Pair(new doubleEnt(6000),
                         new message()));
}

public simulator(){
   this("simulator",new genr("g",3000));
}


public void initialize(){
    tL = 0;
    tN = 0;
    myModel.initialize();
    super.initialize();
 }




public void  deltext(double e,message x)
{

Continue(e);
   for (int i=0; i< x.size();i++)
      if (messageOnPort(x,"nextTN",i)){

        tN = tL +myModel.ta();
        holdIn("outTN",1);
      }
   else if (messageOnPort(x,"getOut",i))
      {
         entity ent = x.getValOnPort("getOut",i);
         doubleEnt tEnt = (doubleEnt)ent;
         double t = tEnt.getv();
         computeInputOutput(t);
         holdIn("sendOut",0);
       }
   else if (messageOnPort(x,"applyDelt",i))
      {
         entity ent = x.getValOnPort("applyDelt",i);
                    //pair of nev time and message
         Pair p = (Pair)ent;
         entity Ent = (entity)p.getKey();
         doubleEnt tEnt = (doubleEnt)Ent;
         double t = tEnt.getv();
         entity mEnt = (entity)p.getValue();
         message m = (message)mEnt;
   //      m = convert(m,myModel);
  //       m.print();

         wrapDeltfunc(t,m);
  //
        myModel.showState();
         passivate();
       }
}
/*
message convert(message inp,devs d){
message m = new message();
    for (entity p = inp.iterator().next();p != null;p = p.get_right()) {
          entity ent = p;
         content c = (content)ent;
         c.devs = d;
         m.add(c);
}
return m;
}
*/
public void  deltint( )
{
passivate();
}

public message  out( )
{

   message  m = new message();
  if (tN >= DevsInterface.INFINITY)tN = 10000000;
  if (phaseIs("outTN"))
         m.add(makeContent("outTN",  new doubleEnt(tN)));
   else  if (phaseIs("sendOut") && output != null)
        m.add(makeContent("sendOut",output));

m.print();
  return m;
}


public  void computeInputOutput(double t){
      if(tN == t) {
          output = myModel.out();
      }
      else{
        output = new message();
      }
}

public  void  wrapDeltfunc(double t,MessageInterface x){
 if(x == null){
    System.out.println("ERROR RECEIVED NULL INPUT  " + myModel.toString());
    return;
  }
  if (x.isEmpty() && tN != t) {
    return;
  }
  else if((!x.isEmpty()) && tN == t) {
    double e = t - tL;
    myModel.deltcon(e,x);
  }
  else if(tN == t) {
    myModel.deltint();
  }
  else if(!x.isEmpty()) {
    double e = t - tL;
    myModel.deltext(e,x);
  }

  tL = t;
  tN = tL + myModel.ta();
}

public String getTooltipText(){
   return
   super.getTooltipText()
   +"\n"+ "mytL: "+tL
   +"\n"+ "mytN: "+tN
    +"\n"+ myModel.toString();
  }
}

