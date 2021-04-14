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


public class coordinator extends ViewableAtomic{


  protected double tN;
  protected devs g,p,t;
  protected message gMail,pMail,tMail;

public coordinator(){
   super("coordinator");
   addInport("getTN");
   addInport("getOut");
   addOutport("nextTN");
   addOutport("getOut");
   addOutport("applyDelt");


   g = new genr("g",3000);
   p = new proc("p", 5000);
   t = new transd("t", 20000);


addTestInput("nextTN",new entity(""));
message m = new message();
m.add(makeContent("start",new entity("val")));
addTestInput("applyDelt",new Pair(new doubleEnt(0),m));
addTestInput("getOut",new doubleEnt(3000));
addTestInput("applyDelt",new Pair(new doubleEnt(3000),
                         new message()));

   initialize();
}

public coordinator(String name,devs G,devs P,devs T){
   super(name);
   addInport("getTN");
   addInport("getOutfromG");
   addInport("getOutfromP");
   addOutport("nextTN");
   addOutport("getOut");
   addOutport("applyDelt");

   g = G;
   p = P;
   t = T;

   initialize();
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

   }
   else if (phaseIs("waitOut")){
      for (int i=0; i< x.size();i++)
      if (messageOnPort(x,"getOutFromG",i))
      {
         entity ent = x.getValOnPort("getOutFromG",i);
         message m = (message)ent;
         entity mEnt = m.read(0);

         content con = (content)mEnt;
         if (con.getPort().equals("out")){ //use g to p and t coupling
          pMail.add(makeContent("in", (entity)con.getValue()));
          tMail.add(makeContent("ariv", (entity)con.getValue()));
         }
       }
       else if (messageOnPort(x,"getOutFromP",i))
      {
         entity ent = x.getValOnPort("getOutFromP",i);
         message m = (message)ent;
         for (int j=0; j< m.size();j++)
           if (messageOnPort(m,"out",j))
             {
         entity val = m.getValOnPort("out",j);
                //use p to  t coupling
          tMail.add(makeContent("solved", val));

             }

      }
     else if (messageOnPort(x,"getOutFromT",i))
      {
         entity ent = x.getValOnPort("getOutFromT",i);
         message m = (message)ent;
         for (int j=0; j< m.size();j++)
           if(true)// (messageOnPort(m,"out",j))
             {
                //use t to  g coupling
          gMail.add(makeContent("stop", new entity()));

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

public void showState(){
    super.showState();

    System.out.println("modeltN: " + tN);


    System.out.println("gMail: " + gMail);
    System.out.println("pMail: " + pMail);

}

}

