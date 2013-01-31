package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class OcrmainAsync extends AsyncTask<Void, Void, String> {
	Ocrmain ocrmain;
	ProgressDialog progressDialog;
	public Bitmap myimage;
	public Uri image_uri;
	String DATA_PATH;
	SegmentationResult result;

	static final String lang = "eng";
	static final String TAG = "Scope.java";

	public OcrmainAsync(Bitmap myimage, Ocrmain main, Uri image_uri,
			String data, SegmentationResult result) {
		this.myimage = myimage;
		this.ocrmain = main;
		this.image_uri = image_uri;
		this.DATA_PATH = data;
		this.result = result;
	}

	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(ocrmain, "Applying Tesseract OCR",
				"Converting image to text", true);
	};

	@Override
	protected String doInBackground(Void... params) {
		Log.v(TAG, "entering tess");

		// Getting uri of image/cropped image
		try {
			myimage = MediaStore.Images.Media.getBitmap(
					ocrmain.getContentResolver(), image_uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputStream input = null;
		try {
			input = ocrmain.getContentResolver().openInputStream(image_uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Log.v(TAG, "bitmap before comp: " + myimage.getByteCount());
		BitmapFactory.Options opt = new BitmapFactory.Options();
		// opt.inSampleSize = 2;
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		myimage = BitmapFactory.decodeStream(input, null, opt);
		Log.v(TAG, "bitmap after comp:" + myimage.getByteCount());

		// TessBase starts
		TessBaseAPI baseApi = new TessBaseAPI();

		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang, TessBaseAPI.OEM_CUBE_ONLY);
		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
		Log.v(TAG, "Before baseApi");
		baseApi.setImage(myimage);
		Log.v(TAG, "Before baseApi2");
		String recognizedText = baseApi.getUTF8Text();
		Log.v(TAG, "Before baseApi3");

		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		if (lang.equalsIgnoreCase("eng")) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}

		// recognizedText is the final OCRed text
		recognizedText = recognizedText.trim();

		// deleting temporary crop file created
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"temp.bmp");
		boolean deleted = file.delete();

		result.Result = recognizedText;
		result.Confidence = baseApi.meanConfidence();
		 // Iterate through the results.
//        final ResultIterator iterator = baseApi.getResultIterator();
//        String lastUTF8Text;
//        float lastConfidence;
//        int count = 0;
//        iterator.begin();
//        do {
//            lastUTF8Text = iterator.getUTF8Text(PageIteratorLevel.RIL_WORD);
//            lastConfidence = iterator.confidence(PageIteratorLevel.RIL_WORD);
//            count++;
//        } while (iterator.next(PageIteratorLevel.RIL_WORD));
//
//        System.out.println("Found " + count + " result.");
//        System.out.println("lastUTF8Text '" + lastUTF8Text + "' should match outputText '" + recognizedText + "'.");
//        System.out.println("Confidence: " + lastConfidence);
        
        //result.Confidence = (int)lastConfidence;
        
		Log.v(TAG, "Confidence:"+ Arrays.toString(baseApi.wordConfidences()));
		ocrmain.ocrResults.add(result);

		// ocrmain.recognizedText = recognizedText;
		baseApi.end();
		Log.i(TAG, "File deleted: " + deleted);
		return null;
	}

	@Override
	protected void onPostExecute(String string) {
		Log.v(TAG, "started post background");
		ocrmain.checkIntentDone();
		progressDialog.dismiss();

	}

}
