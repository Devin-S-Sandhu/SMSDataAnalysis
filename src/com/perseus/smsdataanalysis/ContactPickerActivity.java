package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ContactPickerActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	private final static String LOG_TAG = "ContactPickerActivity";


	private ListView mainListView ;
	private ContactCheckbox[] contacts ;
	private ArrayAdapter<ContactCheckbox> listAdapter ;
	EditText inputSearch;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_picker);
		Log.i(LOG_TAG, "In ContactPickerActivity");

		// Find the ListView resource. 
		mainListView = (ListView) findViewById( R.id.list_view );
		inputSearch = (EditText) findViewById(R.id.inputSearch);

		mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick( AdapterView<?> parent, View item, 
					int position, long id) {
				ContactCheckbox contact = listAdapter.getItem( position );
				contact.toggleChecked();
				ContactViewHolder viewHolder = (ContactViewHolder) item.getTag();
				viewHolder.getCheckBox().setChecked( contact.isChecked() );
				Log.d(LOG_TAG, "Checkbox toggled: name:" + contact.getName() + " number: " +contact.getNumber() );
				if(contact.isChecked())
				{
					Log.d(LOG_TAG, "added into selectedContact");
					SmsUtil.selectedContact.put(contact.number, contact.name);
				}
				else
				{
					SmsUtil.selectedContact.remove(contact.number);
					Log.d(LOG_TAG, "removed from selectedContact");
				}
			}
		});


		contacts = (ContactCheckbox[]) getLastNonConfigurationInstance() ;
		if ( contacts == null ) {
			ArrayList<Contact> contactList = SmsUtil.getContacts(getBaseContext());
			int selectedContactSize = SmsUtil.selectedContact.size();
			contacts = new ContactCheckbox[contactList.size()+selectedContactSize];
			int i = 0;
			
			for(String num : SmsUtil.selectedContact.keySet())
			{
				contacts[i++] = new ContactCheckbox( SmsUtil.selectedContact.get(num), num, true);
			}
			while(i < contacts.length)
			{
				contacts[i] = new ContactCheckbox( contactList.get(i-selectedContactSize).contactName, contactList.get(i-selectedContactSize).num);
				i++;
			}
		}
		ArrayList<ContactCheckbox> contactList = new ArrayList<ContactCheckbox>();
		contactList.addAll( Arrays.asList(contacts) );

		// Set our custom array adapter as the ListView's adapter.
		listAdapter = new ContactArrayAdapter(this, contactList);
		mainListView.setAdapter( listAdapter );

		/**
		 * Enabling Search Filter
		 * */
		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				ContactPickerActivity.this.listAdapter.getFilter().filter(cs);   
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub                          
			}
		});
	}

	private static class ContactCheckbox {
		private String name = "" ;
		private String number = "";
		private boolean checked = false ;
		public ContactCheckbox( String name, String number ) {
			this.setName(name);
			this.setNumber(number);
		}
		public ContactCheckbox( String name, String number, boolean checked ) {
			this.setName(name);
			this.setNumber(number);
			this.setChecked(checked);
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isChecked() {
			return checked;
		}
		public void setChecked(boolean checked) {
			this.checked = checked;
		}
		public String toString() {
			return name ; 
		}
		public void toggleChecked() {
			checked = !checked ;
		}
		public String getNumber() {
			return number;
		}
		public void setNumber(String number) {
			this.number = number;
		}
	}

	/** Holds child views for one row. */
	private static class ContactViewHolder {
		private CheckBox checkBox ;
		private TextView textView ;
		public ContactViewHolder( TextView textView, CheckBox checkBox ) {
			this.checkBox = checkBox ;
			this.textView = textView ;
		}
		public CheckBox getCheckBox() {
			return checkBox;
		}
		public void setCheckBox(CheckBox checkBox) {
			this.checkBox = checkBox;
		}
		public TextView getTextView() {
			return textView;
		}
		public void setTextView(TextView textView) {
			this.textView = textView;
		}    
	}

	private static class ContactArrayAdapter extends ArrayAdapter<ContactCheckbox> {

		private LayoutInflater inflater;

		public ContactArrayAdapter( Context context, List<ContactCheckbox> contactList ) {
			super( context, R.layout.contact_picker_list_item, R.id.rowTextView, contactList );
			// Cache the LayoutInflate to avoid asking for a new one each time.
			inflater = LayoutInflater.from(context) ;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ContactCheckbox contact = (ContactCheckbox) this.getItem( position ); 

			// The child views in each row.
			CheckBox checkBox ; 
			TextView textView ; 

			// Create a new row view
			if ( convertView == null ) {
				convertView = inflater.inflate(R.layout.contact_picker_list_item, null);

				// Find the child views.
				textView = (TextView) convertView.findViewById( R.id.rowTextView );
				checkBox = (CheckBox) convertView.findViewById( R.id.CheckBox01 );

				// Optimization: Tag the row with it's child views, so we don't have to 
				// call findViewById() later when we reuse the row.
				convertView.setTag( new ContactViewHolder(textView,checkBox) );

				checkBox.setOnClickListener( new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v ;
						ContactCheckbox contact = (ContactCheckbox) cb.getTag();
						contact.setChecked( cb.isChecked() );
						Log.d(LOG_TAG, "Checkbox toggled: name:" + contact.getName() + " number: " +contact.getNumber() );
						if(contact.isChecked())
						{
							Log.d(LOG_TAG, "added into selectedContact");
							SmsUtil.selectedContact.put(contact.number, contact.name);
						}
						else
						{
							SmsUtil.selectedContact.remove(contact.number);
							Log.d(LOG_TAG, "removed from selectedContact");
						}
					}
				});        
			}
			// Reuse existing row view
			else {
				// Because we use a ViewHolder, we avoid having to call findViewById().
				ContactViewHolder viewHolder = (ContactViewHolder) convertView.getTag();
				checkBox = viewHolder.getCheckBox() ;
				textView = viewHolder.getTextView() ;
			}

			checkBox.setTag( contact ); 

			checkBox.setChecked( contact.isChecked() );
			textView.setText( new StringBuilder().append(contact.getName()).append(" <").append(contact.getNumber()).append(">").toString() );      

			return convertView;
		}

	}

	public Object onRetainNonConfigurationInstance() {
		return contacts ;
	}
	
	@Override
	public void finish() {
	  // Prepare data intent 
	  Intent data = new Intent();
	  // Activity finished ok, return the data
	  setResult(RESULT_OK, data);
	  super.finish();
	} 

}