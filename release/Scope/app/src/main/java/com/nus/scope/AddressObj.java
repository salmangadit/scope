package com.nus.scope;

public class AddressObj {
	
	public String addressString = "";
	
	public int startLine = -1;
	public int endLine = -1;
	public int startIndex = -1;
	public int endIndex = -1;
	public int segmentNo = 0;
	public int confidence = 1;
	
	public AddressObj( String address_string, int startline, int endline){
		addressString = address_string;
		startLine = startline;
		endLine = endline;	
	}

}
