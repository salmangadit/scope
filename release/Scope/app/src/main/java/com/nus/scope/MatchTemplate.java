package com.nus.scope;

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
	Bitmap source;
	Context currContext;
	
	private static final String TAG = "Scope";
	
	public MatchTemplate(Uri source_uri, Context c)
	{
		this.inputimage_uri=source_uri;
		this.currContext=c;
	}

	public Boolean TemplateMatch() 
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
		Bitmap template = BitmapFactory.decodeResource(currContext.getResources(), R.drawable.nuslogo);
		
		Log.i(TAG, "Changed to matrix from image!");
		
		//     **  Whole bunch of initialisation shizz  **
		//     These ints determine the size of the Mats according to the input image n template
		int s_rows = source.getHeight();    Log.i(TAG, "source rows: "+s_rows);
		int s_cols = source.getWidth();     Log.i(TAG, "source cols: "+s_cols);
		int t_rows = template.getHeight();  Log.i(TAG, "template rows: "+t_rows);
		int t_cols = template.getWidth();   Log.i(TAG, "template cols: "+t_cols);
		
		//    Create all necessary Mats
		Mat sourceMat = new Mat(s_rows,s_cols,CvType.CV_32F); //NUS card image matrix in 32F depth
		Mat src_copy = new Mat (s_rows,s_cols, CvType.CV_32F);
		Mat logo = new Mat(t_rows, t_cols, CvType.CV_32F);      //NUS logo matrix
		Mat grayMat = new Mat(s_rows,s_cols, CvType.CV_32F);   //Grayscale of sourceMat		
		Mat logoMat = new Mat(t_rows, t_cols, CvType.CV_32F);   //NUS logo matrix after grayscale
		Log.i(TAG, "New Mats loaded");
		
	    //   Create results matrix
		int result_cols = src_copy.cols() - logoMat.cols() + 1;
		int result_rows = src_copy.rows() - logoMat.rows() + 1;		
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32F); 
		Log.i(TAG, "Results matrix created");
		Log.i(TAG, "Result_cols : "+result_cols);
		Log.i(TAG, "Result_rows : "+result_rows);
	
		//     Convert the bitmaps to Mats function
		BitmapToMat(source, template, sourceMat, logo);
		
		//    Create copy of the source image
		sourceMat.copyTo(src_copy);
		
		//     Grayscale function
		Grayscale(src_copy, grayMat, logo, logoMat);
		
		//     Match Template function
		Boolean loop = runTM(Imgproc.TM_CCOEFF_NORMED, grayMat, logoMat, result);
		Log.i(TAG, "Final confirmation of template Matching.." +loop);
		
		return loop;
}
		
		public void BitmapToMat(Bitmap source, Bitmap template, Mat sourceMat, Mat logo)
		{
			//  Convert bitmap image to Mat format 			
			Utils.bitmapToMat(source, sourceMat);
			Utils.bitmapToMat(template, logo);
			Log.i(TAG, "Convert bitmap to Mat");
			
		}
		
		public void Grayscale(Mat src_copy, Mat grayMat, Mat logo, Mat logoMat)
		{
			//	     Grayscale function
			Log.i(TAG, "Starting Grayscale");
			Imgproc.cvtColor(src_copy, grayMat, Imgproc.COLOR_RGB2GRAY); //Convert testMat to grayscaled grayMat
			Imgproc.cvtColor(logo, logoMat, Imgproc.COLOR_RGB2GRAY);	//Convert logo to grayscaled logoMat
			Log.i(TAG, "Grayscale DONE");
		}
		
		public Boolean runTM(int process, Mat grayMat1, Mat logoMat1, Mat result1) 
		{
			Mat grayMat = grayMat1;   //Grayscale of sourceMat		
			Mat logoMat = logoMat1;   //NUS logo matrix after grayscale
			Mat result = result1;
			
			//	    Run Template Matching function
			Log.i(TAG, "Starting template match");
			Imgproc.matchTemplate(grayMat, logoMat, result, process);
			Log.i(TAG, "Template match DONE");
			Core.normalize(result, result, 0, 1,Core.NORM_MINMAX, -1, new Mat());
			
		
			//    Get maxima minima
			Core.MinMaxLocResult locres = Core.minMaxLoc(result);
			Log.i(TAG, "Minima maxima FOUND");
			
			Point matchLoc; // This point is the origin point of the template rectangle identified on the image
			if (process > 2)
			{
			matchLoc = locres.maxLoc;  //This depends on the type of template match carried out
			Log.i(TAG, "MaxLoc FOUND");	
			}
			else {
						matchLoc = locres.minLoc;
				}
		
			Point rect_end = new Point (matchLoc.x + logoMat.cols(), matchLoc.y + logoMat.rows());
			Log.i(TAG, "RECT origin points");
			Log.v(TAG, "x is " + matchLoc.x);
			Log.v(TAG, "y is " + matchLoc.y);
			
			Log.i(TAG, "RECT end points");
			Log.v(TAG, "x is " + rect_end.x);
			Log.v(TAG, "y is " + rect_end.y);
			
			
			//  Create variable to determine a match or not. 
			//  This is a bool value where TRUE=match &  FALSE=not a match
			Boolean loop;
				
			//   This function checks whether the coordinates of the template and the identified area match.
			//   If so, a String output is shown on the screen
			if(Math.abs(rect_end.x - matchLoc.x) == 456 && Math.abs(rect_end.y - matchLoc.y) == 235)
			{
				loop=true;
				Log.i(TAG," Templates match! This is an NUS card! " +loop);
			}
			else
				{
					loop=false;
					Log.i(TAG,"It's not quite a match." +loop);
				}

		
			return loop;
			
		}
		

}
