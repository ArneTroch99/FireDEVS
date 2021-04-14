package Component.OddsAndEnds;

import java.util.Random;

import Component.SISO.*;
import model.modeling.message;
import GenCol.Queue;
import GenCol.entity;
import view.modeling.ViewableAtomic;

public class phoneModel extends ViewableAtomic {
	
	// Available phases for this model.
	private static final String OFF_PHASE = "Off";
	private static final String ON_PHASE = "On";
	private static final String SFS_PHASE = "SearchingForState";
	private static final String ACTIVE_PHASE = "Active";
	private static final String READY_PHASE = "Ready";
	
	// Available ports for this model.
	private static final String ON_OFF_PORT = "OnOff";
	private static final String INCOMING_VOICE_PORT = "incomingVoice";
	private static final String OUTGOING_VOICE_PORT = "outgoingVoice";
	
	// Model-specific variables used.
	private boolean serviceFound;
	private Queue queuedCalls;
	private Random random;
	
	/**
	 * Default constructor.
	 */
	public phoneModel() {
		this("Phone");
	}

	/**
	 * Constructor.
	 * @param name The name of this model.
	 */
	public phoneModel(String name) {
		super(name);
		
		// Objects with no default value are initialized here.
		queuedCalls = new Queue();
		random = new Random();
		
		addInport(ON_OFF_PORT);
		addInport(INCOMING_VOICE_PORT);
		
		addOutport(OUTGOING_VOICE_PORT);
		
		addTestInput(ON_OFF_PORT, new entity("start"));
		addTestInput(ON_OFF_PORT, new entity("stop"));
		addTestInput(ON_OFF_PORT, new entity("stop"), 1);
		addTestInput(ON_OFF_PORT, new entity("stop"), 5);
		addTestInput(INCOMING_VOICE_PORT, new entity("a1"));
		addTestInput(INCOMING_VOICE_PORT, new entity("b1"));
		addTestInput(INCOMING_VOICE_PORT, new entity("c1"), 2);
		addTestInput(INCOMING_VOICE_PORT, new entity("d1"));
	}
	
	/**
	 * Initialize the phone model to be in an off phase
	 * perpetually until changed from input.
	 */
	public void initialize() {
		super.initialize();
		sigma = INFINITY;
		phase = OFF_PHASE;
		serviceFound = true;
	}
	
	/**
	 * The external transition function handles the receipt of
	 * external input.  Possible transitions are as follows:
	 * Off -> On
	 * ANY_PHASE -> Off
	 * Ready -> Active
	 * Calls received during the Active phase will be queued
	 * and handled by the internal transition function.
	 * @param e The elapsed time.
	 * @param x The message received as input.
	 */
	public void deltext(double e, message x) {
		
		// The continue is partially for test purposes since most incoming
		// input will merely attempt to alter the phase or be ignored.  However,
		// this may be required to show that time can advance before a new call
		// comes in and it is still queued if a previous call hasn't finished.
		Continue(e);
		for (int i = 0; i < x.getLength(); i++) {
			if (messageOnPort(x, ON_OFF_PORT, i)) {
				entity input = x.getValOnPort(ON_OFF_PORT, i);
				
				if (input.eq("stop")) {
					holdIn(OFF_PHASE, INFINITY);
					reinitialize();
				} else if (input.eq("start") && phaseIs(OFF_PHASE)) {
					holdIn(ON_PHASE, 1);
				}
			} else if (messageOnPort(x, INCOMING_VOICE_PORT, i)) {
				entity voice = x.getValOnPort(INCOMING_VOICE_PORT, i);
				
				if (phaseIs(READY_PHASE) && checkIncomingCall(voice)) {
					queuedCalls.add(voice);
					holdIn(ACTIVE_PHASE, random.nextInt(14) + 1.0);
				} else if (phaseIs(ACTIVE_PHASE) && checkIncomingCall(voice)) {
					queuedCalls.add(voice);
				}
			}
		}
	}
	
