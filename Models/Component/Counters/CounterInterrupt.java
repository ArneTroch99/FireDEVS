package Component.Counters;

import model.modeling.message;
import Component.BasicProcessor.*;
import Component.SISO.*;
import GenCol.entity;
import GenCol.intEnt;
import view.modeling.ViewableAtomic;

public class CounterInterrupt extends ViewableAtomic {

	private static final String INPUTPORT="inPort";

	private static final String OUTPUTPORT="outPort";
	private static final double stepTime=10;

	private static final String RESPOND="respond";
	private static final String PASSIVE="passive";
	private static final String ACTIVE="active";

	private int count;
	private double timeLeft;

	/* 
	 * Input and output ports are declared
	 */
	public CounterInterrupt(String name){
		super(name);
		count=0;
		addInport(INPUTPORT);

		addOutport(OUTPUTPORT);
		addTestInput(INPUTPORT, new entity("0"));
		addTestInput(INPUTPORT, new entity("0"), stepTime);
		addTestInput(INPUTPORT, new entity("0"), stepTime*4/stepTime);
		addTestInput(INPUTPORT, new entity("1"));
	}

	public CounterInterrupt(){
		this("CounterInterrupt");
	}

	/* 
	 * Places the model in phase = passive and require input event for waking up (activating) the model 
	 */
	public void initialize() {
		super.initialize();

		count = 0;
		timeLeft = 0;
		phase = "passive";
		sigma = INFINITY;
	}

	/* 
	 * Input events 0 and 1 are only processed when phase = passive
	 */
	public void deltext(double e, message x) {

		for (int i = 0; i < x.getLength(); i++) {
			if(messageOnPort(x, INPUTPORT, i)){
				entity input=x.getValOnPort(INPUTPORT,i);

				if(input.eq("0") && phaseIs(PASSIVE)){
					holdIn(RESPOND, stepTime);
				} else if (input.eq("1") && phaseIs(PASSIVE)){
					count++;
					holdIn(ACTIVE,stepTime);
				} else if (input.eq("0") && phaseIs(ACTIVE)){
					timeLeft = sigma - e;
					if(timeLeft > 0)
						holdIn(RESPOND, 0);
					else 
						holdIn(RESPOND, stepTime);
				}
			}
		}
	}

	/* 
	 * Places the model in the passive phase independent of phase
	 */
	public void deltint() {
		if(phaseIs(PASSIVE) || phaseIs(ACTIVE))
			passivate();
		else if(phaseIs(RESPOND))
			if(timeLeft > 0)
				holdIn(ACTIVE, timeLeft);
			else 
				passivate();
	}

	/* 
	 * Confluent function generates output and executes internal transition function prior
	 * to executing external transition function.
	 */
	public void deltcon(double e, message x) {
		deltint();
		deltext(0, x);
	}

	/* 
	 * Output port is not used for sending output; instead output is written to console
	 */
	public message out() {
		message m = new message();
		if(phaseIs(RESPOND)){
			//System.out.println("Output = " + count);
			m.add(makeContent(OUTPUTPORT, new intEnt(count)));
		}
		else {
			//System.out.println("Output = -11111");
			m.add(makeContent(OUTPUTPORT, new intEnt(-11111)));
		}

		return m;
	}

	public String getTooltipText(){
		return
				super.getTooltipText()
				+"\n" + "Count: " + count
				+"\n" + "Time Left: " + timeLeft;
	}
}
