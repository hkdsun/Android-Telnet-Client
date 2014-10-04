package com.hkd.socketclient;

import java.util.regex.*;

import android.util.Log;

public class PioneerController{
	private Telnet pioneerclient;
	
	public PioneerController(Telnet telnetclient){
		pioneerclient = telnetclient;
	}
	
	public boolean changeVolume(int newVal) {
		PioneerVolume vol = new PioneerVolume(newVal);
		
		Pattern pattern = Pattern.compile("VOL(.*?)");
		
		Matcher matcher = pattern.matcher(pioneerclient.getResponse("?V", 1));
		
		if(matcher.find()){
			Log.i("matcher group 1",matcher.group(1));
			if(999>vol.pioneerVolume)
				return decreaseVolume(vol);
			else
				return increaseVolume(vol);
		}
		else
			return false;
		//while(pioneerclient.expectResponse(cmd, str, timeout))
	}
	
	//TODO implement a universal getStateOf(ENUM) function
	
	public boolean togglePower() {
//		pioneerclient.sendCommand("PF");
		if(pioneerclient.getResponse("?P",1).contains("PWR0"))
			if(pioneerclient.expectResponse("PF","PWR0", 1)) // power off
				return true;
		else if(pioneerclient.getResponse("?P",1).contains("PWR2"))
			if(pioneerclient.expectResponse("PO","PWR2", 1)) // power off
				return true;
		
		return false;
		
	}
	
	private boolean increaseVolume(PioneerVolume newVal){
		
		while(pioneerclient.expectResponse("VU", String.format("VOL%03d", newVal.pioneerVolume), 1)){
		}
		if(pioneerclient.getResponse("?V",1).contains(String.format("VOL%03d", newVal.pioneerVolume)))
			return true;
		
		return false;
		
		
	}
	
	private boolean decreaseVolume(PioneerVolume newVal){

		while(pioneerclient.expectResponse("VD", String.format("VOL%03d", newVal.pioneerVolume), 1)){
		}
		if(pioneerclient.getResponse("?V",1).contains(String.format("VOL%03d", newVal.pioneerVolume)))
			return true;
		
		return false;
		
	}
	
	public class PioneerVolume implements Comparable<PioneerVolume>{
		public int pioneerVolume;
		
		public PioneerVolume(int humanValue){
			pioneerVolume = (int) (humanValue*2+1);
		}

		@Override
		public int compareTo(PioneerVolume another) {
			return this.pioneerVolume - another.pioneerVolume;
		}
		
		

		
	}
}
