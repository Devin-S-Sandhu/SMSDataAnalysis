package com.perseus.smsdataanalysis;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;

public final class ContactPhotoHelper {
	public static Uri getPhotoUri(Context context, String rawID) {
		String id;
	    try {
	        Cursor cur = context.getContentResolver().query(
	                ContactsContract.Data.CONTENT_URI,
	                null,
	                ContactsContract.Data.RAW_CONTACT_ID + "=" + rawID + " AND "
	                        + ContactsContract.Data.MIMETYPE + "='"
	                        + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
	                null);
	        if (cur != null) {
	            if (!cur.moveToFirst()) {
	                return null; // no photo
	            }
	            id = cur.getString(cur
						.getColumnIndex(Data.CONTACT_ID));
	        } else {
	            return null; // error in cursor process
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
	            .parseLong(id));
	    return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}
}
