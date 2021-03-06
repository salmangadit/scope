/*
 * Class: Analyse
 * Author: Aravindh
 * 
 * 1. Pixel analyser, to check if light background or dark background images
 * 2. Pixel cleaner to remove stray black pixels
 * 3. Pixel filler to fill up jagged letters.
 * 
 */

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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class Analyse {

	private static final String TAG = "Scope.java";
	Uri inputImageUri;
	Context currContext;
	private int BINS = 64;
	Uri uri;
	List<Uri> segmentedResults;
	String file_name = "temple.bmp";
	
	public Analyse(Context c, Uri inputUri) {
		currContext = c;
		inputImageUri = inputUri;
	}
	
	public Analyse(Context c, List<Uri> image) {
		currContext = c;
		segmentedResults = image;
	}
	
	public Analyse(Context c, Uri inputUri, String store) {
		currContext = c;
		inputImageUri = inputUri;
		file_name = store;
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
//		byte data[] = new byte[(int) (sourceImageMat.total()*sourceImageMat.channels())];
//		sourceImageMat.get(0, 0, data);
		
		Imgproc.calcHist(image, channels, mask , histogram, histsize, ranges, accumulate);
		Log.v(TAG, "Histogram: " + histogram.dump()); 
		Log.v(TAG,"Analysing");
		Log.v(TAG, "Element 1: " + histogram.get(1, 0)[0]);
		
		double[] temp_array = new double[64];
		int[] id = new int[64];
		for(int i=0;i<64;i++)
		{   
			temp_array[i] = (double)histogram.get(i, 0)[0];
			id[i] = i;
		}
		Log.v(TAG, "Array1 " +temp_array[2]);
		double t=0;
		int k=0;
		for(int i=0;i<64;i++)
			for(int j =1; j< (64-i); j++)
			{
				if(temp_array[j-1] > temp_array[j]){
					  t = temp_array[j-1];
					  temp_array[j-1]=temp_array[j];
					  temp_array[j]=t;
					  
					  k= id[j-1];
					  id[j-1]=id[j];
					  id[j] = k;
					  }	
			}
		
		int high =0,low=0;
		for(int i=63; i>53 ;i--)
		{
			if(id[i]>32)
				high++;
			else
				low++;
		}
		
		Log.v(TAG," " + low + " "+ high);
		
		
//		
//		Log.v(TAG, "Array2 " +temp_array[2]);
//		
//		double low = 0,high = 0;
//		for(int i=0;i<64;i++)
//		{	
//			if(i<=33)
//				low+=histogram.get(i,0)[0];
//			else
//				high+=histogram.get(i,0)[0];
//			
//		}
//		Log.v(TAG,"middle" + (histogram.get(32,0)[0]) );
//		Log.v(TAG," " + low + " "+ high);
		
		if(high>=low)
			return true;
		return false;
	}
	class color
	{
		int R,G,B;
		public color( int red, int green,int blue)
		{
			R= red;
			G= green;
			B= blue;
		}
	}
	public Uri filler()
	{
		Bitmap sourceImage = null;
		
		try {
			sourceImage = MediaStore.Images.Media.getBitmap(
					currContext.getContentResolver(), inputImageUri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "NULL");
			e.printStackTrace();
		}
		Bitmap dest = Bitmap.createBitmap(sourceImage.getWidth(),
				sourceImage.getHeight(), Bitmap.Config.ARGB_8888);
		Log.v(TAG, "analyse filler " +sourceImage.getByteCount());
		int abc, red , green, blue,count1=0 ;
		color[][] map = new color[sourceImage.getWidth()][sourceImage.getHeight()];
		Log.v(TAG, "Disintegrating image");
		for(int i =0; i < sourceImage.getWidth() ;i++)
			for(int j=0; j < sourceImage.getHeight();j++)
			{
			//Log.v(TAG, "a : " + i + "," +j);	
			count1++;
			abc = sourceImage.getPixel(i, j); 
			red = Color.red(abc);
			green = Color.green(abc);
			blue = Color.blue(abc);
			map[i][j] = new color(red,green,blue);
			}
		int count = 0,start, end,flag, counter1=0,yay=0, counter2=0;
		
		Log.v(TAG,"Single dot pass");
		//************Single dot pass
				for(int i =0; i < sourceImage.getWidth() ;i++)
					for(int j=0; j < sourceImage.getHeight();j++)
				{
					if ( (i>=10)&&(i<= sourceImage.getWidth() -10) &&(j>=10)&&(j<= sourceImage.getHeight() -10))
					{
						if(map[i-1][j-1].R==255 && map[i][j-1].R==255 && map[i+1][j-1].R==255 &&   
						   map[i-1][j+1].R==255 && map[i][j+1].R==255 && map[i+1][j+1].R==255 && 
						   map[i-1][j].R==255 && map[i+1][j].R==255 && 
						   map[i][j].R==0)
						{	
							dest.setPixel(i, j, Color.rgb(255 , 255, 255));
						}
						else
							dest.setPixel(i, j,Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B) );
					}
					else
					{	
						dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
					}
				}
				
				Log.v(TAG,"Vertical pass");
		
		//************Vertical pass
		for(int i =0; i < sourceImage.getWidth() ;i++)
			for(int j=0; j < sourceImage.getHeight();j++)
		{
			if ( (i>=10)&&(i<= sourceImage.getWidth() -10) &&(j>=10)&&(j<= sourceImage.getHeight() -10))
			{
				if(map[i-1][j-1].R==255 && map[i][j-1].R==255 && map[i+1][j-1].R==255 && map[i+2][j-1].R==255 &&   
				   map[i-1][j+1].R==255 && map[i][j+1].R==255 && map[i+1][j+1].R==255 && map[i+2][j+1].R==255 && 
				   map[i-1][j].R==255 && map[i+2][j].R==255 )
				{	
					dest.setPixel(i, j, Color.rgb(255 , 255, 255));
					dest.setPixel(i+1, j, Color.rgb(255 , 255, 255));
					dest.setPixel(i, j+1, Color.rgb(255 , 255, 255));
					dest.setPixel(i+1, j+1, Color.rgb(255 , 255, 255));
					j+=2;
				}
				else
					dest.setPixel(i, j,dest.getPixel(i, j) );
			}
			else
			{	
				dest.setPixel(i, j, dest.getPixel(i, j));
			}
		}
		
		Log.v(TAG,"Horizontal pass");
		//************Horizontal pass
			for(int j=0; j < sourceImage.getHeight();j++)
				for(int i =0; i < sourceImage.getWidth() ;i++)
		{
			if ( (i>=10)&&(i<= sourceImage.getWidth() -10) &&(j>=10)&&(j<= sourceImage.getHeight() -10))
			{
				if(map[i-1][j-1].R==255 && map[i][j-1].R==255 && map[i+1][j-1].R==255 && map[i+2][j-1].R==255 &&   
				   map[i-1][j+1].R==255 && map[i][j+1].R==255 && map[i+1][j+1].R==255 && map[i+2][j+1].R==255 && 
				   map[i-1][j].R==255 && map[i+2][j].R==255 )
				{	
					dest.setPixel(i, j, Color.rgb(255 , 255, 255));
					dest.setPixel(i+1, j, Color.rgb(255 , 255, 255));
					dest.setPixel(i, j+1, Color.rgb(255 , 255, 255));
					dest.setPixel(i+1, j+1, Color.rgb(255 , 255, 255));
					i+=2;
				}
				else
					dest.setPixel(i, j,dest.getPixel(i, j) );
			}
			else
			{	
				dest.setPixel(i, j, dest.getPixel(i, j));
			}
		}
		
//		//************Vertical pass
//			for(int i =0; i < sourceImage.getWidth() ;i++)
//				for(int j=0; j < sourceImage.getHeight();j++)
//			{
//				if ( (i!=0)&&(i<= sourceImage.getWidth() -3) &&(j!=0)&&(j<= sourceImage.getHeight() -3))
//				{
//					if(map[i][j].R ==0 && map[i][j+1].R==255 && map[i+1][j+1].R==0 )
//					{
//						start = j;end=j;flag = 1;
//						while(map[i][j+1+flag].R==255 && map[i+1][j+1+flag].R==0)
//						{
//							flag++;
//							end = j+1+flag;
//						}
//						if(map[i][j+1+flag].R==0 && map[i+1][j+1+flag].R==0)
//						{	
//							for (int k =start; k<=end;k++)
//							{	
//								dest.setPixel(i, k, Color.rgb(0 , 0, 0));
//								counter1 =1;
//							}
//							if(counter1==1)
//								{j+=(end-start);flag=0;}
//						}
//						else
//							dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
//					}
//					else
//						dest.setPixel(i, j,Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B) );
//				}
//				else
//					dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
//			}
//			
////************************ Horizontal pass		
//				for(int j=0; j < sourceImage.getHeight();j++)
//					for(int i =0; i < sourceImage.getWidth() ;i++)
//			{
//				if ( (i!=0)&&(i<= sourceImage.getWidth() -3) &&(j!=0)&&(j<= sourceImage.getHeight() -3))
//				{
//					if(map[i][j].R ==0 && map[i+1][j].R==255 && map[i+1][j-1].R==0 )
//					{
//						start = i;end=i+1;flag = 1;
//						while(map[i+1+flag][j].R==255 && map[i+1+flag][j-1].R==0)
//						{
//							flag++;
//							end = i+1+flag;
//						}
//						if(map[i+1+flag][j].R==0 && map[i+1+flag][j-1].R==0 && ((end - start)<3))
//						{	
//							for (int k =start; k<=end;k++)
//							{	
//								dest.setPixel(k, j, Color.rgb(0, 0, 0));
//								counter2 =1;
//							}
//							if(counter2==1)
//								{i+=(end-start);flag=0;}
//						}
//						//else
//							//dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
//					}
//					//else
//						//dest.setPixel(i, j,Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B) );
//				}
//				//else
//					//dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
//			}
			
			
//****************		From the other side, filler		
//					for(int i =0; i < sourceImage.getWidth() ;i++)
//						for(int j=0; j < sourceImage.getHeight();j++)
//			{
//				if ( (i!=0)&&(i!= sourceImage.getWidth() -1) &&(j!=0)&&(j!= sourceImage.getHeight() -1))
//				{
//					if(map[i][j].R ==0 && map[i][j+1].R==255 && map[i-1][j+1].R==0 )
//					{
//						start = j;end=j;flag = 1;
//						while(map[i][j+1+flag].R==255 && map[i-1][j+1+flag].R==0)
//						{
//							flag++;
//							end = j+1+flag;
//						}
//						if(map[i][j+1+flag].R==0 && map[i-1][j+1+flag].R==0)
//						{	
//							for (int k =start; k<=end;k++)
//							{	
//								dest.setPixel(i, k, Color.rgb(0 , 255, 0));
//								counter2 =1;
//							}
//							if(counter2==1)
//								{j+=(end-start);flag=0;}
//						}
//						else
//							dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
//					}
//					else
//						dest.setPixel(i, j,Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B) );
//				}
//				else
//					dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
//			}
//		
			
		//**********************************/ Feeder Functions for implementation upon testing
		
		
		/// Pass 1 verical
//		for(int j=0; j < sourceImage.getHeight();j++)
//		for(int i =0; i < sourceImage.getWidth() ;i++)
//		{
//			if ( (i!=0)&&(i!= sourceImage.getWidth() -1) &&(j!=0)&&(j!= sourceImage.getHeight() -1) && (map[i][j].R == 255))
//				{		
//						if ( (map[i-1][j+1].R!= 0) && ((map[i+1][j].R == 0 && map[i][j-1].R == 0) || (map[i+1][j].R==0 && Color.red(dest.getPixel(i, j-1)) == 0)) )
//						{	
//							dest.setPixel(i, j, Color.rgb(0 , 255, 0));
//						}
//						else
//						{
//							dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
//						}
//				}
//				else
//				{
//					dest.setPixel(i, j, Color.rgb(map[i][j].R, map[i][j].G, map[i][j].B));
//				}			
//			
//		}
		
	/*	/// Pass 1 vertical_inv		
		for(int j=sourceImage.getHeight()-1; j > 0 ;j--)
		for(int i = sourceImage.getWidth()-1 ; i >0 ;i--)
	{	
			if ( (i!=0)&&(i!= sourceImage.getWidth() -1) && (map[i][j].R == 255))
			{		
					if ( ((map[i-1][j].R == 0 && map[i][j-1].R == 0) || (map[i-1][j].R==0 && Color.red(dest.getPixel(i, j-1)) == 0)) && (map[i+1][j-1].R!= 0) )
				{	
						dest.setPixel(i, j, Color.rgb(0, 0, 0));
					}
					else
					{
						dest.setPixel(i, j,dest.getPixel(i, j) );
					}
			}
			else
			{
				dest.setPixel(i, j, dest.getPixel(i, j));
			}		
	}
		
		//Pass 2.. vertical
		for(int j=0; j < sourceImage.getHeight();j++)
			for(int i =0; i < sourceImage.getWidth() ;i++)
			{
				if ( (i!=0)&&(i!= sourceImage.getWidth() -1) &&(j!=0)&&(j!= sourceImage.getHeight() -1) && (map[i][j].R == 255))
					{		
							if ( (map[i-1][j+1].R!= 0) && ((map[i+1][j].R == 0 && map[i][j-1].R == 0) || (map[i+1][j].R==0 && Color.red(dest.getPixel(i, j-1)) == 0)) )
							{	
								dest.setPixel(i, j, Color.rgb(0, 0, 0));
							}
							else
							{
								dest.setPixel(i, j,dest.getPixel(i, j) );
							}
					}
					else
					{
						dest.setPixel(i, j,dest.getPixel(i, j) );
					}			
				
			}
		
		//Pass 2..vertical_inv
		for(int j=sourceImage.getHeight()-1; j > 0 ;j--)
			for(int i = sourceImage.getWidth()-1 ; i >0 ;i--)
		{	
				if ( (i!=0)&&(i!= sourceImage.getWidth() -1) && (map[i][j].R == 255))
				{		
						if ( ((map[i-1][j].R == 0 && map[i][j-1].R == 0) || (map[i-1][j].R==0 && Color.red(dest.getPixel(i, j-1)) == 0)) && (map[i+1][j-1].R!= 0) )
					{	
							dest.setPixel(i, j, Color.rgb(0, 0, 0));
						}
						else
						{
							dest.setPixel(i, j,dest.getPixel(i, j) );
						}
				}
				else
				{
					dest.setPixel(i, j, dest.getPixel(i, j));
				}		
		}
			//Pass 3.. extra
			for(int i =0; i < sourceImage.getWidth() ;i++)
				for(int j=0; j < sourceImage.getHeight();j++)
			{
				if ( (i!=0)&&(i!= sourceImage.getWidth() -1) &&(j!=0)&&(j!= sourceImage.getHeight() -1) && (map[i][j].R == 0))
					{		
							if ( (map[i][j-1].R== 255) && (map[i][j+1].R== 255) && (map[i-1][j].R== 255)  )
							{	
								dest.setPixel(i, j, Color.rgb(0, 0, 0));
							}
							else
							{
								dest.setPixel(i, j,dest.getPixel(i, j) );
							}
					}
					else
					{
						dest.setPixel(i, j,dest.getPixel(i, j) );
					}			
				
			}*/
			//Pass 4 horizontal digger
//			for(int i =0; i < sourceImage.getWidth() ;i++)
//				for(int j=0; j < sourceImage.getHeight();j++)
//			{
//				if ( (i!=0)&&(i!= sourceImage.getWidth() -1) &&(j!=0)&&(j!= sourceImage.getHeight() -1) && (map[i][j].R == 0))
//					{		
//							if ( (map[i][j-1].R== 255) && (map[i+1][j].R== 255) && (map[i-1][j].R== 255)  )
//							{	
//								dest.setPixel(i, j, Color.rgb(255,255, 255));
//								j++;
//							}
////							else
////							{
////								dest.setPixel(i, j,dest.getPixel(i, j) );
////							}
//					}
////					else
////					{
////						dest.setPixel(i, j,dest.getPixel(i, j) );
////					}			
//				
//			}
		

		Log.v(TAG, "as "+count1 + " "+count);
		store(dest);
	return uri;
	
	}
	
	//Analyses each background segmented and sees if it requires reversed Adaptive thresholding
	public List<Uri> adaptiveSplitter()
	{
		for(int i=0;i<segmentedResults.size();i++)
		{
			inputImageUri = segmentedResults.get(i);
			
			Bitmap source = null;
			try {
				source = MediaStore.Images.Media.getBitmap(
						currContext.getContentResolver(), inputImageUri);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Log.v(TAG, "NULL");
				e.printStackTrace();
			}
			file_name = "analysing"+i+".bmp"; 
			store(source);
			
			if(is_light()==false)
			{
				Adptrev reverse = new Adptrev(currContext,inputImageUri,"bg"+i+".bmp");
				//reverse.thresh_inv();
				segmentedResults.set(i, reverse.thresh_inv());	
			}	
			else
			{
				Adpt adaptive = new Adpt(currContext,inputImageUri,"bg"+i+".bmp");
				//adaptive.thresh();
				segmentedResults.set(i, adaptive.thresh());				
			}
		}
		return segmentedResults;
	}
	
	public void store(Bitmap destImage) {
		Log.v(TAG, "size " +destImage.getByteCount());
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
