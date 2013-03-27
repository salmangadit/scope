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

/* Class: Greyscale
 * Author: Aravindh
 * 
 * Description:
 * A general image processing operator is a function that takes one or more input images 
 * and produces an output image.
 * In this image processing transform, an RGB input bitmap image is converted 
 * into a grey-scaled output bitmap image of the same size. 
 * 
 * Methods return a uri to the grey-scaled image
 * 
 * Sample usage code:
 * Greyscale grey = new Greyscale(this.getApplicationContext(), Uri.parse(path));
 * Uri ppimage=grey.greyscale();
 */

@TargetApi(12)
public class Greyscale {
	double alpha;
	Uri inputImageUri;
	Context currContext;
	
	private static final String TAG = "Scope.java";
	
	// Constructor
	public Greyscale(Context c, Uri inputUri)
	{
		currContext = c;
		inputImageUri = inputUri;
	}
	
	// Method to set image only, if class has already been instantiated
	public void SetImage(Uri inputUri)
	{
		inputImageUri = inputUri;
	}
	
	//Function to greyscale the image
	public Uri greyscale() {
		
		Mat sourceImageMat = new Mat();
		
		Bitmap sourceImage = null;
		Bitmap destImage = null;

		try {
			sourceImage = MediaStore.Images.Media.getBitmap(
					currContext.getContentResolver(), inputImageUri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "NULL");
			e.printStackTrace();
		}

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());
		
		destImage=sourceImage;
		
		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat.zeros(sourceImageMat.size(), sourceImageMat.type());
		Imgproc.cvtColor(sourceImageMat, final_dest_mat, Imgproc.COLOR_RGB2GRAY);
		Utils.matToBitmap(final_dest_mat, destImage);
		
		Log.v(TAG, "destImage Size: " + destImage.getByteCount());
		
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"temp.bmp");

		try {
			FileOutputStream out = new FileOutputStream(file);
			destImage.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			Log.v(TAG, "null2");
			e.printStackTrace();
		}
		
		final Uri uri = Uri.fromFile(file);
		Log.v(TAG, uri.toString());
		
		sourceImage.recycle();
		destImage.recycle();
		
		return uri;
	}
}
