/*
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.modeling;



import java.util.HashMap;
import java.util.Set;

import GenCol.Pair;
import model.simulation.CoupledSimulatorInterface;
import model.simulation.coordinator;
import util.Logging;
import util.classUtils.DevsClassField;
import util.tracking.TrackingType;

//public abstract
public class atomic extends devs implements AtomicInterface,DynamicStructureInterface{
 protected double sigma;
 protected String phase;
 protected ActivityInterface a;
 protected static double  INFINITY  = DevsInterface.INFINITY;
 protected CoupledSimulatorInterface mySim;
 private HashMap<String, DevsClassField> states;

public CoupledSimulatorInterface getSim(){
    return mySim;
}

public atomic(){
this("atomic");
}

public atomic(String name){
super(name);
sigma = INFINITY;
}
public atomic(ActivityInterface a){
this(a.getName());
this.a = a;
}
public void setSimulator(CoupledSimulatorInterface mySim){
this.mySim = mySim;
}

public String getPhase(){
return phase;
}

public void setSigma(double sigma){

  this.sigma = sigma;
}

public double getSigma(){
return sigma;
}

public void initialize(){
}

public void Continue(double e){
 if (sigma < INFINITY)
    setSigma(sigma - e);
}

public void setStates(HashMap<String, DevsClassField> states)
{
    this.states = states;
}

public Set<String> getStateNames()
{
    return this.states.keySet();
}

public Object getStateValue(String name)
{
    if (name.equals("Phase"))
    {
        return getPhase();
    }
    else if (name.equals("Sigma"))
    {
        return getSigma();
    }
    DevsClassField state = this.states.get(name);
    
    if (state != null)
    {
        try
        {
            return state.getObject();
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
    return null;
}

public boolean isStateTagged(String name)
{
   DevsClassField state = this.states.get(name);
   
   if (state == null)
   {
       return false;
   }
   
   return state.isState();
}

public boolean isStateDefaultTracked(String name, TrackingType setting)
{
    DevsClassField state = this.states.get(name);
    
    if (state == null)
    {
        return false;
    }
   
    return state.isDefaultCheckedFor(setting);
}

    public void passivateIn(String phase) {holdIn(phase, INFINITY);}
    public void passivate() {passivateIn("passive");}
    public void holdIn(String phase, double sigma) {holdIn(phase, sigma, null);}

    public void holdIn(String phase, double sigma,ActivityInterface a)
    {

        this.phase = phase;
        setSigma(sigma);
        if (a != null) {
            this.a = a;
            mySim.startActivity(a);
        }
    }


  public void startActiviry(ActivityInterface a){
    this.a = a;
    mySim.startActivity(a);
  }

  public boolean phaseIs(String phase){
  return this.phase.equals(phase);
  }


  public double ta(){
      return sigma;
  }

  public void deltint(){}
  public void deltext(double e,MessageInterface x){deltext(e,(message)x);}
  public void deltext(double e,message x){} //usual devs
  public void deltcon(double e,MessageInterface x){deltcon(e,(message)x);}
  public void deltcon(double e,message x){ //usual devs
   deltint();
   deltext(0,x);
}

 public MessageInterface Out(){

 return out();
}

 public message out(){
 return new message();
}

 public String toString(){
 return "phase :"+ phase + " sigma : " + sigma ;
 }

  public void showState(){
    Logging.log("Model "+ getName() + "'s state: ", Logging.full);
    Logging.log("phase :"+ phase + " sigma : " + sigma, Logging.full);
  }

  public ActivityInterface getActivity(){
  return a;
  }

    public String stringState(){
    return "";
    }


    // Dynamic Structure Interface implementation
    public void addCoupling(String src, String p1, String dest, String p2){
        digraph P = (digraph)getParent();
        if(P!=null){
          //update its parent model's coupling info database
          P.addPair(new Pair(src,p1),new Pair(dest,p2));
          // update the corresponding simulator's coupling info database
          coordinator PCoord = P.getCoordinator();
          PCoord.addCoupling(src,p1,dest,p2);
        }
        /*
        else{   // this is for the distributed simulation situation
          if (mySim instanceof RTCoupledSimulatorClient)
            ((RTCoupledSimulatorClient)mySim).addDistributedCoupling(src,p1,dest,p2);
        }*/
      }

     public void removeCoupling(String src, String p1, String dest, String p2){
        digraph P = (digraph)getParent();
        if(P!=null){
          //update its parent model's coupling info database
          P.removePair(new Pair(src,p1),new Pair(dest,p2));
          // update the corresponding simulator's coupling info database
          coordinator PCoord = P.getCoordinator();
          PCoord.removeCoupling(src,p1,dest,p2);
        }
        /*
        else{   // this is for the distributed simulation situation
          if (mySim instanceof RTCoupledSimulatorClient)
            ((RTCoupledSimulatorClient)mySim).removeDistributedCoupling(src,p1,dest,p2);
        }*/
     }

     public void addModel(IODevs iod){
       digraph P = (digraph)getParent();
       if(P.checkNameUniqueness(iod.getName())){
     //    System.out.println(" add model: "+iod.getName());
         P.add(iod);
         coordinator PCoord = P.getCoordinator();
         PCoord.setNewSimulator( (IOBasicDevs) iod);
       }
       else System.out.println("Conflict!! -- the model name already exists");
     }

     public void removeModel(String modelName){
       digraph P = (digraph)getParent();
       coordinator PCoord = P.getCoordinator();
       // first remove all the coupling related to that model
       PCoord.removeModelCoupling(modelName);
       // then remove the model
       IODevs iod = P.withName(modelName);
       P.remove(iod);
       PCoord.removeModel(iod);
     }

     //Copied from  saurabh
     public void addInport(String modelName, String port){
       //System.out.print("Inside atomic inport");
       digraph P = (digraph)getParent();
       IODevs iod = (IODevs)P.withName(modelName);
       if (P != null){
         if (iod instanceof atomic)
           iod.addInport(port);
         else
           ((digraph)iod).addInport(iod.getName(),port);
       }else
         System.out.print(modelName+ ": Its parent is not defined");

       addInportHook(modelName,port);
     }

     public void addInportHook(String modelName, String port){
    	  //s.s("Inside addInport hook 1");
    	  System.out.print("Inport added: "+port+"      component: "+modelName);
    	}


     public void addOutport(String modelName, String port){
       digraph P = (digraph)getParent();
       IODevs iod = (IODevs)P.withName(modelName);
       if (P != null){
         if (iod instanceof atomic)
           iod.addOutport(port);
         else
           ((digraph)iod).addOutport(iod.getName(),port);
       }else
         System.out.print(modelName+ ": Its parent is not defined");

      addOutportHook(modelName,port);
     }

     

     public void addOutportHook(String modelName, String port){
         //System.out.print("Inside addOutport hook 1");
        System.out.print("Outport added: "+port+"      component: "+modelName);
      }

      public void removeInport(String modelName, String port){
        //System.out.print("Inside atomic removeInport");
        digraph P = (digraph)getParent();
          if (P != null)
          ((IODevs)P.withName(modelName)).removeInport(port);
          else
        	  System.out.print(modelName+ ": Its parent is not defined");

          removeInportHook(modelName,port);
      }

      public void removeInportHook(String modelName, String port){
        //System.out.print("Inside removeInport hook 1");
        System.out.print("Inport removed: "+port+"      component: "+modelName);
      }

      public void removeOutport(String modelName, String port){
        //System.out.print("Inside atomic removeOutport");
        digraph P = (digraph)getParent();
          if (P != null)
            ((IODevs)P.withName(modelName)).removeOutport(port);
          else
            System.out.print(modelName+ ": Its parent is not defined");

          removeOutportHook(modelName,port);
      }

      public void removeOutportHook(String modelName, String port){
        //System.out.print("Inside removeOutport hook 1");
        System.out.print("Inport removed: "+port+"      component: "+modelName);
      }

     
    
}