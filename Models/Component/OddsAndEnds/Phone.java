package Component.OddsAndEnds;

import model.modeling.message;
import Component.BasicProcessor.*;
import Component.SISO.*;
import GenCol.Queue;
import GenCol.entity;


public class Phone extends siso {

	// Phases
	private static final String PHASE_ON = "On";
	private static final String PHASE_OFF = "Off";
	private static final String PHASE_SEARCHING_FOR_SERVICE = "SearchingForService";
	private static final String PHASE_ACTIVE = "Active";
	private static final String PHASE_READY = "Ready";
	
	// Input Ports
	private static final String PORT_IN_ON_OFF = "OnOff";
	private static final String PORT_IN_INCOMING_VOICE = "incomingVoice";
	
	// Output Ports
	private static final String PORT_OUT_OUTGOING_VOICE = "outgoingVoice";
	
	// Events
	private static final String EVENT_START = "start";
	private static final String EVENT_STOP = "stop";
	private String[] talkInput = { "a1", "b1", "c1", "d1", "e1", "f1",
									"g1", "h1", "i1", "j1", "k1", "l1",
									"m1", "n1", "o1", "p1", "q1", "r1",
									"s1", "t1", "u1", "v1", "w1", "x1",
									"y1", "z1" };
	
	// Model Name
	private static final String MODEL_NAME = "PhoneModel";
	
	// Service status
	private boolean serviceAvailable = true;
	
	// Calls waiting to be taken
	protected Queue callsOnHold;
	
	public Phone() {
		super(MODEL_NAME);
		setupModel();
	}
	
	public Phone(String name) {
		super(name);
		setupModel();
	}
	
	private void setupModel() {
		
		// Input ports
		addInport(PORT_IN_ON_OFF);
		addInport(PORT_IN_INCOMING_VOICE);
		
		// Output ports
		addOutport(PORT_OUT_OUTGOING_VOICE);
		
		// Test inputs
		addTestInput(PORT_IN_ON_OFF, new entity(EVENT_START));
		addTestInput(PORT_IN_ON_OFF, new entity(EVENT_STOP));
		
		for(int i = 0; i < talkInput.length; i++) {
			addTestInput(PORT_IN_INCOMING_VOICE, new entity(talkInput[i]));
		}
		
	}
	
	public void initialize() {
		holdIn(PHASE_OFF, INFINITY);
		
		callsOnHold = new Queue();
		
		super.initialize();
	}
	
	public void deltext(double e, message msg) {
		Continue(e);
	
		entity onOffVal = null;
		
		if (messageOnPort(msg, PORT_IN_ON_OFF, 0)) {
			onOffVal = msg.getValOnPort(PORT_IN_ON_OFF, 0);
		}
		
		if (phaseIs(PHASE_OFF)) {
			
			if (onOffVal != null && onOffVal.getName().equals(EVENT_START)) {
				holdIn(PHASE_ON, 1);
			}
			
		} else {
			
			if (onOffVal != null && onOffVal.getName().equals(EVENT_STOP)) {
				holdIn(PHASE_OFF, INFINITY);
				callsOnHold.clear();
			} else {
				
				if (messageOnPort(msg, PORT_IN_INCOMING_VOICE, 0)) {
					entity voiceVal = msg.getValOnPort(PORT_IN_INCOMING_VOICE, 0);
					
					callsOnHold.add(voiceVal.getName());
					
					if (phaseIs(PHASE_READY)) {
						holdIn(PHASE_ACTIVE, Math.random() * 15);
					}
				}
					
			}
			
		}
	}
	
	public void deltint() {
		 {
		if (phase.equals(PHASE_ON))
			holdIn(PHASE_SEARCHING_FOR_SERVICE, 2);
			
		else if (phase.equals(PHASE_SEARCHING_FOR_SERVICE))
		{
			if (isServiceAvailable()) {
				holdIn(PHASE_READY, INFINITY);
			} else {
				holdIn(PHASE_ON, 1);
			}
		}
		else if (phase.equals( PHASE_ACTIVE))
		{
			if (callsOnHold.size() == 0) {
				holdIn(PHASE_READY, INFINITY);
			} else {
				holdIn(PHASE_ACTIVE, Math.random() * 15);
			}
		}
		else{
				holdIn(getPhase(),0.1);
			}
		}
	}
	
	public message out() {
		message m = new message();
		
		if (phaseIs(PHASE_ACTIVE)) {
			m.add(makeContent(PORT_OUT_OUTGOING_VOICE,new entity(((String)callsOnHold.pop()).toUpperCase())));
		}
		
		return m;
	}
	
	public boolean isServiceAvailable() {
		if (serviceAvailable) {
			serviceAvailable = false;
			return true;
		} else {
			serviceAvailable = true;
			return false;
		}
	}
	
	protected void log(String m) {
		System.out.println(m);
	}
	
}
