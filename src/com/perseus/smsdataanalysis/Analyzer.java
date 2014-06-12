package com.perseus.smsdataanalysis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;

public class Analyzer {
	private static final String[] SET_VALUES = new String[] {"a","about","above","after","again","against","all","am","an","and","any","are","aren't","as","at","be","because","been","before","being","below","between","both","but","by","can't","cannot","could","couldn't","did","didn't","do","does","doesn't","doing","don't","down","during","each","few","for","from","further","had","hadn't","has","hasn't","have","haven't","having","he","he'd","he'll","he's","her","here","here's","hers","herself","him","himself","his","how","how's","i","i'd","i'll","i'm","i've","if","in","into","is","isn't","it","it's","its","itself","let's","me","more","most","mustn't","my","myself","no","nor","not","of","off","on","once","only","or","other","ought","our","ours","ourselves","out","over","own","same","shan't","she","she'd","she'll","she's","should","shouldn't","so","some","such","than","that","that's","the","their","theirs","them","themselves","then","there","there's","these","they","they'd","they'll","they're","they've","this","those","through","to","too","under","until","up","very","was","wasn't","we","we'd","we'll","we're","we've","were","weren't","what","what's","when","when's","where","where's","which","while","who","who's","whom","why","why's","with","won't","would","wouldn't","you","you'd","you'll","you're","you've","your","yours","yourself","yourselves", "", "@", "#","$" ,"\\", "/", "%", "^","&","*","(",")"};
	private static final Set<String> STOP_WORDS = new HashSet<String>(Arrays.asList(SET_VALUES));
	private boolean skip_stop_words;
	private boolean analyze_all_sms;
	private boolean analyze_all_contact;

	private HashMap<String, Integer> types;
	private HashMap<String, String> scopes;
	private HashMap<String, String> allContact;
	private Context context;
	private final static Long MILLISEC_TO_HOURS = 3600000l;
	private final static String LOG_TAG = "Analyzer";

	public class Query {
		private String analysisType;
		private String scope;
		private String startDate;
		private String endDate;
		private HashMap<String, String> contacts;

		public Query(String analysisType, String scope, String startDate,
				String endDate, HashMap<String, String> contacts) {
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

		public HashMap<String, String> getContacts() {
			return contacts;
		}

		public void setContacts(HashMap<String, String> contacts) {
			this.contacts = contacts;
		}

		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

	}

	public class Pair<K, V> {

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
		skip_stop_words = true;
		analyze_all_sms = false;
		analyze_all_contact = false;
		this.context = context;
		allContact = SmsUtil.getContacts(context);
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

	public void enableStopwords(Boolean enable){
		skip_stop_words = enable;
	}

	public void enableAnalyzeAll(Boolean enable){
		analyze_all_sms = enable;
	}

