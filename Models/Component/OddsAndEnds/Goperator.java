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
import view.modeling.ViewableAtomic;
import view.simView.*;

public class Goperator extends ViewableAtomic{
	
	protected doubleEnt input,sv;
	protected double processing_time;
	protected Queue q;
	
	public Goperator() {
		this("Goperator", 1.0);
	}
	
	public Goperator(String name, double processingTime){

		super(name);
		processing_time = processingTime;

		addInport("in");
		addOutport("out");
		
		addTestInput("in",new doubleEnt(1.0),1);
		addTestInput("in",new doubleEnt(1.0),0);
		addTestInput("in", new doubleEnt(4.0));
//		addTestInput("in",new doubleEnt(4.0));
//		addTestInput("in",new doubleEnt(8.0));
//		addTestInput("in",new doubleEnt(10.0));
//		addTestInput("in",new doubleEnt(0.5));
//		addTestInput("in",new doubleEnt(0.1));
//		addTestInput("in",new doubleEnt(0.0));
//		addTestInput("in",new doubleEnt(-4.0));

	
		initialize();
	}

	public void initialize(){
	    phase = "passive";
	    sigma = INFINITY;
	    input = new doubleEnt(-INFINITY);
	    q = new Queue();
	    super.initialize();
	}
	
	public void  deltext(double e,message   x)	{
		Continue(e);

		if (phaseIs("passive"))
			for (int i=0; i< x.getLength();i++)
				if (messageOnPort(x,"in",i)){
					input = (doubleEnt)x.getValOnPort("in",i);
					sv = input;
					q.addLast(input);
//					double inputVal = Double.parseDouble(job.toString());
					double time = 1/Math.abs(input.getv());
					holdIn("active", time);
				}
	}

	public void  deltint( ){
		passivateIn("passive");
	
	}

	public void deltcon(double e, message x){
		deltint();
		deltext(0.0, x);
	}

	public message out( ){
		message m = new message();
		String sv = new String();

		if(!q.isEmpty()){
			sv = q.getFirst().toString();
			q.removeFirst();
		
			if (phaseIs("active")) {
				entity e = new entity(sv);
				m.add(makeContent("out", e));
			}
		}
		return m;
	}

	  public void showState(){
		  super.showState();
		  System.out.println("sv: " + sv);
	 }

	 public String getTooltipText(){
		 return
		 super.getTooltipText() +"\n"+"sv: " + sv;
	 }
}
