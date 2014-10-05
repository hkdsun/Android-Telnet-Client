package com.hkd.socketclient;

import java.util.regex.*;

import android.app.Activity;

public class PioneerController{
	private Telnet pioneerclient;
	private MainActivity main;
	
	public PioneerController(Activity mainActivity,Telnet telnetclient){
		pioneerclient = telnetclient;
		main = (MainActivity) mainActivity;
	}
	
	public int getVolume(){
		
		String status = pioneerclient.getResponse("?V", 1);
		
		Pattern pattern = Pattern.compile("VOL(.*?)");
		
		Matcher matcher = pattern.matcher(status);
		
		status = matcher.replaceFirst("");
		
		status = status.replace("\r\n", "");
		status = status.replace(" ", "");
		
		int curvol = Integer.parseInt(status);
		
		return curvol;
		
	}
	
	public boolean pioneerIsOn(){
		
		String status = pioneerclient.getResponse("?P", 1);
		
		status = status.replace("\r\n", "");
		status = status.replace(" ", "");
		
		if(status.contains("PWR0")) 
			return true;
		else 
			return false;		
	}
	
	public boolean changeVolume(int newVal) {
		PioneerVolume vol = new PioneerVolume(newVal);
		main.appendToConsole("Attempting to change volume..please wait\n");
		if(getVolume()>vol.pioneerVolume)
			return decreaseVolume(vol);
		else
			return increaseVolume(vol);
	}
	
	//TODO implement a universal getStateOf(ENUM) function
	
	public boolean togglePower() {
		if(pioneerclient.expectResponse("?P","PWR0",1))
			return pioneerclient.expectResponse("PF","PWR2",1);
		else if(pioneerclient.expectResponse("?P","PWR2",1))
			return pioneerclient.expectResponse("PO","PWR0",1);
		return false;
		
	}
	
	private boolean increaseVolume(PioneerVolume newVal){
		boolean end = false;
		while(!end){
			end = pioneerclient.expectResponse("VU",String.format("VOL%03d", newVal.pioneerVolume), 1);
		}
		main.appendToConsole(String.format("Successfully set volume to %d\n", newVal.pioneerVolume/2-1));

		return true;
		
		
	}
	
	private boolean decreaseVolume(PioneerVolume newVal){
		boolean end = false;
		while(!end){
			end = pioneerclient.expectResponse("VD",String.format("VOL%03d", newVal.pioneerVolume), 1);
		}
		main.appendToConsole(String.format("Successfully set volume to %d\n", newVal.pioneerVolume/2-1));
	
		return true;
		
	}
	
	public void play(){
		pioneerclient.sendCommand("30PB");
		
		return;
	}
	
	public void stop(){
		pioneerclient.sendCommand("20PB");
	}
	
	public void input(String func){
		int fn = -1;
		switch(func){
			case  "Xbox":
				fn = 4;
				break;
			case "Projector":
				fn = 6;
				break;
			case "Radio Paradise":
				fn = 45;
				break;
		}
		if(fn == 45){
			pioneerclient.sendCommand("45FN");
			main.appendToConsole("Please wait while Radio Paradise loads...\n");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			play();
		}
		else{
			pioneerclient.sendCommand(String.format("%02dFN", fn));
		}
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
