package com.example.hiii;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class BitmapHandler {
	private static int IMAGE_MAX_SIZE = 540;
	private static String TAG = "Scope.java";
	
	@TargetApi(13)
	public BitmapHandler(Context ctx){
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		Log.v(TAG, "Screen width: " + width + " height: " + height);
		IMAGE_MAX_SIZE = Math.min(width, height)*4;
	}

	public Bitmap decodeFileAsPath(String uri) {
		// Create a file out of the uri
		File f = null;

		f = new File(uri);

		return decodeFile(f);
	}

	private Bitmap decodeFile(File f) {
		Bitmap b = null;
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			Log.v(TAG,"Test2");
			FileInputStream fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();

			int scale = 1;
			Log.v(TAG, "Decode Image height: " + o.outHeight + " and width: " + o.outWidth);
			
			if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
				scale = (int) Math.pow(
						2,
						(int) Math.round(Math.log(IMAGE_MAX_SIZE
								/ (double) Math.max(o.outHeight, o.outWidth))
								/ Math.log(0.5)));
			}
			Log.v(TAG, "Final scale: " + scale);
			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o2);
			fis.close();
		} catch (IOException e) {
			Log.v(TAG, e.getMessage());
		}
		return b;
	}
}
