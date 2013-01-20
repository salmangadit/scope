package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "enter...");
		Log.v(TAG, "OCR...");

		ocrResults = new ArrayList<SegmentationResult>();

		// Intent input
		filepath = getIntent().getStringExtra("file_path");
		String b = getIntent().getStringExtra("image_uri");
		image_uri = Uri.parse(b);
		Log.v(TAG, image_uri.toString());
		checkResultCount = 0;
		ocr_main();
	}

	public void ocr_main() {

		// Applying segmentation here temporarily until a proper way to transfer
		// lists across intents can be found

		Segmenter segmenter = new Segmenter(this.getApplicationContext(),
				image_uri);
		List<Uri> ppimage = segmenter.SegmentBackground();
		allText = new ArrayList<Uri>();
		allCoords = new ArrayList<String>();

		for (int i = 0; i < ppimage.size(); i++) {
			Segmenter textSegmenter = new Segmenter(
					this.getApplicationContext(), ppimage.get(i));
			List<Uri> textSegs = textSegmenter.SegmentText(i);
			List<String> coordinates = textSegmenter.getCoordinates();

			for (int j = 0; j < textSegs.size(); j++) {
				allText.add(textSegs.get(j));
				allCoords.add(coordinates.get(j));
			}

		}

		Log.v(TAG, "Total segments: " + allText.size());

		for (int i = 0; i < allText.size(); i++) {
			SegmentationResult result = new SegmentationResult();
			result.X = getCoordsX(allCoords.get(i));
			result.Y = getCoordsY(allCoords.get(i));
			new OcrmainAsync(myimage, this, allText.get(i), DATA_PATH, result)
					.execute();
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

	public void doIntent() {
		Intent intent = new Intent(this, ResultActivity.class);
		Globals appState = ((Globals) getApplicationContext());
		appState.setSegmentationResult(ocrResults);
		// intent.putExtra("ocrText", recognizedText);
		startActivity(intent);
	}
}