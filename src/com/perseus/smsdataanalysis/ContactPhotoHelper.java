package com.perseus.smsdataanalysis;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.util.Log;

public final class ContactPhotoHelper {
	public static Uri getPhotoUri(Context context, String id) {
		Log.d("ContactPhotoHelper", "getting photo for :" + id);
	    Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
	            .parseLong(id));
	    return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}
}
