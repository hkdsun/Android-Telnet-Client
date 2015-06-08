package com.hkd.socketclient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import static java.lang.Thread.sleep;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.*;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class PioneerController{
	private TelnetClient pioneerclient;
	protected static final String TAG = "PioneerController";
	volatile private int volume=-1;
    private boolean power=false;
    private boolean changingVolume=false;

	public PioneerController(TelnetClient telnetclient) {
        pioneerclient = telnetclient;
        Runnable r = statusRunnable();
        Thread statusThread = new Thread(r);
        statusThread.start();
    }

    public int getVolume(){
        return volume;
    }
	
	public boolean pioneerIsOn(){
        return power;
	}
	
	public void changeVolume(int newVal) {
		if(volume == newVal || changingVolume)
			return;
		else if(volume>newVal)
			decreaseVolume(newVal);
		else
			increaseVolume(newVal);
	}
	
	//TODO implement a universal getStateOf(ENUM) function

    /**
     * @return the state _after_ toggling the power
     */
	public boolean togglePower() {
        if(power) {
            pioneerclient.sendCommand("PF");
            return false;
        } else {
            pioneerclient.sendCommand("PO");
            return true;
        }
	}

	private void increaseVolume(final int humanVol){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                changingVolume = true;
                try {
                    try {
                        pioneerclient.sendUntilResponse("VU",200,new ResponseTest() {
                            @Override
                            public Boolean test(String str) {
                                Pattern pattern = Pattern.compile("VOL(.*?)");
                                Matcher matcher = pattern.matcher(str);
                                if(matcher.matches()) {
                                    System.out.println("str is " + str);
                                    str = matcher.replaceFirst("");
                                    str = str.replace("\r\n", "");
                                    str = str.replace(" ", "");
                                    int vol = Integer.parseInt(str);
                                    volume = humanVolume(vol);
                                    return vol > rawVolume(humanVol);
                                } else {
                                    return false;
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    changingVolume = false;
                    e.printStackTrace();
                }
                changingVolume = false;
            }
        };
        new Thread(r).start();
	}
	
	private void decreaseVolume(final int humanVol){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                changingVolume = true;
                try {
                    pioneerclient.sendUntilResponse("VD",200,new ResponseTest() {
                        @Override
                        public Boolean test(String str) {
                            Pattern pattern = Pattern.compile("VOL(.*?)");
                            Matcher matcher = pattern.matcher(str);
                            if(matcher.matches()) {
                                str = matcher.replaceFirst("");
                                str = str.replace("\r\n", "");
                                str = str.replace(" ", "");
                                int vol = Integer.parseInt(str);
                                volume = humanVolume(vol);
                                System.out.println("K got it");
                                return vol < rawVolume(humanVol);
                            } else {
                                return false;
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    changingVolume = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                changingVolume = false;
            }
        };
        new Thread(r).start();
	}

    private int rawVolume(int v){
        return v*2+1;
    }

    private int humanVolume(int v){
        return (v-1)/2;
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

    private Runnable statusRunnable(){
        return new Runnable() {
            @Override
            public void run() {
                while(true){
                    String status;
                    try {
                        //Get volume
                        status = pioneerclient.getResponse("?VOL");
                        Pattern pattern = Pattern.compile("VOL(.*?)");
                        Matcher matcher = pattern.matcher(status);
                        if(matcher.matches()) {
                            status = matcher.replaceFirst("");
                            status = status.replace("\r\n", "");
                            status = status.replace(" ", "");
                            System.out.println(status);
                            volume = humanVolume(Integer.parseInt(status));
                        }
                        //Get power status
                        status = pioneerclient.getResponse("?PWR");
                        if(status.contains("PWR2"))
                            power = false;
                        else if(status.contains("PWR0"))
                            power = true;

                        //Give her some time
                        sleep(1000);
                    } catch (IOException | InterruptedException e) {
                        Log.e(TAG,"Status thread timed out");
                        System.out.println("c");
                    }
                }
            }
        };
    }
}

