
package com.perseus.smsdataanalysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class AnalysisResultActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private final static String LOG_TAG = "AnalysisResultActivity";
	private TextView analysisType;
	private TextView startDate;
	private TextView endDate;
	private TextView contacts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_result);
		Log.v(LOG_TAG, "A verbose message");

		this.setTitle("Friend Battle Menu");

		analysisType = (TextView) findViewById(R.id.analysis_type);
		startDate = (TextView) findViewById(R.id.start_date);
		endDate = (TextView) findViewById(R.id.end_date);
		contacts = (TextView) findViewById(R.id.contacts);
		
		Intent intent = getIntent();

		analysisType.setText(intent.getStringExtra("type"));
		startDate.setText(intent.getStringExtra("start_date"));
		endDate.setText(intent.getStringExtra("end_date"));
		contacts.setText(intent.getStringExtra("contacts"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}