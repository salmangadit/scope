package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class Ocrmain extends Activity {

	private static final String TAG = "Scope.java";
	public static final String lang = "eng";
	public Bitmap myimage;
	protected static final String PHOTO_TAKEN = "photo_taken";
	final int PIC_CROP = 2;
	public Uri image_uri;
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/Scope/";
	public String recognizedText = null;

	public List<SegmentationResult> ocrResults = null;

	public String filepath;
	public Bitmap my_image;

	int checkResultCount;
	List<Uri> allText;
	List<String> allCoords;

	List<Uri> adaptiveResults;
	
	Date before, after;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "enter...");
		Log.v(TAG, "OCR...");
		
		ocrResults = new ArrayList<SegmentationResult>();

		Globals appState = ((Globals) getApplicationContext());
		adaptiveResults = appState.getAdaptiveResult();

		// Intent input
		filepath = getIntent().getStringExtra("file_path");
		// String b = getIntent().getStringExtra("image_uri");
		// image_uri = Uri.parse(b);
		// Log.v(TAG, image_uri.toString());
		checkResultCount = 0;
		ocr_main();
	}

	public void ocr_main() {

		// Applying segmentation here temporarily until a proper way to transfer
		// lists across intents can be found

		// Segmenter segmenter = new Segmenter(this.getApplicationContext(),
		// image_uri);
		// List<Uri> ppimage = segmenter.SegmentBackground();
		allText = new ArrayList<Uri>();
		allCoords = new ArrayList<String>();

		for (int i = 0; i < adaptiveResults.size(); i++) {
			Segmenter textSegmenter = new Segmenter(
					this.getApplicationContext(), adaptiveResults.get(i));
			List<Uri> textSegs = textSegmenter.SegmentText(i);
			List<String> coordinates = textSegmenter.getCoordinates();

			for (int j = 0; j < textSegs.size(); j++) {
				allText.add(textSegs.get(j));
				allCoords.add(coordinates.get(j));
			}
		}

		Log.v(TAG, "Total segments: " + allText.size());
		before = new Date();
		
		int limit = 50;
		BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(limit);
		ThreadPoolExecutor executor = 
                new ThreadPoolExecutor(limit, limit, 60, TimeUnit.SECONDS, q);

		for (int i = 0; i < allText.size(); i++) {
			SegmentationResult result = new SegmentationResult();
			result.X = getCoordsX(allCoords.get(i));
			result.Y = getCoordsY(allCoords.get(i));
			result.image = allText.get(i);
			TessBaseAPI baseApi = new TessBaseAPI();

			baseApi.setDebug(true);
			baseApi.init(DATA_PATH, lang, TessBaseAPI.OEM_CUBE_ONLY);
			baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
			Log.v(TAG, "Before baseApi");
			
			
			new OcrmainAsync(this, allText.get(i), DATA_PATH, result, baseApi)
					.executeOnExecutor(executor);
		}
		// Intent intent = new Intent(this, Contacts.class);
		// startActivity(intent);
	}

	private int getCoordsX(String coordinates) {
		String[] coords = coordinates.split(":");
		return Integer.parseInt(coords[0]);
	}

	private int getCoordsY(String coordinates) {
		String[] coords = coordinates.split(":");
		return Integer.parseInt(coords[1]);
	}

	public void checkIntentDone() {
		checkResultCount++;
		if (checkResultCount == allText.size()) {
			doIntent();
		}
	}

	public void doIntent(){
		after = new Date();
		long diff = after.getTime() - before.getTime();
		Log.v(TAG, "Time taken: " + Long.toString(diff));
		Intent intent = new Intent(this, ResultActivity.class);
		Globals appState = ((Globals) getApplicationContext());
		appState.setSegmentationResult(ocrResults);
		// intent.putExtra("ocrText", recognizedText);
		startActivity(intent);
	}
}