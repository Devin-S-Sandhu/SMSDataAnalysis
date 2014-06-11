package com.perseus.smsdataanalysis;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {
	private final String LOG_TAG = "settings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("ttt_prefs");
		addPreferencesFromResource(R.xml.preferences);

		final SharedPreferences prefs = getSharedPreferences("ttt_prefs",
				MODE_PRIVATE);

		final ListPreference numResultsPref = (ListPreference) findPreference("num_results");
		String num_results = prefs.getString("num_results", Integer.toString(10));
		numResultsPref.setSummary("Number of analysis results to display: " + num_results);
		numResultsPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						numResultsPref.setSummary("Number of analysis results to display: " + (CharSequence) newValue);
						// Since we are handling the pref, we must save it
						SharedPreferences.Editor ed = prefs.edit();
						ed.putString("num_results", newValue.toString());
						ed.commit();
						return true;
					}
				});
	    final CheckBoxPreference stopwordsPref = (CheckBoxPreference) findPreference("stopwords");
	    stopwordsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {            
	        public boolean onPreferenceChange(Preference preference, Object newValue) {
	        	SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean("stopwords", (Boolean)newValue);
				ed.commit();       
	            return true;
	        }
	    });
	    final CheckBoxPreference dumpPref = (CheckBoxPreference) findPreference("info_dump");
	    dumpPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {            
	        public boolean onPreferenceChange(Preference preference, Object newValue) {
	        	SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean("info_dump", (Boolean)newValue);
				ed.commit();       
	            return true;
	        }
	    }); 
	    final CheckBoxPreference analyzeAllPref = (CheckBoxPreference) findPreference("analyze_all");
	    analyzeAllPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {            
	        public boolean onPreferenceChange(Preference preference, Object newValue) {
	        	SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean("analyze_all", (Boolean)newValue);
				ed.commit();       
	            return true;
	        }
	    }); 
	    final CheckBoxPreference advancedDatePref = (CheckBoxPreference) findPreference("advancedDatePicker");
	    advancedDatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {            
	        public boolean onPreferenceChange(Preference preference, Object newValue) {
	        	SharedPreferences.Editor ed = prefs.edit();
				ed.putBoolean("advancedDatePicker", (Boolean)newValue);
				ed.commit();
	            return true;
	        }
	    }); 
	    
	    Preference reset = (Preference)findPreference("reset");
	    reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
	                    @Override
	                    public boolean onPreferenceClick(Preference arg0) { 
	                		numResultsPref.setSummary("Number of analysis results to display: " + 10);
	                		numResultsPref.setValue("10");
	                		stopwordsPref.setChecked(true);
	                		dumpPref.setChecked(false);
	                		analyzeAllPref.setChecked(false);
	                		advancedDatePref.setChecked(false);
	                        return true;
	                    }
	                });

	}
}
