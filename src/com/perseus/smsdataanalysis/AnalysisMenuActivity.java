package com.perseus.smsdataanalysis;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class AnalysisMenuActivity extends Activity {
	private final static String LOG_TAG = "AnalysisMenuActivity_tag";

	static final int START_DATE_DIALOG_ID = 0;
	static final int END_DATE_DIALOG_ID = 1;

	private static final int CURR_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	private static final int CURR_MONTH = Calendar.getInstance().get(Calendar.MONTH);
	private static final int CURR_DAY = Calendar.getInstance().get(
			Calendar.DAY_OF_MONTH);
	private static String TODAYSTR = new StringBuilder()
	.append(CURR_MONTH + 1).append("-").append(CURR_DAY).append("-")
	.append(CURR_YEAR).append(" ").toString();
	private static Date TODAY = new Date(CURR_YEAR, CURR_MONTH, CURR_DAY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_menu);
		this.setTitle("Data Analysis Menu");

	}

	public void advanced(View view){
		Intent myIntent = new Intent(AnalysisMenuActivity.this,
				AdvancedAnalysisMenuActivity.class);
		AnalysisMenuActivity.this.startActivity(myIntent);
	}
	
	private String aYearAgo(){
		Calendar c = Calendar.getInstance();
		c.setTime(TODAY);
		c.set(1980, 0, 1);
		Date newDate = c.getTime();
		return new StringBuilder().append(newDate.getMonth() + 1)
				.append("-").append(newDate.getDate()).append("-")
				.append(newDate.getYear()).append(" ").toString();
	}
	
	public void wordFreqSent(View view){
		Log.i(LOG_TAG, "What I said the most - clicked");
		Intent myIntent = new Intent(AnalysisMenuActivity.this,
				AnalysisResultActivity.class);
		myIntent.putExtra("info_dump", false);
		myIntent.putExtra("type", "Word Frequency");
		myIntent.putExtra("scope", "Sent");
		myIntent.putExtra("start_date", aYearAgo());
		myIntent.putExtra("end_date", TODAYSTR);
		myIntent.putExtra("contacts", new HashMap<String, String>());

		AnalysisMenuActivity.this.startActivity(myIntent);
	}

	public void smsFreqSent(View view){
		Intent myIntent = new Intent(AnalysisMenuActivity.this,
				AnalysisResultActivity.class);
		myIntent.putExtra("info_dump", false);
		myIntent.putExtra("type", "SMS Frequency");
		myIntent.putExtra("scope", "Sent");
		myIntent.putExtra("start_date", aYearAgo());
		myIntent.putExtra("end_date", TODAYSTR);
		myIntent.putExtra("contacts", new HashMap<String, String>());

		AnalysisMenuActivity.this.startActivity(myIntent);
		
	}
	public void smsFreqReceived(View view){
		Intent myIntent = new Intent(AnalysisMenuActivity.this,
				AnalysisResultActivity.class);
		myIntent.putExtra("info_dump", false);
		myIntent.putExtra("type", "SMS Frequency");
		myIntent.putExtra("scope", "Received");
		myIntent.putExtra("start_date", aYearAgo());
		myIntent.putExtra("end_date", TODAYSTR);
		myIntent.putExtra("contacts", new HashMap<String, String>());

		AnalysisMenuActivity.this.startActivity(myIntent);
		
	}
}