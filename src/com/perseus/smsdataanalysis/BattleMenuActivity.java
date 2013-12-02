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
    private long contactOneId, contactTwoId;
    private long contactOnePhotoId, contactTwoPhotoId;
    private boolean battleFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_battle_menu);
		Log.v(LOG_TAG, "A verbose message");

		this.setTitle("Friend Battle Menu");
		
		contactOne = contactTwo = "";
		contactOneId = contactTwoId = 0;
		contactOnePhotoId = contactTwoPhotoId = 0;
		
		TextView label = (TextView) findViewById(R.id.friend_one_label);
		label.setText(R.string.friend_one);
		
		label = (TextView) findViewById(R.id.friend_two_label);
		label.setText(R.string.friend_two);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (battleFlag) {
			contactOne = contactTwo = "";
			
			TextView label = (TextView) findViewById(R.id.friend_one_label);
			label.setText(R.string.friend_one);
			
			label = (TextView) findViewById(R.id.friend_two_label);
			label.setText(R.string.friend_two);
			
			battleFlag = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void doBattle(View view) {
		if (!contactOne.isEmpty() && !contactTwo.isEmpty()) {
			Intent intent = new Intent(BattleMenuActivity.this, 
					BattleResultActivity.class);
			intent.putExtra("contactOne", contactOne);
			intent.putExtra("contactTwo", contactTwo);
			intent.putExtra("contactOneId", contactOneId);
			intent.putExtra("contactTwoId", contactTwoId);
			intent.putExtra("contactOnePhotoId", contactOnePhotoId);
			intent.putExtra("contactTwoPhotoId", contactTwoPhotoId);
			intent.putExtra("timeSpan", ((Spinner) findViewById(R.id.time_span)).getSelectedItem().toString());
			
			battleFlag = true;
			
			startActivity(intent);
		}
		else {
			Toast.makeText(getApplicationContext(), "Select two contacts!", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void doLaunchContactPicker(View view) {
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
					
					String photoId = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_ID));

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

						if (contactOne.isEmpty()) {
							contactOne = new StringBuilder()
							.append(nameContact).append(" <")
							.append(cNumber).append(">").append(", ").toString();
							
							contactOneId = Long.parseLong(id);
							if (photoId != null)
								contactOnePhotoId = Long.parseLong(photoId);
							
							TextView label = (TextView) findViewById(R.id.friend_one_label);
							label.setText(label.getText() + " " + nameContact);
						}
						else if (contactTwo.isEmpty()) {
							contactTwo = new StringBuilder()
							.append(nameContact).append(" <")
							.append(cNumber).append(">").append(", ").toString();
							
							contactTwoId = Long.parseLong(id);
							if (photoId != null)
								contactTwoPhotoId = Long.parseLong(photoId);
							
							TextView label = (TextView) findViewById(R.id.friend_two_label);
							label.setText(label.getText() + " " + nameContact);
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