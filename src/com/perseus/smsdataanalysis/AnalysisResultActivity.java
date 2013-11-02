package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

public class AnalysisResultActivity extends Activity {
	private final static String LOG_TAG = "AnalysisResultActivity";
	private TextView analysisType;
	private TextView startDate;
	private TextView endDate;
	private TextView contacts;
	//private TextView result;

	private PieChart pie;

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
		//result = (TextView) findViewById(R.id.text_result);

		Intent intent = getIntent();

		Analyzer.Query query = mAnalyzer.new Query(
				intent.getStringExtra("type"),
				intent.getStringExtra("start_date"),
				intent.getStringExtra("end_date"),
				intent.getStringExtra("contacts"));

		ArrayList<Entry<String, Integer>> queryResult = mAnalyzer
				.doQuery(query);

		analysisType.setText(intent.getStringExtra("type"));
		startDate.setText(intent.getStringExtra("start_date"));
		endDate.setText(intent.getStringExtra("end_date"));
		contacts.setText(intent.getStringExtra("contacts"));
		//result.setText(TextUtils.join("\n", queryResult));

		// initialize our XYPlot reference:
		pie = (PieChart) findViewById(R.id.mySimplePieChart);

		int numSeg = queryResult.size();
		numSeg = (numSeg > 10) ? 10 : numSeg;
		Segment segments[] = new Segment[numSeg];
		SegmentFormatter sf[] = new SegmentFormatter[numSeg];

		EmbossMaskFilter emf = new EmbossMaskFilter(new float[] { 1, 1, 1 },
				0.4f, 10, 8.2f);

		Random rand = new Random();
		Log.v(LOG_TAG, "initalizing segemnts");
		for (int i = 0; i < numSeg; i++) {
			Entry<String, Integer> curr = queryResult.get(i);
			int count = curr.getValue();
			String label = curr.getKey();
			segments[i] = new Segment(label, count);
			int color = Color.argb(255, rand.nextInt(256), rand.nextInt(256),
					rand.nextInt(256));
			sf[i] = new SegmentFormatter(color);
			sf[i].getFillPaint().setMaskFilter(emf);

		}

		Log.v(LOG_TAG, "adding segemnts");
		for (int i = 0; i < numSeg; i++) {
			pie.addSeries(segments[i], sf[i]);
		}
		pie.getBorderPaint().setColor(Color.TRANSPARENT);
		pie.getBackgroundPaint().setColor(Color.TRANSPARENT);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}