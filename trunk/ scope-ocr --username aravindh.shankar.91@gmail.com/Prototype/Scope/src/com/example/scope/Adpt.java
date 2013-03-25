package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

@TargetApi(12)
public class Adpt {
	double alpha;
	Uri inputImageUri;
	Context currContext;
	Mat sourceImageMat = new Mat();
	Mat destImageMat = new Mat();
	Bitmap sourceImage = null;
	Bitmap destImage = null;
	String file_name = "temp.bmp";
	Uri uri;

	private static final String TAG = "Scope.java";

	// Constructor
	public Adpt(Context c, Uri inputUri) {
		currContext = c;
		inputImageUri = inputUri;
	}
	
	public Adpt(Context c, Uri inputUri, String store) {
		currContext = c;
		inputImageUri = inputUri;
		file_name = store;
	}

	// Method to set image only, if class has already been instantiated
	public void SetImage(Uri inputUri) {
		inputImageUri = inputUri;
	}

	public void initiate() {
		try {
			sourceImage = MediaStore.Images.Media.getBitmap(
					currContext.getContentResolver(), inputImageUri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "NULL");
			e.printStackTrace();
		}
	}

	//Applying Gaussian mean adaptive threshold, as thresh_binary, for dark text on light bg
	public Uri thresh() {
		initiate();

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());

		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat
				.zeros(destImageMat.size(), destImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
		
		Log.v(TAG, "Size = 1");
		Imgproc.adaptiveThreshold(destImageMat, final_dest_mat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 9, 9);
		
		Utils.matToBitmap(final_dest_mat, destImage);

		Log.v(TAG, "destImage Size: " + destImage.getByteCount());

		store();
		Log.v(TAG, uri.toString());
		return uri;
	}
	
	public Uri thresh(int value) {
		initiate();
		int order = 1;;
		if(value!=0)
		order = Imgproc.ADAPTIVE_THRESH_MEAN_C;
		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());

		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat
				.zeros(destImageMat.size(), destImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
		
		Log.v(TAG, "Size = 1");
		Imgproc.adaptiveThreshold(destImageMat, final_dest_mat, 255, order, Imgproc.THRESH_BINARY, 5, 5);
		
		Utils.matToBitmap(final_dest_mat, destImage);

		Log.v(TAG, "destImage Size: " + destImage.getByteCount());

		store();
		Log.v(TAG, uri.toString());
		return uri;
	}
	
	// Temporary storage location
	public void store() {
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				file_name);

		try {
			FileOutputStream out = new FileOutputStream(file);
			destImage.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			Log.v(TAG, "null2");
			e.printStackTrace();
		}

		uri = Uri.fromFile(file);
	}
}
