package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class EdgeDetection {

	private static final String TAG = "Scope.java";
	static {
		if (!OpenCVLoader.initDebug()) {
			Log.e(TAG, "Some Error!");
		}
	}
	Uri inputImageUri;
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
	String filePath;
	int ratio = 3;
	int threshold_low = 100;
	int kernel_size = 3;

	public EdgeDetection(Context c, Uri inputuri, String filepath) {
		filePath = filepath;
		currContext = c;
		inputImageUri = inputuri;

		FixImageProperties();
	}

	// Read bitmap
	public Bitmap readBitmap(Uri selectedImage) {
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		AssetFileDescriptor fileDescriptor = null;
		try {
			fileDescriptor = currContext.getContentResolver()
					.openAssetFileDescriptor(selectedImage, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bm = BitmapFactory.decodeFileDescriptor(
						fileDescriptor.getFileDescriptor(), null, options);
				fileDescriptor.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}

	private void FixImageProperties() {
		// try {
		// sourceImage = MediaStore.Images.Media.getBitmap(
		// currContext.getContentResolver(), inputImageUri);
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// Log.v(TAG, "NULL");
		// e.printStackTrace();
		// }
		//
		BitmapHandler bitmaphandler = new BitmapHandler(currContext);
		sourceImage = bitmaphandler.decodeFileAsPath(filePath);
		//sourceImage = readBitmap(inputImageUri);

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
	
	public MatOfPoint EdgeDetect(Mat source) {
		int counter = 0;
		Mat test = new Mat();
		Mat blurred = new Mat();
		source.copyTo(blurred);
		Log.v(TAG, "Blurred Matrix! : " + blurred.total() );
		
		Imgproc.medianBlur(source, blurred, 9);
		Log.v(TAG, "Median Blur Done!");

		Mat gray0 = new Mat(blurred.size(), blurred.type());
		Imgproc.cvtColor(gray0, gray0, Imgproc.COLOR_RGB2GRAY);
		Mat gray = new Mat();

		Log.v(TAG, "Gray0 Matrix! : " + gray0.total() );
		Log.v(TAG, "Gray Matrix! : " + gray.total() );
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> squares = new ArrayList<MatOfPoint>();
		
		// find squares in every color plane of the image
	    for (int c = 0; c < 4; c++)
	    {
	    	Log.v(TAG,"Color Space Entering Iteration: " + c);
	    	if(c==3){
	    		Mat destImageMat = new Mat();
	    		Imgproc.cvtColor(source, destImageMat, Imgproc.COLOR_RGB2GRAY);
	    		destImageMat.copyTo(gray0);
	    	}
	    	
	    	else{
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
	    	}

	     // try several threshold levels
	        int threshold_level = 2;
	        for (int l = 0; l < threshold_level; l++)
	        {
	            // Use Canny instead of zero threshold level!
	            // Canny helps to catch squares with gradient shading
	        	Log.v(TAG,"Threshold Level: " + l);
	        	
	            if (l == 0)
	            {
	                Imgproc.Canny(gray0, gray, 20, 30); // 

	                // Dilate helps to remove potential holes between edge segments
	                Imgproc.dilate(gray, gray, Mat.ones(new Size(3,3),0));
	                
	            }
	            else
	            {
	            	Mat final_dest_mat = Mat.zeros(source.size(), source.type());
	        		Imgproc.adaptiveThreshold(gray0, final_dest_mat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 5);
	        		
	        		if(c == 0){
	    	    		int matrix_size = 2;
	    	    		Size size = new Size(matrix_size,matrix_size);
	    	    		
	    	    		Mat temp_erode_1 = Mat.zeros(final_dest_mat.size(), final_dest_mat.type());
	    				Imgproc.erode( final_dest_mat, temp_erode_1, Mat.ones(size,0));
	    				Mat temp_erode_2 = Mat.zeros(temp_erode_1.size(), temp_erode_1.type());
	    				Imgproc.erode( temp_erode_1, temp_erode_2, Mat.ones(size,0));
	    				
	    				temp_erode_2.copyTo(gray);
	        		}
	        		else{
	        			final_dest_mat.copyTo(gray);
	        		}
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
	                	int rectangle_flag = 0;
	                	double temp_cosine = 0;
	                	 for (int j = 2; j < 5; j++)
	                	 {	 
	                		 double cosine =Math.abs(angle(list[j%4], list[j-2], list[j-1]));
	                		 if(j == 2){
	                			 temp_cosine = cosine;
	                		 }
	                		 else{
	                			 if((cosine > (temp_cosine + 0.05))||(cosine < (temp_cosine - 0.05))){
	                				 rectangle_flag = 1;
	                			 }
	                		 }
	                		 Log.v(TAG,"Cosine of side " + j + " = " + cosine);
	                		 maxcosine = Math.max(maxcosine, cosine);
                         }
	                	 
	                	 if( maxcosine < 0.2 ) {
	                		 MatOfPoint temp = new MatOfPoint();
	                		 approx.convertTo(temp, CvType.CV_32S);
	                		 if((Imgproc.contourArea(approx)/src.total())<0.85){
	                			 if(rectangle_flag == 0){
		                			 squares.add(temp);
		             	    		Log.v(TAG, "Squares Added to List! : " + squares.size());
		                		 }
	                		 }
	                     }
	                }
	            }
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
			return squares.get(maxareaidx);
		}
		else
		{
			return null;
		}
}	
//	    Mat mask = Mat.zeros(dst.size(), dst.type());
//		
//		Imgproc.drawContours(mask, squares, maxareaidx, s, -1);
//		Log.v(TAG, "All Contours drawn!");
//		
//		Point[] p = squares.get(maxareaidx).toArray();
//		Log.v(TAG,"Size: " + squares.get(maxareaidx).size());
//		Log.v(TAG, "Contours of Max Rectangle " + p.length);
//		Log.v(TAG, "Points of Contour : 1) " + p[0].x + " " + p[0].y);
//		Log.v(TAG, "Points of Contour : 2) " + p[1].x + " " + p[1].y);
//		Log.v(TAG, "Points of Contour : 3) " + p[2].x + " " + p[2].y);
//		Log.v(TAG, "Points of Contour : 4) " + p[3].x + " " + p[3].y);
//		Log.v(TAG,"No. of points" + p.length);
//		
//		Mat canvas = new Mat(src.size(),src.type());
//		Scalar s2 = new Scalar(255,255,255);
//		canvas.setTo(s2);
//		src.copyTo(canvas, mask);
		
	public Uri AutoRotation()
	{	
		MatOfPoint edges = EdgeDetect(src);
		int is_negative = 0;
		if(edges!=null)
		{
			MatOfPoint2f forrotatedrect = new MatOfPoint2f();
			edges.convertTo(forrotatedrect, CvType.CV_32FC2);
			RotatedRect rotated = Imgproc.minAreaRect(forrotatedrect);
			
			Log.v(TAG,"Rotated Rect Received" + rotated.angle);
			Log.v(TAG,"Rotated Rect: " + rotated.center.x + rotated.center.y);
			double angle = rotated.angle;
			Size size = rotated.size;
			
			Point[] corners = new Point[4];
			rotated.points(corners);
			Log.v(TAG,"Corner Top Left: "+ corners[2].x + " " + corners[2].y);
			for(int j = 0; j<4; ++j){
				if(corners[j].x < 0 || corners[j].y < 0){
					is_negative = 1;
				}
			}
			
			if(is_negative == 0){
				if(angle<-45.0)
				{
					angle = angle + 90.0;
					double temp = size.width;
					size.width = size.height;
					size.height = temp;
				}	
				
				if(size.width < size.height){
					if(corners[1].y < corners[3].y){
						angle = angle + 90;
					}
					else{
						angle = angle - 90;
					}
					double temp = size.width;
					size.width = size.height;
					size.height = temp;
				}
				
				Log.v(TAG,"Final Angle Desired: " + angle);
				
				Mat transform = Imgproc.getRotationMatrix2D(rotated.center, angle, 1.0);
				Mat rotatedMat = new Mat(src.size(),src.type());
				
				Log.v(TAG,"Size" + size.width + size.height);
				
				Imgproc.warpAffine(src, rotatedMat, transform, src.size(),Imgproc.INTER_CUBIC);
				
				Log.v(TAG,"RotatedMat found " + rotatedMat.total());
				
				rotatedMat.convertTo(rotatedMat, 0);
				
				
				Log.v(TAG,"Size of patch: " + size.height + " " + size.width);
				Log.v(TAG,"Center of patch: "+ rotated.center.x + " " + rotated.center.y);
			
				
				double ht = size.height;
				double wdth = size.width;
				Point centre_point = rotated.center;
				Point[] p = new Point[4];
				p[0] = new Point(centre_point.x - (wdth/2), centre_point.y + (ht/2));
				p[1] = new Point(centre_point.x - (wdth/2), centre_point.y - (ht/2));
				p[2] = new Point(centre_point.x + (wdth/2), centre_point.y - (ht/2));
				p[3] = new Point(centre_point.x + (wdth/2), centre_point.y + (ht/2));
				
				if(rotatedMat != null){
					
					Rect roi = new Rect((int)p[1].x,(int)p[1].y,(int)wdth,(int) ht);
					Mat cropped = new Mat(rotatedMat,roi);
					
					Bitmap croppedImg = Bitmap.createBitmap(cropped.cols(), cropped.rows(),  Bitmap.Config.ARGB_8888);               
					Utils.matToBitmap(cropped, croppedImg);
					
					//Utils.matToBitmap(canvas, destImage);
					Log.v(TAG, "Mat to Bitmap Successful");
					Log.v(TAG, getBitmapUri(croppedImg).toString());
					return getBitmapUri(croppedImg);
					}
				}
		}
		

		Utils.matToBitmap(src, destImage);
		if(edges == null){
			Log.v(TAG,"No edges found, showing original image!");
		}
		if(is_negative == 1){
			Log.v(TAG,"Negative corners found, showing original image");
		}

		Log.v(TAG, getBitmapUri(destImage).toString());
		return getBitmapUri(destImage);		
		
	}
}