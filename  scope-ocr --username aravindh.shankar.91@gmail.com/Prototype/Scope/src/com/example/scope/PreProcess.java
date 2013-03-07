package com.example.scope;

import java.util.List;

import org.opencv.android.OpenCVLoader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PreProcess extends Activity {

	private static final String TAG = "Scope.java";
	static {
		if (!OpenCVLoader.initDebug()) {
			Log.e(TAG, "Some Error!");
		}
	}
	public double alpha = 3.0;
	public double beta = 0;
	public Bitmap myimage;
	public Bitmap ppimage;
	public Uri image_uri;
	public String filepath;
	public Uri result_uri = null;
	ProgressDialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG, "enter...");
		Log.v(TAG, "Preprocess...");

		setContentView(R.layout.cropping);
		ActionBar ab = getActionBar();
		ab.setTitle("Title");
		ab.setDisplayShowTitleEnabled(false);
		ab.setSubtitle("Subtitle");
		ab.setDisplayShowTitleEnabled(false);
		// Intent input
		filepath = getIntent().getStringExtra("file_path");
		String b = getIntent().getStringExtra("image_uri");
		image_uri = Uri.parse(b);
		Log.v(TAG, image_uri.toString());
		setContentView(R.layout.cropping);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		TextView progress = (TextView) findViewById(R.id.txtProgress);
		dialog = ProgressDialog.show(PreProcess.this, "Pre-processing", "Greyscaling image", true);
		///////////////Pre process starts
		progress.setText("Greyscaling image");
		Greyscale grey = new Greyscale(this.getApplicationContext(), image_uri);
		Uri ppimage=grey.greyscale();
		
		dialog.setMessage("Applying bilateral filter");
		progress.setText("Applying bilateral filter");
		Smoothing smoother = new Smoothing(this.getApplicationContext(),
				ppimage);
		Uri ppimage1 = smoother.BilateralFilter();
		dialog.setMessage("Applying adaptive thresholding");
		progress.setText("Applying adaptive thresholding");
		Adpt initadpt = new Adpt(this.getApplicationContext(),ppimage1,"adpt1.bmp");
		Uri ppimage2 = initadpt.thresh();
		dialog.setMessage("Applying line segmentation");
		progress.setText("Applying line segmentation");
		SegmentLine segmenter = new SegmentLine(this.getApplicationContext(),ppimage2, ppimage1);
		List<Uri> segmentedResults = segmenter.segLine();
		
		Analyse analyser=new Analyse(this.getApplicationContext(),segmentedResults);
		segmentedResults = analyser.adaptiveSplitter();	
		
		Globals appState = ((Globals) getApplicationContext());
		appState.setAdaptiveResult(segmentedResults);
		
		progress.setText("Pre-processing complete.");
		dialog.dismiss();
		////////////////Pre process ends : segmentedResults is a list of URIs of processed segments
		
		final Uri uri;
		uri = image_uri;
	
		//new PreProcessAsync(uri, this).execute();

//		Button button_done = (Button) findViewById(R.id.button1);

		final Context a = this;

//		button_done.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Log.v(TAG, "Done button clicked");
//				Intent i = new Intent(a, Ocrmain.class);
//				i.putExtra("file_path", filepath);
//				//i.putExtra("image_uri", result_uri.toString());
//				startActivity(i);
//
//			}
//		});
		
		Intent i = new Intent(a, Ocrmain.class);
		i.putExtra("file_path", filepath);
		//i.putExtra("image_uri", result_uri.toString());
		startActivity(i);
	}
	
	public void setImageURI(Uri uri){
		ImageView imageView = (ImageView) findViewById(R.id.imgView);
		imageView.setImageURI(uri);
	}
}
