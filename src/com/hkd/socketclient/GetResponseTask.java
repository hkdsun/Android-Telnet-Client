package com.hkd.socketclient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class GetResponseTask extends AsyncTask<Object, Void, String> {
	Activity main;
	
	public GetResponseTask(Activity activity){
		this.main = activity;
	}

	@Override
	protected String doInBackground(Object... params) {
		try {
			Telnet client = (Telnet) params[0];
			BufferedInputStream instr = client.getStream();
			String cmd = (String) params[1];
			
			
			
			int len=instr.available();
			byte[] buff = new byte[1024];
			int ret_read = 0;
			
			instr.read(buff,0,len);
			
			client.sendCommand(cmd);
			publishProgress();
			try {
				//Need to implement listener ASAP
				Thread.sleep(300);
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

	@Override
	protected void onProgressUpdate(Void... values) {
		((MainActivity) main).appendToConsole("Getting response from server...please wait\n");
		super.onProgressUpdate(values);
	}

}
