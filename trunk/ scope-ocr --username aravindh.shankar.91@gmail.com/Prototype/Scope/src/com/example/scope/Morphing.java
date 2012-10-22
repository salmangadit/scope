package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/* Class: Morphing
 * Author: Aravindh
 * 
 * Description:
 * Morphing refers to a set of operations that process images based on shapes. Morphological operations 
 * apply a structuring element to an input image and generate an output image.
 * The most basic morphological operations are two: Erosion and Dilation. They have a wide array of uses, i.e.
 * Removing noise, Isolation of individual elements and joining disparate elements in an image
 * and finding intensity bumps or holes in an image
 * 
 * This class tries to apply the erode and the dilate operation to the original image.
 * This operations consists of convoluting an image  with some kernel (), which can have any shape or size, usually a square or circle.
 * The kernel  has a defined anchor point, usually being the center of the kernel.
 * As the kernel  is scanned over the image, we compute the minimal pixel value overlapped by and replace the image pixel under the anchor point with that minimal value.
 * 
 * Since dilation and erosion are sister operations and to make the api calls more intuitive,
 * only one function erode(%) is publicly available for calls. Giving a neative percentage input to erode(%) would invoke the dilate operation.
 * 
 * Accepts input % from -100% to 100%
 * 100% is capped at kernel matrix size = 10
 * 0% starts at kernel matrix size = 0
 * Negative % value invokes dilate(%) with the same thresholds
 * 
 * Input bitmap size = Output bitmap size
 *
 * Sample usage code:
 * Morphing morphing = new Morphing(context, inputImageUri);
 * Uri morphedImage = morphing.erode(30); //erode using matrix size (3,3)
 */

public class Morphing {
	double matrix_size;
	Uri inputImageUri;
	Context currContext;
	
	private static final String TAG = "Scope.java";
	
	// Constructor
	public Morphing(Context c, Uri inputUri)
	{
		currContext = c;
		inputImageUri = inputUri;
	}
	
	// Method to set image only, if class has already been instantiated
	public void SetImage(Uri inputUri)
	{
		inputImageUri = inputUri;
	}
	
	// Input value for morphing as a percentage
	public Uri erode(double matrixPercentage) {
		
		
		if (matrixPercentage > 100)
			matrixPercentage = 100;
		else if (matrixPercentage < -100)
			matrixPercentage = -100;
		
		matrix_size = matrixPercentage/10;
		
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
		Mat destImageMat = Mat.zeros(sourceImageMat.size(), sourceImageMat.type());
		
		if(matrix_size>=0)
		{
			Size size = new Size(matrix_size,matrix_size);
			Imgproc.erode( sourceImageMat, destImageMat, Mat.ones(size,0));	
		}
		else
		{
			Size size = new Size(-matrix_size,-matrix_size);
			Imgproc.dilate( sourceImageMat, destImageMat, Mat.ones(size,0));	
		}
			
		Utils.matToBitmap(destImageMat, destImage);
		
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
