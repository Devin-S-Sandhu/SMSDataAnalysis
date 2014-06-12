package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
		findViewById(R.id.back).setVisibility(View.GONE);

		mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick( AdapterView<?> parent, View item, 
					int position, long id) {
				ContactCheckbox contact = listAdapter.getItem( position );
				Log.d(LOG_TAG, "Checkbox toggled: name:" + contact.getName() + " number: " +contact.getID() );

				SmsUtil.selectedContact.put(contact.id, contact.name);
				
				Intent data = new Intent();
				data.putExtra("name", contact.getName());
				data.putExtra("ID", contact.getID());
				setResult(RESULT_OK, data);
				finish();
			}
		});


		contacts = (ContactCheckbox[]) getLastNonConfigurationInstance() ;
		if ( contacts == null ) {
			ArrayList<Contact> contactList = SmsUtil.getContactsArray(getBaseContext());
			contacts = new ContactCheckbox[contactList.size()-SmsUtil.selectedContact.size()];
			int i = 0;
			for(Contact c: contactList)
			{
				if(SmsUtil.selectedContact.containsKey(c.id))
					continue;
				Uri u = ContactPhotoHelper.getPhotoUri(this, c.id);
				contacts[i++] = new ContactCheckbox( c.contactName, c.id, u);
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
	
	public void clearSearch(View view)
	{
		inputSearch.setText("");
	}

	public void back(View view)
	{
		finish();
	}
	
	private static class ContactCheckbox {
		private String name = "" ;
		private String id = "";
		private Uri u = null;
		
		public ContactCheckbox( String name, String id, Uri u ) {
			this.setName(name);
			this.setID(id);
			this.setUri(u);
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String toString() {
			return name ; 
		}
		public String getID() {
			return id;
		}
		public void setID(String id) {
			this.id = id;
		}
		public Uri getUri() {
			return u;
		}
		public void setUri(Uri u) {
			this.u = u;
		}
	}

	/** Holds child views for one row. */
	private static class ContactViewHolder {
		private TextView textView ;
		private ImageView imageView ;
		public ContactViewHolder( TextView textView, ImageView imageView ) {
			this.textView = textView ;
			this.imageView = imageView ;
		}
		public TextView getTextView() {
			return textView;
		}
		public void setTextView(TextView textView) {
			this.textView = textView;
		}
		public ImageView getImageView() {
			return imageView;
		}
		public void setImageView(ImageView imageView) {
			this.imageView = imageView;
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
			TextView textView ; 
			ImageView contact_photo ;

			// Create a new row view
			if ( convertView == null ) {
				convertView = inflater.inflate(R.layout.contact_picker_list_item, null);

				// Find the child views.
				textView = (TextView) convertView.findViewById( R.id.rowTextView );
				convertView.findViewById( R.id.CheckBox01 ).setVisibility(View.GONE);

				contact_photo = ((ImageView) convertView.findViewById(R.id.contact_photo));

				// Optimization: Tag the row with it's child views, so we don't have to 
				// call findViewById() later when we reuse the row.
				convertView.setTag( new ContactViewHolder(textView,contact_photo) );
			}
			// Reuse existing row view
			else {
				// Because we use a ViewHolder, we avoid having to call findViewById().
				ContactViewHolder viewHolder = (ContactViewHolder) convertView.getTag();
				textView = viewHolder.getTextView() ;
				contact_photo = viewHolder.getImageView();
			}

			textView.setTag( contact ); 
			textView.setText(contact.getName() );

			if(contact.getUri() != null)
				contact_photo.setImageURI(contact.getUri());
			return convertView;
		}

	}

	public Object onRetainNonConfigurationInstance() {
		return contacts ;
	}
	

}