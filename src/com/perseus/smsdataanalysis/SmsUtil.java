package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

public class SmsUtil {

	public static HashMap<String, String> selectedContact = new HashMap<String, String>();
	private static HashMap<String, String> Phone_ID = new HashMap<String, String>();
	private static HashMap<String, HashSet<String>> ID_Phone;
	private static HashMap<String, String> contactNameList;

	private static void initalizeContacts(Context context){
		contactNameList = new HashMap<String, String>();

		StringBuffer selection = new StringBuffer();
		selection.append(Contacts.HAS_PHONE_NUMBER).append("=1");
		Cursor cursor = context.getContentResolver()
				.query( Contacts.CONTENT_URI,
						new String[] { Contacts._ID, Contacts.DISPLAY_NAME_PRIMARY, Contacts.HAS_PHONE_NUMBER}, selection.toString(), null, null);

		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor
					.getColumnIndex(Contacts._ID));
			String name = cursor.getString(cursor
					.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY));
			if(contactNameList.containsKey(id))
			{
				Log.d("SmsUtil.initalizeContact", "contain contact: " + name + ", " + id);
				continue;
			}
			contactNameList.put(id, name);
		}

		cursor.close();
		Log.i("contactLength",String.valueOf(contactNameList.size()));
	}
	
	private static void initalizePhoneList(Context context){
		ID_Phone = new HashMap<String,HashSet<String>>();
		
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/"), new String[] { "address"},
				null, null, null);
		String number, id;
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				number = cursor.getString(0);
				id = SmsUtil.getIDByPhone(context, number);
				if(ID_Phone.containsKey(id)){
					ID_Phone.get(id).add(number);
				}
				else{
					HashSet<String> tempSet = new HashSet<String>();
					tempSet.add(number);
					ID_Phone.put(id, tempSet);
				}
				cursor.moveToNext();
			}
		}
		cursor.close();
	}

	public static String getIDByPhone(Context context, String number) {
		if(Phone_ID.containsKey(number))
			return Phone_ID.get(number);
		Uri lookupUri = Uri.withAppendedPath(
				PhoneLookup.CONTENT_FILTER_URI, 
				Uri.encode(number));
		String[] mPhoneNumberProjection = {PhoneLookup.NUMBER, PhoneLookup._ID };
		Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
		try {
			if (cur.moveToFirst()) {
				String result = cur.getString(cur.getColumnIndex(PhoneLookup._ID));
				cur.close();
				Phone_ID.put(number, result);
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
		if(contactNameList == null)
			initalizeContacts(context);
		
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(String id : contactNameList.keySet())
			result.add(new Contact(contactNameList.get(id), id));
		
		Collections.sort(result);
		return result;
	}

	public static HashMap<String, String> getContacts(Context context){
		if(contactNameList == null)
			initalizeContacts(context);
		return contactNameList;
	}

	public static HashMap<String, HashSet<String>> getPhoneList(Context context){
		if(ID_Phone == null)
			initalizePhoneList(context);
		return ID_Phone;
	}

}