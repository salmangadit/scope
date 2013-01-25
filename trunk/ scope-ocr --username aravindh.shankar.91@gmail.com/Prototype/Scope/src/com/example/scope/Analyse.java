package com.example.scope;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Analyse {

	private static final String TAG = "Scope.java";
	Uri inputImageUri;
	Context currContext;
	private int BINS = 16;
	List<Uri> segmentedResults;
	
	public Analyse(Context c, Uri inputUri) {
		currContext = c;
		inputImageUri = inputUri;
	}
	
	public Analyse(Context c, List<Uri> image) {
		currContext = c;
		segmentedResults = image;
	}

	//To analyse whether maximum pixels in image are light/dark
	public boolean is_light() {
		
		Bitmap sourceImage = null;
		
		Boolean accumulate = false;

		Mat histogram = new Mat();
		Mat sourceImageMat = new Mat();
		
		Mat mask = new Mat();
		
		MatOfInt histsize = new MatOfInt(BINS);
		MatOfInt channels = new MatOfInt(0);
		MatOfFloat ranges = new MatOfFloat(0f, 256f);
		List<Mat> image = new LinkedList<Mat>();
		
		
		Greyscale gr = new Greyscale(currContext, inputImageUri);
		Uri ppimage=gr.greyscale();
		
		
		try {
			sourceImage = MediaStore.Images.Media.getBitmap(
					currContext.getContentResolver(), ppimage);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "NULL");
			e.printStackTrace();
		}
		
		Utils.bitmapToMat(sourceImage, sourceImageMat);
		image.add(sourceImageMat);
		byte data[] = new byte[(int) (sourceImageMat.total()*sourceImageMat.channels())];
		sourceImageMat.get(0, 0, data);
		Imgproc.calcHist(image, channels, mask , histogram, histsize, ranges, accumulate);
		Log.v(TAG, "Histogram: " + histogram.dump()); 
		
		double low = 0,high = 0;
		for(int i=0;i<16;i++)
		{
			if(i<13)
				low+=histogram.get(i,0)[0];
			else
				high+=histogram.get(i,0)[0];
		}
		Log.v(TAG," " + high + " "+ low);
		
		if(high>low)
			return true;
		return false;
	}
	
	//Analyses each background segmented and sees if it requires reversed Adaptive thresholding
	public List<Uri> adaptiveSplitter()
	{
		for(int i=0;i<segmentedResults.size();i++)
		{
			inputImageUri = segmentedResults.get(i);
			
			if(is_light()==false)
			{
				Adptrev reverse = new Adptrev(currContext,inputImageUri,"bg"+i+".bmp");
				reverse.thresh_inv();
			}	
			else
			{
				Adpt adaptive = new Adpt(currContext,inputImageUri,"bg"+i+".bmp");
				adaptive.thresh();
			}
		}
		return segmentedResults;
	}
	
	
}
