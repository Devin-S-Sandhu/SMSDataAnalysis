package com.perseus.smsdataanalysis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	private HashMap<String, String> scopes;
	private HashMap<String, String> contactNames;
	private Context context;
	private final static String LOG_TAG = "Analyzer";

	public class Query {
		private String analysisType;
		private String scope;
		private String startDate;
		private String endDate;
		private String contacts;

		public Query(String analysisType, String scope, String startDate,
				String endDate, String contacts) {
			this.analysisType = analysisType;
			this.setScope(scope);
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

		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

	}

	private class Pair<K, V> {

		private K element0;
		private V element1;

		public Pair(K element0, V element1) {
			this.element0 = element0;
			this.element1 = element1;
		}

		public K getElement0() {
			return element0;
		}

		public V getElement1() {
			return element1;
		}

		public void setElement0(K newVal) {
			this.element0 = newVal;
		}

		public void setElement1(V newVal) {
			this.element1 = newVal;
		}

	}

	public Analyzer(Context context) {
		this.context = context;
		contactNames = new HashMap<String, String>();
		types = new HashMap<String, Integer>();
		scopes = new HashMap<String, String>();
		String[] analysisTypes = context.getResources().getStringArray(
				R.array.analaysis_type_arrays);
		String[] analysisScopes = context.getResources().getStringArray(
				R.array.scope_array);

		for (int index = 0; index < analysisTypes.length; index++)
			types.put(analysisTypes[index], index);
		scopes.put(analysisScopes[0], "");
		scopes.put(analysisScopes[1], "sent");
		scopes.put(analysisScopes[2], "inbox");

	}

	// Parsing the query and calling the correct method
	public ArrayList<Entry<String, Integer>> doQuery(Query query) {
		ArrayList<Entry<String, Integer>> result = new ArrayList<Entry<String, Integer>>();
		ArrayList<String> contactsList = parseContacts(query.getContacts());
		Pair<Long, Long> range = parseDates(query.getStartDate(),
				query.getEndDate());
		switch (types.get(query.getAnalysisType())) {
		case 0:
			result = wordFrequency(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), contactsList);
			break;
		case 1:
			result = smsFrequency(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), contactsList);
			break;
		case 2:
			result = smsLength(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), contactsList);
			break;
		}
		return result;

	}

	// parses dates of the format MM-DD-YYYY into longs for use in analyses
	private Pair<Long, Long> parseDates(String startDate, String endDate) {

		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss",
				Locale.US);
		long start;
		long end;
		try {
			start = sdf.parse(startDate + " 00:00:00").getTime();
			end = sdf.parse(endDate + " 23:59:59").getTime();
			Log.d(LOG_TAG, sdf.format(start));
			Log.d(LOG_TAG, sdf.format(end));
		} catch (ParseException e) {
			// TODO do better error handling, prompt the user somehow
			Log.e(LOG_TAG, "Parse Error: " + e.getMessage());
			start = 0;
			end = System.currentTimeMillis();
		}
		return new Pair<Long, Long>(start, end);
	}

	// parses contact strings of the following form
	// NAME <111-111-1111>, NAME <111-111-1111>, etc.
	private ArrayList<String> parseContacts(String contacts) {
		ArrayList<String> contactsList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("([^<]+)<([^>]+)>,? ?");
		Matcher matcher = pattern.matcher(contacts);
		String number;
		String name;
		while (matcher.find()) {
			name = matcher.group(1);
			number = PhoneNumberUtils.stripSeparators(matcher.group(2));
			contactsList.add(number);
			contactNames.put(number, name);
			contactNames.put(matcher.group(2), name);
		}
		return contactsList;
	}

	// creates a hash of numbers to names
	private void getContactNames(ArrayList<String> contactsList) {
		// contact names was already populated in parseContacts()
		if (contactNames.size() != 0)
			return;
		Cursor cursor = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] { Phone.DISPLAY_NAME, Phone.NUMBER }, null, null,
				Phone.DISPLAY_NAME + " ASC");
		if (cursor.moveToFirst()) {
			do {
				// key = parsed phone number, value = contact name
				contactNames.put(
						PhoneNumberUtils.stripSeparators(cursor.getString(1)),
						cursor.getString(0));
			} while (cursor.moveToNext());
		}
	}

	// returns a cursor filled with the relevant texts
	private Cursor getCursor(String scope, String[] projection, Long startDate,
			Long endDate, ArrayList<String> contactsList) {
		StringBuilder selection = new StringBuilder("date BETWEEN " + startDate
				+ " AND " + endDate);
		if (contactsList.size() != 0) {
			selection.append(" AND (address");
			boolean first = true;
			for (String contact : contactsList) {
				if (!first)
					selection.append(" OR address");
				selection.append(" LIKE '%");
				selection.append(contact);
				selection.append("%'");
				first = false;
			}
			selection.append(")");
		}
		return context.getContentResolver().query(
				Uri.parse("content://sms/" + scope), projection,
				selection.toString(), null, null);
	}

	// Temporary method to display the data in a textual format, should be
	// replaced with graphics soonish
	private ArrayList<Entry<String, Integer>> formatResult(
			Set<Entry<String, Integer>> entrySet) {
		ArrayList<Entry<String, Integer>> out = new ArrayList<Entry<String, Integer>>();
		out.addAll(entrySet);

		Collections.sort(out, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> lhs,
					Entry<String, Integer> rhs) {
				return rhs.getValue() - lhs.getValue();
			}

		});

		return out;
	}

	private ArrayList<Entry<String, Integer>> wordFrequency(String scope,
			Long startDate, Long endDate, ArrayList<String> contactsList) {
		Cursor cursor = getCursor(scope, new String[] { "body" }, startDate,
				endDate, contactsList);
		HashMap<String, Integer> freq = new HashMap<String, Integer>();

		if (cursor.moveToFirst()) {
			do {
				// Grab all words without punctuation and ignoring case
				for (String s : cursor.getString(0).split("\\s+")) {
					s = s.toLowerCase(Locale.US).replaceAll("\\.|!|\\?|,", "");
					if (freq.containsKey(s))
						freq.put(s, freq.get(s) + 1);
					else
						freq.put(s, 1);
				}
			} while (cursor.moveToNext());
		}

		return formatResult(freq.entrySet());
	}

	private ArrayList<Entry<String, Integer>> smsFrequency(String scope,
			Long startDate, Long endDate, ArrayList<String> contactsList) {
		Cursor cursor = getCursor(scope, new String[] { "address" }, startDate,
				endDate, contactsList);

		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		String name;
		String number;
		if (cursor.moveToFirst()) {
			getContactNames(contactsList);
			do {
				number = PhoneNumberUtils.stripSeparators(cursor.getString(0));
				// if we don't have a name for the number let's try some fuzzy
				// matching and if that fails the number if their name
				if (contactNames.containsKey(number))
					name = contactNames.get(number);
				else {
					name = number;
					for (String s : contactNames.keySet())
						if (PhoneNumberUtils.compare(s, number)) {
							name = contactNames.get(s);
							contactNames.put(number, name);
							break;
						}
				}
				if (freq.containsKey(name))
					freq.put(name, freq.get(name) + 1);
				else
					freq.put(name, 1);

			} while (cursor.moveToNext());
		}

		return formatResult(freq.entrySet());
	}

	private ArrayList<Entry<String, Integer>> smsLength(String scope,
			Long startDate, Long endDate, ArrayList<String> contactsList) {
		Cursor cursor = getCursor(scope, new String[] { "body", "address" },
				startDate, endDate, contactsList);

		HashMap<String, Pair<Integer, Integer>> smsLength = new HashMap<String, Pair<Integer, Integer>>();
		int messageLength;
		String address;
		if (cursor.moveToFirst()) {
			getContactNames(contactsList);
			do {
				messageLength = cursor.getString(0).length();
				address = cursor.getString(1);
				// key is address, value is a pair
				// pairs are freq, total length
				if (smsLength.containsKey(address)) {
					Pair<Integer, Integer> pair = smsLength.get(address);
					pair.setElement0(pair.getElement0() + 1);
					pair.setElement1(pair.getElement1() + messageLength);
				} else
					smsLength.put(address, new Pair<Integer, Integer>(1,
							messageLength));
			} while (cursor.moveToNext());
		}

		// for now we return the address, but I'm keeping the frequency and
		// total length in case we change our minds later
		HashMap<String, Integer> average = new HashMap<String, Integer>();
		String name;
		for (String key : smsLength.keySet()) {
			if (contactNames.containsKey(key))
				name = contactNames.get(key);
			else {
				name = key;
				// time for terrible performance to deal with those pesky
				// country codes
				for (String s : contactNames.keySet())
					if (PhoneNumberUtils.compare(s, key)) {
						name = contactNames.get(s);
						contactNames.put(key, name);
						break;
					}
			}
			average.put(name, (Integer) smsLength.get(key).getElement1()
					/ smsLength.get(key).getElement0());
		}

		return formatResult(average.entrySet());
	}

}
