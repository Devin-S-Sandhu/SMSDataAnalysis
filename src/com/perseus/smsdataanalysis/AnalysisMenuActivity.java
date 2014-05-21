package com.perseus.smsdataanalysis;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class AnalysisMenuActivity extends Activity {
	private final static String LOG_TAG = "AnalysisMenuActivity_tag";
	private Spinner analysisType;
	private Spinner scope;
	private Spinner time_span;
	private TextView startDate, endDate, analysisDescriptionView;
	private SharedPreferences mPrefs;
	private boolean advanceDatePicker;
	private TableLayout contactTable;

	private int start_year, end_year;
	private int start_month, end_month;
	private int start_day, end_day;
	private DatePickerDialog startDatePickerDialog;
	private DatePickerDialog endDatePickerDialog;

	static final int START_DATE_DIALOG_ID = 0;
	static final int END_DATE_DIALOG_ID = 1;

	private static final int CURR_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	private static final int CURR_MONTH = Calendar.getInstance().get(Calendar.MONTH);
	private static final int CURR_DAY = Calendar.getInstance().get(
			Calendar.DAY_OF_MONTH);
	private static Date TODAY = new Date(CURR_YEAR, CURR_MONTH, CURR_DAY);

	private static final int CONTACT_PICKER_RESULT = 1001;
	private static final int OPTION_MENU_RESULT = 1002;

	public static final String[] PEOPLE_PROJECTION = new String[] {
		ContactsContract.Contacts._ID, Contacts.DISPLAY_NAME,
		ContactsContract.CommonDataKinds.Phone.NUMBER };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_menu);

		mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
		advanceDatePicker = mPrefs.getBoolean("advancedDatePicker", false);

		this.setTitle("Data Analysis Menu");
		scope = (Spinner) findViewById(R.id.scope_spinner);
		startDate = (TextView) findViewById(R.id.start_date_display);
		endDate = (TextView) findViewById(R.id.end_date_display);
		analysisType = (Spinner) findViewById(R.id.analysis_type_spinner);
		analysisDescriptionView = (TextView) findViewById(R.id.analysis_description_view);
		time_span = (Spinner) findViewById(R.id.time_span);
		contactTable = (TableLayout) findViewById(R.id.contactTable);

		scope.setSelection(mPrefs.getInt("scope", 0));
		analysisType.setSelection(mPrefs.getInt("analysisType", 0));
		time_span.setSelection(mPrefs.getInt("time_span", 0));

		updateDatePicker();
		setCurrentDateOnView();

		analysisType.setOnItemSelectedListener(new OnItemSelectedListener() {

			private String[] analysisDescriptions = AnalysisMenuActivity.this
					.getResources().getStringArray(
							R.array.analysis_description_array);

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				analysisDescriptionView.setText(analysisDescriptions[arg2]);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				analysisDescriptionView.setText(analysisDescriptions[0]);
			}
		});

		startDatePickerDialog = new DatePickerDialog(this,
				startDatePickerListener, start_year, start_month, start_day);
		endDatePickerDialog = new DatePickerDialog(this, endDatePickerListener,
				end_year, end_month, end_day);
		SmsUtil.selectedContact = new HashMap<String, String>();
	}

	private void updateDatePicker() {
		if(advanceDatePicker){
			findViewById(R.id.datePickerlabel).setVisibility(View.GONE);
			findViewById(R.id.datePickerRow).setVisibility(View.GONE);
		}
		else{
			findViewById(R.id.advancedDatePickerLabel).setVisibility(View.GONE);
			findViewById(R.id.advancedDatePickerRow).setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(LOG_TAG, "in onOptionsItemSelected selecting");
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivityForResult(new Intent(this, Settings.class), OPTION_MENU_RESULT);
			return true;
		}
		return false;
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("onActivityResult", "requestCode: " + requestCode
				+ " resultCode: " + resultCode + "data: " + data);
		if (requestCode == OPTION_MENU_RESULT)
		{
			Log.d(LOG_TAG, "Option menu result, recreating activity");
			recreate();
		}
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				contactTable.removeAllViews();
				if(SmsUtil.selectedContact.size() == 0)
				{
				    TableRow row = (TableRow)LayoutInflater.from(this).inflate(R.layout.attrib_row, null);
				    ((TextView)row.findViewById(R.id.attrib_name)).setText("Analyze all contacts");
				    row.removeView(row.findViewById(R.id.paddingleft));
				    row.setPadding(30, 0, 0, 0);
				    contactTable.addView(row,contactTable.getChildCount()-1);
				}
				else{
					for(String number : SmsUtil.selectedContact.keySet())
					{
					    // Inflate your row "template" and fill out the fields.
					    TableRow row = (TableRow)LayoutInflater.from(this).inflate(R.layout.attrib_row, null);
					    ((TextView)row.findViewById(R.id.attrib_name)).setText(SmsUtil.selectedContact.get(number));
						ImageView contact_photo = ((ImageView) row.findViewById(R.id.contact_photo));
						new ContactPhotoHelper(this, contact_photo,
								number).addThumbnail();
					    contactTable.addView(row,contactTable.getChildCount()-1);
					}
				}
				contactTable.requestLayout(); 
				break;
			}
		} else {
			// gracefully handle failure
			Log.w(LOG_TAG, "Warning: activity result not ok");
		}
	}

	// display current date
	public void setCurrentDateOnView() {
		setCurrentDate();
		// set current date into textview
		String newStartDate = new StringBuilder()
		.append(start_month + 1).append("-").append(start_day)
		.append("-").append(start_year).append(" ").toString();
		String newEndDate = new StringBuilder()
		.append(end_month + 1).append("-").append(end_day).append("-")
		.append(end_year).append(" ").toString();

		startDate.setText(mPrefs.getString("startDate", newStartDate));
		endDate.setText(mPrefs.getString("endDate", newEndDate));
	}

	private void setCurrentDate() {
		start_year = CURR_YEAR - 3;
		start_month = CURR_MONTH;
		start_day = CURR_DAY;
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
		myIntent.putExtra("info_dump", mPrefs.getBoolean("info_dump", false));
		myIntent.putExtra("type", analysisType.getSelectedItem().toString());
		myIntent.putExtra("scope", scope.getSelectedItem().toString());
		if(!advanceDatePicker)
		{
			updateStartDate();
		}
		myIntent.putExtra("start_date", startDate.getText().toString());
		myIntent.putExtra("end_date", endDate.getText().toString());
		
		StringBuilder selectedContacts = new StringBuilder("");
		for(String num : SmsUtil.selectedContact.keySet())
			selectedContacts.append(SmsUtil.selectedContact.get(num)).append(" <").append(num).append(">,");
		myIntent.putExtra("contacts", selectedContacts.toString());
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putInt("scope", scope.getSelectedItemPosition());
		ed.putInt("analysisType", analysisType.getSelectedItemPosition());
		ed.putInt("time_span", time_span.getSelectedItemPosition());
		ed.putString("startDate", startDate.getText().toString());
		ed.putString("endDate", endDate.getText().toString());
		ed.commit();

		AnalysisMenuActivity.this.startActivity(myIntent);
	}

	public void doLaunchContactPicker(View view) {
		Intent intent = new Intent(AnalysisMenuActivity.this, ContactPickerActivity.class);
		startActivityForResult(intent, CONTACT_PICKER_RESULT);
	}

	private void updateStartDate(){
		String time_span_str = time_span.getSelectedItem().toString();

		Calendar c = Calendar.getInstance();
		c.setTime(TODAY);

		HashMap<String, Integer> timeSpans = new HashMap<String, Integer>();
		String[] timeSpanArray = getApplicationContext().getResources()
				.getStringArray(R.array.time_span_array);
		for (int i = 0; i < timeSpanArray.length; i++) {
			timeSpans.put(timeSpanArray[i], i);
		}
		switch (timeSpans.get(time_span_str)) {
		case 0:
			c.add(Calendar.MONTH, -1);
			break;
		case 1:
			c.add(Calendar.YEAR, -1);
			break;
		default:
			c.set(1980, 0, 1);
			break;
		}
		Date newDate = c.getTime();
		startDate.setText(
				new StringBuilder().append(newDate.getMonth() + 1)
				.append("-").append(newDate.getDate()).append("-")
				.append(newDate.getYear()).append(" "));
	}
}