package com.nus.scope;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.widget.Toast;

public class TabListener implements ActionBar.TabListener{
	public Fragment fragment;
	 
	public TabListener(Fragment fragment) {
	this.fragment = fragment;
	}
	 
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	Toast re = Toast.makeText(MainActivity.appContext, "Reselected!", Toast.LENGTH_LONG);
	re.show();
	}
	 
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
	ft.replace(R.id.fragment_container, fragment);
	}
	 
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	ft.remove(fragment);
	}
}
