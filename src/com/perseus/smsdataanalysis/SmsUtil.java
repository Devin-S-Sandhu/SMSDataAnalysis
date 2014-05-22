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
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

public class SmsUtil {

	public static HashMap<String, String> selectedContact = new HashMap<String, String>();
	private static ArrayList<Contact> contactList;
	
	private static void initalizeContacts(Context context){
		contactList = new ArrayList<Contact>();
		Cursor cursor = context.getContentResolver()
				.query(Phone.CONTENT_URI,
						new String[] { Phone._ID, Phone.DISPLAY_NAME,
						Phone.NUMBER }, null, null, null);
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			Contact contact = new Contact();
			contact.contactName = cursor.getString(cursor
					.getColumnIndex(Phone.DISPLAY_NAME));
			contact.num = cursor.getString(cursor
					.getColumnIndex(Phone.NUMBER));
			contactList.add(contact);
		}
		Collections.sort(contactList);

		Log.i("contactLength",String.valueOf(contactList.size()));
	}
	
	public static ArrayList<Contact> getSelectedContacts(){
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(String number : selectedContact.keySet())
			result.add(new Contact(selectedContact.get(number), number));
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
			if(selectedContact.containsKey(result.get(i).num))
				result.remove(i);
		}
		return contactList;
	}

	public static ArrayList<String> getContactsNumbers(Context context) {
		ArrayList<String> contacts = new ArrayList<String>();
		ArrayList<Contact> cList = getContacts(context);
		for(Contact contact : cList)
		{
			contacts.add(contact.num);
		}
		Log.i("contactLength",String.valueOf(contacts.size()));
		return contacts;
	}

}
