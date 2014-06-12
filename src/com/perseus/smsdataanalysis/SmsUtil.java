package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class SmsUtil {

	public static HashMap<String, String> selectedContact = new HashMap<String, String>();
	private static HashMap<String, String> contactList;

	private static void initalizeContacts(Context context){
		contactList = new HashMap<String, String>();

		StringBuffer selection = new StringBuffer();
		selection.append(Data.HAS_PHONE_NUMBER).append("=1");
		Cursor cursor = context.getContentResolver()
				.query( Data.CONTENT_URI,
						new String[] {Data.CONTACT_ID, Data.DISPLAY_NAME_PRIMARY, Data.HAS_PHONE_NUMBER}, selection.toString(), null, null);

		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor
					.getColumnIndex(Data.CONTACT_ID));
			String name = cursor.getString(cursor
					.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
			if(contactList.containsKey(id))
			{
				Log.d("SmsUtil.initalizeContact", "contain contact: " + name + ", " + id);
				continue;
			}
			contactList.put(id, name);
		}

		cursor.close();
		Log.i("contactLength",String.valueOf(contactList.size()));
	}

	public static String getIDByPhone(Context context, String number) {
		/// number is the phone number
		Uri lookupUri = Uri.withAppendedPath(
				PhoneLookup.CONTENT_FILTER_URI, 
				Uri.encode(number));
		String[] mPhoneNumberProjection = {PhoneLookup.NUMBER, PhoneLookup._ID };
		Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
		try {
			if (cur.moveToFirst()) {
				String result = cur.getString(cur.getColumnIndex(PhoneLookup._ID));
				cur.close();
				return result;
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		return null;
	}
	
	public static ArrayList<Contact> getSelectedContactsArray(){
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(String id : selectedContact.keySet())
			result.add(new Contact(selectedContact.get(id), id));
		Collections.sort(result);
		return result;
	}
	
	public static ArrayList<Contact> getContactsArray(Context context){
		if(contactList == null)
			initalizeContacts(context);
		
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(String id : contactList.keySet())
			result.add(new Contact(contactList.get(id), id));
		
		Collections.sort(result);
		return result;
	}

	public static HashMap<String, String> getContacts(Context context){
		if(contactList == null)
			initalizeContacts(context);
		return contactList;
	}

}