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

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class SmsUtil {

	public static HashMap<String, String> selectedContact = new HashMap<String, String>();
	private static ArrayList<Contact> contactList;
	
	private static void initalizeContacts(Context context){
		contactList = new ArrayList<Contact>();
		Cursor cursor = context.getContentResolver()
				.query(ContactsContract.RawContacts.CONTENT_URI,
						new String[] { ContactsContract.RawContacts._ID, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY}, null, null, null);
		
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			Contact contact = new Contact();
			contact.contactName = cursor.getString(cursor
					.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
			contact.id = cursor.getString(cursor
					.getColumnIndex(ContactsContract.RawContacts._ID));
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
