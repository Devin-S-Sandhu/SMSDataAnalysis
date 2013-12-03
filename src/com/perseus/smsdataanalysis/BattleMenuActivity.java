package com.perseus.smsdataanalysis;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BattleMenuActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	private final static String LOG_TAG = "MyFirstApp_tag";

	private final static int CONTACT_PICKER_RESULT = 0;

	private String contactOne, contactTwo;
	private String contactOneNumber, contactTwoNumber;
	private String contactOneName, contactTwoName;

	private boolean pickingContactOne = false;
	private boolean pickingContactTwo = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_battle_menu);
		Log.v(LOG_TAG, "A verbose message");

		this.setTitle("Friend Battle Menu");

		contactOne = contactTwo = "";
		contactOneNumber = contactTwoNumber = "";

		TextView label = (TextView) findViewById(R.id.friend_one_label);
		label.setText("");

		label = (TextView) findViewById(R.id.friend_two_label);
		label.setText("");
	}

	public void doBattle(View view) {
		if (!contactOne.isEmpty() && !contactTwo.isEmpty()) {
			Intent intent = new Intent(BattleMenuActivity.this,
					BattleResultActivity.class);
			intent.putExtra("contactOne", contactOne);
			intent.putExtra("contactTwo", contactTwo);
			intent.putExtra("contactOneNumber", contactOneNumber);
			intent.putExtra("contactTwoNumber", contactTwoNumber);
			intent.putExtra("contactOneName", contactOneName);
			intent.putExtra("contactTwoName", contactTwoName);
			intent.putExtra("timeSpan",
					((Spinner) findViewById(R.id.time_span)).getSelectedItem()
							.toString());

			startActivity(intent);
		} else {
			Toast.makeText(getApplicationContext(), "Select two contacts!",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void doLaunchContactPicker(View view) {
		if (view.getId() == R.id.contact_one_button) {
			pickingContactOne = true;
			pickingContactTwo = false;
		} else if (view.getId() == R.id.contact_two_button) {
			pickingContactOne = false;
			pickingContactTwo = true;
		}

		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, CONTACT_PICKER_RESULT);
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
						Toast.makeText(getApplicationContext(), cNumber,
								Toast.LENGTH_SHORT).show();

						String nameContact = c
								.getString(c
										.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

						if (pickingContactOne) {
							contactOne = new StringBuilder()
									.append(nameContact).append(" <")
									.append(cNumber).append(">").append(", ")
									.toString();

							contactOneNumber = cNumber;
							contactOneName = nameContact;

							TextView label = (TextView) findViewById(R.id.friend_one_label);
							label.setText(nameContact);

							pickingContactOne = false;
							pickingContactTwo = false;
						} else if (pickingContactTwo) {
							contactTwo = new StringBuilder()
									.append(nameContact).append(" <")
									.append(cNumber).append(">").append(", ")
									.toString();

							contactTwoNumber = cNumber;
							contactTwoName = nameContact;

							TextView label = (TextView) findViewById(R.id.friend_two_label);
							label.setText(nameContact);

							pickingContactOne = false;
							pickingContactTwo = false;
						}
					}
				}
				break;
			}
		} else {
			// gracefully handle failure
			Log.w(LOG_TAG, "Warning: activity result not ok");
		}
	}

}