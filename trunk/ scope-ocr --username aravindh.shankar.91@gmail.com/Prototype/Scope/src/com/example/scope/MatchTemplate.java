package com.example.scope;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;


/* Class: MatchTemplate
 * Author: Arnab
 * 
 * Description:
 * A general image processing operator is a function that takes one or more input images 
 * and produces an output image.
 * In this image processing transform, an RGB input bitmap image is compared to a template image 
 * to check whether the input image contains the template image in its layout.
 * 
 * Methods return a boolean variable where TRUE=template exists in image
 * 										FALSE=template does not exist in image
 * 
 * Sample usage code:
 * Imgproc.matchTemplate(grayMat, logoMat, result, Imgproc.TM_CCOEFF_NORMED);
 * 
 * if((Math.abs(origin.x - matchLoc.x)<= 300) && (Math.abs(origin.y - matchLoc.y)<= 300))
			{			confirm=true; }
 */


@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class MatchTemplate {
	Uri inputimage_uri;
	Bitmap source = null;
	Context currContext;
	
	private static final String TAG = "Scope.java";
	static
	{
		if(!OpenCVLoader.initDebug())
		{
			Log.e(TAG, "Some Error! OpenCV not loaded dude!");
		}
	}
	
	
	public MatchTemplate(Uri source_uri, Context c)
	{
		this.inputimage_uri=source_uri;
		this.currContext=c;
	}
	
	
	public boolean TM() 
	{
		try {
			source = MediaStore.Images.Media.getBitmap(
					currContext.getContentResolver(), inputimage_uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "NULL");
			e.printStackTrace();
		}

		Log.v(TAG, "sourceImage Size: " + source.getByteCount());
			
		Log.i(TAG, "Image loaded from res folder");
			
		//   Initialize the input image source
		Bitmap template = BitmapFactory.decodeResource(currContext.getResources(), R.drawable.smallnuslogo);
		
		Log.i(TAG, "Changed to matrix from image!");
		
		int s_rows = source.getHeight(); 
		int s_cols = source.getWidth();
		int t_rows = template.getHeight();
		int t_cols = template.getWidth();
		
		
		Mat sourceMat = new Mat(s_rows,s_cols,CvType.CV_32F); //NUS card image matrix in 32F depth
		Mat src_copy = new Mat (s_rows,s_cols, CvType.CV_32F);
		
		Mat logo = new Mat(t_rows, t_cols, CvType.CV_32F);      //NUS logo image matrix
		Mat grayMat = new Mat();   //Grayscale of sourceMat		
		Mat logoMat = new Mat(t_rows, t_cols, CvType.CV_32F);   //NUS logo matrix after grayscale
		Log.i(TAG, "New Mats loaded");
		
		//  Convert bitmap image to Mat format 			
		Utils.bitmapToMat(source, sourceMat);
		Utils.bitmapToMat(template, logo);
		Log.i(TAG, "Convert bitmap to Mat");
		
		//    Create copy of the source image
		sourceMat.copyTo(src_copy);
		
		//     Grayscale function
		Log.i(TAG, "Starting Grayscale");
		Imgproc.cvtColor(src_copy, grayMat, Imgproc.COLOR_RGB2GRAY); //Convert testMat to grayscaled grayMat
		Imgproc.cvtColor(logo, logoMat, Imgproc.COLOR_RGB2GRAY);	//Convert logo to grayscaled logoMat
		Log.i(TAG, "Grayscale DONE");
		
				
		//   Create results matrix
		int result_cols = src_copy.cols() - logoMat.cols() + 1;
		int result_rows = src_copy.rows() - logoMat.rows() + 1;		
		Log.i(TAG, "Result_cols : "+result_cols);
		Log.i(TAG, "Result_rows : "+result_rows);
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32F); 
		Log.i(TAG, "Results matrix created");
		
		
		Log.i(TAG, "Running method 1"); 
		Boolean loop1 = runTM(Imgproc.TM_CCOEFF, grayMat, logoMat, result); 
		Log.i(TAG, "Running method 2");
		Boolean loop2 = runTM(Imgproc.TM_CCOEFF_NORMED, grayMat, logoMat, result); 
		Log.i(TAG, "Running method 3");
		Boolean loop3 = runTM(Imgproc.TM_CCORR, grayMat, logoMat, result);  
		Log.i(TAG, "Running method 4");
		Boolean loop4 = runTM(Imgproc.TM_CCORR_NORMED, grayMat, logoMat, result); 
		Log.i(TAG, "Running method 5");
		Boolean loop5 = runTM(Imgproc.TM_SQDIFF, grayMat, logoMat, result); 
		Log.i(TAG, "Running method 6");
		Boolean loop6 = runTM(Imgproc.TM_SQDIFF_NORMED, grayMat, logoMat, result);
		
		//  Create variable to determine a match or not. 
		//   This is a bool value where TRUE=match &  FALSE=not a match
		Boolean confirm = loop1|loop2|loop3|loop4|loop5|loop6;  Log.i(TAG, "Final confirmation of template Matching.." +confirm);  
		return confirm;
		
	}
		
	public Boolean runTM(int process, Mat grayMat, Mat logoMat, Mat result) 
	{
	
		
		//	    Run Template Matching function
		Log.i(TAG, "Starting template match");
		Imgproc.matchTemplate(grayMat, logoMat, result, Imgproc.TM_CCOEFF);
		Log.i(TAG, "Template match DONE");
		Core.normalize(result, result, 0, 1,Core.NORM_MINMAX, -1, new Mat());
		
	
		//    Get maxima minima
		Core.MinMaxLocResult locres = Core.minMaxLoc(result);
		Log.i(TAG, "Minima maxima FOUND");
		
		Point matchLoc = locres.maxLoc;  //This depends on the type of template match carried out
										// This point is the origin point of the template rectangle identified on the image
		Log.i(TAG, "MaxLoc FOUND");	
		
		//   Draw rectangle around matched template  
		Log.i(TAG, "Drawing RECT |___|");
		
		Point rect_end = new Point (matchLoc.x + logoMat.cols(), matchLoc.y + logoMat.rows());
		Log.i(TAG, "RECT origin points");
		Log.v(TAG, "x is " + matchLoc.x);
		Log.v(TAG, "y is " + matchLoc.y);
		
		Log.i(TAG, "RECT end points");
		Log.v(TAG, "x is " + rect_end.x);
		Log.v(TAG, "y is " + rect_end.y);
		
		//      TEST DATA    //
		//
		Point origin = new Point(950,65);
		Point test_end = new Point(1550,325);
		
		//  Create variable to determine a match or not. 
		//  This is a bool value where TRUE=match &  FALSE=not a match
		Boolean loop;
			
		//   This function checks whether the coordinates of the template and the identified area match.
		//   If so, a String output is shown on the screen
		if((Math.abs(origin.x - matchLoc.x)<= 300) && (Math.abs(origin.y - matchLoc.y)<= 300) && (Math.abs(test_end.x - rect_end.x)<= 300) && (Math.abs(test_end.y - rect_end.y)<= 300))
			{
				loop=true;
				Log.i(TAG,"Templates match! This is an NUS card! "+loop);
			}
		else
			{
				loop=false;
				Log.i(TAG,"It's not quite a match."+loop);
			}
		
		return loop;
	}
}
