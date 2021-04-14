package Component.Counters;

import model.modeling.message;
import Component.BasicProcessor.*;
import Component.SISO.*;
import GenCol.entity;
import view.modeling.ViewableAtomic;

public class binaryCounterRespondImmediately extends ViewableAtomic {

	protected int counter;
	protected double stepTime;
	protected double remainingTime = 0;
	public  binaryCounterRespondImmediately()
	{
		super("binaryCounterImmediate");
		stepTime = 10.0;
		
		addInport("in");
		addOutport("out");
		
		addTestInput("in",new entity("1"));
		addTestInput("in",new entity("2"),10);
		addTestInput("in",new entity("0"));
		addTestInput("in",new entity("0"), 10);
		addTestInput("in",new entity("0"), 5);
		
	}
	
	public binaryCounterRespondImmediately(double step)
	{
		super("binaryCounterImmediate");
		stepTime = step;
		
		addInport("in");
		addOutport("out");
		
		addTestInput("in",new entity("1"));
		addTestInput("in",new entity("2"),10);
		addTestInput("in",new entity("0"));
		addTestInput("in",new entity("0"), 10);
		addTestInput("in",new entity("0"), 5);
	}
	
	public void initialize()
	{
		//initialize to passive indefinately.
		holdIn("passive", INFINITY);
		counter = 0;
		remainingTime = 0;
		super.initialize();
	}
	
	public void deltint(){
		//go back to passive.
		double temp;
		// need to resume the active 
		if(phaseIs("respond") && remainingTime > 0.0)
		{
			counter++;
			temp = remainingTime;
			remainingTime = -1.0;
			holdIn("active", temp);
		}
		else
			holdIn("passive", INFINITY);	//must be caps
		
		
	}
	
	public message out(){
		
		message m = new message();
		if(phaseIs("respond"))
		{
			m.add(makeContent("out", new entity(Integer.toString(counter))));
		}
		return m;
	}
	
	public void deltcon(double e,message x)
	{
	   deltint();
	   deltext(0,x);
	}
	
	public void deltext(double e, message x){
		//sigma = sigma – elapsed time; //  update sigma to reflect elapsed time
		Continue(e);
		//when receive input value on input port “in”
		for (int i=0; i< x.size(); i++){
		if(messageOnPort(x, "in", i))
		{
		//    if input is non zero and phase is passive then 
			entity value = x.getValOnPort("in", i);
			if(!value.eq("0") && phaseIs("passive"))
			{
				//counter is incremented on internal transition
				holdIn("active", stepTime);
			}
			else if(value.eq("0"))
			{
				remainingTime = stepTime - e;
				holdIn("respond", 0);
			}
		}
		}
	}
	
}