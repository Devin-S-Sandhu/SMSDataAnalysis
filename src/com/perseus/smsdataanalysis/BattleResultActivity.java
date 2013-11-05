package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class BattleResultActivity extends Activity {
	private static final String LOG_TAG = "BattleResultActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_battle_result);
		
		Analyzer analyzer = new Analyzer(getApplicationContext());
		Intent intent = getIntent();
		
		// TODO use value in arrays.xml for analysis type
		String contacts = new StringBuilder()
		.append(intent.getStringExtra("contactOne"))
		.append(intent.getStringExtra("contactTwo")).toString();
		Log.d(LOG_TAG, contacts);
		Analyzer.Query query = analyzer.new Query(
				"SMS Frequency",
				"Received",
				"10-4-2013",
				"11-4-2013",
				contacts);
		
		ArrayList<Entry<String, Integer>> queryResult = analyzer
				.doQuery(query);
		Log.d(LOG_TAG, queryResult.toString());
		
		if (queryResult.size() >= 2) {
			Entry<String, Integer> contactOneData = queryResult.get(0);
			Entry<String, Integer> contactTwoData = queryResult.get(1);
			
			TextView winnerLabel = ((TextView) findViewById(R.id.winner_label));
			winnerLabel.setText(contactOneData.getValue() > contactTwoData.getValue() ? 
					contactOneData.getKey() : contactTwoData.getKey() + " " + winnerLabel.getText());
			
			((TextView) findViewById(R.id.friend_one_info)).setText(contactOneData.getKey() + ": " + 
					contactOneData.getValue() + " texts");
			((TextView) findViewById(R.id.friend_two_info)).setText(contactTwoData.getKey() + ": " + 
					contactTwoData.getValue() + " texts");
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
