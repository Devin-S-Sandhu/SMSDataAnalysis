package com.perseus.smsdataanalysis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	private final static String LOG_TAG = "SMSDataAnalysis_tag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean allBox = getApplicationContext().getContentResolver().query(
				Uri.parse("content://sms/"), new String[] { "address", "body" },
				null, null, null).getColumnCount() < 1;
		boolean inbox = getApplicationContext().getContentResolver().query(
				Uri.parse("content://sms/inbox"), new String[] { "address", "body" },
				null, null, null).getColumnCount() < 1;
		boolean outbox = getApplicationContext().getContentResolver().query(
				Uri.parse("content://sms/sent"), new String[] { "address", "body" },
				null, null, null).getColumnCount() < 1;
		if(allBox || inbox || outbox)
		{
			new AlertDialog.Builder(this)
		    .setTitle("Device not supported")
		    .setMessage("Your device either contains no SMS data or store SMS differently. This App will not work as expected")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
		     })
		    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // do nothing
		        }
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();
		}
			
		setContentView(R.layout.activity_main);
		Log.v(LOG_TAG, "A verbose message");
	}
	
	public void startAnalysisActivity(View view) {
		Intent myIntent = new Intent(MainActivity.this,
				AnalysisMenuActivity.class);
		// myIntent.putExtra("key", value); //Optional parameters
		MainActivity.this.startActivity(myIntent);
	}

	public void startBattleActivity(View view) {
		Intent myIntent = new Intent(MainActivity.this,
				BattleMenuActivity.class);
		// myIntent.putExtra("key", value); //Optional parameters
		MainActivity.this.startActivity(myIntent);
	}
}