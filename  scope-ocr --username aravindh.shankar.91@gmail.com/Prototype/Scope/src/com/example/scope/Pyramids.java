package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/* Class: Pyramids
 * Author: Aravindh
 * 
 * Description:
 * A general image processing operator is a function that takes one or more input images 
 * and produces an output image.
 * In this image processing transform, an RGB input bitmap image is performed on by two operations
 * pyrUp and pyrDown.
 * 
 * 1.pyrUp is used to downisize an image (zoom out) using the concept of gaussian pyramids.
 *   New image bitmap is 4 times the size of original image.
 *   
 * 2.pyrDown is used to upsize an image (zoom in) using the concept of gaussian pyramids.
 *   New image bitmap is 1/4 times the size of original image.
 * 
 * Methods return a uri to the result image
 * 
 * Sample usage code:
 * Pyramids pyr = new Pyramids(this.getApplicationContext(),Uri.parse(path));
 * Uri ppimage = pyr.pyrDown();
 * pyr.SetImage(ppimage);
 * ppimage=pyr.pyrDown(); // Performing pyrDown twice on the same image
 */

@TargetApi(12)
public class Pyramids {
	double alpha;
	Uri inputImageUri;
	Context currContext;
	Mat sourceImageMat = new Mat();
	Bitmap sourceImage = null;
	Bitmap destImage = null;
	Uri uri;
	
	private static final String TAG = "Scope.java";
	
	// Constructor
	public Pyramids(Context c, Uri inputUri)
	{
		currContext = c;
		inputImageUri = inputUri;
	}
	
	// Method to set image only, if class has already been instantiated
	public void SetImage(Uri inputUri)
	{
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

	
	public Uri pyrDown() {
		initiate();
		destImage = Bitmap.createScaledBitmap(sourceImage, (sourceImage.getWidth()/2) + (sourceImage.getWidth()%2), (sourceImage.getHeight()/2)+(sourceImage.getHeight()%2), true);

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());
		
		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Size size = new Size((sourceImageMat.cols()+1)/2,(sourceImageMat.rows()+1)/2);
		Mat final_dest_mat = Mat.zeros(size, sourceImageMat.type());
		Imgproc.pyrDown(sourceImageMat, final_dest_mat);
		Utils.matToBitmap(final_dest_mat, destImage);
		
		Log.v(TAG, "destImage Size: " + destImage.getByteCount());
		
		store();
		Log.v(TAG, uri.toString());
		return uri;
	}
	
	public Uri pyrUp() {
		initiate();
		destImage = Bitmap.createScaledBitmap(sourceImage, sourceImage.getWidth()*2, sourceImage.getHeight()*2, true);

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());
		
		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Size size = new Size(sourceImageMat.cols()*2,sourceImageMat.rows()*2);
		Mat final_dest_mat = Mat.zeros(size, sourceImageMat.type());
		Imgproc.pyrUp(sourceImageMat, final_dest_mat);
		Utils.matToBitmap(final_dest_mat, destImage);
		
		Log.v(TAG, "destImage Size: " + destImage.getByteCount());
		
		store();
		Log.v(TAG, uri.toString());
		return uri;
	}
	
	public void store() {
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

		uri = Uri.fromFile(file);
	}
}
