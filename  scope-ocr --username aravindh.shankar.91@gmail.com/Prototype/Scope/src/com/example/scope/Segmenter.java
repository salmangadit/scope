package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class Segmenter {
	Uri inputImageUri;
	Context currContext;
	Random rand;
	int min = 100;
	int max = 255;
	static int BIN_NUMBER = 16;

	private static final String TAG = "Scope.java";

	// Constructor
	public Segmenter(Context c, Uri inputUri) {
		currContext = c;
		inputImageUri = inputUri;
		rand = new Random();
	}

	// Method to set image only, if class has already been instantiated
	public void SetImage(Uri inputUri) {
		inputImageUri = inputUri;
	}

	/*
	 * This method segments the image according to background color
	 */
	public List<Uri> SegmentBackground() {
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
		destImageMat_temp = Mat.zeros(sourceImageMat.size(),
				sourceImageMat.type());
		destImageMat = Mat.zeros(sourceImageMat.size(), sourceImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat_temp,
				Imgproc.COLOR_BGR2GRAY, 0);

		// Calculate histogram
		List<Mat> images = new ArrayList<Mat>();
		images.add(destImageMat_temp);

		MatOfInt channels = new MatOfInt(0);
		MatOfInt histSize = new MatOfInt(BIN_NUMBER);
		MatOfFloat ranges = new MatOfFloat(0f, 256f);

		Mat hist = new Mat();
		Imgproc.calcHist(images, channels, new Mat(), hist, histSize, ranges);

		Log.v(TAG, "Histogram: " + hist.dump());
		List<Integer> histValues = this.stringToIntegerList(hist.dump());

		// Find percentage of histogram to correspond to background value
		int maxHistValue = Collections.max(histValues);
		Log.v(TAG, "Tolerance value from Histogram: " + 0.3 * maxHistValue);
		// Get bins of all possible matches
		List<Integer> bIndex = new ArrayList<Integer>();

		for (int i = 0; i < histValues.size(); i++) {
			if (histValues.get(i) >= 0.3 * maxHistValue) {
				bIndex.add(maxBinValue(i));
			}
		}
		// Make bounding rectangles
		List<Rect> boundingRectangles = new ArrayList<Rect>();
		List<Rect> backgrounds = new ArrayList<Rect>();
		List<Uri> segmentedResults = new ArrayList<Uri>();

		for (int m = 0; m < bIndex.size(); m++) {
			// Apply mask
			Core.inRange(destImageMat_temp, new Scalar(bIndex.get(m)
					- BIN_NUMBER), new Scalar(bIndex.get(m)), destImageMat);

			// Find contours for each mask
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Mat heirarchy = new Mat();
			Imgproc.findContours(destImageMat, contours, heirarchy,
					Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(
							0, 0));

			for (int i = 0; i < contours.size(); i++) {
				Rect currentRectangle = Imgproc.boundingRect(contours.get(i));
				boundingRectangles.add(currentRectangle);
			}

			// Find largest contour
			double maxArea = 0;
			int maxAreaIdx = -1;
			for (int i = 0; i < boundingRectangles.size(); i++) {
				if (boundingRectangles.get(i).area() > maxArea) {
					maxAreaIdx = i;
					maxArea = boundingRectangles.get(i).area();
				}
			}
			Log.v(TAG, "Maximum index: " + maxAreaIdx);

			if (maxAreaIdx > -1) {
				Scalar color = new Scalar((rand.nextInt(max - min + 1) + min),
						(rand.nextInt(max - min + 1) + min), (rand.nextInt(max
								- min + 1) + min));
				Core.rectangle(sourceImageMat,
						boundingRectangles.get(maxAreaIdx).tl(),
						boundingRectangles.get(maxAreaIdx).br(), color, 2, 8, 0);

				backgrounds.add(boundingRectangles.get(maxAreaIdx));

				// Create region of interest and save as a seperate file
				Mat cropped = performCrop(boundingRectangles.get(maxAreaIdx).x,
						boundingRectangles.get(maxAreaIdx).y,
						boundingRectangles.get(maxAreaIdx).width,
						boundingRectangles.get(maxAreaIdx).height,
						sourceImageMat);
				destImage = Bitmap.createBitmap(
						boundingRectangles.get(maxAreaIdx).width,
						boundingRectangles.get(maxAreaIdx).height,
						Bitmap.Config.ARGB_8888);
				Utils.matToBitmap(cropped.clone(), destImage);

				Log.v(TAG, "destImage Size:" + destImage.getByteCount());

				File file = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						"bg-seg" + m + ".bmp");

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
		}

		return segmentedResults;
	}
	
	/*
	 * This method segments the different parts of the card into its text components
	 */
	public List<Uri> SegmentText(Integer backgroundSegmentationId) {
		Mat sourceImageMat = new Mat();
		Mat destImageMat_temp = new Mat();
		Mat destImageMat = new Mat();

		List<Uri> segmentedResults = new ArrayList<Uri>();

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
		destImageMat_temp = Mat.zeros(sourceImageMat.size(),
				sourceImageMat.type());
		destImageMat = Mat.zeros(sourceImageMat.size(), sourceImageMat.type());
		Imgproc.cvtColor(sourceImageMat, destImageMat_temp,
				Imgproc.COLOR_BGR2GRAY, 0);
		Imgproc.medianBlur(destImageMat_temp, destImageMat, 7);
		Imgproc.adaptiveThreshold(destImageMat, destImageMat, 255, 1, 1, 11, 2);

		Imgproc.dilate(destImageMat, destImageMat_temp, new Mat(), new Point(),
				7);
		Imgproc.erode(destImageMat_temp, destImageMat, new Mat(), new Point(),
				2);

		Imgproc.threshold(destImageMat, destImageMat, 160, 160,
				Imgproc.THRESH_BINARY);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Mat heirarchy = new Mat();
		Imgproc.findContours(destImageMat, contours, heirarchy,
				Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		List<Rect> boundingRectangles_temp = new ArrayList<Rect>();

		// Check tolerance area for large rectangles
		int sourceImageArea = sourceImageMat.width() * sourceImageMat.height();
		double sourceAreaTolerance = sourceImageArea * 0.9;

		for (int i = 0; i < contours.size(); i++) {
			Rect currentRectangle = Imgproc.boundingRect(contours.get(i));

			// Remove contours that could be mistaken edges
			if (currentRectangle.area() < sourceAreaTolerance) {
				boundingRectangles_temp.add(currentRectangle);
			}
		}

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

		Mat drawing = Mat.zeros(destImageMat.size(), CvType.CV_8UC3);
		for (int i = 0; i < boundingRectangles.size(); i++) {
			Scalar color = new Scalar((rand.nextInt(max - min + 1) + min),
					(rand.nextInt(max - min + 1) + min), (rand.nextInt(max
							- min + 1) + min));
			// Imgproc.drawContours(destImageMat, contours, i, color, 1, 8,
			// heirarchy, 0, new Point());
			Core.rectangle(sourceImageMat, boundingRectangles.get(i).tl(),
					boundingRectangles.get(i).br(), color, 2, 8, 0);

			// Create region of interest and save as a seperate file
			Mat cropped = performCrop(boundingRectangles.get(i).x,
					boundingRectangles.get(i).y,
					boundingRectangles.get(i).width,
					boundingRectangles.get(i).height, sourceImageMat);

			destImage = Bitmap.createBitmap(boundingRectangles.get(i).width,
					boundingRectangles.get(i).height, Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(cropped.clone(), destImage);

			Log.v(TAG, "destImage Size:" + destImage.getByteCount());

			File file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"bg-seg"+backgroundSegmentationId+"-text-seg" + i + ".bmp");

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

		return segmentedResults;
	}

	private int maxBinValue(int bin) {
		int binSize = 256 / BIN_NUMBER;
		int compartment = binSize * (bin + 1);

		return compartment - 1;
	}

	private List<Integer> stringToIntegerList(String s) {
		s = s.replace("[", "");
		s = s.replace("]", "");

		String[] values = s.split(";");
		List<Integer> numbers = new ArrayList<Integer>();

		for (int i = 0; i < values.length; i++) {
			numbers.add(Integer.parseInt(values[i].trim()));
		}

		return numbers;
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
