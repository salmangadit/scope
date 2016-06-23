package com.nus.scope;

import android.net.Uri;

public class SegmentationResult {
	public int X;
	public int Y;
	public String Result = null;
	public int Confidence;
	public Uri image;
	
	public String dumpResult() {
		return "(x,y) = " + X + "," + Y + " , Res: " + Result + ", Conf: "
				+ Confidence;
	}
}
