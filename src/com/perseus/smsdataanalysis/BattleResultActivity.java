package com.perseus.smsdataanalysis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BattleResultActivity extends Activity {
	private static final String LOG_TAG = "BattleResultActivity";

	private final int CURR_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	private final int CURR_MONTH = Calendar.getInstance().get(Calendar.MONTH);
	private final int CURR_DAY = Calendar.getInstance().get(
			Calendar.DAY_OF_MONTH);

	private HashMap<String, Integer> timeSpans;
	private String timeSpan;

	private String[] analysisTypes;

	private Date startDate;
	private Date endDate;

	private String contactOneName;
	private String contactTwoName;
	private String contactOneNumber;
	private String contactTwoNumber;

	private Analyzer mAnalyzer;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_battle_result);
		mAnalyzer = new Analyzer(getApplicationContext());
		Intent intent = getIntent();

		timeSpans = new HashMap<String, Integer>();
		String[] timeSpanArray = getApplicationContext().getResources()
				.getStringArray(R.array.time_span_array);
		for (int i = 0; i < timeSpanArray.length; i++) {
			timeSpans.put(timeSpanArray[i], i);
		}
		timeSpan = intent.getStringExtra("timeSpan");

		analysisTypes = getApplicationContext().getResources().getStringArray(
				R.array.analaysis_type_arrays);

		this.setTitle(this.getTitle() + " for " + timeSpan);

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

		Log.d(LOG_TAG, "start date: " + startDate.toString());
		Log.d(LOG_TAG, "end date: " + endDate.toString());

		contactOneName = intent.getStringExtra("contactOneName");
		contactTwoName = intent.getStringExtra("contactTwoName");
		contactOneNumber = intent.getStringExtra("contactOneNumber");
		contactTwoNumber = intent.getStringExtra("contactTwoNumber");

		HashMap<String,String> contacts = new HashMap<String,String>();
		contacts.put(contactOneNumber, contactOneName);
		contacts.put(contactTwoNumber, contactTwoName);

		String start = (startDate.getMonth() + 1) + "-" + startDate.getDay()
				+ "-" + startDate.getYear();

		String end = (endDate.getMonth() + 1) + "-" + endDate.getDay() + "-"
				+ endDate.getYear();

		Log.d(LOG_TAG, "### " + start);
		Log.d(LOG_TAG, "### " + end);

		// create queries
		Analyzer.Query query1 = mAnalyzer.new Query(analysisTypes[1],
				"Received", start, end, contacts);

		Analyzer.Query query2 = mAnalyzer.new Query(analysisTypes[1], "Sent",
				start, end, contacts);

		Analyzer.Query query3 = mAnalyzer.new Query(analysisTypes[5],
				"Received", start, end, contacts);

		Analyzer.Query query4 = mAnalyzer.new Query(analysisTypes[5], "Sent",
				start, end, contacts);

		new BattleTask().execute(query1, query2, query3, query4);
	}

	private class BattleTask
			extends
			AsyncTask<Analyzer.Query, Void, ArrayList<ArrayList<Analyzer.Pair<String, Integer>>>> {
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
		protected ArrayList<ArrayList<Analyzer.Pair<String, Integer>>> doInBackground(
				Analyzer.Query... params) {
			ArrayList<ArrayList<Analyzer.Pair<String, Integer>>> result = new ArrayList<ArrayList<Analyzer.Pair<String, Integer>>>();
			result.add(mAnalyzer.doQuery(params[0]));
			result.add(mAnalyzer.doQuery(params[1]));
			result.add(mAnalyzer.doQuery(params[2]));
			result.add(mAnalyzer.doQuery(params[3]));
			return result;
		}

		@Override
		protected void onPostExecute(
				ArrayList<ArrayList<Analyzer.Pair<String, Integer>>> result) {
			int contactOneWins = 0, contactTwoWins = 0;
			ArrayList<ArrayList<Integer>> dataToDisplay = new ArrayList<ArrayList<Integer>>();

			for (ArrayList<Analyzer.Pair<String, Integer>> queryResult : result) {
				if (queryResult.size() < 2) {
					mProgressDialog.dismiss();
					return;
				}

				ArrayList<Integer> row = new ArrayList<Integer>();

				String winnerName = queryResult.get(0).getElement0().trim();

				if (queryResult.get(0).getElement1() == queryResult.get(1)
						.getElement1()) {
					row.add(queryResult.get(0).getElement1());
					row.add(queryResult.get(1).getElement1());
				} else if (winnerName.equals(contactOneName)) {
					contactOneWins++;
					row.add(queryResult.get(0).getElement1());
					row.add(queryResult.get(1).getElement1());
				} else if (winnerName.equals(contactTwoName)) {
					contactTwoWins++;
					row.add(queryResult.get(1).getElement1());
					row.add(queryResult.get(0).getElement1());
				}

				dataToDisplay.add(row);
			}

			TextView winnerLabel = ((TextView) findViewById(R.id.winner_label));
			String winner = "It's a tie!";
			ImageView winnerPhoto = ((ImageView) findViewById(R.id.winner_photo));
			if (contactOneWins > contactTwoWins) {
				winner = contactOneName + " Wins!";
				Uri u = ContactPhotoHelper.getPhotoUri(BattleResultActivity.this, contactOneNumber);
				if(u != null)
					winnerPhoto.setImageURI(u);
				if(winnerPhoto.getDrawable() == null){
					String uri = "@drawable/fighter1";
					int imageResource = getResources().getIdentifier(uri, null, getApplicationContext().getPackageName());
					winnerPhoto.setImageResource(imageResource);
				}

				TextView contact_one_wins = ((TextView) findViewById(R.id.contact_one_wins));
				contact_one_wins.setText("WINNER");
				contact_one_wins.setBackgroundColor(Color.parseColor("#bababa"));
				
				ImageView photo2 = (ImageView) findViewById(R.id.photo2);
				photo2.setVisibility(View.GONE);
			} else if (contactTwoWins > contactOneWins) {
				winner = contactTwoName + " Wins!";
				Uri u = ContactPhotoHelper.getPhotoUri(BattleResultActivity.this, contactTwoNumber);
				if(u != null)
					winnerPhoto.setImageURI(u);
				if(winnerPhoto.getDrawable() == null){
					String uri = "@drawable/fighter2";
					int imageResource = getResources().getIdentifier(uri, null, getApplicationContext().getPackageName());
					winnerPhoto.setImageResource(imageResource);
				}
				
				TextView contact_two_wins = ((TextView) findViewById(R.id.contact_two_wins));
				contact_two_wins.setText("WINNER");
				contact_two_wins.setBackgroundColor(Color.parseColor("#bababa"));
				
				ImageView photo2 = (ImageView) findViewById(R.id.photo2);
				photo2.setVisibility(View.GONE);
			}
			else{
				Uri u = ContactPhotoHelper.getPhotoUri(BattleResultActivity.this, contactOneNumber);
				if(u != null)
					winnerPhoto.setImageURI(u);
				if(winnerPhoto.getDrawable() == null){
					String uri = "@drawable/fighter1";
					int imageResource = getResources().getIdentifier(uri, null, getApplicationContext().getPackageName());
					winnerPhoto.setImageResource(imageResource);
				}
				
				ImageView photo2 = (ImageView) findViewById(R.id.photo2);
				
				Uri u2 = ContactPhotoHelper.getPhotoUri(BattleResultActivity.this, contactTwoNumber);
				if(u2 != null)
					photo2.setImageURI(u2);
				if(photo2.getDrawable() == null){
					String uri = "@drawable/fighter2";
					int imageResource = getResources().getIdentifier(uri, null,getApplicationContext().getPackageName());
					photo2.setImageResource(imageResource);
				}
				
			}
			winnerLabel.setText(winner);

			((TextView) findViewById(R.id.contact_one_name))
					.setText(contactOneName);
			((TextView) findViewById(R.id.contact_two_name))
					.setText(contactTwoName);

			((TextView) findViewById(R.id.contact_one_messages_received))
					.setText("" + dataToDisplay.get(0).get(0));
			((TextView) findViewById(R.id.contact_two_messages_received))
					.setText("" + dataToDisplay.get(0).get(1));

			((TextView) findViewById(R.id.contact_one_messages_sent))
					.setText("" + dataToDisplay.get(1).get(0));
			((TextView) findViewById(R.id.contact_two_messages_sent))
					.setText("" + dataToDisplay.get(1).get(1));

			((TextView) findViewById(R.id.contact_one_interval_received))
					.setText("" + dataToDisplay.get(2).get(0) + " hours");
			((TextView) findViewById(R.id.contact_two_interval_received))
					.setText("" + dataToDisplay.get(2).get(1) + " hours");

			((TextView) findViewById(R.id.contact_one_interval_sent))
					.setText("" + dataToDisplay.get(3).get(0) + " hours");
			((TextView) findViewById(R.id.contact_two_interval_sent))
					.setText("" + dataToDisplay.get(3).get(1) + " hours");

			mProgressDialog.dismiss();
		}

	}

	public static Bitmap loadContactPhoto(ContentResolver cr, long id,
			long photo_id) {

		Uri uri = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, id);
		InputStream input = ContactsContract.Contacts
				.openContactPhotoInputStream(cr, uri);
		if (input != null) {
			return BitmapFactory.decodeStream(input);
		} else {
			Log.d("PHOTO", "first try failed to load photo");

		}

		byte[] photoBytes = null;

		Uri photoUri = ContentUris.withAppendedId(
				ContactsContract.Data.CONTENT_URI, photo_id);

		Cursor c = cr.query(photoUri,
				new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO },
				null, null, null);

		try {
			if (c.moveToFirst())
				photoBytes = c.getBlob(0);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			c.close();
		}

		if (photoBytes != null)
			return BitmapFactory.decodeByteArray(photoBytes, 0,
					photoBytes.length);
		else
			Log.d("PHOTO", "second try also failed");
		return null;
	}

}