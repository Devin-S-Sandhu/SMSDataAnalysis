/*  
 * From https://github.com/krishnalalstha/Spannable
 * By Krishna Lal Shrestha
 * Modified by Po-Chen Yang
 * 
 */
package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.util.Log;

public class SmsUtil {

	public static HashMap<String, String> selectedContact = new HashMap<String, String>();
	private static ArrayList<Contact> contactList;
	
	private static void initalizeContacts(Context context){
		contactList = new ArrayList<Contact>();
		HashSet<String> addedSet = new HashSet<String>();
		
		StringBuffer selection = new StringBuffer();
		selection.append(Data.HAS_PHONE_NUMBER).append("=1 AND ");
		selection.append(Data.MIMETYPE).append("='").append(Phone.CONTENT_ITEM_TYPE).append("'");
		Cursor cursor = context.getContentResolver()
				.query( Data.CONTENT_URI,
						new String[] { Data.RAW_CONTACT_ID, Data.DISPLAY_NAME_PRIMARY, Data.HAS_PHONE_NUMBER, Data.MIMETYPE}, selection.toString(), null, null);
		
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor
					.getColumnIndex(Data.RAW_CONTACT_ID));
			if(addedSet.contains(id))
				continue;
			Contact contact = new Contact();
			contact.contactName = cursor.getString(cursor
					.getColumnIndex(Data.DISPLAY_NAME_PRIMARY));
			contact.id = id;
			addedSet.add(id);
			contactList.add(contact);
		}
		Collections.sort(contactList);

		Log.i("contactLength",String.valueOf(contactList.size()));
	}
	
	public static ArrayList<Contact> getSelectedContacts(){
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(String id : selectedContact.keySet())
			result.add(new Contact(selectedContact.get(id), id));
		Collections.sort(result);
		return result;
	}
	
	public static ArrayList<Contact> getContacts(Context context){
		if(contactList == null)
			initalizeContacts(context);
		return contactList;
		
	}
	
	public static ArrayList<Contact> getUnselectedContacts(Context context) {
		if(contactList == null)
			initalizeContacts(context);
		ArrayList<Contact> result = (ArrayList<Contact>) contactList.clone();
		
		for(int i = 0; i < result.size(); i++)
		{
			if(selectedContact.containsKey(result.get(i).id))
				result.remove(i);
		}
		return contactList;
	}

	public static ArrayList<String> getContactsID(Context context) {
		ArrayList<String> contacts = new ArrayList<String>();
		ArrayList<Contact> cList = getContacts(context);
		for(Contact contact : cList)
		{
			contacts.add(contact.id);
		}
		Log.i("contactLength",String.valueOf(contacts.size()));
		return contacts;
	}

}
