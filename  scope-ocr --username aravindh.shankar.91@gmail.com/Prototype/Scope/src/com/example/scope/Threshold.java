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

/* Class: Threshold
 * Author: Aravindh
 * 
 * Description:
 * A general image processing operator is a function that takes one or more input images 
 * and produces an output image.
 * 
 * This class tries to five types of thresholding operations.Applies a fixed-level threshold to each array element.
 * The class methods apply fixed-level thresholding to an RGB image, by converting it into a single channel greyscale image. 
 * The function is typically used for removing a noise, that is, filtering out pixels with too small or too large values. 
 * 
 * There are several types of thresholding functions supported in this class. They are :
 * 
 * Uri thresh_binary(double thresh, double maxval)
 * ---> dst(x,y) = maxval if src(x,y) > thresh; 0 otherwise
 * 
 * Uri thresh_binary_inv(double thresh, double maxval)
 * ---> dst(x,y) = 0 if src(x,y) > thresh; maxval otherwise
 * 
 * Uri thresh_trunc(double thresh)
 * ---> dst(x,y) = threshold if src(x,y) > thresh; src(x,y) otherwise
 * 
 * Uri thresh_tozero(double thresh) 
 * --->dst(x,y) = src(x,y) if src(x,y) > thresh; 0 otherwise
 * 
 * Uri thresh_tozero_inv(double thresh)  
 * --->dst(x,y) = 0 if src(x,y) > thresh; src(x,y) otherwise
 *
 * double otsu()
 * --->the function determines the optimal threshold value using the Otsu's algorithm and returns it. 
 * This value may later be used as <thresh> in any of the five above mentioned functions
 * 
 * Sample usage code:
 * 
 * Threshold thresh = new Threshold(this.getApplicationContext(),Uri.parse(path));
 * double value = thresh.otsu();
 * Uri ppimage = thresh.thres_tozero(180); //Uses user defined value to perform threshold operation on the image
 * ppimage = thresh.thresh_tozero(value); //Uses the otsu threshold value to perform threshold operation
 * 
 */

@TargetApi(12)
public class Threshold {
	double alpha;
	Uri inputImageUri;
	Context currContext;
	Mat sourceImageMat = new Mat();
	Mat destImageMat = new Mat();
	Bitmap sourceImage = null;
	Bitmap destImage = null;
	String file_name = "clean.bmp";
	Uri uri;

	private static final String TAG = "Scope.java";

	// Constructor
	public Threshold(Context c, Uri inputUri) {
		currContext = c;
		inputImageUri = inputUri;
	}
	
	public Threshold(Context c, Uri inputUri, String store) {
		currContext = c;
		inputImageUri = inputUri;
		file_name = store;
	}

	// Method to set image only, if class has already been instantiated
	public void SetImage(Uri inputUri) {
		inputImageUri = inputUri;
	}

	// Input alpha value for contrast as a percentage
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

	public Uri thresh_binary(double thresh, double maxval) {
		initiate();

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());

		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat
				.zeros(destImageMat.size(), destImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(destImageMat, final_dest_mat, thresh, maxval,
				Imgproc.THRESH_BINARY);
		Utils.matToBitmap(final_dest_mat, destImage);

		Log.v(TAG, "destImage Size: " + destImage.getByteCount());

		store();
		Log.v(TAG, uri.toString());
		return uri;
	}
	
	public Uri thresh_binary_inv(double thresh, double maxval){
		initiate();

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());

		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat
				.zeros(destImageMat.size(), destImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(destImageMat, final_dest_mat, thresh, maxval,
				Imgproc.THRESH_BINARY_INV);
		Utils.matToBitmap(final_dest_mat, destImage);

		Log.v(TAG, "destImage Size: " + destImage.getByteCount());

		store();
		Log.v(TAG, uri.toString());
		return uri;		
	}

	public Uri thresh_trunc(double thresh) {
		double maxval = thresh;
		initiate();

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());

		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat
				.zeros(destImageMat.size(), destImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(destImageMat, final_dest_mat, thresh, maxval,
				Imgproc.THRESH_TRUNC);
		Utils.matToBitmap(final_dest_mat, destImage);

		Log.v(TAG, "destImage Size: " + destImage.getByteCount());

		store();
		Log.v(TAG, uri.toString());
		return uri;		
		
	}
	
	public Uri thresh_tozero(double thresh) {
		double maxval = thresh;
		initiate();

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());

		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat
				.zeros(destImageMat.size(), destImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(destImageMat, final_dest_mat, thresh, maxval,
				Imgproc.THRESH_TOZERO);
		Utils.matToBitmap(final_dest_mat, destImage);

		Log.v(TAG, "destImage Size: " + destImage.getByteCount());

		store();
		Log.v(TAG, uri.toString());
		return uri;		
		
	}

	public Uri thresh_tozero_inv(double thresh) {
		double maxval = thresh;
		initiate();

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());

		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat
				.zeros(destImageMat.size(), destImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(destImageMat, final_dest_mat, thresh, maxval,
				Imgproc.THRESH_TOZERO_INV);
		Utils.matToBitmap(final_dest_mat, destImage);

		Log.v(TAG, "destImage Size: " + destImage.getByteCount());

		store();
		Log.v(TAG, uri.toString());
		return uri;		
		
	}
	
	public double otsu()
	{
		double maxval = 255;
		
		initiate();
		
		Mat temp = new Mat();
		destImage = sourceImage;
		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Mat final_dest_mat = Mat
				.zeros(temp.size(), temp.type());
		Imgproc.cvtColor(sourceImageMat, temp, Imgproc.COLOR_RGB2GRAY);
		temp.convertTo(destImageMat,0);
		double value = Imgproc.threshold(destImageMat, final_dest_mat, 0, maxval,
				Imgproc.THRESH_OTSU);
		Utils.matToBitmap(final_dest_mat, destImage);
		Log.v(TAG,"value: "+value);
		
		return value;			
	}
	
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
