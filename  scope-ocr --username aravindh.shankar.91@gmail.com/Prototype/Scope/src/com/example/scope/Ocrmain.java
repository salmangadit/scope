package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

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
		Log.v(TAG, "entering tess");

		// Getting uri of image/cropped image
		try {
			myimage = MediaStore.Images.Media.getBitmap(
					this.getContentResolver(), image_uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v(TAG, "bitmap " + myimage.getByteCount());
		
		//TessBase starts
		TessBaseAPI baseApi = new TessBaseAPI();

		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		Log.v(TAG, "Before baseApi");
		baseApi.setImage(myimage);
		Log.v(TAG, "Before baseApi2");
		String recognizedText = baseApi.getUTF8Text();
		Log.v(TAG, "Before baseApi3");
		baseApi.end();


		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		if (lang.equalsIgnoreCase("eng")) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}

		//recognizedText is the final OCRed text
		recognizedText = recognizedText.trim();

		String ocrtext = "And...BAM! OCRed: " + recognizedText;
		Toast toast = Toast.makeText(this.getApplicationContext(), ocrtext,
				Toast.LENGTH_LONG);
		toast.show();
		
		//deleting temporary crop file created
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"temp.bmp");
		boolean deleted = file.delete();
		Log.i(TAG, "File deleted: "+deleted);
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}