	// Parsing the query and calling the correct method
	public ArrayList<Pair<String, Integer>> doQuery(Query query) {
		ArrayList<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
		Set<String> idList = query.getContacts().keySet();
		if(idList.size() == 0)
		{
			idList = allContact.keySet();
			analyze_all_contact = true;
		}
		Pair<Long, Long> range = parseDates(query.getStartDate(),
				query.getEndDate());
		switch (types.get(query.getAnalysisType())) {
		case 0:
			result = wordFrequency(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), idList);
			break;
		case 1:
			result = smsFrequency(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), idList);
			break;
		case 2:
			result = smsLength(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), idList,
					false);
			break;
		case 3:
			result = smsLength(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), idList,
					true);
			break;
		case 4:
			result = smsInterval(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), idList,
					false);
			break;
		case 5:
			result = smsInterval(scopes.get(query.getScope()),
					range.getElement0(), range.getElement1(), idList,
					true);
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
	private ArrayList<String> parseContacts(HashMap<String, String> contacts) {
		ArrayList<String> contactsList = new ArrayList<String>();
		for(String id : contacts.keySet())
		{
			contactsList.add(id);
		}
		Log.d(LOG_TAG, contactsList.toString());
		if (contactsList.size() == 0)
		{
			for(String id : allContact.keySet())
			{
				contactsList.add(id);
			}
		}
		return contactsList;
	}

	// returns a cursor filled with the relevant texts
	private Cursor getCursor(String scope, String[] projection, Long startDate,
			Long endDate, String sortOrder) {
		StringBuilder selection = new StringBuilder("date BETWEEN " + startDate
				+ " AND " + endDate);
		Uri u;
		if(scope.equalsIgnoreCase("inbox"))
			u = Telephony.Sms.Inbox.CONTENT_URI;
		else if(scope.equalsIgnoreCase("sent"))
			u = Telephony.Sms.Sent.CONTENT_URI;
		else
			u = Telephony.Sms.CONTENT_URI;	
		Cursor result = context.getContentResolver().query(
				u, projection,
				selection.toString(), null, sortOrder);
		return result;
	}

	// Creates an arraylist of pairs to return and be graphed
	// Also, if contacts list is not makes sure that each contact is in the out
	// array
	private ArrayList<Pair<String, Integer>> formatResult(
			HashMap<String, Integer> hash, boolean reallyIncludeAllContacts) {
		ArrayList<Pair<String, Integer>> out = new ArrayList<Pair<String, Integer>>();

		HashSet<String> tempNameList = new HashSet<String>();

		for (Entry<String, Integer> e : hash.entrySet())
		{
			if(tempNameList.contains(e.getKey()))
			{
				Pair<String, Integer> duplication = out.get(out.indexOf(e.getKey()));
				duplication.setElement1(duplication.getElement1()+e.getValue());
			}
			else
			{
				out.add(new Pair<String, Integer>(e.getKey(), e.getValue()));
				tempNameList.add(e.getKey());
			}
		}
		
		// if given a contact list, check if we haven't added a contact to the
		// out list and add them with a dummy value
//		if (includeAllContacts && reallyIncludeAllContacts) {
//			for (String contact : contactNames.values()) {
//				if (!hash.containsKey(contact))
//				{
//					if(!tempNameList.contains(contact))
//					{
//						out.add(new Pair<String, Integer>(contact, 0));
//						tempNameList.add(contact);
//					}
//				}
//			}
//		}

		Collections.sort(out, new Comparator<Pair<String, Integer>>() {

			@Override
			public int compare(Pair<String, Integer> lhs,
					Pair<String, Integer> rhs) {
				return rhs.getElement1() - lhs.getElement1();
			}

		});

		return out;
	}

	private ArrayList<Pair<String, Integer>> wordFrequency(String scope,
			Long startDate, Long endDate, Set<String> idList) {
		Cursor cursor = getCursor(scope, new String[] { "address", "body" }, startDate,
				endDate, null);
		HashMap<String, Integer> freq = new HashMap<String, Integer>();

		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				String id = cursor.getString(0);
				id = SmsUtil.getIDByPhone(context, id);
				if(!analyze_all_sms && (id == null || !idList.contains(id)))
				{
					cursor.moveToNext();
					continue;
				}
				
				// Grab all words without punctuation and ignoring case
				for (String s : cursor.getString(1).split("\\s+")) {
					s = s.toLowerCase(Locale.US).replaceAll("\\.|!|\\?|,", "");
					if(skip_stop_words && STOP_WORDS.contains(s))
						continue;
					if (freq.containsKey(s))
						freq.put(s, freq.get(s) + 1);
					else
						freq.put(s, 1);
				}
				cursor.moveToNext();
			}
		}

		// we graph words not contacts so we don't need to pass contactslist to
		// 
		cursor.close();
		return formatResult(freq, false);
	}

	private ArrayList<Pair<String, Integer>> smsFrequency(String scope,
			Long startDate, Long endDate, Set<String> idList) {
		Cursor cursor = getCursor(scope, new String[] {"address" }, startDate,
				endDate, null);
		Log.d(LOG_TAG, "smsfreqcount: " + cursor.getCount());

		HashMap<String, Integer> freq = new HashMap<String, Integer>();
		String name;
		String id;
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				id = cursor.getString(0);
				Log.d(LOG_TAG, "person: " + id);
				id = SmsUtil.getIDByPhone(context, id);
				Log.d(LOG_TAG, "person: " + id);
				if(id == null || !idList.contains(id))
				{
					cursor.moveToNext();
					continue;
				}
				name = allContact.get(id);
				if (freq.containsKey(name))
					freq.put(name, freq.get(name) + 1);
				else
					freq.put(name, 1);
				cursor.moveToNext();
			}
		}
		Log.d(LOG_TAG, "### " + freq.toString());
		cursor.close();
		return formatResult(freq, true);
	}
	private ArrayList<Pair<String, Integer>> smsLength(String scope,
			Long startDate, Long endDate, Set<String> idList,
			boolean reverse) {
		Cursor cursor = getCursor(scope, new String[] { "body", "person" },
				startDate, endDate, null);

		HashMap<String, Pair<Integer, Integer>> smsLength = new HashMap<String, Pair<Integer, Integer>>();
		int messageLength;
		String person;
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				messageLength = cursor.getString(0).length();
				person = cursor.getString(1);
				Log.d(LOG_TAG, messageLength +" "+person);
				if(person == null || messageLength < 1)
				{
					Log.e(LOG_TAG, "Null D:");
					//added by Po-Chen to ignore the data with null
					cursor.moveToNext();
					continue;
				}

				// key is person, value is a pair
				// pairs are freq, total length
				if (smsLength.containsKey(person)) {
					Pair<Integer, Integer> pair = smsLength.get(person);
					pair.setElement0(pair.getElement0() + 1);
					pair.setElement1(pair.getElement1() + messageLength);
				} else
					smsLength.put(person, new Pair<Integer, Integer>(1,
							messageLength));
				cursor.moveToNext();
			}
		}

		// for now we return the average, but I'm keeping the frequency and
		// total length in case we change our minds later
		HashMap<String, Integer> average = new HashMap<String, Integer>();
		String name;
		for (String key : smsLength.keySet()) {
			name = allContact.get(key);
			average.put(name, (Integer) smsLength.get(key).getElement1()
					/ smsLength.get(key).getElement0());
		}

		ArrayList<Pair<String, Integer>> result = formatResult(average, true);
		if (reverse)
			Collections.reverse(result);
		cursor.close();
		return result;
	}

	private ArrayList<Pair<String, Integer>> smsInterval(String scope,
			Long startDate, Long endDate, Set<String> idList,
			boolean reverse) {
		Cursor cursor = getCursor(scope, new String[] { "person", "date" },
				startDate, endDate, "person,date DESC");

		HashMap<String, Pair<Long, Integer>> smsInterval = new HashMap<String, Pair<Long, Integer>>();
		String oldAddress;
		String curAddress;
		Long oldDate;
		Long curDate;

		if (cursor.moveToFirst()) {
			oldAddress = cursor.getString(0);
			oldDate = cursor.getLong(1);
			cursor.moveToNext();
			while (!cursor.isAfterLast()) {
				Log.d(LOG_TAG, cursor.getString(0) +" "+cursor.getLong(1));
				curAddress = cursor.getString(0);
				curDate = cursor.getLong(1);
				if(curAddress == null)
				{
					Log.e(LOG_TAG, "Null D:");
					//added by Po-Chen to ignore the data with null
					cursor.moveToNext();
					continue;
				}
				// if we're still looking at messages to and from the same
				// person then update the smsInterval
				if (curAddress.equals(oldAddress)) {
					if (smsInterval.containsKey(curAddress)) {
						Pair<Long, Integer> pair = smsInterval.get(curAddress);
						pair.setElement0(pair.getElement0()
								+ Math.abs(oldDate - curDate));
						pair.setElement1(pair.getElement1() + 1);
					} else {
						smsInterval.put(curAddress, new Pair<Long, Integer>(
								Math.abs(oldDate - curDate), 1));
					}

				}

				oldAddress = curAddress;
				oldDate = curDate;
				cursor.moveToNext();
			}
		}

		HashMap<String, Integer> average = new HashMap<String, Integer>();
		String name;
		Long hours;
		for (String key : smsInterval.keySet()) {
			name = allContact.get(key);
			hours = (smsInterval.get(key).getElement0() / smsInterval.get(key).getElement1()) / MILLISEC_TO_HOURS;
			Log.d(LOG_TAG, name);
			Log.d(LOG_TAG,""+hours);
			Log.d(LOG_TAG, ""+hours.intValue());
			if(hours.intValue() != 0)
				average.put(name, hours.intValue());
		}

		ArrayList<Pair<String, Integer>> result = formatResult(average, true);
		if (reverse){
			Collections.reverse(result);
		}
		cursor.close();
		return result;
	}
}