package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ContactPickerActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	private final static String LOG_TAG = "SMSDataAnalysis_tag";

	// List view
	private ListView lv;

	// Listview Adapter
	ArrayAdapter<String> adapter;

	// Search EditText
	EditText inputSearch;

	// ArrayList for Listview
	ArrayList<HashMap<String, String>> productList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_picker);

		// Listview Data
		ArrayList<Contact> contactList = SmsUtil.getContacts(getBaseContext());
		String products[] = new String[contactList.size()];
		for(int i = 0; i < products.length; i++){
			products[i] = new StringBuilder().append(contactList.get(i).contactName).append(" <").append(contactList.get(i).num).append(">").toString();
		}

		lv = (ListView) findViewById(R.id.list_view);
		inputSearch = (EditText) findViewById(R.id.inputSearch);

		// Adding items to listview
		adapter = new ArrayAdapter<String>(this, R.layout.contact_picker_list_item, R.id.product_name, products);
		lv.setAdapter(adapter);

		/**
		 * Enabling Search Filter
		 * */
		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				ContactPickerActivity.this.adapter.getFilter().filter(cs);   
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
}