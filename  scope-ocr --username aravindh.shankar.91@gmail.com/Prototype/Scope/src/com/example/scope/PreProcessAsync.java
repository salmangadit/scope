package com.example.scope;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class PreProcessAsync extends AsyncTask<Void, Void, String> {
	PreProcess preprocess;
	ProgressDialog progressDialog;
	final Uri uri;
	Uri process_uri_1, process_uri_2;
	private static final String TAG = "Scope.java";

	public PreProcessAsync(Uri uri, PreProcess main) {
		this.uri = uri;
		this.preprocess = main;
	}

	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(preprocess, "Pre-Processing",
				"Applying Pre Processing filters to image", true);
	};

	@Override
	protected String doInBackground(Void... params) {
		Smoothing smoother = new Smoothing(preprocess.getApplicationContext(),
				uri);
		process_uri_1 = smoother.BilateralFilter();

		Threshold thresh = new Threshold(preprocess.getApplicationContext(),
				process_uri_1);
		double value = thresh.otsu();
		Log.v(TAG, "otsu  " + value);
		process_uri_2 = thresh.thresh_binary(value, 255);

		Log.v(TAG, process_uri_2.toString());
		preprocess.result_uri = process_uri_2;

		
		Log.v(TAG,"finished background");
		return null;
	}
	
	@Override
	protected void onPostExecute(String string) {
		Log.v(TAG,"started post background");
		progressDialog.dismiss();
		preprocess.runOnUiThread(new Runnable() {
			public void run() {
				// stuff that updates ui
				preprocess.setImageURI(process_uri_2);
			}
		});
	}

}
