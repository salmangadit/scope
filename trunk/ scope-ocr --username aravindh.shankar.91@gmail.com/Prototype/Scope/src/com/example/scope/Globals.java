package com.example.scope;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.net.Uri;

public class Globals extends Application {
	private List<SegmentationResult> result = null;
	private List<Uri> adaptiveResults = null;
	
	public List<SegmentationResult> getSegmentationResult() {
		if (result == null) {
			return new ArrayList<SegmentationResult>();
		}

		return result;
	}

	public void setSegmentationResult(List<SegmentationResult> s) {
		result = s;
	}
	
	public List<Uri> getAdaptiveResult() {
		if (adaptiveResults == null) {
			return new ArrayList<Uri>();
		}

		return adaptiveResults;
	}

	public void setAdaptiveResult(List<Uri> s) {
		adaptiveResults = s;
	}
}
