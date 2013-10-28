package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Analyzer {
	private HashMap<String, Integer> types;
	private final static String LOG_TAG = "Analyzer";

	public Analyzer() {
		types = new HashMap<String, Integer>();
		int ct = 0;
		types.put("Word Frequency", ct++);
		types.put("Word Frequency Sent", ct++);
		types.put("Word Frequency Received", ct++);

	}

	// Parsing the query and calling the correct method
	public ArrayList<String> query(String analysisType, String startDate,
			String endDate, String contacts, Context context) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> contactsList = parseContacts(contacts);
		switch (types.get(analysisType)) {
		case 0:
			result = wordFrequency("", startDate, endDate, contactsList,
					context);
			break;
		case 1:
			result = wordFrequency("sent", startDate, endDate, contactsList,
					context);
			break;
		case 2:
			result = wordFrequency("inbox", startDate, endDate, contactsList,
					context);
			break;
		}
		return result;

	}

	// parses contact strings of the follosing form
	// NAME <111-111-1111>, NAME <111-111-1111>, etc.
	private ArrayList<String> parseContacts(String contacts) {
		ArrayList<String> contactsList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("[^<]+<([^>]+)>");
		Matcher matcher = pattern.matcher(contacts);
		while (matcher.find()) {
			contactsList.add(matcher.group(1).replaceAll("\\D+",""));
		}
		return contactsList;
	}

	private ArrayList<String> wordFrequency(String scope, String startDate,
			String endDate, ArrayList<String> contactsList, Context context) {
		// debugging fun!
		for (String s : contactsList) {
			Log.v(LOG_TAG, s);
		}
		Log.v(LOG_TAG, "--------");
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/" + scope),
				new String[] { "body", "address" }, null, null, null);
		cursor.moveToFirst();
		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		do {
//			Checking numbers
			if(contactsList.size() != 0)
			{
				String number = cursor.getString(1);
				if(number.length() > 10)
					number.replace("+", "");
				if(!contactsList.contains(number))
					continue;
			}
			for (String s : cursor.getString(0).split(" ")) {
				if (freq.containsKey(s))
					freq.put(s, freq.get(s) + 1);
				else
					freq.put(s, 1);
				Log.v(LOG_TAG, cursor.getString(0) + " from " + cursor.getString(1));
			}
		} while (cursor.moveToNext());
		ArrayList<Entry<String, Integer>> out = new ArrayList<Entry<String, Integer>>();
		out.addAll(freq.entrySet());
		Collections.sort(out, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> lhs,
					Entry<String, Integer> rhs) {
				return rhs.getValue() - lhs.getValue();
			}

		});

		ArrayList<String> result = new ArrayList<String>();
		for (int index = 0; index < out.size(); index++) {
			result.add(out.get(index).getValue() + ": "
					+ out.get(index).getKey());
		}
		return result;

	}
}
