/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.BasicProcessor;


import java.lang.*;

import GenCol.*;


import model.modeling.*;
import model.simulation.*;

import util.*;
import view.modeling.ViewableAtomic;
import view.simView.*;

public class genrRand extends ViewableAtomic{


  protected double int_arr_time;
  protected int count;
  protected rand r;

public genrRand(){
   super("genrRand");
   addInport("stop");
   addInport("start");
  addOutport("out");
   int_arr_time = 10;

addTestInput("start",new entity("val"));
addTestInput("stop",new entity("val"));
addTestInput("stop",new entity("val000000000000000000000000000000000000"));
    r = new rand(1);
  initialize();
}

public genrRand(String name,double Int_arr_time){
   super(name);
   addInport("stop");
   addInport("start");
   //addOutport("out");
   int_arr_time = Int_arr_time ;
    r = new rand(1);
    initialize();
}

public void initialize(){

if (r != null)// must be left in
   holdIn("busy",r.uniform(int_arr_time));

     count = 0;
     super.initialize();
 }




public void  deltext(double e,message x)
{
Continue(e);

   for (int i=0; i< x.size();i++)
      if (messageOnPort(x,"start",i))
      {
  // holdIn("busy",r.uniform(int_arr_time));
     holdIn("busy",r.expon(int_arr_time));
     }

   for (int i=0; i< x.size();i++)
      if (messageOnPort(x,"stop",i))
         passivate();
}

public void  deltint( )
{
if(phaseIs("busy")){
   count = count +1;
    holdIn("busy",r.uniform(int_arr_time));
   //holdIn("busy",r.normal(int_arr_time,1.0));
}
}

public message  out( )
{

   message  m = new message();
   content con = makeContent("out",
            new job("job" + count,r.expon(1000)));
   m.add(con);
  return m;
}


}

