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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
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

		contactOneName = intent.getStringExtra("contactOneName");
		contactTwoName = intent.getStringExtra("contactTwoName");
		contactOneNumber = intent.getStringExtra("contactOneNumber");
		contactTwoNumber = intent.getStringExtra("contactTwoNumber");

		String contacts = new StringBuilder().append(contactOneName)
				.append(" <").append(contactOneNumber).append(">").append(", ")
				.append(contactTwoName).append(" <").append(contactTwoNumber)
				.append(">").toString();

		Log.d(LOG_TAG, contacts);

		// create queries
		Analyzer.Query query1 = mAnalyzer.new Query(analysisTypes[1],
				"Received", startDate.getMonth() + "-" + startDate.getDay()
						+ "-" + startDate.getYear(), endDate.getMonth() + "-"
						+ endDate.getDay() + "-" + endDate.getYear(), contacts);

		Analyzer.Query query2 = mAnalyzer.new Query(analysisTypes[1], "Sent",
				startDate.getMonth() + "-" + startDate.getDay() + "-"
						+ startDate.getYear(), endDate.getMonth() + "-"
						+ endDate.getDay() + "-" + endDate.getYear(), contacts);

		Analyzer.Query query3 = mAnalyzer.new Query(analysisTypes[5],
				"Received", startDate.getMonth() + "-" + startDate.getDay()
						+ "-" + startDate.getYear(), endDate.getMonth() + "-"
						+ endDate.getDay() + "-" + endDate.getYear(), contacts);

		Analyzer.Query query4 = mAnalyzer.new Query(analysisTypes[5], "Sent",
				startDate.getMonth() + "-" + startDate.getDay() + "-"
						+ startDate.getYear(), endDate.getMonth() + "-"
						+ endDate.getDay() + "-" + endDate.getYear(), contacts);

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
				new ContactPhotoHelper(BattleResultActivity.this, winnerPhoto,
						contactOneNumber).addThumbnail();
				// winnerPhoto.setImageBitmap(loadContactPhoto(getContentResolver(),
				// contactOneId, contactOnePhotoId));
			} else if (contactTwoWins > contactOneWins) {
				winner = contactTwoName + " Wins!";
				new ContactPhotoHelper(BattleResultActivity.this, winnerPhoto,
						contactTwoNumber).addThumbnail();
				// winnerPhoto.setImageBitmap(loadContactPhoto(getContentResolver(),
				// contactTwoId, contactTwoPhotoId));
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

			((TextView) findViewById(R.id.contact_one_wins)).setText(""
					+ contactOneWins);
			((TextView) findViewById(R.id.contact_two_wins)).setText(""
					+ contactTwoWins);

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
