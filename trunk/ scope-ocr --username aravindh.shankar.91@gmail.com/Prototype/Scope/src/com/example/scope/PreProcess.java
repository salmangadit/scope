package com.example.scope;

import java.util.List;

import org.opencv.android.OpenCVLoader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PreProcess extends Activity {

	private static final String TAG = "Scope.java";

	public double alpha = 3.0;
	public double beta = 0;
	public Bitmap myimage;
	public Bitmap ppimage;
	public Uri image_uri;
	public String filepath;
	public Uri result_uri = null;
	ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(TAG, "enter...");
		Log.v(TAG, "Preprocess...");

		ActionBar ab = getActionBar();
		ab.setTitle("Title");
		ab.setDisplayShowTitleEnabled(false);
		ab.setSubtitle("Subtitle");
		ab.setDisplayShowTitleEnabled(false);
		setContentView(R.layout.cropping);

		// Intent input
		filepath = getIntent().getStringExtra("file_path");
		String b = getIntent().getStringExtra("image_uri");
		image_uri = Uri.parse(b);
		Log.v(TAG, image_uri.toString());
		// setContentView(R.layout.cropping);
	}

	@Override
	public void onResume() {
		super.onResume();
		final Uri uri;
		uri = image_uri;

		new PreProcessAsync(uri, this).execute();

		// Button button_done = (Button) findViewById(R.id.button1);

		final Context a = this;

		// button_done.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Log.v(TAG, "Done button clicked");
		// Intent i = new Intent(a, Ocrmain.class);
		// i.putExtra("file_path", filepath);
		// //i.putExtra("image_uri", result_uri.toString());
		// startActivity(i);
		//
		// }
		// });
	}

	public void nextActivity() {
		Intent i = new Intent(this, Ocrmain.class);
		i.putExtra("file_path", filepath);
		// i.putExtra("image_uri", result_uri.toString());
		startActivity(i);
	}
}
