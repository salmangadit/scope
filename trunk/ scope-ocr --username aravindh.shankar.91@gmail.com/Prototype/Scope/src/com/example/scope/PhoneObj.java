package com.example.scope;

public class PhoneObj {
	
	public String phoneString = "";
	public String phoneNumberString = "";
	public int phoneNumber;
	public int confidence = 1;
	public boolean isFax = false;
	
	public PhoneObj(String phonestring, String number){
		phoneString = phonestring;
		phoneNumberString = number;
		
		if(IsInt(phoneNumberString)){
			phoneNumber = Integer.parseInt(phoneNumberString);
		}
	}

	//Checks if string is a number
	private boolean IsInt(String s){
		try{ 
			Integer.parseInt(s); 
			return true; 
		}
		catch(NumberFormatException er){
			return false; 
		}
	}
}
