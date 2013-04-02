package com.example.scope;

import java.util.ArrayList;
import java.util.Date;
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
	List <Uri> segmentedResults_final =  new ArrayList<Uri>();

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
		//progress.setMessage("Greyscaling image");
		Date now = new Date();
		Date first = (Date)now.clone();
		
		Greyscale grey = new Greyscale(preprocess.getApplicationContext(), uri);
		Uri ppimage1 = grey.greyscale();
		
		Date later = new Date();
		long diff = later.getTime() - now.getTime();
		Log.v(TAG, "Greyscale time: " + diff);
		now = new Date();
		// progress.setMessage("Applying bilateral filter");
		publishProgress("Applying bilateral filter");
		
     
		
//		Smoothing smoother = new Smoothing(preprocess.getApplicationContext(),
//				ppimage);
//		Uri ppimage1 = smoother.BilateralFilter();
//		later = new Date();
//		diff = later.getTime() - now.getTime();
//		Log.v(TAG, "Bilateral time: " + diff);
//		now = new Date();
		
		// progress.setMessage("Applying adaptive thresholding");
		publishProgress("Applying adaptive thresholding");
		Adpt initadpt = new Adpt(preprocess.getApplicationContext(), ppimage1,
				"adpt1.bmp");
		Uri ppimage2 = initadpt.thresh();
		
//		Analyse dig = new Analyse(preprocess.getApplicationContext(),
//		ppimage2, "dig.bmp");
//		dig.filler();
		
		later = new Date();
		diff = later.getTime() - now.getTime();
		Log.v(TAG, "Adapt thresh time: " + diff);
		now = new Date();
		
		publishProgress("Morphing thresholding");
		Morphing morphing1 = new Morphing(preprocess.getApplicationContext(),ppimage2);
		Uri ppimage3 = morphing1.erode_iterate(30, 10);	
      
      	later = new Date();
		diff = later.getTime() - now.getTime();
		Log.v(TAG, "Morphing time: " + diff);
		now = new Date();
		
		// progress.setMessage("Applying line segmentation");
		publishProgress("Applying line segmentation");
		SegmentLine segmenter = new SegmentLine(
				preprocess.getApplicationContext(), ppimage3, ppimage1);
		List<Uri> segmentedResults = segmenter.segLine();
		
		later = new Date();
		diff = later.getTime() - now.getTime();
		Log.v(TAG, "Line segmentation time: " + diff);
		now = new Date();
	
		publishProgress("Analysing segments");
		Analyse analyser = new Analyse(preprocess.getApplicationContext(),
				segmentedResults);
		List <Uri> segmentedResults_analysed = analyser.adaptiveSplitter();
		
		later = new Date();
		diff = later.getTime() - now.getTime();
		Log.v(TAG, "Adaptive splitter time: " + diff);
		now = new Date();
		
		publishProgress("Cleaning segments");
		
		for (int i = 0; i < segmentedResults_analysed.size(); i++) {
		Analyse fill = new Analyse(preprocess.getApplicationContext(),
				segmentedResults_analysed.get(i), "filled" + i + ".bmp");
		segmentedResults_final.add(fill.filler());
		}
		
		later = new Date();
		diff = later.getTime() - now.getTime();
		Log.v(TAG, "Cleaning image time: " + diff);
		//now = new Date();
		
		// Check if card is NUS card
//		MatchTemplate matcher = new MatchTemplate(uri, preprocess.getApplicationContext());
//		boolean isNUS = matcher.TemplateMatch();
		boolean isNUS = true;

		if (isNUS) {
			Log.v(TAG, "This is an NUS card");
		} else {
			Log.v(TAG, "This is NOT an NUS card");
		}

		Globals appState = ((Globals) preprocess.getApplicationContext());
		appState.setAdaptiveResult(segmentedResults_final);

		// progress.setMessage("Pre-processing complete.");
		publishProgress("Pre-processing complete.");
		
		Date finished = new Date();
		diff = finished.getTime() - first.getTime();
		Log.v(TAG, "Pre process time: " + diff);
		
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
