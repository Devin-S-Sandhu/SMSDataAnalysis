package com.perseus.smsdataanalysis;

import java.util.Arrays;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Settings extends PreferenceActivity {
	private final int NUM_ENTRIES = 6;
	private final String LOG_TAG = "settings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("ttt_prefs");
		addPreferencesFromResource(R.xml.preferences);

		final SharedPreferences prefs = getSharedPreferences("ttt_prefs",
				MODE_PRIVATE);

		final ListPreference numResultsPref = (ListPreference) findPreference("num_results");
		String[] entries = new String[NUM_ENTRIES];
		for (int x = 0; x < NUM_ENTRIES; x++)
			entries[x] = Integer.toString(5 + x);
		Log.d(LOG_TAG, Arrays.toString(entries));
		numResultsPref.setEntryValues(entries);
		numResultsPref.setEntries(entries);
		// TODO actually update the relevant value

	}
}
