package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

	public String filepath;
	public Bitmap my_image;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "enter...");
		Log.v(TAG, "OCR...");

		// Intent input
		filepath = getIntent().getStringExtra("file_path");
		String b = getIntent().getStringExtra("image_uri");
		image_uri = Uri.parse(b);
		Log.v(TAG, image_uri.toString());
	    
		ocr_main();
	}

	public void ocr_main() {
		
		new OcrmainAsync(myimage, this, image_uri, DATA_PATH).execute();

//		Intent intent = new Intent(this, Contacts.class);
//		startActivity(intent);
	}
	
	public void doIntent(){
		Intent intent = new Intent(this, ResultActivity.class);
		intent.putExtra("ocrText", recognizedText);
		startActivity(intent);
	}
}