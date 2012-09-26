package com.example.scope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	protected static String _path;
	protected boolean _taken;

	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/Scope/";
	private static final String TAG = "Scope.java";
	public static final String lang = "eng";
	protected static final String PHOTO_TAKEN = "photo_taken";
	public static Context appContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create any missing directories
		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path
							+ " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}

		}

		// This area needs work and optimization
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata"))
				.exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/eng.traineddata");
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
				Log.e(TAG,
						"Was unable to copy " + lang + " traineddata "
								+ e.toString());
			}
		}
		
		setContentView(R.layout.activity_main);
		
		// ActionBar gets initiated
		ActionBar actionbar = getActionBar();

		// Tell the ActionBar we want to use Tabs.
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// initiating both tabs and set text to it.
		ActionBar.Tab ScanTab = actionbar.newTab().setText("SCAN");
		ActionBar.Tab UploadTab = actionbar.newTab().setText("UPLOAD");
		ActionBar.Tab SettingsTab = actionbar.newTab().setText("SETTINGS");

		// create the two fragments we want to use for display content
		Fragment ScanFragment = new ScanFragment();
		Fragment UploadFragment = new UploadFragment();
		Fragment SettingsFragment = new SettingsSetterFragment();

		// set the Tab listener. Now we can listen for clicks.
		ScanTab.setTabListener(new TabListener(ScanFragment));
		UploadTab.setTabListener(new TabListener(UploadFragment));	
		SettingsTab.setTabListener(new TabListener(SettingsFragment));

		// add the two tabs to the actionbar
		actionbar.addTab(ScanTab);
		actionbar.addTab(UploadTab);
		actionbar.addTab(SettingsTab);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}