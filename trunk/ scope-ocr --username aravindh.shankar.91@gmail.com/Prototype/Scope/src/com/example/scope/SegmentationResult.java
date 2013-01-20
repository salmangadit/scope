package com.example.scope;

public class SegmentationResult {
	public int X;
	public int Y;
	public String Result = null;
	public int Confidence;

	public String dumpResult() {
		return "(x,y) = " + X + "," + Y + " , Res: " + Result + ", Conf: "
				+ Confidence;
	}
}
