package com.example.scope;

import java.util.List;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class PreProcessAsync extends AsyncTask<Void, String, String> {
	PreProcess preprocess;
	ProgressDialog progress;
	final Uri uri;
	Uri process_uri_1, process_uri_2;
	private static final String TAG = "Scope.java";

	public PreProcessAsync(Uri uri, PreProcess main) {
		this.uri = uri;
		this.preprocess = main;
	}

	protected void onPreExecute() {
		progress = ProgressDialog.show(preprocess, "Pre-Processing",
				"Applying Pre Processing filters to image", true);
	};

	@Override
	protected String doInBackground(Void... params) {
		progress.setMessage("Greyscaling image");

		// Check if card is NUS card
//		MatchTemplate matcher = new MatchTemplate(uri,
//				preprocess.getApplicationContext());
		boolean isNUS = false;

		if (isNUS){
			Log.v(TAG, "This is an NUS card");
		}
		else{
			Log.v(TAG, "This is NOT an NUS card");
		}
		Greyscale grey = new Greyscale(preprocess.getApplicationContext(), uri);
		Uri ppimage = grey.greyscale();

		// progress.setMessage("Applying bilateral filter");
		publishProgress("Applying bilateral filter");
		Smoothing smoother = new Smoothing(preprocess.getApplicationContext(),
				ppimage);
		Uri ppimage1 = smoother.BilateralFilter();

		// progress.setMessage("Applying adaptive thresholding");
		publishProgress("Applying adaptive thresholding");
		Adpt initadpt = new Adpt(preprocess.getApplicationContext(), ppimage1,
				"adpt1.bmp");
		Uri ppimage2 = initadpt.thresh();
		
//        Morphing morphing1 = new Morphing(preprocess.getApplicationContext(),ppimage2);
//      Uri ppimage3 = morphing1.erode(20);
		

		

		// progress.setMessage("Applying line segmentation");
		publishProgress("Applying line segmentation");
		SegmentLine segmenter = new SegmentLine(
				preprocess.getApplicationContext(), ppimage2, ppimage1);
		List<Uri> segmentedResults = segmenter.segLine();

		Analyse analyser = new Analyse(preprocess.getApplicationContext(),
				segmentedResults);
		segmentedResults = analyser.adaptiveSplitter();

		publishProgress("Cleaning segments");
		// Cleaner function
//		for (int i = 0; i < segmentedResults.size(); i++) {
//			Threshold thresh = new Threshold(
//					preprocess.getApplicationContext(),
//					segmentedResults.get(i), "clean" + i + ".bmp");
//			segmentedResults.set(i, thresh.thresh_binary(10, 255));
//		}

		
		// ONLY IF NUS CARD FOR NOW
		if (isNUS) {
			publishProgress("Applying cleaning for NUS card");
			for (int i = 0; i < segmentedResults.size(); i++) {
				Analyse fill = new Analyse(preprocess.getApplicationContext(),
						segmentedResults.get(i), "temple" + i + ".bmp");
				segmentedResults.set(i, fill.filler());
			}
		}
		
		Globals appState = ((Globals) preprocess.getApplicationContext());
		appState.setAdaptiveResult(segmentedResults);

		// progress.setMessage("Pre-processing complete.");
		publishProgress("Pre-processing complete.");

		Log.v(TAG, "finished background");
		preprocess.result_uri = uri;
		return null;
	}

	@Override
	protected void onProgressUpdate(String... msg) {
		progress.setMessage(msg[0]);
	}

	@Override
	protected void onPostExecute(String string) {
		Log.v(TAG, "started post background");
		progress.dismiss();
		preprocess.nextActivity();
	}

}
