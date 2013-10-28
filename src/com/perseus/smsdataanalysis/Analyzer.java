package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

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

	private ArrayList<String> parseContacts(String contacts) {
		// TODO parse contact data
		return null;
	}

	// Bleeding edge development method
	// Should look like a word frequency
	private ArrayList<String> wordFrequency(String scope, String startDate,
			String endDate, ArrayList<String> contactsList, Context context) {
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/" + scope), new String[] { "body" },
				null, null, null);
		cursor.moveToFirst();
		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		do {
			for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
				for (String s : cursor.getString(idx).split(" ")) {
					if (freq.containsKey(s))
						freq.put(s, freq.get(s) + 1);
					else
						freq.put(s, 1);
				}
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
