package com.example.scope;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/* Class: EdgeDetection
 * Author: Varun Ganesh
 * 
 * Description:
 * Takes the input image and identifies the area of interest i.e. the borders
 * of the contact card and performs a cropping operation of the area
 * Algorithm uses a combination of Canny Detector and thresholding at multiple
 * levels to detect card edges.
 * 
 * Sample usage code:
 * EdgeDetection detector = new EdgeDetection(context, String inputfilepath);
 * Uri EdgeDetectedImage = detector.EdgeDetect();
 */

public class EdgeDetection {

	private static final String TAG = "Scope.java";
	
	static {
		if (!OpenCVLoader.initDebug()) {
			Log.e(TAG, "Some Error!");
		}
	}
	
	String filePath;
	Context currContext;
	Mat src = new Mat();
	Mat dst = new Mat();
	Mat dst_x = new Mat();
	Mat dst_y = new Mat();
	Mat edges = new Mat();
	Size s = new Size(3, 3);
	Bitmap destImage = null;
	Bitmap sourceImage = null;
	int ddepth = CvType.CV_16S;
	
	int ratio = 3;
	int threshold_low = 100;
	int kernel_size = 3;

	public EdgeDetection(Context c, String filepath) {
		filePath = filepath;
		currContext = c;
		FixImageProperties();
	}

	private void FixImageProperties() {
		BitmapHandler bitmaphandler = new BitmapHandler(currContext);
		sourceImage = bitmaphandler.decodeFileAsPath(filePath);
		destImage = sourceImage;
		src.release();
		Utils.bitmapToMat(sourceImage, src);
		Log.v(TAG, "Bitmap to Mat Successful");
		dst = Mat.zeros(src.size(), src.type());
	}

	// Private method to retrieve the URI of the converted image
	private Uri getBitmapUri(Bitmap image) {
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"temp.bmp");

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

	public double angle( Point pt1, Point pt2, Point pt0 ) {
	    double dx1 = pt1.x - pt0.x;
	    double dy1 = pt1.y - pt0.y;
	    double dx2 = pt2.x - pt0.x;
	    double dy2 = pt2.y - pt0.y;
	    return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
	}
	
