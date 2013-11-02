package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class Analyzer {
	private HashMap<String, Integer> types;
	private HashMap<String, String> contactNames;
	private Context context;
	private final static String LOG_TAG = "Analyzer";

	public class Query {
		private String analysisType;
		private String startDate;
		private String endDate;
		private String contacts;

		public Query(String analysisType, String startDate, String endDate,
				String contacts) {
			this.analysisType = analysisType;
			this.startDate = startDate;
			this.endDate = endDate;
			this.contacts = contacts;
		}

		public String getAnalysisType() {
			return analysisType;
		}

		public void setAnalysisType(String analysisType) {
			this.analysisType = analysisType;
		}

		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getEndDate() {
			return endDate;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}

		public String getContacts() {
			return contacts;
		}

		public void setContacts(String contacts) {
			this.contacts = contacts;
		}

	}

	public Analyzer(Context context) {
		this.context = context;
		types = new HashMap<String, Integer>();
		String[] analysisTypes = context.getResources().getStringArray(
				R.array.analaysis_type_arrays);
		for (int index = 0; index < analysisTypes.length; index++)
			types.put(analysisTypes[index], index);

	}

	// Parsing the query and calling the correct method
	public ArrayList<Entry<String, Integer>> doQuery(Query query) {
		ArrayList<Entry<String, Integer>> result = new ArrayList<Entry<String, Integer>>();
		ArrayList<String> contactsList = parseContacts(query.getContacts());
		switch (types.get(query.getAnalysisType())) {
		case 0:
			result = wordFrequency("", query.getStartDate(),
					query.getEndDate(), contactsList);
			break;
		case 1:
			result = wordFrequency("sent", query.getStartDate(),
					query.getEndDate(), contactsList);
			break;
		case 2:
			result = wordFrequency("inbox", query.getStartDate(),
					query.getEndDate(), contactsList);
			break;
		case 3:
			result = smsFrequency("", query.getStartDate(), query.getEndDate(),
					contactsList);
			break;
		case 4:
			result = smsFrequency("sent", query.getStartDate(),
					query.getEndDate(), contactsList);
			break;
		case 5:
			result = smsFrequency("inbox", query.getStartDate(),
					query.getEndDate(), contactsList);
			break;
		}
		return result;

	}

	// parses contact strings of the following form
	// NAME <111-111-1111>, NAME <111-111-1111>, etc.
	// TODO also create the contacts hash here to save time
	private ArrayList<String> parseContacts(String contacts) {
		ArrayList<String> contactsList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("[^<]+<([^>]+)>");
		Matcher matcher = pattern.matcher(contacts);
		String number = "";
		while (matcher.find()) {
			number = PhoneNumberUtils.stripSeparators(matcher.group(1));
			contactsList
					.add(number);
		}
		return contactsList;
	}

	// creates a hash of numbers to names
	private void getContactNames() {
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] { Phone.DISPLAY_NAME, Phone.NUMBER }, null, null,
				Phone.DISPLAY_NAME + " ASC");
		contactNames = new HashMap<String, String>();
		if (cursor.moveToFirst()) {
			do {
				// key = parsed phone number, value = contact name
				contactNames.put(
						PhoneNumberUtils.stripSeparators(cursor.getString(1)),
						cursor.getString(0));
				Log.d(LOG_TAG,
						PhoneNumberUtils.stripSeparators(cursor.getString(1))
								+ " : " + cursor.getString(0));
			} while (cursor.moveToNext());
		}
	}

	// Temporary method to display the data in a textual format, should be
	// replaced with graphics soonish
	// AAAHHHH now it doesn't flatten just returns the set to help with the
	// graphing
	// TODO change name
	private ArrayList<Entry<String, Integer>> formatResult(
			Set<Entry<String, Integer>> entrySet) {
		ArrayList<Entry<String, Integer>> out = new ArrayList<Entry<String, Integer>>();
		out.addAll(entrySet);

		// TODO make only one pas to put the data in a form that the graph api
		// wants
		// for(Entry<String, Integer> entry : freq.entrySet())
		// {
		//
		// }

		Collections.sort(out, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> lhs,
					Entry<String, Integer> rhs) {
				return rhs.getValue() - lhs.getValue();
			}

		});

		return out;

		// Breaking open the entries and displaying them as strings, to be
		// replaced by a graphic
		// ArrayList<String> result = new ArrayList<String>();
		// for (int index = 0; index < out.size(); index++) {
		// result.add(out.get(index).getValue() + ": "
		// + out.get(index).getKey());
		// }
		// return result;
	}

	// TODO handle date range
	private ArrayList<Entry<String, Integer>> wordFrequency(String scope,
			String startDate, String endDate, ArrayList<String> contactsList) {
		String[] columnsForAnalysis;
		if (contactsList.size() == 0)
			columnsForAnalysis = new String[] { "body" };
		else
			columnsForAnalysis = new String[] { "body", "address" };
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/" + scope), columnsForAnalysis, null,
				null, null);
		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		int debugCount = 0;
		if (cursor.moveToFirst()) {
			do {
				// Checking numbers
				if (contactsList.size() != 0) {
					String number = PhoneNumberUtils.stripSeparators(cursor
							.getString(0));
					if (!contactsList.contains(number))
						continue;
				}
				// Grab all words without punctuation and ignoring case
				for (String s : cursor.getString(0).split("\\s+")) {
					s = s.toLowerCase(Locale.US).replaceAll("\\.|!|\\?|,", "");
					if (freq.containsKey(s))
						freq.put(s, freq.get(s) + 1);
					else
						freq.put(s, 1);
					Log.v(LOG_TAG, cursor.getString(0));
				}
				debugCount++;
			} while (cursor.moveToNext());
		}

		Log.d(LOG_TAG, debugCount + " messages total");

		return formatResult(freq.entrySet());
	}

	// TODO handle date range
	private ArrayList<Entry<String, Integer>> smsFrequency(String scope,
			String startDate, String endDate, ArrayList<String> contactsList) {
		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/" + scope),
				new String[] { "address" }, null, null, null);
		if (cursor.moveToFirst()) {
			getContactNames();
			do {
				String number = PhoneNumberUtils.stripSeparators(cursor
						.getString(0));
				if (contactsList.size() != 0 && !contactsList.contains(number))
					continue;
				String name = contactNames.get(number);
				if (name == null)
					name = number;
				// time for terrible performance to deal with those pesky
				// country codes
				for (String s : contactNames.keySet())
					if (PhoneNumberUtils.compare(s, number)) {
						name = contactNames.get(s);
						break;
					}
				if (freq.containsKey(name))
					freq.put(name, freq.get(name) + 1);
				else
					freq.put(name, 1);

				// Log.d(LOG_TAG, number + " : " + cursor.getString(1));
			} while (cursor.moveToNext());
		}

		return formatResult(freq.entrySet());
	}

}