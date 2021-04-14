package Component.SISO;

import Component.BasicProcessor.*;
import GenCol.doubleEnt;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;

public class CounterSISO extends siso{
	
	int count;
	boolean wasActive;
	double stepTime;
	
	public CounterSISO(){
		this("prob2");
	}
	
	public CounterSISO(String name){
		super(name);
		addInport("in");
		addOutport("out");
		
		addTestInput("in",new doubleEnt((double)1));
		initalize();
	}
	
	public void initalize(){
		holdIn("passive", INFINITY);
		count = 0;
		stepTime=1;
		wasActive = false;
		super.initialize();
	}
	
	public void  Deltext(double e, double input){
		 Continue(e);
		    if(input>0 && phase=="passive"){
			    holdIn("active",stepTime);  
		    }
		    else if (input == 0 && (phase=="passive" || phase=="active")){
		    	if(phase=="active"){
		    		wasActive=true;
		    	}
		    	holdIn("respond", stepTime);
		    }
		}
	
	public void deltint(){
		if(phase=="active"){
			count = count + 1;
		}
		passivate();
	}
	
	public message out( ){
		message  m = new message();
		if(phase=="respond"){
			   content con = makeContent("out", new entity("count: " + count));
			   m.add(con);
		}
		if(wasActive){
			holdIn("active",stepTime);
			wasActive=false;
		}
		return m;
	}
	
	public void showState(){
	    super.showState();
	    System.out.println("count: " + count);
	}
	
	public String getTooltipText(){
		   return
		   super.getTooltipText()
		    +"\n"+" count: " + count
		     +"\n"+" stepTime: " + stepTime;
		  }
}

