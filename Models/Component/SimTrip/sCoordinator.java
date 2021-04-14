/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.SimTrip;


import java.awt.*;

import Component.BasicProcessor.*;
import GenCol.*;
import model.modeling.*;
import model.simulation.*;
import view.modeling.ViewableAtomic;
import view.simView.*;



public class sCoordinator extends ViewableAtomic{


  protected double tN;
  protected devs g,p,t;
  protected message gMail,pMail,tMail;

public sCoordinator(){
   this("sCoordinator",
       new genr("g",3000), new proc("p", 5000), new transd("t", 20000));
   }

public sCoordinator(String nm, devs g, devs p, devs t){
   super(nm);
   addInport("getTN");
   addInport("getOutFromG");
   addInport("getOutFromP");
   addInport("getOutFromT");
   addOutport("nextTN");
   addOutport("getOut");
   addOutport("applyDelt");


   this.g = g;
   this.p = p;
   this.t = t;


addTestInput("getTN",new doubleEnt(1000));
addTestInput("getTN",new doubleEnt(2000));
message m = new message();
m.add(makeContent("out",new entity("job0")));
addTestInput("getOutFromG",m);
}



public void initialize(){
    tN = INFINITY;
    gMail = new message();
    pMail = new message();
    tMail = new message();

    holdIn("nextTN",0);
    super.initialize();
 }




public void  deltext(double e,message x)
{
Continue(e);

   if (phaseIs("waitTN")){
   for (int i=0; i< x.size();i++)
         if (messageOnPort(x,"getTN",i))
      {
         entity ent = x.getValOnPort("getTN",i);
         doubleEnt tEnt = (doubleEnt)ent;
         double t = tEnt.getv();
      if (t < tN) tN = t;
      }
   if (tN >= 10000000)passivate();
   }
   else if (phaseIs("waitOut")){
      for (int i=0; i< x.size();i++)
      if (messageOnPort(x,"getOutFromG",i))
      {
         entity ent = x.getValOnPort("getOutFromG",i);
         message m = (message)ent;
         if (!m.isEmpty() && messageOnPort(m,"out",0)){

         entity mEnt = m.getValOnPort("out",0);
                        //use g to p and t coupling
          pMail.add(makeContent("in",mEnt));
          tMail.add(makeContent("ariv", mEnt));
         }
       }
       else if (messageOnPort(x,"getOutFromP",i))
      {
         entity ent = x.getValOnPort("getOutFromP",i);
         message m = (message)ent;
         if (!m.isEmpty() && messageOnPort(m,"out",0)){
         entity mEnt = m.getValOnPort("out",0);
                   //use p to  t coupling
          tMail.add(makeContent("solved",mEnt));
        }

      }
     else if (messageOnPort(x,"getOutFromT",i))
      {
         entity ent = x.getValOnPort("getOutFromT",i);
         message m = (message)ent;
         if (!m.isEmpty() && messageOnPort(m,"out",0)){
         entity mEnt = m.getValOnPort("out",0);
          gMail.add(makeContent("stop", mEnt));
                //use t to  g coupling
             }

      }

   }

}

public void  deltint( )
{
if (phaseIs("nextTN"))
    holdIn("waitTN",10);
else if (phaseIs("waitTN")){
 if (tN < INFINITY)
    holdIn("getOut",0);
 else passivate();
}
 else if (phaseIs("getOut"))
    holdIn("waitOut",10);
 else if (phaseIs("waitOut"))
    holdIn("applyDelt",0);
 else if (phaseIs("applyDelt")){

    tN = INFINITY;
    gMail = new message();
    pMail = new message();
    tMail = new message();

    holdIn("nextTN",1);
 }

}

public message  out( )
{
   message  m = new message();
   if (phaseIs("nextTN"))
         m.add(makeContent("nextTN",new entity()));
   else if (phaseIs("getOut"))
         m.add(makeContent("getOut",new doubleEnt(tN)));
   else if (phaseIs("applyDelt")){
         m.add(makeContent("applyDeltG",new Pair
             (new doubleEnt(tN),gMail)));
         m.add(makeContent("applyDeltP",new Pair
             (new doubleEnt(tN),pMail)));
        m.add(makeContent("applyDeltT",new Pair
             (new doubleEnt(tN),tMail)));
   }

  return m;
}


public String getTooltipText(){
   return
   super.getTooltipText()
    +"\n"+"modeltN: " + tN
    +"\n"+"gMail: " + gMail.toString()
     +"\n"+"pMail: " + pMail.toString()
      +"\n"+"tMail: " + tMail.toString();
  }

}