	public Uri EdgeDetect() {
		Mat blurred = new Mat();
		src.copyTo(blurred);
		Log.v(TAG, "Blurred Matrix! : " + blurred.total() );
		
		Imgproc.medianBlur(src, blurred, 9);
		Log.v(TAG, "Median Blur Done!");
        
		Mat gray0 = new Mat(blurred.size(), blurred.type());
		Imgproc.cvtColor(gray0, gray0, Imgproc.COLOR_RGB2GRAY);
		Mat gray = new Mat();

		Log.v(TAG, "Gray0 Matrix! : " + gray0.total() );
		Log.v(TAG, "Gray Matrix! : " + gray.total() );
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> squares = new ArrayList<MatOfPoint>();
		
		// find squares in every color plane of the image
	    for (int c = 0; c < 3; c++)
	    {
			Log.v(TAG, "Mix Channels Started! : " + gray0.total());
	        int ch[] = {c, 0};
	        MatOfInt fromto = new MatOfInt(ch);
	        List<Mat> blurredlist = new ArrayList<Mat>();
	        List<Mat> graylist = new ArrayList<Mat>();
	        blurredlist.add(0, blurred);
	        graylist.add(0, gray0);
	        Core.mixChannels(blurredlist, graylist, fromto);
	        gray0 = graylist.get(0);
			Log.v(TAG, "Mix Channels Done! : " + gray0.total() );
	     // try several threshold levels
	        int threshold_level = 2;
	        for (int l = 0; l < threshold_level; l++)
	        {
	            // Use Canny instead of zero threshold level!
	            // Canny helps to catch squares with gradient shading
	        	Log.v(TAG,"Threshold Level: " + l);
	        	
	            if (l >=0)
	            {
	                Imgproc.Canny(gray0, gray, 20, 30); // 

	                // Dilate helps to remove potential holes between edge segments
	                Imgproc.dilate(gray, gray, Mat.ones(new Size(3,3),0));
	                
	            }
	            else
	            {
	                    int thresh = (l+1) * 255 / threshold_level;
	                    Imgproc.threshold(gray0, gray, thresh, 255, Imgproc.THRESH_TOZERO);
	            }

	    		Log.v(TAG, "Canny (or Thresholding) Done!");
	    		Log.v(TAG, "Gray Matrix (after)! : " + gray.total() );
	            // Find contours and store them in a list
	            Imgproc.findContours(gray, contours, new Mat(), 1, 2);
	    		Log.v(TAG, "Contours Found!");
	            
	            MatOfPoint2f approx = new MatOfPoint2f();
	            MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
	            MatOfPoint mMOP = new MatOfPoint();
	            for( int i = 0; i < contours.size(); i++ )
	            {
	            	contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
	            	Imgproc.approxPolyDP(mMOP2f1, approx, Imgproc.arcLength(mMOP2f1, true)*0.02, true);
	            	approx.convertTo(mMOP, CvType.CV_32S);
	        
	                if( approx.rows()==4 && Math.abs(Imgproc.contourArea(approx)) > 1000 && Imgproc.isContourConvex(mMOP))
	                {
	                	
	                	Log.v(TAG,"Passes Conditions! " + approx.size().toString());
	                	double maxcosine = 0;
	                	Point[] list = approx.toArray();
	                	
	                	 for (int j = 2; j < 5; j++)
	                	 {
	                		 double cosine =Math.abs(angle(list[j%4], list[j-2], list[j-1]));
	                		 maxcosine = Math.max(maxcosine, cosine);
                         }
	                	 
	                	 if( maxcosine < 0.3 ) {
	                		 MatOfPoint temp = new MatOfPoint();
	                		 approx.convertTo(temp, CvType.CV_32S);
	                         squares.add(temp);
	                     }
	                }

	            }
	    		Log.v(TAG, "Squares Added to List! : " + squares.size());
	        }
	    }
	    
	    double maxarea = 0;
		double secmaxarea = 0;
		int maxareaidx = 0;
		for(int idx =0; idx< squares.size();++idx)
		{
			Mat contour = squares.get(idx);
			double area = Math.abs(Imgproc.contourArea(contour));
			if(area > maxarea)
			{
				maxarea = area;
				maxareaidx = idx;
			}
		}
		
		for(int idx =0; idx< squares.size();++idx)
		{
			Mat contour = squares.get(idx);
			double area = Imgproc.contourArea(contour);
			if(area > secmaxarea && area < maxarea)
			{
				secmaxarea = area;
			}
		}
		
		Log.v(TAG, "Max Area calculated!" + maxarea);
		Log.v(TAG, "Biggest Contour Index" + maxareaidx);
		Log.v(TAG, "Second Biggest Area " + secmaxarea);

		if(!(squares.isEmpty()))
		{
	    Mat mask = Mat.zeros(dst.size(), dst.type());
		org.opencv.core.Scalar s = new Scalar(255,255,255);
		Imgproc.drawContours(mask, squares, maxareaidx, s, -1);
		Log.v(TAG, "All Contours drawn!");
		
		Point[] p = squares.get(maxareaidx).toArray();
		Log.v(TAG, "Contours of Max Rectangle " + p.length);
		Log.v(TAG, "Points of Contour : 1) " + p[0].x + " " + p[0].y);
		Log.v(TAG, "Points of Contour : 2) " + p[1].x + " " + p[1].y);
		Log.v(TAG, "Points of Contour : 3) " + p[2].x + " " + p[2].y);
		Log.v(TAG, "Points of Contour : 4) " + p[3].x + " " + p[3].y);
		
		Mat canvas = new Mat(src.size(),src.type());
		Scalar s2 = new Scalar(0,0,0);
		canvas.setTo(s2);
		src.copyTo(canvas, mask);
		
		org.opencv.core.Scalar s1 = new Scalar(255);
		Imgproc.drawContours(canvas, squares, maxareaidx, s1, 1);
		
		Utils.matToBitmap(canvas, destImage);
		Log.v(TAG, "Mat to Bitmap Successful");
		Log.v(TAG, getBitmapUri(destImage).toString());
		return getBitmapUri(destImage);
		}
		else
		{
			Utils.matToBitmap(src, destImage);
			Log.v(TAG,"No edges found, showing original image!");
			Log.v(TAG, getBitmapUri(destImage).toString());
			return getBitmapUri(destImage);		
		}
	}
}