package com.nus.scope;

public class ParsedResults {
	
	public String address = "";
	public String emails = "";
	public String numbers = "";
	public String name = "";
	public String fax = "";
	public String website = "";
	
	public ParsedResults(){
		
	}
	
	public ParsedResults(String address_string, String email_string, String number_string, String name_string, String fax_string, String website){
		address = address_string;
		emails = email_string;
		numbers = number_string;
		name = name_string;
		fax = fax_string;
		this.website = website;
	}

}
