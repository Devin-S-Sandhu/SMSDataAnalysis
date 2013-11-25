package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class BattleResultActivity extends Activity {
	private static final String LOG_TAG = "BattleResultActivity";
	
	private final int CURR_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private final int CURR_MONTH = Calendar.getInstance().get(Calendar.MONTH);
    private final int CURR_DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    private HashMap<String, Integer> timeSpans;
    private String timeSpan;
    
    private Date startDate;
    private Date endDate;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_battle_result);
		
		Analyzer analyzer = new Analyzer(getApplicationContext());
		Intent intent = getIntent();
		
		timeSpans = new HashMap<String, Integer>();
		String[] timeSpanArray = getApplicationContext().getResources().getStringArray(R.array.time_span_array);
		for (int i = 0; i < timeSpanArray.length; i++) {
			timeSpans.put(timeSpanArray[i], i);
		}
		timeSpan = intent.getStringExtra("timeSpan");
		
		endDate = new Date(CURR_YEAR, CURR_MONTH, CURR_DAY);
		Calendar c = Calendar.getInstance();
		c.setTime(endDate);
		switch (timeSpans.get(timeSpan)) {
		case 0:
			c.add(Calendar.MONTH, -1);
			break;
		case 1:
			c.add(Calendar.YEAR, -1);
			break;
		default:
			c.add(Calendar.YEAR, -30);
			break;
		}
		startDate = c.getTime();
		
		// TODO use value in arrays.xml for analysis type
		String contacts = new StringBuilder()
		.append(intent.getStringExtra("contactOne"))
		.append(intent.getStringExtra("contactTwo")).toString();
		Log.d(LOG_TAG, contacts);
		Analyzer.Query query = analyzer.new Query(
				"SMS Frequency",
				"Received",
				startDate.getMonth() + "-" +
				startDate.getDay() + "-" +
				startDate.getYear(),
				endDate.getMonth() + "-" +
				endDate.getDay() + "-" +
				endDate.getYear(),
				contacts);
		
		ArrayList<Analyzer.Pair<String, Integer>> queryResult = analyzer
				.doQuery(query);
		Log.d(LOG_TAG, queryResult.toString());
		
		if (queryResult.size() >= 2) {
			Analyzer.Pair<String, Integer> contactOneData = queryResult.get(0);
			Analyzer.Pair<String, Integer> contactTwoData = queryResult.get(1);
			
			TextView winnerLabel = ((TextView) findViewById(R.id.winner_label));
			String winner = contactOneData.getElement1() > contactTwoData.getElement1() ? 
					contactOneData.getElement0() : contactTwoData.getElement0();
			winnerLabel.setText(winner + " Wins!");
			
			((TextView) findViewById(R.id.friend_one_info)).setText(contactOneData.getElement0() + ": " + 
					contactOneData.getElement1() + " texts");
			((TextView) findViewById(R.id.friend_two_info)).setText(contactTwoData.getElement0() + ": " + 
					contactTwoData.getElement1() + " texts");
		}
		else {
			Log.wtf(LOG_TAG, "lol wut: " + queryResult.size());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.battle_result, menu);
		return true;
	}

}
