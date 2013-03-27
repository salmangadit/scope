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

	List<String> coordinates = null;

	private static final String TAG = "Scope.java";

	// Constructor
	public Segmenter(Context c, Uri inputUri) {
		currContext = c;
		inputImageUri = inputUri;
		rand = new Random();
		coordinates = new ArrayList<String>();
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
				// Core.rectangle(sourceImageMat,
				// boundingRectangles.get(maxAreaIdx).tl(),
				// boundingRectangles.get(maxAreaIdx).br(), color, 2, 8, 0);

				backgrounds.add(boundingRectangles.get(maxAreaIdx));

				// Create region of interest and save as a seperate file
				Mat cropped = performCrop(boundingRectangles.get(maxAreaIdx).x,
						boundingRectangles.get(maxAreaIdx).y,
						boundingRectangles.get(maxAreaIdx).width,
						boundingRectangles.get(maxAreaIdx).height,
						sourceImageMat);

				coordinates.add(boundingRectangles.get(maxAreaIdx).x + ":"
						+ boundingRectangles.get(maxAreaIdx).y);

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

	public List<String> getCoordinates() {
		return coordinates;
	}

	/*
	 * This method segments the different parts of the card into its text
	 * components
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
		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_BGR2GRAY,
				0);
		Imgproc.medianBlur(destImageMat, destImageMat, 7);
		Imgproc.adaptiveThreshold(destImageMat, destImageMat, 255, 1, 1, 15, 2);

		Imgproc.dilate(destImageMat, destImageMat, new Mat(), new Point(), 6);
		Imgproc.erode(destImageMat, destImageMat, new Mat(), new Point(), 2);

		// Imgproc.threshold(destImageMat, destImageMat, 160, 160,
		// Imgproc.THRESH_BINARY_INV);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Mat heirarchy = new Mat();

		destImageMat_temp = destImageMat.clone();
		Imgproc.findContours(destImageMat_temp, contours, heirarchy,
				Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		List<Rect> boundingRectangles_temp = new ArrayList<Rect>();

		// Check tolerance area for large rectangles
		int sourceImageArea = sourceImageMat.width() * sourceImageMat.height();

		Log.v(TAG, "Total image area: " + sourceImageArea);
		double sourceAreaTolerance = sourceImageArea * 0.85;
		double toleranceMin = sourceImageArea * 0.001;

		for (int i = 0; i < contours.size(); i++) {
			Rect currentRectangle = Imgproc.boundingRect(contours.get(i));

			// Remove contours that could be mistaken edges
			if (currentRectangle.area() < sourceAreaTolerance
					&& currentRectangle.area() > toleranceMin) {
				boundingRectangles_temp.add(currentRectangle);
			}
		}
		Log.v(TAG,
				"Total contours found: " + contours.size());
		Log.v(TAG,
				"Total text regions found: " + boundingRectangles_temp.size());

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
		
		Log.v(TAG, "After removing inner rects: " + boundingRectangles.size());

		int MIN_DIST_CORNERS = 60;
		List<List<Rect>> clusters = new ArrayList<List<Rect>>(
				boundingRectangles.size());

		// Rectangle clustering algorithm
		for (int i = 0; i < boundingRectangles.size(); i++) {
			Point topLeft = new Point(boundingRectangles.get(i).x,
					boundingRectangles.get(i).y);
			Point topRight = new Point(boundingRectangles.get(i).x
					+ boundingRectangles.get(i).width,
					boundingRectangles.get(i).y);
			Point bottomLeft = new Point(boundingRectangles.get(i).x,
					boundingRectangles.get(i).y
							+ boundingRectangles.get(i).height);
			Point bottomRight = new Point(boundingRectangles.get(i).x
					+ boundingRectangles.get(i).width,
					boundingRectangles.get(i).y
							+ boundingRectangles.get(i).height);

			List<Point> first = new ArrayList<Point>();
			first.add(topLeft);
			first.add(topRight);
			first.add(bottomLeft);
			first.add(bottomRight);

			List<Rect> toCluster = new ArrayList<Rect>();

			for (int j = 0; j < boundingRectangles.size(); j++) {
				if (boundingRectangles.get(j).equals(boundingRectangles.get(i))) {
					continue;
				}
				Point topLeftSecond = new Point(boundingRectangles.get(j).x,
						boundingRectangles.get(j).y);
				Point topRightSecond = new Point(boundingRectangles.get(j).x
						+ boundingRectangles.get(j).width,
						boundingRectangles.get(j).y);
				Point bottomLeftSecond = new Point(boundingRectangles.get(j).x,
						boundingRectangles.get(j).y
								+ boundingRectangles.get(j).height);
				Point bottomRightSecond = new Point(boundingRectangles.get(j).x
						+ boundingRectangles.get(j).width,
						boundingRectangles.get(j).y
								+ boundingRectangles.get(j).height);

				List<Point> second = new ArrayList<Point>();
				second.add(topLeftSecond);
				second.add(topRightSecond);
				second.add(bottomLeftSecond);
				second.add(bottomRightSecond);

				boolean doneCluster = false;
				for (int m = 0; m < first.size(); m++) {
					for (int n = 0; n < second.size(); n++) {
						double distance = distance(first.get(m), second.get(n));

						if (distance < MIN_DIST_CORNERS) {
							if (!toCluster.contains(boundingRectangles.get(i))) {
								toCluster.add(boundingRectangles.get(i));
							}

							if (!toCluster.contains(boundingRectangles.get(j))) {
								toCluster.add(boundingRectangles.get(j));
							}

							doneCluster = true;
							break;
						}
					}

					if (doneCluster)
						break;
					else if (!toCluster.contains(boundingRectangles.get(i))) {
						toCluster.add(boundingRectangles.get(i));
					}

				}
			}

			boolean addToCluster = false;
			int clusterIndexToAdd = 0;

			// analyse clusters
			for (int x = 0; x < clusters.size(); x++) {
				for (int y = 0; y < clusters.get(x).size(); y++) {
					for (int z = 0; z < toCluster.size(); z++) {
						if (toCluster.get(z).equals(clusters.get(x).get(y))) {
							addToCluster = true;
							clusterIndexToAdd = x;
						}
					}
				}
			}

			// if can be added to an existing cluster
			if (addToCluster) {
				for (int z = 0; z < toCluster.size(); z++) {
					if (!clusters.get(clusterIndexToAdd).contains(
							toCluster.get(z))) {
						clusters.get(clusterIndexToAdd).add(toCluster.get(z));
					}
				}
			} else {
				// create new cluster
				if (toCluster.size() > 0)
					clusters.add(toCluster);

			}
		}

		// Apply result of clustering
		List<Rect> boundingClusteredRects = new ArrayList<Rect>();

		for (int i = 0; i < clusters.size(); i++) {
			double minX = sourceImageMat.width();
			double minY = sourceImageMat.height();
			double maxX = 0;
			double maxY = 0;

			for (int j = 0; j < clusters.get(i).size(); j++) {
				if (clusters.get(i).get(j).x < minX) {
					minX = clusters.get(i).get(j).x;
				}

				if (clusters.get(i).get(j).x + clusters.get(i).get(j).width > maxX) {
					maxX = clusters.get(i).get(j).x
							+ clusters.get(i).get(j).width;
				}

				if (clusters.get(i).get(j).y < minY) {
					minY = clusters.get(i).get(j).y;
				}

				if (clusters.get(i).get(j).y + clusters.get(i).get(j).height > maxY) {
					maxY = clusters.get(i).get(j).y
							+ clusters.get(i).get(j).height;
				}
			}

			Rect clustered = new Rect((int) minX, (int) minY,
					(int) (maxX - minX), (int) (maxY - minY));
			boundingClusteredRects.add(clustered);
		}
		
		Log.v(TAG, "After clustering: " + boundingClusteredRects.size());

		//Mat drawing = Mat.zeros(destImageMat.size(), CvType.CV_8UC3);
		for (int i = 0; i < boundingClusteredRects.size(); i++) {
			Scalar color = new Scalar((rand.nextInt(max - min + 1) + min),
					(rand.nextInt(max - min + 1) + min), (rand.nextInt(max
							- min + 1) + min));
			// Imgproc.drawContours(destImageMat, contours, i, color, 1, 8,
			// heirarchy, 0, new Point());
			// Core.rectangle(sourceImageMat, boundingRectangles.get(i).tl(),
			// boundingRectangles.get(i).br(), color, 2, 8, 0);

			// Create region of interest and save as a seperate file
			Mat cropped = performCrop(boundingClusteredRects.get(i).x,
					boundingClusteredRects.get(i).y,
					boundingClusteredRects.get(i).width,
					boundingClusteredRects.get(i).height, sourceImageMat);

			coordinates.add(boundingClusteredRects.get(i).x + ":"
					+ boundingClusteredRects.get(i).y);

			destImage = Bitmap.createBitmap(boundingClusteredRects.get(i).width,
					boundingClusteredRects.get(i).height, Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(cropped.clone(), destImage);

			Log.v(TAG, "destImage Size:" + destImage.getByteCount());

			File file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"bg-seg" + backgroundSegmentationId + "-text-seg" + i
							+ ".bmp");

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

		if (segmentedResults.size() == 0) {
			Log.v(TAG, "No Segments Found. Returning original");
			segmentedResults.add(inputImageUri);
			coordinates.add("99:99");
		}

		return segmentedResults;
	}
	
	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((Math.pow((p1.x - p2.x), 2) + Math.pow(p1.y - p2.y, 2)));
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
