package com.nus.scope;

import org.opencv.android.OpenCVLoader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	protected static String _path;
	protected boolean _taken;
	private static final String TAG = "Scope.java";
	
	static {
		if (!OpenCVLoader.initDebug()) {
			Log.e(TAG, "Hello, Some Error!");
		}
	}
	
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/Scope/";
	//private static final String TAG = "Scope.java";
	public static final String lang = "eng";
	protected static final String PHOTO_TAKEN = "photo_taken";
	public static Context appContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar ab = getActionBar();
		ab.setTitle("Title");
		ab.setDisplayShowTitleEnabled(false);
		ab.setSubtitle("Subtitle");
		ab.setDisplayShowTitleEnabled(false);

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