package com.hkd.socketclient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.*;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class PioneerController{
	private Telnet pioneerclient;
	private MainActivity main;
	protected static final String TAG = "PioneerController";
	private int volume=-1;

	
	public PioneerController(Activity mainActivity,Telnet telnetclient){
		pioneerclient = telnetclient;
		main = (MainActivity) mainActivity;
	}
	
	public int getVolume(){
		Log.d("GetVolume", "start getting volume");
		AsyncTask<Void,Void,Integer> task = new AsyncTask<Void,Void,Integer>(){
		GetResponseTask response = pioneerclient.getResponse(main);
			protected void onPreExecute(){
				main.setConsole("getting volume\n");
				
				response.execute(pioneerclient,"?V");
			}
			
			@Override
			protected Integer doInBackground(Void... params) {
				String status;
				try {
					status = response.get(5000,TimeUnit.MILLISECONDS);
				} catch (InterruptedException | ExecutionException
						| TimeoutException e) {
					volume = -1;
					e.printStackTrace();
					return volume;
				}
				
				
				
				Pattern pattern = Pattern.compile("VOL(.*?)");
				
				Matcher matcher = pattern.matcher(status);
				
				status = matcher.replaceFirst("");
				
				status = status.replace("\r\n", "");
				status = status.replace(" ", "");
				if(isNumeric(status)){
					volume = Integer.parseInt(status);
				}
				else{
					volume = -1;
				}
				return volume;
			}
			
			protected void onPostExecute(Integer result){
				if(result > -1){
					main.appendToConsole("Volume is at " + volume + "\n");
				}
				else{
					main.appendToConsole("Error getting volume\n");
				}
			}
			
		};
		
		task.execute();
		
		return volume;
		
		
		
		
		
	}
	
	public boolean pioneerIsOn(boolean block){
		
		boolean blockresult;
		Log.d("pioneerIsOn", "start power transaction");
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void,Void,Boolean>(){
		GetResponseTask response = pioneerclient.getResponse(main);
			protected void onPreExecute(){
				main.setConsole("getting power\n");
				
				response.execute(pioneerclient,"?P");
			}
			
			@Override
			protected Boolean doInBackground(Void... params) {
				String status;

				try {
					status = response.get(5000,TimeUnit.MILLISECONDS);
				} catch (InterruptedException | ExecutionException
						| TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				status = status.replace("\r\n", "");
				status = status.replace(" ", "");
				
				if(status.contains("PWR0")) 
					return true;
				else 
					return false;
			}
			
			protected void onPostExecute(Boolean result){
				main.setPower(result);
				
			}
			
		};
		
		task.execute();
		
		if(block){
			try {
				task.get(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		main.setConsole("Didn't receive power response in time");
		return false;
				
				
	}
	
	public boolean changeVolume(int newVal) {
		PioneerVolume vol = new PioneerVolume(newVal);
		main.appendToConsole("Attempting to change volume..please wait\n");
		getVolume();		
		if(volume>vol.pioneerVolume)
			//return decreaseVolume(vol);
			return false;
		else
			//return increaseVolume(vol);
			return  false;
	}
	
	//TODO implement a universal getStateOf(ENUM) function
	
	public boolean togglePower() {
		
		AsyncTask<Void, Void, Boolean> expecttask = new AsyncTask<Void,Void,Boolean>(){
			GetResponseTask response = pioneerclient.getResponse(main);

			@Override
			protected Boolean doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		GetResponseAsync responsetask = new GetResponseAsync("?P", 1000);
		
		if(responsetask.execute().get()){
			//Turn off
			expecttask.execute(params);
		}
		else{
			//Turn on
			expecttask.execute(params);
		}
		
		
		if(pioneerclient.expectResponse("?P","PWR0",1))
			return pioneerclient.expectResponse("PF","PWR2",1);
		else if(pioneerclient.expectResponse("?P","PWR2",1))
			return pioneerclient.expectResponse("PO","PWR0",1);
		return false;
		
	}
	
	/* increase/decrease volume
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
	*/
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
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
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

	private class  GetResponseAsync extends AsyncTask<Void,Void,String>{
		final String cmd;
		final int timeout;
		GetResponseTask response;
		
		public GetResponseAsync(String cmd, int timeout){
			this.cmd = cmd;
			this.timeout = timeout;
			response = pioneerclient.getResponse(main);
		}
		
		
		protected void onPreExecute(){			
			response.execute(pioneerclient,cmd);
		}
		
		@Override
		protected String doInBackground(Void... params) {
			String result;

			try {
				result = response.get(timeout,TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			result = result.replace("\r\n", "");
			result = result.replace(" ", "");
			
			return result;
		}
		
		

	}
}