	/**
	 * The internal transition function handles what to do when phases
	 * have completed.  Possible transitions are as follows:
	 * On -> SearchingForService (SFS)
	 * SFS -> Ready OR On
	 * Ready -> SFS
	 * Active -> Active (New process) OR Ready
	 * Transitions not listed here are handled in the external
	 * transition function.
	 */
	public void deltint() {
		if (phaseIs(ON_PHASE) || phaseIs(READY_PHASE)) {
			holdIn(SFS_PHASE, 2);
		} else if (phaseIs(SFS_PHASE)) {
			if (searchForService()) {
				holdIn(READY_PHASE, 5);
			} else {
				holdIn(ON_PHASE, 1);
			}
		} else if (phaseIs(ACTIVE_PHASE)) {
			queuedCalls.remove();
			
			if (!queuedCalls.isEmpty()) {
				holdIn(ACTIVE_PHASE, random.nextInt(14) + 1.0);
			} else {
				holdIn(READY_PHASE, 5);
			}
		}
	}
	
	/**
	 * The confluent function generates output and executes the internal
	 * transition function before executing the external transition function.
	 * @param e The elapsed time.
	 * @param x The message received as input for the external transition function.
	 */
	public void deltcon(double e, message x) {
		deltint();
		deltext(0, x);
	}
	
	/**
	 * The output function for this model only sends out an appropriate
	 * call response string if the phase is active, otherwise nothing is
	 * sent.
	 * @return The message to send as output.
	 */
	public message out() {
		message m = new message();
		
		if (phaseIs(ACTIVE_PHASE)) {
			m.add(makeContent(OUTGOING_VOICE_PORT, getReplyString((entity)queuedCalls.first())));
		}
		
		return m;
	}
	
	/**
	 * Adds information about the queue to the state information.
	 */
	public void showState() {
		super.showState();
		System.out.println("Queued calls: " + queuedCalls.size());
	}
	
	/**
	 * Adds the number of queued calls to the tooltip.
	 */
	public String getTooltipText() {
		return super.getTooltipText() + "\nQueued calls: " + queuedCalls.size();
	}
	
	/**
	 * Emulates searching for service.  Per assignment assumptions
	 * this returns true first because serviceFound is initialized
	 * to true and alternates each time it is called.
	 * @return True and false alternating.
	 */
	private boolean searchForService() {
		// Set serviceFound to its opposite and then return its
		// original value, the opposite of the new value.
		serviceFound = !serviceFound;
		return !serviceFound;
	}
	
	/**
	 * Helper method to determine if the input sent represents a call.
	 * A valid call is of the form "x1" where x is a valid letter followed
	 * by the number 1.
	 * @param input The entity provided as input.  The use of entity is
	 * for convenience purposes.
	 * @return True if the given entity appears to be a call input; false
	 * otherwise.
	 */
	private boolean checkIncomingCall(entity input) {
		boolean isCall = false;
		
		String inner = input.getName();
		
		if (inner.length() == 2 && Character.isLetter(inner.charAt(0)) && Character.isLowerCase(inner.charAt(0))
				&& inner.charAt(1) == '1') {
			isCall = true;
		}
		
		return isCall;
	}
	
	/**
	 * Assumes the function can only receive a string of the form
	 * "x1" where x is a lower case letter followed by the number
	 * 1. This returns a response string with the same letter in
	 * uppercase followed by a 2.
	 * @param in The string to convert.
	 * @return The response string in an entity object.
	 */
	private entity getReplyString(entity in) {
		String letter = in.getName().substring(0, 1).toUpperCase();  // get the letter
		return new entity(letter + 2);
	}
	
	/**
	 * Resets the phone to its default with no queued calls and the first
	 * call to searchForService returning true.
	 */
	private void reinitialize() {
		queuedCalls.clear();
		serviceFound = true;
	}
}
