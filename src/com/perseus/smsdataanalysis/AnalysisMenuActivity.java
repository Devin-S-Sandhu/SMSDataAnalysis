package com.perseus.smsdataanalysis;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AnalysisMenuActivity extends Activity {
	private final static String LOG_TAG = "AnalysisMenuActivity_tag";
	private Spinner analysisType;
	private Spinner scope;
	private TextView startDate, endDate;
	private CustomMultiAutoCompleteTextView selectContact;

	private int start_year, end_year;
	private int start_month, end_month;
	private int start_day, end_day;
	private DatePickerDialog startDatePickerDialog;
	private DatePickerDialog endDatePickerDialog;

	static final int START_DATE_DIALOG_ID = 0;
	static final int END_DATE_DIALOG_ID = 1;

	static final int CURR_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	static final int CURR_MONTH = Calendar.getInstance().get(Calendar.MONTH);
	static final int CURR_DAY = Calendar.getInstance().get(
			Calendar.DAY_OF_MONTH);

	private static final int CONTACT_PICKER_RESULT = 1001;

	public static final String[] PEOPLE_PROJECTION = new String[] {
			ContactsContract.Contacts._ID, Contacts.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Phone.NUMBER };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_menu);

		this.setTitle("Data Analysis Menu");
		scope = (Spinner) findViewById(R.id.scope);
		startDate = (TextView) findViewById(R.id.start_date_display);
		endDate = (TextView) findViewById(R.id.end_date_display);
		selectContact = (CustomMultiAutoCompleteTextView) findViewById(R.id.select_contact);

		analysisType = (Spinner) findViewById(R.id.analysis_type);
		setCurrentDateOnView();

		startDatePickerDialog = new DatePickerDialog(this,
				startDatePickerListener, start_year, start_month, start_day);
		endDatePickerDialog = new DatePickerDialog(this, endDatePickerListener,
				end_year, end_month, end_day);
		//Cursor cursor = getContentResolver().query(
		//		ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
		//		PEOPLE_PROJECTION, null, null, null);
		//ContactListAdapter adapter = new ContactListAdapter(this, cursor);

		ContactPickerAdapter adapter = new ContactPickerAdapter(this,
				android.R.layout.simple_list_item_1, SmsUtil.getContacts(
					this, false));
		selectContact.setAdapter(adapter);
		//selectContact
		//		.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		selectContact.setHorizontalScrollBarEnabled(true);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				Uri contactData = data.getData();
				@SuppressWarnings("deprecation")
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String id = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

					String hasPhone = c
							.getString(c
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = " + id, null, null);
						phones.moveToFirst();
						String cNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						/*
						 * Toast.makeText(getApplicationContext(), cNumber,
						 * Toast.LENGTH_SHORT).show();
						 */

						String nameContact = c
								.getString(c
										.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

						selectContact.setText(new StringBuilder()
								.append(selectContact.getText().toString())
								.append(nameContact).append(" <")
								.append(cNumber).append(">").append(", "));
						// selectContact.setWidth(500);
					}
				}
				break;
			}
		} else {
			// gracefully handle failure
			Log.w(LOG_TAG, "Warning: activity result not ok");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// display current date
	public void setCurrentDateOnView() {
		setCurrentDate();
		// set current date into textview
		startDate.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(start_month + 1).append("-").append(start_day)
				.append("-").append(start_year).append(" "));
		endDate.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(end_month + 1).append("-").append(end_day).append("-")
				.append(end_year).append(" "));
	}

	private void setCurrentDate() {
		start_year = 1970;
		start_month = 0;
		start_day = 1;
		end_year = CURR_YEAR;
		end_month = CURR_MONTH;
		end_day = CURR_DAY;
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case START_DATE_DIALOG_ID:
			// set date picker as current date
			startDatePickerDialog
					.updateDate(start_year, start_month, start_day);
			return startDatePickerDialog;
		case END_DATE_DIALOG_ID:
			// set date picker as current date
			endDatePickerDialog.updateDate(end_year, end_month, end_day);
			return endDatePickerDialog;
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener startDatePickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			if ((selectedYear > end_year)
					|| (selectedYear == end_year && selectedMonth > end_month)
					|| (selectedYear == end_year
							&& selectedMonth == start_month && selectedDay > end_day)) {
				Toast.makeText(AnalysisMenuActivity.this,
						"Invalid start date!", Toast.LENGTH_SHORT).show();
				startDatePickerDialog.updateDate(start_year, start_month,
						start_day);
			} else {
				start_day = selectedDay;
				start_month = selectedMonth;
				start_year = selectedYear;
				// set selected date into textview
				startDate.setText(new StringBuilder().append(start_month + 1)
						.append("-").append(start_day).append("-")
						.append(start_year).append(" "));
			}
		}
	};

	private DatePickerDialog.OnDateSetListener endDatePickerListener = new DatePickerDialog.OnDateSetListener() {
		// when dialog box is closed, below method will be called.
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			if ((selectedYear > CURR_YEAR)
					|| (selectedYear == CURR_YEAR && selectedMonth > CURR_MONTH)
					|| (selectedYear == CURR_YEAR
							&& selectedMonth == CURR_MONTH && selectedDay > CURR_DAY)) {
				Toast.makeText(AnalysisMenuActivity.this,
						"Invalid end date! Cannot analyze future texts!",
						Toast.LENGTH_SHORT).show();
				endDatePickerDialog.updateDate(end_year, end_month, end_day);
			} else {
				if ((selectedYear < start_year)
						|| (selectedYear == start_year && selectedMonth < start_month)
						|| (selectedYear == start_year
								&& selectedMonth == start_month && selectedDay < start_day)) {
					start_day = end_day;
					start_month = end_month;
					start_year = end_year;
					// set selected date into textview
					startDate.setText(new StringBuilder()
							.append(start_month + 1).append("-")
							.append(start_day).append("-").append(start_year)
							.append(" "));
					startDatePickerDialog.updateDate(start_year, start_month,
							start_day);
				}
				end_year = selectedYear;
				end_month = selectedMonth;
				end_day = selectedDay;
				// set selected date into textview
				endDate.setText(new StringBuilder().append(end_month + 1)
						.append("-").append(end_day).append("-")
						.append(end_year).append(" "));
			}
		}
	};

	@SuppressWarnings("deprecation")
	public void pickStartDate(View view) {
		showDialog(START_DATE_DIALOG_ID);
	}

	@SuppressWarnings("deprecation")
	public void pickEndDate(View view) {
		showDialog(END_DATE_DIALOG_ID);
	}

	public void analyze(View view) {
		Intent myIntent = new Intent(AnalysisMenuActivity.this,
				AnalysisResultActivity.class);
		myIntent.putExtra("type", analysisType.getSelectedItem().toString());
		myIntent.putExtra("scope", scope.getSelectedItem().toString());
		myIntent.putExtra("start_date", startDate.getText().toString());
		myIntent.putExtra("end_date", endDate.getText().toString());
		myIntent.putExtra("contacts", selectContact.getText().toString());
		AnalysisMenuActivity.this.startActivity(myIntent);
	}

	public void doLaunchContactPicker(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, CONTACT_PICKER_RESULT);
	}

	
}