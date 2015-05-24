package com.hkd.socketclient;

import java.io.BufferedInputStream;
import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class ExpectResponseTask extends AsyncTask<Object, Void, Boolean> {
	Activity main;
	
	public ExpectResponseTask(Activity main){
		this.main = (MainActivity) main;
	}
	@Override
	protected Boolean doInBackground(Object... params) {
		TelnetConnection client = (TelnetConnection) params[0];
		String cmd = (String) params[1];
		String str = (String) params[2];
		BufferedInputStream instr = (BufferedInputStream) client.getStream();
		boolean result = false;
		
		try {
			int len=instr.available();
			byte[] buff = new byte[1024];
			int ret_read = 0;
			
			client.sendCommand(cmd);
			
			try {
				Thread.sleep(500);
				//TODO implement notifier ASAP
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

}
