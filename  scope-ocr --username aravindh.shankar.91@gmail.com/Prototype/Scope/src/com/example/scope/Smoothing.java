package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/* Class: Smoothing
 * Author: Salman
 * 
 * Description: 
 * Smoothing, also called blurring, is a simple and frequently used image processing operation. It is
 * mainly used to reduce noise in a picture. 
 * 
 * To perform a smoothing operation we will apply a filter to our image. The most common type of filters are 
 * linear, in which an output pixel’s value is determined as a weighted sum of input pixel values.
 * Eg:- g(i,j) = Sum(f(i+k, j+l).h(k,l))
 * 		g = Output image
 * 		f = Source image
 * 		h(k,l) = Kernel, which is the coefficients of the filter
 * 
 * Sample usage code:
 * Smoothing smoother = new Smoothing(context, uri);
 * Uri normalizedImage = smoother.HomogeneousFilter();
 * Uri gaussianImage = smoother.GaussianFilter();
 * Uri medianImage = smoother.MedianFilter();
 * Uri bilateralImage = smoother.BilateralFilter();
 */
public class Smoothing {
	Uri inputImageUri;
	Context currContext;
	Mat src=new Mat();
	Mat dst=new Mat();
	Bitmap destImage = null;
	Bitmap sourceImage = null;

	static int MAX_KERNEL_LENGTH = 31;

	private static final String TAG = "Scope.java";

	// Constructor
	public Smoothing(Context c, Uri inputUri) {
		currContext = c;
		inputImageUri = inputUri;

		FixImageProperties();
	}

	// Method to set image only, if class has already been instantiated
	public void SetImage(Uri inputUri) {
		inputImageUri = inputUri;
		FixImageProperties();
	}

	/*
	 * Normalised Box/Homogeneous Filter: This filter is the simplest of all!
	 * Each output pixel is the mean of its kernel neighbors (all of them
	 * contribute with equal weights) Check
	 * ./DocumentationImages/normalizedbox.png
	 */
	public Uri HomogeneousFilter() {
		Log.v(TAG, "null2");
		for (int i = 1; i < 4; i = i + 2) {
			Imgproc.blur(src, dst, new Size(i, i), new Point(-1, -1));
		}

		Utils.matToBitmap(dst, destImage);

		return getBitmapUri(destImage);
	}

	/*
	 * Gaussian Filter: Probably the most useful filter (although not the
	 * fastest). Gaussian filtering is done by convolving each point in the
	 * input array with a Gaussian kernel and then summing them all to produce
	 * the output array. Check ./DocumentationImages/gaussian.png
	 */
	public Uri GaussianFilter() {
		Log.v(TAG, "null2");
		for (int i = 1; i < 5; i = i + 2) {
			Imgproc.GaussianBlur(src, dst, new Size(i, i), 0, 0);
		}

		Utils.matToBitmap(dst, destImage);

		return getBitmapUri(destImage);
	}

	/*
	 * Median Filter: The median filter run through each element of the signal
	 * (in this case the image) and replace each pixel with the median of its
	 * neighboring pixels
	 */
	public Uri MedianFilter() {
		Log.v(TAG, "null2");
		for (int i = 1; i < 4; i = i + 2) {
			Imgproc.medianBlur(src, dst, i);
		}

		Utils.matToBitmap(dst, destImage);

		return getBitmapUri(destImage);
	}

	/*
	 * Bilateral Filter: Sometimes the filters do not only dissolve the noise,
	 * but also smooth away the edges. To avoid this (at certain extent at
	 * least), we can use a bilateral filter.In an analogous way as the Gaussian
	 * filter, the bilateral filter also considers the neighboring pixels with
	 * weights assigned to each of them. These weights have two components, the
	 * first of which is the same weighting used by the Gaussian filter. The
	 * second component takes into account the difference in intensity between
	 * the neighboring pixels and the evaluated one.
	 * 
	 * Reference:
	 * http://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/MANDUCHI1
	 * /Bilateral_Filtering.html
	 */
	public Uri BilateralFilter() {
		Mat destImageMat_temp = new Mat();
		Imgproc.cvtColor(src, destImageMat_temp, Imgproc.COLOR_RGBA2RGB, 0);
		
		Log.v(TAG, "Start bilateral");
		for (int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2) {
			try {
			Imgproc.bilateralFilter(destImageMat_temp, dst, i, i * 2, i / 2);
			} catch (CvException e) {
				Log.v(TAG, e.getMessage());
			}
		}
		Log.v(TAG, "End bilateral, start convert");
		Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2RGBA, 0);
		Log.v(TAG, "End convert");
		Utils.matToBitmap(dst, destImage);

		return getBitmapUri(destImage);
	}
	
	private void FixImageProperties(){
		try {
			sourceImage = MediaStore.Images.Media.getBitmap(
					currContext.getContentResolver(), inputImageUri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "NULL");
			e.printStackTrace();
		}
		
		destImage = sourceImage;
		
		Utils.bitmapToMat(sourceImage, src);
		Log.v(TAG, "Bitmapped the matrix!");	
		dst = Mat.zeros(src.size(), src.type());
	}

	// Private method to retrieve the URI of the converted image
	private Uri getBitmapUri(Bitmap image) {
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"temp2.bmp");

		try {
			FileOutputStream out = new FileOutputStream(file);
			image.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			Log.v(TAG, "null2");
			e.printStackTrace();
		}

		final Uri uri = Uri.fromFile(file);

		return uri;
	}
}