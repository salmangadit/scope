package com.example.scope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

@TargetApi(12)
public class Adptrev {
	
	double alpha;
	Uri inputImageUri;
	Context currContext;
	Mat sourceImageMat = new Mat();
	Bitmap sourceImage = null;
	Bitmap destImage = null;
	double MID = 128;
	String file_name = "temp.bmp";
	Uri uri;

	private static final String TAG = "Scope.java";

	// Constructor
	public Adptrev(Context c, Uri inputUri) {
		currContext = c;
		inputImageUri = inputUri;
	}

	public Adptrev(Context c, Uri inputUri, String store) {
		currContext = c;
		inputImageUri = inputUri;
		file_name = store;
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
	}
	
	
    //Applying Gaussian mean adaptive threshold after reversing pixel values, for dark text on light bg
    public Uri thresh_inv() {
            Log.v(TAG,"Threshold invert running");
            initiate();

            Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());

            destImage = sourceImage;

            Utils.bitmapToMat(sourceImage, sourceImageMat);
            Mat destImageMat = Mat
                            .zeros(sourceImageMat.size(), sourceImageMat.type());
            Mat final_dest_mat = Mat
                            .zeros(sourceImageMat.size(), sourceImageMat.type());
            Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
            
            //Reversing pixel intensities about the mean 128
            for(int i =0;i <destImageMat.rows();i++)
                    for(int j=0; j <destImageMat.cols();j++)
                    {       
                            
                       double diff = MID - destImageMat.get(i, j)[0];
                       if (diff>0)
                               destImageMat.put(i, j,255);
                       else
                               destImageMat.put(i, j,0);
                    }
            
            Utils.matToBitmap(destImageMat, destImage);
            String temp = file_name;
            file_name = "tempor.bmp";
            store();
            file_name = temp;
            Imgproc.adaptiveThreshold(destImageMat, final_dest_mat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 7, 5);
    
            Utils.matToBitmap(final_dest_mat, destImage);

            Log.v(TAG, "destImage Size: " + destImage.getByteCount());

            store();
            Log.v(TAG, uri.toString());
            return uri;
    }
    

//	//Applying Gaussian mean adaptive threshold after reversing pixel values, for dark text on light bg
//	public Uri thresh_inv() {
//		Log.v(TAG,"Threshold invert running");
//		initiate();
//
//		Log.v(TAG, "sourceImage Size: " + sourceImage.getByteCount());
//
//		destImage = sourceImage;
//
//		Utils.bitmapToMat(sourceImage, sourceImageMat);
//		Mat destImageMat = Mat
//				.zeros(sourceImageMat.size(), sourceImageMat.type());
//		Mat final_dest_mat = Mat
//				.zeros(sourceImageMat.size(), sourceImageMat.type());
//		Imgproc.cvtColor(sourceImageMat, destImageMat, Imgproc.COLOR_RGB2GRAY);
//		int BINS = 64;
//		MatOfInt histsize = new MatOfInt(BINS);
//		MatOfInt channels = new MatOfInt(0);
//		MatOfFloat ranges = new MatOfFloat(0f, 256f);
//		List<Mat> image = new LinkedList<Mat>();
//		Mat mask = new Mat();
//		Mat histogram = new Mat();
//		Boolean accumulate = false;
//		
//		image.add(destImageMat);
//		Imgproc.calcHist(image, channels, mask , histogram, histsize, ranges, accumulate);
//		
//		double[] temp_array = new double[64];
//		int[] id = new int[64];
//		for(int i=0;i<64;i++)
//		{   
//			temp_array[i] = (double)histogram.get(i, 0)[0];
//			id[i] = i;
//		}
//		Log.v(TAG, "Array1 " +temp_array[2]);
//		double t=0;
//		int k=0;
//		for(int i=0;i<64;i++)
//			for(int j =1; j< (64-i); j++)
//			{
//				if(temp_array[j-1] > temp_array[j]){
//					  t = temp_array[j-1];
//					  temp_array[j-1]=temp_array[j];
//					  temp_array[j]=t;
//					  
//					  k= id[j-1];
//					  id[j-1]=id[j];
//					  id[j] = k;
//					  }	
//			}
//		
//		for(int i=63; i>53 ;i--)
//		{
//			if(id[i]<32)
//				MID = (id[i]*4 +14);
//			
//		}
//		
//		Log.v(TAG, "MID "+MID);
//		//Reversing pixel intensities about the mean 128
//		for(int i =0;i <destImageMat.rows();i++)
//			for(int j=0; j <destImageMat.cols();j++)
//			{	
//				
//			   double diff = MID - destImageMat.get(i, j)[0];
//			   if (diff>0)
//				   destImageMat.put(i, j, 255);
//			   else
//				   destImageMat.put(i, j, 0);
//			}
//		
//		
//		Utils.matToBitmap(destImageMat, destImage);
//		String temp = file_name;
//		file_name = "tempor.bmp";
//		store();
//		file_name = temp;
//		Imgproc.adaptiveThreshold(destImageMat, final_dest_mat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 9, 9);
//	
//		Utils.matToBitmap(final_dest_mat, destImage);
//
//		Log.v(TAG, "destImage Size: " + destImage.getByteCount());
//
//		store();
//		Log.v(TAG, uri.toString());
//		return uri;
//	}

	// Temporary storage location
	public void store() {
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						file_name);

		try {
			FileOutputStream out = new FileOutputStream(file);
			destImage.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			Log.v(TAG, "null2");
			e.printStackTrace();
		}

		uri = Uri.fromFile(file);
	}
}
