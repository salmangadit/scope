package com.example.scope;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class Globals extends Application {
	private List<SegmentationResult> result = null;

	public List<SegmentationResult> getSegmentationResult() {
		if (result == null) {
			return new ArrayList<SegmentationResult>();
		}

		return result;
	}

	public void setSegmentationResult(List<SegmentationResult> s) {
		result = s;
	}
}
