package com.perseus.smsdataanalysis;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class AnalysisResultActivity extends Activity {
	private final static String LOG_TAG = "AnalysisResultActivity";
	private TextView analysisType;
	private TextView startDate;
	private TextView endDate;
	private TextView contacts;
	private TextView result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_result);
		Log.v(LOG_TAG, "A verbose message");
		Analyzer mAnalyzer = new Analyzer(getApplicationContext());

		this.setTitle("Data Analysis Result");

		analysisType = (TextView) findViewById(R.id.analysis_type);
		startDate = (TextView) findViewById(R.id.start_date);
		endDate = (TextView) findViewById(R.id.end_date);
		contacts = (TextView) findViewById(R.id.contacts);
		result = (TextView) findViewById(R.id.text_result);

		Intent intent = getIntent();

		Analyzer.Query query = mAnalyzer.new Query(
				intent.getStringExtra("type"),
				intent.getStringExtra("start_date"),
				intent.getStringExtra("end_date"),
				intent.getStringExtra("contacts"));

		ArrayList<String> queryResult = mAnalyzer.doQuery(query);

		analysisType.setText(intent.getStringExtra("type"));
		startDate.setText(intent.getStringExtra("start_date"));
		endDate.setText(intent.getStringExtra("end_date"));
		contacts.setText(intent.getStringExtra("contacts"));
		result.setText(TextUtils.join("\n", queryResult));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}