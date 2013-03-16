package com.example.scope;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class Contacts extends Activity {
	EditText etName;
	EditText etMobile;
	EditText etFax;
	EditText etEmail;
	EditText etAddress;
	EditText etWebsite;

	List<SegmentationResult> ocrResults;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		etName = (EditText) findViewById(R.id.et_name);
		etMobile = (EditText) findViewById(R.id.et_mobile_phone);
		etFax = (EditText) findViewById(R.id.et_home_phone);
		etEmail = (EditText) findViewById(R.id.et_home_email);
		etAddress = (EditText) findViewById(R.id.et_work_email);
		etWebsite = (EditText) findViewById(R.id.et_website);
		
		Globals appState = ((Globals) getApplicationContext());
		ocrResults = appState.getSegmentationResult();
		
		ArrayList<String> extractedStrings = this.extractStrings(ocrResults);
		
		StringParser parser = new StringParser();
		ParsedResults results = new ParsedResults();
		results = parser.CardParse(extractedStrings);
		
		//Update fields
		etName.setText(results.name);
		etMobile.setText(results.numbers);
		etEmail.setText(results.emails);
		etFax.setText(results.fax);
		etAddress.setText(results.address);
		etWebsite.setText(results.website);
		
		// Creating a button click listener for the "Add Contact" button
		OnClickListener addClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {

				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

				int rawContactID = ops.size();

				// Adding insert operation to operations list
				// to insert a new raw contact in the table
				// ContactsContract.RawContacts
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.RawContacts.CONTENT_URI)
						.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
								null).withValue(RawContacts.ACCOUNT_NAME, null)
						.build());

				// Adding insert operation to operations list
				// to insert display name in the table ContactsContract.Data
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID,
								rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE,
								StructuredName.CONTENT_ITEM_TYPE)
						.withValue(StructuredName.DISPLAY_NAME,
								etName.getText().toString()).build());

				// Adding insert operation to operations list
				// to insert Mobile Number in the table ContactsContract.Data
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID,
								rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE,
								Phone.CONTENT_ITEM_TYPE)
						.withValue(Phone.NUMBER, etMobile.getText().toString())
						.withValue(Phone.TYPE,
								CommonDataKinds.Phone.TYPE_MOBILE).build());

				// Adding insert operation to operations list
				// to insert Home Phone Number in the table
				// ContactsContract.Data
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID,
								rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE,
								Phone.CONTENT_ITEM_TYPE)
						.withValue(Phone.NUMBER,
								etFax.getText().toString())
						.withValue(Phone.TYPE, Phone.TYPE_FAX_HOME).build());

				// Adding insert operation to operations list
				// to insert Home Email in the table ContactsContract.Data
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID,
								rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE,
								Email.CONTENT_ITEM_TYPE)
						.withValue(Email.ADDRESS,
								etEmail.getText().toString())
						.withValue(Email.TYPE, Email.TYPE_HOME).build());

				// Adding insert operation to operations list
				// to insert Work Email in the table ContactsContract.Data
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID,
								rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE,
								CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
								etAddress.getText().toString())
						.withValue(CommonDataKinds.StructuredPostal.TYPE, CommonDataKinds.StructuredPostal.TYPE_WORK).build());
				
				//Website
				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID,
								rawContactID)
						.withValue(ContactsContract.Data.MIMETYPE,
								CommonDataKinds.Website.CONTENT_ITEM_TYPE)
						.withValue(CommonDataKinds.Website.URL,
								etWebsite.getText().toString())
						.withValue(CommonDataKinds.Website.TYPE, CommonDataKinds.Website.TYPE_WORK).build());

				try {
					// Executing all the insert operations as a single database
					// transaction
					getContentResolver().applyBatch(ContactsContract.AUTHORITY,
							ops);
					Toast.makeText(getBaseContext(),
							"Contact is successfully added", Toast.LENGTH_SHORT)
							.show();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					e.printStackTrace();
				}
			}
		};

		// Creating a button click listener for the "Add Contact" button
		OnClickListener contactsClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Creating an intent to open Android's Contacts List
				Intent contacts = new Intent(Intent.ACTION_VIEW,
						ContactsContract.Contacts.CONTENT_URI);

				// Starting the activity
				startActivity(contacts);
			}
		};

		// Getting reference to "Add Contact" button
		Button btnAdd = (Button) findViewById(R.id.btn_add);

		// Getting reference to "Contacts List" button
		Button btnContacts = (Button) findViewById(R.id.btn_contacts);

		// Setting click listener for the "Add Contact" button
		btnAdd.setOnClickListener(addClickListener);

		// Setting click listener for the "List Contacts" button
		btnContacts.setOnClickListener(contactsClickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_contacts, menu);
		return true;
	}

	private ArrayList<String> extractStrings(List<SegmentationResult> ocr) {
		ArrayList<String> extracted = new ArrayList<String>();

		for (int i = 0; i < ocr.size(); i++) {
			if (ocr.get(i).Result.trim() != "")
				extracted.add(ocr.get(i).Result);
		}
		
		return extracted;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
