package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
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

	public class Query {
		private String analysisType;
		private String startDate;
		private String endDate;
		private String contacts;
		private Context context;

		public Query(String analysisType, String startDate, String endDate,
				String contacts, Context context) {
			this.analysisType = analysisType;
			this.startDate = startDate;
			this.endDate = endDate;
			this.contacts = contacts;
			this.context = context;
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

		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			this.context = context;
		}

	}

	public Analyzer() {
		// TODO make this an enum, or something an array in xml
		types = new HashMap<String, Integer>();
		int ct = 0;
		types.put("Word Frequency", ct++);
		types.put("Word Frequency Sent", ct++);
		types.put("Word Frequency Received", ct++);

	}

	// Parsing the query and calling the correct method
	public ArrayList<String> doQuery(Query query) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> contactsList = parseContacts(query.getContacts());
		switch (types.get(query.getAnalysisType())) {
		case 0:
			result = wordFrequency("", query.getStartDate(),
					query.getEndDate(), contactsList, query.getContext());
			break;
		case 1:
			result = wordFrequency("sent", query.getStartDate(),
					query.getEndDate(), contactsList, query.getContext());
			break;
		case 2:
			result = wordFrequency("inbox", query.getStartDate(),
					query.getEndDate(), contactsList, query.getContext());
			break;
		}
		return result;

	}

	// parses contact strings of the following form
	// NAME <111-111-1111>, NAME <111-111-1111>, etc.
	private ArrayList<String> parseContacts(String contacts) {
		ArrayList<String> contactsList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("[^<]+<([^>]+)>");
		Matcher matcher = pattern.matcher(contacts);
		while (matcher.find()) {
			contactsList.add(matcher.group(1).replaceAll("\\D+", ""));
		}
		return contactsList;
	}

	// TODO handle date range
	private ArrayList<String> wordFrequency(String scope, String startDate,
			String endDate, ArrayList<String> contactsList, Context context) {
		String[] columnsForAnalysis;
		if (contactsList.size() == 0)
			columnsForAnalysis = new String[] { "body" };
		else
			columnsForAnalysis = new String[] { "body", "address" };
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/" + scope), columnsForAnalysis, null,
				null, null);
		cursor.moveToFirst();
		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		int count = 0;
		do {
			// Checking numbers
			if (contactsList.size() != 0) {
				String number = cursor.getString(1);
				if (number.length() > 10)
					number.replace("+", "");
				if (!contactsList.contains(number))
					continue;
			}
			for (String s : cursor.getString(0).split("\\s+")) {
				s = s.toLowerCase(Locale.US).replaceAll("\\.|!|\\?|,", "");
				if (freq.containsKey(s))
					freq.put(s, freq.get(s) + 1);
				else
					freq.put(s, 1);
				Log.v(LOG_TAG, cursor.getString(0));
			}
			count++;
		} while (cursor.moveToNext());
		
		Log.d(LOG_TAG, count + " messages total");
		
		ArrayList<Entry<String, Integer>> out = new ArrayList<Entry<String, Integer>>();
		out.addAll(freq.entrySet());
		
//		TODO make only one pas to put the data in a form that the graph api wants
//		for(Entry<String, Integer> entry : freq.entrySet())
//		{
//			
//		}
		
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
