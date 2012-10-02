package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

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

		// Intent input
		filepath = getIntent().getStringExtra("file_path");
		String b = getIntent().getStringExtra("image_uri");
		image_uri = Uri.parse(b);
		Log.v(TAG, image_uri.toString());

		pre_process();
	}

	public void pre_process() {

		Mat src = new Mat();
		Mat dst = new Mat();

		try {
			myimage = MediaStore.Images.Media.getBitmap(
					this.getContentResolver(), image_uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "NULL");
			e.printStackTrace();
		}

		ppimage = myimage;
		Log.v(TAG, "not screwed");

		Utils.bitmapToMat(myimage, src);
		Log.v(TAG, "not screwed1");
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGB2GRAY, 0);
		Mat dst1 = Mat.zeros(dst.size(), dst.type());
		dst.convertTo(dst1, -1, alpha, beta);
		Imgproc.equalizeHist(dst1, dst1);
		Log.v(TAG, "not screwed2");
		Utils.matToBitmap(dst1, ppimage);

		Log.v(TAG, "Myimage Size:" + myimage.getByteCount());
		Log.v(TAG, "PPimage Size:" + ppimage.getByteCount());

		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"temp.bmp");

		try {
			FileOutputStream out = new FileOutputStream(file);
			ppimage.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			Log.v(TAG, "null2");
			e.printStackTrace();
		}

		Uri uri = Uri.fromFile(file);
		Log.v(TAG, uri.toString());

		Intent i = new Intent(this, Ocrmain.class);
		i.putExtra("file_path", filepath);
		i.putExtra("image_uri", uri.toString());
		startActivity(i);

	}

}