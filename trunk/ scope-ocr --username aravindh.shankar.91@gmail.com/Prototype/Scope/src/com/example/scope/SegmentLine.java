package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

@TargetApi(12)
public class SegmentLine {
	double alpha;
	Uri inputImageUri, filterImageUri;
	Context currContext;
	Mat sourceImageMat = new Mat();
	Mat destImageMat = new Mat();
	Bitmap sourceImage = null;
	Bitmap destImage = null;
	Bitmap filterImage = null;
	String file_name = "temp.bmp";
	int min = 100;
	int max = 255;
	static int BIN_NUMBER = 16;
	Random rand;
	List<String> coordinates = null;
	Uri uri;

	private static final String TAG = "Scope.java";

	// Constructor
	public SegmentLine(Context c, Uri inputUri, Uri filterUri) {
		currContext = c;
		inputImageUri = inputUri;
		filterImageUri = filterUri;
		rand = new Random();
		coordinates = new ArrayList<String>();
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
		try {
			filterImage = MediaStore.Images.Media.getBitmap(
					currContext.getContentResolver(), filterImageUri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "NULL");
			e.printStackTrace();
		}
	}

	// This method segments the different parts of the card by line
	// components
	public List<Uri> segLine() {
		initiate();

		Log.v(TAG, "Segmenting line");
		Mat sourceImageMat = new Mat();
		Mat filterImageMat = new Mat();
		Mat destImageMat_temp = new Mat();
		Mat destImageMat = new Mat();

		List<Uri> segmentedResults = new ArrayList<Uri>();

		Bitmap destImage = null;

		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());
		destImage = sourceImage;

		Utils.bitmapToMat(sourceImage, sourceImageMat);
		Utils.bitmapToMat(filterImage, filterImageMat);
		
		destImageMat_temp = Mat.zeros(sourceImageMat.size(),
				sourceImageMat.type());
		destImageMat = Mat.zeros(sourceImageMat.size(), sourceImageMat.type());

		Core.rectangle(sourceImageMat, new Point(0, 0), new Point(
				sourceImageMat.width(), sourceImageMat.height()), new Scalar(
				255, 0, 0), 25, 8, 0);

		Imgproc.cvtColor(sourceImageMat, destImageMat_temp,
				Imgproc.COLOR_BGR2GRAY, 0);

		Imgproc.threshold(destImageMat_temp, destImageMat, 160, 160,
				Imgproc.THRESH_BINARY);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Mat heirarchy = new Mat();
		Imgproc.findContours(destImageMat, contours, heirarchy,
				Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		List<Rect> boundingRectangles_temp = new ArrayList<Rect>();

		// Check tolerance area for large rectangles
		int sourceImageArea = sourceImageMat.width() * sourceImageMat.height();
		double sourceAreaTolerance = sourceImageArea * 0.95;
		double sourceAreaMinTolerance = sourceImageArea * 0.01;
		
		Log.v(TAG, "Number of Contours: " + contours.size());

		for (int i = 0; i < contours.size(); i++) {
			Rect currentRectangle = Imgproc.boundingRect(contours.get(i));

			// Remove contours that could be mistaken edges
			if (currentRectangle.area() < sourceAreaTolerance && currentRectangle.area()>sourceAreaMinTolerance) {
				boundingRectangles_temp.add(currentRectangle);
			}
		}
		
		Log.v(TAG, "After size restrictions: " + boundingRectangles_temp.size());

		// Remove all rectangles inside any outer rectangles
		List<Integer> toRemove = new ArrayList<Integer>();
		// Perform polygon test to remove inner rectangles
		for (int i = 0; i < boundingRectangles_temp.size(); i++) {
			for (int j = 0; j < boundingRectangles_temp.size(); j++) {
				if ((boundingRectangles_temp.get(i).br()
						.inside(boundingRectangles_temp.get(j)))
						&& (boundingRectangles_temp.get(i).tl()
								.inside(boundingRectangles_temp.get(j)))) {

					if (!toRemove.contains(i)) {
						toRemove.add(i);
					}
				}
			}
		}
		List<Rect> boundingRectangles = new ArrayList<Rect>();
		for (int i = 0; i < boundingRectangles_temp.size(); i++) {
			// boundingRectangles_temp.remove(boundingRectangles_temp.get(toRemove.get(i)));
			if (toRemove.contains(i)) {
				continue;
			}

			boundingRectangles.add(boundingRectangles_temp.get(i));
		}

		Log.v(TAG, "After inner rects " + boundingRectangles.size());

		for (int i = 0; i < boundingRectangles.size(); i++) {
			Scalar color = new Scalar((rand.nextInt(max - min + 1) + min),
					(rand.nextInt(max - min + 1) + min), (rand.nextInt(max
							- min + 1) + min));

//			Core.rectangle(filterImageMat, boundingRectangles.get(i).tl(),
//					boundingRectangles.get(i).br(), color, 2, 8, 0);

			// Create region of interest and save as a seperate file
			Mat cropped = performCrop(boundingRectangles.get(i).x,
					boundingRectangles.get(i).y,
					boundingRectangles.get(i).width,
					boundingRectangles.get(i).height, filterImageMat);

			destImage = Bitmap.createBitmap(boundingRectangles.get(i).width,
					boundingRectangles.get(i).height, Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(cropped, destImage);

			Log.v(TAG, "destImage Size:" + destImage.getByteCount());

			File file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"bg_line" + i + ".bmp");

			try {
				FileOutputStream out = new FileOutputStream(file);
				destImage.compress(Bitmap.CompressFormat.PNG, 90, out);
			} catch (Exception e) {
				Log.v(TAG, "null2");
				e.printStackTrace();
			}

			final Uri uri = Uri.fromFile(file);
			Log.v(TAG, uri.toString());
			segmentedResults.add(uri);
		}
		
		if (segmentedResults.size() == 0){
			segmentedResults.add(filterImageUri);
		}
		
		sourceImage.recycle();
		filterImage.recycle();
		destImage.recycle();
		
		return segmentedResults;

	}

	private Mat performCrop(int x, int y, int width, int height, Mat source) {
		Mat destImageMat = new Mat();
		destImageMat = Mat.zeros(source.size(), source.type());
		Imgproc.cvtColor(source, destImageMat, Imgproc.COLOR_BGR2GRAY, 0);

		Rect roi = new Rect(x, y, width, height);
		Mat cropped = new Mat(destImageMat, roi);

		return cropped;
	}

}
