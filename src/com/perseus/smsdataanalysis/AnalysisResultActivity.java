package com.perseus.smsdataanalysis;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.LineRegion;
import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.TextOrientationType;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.perseus.smsdataanalysis.Analyzer.Query;

public class AnalysisResultActivity extends Activity {
	private final static String LOG_TAG = "AnalysisResultActivity";
	private static final String NO_SELECTION_TXT = "Touch bar to select.";
	private final Random generator = new Random();

	private TextView analysisType;
	private TextView startDate;
	private TextView endDate;
	private TextView contacts;
	private TextView result;

	private ListView lv;

	private PieChart pie;
	private XYPlot plot;

	private MyBarFormatter selectionFormatter;

	private TextLabelWidget selectionWidget;

	private Pair<Integer, XYSeries> selection;

	private ArrayList<Entry<String, Integer>> queryResult;
	private Intent intent;
	private Analyzer mAnalyzer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_result);
		this.setTitle("Data Analysis Result");

		mAnalyzer = new Analyzer(getApplicationContext());
		intent = getIntent();
		Analyzer.Query query = mAnalyzer.new Query(
				intent.getStringExtra("type"), intent.getStringExtra("scope"),
				intent.getStringExtra("start_date"),
				intent.getStringExtra("end_date"),
				intent.getStringExtra("contacts"));

		Log.d(LOG_TAG, "before asnyc task");

		new AnalysisTask().execute(query);
	}

	private class AnalysisTask extends
			AsyncTask<Analyzer.Query, Void, ArrayList<Entry<String, Integer>>> {
		ProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(AnalysisResultActivity.this);
			mProgressDialog.setTitle(intent.getStringExtra("type"));
			mProgressDialog.setMessage(getResources().getString(
					R.string.spinner_message));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.show();
		}

		@Override
		protected ArrayList<Entry<String, Integer>> doInBackground(
				Query... params) {
			return mAnalyzer.doQuery(params[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<Entry<String, Integer>> result) {
			mProgressDialog.dismiss();
			Log.d(LOG_TAG, result.toString());
			queryResult = result;
			String resultDump = TextUtils.join("\n", queryResult);
			Log.d(LOG_TAG, "Result: " + resultDump);

			if (queryResult != null && !queryResult.isEmpty()) {
				lv = (ListView) findViewById(R.id.listView1);
				lv.setAdapter(new MyViewAdapter(getApplicationContext(),
						R.layout.listview_analysis_result_item, null));

				Toast.makeText(getApplicationContext(),
						"Scroll down for more graphs", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getApplicationContext(), "The result is empty",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class MyViewAdapter extends ArrayAdapter<View> {
		public MyViewAdapter(Context context, int resId, List<View> views) {
			super(context, resId, views);
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			LayoutInflater inf = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View v = convertView;
			if (v == null) {
				v = inf.inflate(R.layout.listview_analysis_result_item, parent,
						false);
			}

			analysisType = (TextView) v.findViewById(R.id.analysis_type);
			startDate = (TextView) v.findViewById(R.id.start_date);
			endDate = (TextView) v.findViewById(R.id.end_date);
			contacts = (TextView) v.findViewById(R.id.contacts);
			result = (TextView) v.findViewById(R.id.text_result);

			String type = intent.getStringExtra("type");
			analysisType.setText(type);
			startDate.setText(intent.getStringExtra("start_date"));
			endDate.setText(intent.getStringExtra("end_date"));
			contacts.setText(intent.getStringExtra("contacts"));
			result.setText(TextUtils.join("\n", queryResult));

			selectionFormatter = new MyBarFormatter(Color.YELLOW, Color.WHITE);
			pie = (PieChart) v.findViewById(R.id.mySimplePieChart);

			// initialize our XYPlot reference:
			plot = (XYPlot) v.findViewById(R.id.xyplot);

			selectionWidget = new TextLabelWidget(plot.getLayoutManager(),
					NO_SELECTION_TXT, new SizeMetrics(PixelUtils.dpToPix(100),
							SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(100),
							SizeLayoutType.ABSOLUTE),
					TextOrientationType.HORIZONTAL);

			selectionWidget.getLabelPaint().setTextSize(PixelUtils.dpToPix(16));

			// add a dark, semi-transparent background to the selection label
			// widget:
			Paint p = new Paint();
			p.setARGB(100, 0, 0, 0);
			selectionWidget.setBackgroundPaint(p);

			selectionWidget.position(0, XLayoutStyle.RELATIVE_TO_CENTER,
					PixelUtils.dpToPix(45), YLayoutStyle.ABSOLUTE_FROM_TOP,
					AnchorPosition.TOP_MIDDLE);
			selectionWidget.pack();

			// reduce the number of range labels
			plot.setTicksPerRangeLabel(3);
			plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
			plot.getGraphWidget().setGridPadding(30, 10, 30, 0);

			plot.setTicksPerDomainLabel(2);

			plot.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
						onPlotClicked(new PointF(motionEvent.getX(),
								motionEvent.getY()));
					}
					return true;
				}
			});

			plot.setDomainValueFormat(new NumberFormat() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public StringBuffer format(double value, StringBuffer buffer,
						FieldPosition field) {
					return new StringBuffer("");
				}

				@Override
				public StringBuffer format(long value, StringBuffer buffer,
						FieldPosition field) {
					throw new UnsupportedOperationException(
							"Not yet implemented.");
				}

				@Override
				public Number parse(String string, ParsePosition position) {
					throw new UnsupportedOperationException(
							"Not yet implemented.");
				}
			});

			// Remove all current series from each plot
			Iterator<XYSeries> iterator1 = plot.getSeriesSet().iterator();
			while (iterator1.hasNext()) {
				XYSeries setElement = iterator1.next();
				plot.removeSeries(setElement);
			}

			int numSeg = queryResult.size();
			numSeg = (numSeg > 10) ? 10 : numSeg;
			Segment segments[] = new Segment[numSeg];
			SegmentFormatter sf[] = new SegmentFormatter[numSeg];

			EmbossMaskFilter emf = new EmbossMaskFilter(
					new float[] { 1, 1, 1 }, 0.4f, 10, 8.2f);

			int max = 0;
			Log.v(LOG_TAG, "initalizing segemnts");
			for (int i = 0; i < numSeg; i++) {
				Entry<String, Integer> curr = queryResult.get(i);
				int count = curr.getValue();
				max = (count > max) ? count : max;
				String label = curr.getKey();
				segments[i] = new Segment(label, count);
				int color = Color.argb(255, generator.nextInt(256),
						generator.nextInt(256), generator.nextInt(256));
				sf[i] = new SegmentFormatter(color);
				sf[i].getFillPaint().setMaskFilter(emf);

				Number[] x = { i };
				XYSeries curSeries = new SimpleXYSeries(Arrays.asList(x),
						Arrays.asList(count), label);

				MyBarFormatter formatter = new MyBarFormatter(Color.argb(255,
						generator.nextInt(256), generator.nextInt(256),
						generator.nextInt(256)), Color.LTGRAY);

				plot.addSeries(curSeries, formatter);
				pie.addSeries(segments[i], sf[i]);
			}
			pie.getBorderPaint().setColor(Color.TRANSPARENT);
			pie.getBackgroundPaint().setColor(Color.DKGRAY);

			updatePlot();

			pie.setTitle(type + " Pie Chart Result");
			plot.setTitle(type + " Bar Graph Result");
			return v;
		}
	}

	private void updatePlot() {

		// Setup the BarRenderer with our selected options
		MyBarRenderer renderer = ((MyBarRenderer) plot
				.getRenderer(MyBarRenderer.class));
		renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.values()[2]);
		renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.values()[0]);
		renderer.setBarWidth(50);
		renderer.setBarGap(1);

		plot.setRangeTopMin(0);

		plot.redraw();

	}

	private void onPlotClicked(PointF point) {

		// make sure the point lies within the graph area. we use gridrect
		// because it accounts for margins and padding as well.
		if (plot.getGraphWidget().getGridRect().contains(point.x, point.y)) {
			Number x = plot.getXVal(point);
			Number y = plot.getYVal(point);

			selection = null;
			double xDistance = 0;
			double yDistance = 0;

			// find the closest value to the selection:
			for (XYSeries series : plot.getSeriesSet()) {
				for (int i = 0; i < series.size(); i++) {
					Number thisX = series.getX(i);
					Number thisY = series.getY(i);
					if (thisX != null && thisY != null) {
						double thisXDistance = LineRegion.measure(x, thisX)
								.doubleValue();
						double thisYDistance = LineRegion.measure(y, thisY)
								.doubleValue();
						if (selection == null) {
							selection = new Pair<Integer, XYSeries>(i, series);
							xDistance = thisXDistance;
							yDistance = thisYDistance;
						} else if (thisXDistance < xDistance) {
							selection = new Pair<Integer, XYSeries>(i, series);
							xDistance = thisXDistance;
							yDistance = thisYDistance;
						} else if (thisXDistance == xDistance
								&& thisYDistance < yDistance
								&& thisY.doubleValue() >= y.doubleValue()) {
							selection = new Pair<Integer, XYSeries>(i, series);
							xDistance = thisXDistance;
							yDistance = thisYDistance;
						}
					}
				}
			}

		} else {
			// if the press was outside the graph area, deselect:
			selection = null;
		}

		if (selection == null) {
			selectionWidget.setText(NO_SELECTION_TXT);
		} else {
			selectionWidget.setText(selection.second.getTitle() + ", Count: "
					+ selection.second.getY(selection.first));
		}
		plot.redraw();
	}

	private class MyBarFormatter extends BarFormatter {
		public MyBarFormatter(int fillColor, int borderColor) {
			super(fillColor, borderColor);
		}

		@Override
		public Class<? extends SeriesRenderer> getRendererClass() {
			return MyBarRenderer.class;
		}

		@Override
		public SeriesRenderer getRendererInstance(XYPlot plot) {
			return new MyBarRenderer(plot);
		}
	}

	private class MyBarRenderer extends BarRenderer<MyBarFormatter> {

		public MyBarRenderer(XYPlot plot) {
			super(plot);
		}

		/**
		 * Implementing this method to allow us to inject our special selection
		 * formatter.
		 * 
		 * @param index
		 *            index of the point being rendered.
		 * @param series
		 *            XYSeries to which the point being rendered belongs.
		 * @return
		 */
		// @Override
		protected MyBarFormatter getFormatter(int index, XYSeries series) {
			if (selection != null && selection.second == series
					&& selection.first == index) {
				return selectionFormatter;
			} else {
				return getFormatter(series);
			}
		}
	}

}