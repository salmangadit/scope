package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.OpenCVLoader;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
//		Bitmap sourceImage = null;
		
//		BitmapHandler bitmaphandler = new BitmapHandler(this.getApplicationContext());
//		myimage = bitmaphandler.decodeFileAsPath(filepath);
		
//		try {
//			sourceImage = MediaStore.Images.Media.getBitmap(
//					getContentResolver(), image_uri);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			Log.v(TAG, "NULL");
//			e.printStackTrace();
//		}
//
//		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());
//		
//		
//		File file = new File(
//				Environment
//						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//				"temp.bmp");
//
//		try {
//			FileOutputStream out = new FileOutputStream(file);
//			sourceImage.compress(Bitmap.CompressFormat.PNG, 90, out);
//		} catch (Exception e) {
//			Log.v(TAG, "null2");
//			e.printStackTrace();
//		}

		final Uri uri, process_uri_1,process_uri_2;
		uri = image_uri;
		//uri = Uri.fromFile(file);
	
//		Morphing morphing = new Morphing(this.getApplicationContext(),uri);
//		process_uri_1 = morphing.erode(-20);
		
		Smoothing smoother = new Smoothing(this.getApplicationContext(),uri);
		process_uri_1 = smoother.BilateralFilter();
	
		Threshold thresh = new Threshold(this.getApplicationContext(),process_uri_1 );
		double value = thresh.otsu();
		Log.v(TAG,"otsu  "+value);
		process_uri_2 = thresh.thresh_binary(value,255);
		
		Log.v(TAG, process_uri_2.toString());

		ImageView imageView = (ImageView) findViewById(R.id.imgView);
		imageView.setImageURI(process_uri_2);
		Button button_done = (Button) findViewById(R.id.button1);

		final Context a = this;

		button_done.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.v(TAG, "Done button clicked");
				Intent i = new Intent(a, Ocrmain.class);
				i.putExtra("file_path", filepath);
				i.putExtra("image_uri", process_uri_2.toString());
				startActivity(i);

			}
		});

	}
}
