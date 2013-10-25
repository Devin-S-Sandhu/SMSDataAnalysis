package com.perseus.smsdataanalysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class AnalysisMenuActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private final static String LOG_TAG = "MyFirstApp_tag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_menu);
		Log.v(LOG_TAG, "A verbose message");

		this.setTitle("Data Analysis Menu");
		
		//Intent intent = getIntent();
		//String value = intent.getStringExtra("key"); //if it's a string you stored.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}