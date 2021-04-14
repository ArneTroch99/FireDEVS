/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */

package Component.BasicProcessor;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import GenCol.doubleEnt;
import GenCol.entity;


import model.modeling.content;
import model.modeling.message;

import view.modeling.ViewableAtomic;



public class transd extends  ViewableAtomic{
 protected Map arrived, solved;
 protected double clock,total_ta,observation_time;
 public Double count=0.00;

 public transd(String  name,double Observation_time){
  super(name);
   addInport("in");
   addOutport("out");
   addOutport("TA");
   addOutport("Thru");
  addInport("ariv");
  addInport("solved");
  //addOutport("out");
  arrived = new HashMap();
  solved = new HashMap();
  observation_time = Observation_time;
  addTestInput("ariv",new entity("val"));
  addTestInput("solved",new entity("val"));
  initialize();
  
  setBackgroundColor(Color.green);
 }

 public transd() {this("transd", 200);}

 public void initialize(){
  phase = "active";
  sigma = observation_time;
  clock = 0;
  total_ta = 0;
  super.initialize();
 }

 public void showState(){
  super.showState();
  System.out.println("arrived: " + arrived.size());
  System.out.println("solved: " + solved.size());
  System.out.println("TA: "+compute_TA());
  System.out.println("Thruput: "+compute_Thru());
 }

 public void  deltext(double e,message  x){
//	 System.out.println("--------Transduceer elapsed time ="+e);
//	 System.out.println("-------------------------------------");
  clock = clock + e;
  Continue(e);
  entity  val;
  for(int i=0; i< x.size();i++){
    if(messageOnPort(x,"ariv",i)){
       val = x.getValOnPort("ariv",i);
           arrived.put(val.getName(),new doubleEnt(clock));
    }
    if(messageOnPort(x,"solved",i)){
       val = x.getValOnPort("solved",i);
       count++;
       /*
       if(arrived.contains(val)){
    	   System.out.println("Debug: val="+val);
         entity  ent = (entity)arrived.assoc(val.getName());

         doubleEnt  num = (doubleEnt)ent;
         double arrival_time = num.getv();

         double turn_around_time = clock - arrival_time;
         total_ta = total_ta + turn_around_time;
         solved.put(val, new doubleEnt(clock));
       }
       */
       if(arrived.containsKey(val.getName())){
         //entity  ent = (entity)arrived.assoc(val.getName());
         entity  ent = (entity)arrived.get(val.getName());

         doubleEnt  num = (doubleEnt)ent;
         double arrival_time = num.getv();

         double turn_around_time = clock - arrival_time;
         total_ta = total_ta + turn_around_time;
         solved.put(val.getName(), new doubleEnt(clock));
       }
    }
  }
show_state();
 }

 public void  deltint(){
  clock = clock + sigma;
  passivate();
  show_state();
 }

 public  message    out( ){
  message  m = new message();
  content  con1 = makeContent("TA",new entity(" "+compute_TA()));
  content  con2 = makeContent("out",new entity(count.toString()));
  content  con3 = makeContent("Thru",new entity(" "+compute_Thru()));
  m.add(con1);
  m.add(con2);
  m.add(con3);
  return m;
 }

 public double compute_TA(){
  double avg_ta_time = 0;
  if(!solved.isEmpty())
    avg_ta_time = ((double)total_ta)/solved.size();
  return avg_ta_time;
 }

 
 public double compute_Thru(){
  double thruput = 0;
  if(clock > 0)
    thruput = solved.size()/(double)clock;
  return thruput;
 }

 public void  show_state(){

//  System.out.println("state of  "  +  name  +  ": " );
// System.out.println("phase, sigma : "
//          + phase  +  " "  +  sigma  +  " "   );

 if (arrived != null && solved != null){
//   System.out.println(" jobs arrived :" );
  //          arrived.print_all();;

//  System.out.println("total :"  +  arrived.size()  );


//  System.out.println("jobs solved :" );
  //          solved.print_all();
//  System.out.println("total :"  +  solved.size()  );
//  System.out.println("AVG TA = "  +  compute_TA()   );
//  System.out.println("THRUPUT = "  +  compute_Thru()  );
  }
 }
}
