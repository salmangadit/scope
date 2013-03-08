package com.example.hiii;


import org.opencv.android.OpenCVLoader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

@TargetApi(12)
public class MainActivity extends Activity {
	


	private static final String TAG = "Scope.java";
	static {
		if (!OpenCVLoader.initDebug()) {
			Log.e(TAG, "Some Error!");
		}
	}
	public double alpha = 2.0;
	public double beta = 0;
	public Bitmap myimage;
	public Bitmap ppimage;
	public Uri image_uri;
	public String filepath;
	private Uri selectedImage;
	protected static Button _button;
	private static int RESULT_LOAD_IMAGE = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "hellooo...");
		Log.v(TAG, "Preprocess...");

		setContentView(R.layout.activity_main);
		
		
		_button = (Button) findViewById(R.id.upload);
		_button.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v)
			{
			Log.v(TAG, "Gallery button pressed");
			Log.v(TAG, "1.. Pass");
			// Activity to open Gallery
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			Log.v(TAG, "2.5.. Pass");
			startActivityForResult(i, RESULT_LOAD_IMAGE);
			Log.v(TAG, "3.. Pass");
			
			}
		});
	}	
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			Log.v(TAG, "4.. Pass");
			super.onActivityResult(requestCode, resultCode, data);
			Log.v(TAG, "5.. Pass");
			if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
					&& null != data) {
				selectedImage = data.getData();
				Log.v(TAG, "Selected Image : " + selectedImage.toString());
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = this.getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				Log.v(TAG, "7.5.. Pass");
				cursor.moveToFirst();
				Log.v(TAG, "8.. Pass");

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				Log.v(TAG, "9.. Pass");
				Log.v(TAG, filePath);
				EdgeDetect(filePath);
			}
		}

		

	private void EdgeDetect(String filepath)
	{	
		Log.v(TAG, "Entering edge detection!");
		EdgeDetection detector1 = new EdgeDetection(this.getApplicationContext(), selectedImage, filepath);
		Uri ppimage = detector1.AutoRotation();
		Log.v(TAG,ppimage.toString());
		
		ImageView imageView = (ImageView) findViewById(R.id.imgView);
		imageView.setImageURI(selectedImage);
		ImageView imageView2 = (ImageView) findViewById(R.id.imgView2);
		imageView2.setImageURI(ppimage);
	}
}