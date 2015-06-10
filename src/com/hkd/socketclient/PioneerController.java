package com.hkd.socketclient;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class PioneerController{
	private TelnetClient pioneerclient;
	protected static final String TAG = "PioneerController";
	volatile private int volume=-1;
    volatile private boolean power=false;
    private AtomicBoolean changingVolume = new AtomicBoolean(false);
    private Thread statusThread;
    private MainActivity context;
    private boolean dirtyvol = false;
    private int COMMAND_SPEED = 250;

    public PioneerController(String ip, int port, MainActivity con) throws IOException, InterruptedException {
        TelnetClient connection = null;
        try {
            connection = new TelnetClient(ip, port);
        } catch (IOException e) {
            Log.e(TAG,"Could not establish connection with server");
            e.printStackTrace();
        }
        pioneerclient = connection;
        Runnable r = statusRunnable();
        statusThread = new Thread(r);
        statusThread.start();
        context = con;
        final InputStreamReader a = pioneerclient.spawnSpy();
        final BufferedReader reader = new BufferedReader(a);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()){
                        final String line = reader.readLine();
                        if(line != null) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    context.appendToConsole(line);
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public int getVolume(){
        return volume;
    }
	
	public boolean pioneerIsOn(){
        return power;
	}
	
	public void changeVolume(int newVal) {
		if(volume>newVal)
			decreaseVolume(newVal);
		else
			increaseVolume(newVal);
        dirtyvol=false;
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
                changingVolume.set(true);
                final int[] vol = {0};
                try {
                    try {
                        pioneerclient.sendUntilResponse("VU",COMMAND_SPEED,new ResponseTest() {
                            @Override
                            public Boolean test(String str) {
                                Pattern pattern = Pattern.compile("VOL(.*?)");
                                Matcher matcher = pattern.matcher(str);
                                if(matcher.matches()) {
                                    str = matcher.replaceFirst("");
                                    str = str.replace("\r\n", "");
                                    str = str.replace(" ", "");
                                    vol[0] = BaseTools.getIntOrElse(str, vol[0]);
                                    return vol[0] > rawVolume(humanVol);
                                } else {
                                    return false;
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    changingVolume.set(false);
                    e.printStackTrace();
                }
                changingVolume.set(false);
            }
        };
        new Thread(r).start();
	}
	
	private void decreaseVolume(final int humanVol){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                changingVolume.set(false);
                final int[] vol = {0};
                try {
                    pioneerclient.sendUntilResponse("VD",COMMAND_SPEED,new ResponseTest() {
                        @Override
                        public Boolean test(String str) {
                            Pattern pattern = Pattern.compile("VOL(.*?)");
                            Matcher matcher = pattern.matcher(str);
                            if(matcher.matches()) {
                                str = matcher.replaceFirst("");
                                str = str.replace("\r\n", "");
                                str = str.replace(" ", "");
                                vol[0] = BaseTools.getIntOrElse(str, vol[0]);
                                return vol[0] < rawVolume(humanVol);
                            } else {
                                return false;
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    changingVolume.set(false);
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                changingVolume.set(false);
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
                        if(!changingVolume.get()) {
                            //Get volume
                            status = pioneerclient.getResponse("?VOL");
                            Pattern pattern = Pattern.compile("VOL(.*?)");
                            Matcher matcher = pattern.matcher(status);
                            if (matcher.matches()) {
                                status = matcher.replaceFirst("");
                                status = status.replace("\r\n", "");
                                status = status.replace(" ", "");
                                volume = humanVolume(BaseTools.getIntOrElse(status, volume));
                            }
                            //Get power status
                            status = pioneerclient.getResponse("?PWR");
                            if (status.contains("PWR2"))
                                power = false;
                            else if (status.contains("PWR0"))
                                power = true;
                            final StringBuilder str = new StringBuilder();
                            str.append("Volume set to: ");
                            str.append(volume);
                            str.append("\nPower is: ");
                            if (power)
                                str.append("ON");
                            else
                                str.append("OFF");
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!dirtyvol) {
                                        context.setVolume(volume);
                                        dirtyvol = true;
                                    }
                                    context.setPower(power);
                                    context.setStatus(str.toString());
                                }
                            });
                        }
                        sleep(1000);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG,"Status thread timed out");
                        break;
                    }
                }
            }
        };
    }

    public boolean isConnected() {
        return pioneerclient.isConnected();
    }

    public boolean disconnect() {
        statusThread.interrupt();
        return pioneerclient.disconnect();
    }

    public void sendCommand(String s) {
        pioneerclient.sendCommand(s);
    }
}

