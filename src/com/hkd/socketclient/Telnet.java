package com.hkd.socketclient;


import java.io.InputStream;
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
	private InputStream instream;
	private OutputStream outstream;
	private ReaderThreadTask readerThread;
	private final String SERVER_IP;
	private final int SERVERPORT;
	
	public Telnet(MainActivity activity, String ip, int port) throws IOException{
		
		main = activity;
		SERVER_IP = ip;
		SERVERPORT = port;
		new ConnectTask().execute();		
		
	}
	
	//TELNET
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
			    	instream = client.getInputStream();
					main.toastFast(String.format(Locale.ENGLISH, "Connected to %s,%d", SERVER_IP,SERVERPORT));
					startReaderThread();
					//readerThread = new ReaderThreadTask();
					//readerThread.execute();
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

	protected void startReaderThread() {
		main.runOnUiThread(new Runnable() {
	    public void run() {
	    	readerThread = new ReaderThreadTask();
			readerThread.execute();
	    }
	});
		
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
	
	
	public String getResponse(String cmd, int timeout){
		readerThread.cancel(true); //pause reader thread
		
		//clear the buffer
		try {
			instream.skip(instream.available());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		sendCommand(cmd);
		boolean read_ok=false;
		byte buff[] = new byte[1024];
		int ret_read = 0;
		try {
			//TODO need to implement notifier
	        Thread.sleep(50);
	        
	        
	        ret_read = instream.read(buff);
	        if((ret_read >= 1))
	        {
	            read_ok = true;
	        }
			
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		startReaderThread(); //resume reader
		if(read_ok)
			return new String(buff,0,ret_read);
		else
			return "";
	}
	
	public boolean expectResponse(String cmd, String str, int timeout) {
		readerThread.cancel(true); //pause reader thread
		
		//clear the buffer
		try {
			instream.skip(instream.available());
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		sendCommand(cmd);
		boolean read_ok=false;
		boolean negotiation_ok=false;
		
		try {
			byte buff[] = new byte[1024];
	        //TODO need to implement notifier
			Thread.sleep(50);
	        int ret_read = 0;
	        
	        ret_read = instream.read(buff);
	        if((ret_read >= 1))
	        {
	            read_ok = true;
	        }
			
	        if(read_ok && (new String(buff,0,ret_read)).contains(str)) 
	        	negotiation_ok = true;
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		startReaderThread(); //resume reader
		
		return negotiation_ok;
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
		
		readerThread.cancel(true);
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
	
	
	private class ReaderThreadTask extends AsyncTask<Void,String,Void > {
		@Override
	    protected void onPreExecute() {
	    	
			main.setConsole("Connected...\n");
	    }
	    
		/* (non-Javadoc)
	     * @see android.os.AsyncTask#doInBackground(Params[])
	     */
	    @Override
	    protected Void doInBackground(Void... params) {
	    	try
	        {
	            byte[] buff = new byte[1024];
	            int ret_read = 0;

	            do
	            {
	                ret_read = instream.read(buff);
	                if(ret_read > 0)
	                {
	                	String s = new String(buff,0,ret_read);
	                	publishProgress(s);
	                	
	                	
	                }
	            }
	            while (ret_read >= 0);
	        }
	        catch (IOException e)
	        {
	            System.err.println("Exception while reading socket:" + e.getMessage());
	        }
			return null;
			
	    }
	    
	    

	    protected void onProgressUpdate(String... values) {
	    	main.appendToConsole(values[0]);
        	Log.i("Telnet InputStream", values[0]);
			super.onProgressUpdate(values);
		}

		protected void onPostExecute(Void result) {
	        main.setConsole("Disconnected");
	    }
	}


	
}
