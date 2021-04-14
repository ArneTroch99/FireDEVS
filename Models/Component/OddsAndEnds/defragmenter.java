package Component.OddsAndEnds;

import model.modeling.*;
import model.simulation.*;
import view.modeling.ViewableAtomic;
import Component.BasicProcessor.*;
import GenCol.*;

public class defragmenter extends ViewableAtomic{
	
	protected entity job;
	protected double processing_time;
	
	public defragmenter() {
		this("defragmenter", 20.0);
	}

	public defragmenter(String name, double processingTime){

		super(name);
		processing_time = processingTime;

		addInport("in");
		addOutport("out");
		
		addTestInput("in", new entity("startV1"));
		addTestInput("in", new entity("startV2"));
		addTestInput("in", new entity("startBoth"));
		addTestInput("in", new entity("cancelV1"));
		addTestInput("in", new entity("cancelV2"));
		addTestInput("in", new entity("cancelBoth"));
		addTestInput("in", new entity("finishV1"));
		addTestInput("in", new entity("finishV2"));
		addTestInput("in", new entity("finishBoth"));
		
		initialize();
	}
	
	public void initialize(){
	    phase = "idle";
	    sigma = INFINITY;
	    job = new entity("job");
	    super.initialize();
	}

	public void  deltext(double e, message   x)
	{
		Continue(e);

		if (phaseIs("idle"))
			for (int i=0; i< x.getLength();i++)
				if (messageOnPort(x,"in",i))
				{
					job = x.getValOnPort("in",i);
					String testInput = job.toString();
					if(testInput.equals("startV1"))
						holdIn("busy_V1", 30);
					else if(testInput.equals("startV2"))
						holdIn("busy_V2", 20);
					else if(testInput.equals("startBoth"))
						holdIn("busy_Both", 40);
				}
		
		if(phaseIs("busy_V1"))
			for(int i = 0; i < x.getLength(); i++)
				if(messageOnPort(x,"in",i)){
					job = x.getValOnPort("in",i);
					String testInput = job.toString();
					if(testInput.equals("cancelV1"))
						holdIn("idle", INFINITY);	
					else if(testInput.equals("finishV1"))
						holdIn("idle", INFINITY);
				}
		if(phaseIs("busy_V2"))
			for(int i = 0; i < x.getLength(); i++)
				if(messageOnPort(x,"in",i)){
					job = x.getValOnPort("in",i);
					String testInput = job.toString();
					if(testInput.equals("cancelV2"))
						holdIn("idle", INFINITY);	
					else if(testInput.equals("finishV2"))
						holdIn("idle", INFINITY);
				}
		if(phaseIs("busy_Both"))
			for(int i = 0; i < x.getLength(); i++)
				if(messageOnPort(x,"in",i)){
					job = x.getValOnPort("in",i);
					String testInput = job.toString();
					if(testInput.equals("cancelBoth"))
						holdIn("idle", INFINITY);	
					else if(testInput.equals("finishBoth"))
						holdIn("idle", INFINITY);
				}	
	}

	public void  deltint( ){
		passivateIn("idle");
	
	}

	public void deltcon(double e, message x){
		deltint();
		deltext(0.0, x);
	}

	public message out( ){
		message m = new message();
		if(phaseIs("idle")){
			entity e = new entity("Idle");
			m.add(makeContent("out", e));
		}
		else if (phaseIs("busy_V1")) {
			entity e = new entity("Defragmenting_V1");
			m.add(makeContent("out", e));
		}
		else if (phaseIs("busy_V2")) {
			entity e = new entity("Defragmenting_V2");
			m.add(makeContent("out", e));
		}
		else if (phaseIs("busy_Both")) {
			entity e = new entity("Defragmenting_Both");
			m.add(makeContent("out", e));
		}
		
		return m;
	}

	  public void showState(){
		  super.showState();
		  System.out.println("job: " + job.getName());
	 }

	 public String getTooltipText(){
		 return
		 super.getTooltipText() +"\n"+"job: " + job.getName();
	 }

}
