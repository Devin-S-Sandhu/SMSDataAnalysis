package com.perseus.smsdataanalysis;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
		SmsUtil.selectedContact = new HashMap<String, String>();
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
		if (view.getId() == R.id.contact_one) {
			pickingContactOne = true;
			pickingContactTwo = false;
			SmsUtil.selectedContact.remove(contactOneNumber);
		} else if (view.getId() == R.id.contact_two) {
			pickingContactOne = false;
			pickingContactTwo = true;
			SmsUtil.selectedContact.remove(contactTwoNumber);
		}

		Intent intent = new Intent(BattleMenuActivity.this,
				ContactPickerActivity.class);
		startActivityForResult(intent, CONTACT_PICKER_RESULT);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				String nameContact = data.getStringExtra("name");
				String cNumber = data.getStringExtra("ID");
				
				ImageView contact_photo = ((ImageView) findViewById(R.id.contact_one_button));
				Uri u = null;

				if (pickingContactOne) {
					contactOne = new StringBuilder()
					.append(nameContact).append(" <")
					.append(cNumber).append(">").append(", ")
					.toString();

					contactOneNumber = cNumber;
					contactOneName = nameContact;

					TextView label = (TextView) findViewById(R.id.friend_one_label);
					label.setText(nameContact);
					u = ContactPhotoHelper.getPhotoUri(this, contactOneNumber);
				} else if (pickingContactTwo) {
					contactTwo = new StringBuilder()
					.append(nameContact).append(" <")
					.append(cNumber).append(">").append(", ")
					.toString();

					contactTwoNumber = cNumber;
					contactTwoName = nameContact;

					TextView label = (TextView) findViewById(R.id.friend_two_label);
					label.setText(nameContact);
					contact_photo = ((ImageView) findViewById(R.id.contact_two_button));

					u = ContactPhotoHelper.getPhotoUri(this, contactTwoNumber);
				}

				if(u != null){
					contact_photo.getLayoutParams().height = 300;
					contact_photo.setImageURI(u);
				}
				if(contact_photo.getDrawable() == null){
					String uri = "@drawable/fighter1";
					if(pickingContactTwo)
						uri = "@drawable/fighter2";
					int imageResource = getResources().getIdentifier(uri, null, getApplicationContext().getPackageName());
					contact_photo.setImageResource(imageResource);
				}

				pickingContactOne = false;
				pickingContactTwo = false;
				break;
			}
		} else {
			if(pickingContactOne && !contactOneNumber.equals("") && !contactOneName.equals(""))
				SmsUtil.selectedContact.put(contactOneNumber, contactOneName);
			else if(pickingContactTwo && !contactTwoNumber.equals("") && !contactTwoName.equals(""))
				SmsUtil.selectedContact.put(contactTwoNumber, contactTwoName);
			pickingContactOne = false;
			pickingContactTwo = false;
		}
	}

}