package Component.OddsAndEnds;

import model.modeling.message;
import Component.BasicProcessor.*;
import GenCol.entity;
import view.modeling.ViewableAtomic;

public class MyCellPhone extends ViewableAtomic{

	private static final String INPUTPORT = "SwitchOn/Off";
	private static final String CALLEVENTPORT = "incomingVoice";
	private static final String OUTPUTPORT = "outgoingVoice";
	private static final String SEARCHFORSERVICE="SearchingForService";
	private static final String ACTIVE="Active";
	private static final String READY="Ready";
	private static final String INTERRUPT = "Interrupt";
	private static final String ON="On";
	private static final String OFF="Off";

	private String outEvent;
	private boolean present = false;
	private double timeRem;
	private double steptime = 10;
	MyQueue<MyEntity> callEventQ = new MyQueue<MyEntity>();

	public MyCellPhone(String name){
		super(name);
		addInport(INPUTPORT);
		addInport(CALLEVENTPORT);
		addOutport(OUTPUTPORT);
		addTestInput(INPUTPORT, new entity("Start"));
		addTestInput(INPUTPORT, new entity("Stop"));

		char[] letters = new char[26];
		for(int i=0;i<26;i++){
			letters[i] = (char) ('a' + i);
		}
		for(int i=0;i<26;i++){
			addTestInput(CALLEVENTPORT,new MyEntity(letters[i]+"1", (int) (i+10*Math.random())));
		}
	}

	public MyCellPhone(){
		this("MyCellPhone");
	}

	/* 
	 * Places the model in phase = passive and require input event for waking up (activating) the model 
	 */
	public void initialize() {
		super.initialize();
		phase = "Off";
		sigma = INFINITY;
	}

	public void deltext(double e, message x) {

		for (int i = 0; i < x.getLength(); i++) {
			if(messageOnPort(x, INPUTPORT, i)){
				entity input=x.getValOnPort(INPUTPORT,i);
				timeRem = sigma - e;
				if(input.eq("Start") && phaseIs(OFF)){
					holdIn(ON, 5);
				}else if(input.eq("Stop")){
					sigma = INFINITY;
					holdIn(OFF,sigma);
				}
			}
			if(messageOnPort(x,CALLEVENTPORT,i)){
				MyEntity input = (MyEntity) x.getValOnPort(CALLEVENTPORT,i);
				if(phaseIs(READY)){
					outEvent = input.getName();
					holdIn(ACTIVE,input.getCallDuration());
				}else if(phaseIs(ACTIVE)){
					callEventQ.enqueue(input);
					timeRem = sigma - e;
					holdIn(INTERRUPT, 0.1);
				}
			}
		}
	}

	public void deltint() {
		if(phaseIs(INTERRUPT) && timeRem>0){
			holdIn(ACTIVE, timeRem);
		}else if(phaseIs(ACTIVE)){
			if(!callEventQ.isEmpty()){
				MyEntity e = (MyEntity) callEventQ.dequeue();
				outEvent = e.getName();
				holdIn(ACTIVE,e.getCallDuration());
			}
			else{
				holdIn(READY,0);
			}
		}
		if(phaseIs(ON) || phaseIs(READY))
			holdIn(SEARCHFORSERVICE, 5);
		else if(phaseIs(SEARCHFORSERVICE)){
			if(!present){
				holdIn(READY, steptime);
				present = true;
			} else {
				holdIn(ON, steptime);
				present = false;
			}
		}
	}

	public void deltcon(double e, message x) {
		deltext(0, x);
		deltint();
	} 

	public message out() {
		message m = new message();
		if(phaseIs(ACTIVE)){
			if(outEvent!=null){
				System.out.println("Output = " + outEvent.toUpperCase().charAt(0) + 2);
			}
		}
		else 
		{
			System.out.println("Output = " + Integer.MIN_VALUE);
		}
		return m;
	}
}
