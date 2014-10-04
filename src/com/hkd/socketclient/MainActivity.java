package com.hkd.socketclient;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.SimpleOptionHandler;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.io.*;

import java.util.Locale;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	TelnetClient client = null;

	private Toast fastToast;
	private static int SERVERPORT = 23;
	private static String SERVER_IP = "192.168.0.105";

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		

		fastToast = Toast.makeText(this,"", Toast.LENGTH_SHORT);
	}
	
	@Override
	protected void onStop() {
		if (client!=null && client.isConnected()) {
			try {
				client.disconnect();
				Log.i("Socket", "Disconnected from server");
			} catch (IOException e) {
				Log.e("Socket", "Error disconnecting from server");
				e.printStackTrace();
			}
		}
		super.onStop();
	}

	public void onClickConnect(View view){
		EditText etIp = (EditText) findViewById(R.id.EditTextIp);
		//EditText etPort = (EditText) findViewById(R.id.EditTextPort);
		
		if(!etIsEmpty(etIp)){	
			String tmp = etIp.getText().toString();
			
			if(tmp.contains(":")){
				String[] address = tmp.split(":");
				SERVER_IP = address[0];
				SERVERPORT = Integer.parseInt(address[1]);
			}
			else{
				SERVER_IP = etIp.getText().toString();
			}
		}
		else 
			toastFast("Enter a server IP");
		
		if(client!=null && client.isConnected()) 
			toastFast("Already connected");
		else 
			new Thread(new ClientThread()).start();
		
		return;
	}
	
	public void onClickDisconnect(View view){
		if (client!=null && client.isConnected()) {
			try {
				client.disconnect();
				toastFast("Disconnected from server");
			} catch (IOException e) {
				Log.e("Socket", "Error disconnecting from server");
				e.printStackTrace();
			}
		}
		else{
			toastFast("Already disconnected");
		}
		return;
	}
	
	public void onClickSend(View view) {
		
		if(client==null || !client.isConnected()){
			toastFast("Not connected to a server");			
			return;
		}
		
		EditText et = (EditText) findViewById(R.id.EditTextCommand);
		
		StringBuilder stringBuilder = new StringBuilder();
		
		
		
		stringBuilder.append(et.getText().toString().toUpperCase(Locale.ENGLISH));
		stringBuilder.append("\n\r");
		
		byte[] cmd = stringBuilder.toString().getBytes();
		
		OutputStream outstr = client.getOutputStream();
		Log.i("command",(new String(cmd, 0, cmd.length)));
		
		try {
			outstr.write(cmd, 0, cmd.length);
			outstr.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		
	}
	
	

	private TelnetClient connectToServer(final String ip,final int port) throws IOException{
		TelnetClient tc = null;
		tc = new TelnetClient();

		if(ip == null){
			toastFast("Enter the server address");
			return null;
		}
		
        try {
			tc.connect(ip, port);
			this.runOnUiThread(new Runnable() {
			    public void run() {
					toastFast(String.format("Connected to %s,%d", ip,port));
					new ReaderThreadTask().execute();
			    }
			});
			return tc;
        } catch	(SocketException ex) {
        	toastFast("Connection error...");
        	throw new SocketException("Connection error...");
        } catch (IOException ex) {
        	toastFast("Connection error...");
        	throw new IOException("Connection error..."); // try next port
        }
        
        
		
        		
	}
	
	private void toastFast(String str) {
		// TODO Auto-generated method stub
		Context context = getApplicationContext();
		CharSequence text = str;
		fastToast.setText(str);
		fastToast.show();
	}

	private boolean etIsEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }
	
	
	class ClientThread implements Runnable {

		@Override
		public void run() {
			
			try {
				client = connectToServer(SERVER_IP, SERVERPORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			
			
		}

	}
	
	
	private class ReaderThreadTask extends AsyncTask<Void,String,Void > {
		InputStream instr = client.getInputStream();
		TextView et = (TextView) findViewById(R.id.inputStreamTextView);

		@Override
	    protected void onPreExecute() {
	    	
			et.setText("Connected...\n");
			et.setMovementMethod(new ScrollingMovementMethod());
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
	                ret_read = instr.read(buff);
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
	    	et.append(values[0]);
        	Log.i("Telnet InputStream", values[0]);
			super.onProgressUpdate(values);
		}

		protected void onPostExecute(Void result) {
	        et.setText("Disconnected");
	    }
	}
	
	
	
	
	
}