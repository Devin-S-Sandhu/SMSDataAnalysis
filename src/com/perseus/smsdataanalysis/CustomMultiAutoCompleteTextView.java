/*  
 * From https://github.com/krishnalalstha/Spannable
 * By Krishna Lal Shrestha
 * Modified by Po-Chen Yang
 * 
 */

package com.perseus.smsdataanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class CustomMultiAutoCompleteTextView extends MultiAutoCompleteTextView {

	private Context context;
	private LayoutInflater layoutInflater;
	public boolean isCharaterAdded = true, isContactAddedFromDb = false,
			isTextAdditionInProgress = false, checkValidation = true,
			isTextDeletedFromTouch = false;
	private int beforeChangeIndex = 0, afterChangeIndex = 0, stringLength = 0;
	private String changeString = "";

	public CustomMultiAutoCompleteTextView(Context context) {
		super(context);
		init(context);
	}

	public CustomMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CustomMultiAutoCompleteTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * Method called at each object initialization
	 * 
	 * @param context
	 */
	public void init(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		this.addTextChangedListener(textWatcher);
		this.setThreshold(0);
		this.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

		/*
		 * on each item click a new contact is added,so once this is added,it
		 * must not appear on the contact list of this TextView
		 */
		this.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Contact contact = (Contact) parent.getItemAtPosition(position);
				SmsUtil.selectedContact.put(contact.num, contact.contactName);
				updateQuickContactList();
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.MultiAutoCompleteTextView#replaceText(java.lang.CharSequence
	 * ) this method is called whenever there is text replacing going on as soon
	 * as the user clicks the list item and we have assumed that all the contact
	 * in the contact list need not go for validation
	 */
	@Override
	protected void replaceText(CharSequence text) {
		Log.d("replaceText", "text: " + text);
		checkValidation = false;
		super.replaceText(text);
	}

	private TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			Log.d("onTextChanged", "s: " + s + " start: " + start + " before: "
					+ before + " count: " + count);
			// phoneNum.getText().setSpan(new ForegroundColorSpan(Color.BLACK),
			// before, before, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			String addedString = s.toString();
			if (!isTextAdditionInProgress) {
				Log.i("onTextChanged", "!isTextAdditionInProgress");
				if (stringLength < addedString.length()) {
					Log.i("onTextChanged",
							"stringLength < addedString.length()");
					// something is added

					if (!TextUtils.isEmpty(addedString.trim())) {
						Log.i("onTextChanged",
								"!TextUtils.isEmpty(addedString.trim())");
						int startIndex = isContactAddedFromDb ? addedString
								.length()
								: CustomMultiAutoCompleteTextView.this
										.getSelectionEnd();
						startIndex = startIndex < 1 ? 1 : startIndex;
						String charAtStartIndex = Character
								.toString(addedString.charAt(startIndex - 1));
						Log.d("onTextChanged", "charAtStartIndex: "
								+ charAtStartIndex);

						if (charAtStartIndex.equals(",")) {
							Log.i("onTextChanged",
									"charAtStartIndex.equals(\",\")");
							isTextAdditionInProgress = true;
							addOrCheckSpannable(s, startIndex);
						}
					}
				}
			}

			stringLength = addedString.length();

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			Log.d("beforeTextChanged", "s: " + s + " start: " + start
					+ " count: " + count + " after: " + after);
			beforeChangeIndex = CustomMultiAutoCompleteTextView.this
					.getSelectionStart();
			changeString = s.toString();

		}

		@Override
		public void afterTextChanged(Editable s) {
			Log.d("afterTextChanged", CustomMultiAutoCompleteTextView.this
					.getText().toString());
			afterChangeIndex = CustomMultiAutoCompleteTextView.this
					.getSelectionEnd();
			if (!isTextDeletedFromTouch
					&& s.toString().length() < changeString.length()
					&& !isTextAdditionInProgress) {
				String deletedString = "";
				String deletedContact = "";
				Log.d("afterTextChanged",
						"afterChangeIndex: "
								+ Integer.toString(afterChangeIndex));
				Log.d("afterTextChanged",
						"beforeChangeIndex"
								+ Integer.toString(beforeChangeIndex));
				deletedString = changeString.substring(afterChangeIndex,
						beforeChangeIndex);
				if (deletedString.length() > 0 && deletedString.contains(",")) {
					String currentText = CustomMultiAutoCompleteTextView.this
							.getText().toString();
					int selectionStart = CustomMultiAutoCompleteTextView.this
							.getSelectionStart();
					Log.d("afterTextChanged", "currentText: " + currentText);
					Log.d("afterTextChanged",
							"selectionStart: "
									+ Integer.toString(selectionStart));
					deletedString = currentText.substring(0, selectionStart);
					Log.d("afterTextChanged", "deletedString: " + deletedString);
					int prev = deletedString.lastIndexOf(",");
					Log.d("afterTextChanged", "prev: " + Integer.toString(prev));
					deletedString = deletedString.substring(prev + 1);
					deletedContact = deletedString.split(" <")[0];
					Log.d("afterTextChanged", "deletedString: " + deletedString);
					deleteString(prev + 1, prev + deletedString.length() + 1);
					Log.d("afterTextChanged",
							"prev: " + prev + " deletedString.length():"
									+ deletedString.length());
				}

				if (!TextUtils.isEmpty(deletedContact.trim()))
					deleteFromHashMap(deletedContact);
			}
		}
	};

	public void addOrCheckSpannable(CharSequence s, int startIndex) {
		Log.d("addOrCheckSpannable", "s: " + s + " startIndex: " + startIndex);

		boolean checkSpannable = false;
		String overallString;
		if (s == null) {
			checkSpannable = true;
			s = this.getText();
			startIndex = this.getSelectionEnd();
			startIndex = startIndex < 1 ? 1 : startIndex;
			// startIndex =-1;
			overallString = this.getText().toString();
			if (TextUtils.isEmpty(overallString.trim()))
				return;
		} else {
			overallString = s.toString();
			startIndex = startIndex - 1;
		}
		Log.d("addOrCheckSpannable", "overallString: " + overallString
				+ " startIndex: " + startIndex);

		int spanEnd = 0;
		for (int i = startIndex - 1; i >= 0; i--) {
			Character c = overallString.charAt(i);
			if (c == ',') {
				spanEnd = i;
				break;
			}
		}
		Log.d("addOrCheckSpannable", "spanEnd: " + spanEnd);

		SpannableStringBuilder ssb = new SpannableStringBuilder(s);
		int cursorCurrentPoint = this.getSelectionEnd();
		boolean addedFromFirst = cursorCurrentPoint < overallString.length();

		ClickableSpan[] spans = ssb.getSpans(0,
				addedFromFirst ? spanEnd : ssb.length(), ClickableSpan.class);
		Log.d("addOrCheckSpannable", "spans: " + spans.toString()
				+ " spans.length: " + spans.length);

		boolean someUnknownChange = false;
		if (spans.length > 0) {
			ClickableSpan underlineSpan = spans[spans.length - 1];
			spanEnd = ssb.getSpanEnd(underlineSpan);

			int k = 0;
			for (int m = 0; m < spans.length; m++) {
				ClickableSpan someSpan = spans[m];

				int end = ssb.getSpanEnd(someSpan);
				;
				if (k < end)
					k = end;
			}

			spanEnd = k;

			if (spanEnd < overallString.length()) {
				Character c = overallString.charAt(spanEnd);
				if (c == ',') {
					spanEnd += 1;
				} else {
					ssb.insert(spanEnd, ",");
					spanEnd += 1;
					startIndex += 1;
					someUnknownChange = true;
				}
			}
		}

		if (startIndex > -1 && spanEnd > -1) {
			Log.i("addOrCheckSpannable", "startIndex > -1 && spanEnd > -1");

			if (checkSpannable) {
				Log.i("addOrCheckSpannable", "checkSpannable");
				ClickableSpan[] span = someUnknownChange ? ssb.getSpans(
						spanEnd - 1, startIndex - 1, ClickableSpan.class) : ssb
						.getSpans(spanEnd, startIndex, ClickableSpan.class);
				if (span.length > 0) {
					// has span
				} else if (startIndex > spanEnd) {
					// ssb.replace(someUnknownChange?spanEnd-1:spanEnd,
					// someUnknownChange?startIndex-1:startIndex , "");
					ssb.replace(spanEnd, startIndex, "");

				}
				return;
			} else {
				Log.i("addOrCheckSpannable", "!checkSpannable");
				// this is to checked whether the user deletes comma and adds
				// again
				if ((Math.abs(spanEnd - 1 - startIndex) > 1)) {
					Log.i("addOrCheckSpannable",
							"(Math.abs(spanEnd - 1 - startIndex) > 1");
					String userInputString = someUnknownChange ? overallString
							.substring(spanEnd - 1, startIndex - 1)
							: overallString.substring(spanEnd, startIndex);
					String trimString = userInputString.trim();
					if (trimString.length() == 0) {
						ssb.replace(spanEnd, startIndex + 1, "");

					} else {
						if (userInputString
								.charAt(userInputString.length() - 1) == ','
								&& spanEnd - 1 >= 0 && startIndex - 1 >= 0)
							userInputString = overallString.substring(
									spanEnd - 1, startIndex - 1);

						if (checkValidation) {
							Log.i("addOrCheckSpannable", "checkValidation");

							if (PhoneNumberUtils
									.isGlobalPhoneNumber(userInputString)) {

								BitmapDrawable bmpDrawable = getBitmapFromText(userInputString);
								bmpDrawable.setBounds(0, 0,
										bmpDrawable.getIntrinsicWidth(),
										bmpDrawable.getIntrinsicHeight());
								ssb.setSpan(new ImageSpan(bmpDrawable),
										spanEnd, startIndex,
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								// mm=phoneNum.getMovementMethod();
								this.setMovementMethod(LinkMovementMethod
										.getInstance());
								ClickableSpan clickSpan = new ClickableSpan() {

									@Override
									public void onClick(View view) {
										deleteString();
									}

								};
								ssb.setSpan(clickSpan, spanEnd, startIndex,
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

							} else {
								ssb.replace(spanEnd, startIndex + 1, "");

							}
						} else {
							Log.i("addOrCheckSpannable", "!checkValidation");

							BitmapDrawable bmpDrawable = getBitmapFromText(userInputString);
							bmpDrawable.setBounds(0, 0,
									bmpDrawable.getIntrinsicWidth(),
									bmpDrawable.getIntrinsicHeight());
							ssb.setSpan(new ImageSpan(bmpDrawable), spanEnd,
									startIndex,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							// mm=phoneNum.getMovementMethod();
							this.setMovementMethod(LinkMovementMethod
									.getInstance());

							ClickableSpan clickSpan = new ClickableSpan() {

								@Override
								public void onClick(View view) {
									deleteString();
								}

							};
							ssb.setSpan(clickSpan, spanEnd, startIndex,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						}
					}

				}
			}
			Log.d("AddOrCheckSpannable", "ssb: " + ssb.toString());
			setSpannableText(ssb);
		}
	}

	public void setSpannableText(final Spannable ssb) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				CustomMultiAutoCompleteTextView.this.setText(ssb);
				CustomMultiAutoCompleteTextView.this
						.setSelection(CustomMultiAutoCompleteTextView.this
								.getText().toString().length());
				resetFlags();
			}
		}, 20);
	}

	public void addTextView(String message) {
		int[] startEnd = getSelectionStartAndEnd();
		int start = startEnd[0];
		int end = startEnd[1];

		BitmapDrawable bitmapDrawable = getBitmapFromText(message);
		SpannableStringBuilder spannableStringBuilder = addSpanText(message,
				bitmapDrawable);

		this.getText().replace(Math.min(start, end), Math.max(start, end),
				spannableStringBuilder, 0, spannableStringBuilder.length());
	}

	/**
	 * @param ss
	 * @param bd
	 * @return
	 * 
	 *         this method is used whenever we need to add string at last or
	 *         simply during append
	 */
	public SpannableStringBuilder addSpanText(String ss, BitmapDrawable bd) {

		bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		final SpannableStringBuilder builder = new SpannableStringBuilder();
		builder.append(ss);
		builder.setSpan(new ImageSpan(bd), builder.length() - ss.length(),
				builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		this.setMovementMethod(LinkMovementMethod.getInstance());
		ClickableSpan clickSpan = new ClickableSpan() {

			@Override
			public void onClick(View view) {
				deleteString();
			}

		};
		builder.setSpan(clickSpan, builder.length() - ss.length(),
				builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return builder;
	}

	private void deleteString() {
		Log.d("deleteString", "deleting String");
		int[] startEnd = getSelectionStartAndEnd();
		int i = startEnd[0];
		int j = startEnd[1];
		isTextDeletedFromTouch = true;
		isTextAdditionInProgress = true;

		final SpannableStringBuilder sb = new SpannableStringBuilder(
				this.getText());

		String deletedSubString = sb
				.subSequence(Math.min(i, j), Math.max(i, j)).toString();
		deletedSubString = deletedSubString.split(" <")[0];
		deleteFromHashMap(deletedSubString);

		boolean hasCommaAtLast = true;
		try {
			sb.subSequence(Math.min(i, j + 1), Math.max(i, j + 1)).toString();
		} catch (Exception e) {
			hasCommaAtLast = false;
		}

		sb.replace(Math.min(i, hasCommaAtLast ? j + 1 : j),
				Math.max(i, hasCommaAtLast ? j + 1 : j), "");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				CustomMultiAutoCompleteTextView.this.setText(sb);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						isTextAdditionInProgress = false;
						stringLength = CustomMultiAutoCompleteTextView.this
								.getText().toString().length();
						isTextDeletedFromTouch = false;
						// Log.i("I am replacing text","I am replacing text 4");
						CustomMultiAutoCompleteTextView.this
								.setMovementMethod(LinkMovementMethod
										.getInstance());
						setSelection(getText().length());
					}
				}, 50);

			}
		}, 10);
	}

	private void deleteString(int start, int end) {
		isTextDeletedFromTouch = true;
		isTextAdditionInProgress = true;

		final SpannableStringBuilder sb = new SpannableStringBuilder(
				this.getText());
		Log.d("deleteString", "sb: " + sb.toString());
		sb.replace(start, end, "");
		Log.d("deleteString", "sb after replace: " + sb.toString());
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				CustomMultiAutoCompleteTextView.this.setText(sb);
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						isTextAdditionInProgress = false;
						stringLength = CustomMultiAutoCompleteTextView.this
								.getText().toString().length();
						isTextDeletedFromTouch = false;
						// Log.i("I am replacing text","I am replacing text 4");
						CustomMultiAutoCompleteTextView.this
								.setMovementMethod(LinkMovementMethod
										.getInstance());
						setSelection(getText().length());
					}
				}, 50);

			}
		}, 10);
	}

	/**
	 * @param message
	 * @return BitmapDrawable method which takes string as input and created a
	 *         bitmapDrawable from that string using layoutInflater
	 */
	private BitmapDrawable getBitmapFromText(String message) {
		TextView textView = (TextView) layoutInflater.inflate(
				R.layout.textview, null);
		textView.setText(message);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) SmsUtil
				.extractBitmapFromTextView(textView);
		return bitmapDrawable;
	}

	/**
	 * method which simply resets the boolean values so that this TextView is
	 * now able to start fresh for the purpose of textaddition and so on
	 */
	public void resetFlags() {
		isContactAddedFromDb = false;
		isTextAdditionInProgress = false;
		checkValidation = true;
	}

	/**
	 * @param subString
	 *            method which deletes the stored value from HashMap and invoked
	 *            this textview adapter to update its list i.e. when item is
	 *            deleted(from contact list),the same must appear on this
	 *            textview list
	 */
	private void deleteFromHashMap(String subString) {
		@SuppressWarnings("unchecked")
		HashMap<String, String> selectedContactClone = (HashMap<String, String>) SmsUtil.selectedContact
				.clone();
		Log.d("deleteFromHashMap substring", subString);
		for (Map.Entry<String, String> mapEntry : selectedContactClone
				.entrySet()) {
			if (subString.equals(mapEntry.getValue())) {
				SmsUtil.selectedContact.remove(mapEntry.getKey());
			}
		}
		updateQuickContactList();
		selectedContactClone = null;

	}

	/**
	 * This method simply fetches new contact list from database as soon as
	 * there is update(add/delete)on SmsUtil.selectedContact
	 */
	public void updateQuickContactList() {
		ArrayList<Contact> contactList = SmsUtil.getContacts(context, false);
		((ContactPickerAdapter) this.getAdapter()).setContactList(contactList);
	}

	/**
	 * @return int[] method which simply gets the getSelectionStart and
	 *         getSelectionEnd of this TextView
	 */
	private int[] getSelectionStartAndEnd() {
		int[] startEnd = new int[2];
		startEnd[0] = this.getSelectionStart() < 0 ? 0 : this
				.getSelectionStart();
		startEnd[1] = this.getSelectionEnd() < 0 ? 0 : this.getSelectionEnd();
		return startEnd;
	}

	/**
	 * @author santu This class is made simply because the CommaTokenizer always
	 *         adds space at the end ,and for our purpose we do not need space
	 *         at end at all
	 */
	/*
	 * public class CustomCommaTokenizer extends CommaTokenizer{
	 * 
	 * @Override public CharSequence terminateToken(CharSequence text) {
	 * CharSequence charSequence = super.terminateToken(text); return
	 * charSequence.subSequence(0, charSequence.length()-1); } }
	 */

	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		addOrCheckSpannable(null, 0);
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		Editable currText = getText();
		Log.d("onSelectionChanged", "selStart: " + selStart + " selEnd: "
				+ selEnd);
		if(currText.length() > 0 && selStart >1)
			Log.d("onSelectionChanged", "charAt(selStart-1): " + currText.charAt(selStart-1));
		
		super.onSelectionChanged(selStart, selEnd);
		if (currText.length() > 0 && selStart > 0 && currText.length() > selStart && currText.charAt(selStart) == ',')
		{
			Log.i("onSelectionChanged", "move selection forward");
			setSelection(selStart+1);
		}
	}
}
