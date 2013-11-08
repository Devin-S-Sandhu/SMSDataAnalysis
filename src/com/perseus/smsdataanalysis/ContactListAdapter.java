package com.perseus.smsdataanalysis;

import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class ContactListAdapter extends CursorAdapter implements Filterable {
	private ContentResolver mCR;

	@SuppressWarnings("deprecation")
	public ContactListAdapter(Context context, Cursor c) {
		super(context, c);
		mCR = context.getContentResolver();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		((TextView) view).setText(new StringBuilder()
				.append(cursor.getString(1)).append(" ")
				.append(cursor.getString(2)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		final TextView view = (TextView) inflater.inflate(
				android.R.layout.simple_dropdown_item_1line, parent, false);
		view.setText(cursor.getString(1));
		return view;

	}

	@Override
	public String convertToString(Cursor cursor) {
		// output text
		return new StringBuilder().append(cursor.getString(1)).append(" <")
				.append(cursor.getString(2)).append(">").append(", ")
				.toString();
	}

	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (getFilterQueryProvider() != null) {
			return getFilterQueryProvider().runQuery(constraint);
		}

		StringBuilder buffer = null;
		String[] args = null;
		if (constraint != null) {
			buffer = new StringBuilder();
			buffer.append("UPPER(");
			buffer.append(Contacts.DISPLAY_NAME);
			buffer.append(") GLOB ?");
			args = new String[] { constraint.toString().toUpperCase(Locale.US)
					+ "*" };
		}

		return mCR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, // URI
				AnalysisMenuActivity.PEOPLE_PROJECTION, // projection
				buffer == null ? null : buffer.toString(), // selection
				args, // selectionArgs
				Contacts.DISPLAY_NAME // sortOrder
				);
	}
}