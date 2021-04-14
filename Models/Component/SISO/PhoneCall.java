package Component.SISO;

import model.modeling.content;
import model.modeling.message;
import Component.BasicProcessor.*;
import Component.SISO.*;
import GenCol.entity;


public class PhoneCall extends siso {
	
	public entity input;
	public double timeLeft;
	public boolean recieveIV;
	private static final int timeUnit=10;
	
	public PhoneCall(){
		super("Phone");
		addInport("OnOff");
		addInport("incomingVoice");
		addOutport("outcomingVoice");
		
		addTestInput("OnOff", new entity("On"));
		addTestInput("OnOff", new entity("Off"));
		addTestInput("incomingVoice", new entity("a1"));
		addTestInput("incomingVoice", new entity("b1"));
		addTestInput("incomingVoice", new entity("c1"));
		addTestInput("incomingVoice", new entity("d1"));
		
		initalize();
	}
	
	// Initially the phone will be off
	public void initalize(){
		holdIn("Off", INFINITY);
		recieveIV=false;
		super.initialize();
	}
	
	public void  deltext(double e, message x){
		Continue(e);
		for(int i=0; i<x.getLength(); i++){
			// if On or Off is sent to the port, do the following
			if(messageOnPort(x,"OnOff",i)){
				input=x.getValOnPort("OnOff", i);
				// Here when turning on, it will automatically try to
				// search for service and assuming service will be found
				// after search, and will be in the ready state
				if(input.eq("On") && phase=="Off"){
					holdIn("On", timeUnit);
				}
				else if(input.eq("On")){
					holdIn("On", INFINITY);
				}  // whenever off is input, it will automatically turn off
				// no matter what the previous phase was
				else if(input.eq("Off")){
					holdIn("Off",INFINITY);
				}
			}
			// if a message is passed on to the on port of the inPort
			// do the following
			else if(messageOnPort(x,"incomingVoice",i)||recieveIV==true){
				input=x.getValOnPort("incomingVoice", i);
				recieveIV=true;
				// the message will only be received if the phase is in ready
				if(phase=="Ready"){
					holdIn("Active",timeUnit);
				} // if in phase On, it will search for service and when ready
				// with go into state active but we cannot receive the call because
				// the incoming voice was received before there was service
				else if (phase=="On"){
					holdIn("SearchForService",(timeUnit*2));
				} // we have to deal with incoming calls when already on a call
				else if(phase=="Active"){
					timeLeft=sigma-e;
					if(timeLeft>0){
						holdIn("Active",0);
					} else {
						holdIn("Active",timeUnit);
					}
				}
			} else{
				if(phase=="SearchForService")
					holdIn("SearchForService", (timeUnit*2));
				else if(phase=="Ready")
				holdIn("Ready",INFINITY);
			}
		}
	}
	
	public void deltint(){
		if(phase=="SearchForService"){
			holdIn("Ready",INFINITY);
		}
		else if(phase=="On"){
			holdIn("SearchForService", (timeUnit*2));
		} else
		if(phase!="Off" || phase!="On"){
			if(phase=="Active"){
				if(timeLeft>0)
					holdIn("Active",timeLeft);
				else
					holdIn("Ready", INFINITY);
			}
			holdIn("Ready", INFINITY);
		} 
		else if(phase=="Off"){
			holdIn("Off", INFINITY);
		}
	}
	
	public message out(){
		message m = new message();
		String x;
		// message are sent out only when in phase active
		// and will return it to previous "ready" state
		// in order to continue to next message
		if(phase=="Active"){
			x=input.toString();
			x=x.substring(0,x.length()-1);
			content con = makeContent("outcomingVoice", new entity( x.toUpperCase() + "2"));
			m.add(con);
			recieveIV=false;
		}
		return m;
	}
	
	public void showState(){
		super.showState();
	}
	
	public String getTooltipText(){
		   return
		   super.getTooltipText();
		  }
}