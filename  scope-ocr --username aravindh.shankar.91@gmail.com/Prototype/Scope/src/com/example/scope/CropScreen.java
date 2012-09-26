package com.example.scope;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
		Log.v(TAG, "creating...");
		final String filepath = getIntent().getStringExtra("file_path");
		Bitmap yourSelectedImage = BitmapFactory.decodeFile(filepath);
		ImageView imageView = (ImageView) findViewById(R.id.imgView);
		imageView.setImageBitmap(yourSelectedImage);

		Button button_crop = (Button) findViewById(R.id.crop);
		Button button_done = (Button) findViewById(R.id.done);
		// Context a=getActivity();
		final Context a = this;

		button_crop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "crop clicked");
				Intent intent = new Intent(a, Cropping.class);
				intent.putExtra("file_path", filepath);
				startActivity(intent);

			}
		});

		button_done.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				

			}
		});

	}

}
