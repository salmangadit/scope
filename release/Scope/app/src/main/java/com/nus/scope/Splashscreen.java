package com.nus.scope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Splashscreen extends Activity {

	// private static final int SPLASH_DISPLAY_TIME = 2000;
	private static final String TAG = "Scope.java";
	public static final String lang = "eng";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/Scope/";
	private int count = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
		//Log.v(TAG, "splash screen");
		new AsyncTask<Void, Void, Void> (){
			@Override
			protected void onPreExecute() {
            
				setContentView(R.layout.splashscreen);
				
				// Checking libraries that are installed
				count = 0;
				if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata"))
						.exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/osd.traineddata"))
						.exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/eng.cube.bigrams"))
						.exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/eng.cube.fold")).exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/eng.cube.lm")).exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/eng.cube.nn")).exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/eng.cube.params"))
						.exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/eng.cube.size")).exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/eng.tesseract_cube.nn"))
						.exists())
					count++;
				if (!(new File(DATA_PATH + "tessdata/eng.cube.word-freq"))
						.exists())
					count++;
				
				if (count > 5) {
					 String text = "Preparing libraries for first time use. Please wait...";
					 Toast toast = Toast.makeText(getApplicationContext(), text,
					 Toast.LENGTH_LONG );
					 toast.show();
				}			
			}
	
			@Override
			protected Void doInBackground(Void... arg0) {

				String[] paths = new String[] { DATA_PATH,
						DATA_PATH + "tessdata/" };
				Log.v(TAG, "datapath: " + DATA_PATH);
				for (String path : paths) {
					File dir = new File(path);
					if (!dir.exists()) {
						if (!dir.mkdirs()) {
							Log.v(TAG, "ERROR: Creation of directory " + path
									+ " on sdcard failed");
						} else {
							Log.v(TAG, "Created directory " + path
									+ " on sdcard");
						}
					}

				}


				// Loading OCR libraries
				if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata"))
						.exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.traineddata");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.traineddata");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied " + lang + " traineddata");
					} catch (IOException e) {
						Log.e(TAG, "Was unable to copy " + lang
								+ " traineddata " + e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/osd.traineddata"))
						.exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/osd.traineddata");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/osd.traineddata");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied " + "osd" + " traineddata");
					} catch (IOException e) {
						Log.e(TAG, "Was unable to copy " + "osd"
								+ " traineddata " + e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/eng.cube.bigrams"))
						.exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.cube.bigrams");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.cube.bigrams");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied eng.cube.bigrams");
					} catch (IOException e) {
						Log.e(TAG,
								"Was unable to copy eng.cube.bigrams "
										+ e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/eng.cube.fold")).exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.cube.fold");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.cube.fold");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied eng.cube.fold");
					} catch (IOException e) {
						Log.e(TAG,
								"Was unable to copy eng.cube.fold "
										+ e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/eng.cube.lm")).exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.cube.lm");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.cube.lm");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied eng.cube.lm");
					} catch (IOException e) {
						Log.e(TAG,
								"Was unable to copy eng.cube.lm "
										+ e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/eng.cube.nn")).exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.cube.nn");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.cube.nn");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied eng.cube.nn");
					} catch (IOException e) {
						Log.e(TAG,
								"Was unable to copy eng.cube.nn "
										+ e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/eng.cube.params"))
						.exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.cube.params");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.cube.params");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied eng.cube.params");
					} catch (IOException e) {
						Log.e(TAG,
								"Was unable to copy eng.cube.params "
										+ e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/eng.cube.size")).exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.cube.size");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.cube.size");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied eng.cube.size");
					} catch (IOException e) {
						Log.e(TAG,
								"Was unable to copy eng.cube.size "
										+ e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/eng.tesseract_cube.nn"))
						.exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.tesseract_cube.nn");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.tesseract_cube.nn");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied eng.tesseract_cube.nn");
					} catch (IOException e) {
						Log.e(TAG, "Was unable to copy eng.tesseract_cube.nn "
								+ e.toString());
					}
				}

				if (!(new File(DATA_PATH + "tessdata/eng.cube.word-freq"))
						.exists()) {
					try {

						AssetManager assetManager = getAssets();
						InputStream in = assetManager
								.open("tessdata/eng.cube.word-freq");
						// GZIPInputStream gin = new GZIPInputStream(in);
						OutputStream out = new FileOutputStream(DATA_PATH
								+ "tessdata/eng.cube.word-freq");

						// Transfer bytes from in to out
						byte[] buf = new byte[1024];
						int len;
						// while ((lenf = gin.read(buff)) > 0) {
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
						in.close();
						// gin.close();
						out.close();

						Log.v(TAG, "Copied eng.cube.word-freq");
					} catch (IOException e) {
						Log.e(TAG,
								"Was unable to copy eng.cube.word-freq "
										+ e.toString());
					}
				}
			return null;
			}
			@Override
		    protected void onPostExecute(Void result) {
		      passover();
		    }	
		}.execute();
		
		
		
	}

	private void passover(){
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
	}
}
