package com.hkd.socketclient;


import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;

import java.util.Locale;

import android.os.AsyncTask;
import android.util.Log;

public class Telnet {
	private TelnetClient client = null;
	private MainActivity main;
	private OutputStream outstream;
//	private ReaderThreadTask readerThread;
	private final String SERVER_IP;
	private final int SERVERPORT;

	public Telnet(MainActivity activity, String ip, int port) throws IOException{
		
		main = activity;
		SERVER_IP = ip;
		SERVERPORT = port;
		client = new TelnetClient();
		connect();
		
	}
	
	//TELNET
	private void connect(){
		ConnectTask connection = new ConnectTask();
		connection.execute();
	}
	
	private void connectToServer() throws IOException{
		client = new TelnetClient();

		if(SERVER_IP == null){
			main.toastFast("Enter the server address");
			return;
		}
		
        try {
			client.connect(SERVER_IP, SERVERPORT);
			main.runOnUiThread(new Runnable() {
			    public void run() {
					main.toastFast(String.format(Locale.ENGLISH, "Connected to %s,%d", SERVER_IP,SERVERPORT));
					main.setConsole(String.format("Connected to %s:%d\n",SERVER_IP,SERVERPORT));
			    }
			});
			
			
			return;
        } catch	(SocketException ex) {
        	main.toastFast("Connection error...");
        	throw new SocketException("Connection error...");
        } catch (IOException ex) {
        	main.toastFast("Connection error...");
        	throw new IOException("Connection error..."); // try next port
        }
        
        

        		
	}

	//TELNET HELPERS
	public boolean sendCommand(String cmd){
		if(client==null || !client.isConnected()){
			main.toastFast("Not connected to a server");			
			return false;
		}
		
		
		StringBuilder stringBuilder = new StringBuilder();
		
		
		
		stringBuilder.append(cmd.toUpperCase(Locale.ENGLISH));
		stringBuilder.append("\n\r");
		
		byte[] cmdbyte = stringBuilder.toString().getBytes();
		
		outstream = client.getOutputStream();
		Log.i("command",(new String(cmdbyte, 0, cmdbyte.length)));
		
		try {
			outstream.write(cmdbyte, 0, cmdbyte.length);
			outstream.flush();
			return true;
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		
	}
	
	
	public boolean expectResponse(String cmd, String str, int timeout){
		BufferedInputStream instr = (BufferedInputStream) client.getInputStream();
		boolean result = false;
		
		try {
			int len=instr.available();
			byte[] buff = new byte[1024];
			int ret_read = 0;
			
			sendCommand(cmd);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			len=instr.available();
			if(len>0){
				ret_read=instr.read(buff,0,len);	
			}
			if(ret_read>0){
				String res = new String(buff,0,ret_read);
				Log.i("readline", res);
				if(res.contains(str)){
					result = true;
				}
				else{
					result = false;
				}
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String getResponse(String cmd, int timeout){
		BufferedInputStream instr = (BufferedInputStream) client.getInputStream();

		
		try {
			int len=instr.available();
			byte[] buff = new byte[1024];
			int ret_read = 0;
			instr.read(buff,0,len);
			sendCommand(cmd);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			len=instr.available();
			if(len>0){
				ret_read=instr.read(buff,0,len);	
			}
			if(ret_read>0){
				String res = new String(buff,0,ret_read);
				Log.i("readline", res);
					
				return res;
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}


	public boolean isConnected() {
		return client.isConnected();
	}

	//exits telnet session and cleans up the telnet console
	public boolean disconnect() {
		
		try {
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
//		readerThread.cancel(true);
		main.setConsole("Disconnected...");
		return true;

	}
	
	//THREADS		
	private class ConnectTask extends AsyncTask<Void,Void,Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				connectToServer();
			} catch (IOException e) {
				e.printStackTrace();
			}

	        return null;
		}
	}


	
}
