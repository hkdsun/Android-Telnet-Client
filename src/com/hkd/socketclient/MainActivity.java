package com.hkd.socketclient;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private TelnetClient client = null;
	private Toast fastToast;
	private static TextView et;
	private static NumberPicker numpicker;
	private static int SERVERPORT = 23;
	private static String SERVER_IP = "192.168.0.105";
	private static final int MIN_VOL = 0;
	private static final int MAX_VOL = 80;	

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }
		

}