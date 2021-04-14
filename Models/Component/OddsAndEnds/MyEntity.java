package Component.OddsAndEnds;

import Component.BasicProcessor.*;
import GenCol.entity;

public class MyEntity extends entity {
	
	int callDuration;
	
	MyEntity(String nm, int duration){
		super.name = nm;
		callDuration = duration;
	}
	
	public int getCallDuration() {
		return callDuration;
	}
	
}
