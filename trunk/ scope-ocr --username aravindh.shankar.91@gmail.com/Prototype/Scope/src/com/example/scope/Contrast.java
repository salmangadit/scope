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

/* Class: Contrast
 * Author: Aravindh
 * 
 * Description:
 * A general image processing operator is a function that takes one or more input images 
 * and produces an output image.
 * In this kind of image processing transform, each output pixel’s value depends on only 
 * the corresponding input pixel value (plus, potentially, some globally collected information 
 * or parameters).
 * 
 * This class tries to increase the contrast in an image by multiplying the pixels by a constant float value.
 * out_mat(x,y) = inp_mat(x,y)*alpha
 * alpha at 1, means the output image is the same as input
 * 
 * Normal image is at 20% contrast
 * 100% is capped at alpha = 3, otherwise image becomes too white
 * 0% starts at alpha = 0.5
 * 
 * Input bitmap size = Output bitmap size
 *
 * Sample usage code:
 * Contrast contrast = new Contrast(context, inputImageUri);
 * Uri contrastImage = contrast.contrast_change(30) //30% change in contrast
 */

public class Contrast {
	double alpha;
	Uri inputImageUri;
	Context currContext;
	
	private static final String TAG = "Scope.java";
	
	// Constructor
	public Contrast(Context c, Uri inputUri)
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
	public Uri contrast_change(double alphaPercentage) {
		
		if (alphaPercentage > 100)
			alphaPercentage = 3;
		else if (alphaPercentage < 0)
			alphaPercentage = 0.5;
		
		alpha = 0.5 + alphaPercentage/40;
		
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
		
		destImage=sourceImage;
		
		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Imgproc.cvtColor(sourceImageMat, destImageMat_temp, Imgproc.COLOR_RGB2BGRA, 0);
		Imgproc.cvtColor(destImageMat_temp, destImageMat, Imgproc.COLOR_BGRA2RGBA, 0);
		Mat final_dest_mat = Mat.zeros(destImageMat.size(), destImageMat.type());
		destImageMat.convertTo(final_dest_mat, -1, alpha, 0);
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
		
		return uri;
	}
}
