package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/* Class: Brightness
 * Author: Salman
 * 
 * Description:
 * A general image processing operator is a function that takes one or more input images 
 * and produces an output image.
 * In this kind of image processing transform, each output pixel’s value depends on only 
 * the corresponding input pixel value (plus, potentially, some globally collected information 
 * or parameters).
 * 
 * One commonly used point process is addition with a constant:
 * 		- g(i,j) = f(i,j) + B
 * where B = bias parameter, also known as brightness control.
 * 		 g = output image
 * 		 f = source image
 * 
 * Sample usage code:
 * Brightness brightner = new Brightness(context, inputImageUri);
 * Uri brightenedImage = brightner.Brighten(30) //30% brightness
 */

public class Brightness {
	double beta;
	Uri inputImageUri;
	Context currContext;
	
	private static final String TAG = "Scope.java";
	
	// Constructor
	public Brightness(Context c, Uri inputUri)
	{
		currContext = c;
		inputImageUri = inputUri;
	}
	
	// Method to set image only, if class has already been instantiated
	public void SetImage(Uri inputUri)
	{
		inputImageUri = inputUri;
	}
	
	// Input beta value for brightness as a percentage
	public Uri Brighten(double betaPercentage) {
		
		if (betaPercentage > 100)
			betaPercentage = 100;
		else if (betaPercentage < 0)
			betaPercentage = 0;
		
		beta = betaPercentage;
		
		Mat sourceImageMat = new Mat();
		Mat destImageMat_temp = new Mat();
		Mat destImageMat = new Mat();
		
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

		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Imgproc.cvtColor(sourceImageMat, destImageMat_temp, Imgproc.COLOR_RGB2BGRA, 0);
		Imgproc.cvtColor(destImageMat_temp, destImageMat, Imgproc.COLOR_BGRA2RGBA, 0);
		Mat final_dest_mat = Mat.zeros(destImageMat.size(), destImageMat.type());
		destImageMat.convertTo(final_dest_mat, -1, 1, beta);
		Utils.matToBitmap(final_dest_mat, destImage);

		Log.v(TAG, "destImage Size:" + destImage.getByteCount());
		
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
		sourceImage = null;
		
		return uri;
	}
}
