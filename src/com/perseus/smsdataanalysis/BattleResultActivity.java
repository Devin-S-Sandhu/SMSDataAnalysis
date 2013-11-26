package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.perseus.smsdataanalysis.Analyzer.Pair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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
    
    private Analyzer mAnalyzer;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_battle_result);
		
		mAnalyzer = new Analyzer(getApplicationContext());
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
		
		// create queries
		Analyzer.Query query1 = mAnalyzer.new Query(
				"SMS Frequency",
				"Received",
				startDate.getMonth() + "-" +
				startDate.getDay() + "-" +
				startDate.getYear(),
				endDate.getMonth() + "-" +
				endDate.getDay() + "-" +
				endDate.getYear(),
				contacts);
		
		Analyzer.Query query2 = mAnalyzer.new Query(
				"SMS Frequency",
				"Sent",
				startDate.getMonth() + "-" +
				startDate.getDay() + "-" +
				startDate.getYear(),
				endDate.getMonth() + "-" +
				endDate.getDay() + "-" +
				endDate.getYear(),
				contacts);
		
		new BattleTask().execute(query1, query2);
	}
	
	private class BattleTask extends AsyncTask<Analyzer.Query, Void, ArrayList<ArrayList<Analyzer.Pair<String, Integer>>>> {
		ProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(BattleResultActivity.this);
			mProgressDialog.setTitle("Friend Battle");
			mProgressDialog.setMessage(getResources().getString(
					R.string.spinner_message));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.show();
		}

		@Override
		protected ArrayList<ArrayList<Analyzer.Pair<String, Integer>>> doInBackground(Analyzer.Query... params) {
			ArrayList<ArrayList<Analyzer.Pair<String, Integer>>> result = new ArrayList<ArrayList<Analyzer.Pair<String, Integer>>>();
			result.add(mAnalyzer.doQuery(params[0]));
			result.add(mAnalyzer.doQuery(params[1]));
			return result;
		}
		
		@Override
		protected void onPostExecute(ArrayList<ArrayList<Analyzer.Pair<String, Integer>>> result) {
			Analyzer.Pair<String, Integer> contactOneData, contactTwoData;
			String contactOneName = result.get(0).get(0).getElement0(), contactTwoName = result.get(0).get(1).getElement0();
			int contactOneWins = 0, contactTwoWins = 0;
			
			for (ArrayList<Analyzer.Pair<String, Integer>> queryResult : result) {
				if (queryResult.size() < 2) {
					mProgressDialog.dismiss();
					return;
				}
				
				contactOneData = queryResult.get(0);
				contactTwoData = queryResult.get(1);
				
				if (contactOneData.getElement1() > contactTwoData.getElement1())
					contactOneWins++;
				else if (contactTwoData.getElement1() > contactOneData.getElement1())
					contactTwoWins++;
			}
			
			TextView winnerLabel = ((TextView) findViewById(R.id.winner_label));
			String winner = "It's a tie!";
			if(contactOneWins > contactTwoWins)
				winner = contactOneName + " Wins!";
			else if(contactTwoWins > contactOneWins)
				winner = contactTwoName + " Wins!";
			winnerLabel.setText(winner);
			
			((TextView) findViewById(R.id.friend_one_info)).setText(contactOneName + ": " + 
					contactOneWins + " wins");
			((TextView) findViewById(R.id.friend_two_info)).setText(contactTwoName + ": " + 
					contactTwoWins + " wins");
			
			mProgressDialog.dismiss();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.battle_result, menu);
		return true;
	}

}
