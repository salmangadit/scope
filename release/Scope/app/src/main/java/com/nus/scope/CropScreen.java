package com.nus.scope;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class CropScreen extends Activity {
	private static final String TAG = "Scope.java";
	public Bitmap myimage;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.v(TAG, "enter...");
		setContentView(R.layout.cropscreen);
		Log.v(TAG, "cropscreen...");
		ActionBar ab = getActionBar();
		ab.setTitle("Title");
		ab.setDisplayShowTitleEnabled(false);
		ab.setSubtitle("Subtitle");
		ab.setDisplayShowTitleEnabled(false);
		final String filepath = getIntent().getStringExtra("file_path");
		String b = getIntent().getStringExtra("image_uri");
		final Uri image_uri = Uri.parse(b);
		Log.v(TAG, image_uri.toString());

		try {
			myimage = MediaStore.Images.Media.getBitmap(
					this.getContentResolver(), image_uri);
			// bmp =
			// MediaStore.Images.Media.getBitmap(this.getContentResolver(),
			// Uri.fromFile(file) );
			// do whatever you want with the bitmap (Resize, Rename, Add To
			// Gallery, etc)
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.v(TAG,"Myimageeeeee Size:"+myimage.getByteCount());
		
		//Bitmap yourSelectedImage = BitmapFactory.decodeFile(filepath);
		ImageView imageView = (ImageView) findViewById(R.id.imgView);
		imageView.setImageBitmap(myimage);

		Button button_crop = (Button) findViewById(R.id.crop);
		Button button_done = (Button) findViewById(R.id.done);
	    final Context a=this;
		
		button_crop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "crop button clicked");
				Log.v(TAG, image_uri.toString());
				Intent intent = new Intent(a, Cropping.class);
				intent.putExtra("file_path", filepath);
				intent.putExtra("image_uri", image_uri.toString());
				startActivity(intent);

			}
		});

		button_done.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				myimage.recycle();
				Log.v(TAG, "OCR button clicked");
				Intent intent = new Intent(a, PreProcess.class);
				intent.putExtra("file_path", filepath);
				intent.putExtra("image_uri", image_uri.toString());
				startActivity(intent);
			}
		});

	}

}
