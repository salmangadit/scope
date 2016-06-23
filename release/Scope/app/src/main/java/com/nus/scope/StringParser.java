package com.nus.scope;

import java.io.*;
import java.util.ArrayList;

import android.util.Log;

public class StringParser {
	private static final String TAG = "Scope.java";
	private final String[] ADDRESS_DICT = new String[] { "level", "building",
			"headquarters", "centre", "engineering", "drive", "street", "road",
			"lane", "house" };
	private final String[] EMAIL_DICT = new String[] { "email", "mail",
			"e-mail" };
	private final String[] SITE_DICT = new String[] { "yahoo", "gmail",
			"google", "hotmail", "nus.edu.sg" };
	private final String[] DOMAIN_DICT = new String[] { "http", "www", "net",
			"com", "edu", "org" };
	private final String[] WEBSITE_DICT = new String[] { "website", "site" };
	private final String[] PHONE_DICT = new String[] { "phone", "cell",
			"telephone", "mobile", "fax", "tel" };
	private final String WORD_SINGAPORE = "singapore";

	public String addressString = "";
	public String emailString = "";
	public String nameString = "";
	public String numberString = "";
	public String faxString = "";
	public String websiteString = "";
	public String inputString;
	private String text;

	private ArrayList<String> linesList = new ArrayList<String>();
	private ArrayList<String> address_linesList = new ArrayList<String>();
	private ArrayList<String> numbers_linesList = new ArrayList<String>();
	private ArrayList<String> email_linesList = new ArrayList<String>();
	private ArrayList<String> site_linesList = new ArrayList<String>();

	private ArrayList<AddressObj> addressList = new ArrayList<AddressObj>();
	private ArrayList<EmailObj> emailList = new ArrayList<EmailObj>();
	private ArrayList<PhoneObj> numberList = new ArrayList<PhoneObj>();
	private ArrayList<SiteObj> siteList = new ArrayList<SiteObj>();

	private String[] wordsList;
	private String[] linesArray;

	private int[] trackLines;

	private int lineCount = 0;
	private int startLine = -1;
	private int endLine = -1;
	private int addressConfidence = 0;

	public StringParser() {

	}

	public ParsedResults CardParse(ArrayList<String> input_strings) {
		for (String input : input_strings) {
			text = input.trim();
			linesArray = text.split("\n");
			for (int i = 0; i < linesArray.length; ++i) {
				linesArray[i] = linesArray[i].replaceAll("\\s+", " ");
				if (!(linesArray[i].isEmpty())) {
					linesList.add(linesArray[i].toLowerCase());
				}
			}

			trackLines = new int[linesList.size()];
			for (int i = 0; i < trackLines.length; ++i) {
				trackLines[i] = 0;
			}
			
			for(int j = 0; j < linesList.size(); ++j){
				Log.v(TAG, linesList.get(j));
			}
			address_linesList = linesList;
			numbers_linesList = linesList;
			FilterAddress();
			FilterWeb();
			FilterEmail();
			FilterPhone();
			FilterMisc();
			ClearLists();
		}
		return new ParsedResults(addressString, emailString, numberString,
				nameString, faxString, websiteString);
	}

	// Parses each line of retrieved text
	// Identifies and filters addresses and adds to addressList
	public void FilterAddress() {
		String address_string = "";
		int start_flag = 0;
		// Parse each line of the retrieved text
		for (int i = 0; i < address_linesList.size(); ++i) {
			wordsList = address_linesList.get(i).split(" ");
			IdentifyAddress();
			lineCount++;
		}

		for (int i = 0; i < address_linesList.size(); ++i) {
			if (i == startLine) {
				start_flag = 1;
				address_string += address_linesList.get(i) + " ";
			} else if (start_flag > 0 && i <= endLine) {
				address_string += address_linesList.get(i) + " ";
			}
		}
		address_string = CheckPIN(address_string);
		AddressObj temp = new AddressObj(address_string, startLine, endLine);
		if (addressConfidence == 1) {
			temp.confidence++;
			addressConfidence = 0;
			addressList.add(temp);
		}
		lineCount = 0;
		startLine = endLine = -1;

		GetAddress();

	}

	// Checks whether the selected address contains PINCODE and City
	// And filters the address and assigns confidence value if found.
	private String CheckPIN(String text) {
		int last_index = -1;
		text = text.replaceAll(":", " ");
		text = text.replaceAll("-", " ");
		text = text.replaceAll("\\s+", " ");
		wordsList = text.split(" ");
		for (int i = 0; i < wordsList.length; ++i) {
			if ((wordsList[i].equals(WORD_SINGAPORE))
					|| (LevenshteinDistance(wordsList[i], WORD_SINGAPORE) < 3)) {
				if ((i+1 < wordsList.length) && IsInt(wordsList[i + 1]) && (wordsList[i + 1].length() == 6)) {
					last_index = i + 1;
				}
				else{
					last_index = i;
				}
					addressConfidence = 1;
			}
		}
		if (last_index > -1) {
			text = "";
			for (int i = 0; i <= last_index; ++i) {
				StringBuilder word = new StringBuilder(wordsList[i]);
				word.setCharAt(0, Character.toUpperCase(word.charAt(0)));
				wordsList[i] = word.toString();
				text += wordsList[i] + " ";
			}
		}
		return text;
	}

	// Gets addresses that pass confidence checks
	private void GetAddress() {
		int max = 0;
		for (AddressObj address_obj : addressList) {
			if (address_obj.confidence > max) {
				max = address_obj.confidence;
			}
		}
		for (AddressObj address_obj : addressList) {
			if (address_obj.confidence == max && max > 1) {
				addressString += address_obj.addressString + " ";
			}
		}
	}

	// Does preliminary check for addresses
	private void IdentifyAddress() {
		int email_flag = 0;
		for (int i = 0; i < wordsList.length; ++i) {
			for (int j = 0; j < ADDRESS_DICT.length; ++j) {
				if (wordsList[i].equals(ADDRESS_DICT[j])) {
					if (startLine < 0) {
						startLine = lineCount;
						endLine = startLine;
					} else {
						endLine = lineCount;
					}
					trackLines[lineCount] = 1;
				} else if (LevenshteinDistance(wordsList[i], ADDRESS_DICT[j]) < 3) {
					if (wordsList[i].length() >= ADDRESS_DICT[j].length()) {

						if (startLine < 0) {
							startLine = lineCount;
							endLine = startLine;
						} else {
							endLine = lineCount;
						}
						wordsList[i] = wordsList[i]
								.replaceAll("[^\\w\\s]+", "");
						String temp = address_linesList.get(lineCount)
								.replaceAll(wordsList[i], ADDRESS_DICT[j]);
						address_linesList.set(lineCount, temp);
						trackLines[lineCount] = 1;
					}
				}
			}
			
			if ((wordsList[i].equals(WORD_SINGAPORE))|| (LevenshteinDistance(wordsList[i], WORD_SINGAPORE) < 3)) {
				if ((i+1 < wordsList.length) && IsInt(wordsList[i + 1]) && (wordsList[i + 1].length() == 6)) {
					if (startLine < 0) {
						startLine = lineCount;
						endLine = startLine;
					} else {
						endLine = lineCount;
					}
					trackLines[lineCount] = 1;
				}
			}

			wordsList[i] = wordsList[i].replaceAll(":", " ");
			wordsList[i] = wordsList[i].replaceAll("-", " ");
			wordsList[i] = wordsList[i].replaceAll("\\s+", " ");

			for (int k = 0; k < WEBSITE_DICT.length; ++k) {
				if (wordsList[i].equals(WEBSITE_DICT[k])
						|| (LevenshteinDistance(wordsList[i], WEBSITE_DICT[k]) < 3)) {
					site_linesList.add(address_linesList.get(lineCount));
					trackLines[lineCount] = 1;
				}
			}

			if (wordsList[i].contains("@")) {
				email_flag = 1;
				trackLines[lineCount] = 1;
			}

			if (email_flag == 1) {
				email_linesList.add(address_linesList.get(lineCount));
			}
		}
	}

	// Does check to identify valid Websites
	private void FilterWeb() {
		int site_confidence = 1;
		for (String line : site_linesList) {
			wordsList = line.split(" ");
			for (String word : wordsList) {
				for (int i = 0; i < WEBSITE_DICT.length; ++i) {
					if (word.equals(WEBSITE_DICT[i])
							|| (LevenshteinDistance(word, WEBSITE_DICT[i]) < 3)) {
						site_confidence++;
					}
				}
				for (String domain : DOMAIN_DICT) {
					if (word.contains(domain)) {
						site_confidence++;
					}
				}
				if( site_confidence > 0 ){
					siteList.add(new SiteObj(word, site_confidence));
				}
			}
		}
		GetWeb();
	}
	
	// Picks site with highest confidence
	private void GetWeb() {
		int max = 0;
		for (SiteObj site_obj : siteList) {
			if (site_obj.confidence >= max) {
				max = site_obj.confidence;
			}
		}
		for (SiteObj site_obj : siteList) {
			if (site_obj.confidence == max) {
				websiteString += site_obj.siteName + " ";
			}
		}
	}

	// Does check to identify valid email Id's
	private void FilterEmail() {
		int email_flag = 0;
		int list_count = 0;
		String email_string = "";
		// Check each line in the list for "@" symbol
		for (String line : email_linesList) {
			email_flag = 0;
			email_string = "";
			line = line.replaceAll(":", " ");
			line = line.replaceAll("\\s+", " ");
			wordsList = line.split(" ");
			for (String word : wordsList) {
				// Check for "email" indicator
				for (int j = 0; j < EMAIL_DICT.length; ++j) {
					if ((word.equals(EMAIL_DICT[j]))
							|| (LevenshteinDistance(word, EMAIL_DICT[j]) < 3)) {
						email_flag = 1;
					}
				}
				if (word.contains("@")) {
					emailList.add(new EmailObj(word));

				}
			}
			// Add confidence if "email" indicator is found
			if (email_flag == 1) {
				emailList.get(list_count).confidence += 1;
			}
			list_count++;
		}
		for (EmailObj email_obj : emailList) {
			email_string = email_obj.emailString;
			int index = -1;
			int end_index = index;
			String substring = "";
			// Add confidence if common site names are found
			for (String site : SITE_DICT) {
				index = email_string.indexOf('@');
				end_index = index + site.length();
				if (end_index >= email_string.length()) {
					end_index = email_string.length() - 1;
				}
				index++;
				substring = email_string.substring(index, end_index + 1);
				if (email_string.equals(site)
						|| LevenshteinDistance(substring, site) < 3) {
					email_obj.emailString = email_obj.emailString.replace(
							substring, site);
					email_obj.confidence += 1;
				}

			}
			// Add confidence if email string contains common domain names
			for (String domain : DOMAIN_DICT) {
				substring = email_obj.emailString.substring(index,
						email_obj.emailString.length());
				if (substring.contains(domain)) {
					email_obj.confidence += 1;
				}
			}
			System.out.println(email_obj.emailString + email_obj.confidence);
		}

		GetEmails();
	}

	// Consolidates all email id's and takes ones with highest confidence
	private void GetEmails() {
		int max = 0;
		for (EmailObj email_obj : emailList) {
			if (email_obj.confidence >= max) {
				max = email_obj.confidence;
			}
		}
		for (EmailObj email_obj : emailList) {
			if ((email_obj.confidence > max - 1) && (email_obj.confidence > 1)) {
				emailString += email_obj.emailString + " ";
			}
		}
	}

	// Does check to identify valid phone numbers
	private void FilterPhone() {
		int number_start = -1;
		int number_end = -1;
		int start_index = -1;
		int end_index = -1;
		int string_flag = 0;
		String phone_string = "";
		String phone_number = "";
		lineCount = 0;

		for (String line : numbers_linesList) {
			start_index = end_index = -1;
			number_start = number_end = -1;
			line = line.replaceAll("[^\\w\\s]+", "");
			wordsList = line.split(" ");
			for (int i = 0; i < wordsList.length; ++i) {
				if (IsInt(wordsList[i])) {
					if (number_start < 0) {
						number_start = i;
						number_end = end_index = number_start;
						trackLines[lineCount] = 1;
					} else {
						number_end = i;
						end_index = i;
					}
				} else {
					if (number_start < 0) {
						if (start_index < 0) {
							start_index = end_index = i;
						}
					} else {
						end_index = i - 1;
						if (start_index < 0) {
							start_index = end_index;
						}
						string_flag = 1;
					}
				}
				if (i == wordsList.length - 1 || string_flag == 1) {
					if (start_index < 0) {
						start_index = end_index = i;
					}
					for (int j = start_index; j <= end_index; ++j) {
						phone_string += wordsList[j] + " ";
					}
					if (number_start > -1) {
						for (int j = number_start; j <= number_end; ++j) {
							phone_number += wordsList[j];
						}
						phone_number = phone_number.trim();
						phone_string = phone_string.trim();
						numberList
								.add(new PhoneObj(phone_string, phone_number));
					}
					number_start = number_end = -1;
					start_index = end_index = i;
					phone_string = phone_number = "";
					string_flag = 0;
				}
			}
			lineCount++;
		}

		for (PhoneObj phone_obj : numberList) {
			wordsList = phone_obj.phoneString.split(" ");
			for (String word : wordsList) {
				for (String key : PHONE_DICT) {
					if (word.equals(key) || LevenshteinDistance(word, key) < 3) {
						phone_obj.confidence++;
						if (word.equals("f") || word.equals("fax")) {
							phone_obj.isFax = true;
						}
					}
				}
			}
		}

		GetNumbers();
	}

	// Gets numbers with highest confidence
	private void GetNumbers() {
		int max = 0;
		for (PhoneObj phone_obj : numberList) {
			if (phone_obj.confidence >= max) {
				max = phone_obj.confidence;
			}
		}
		for (PhoneObj phone_obj : numberList) {
			if ((phone_obj.confidence >= max - 1) && (phone_obj.confidence > 1)) {
				if(phone_obj.phoneNumberString.length() > 6){
					if (phone_obj.isFax) {
						faxString += phone_obj.phoneNumberString + " ";
					} else {
						numberString += phone_obj.phoneNumberString + " ";
					}
				}
			}
			System.out.println(phone_obj.phoneNumberString
					+ phone_obj.confidence);
		}
	}

	// Filters out the text that does not match any criteria
	private void FilterMisc() {
		String namestring = "";
		String temp_string = "";
		String address = "";
		String temp = "";
		for (int j = 0; j < linesList.size(); ++j) {
			if (trackLines[j] == 0) {
				namestring = linesList.get(j);
				temp_string = namestring.replaceAll("[^\\w\\s]+", "");
				address = addressString.toLowerCase();
				address = address.replaceAll("[^\\w\\s]+", "");

				if (!(address.contains(temp_string))) {
					wordsList = namestring.split(" ");
					temp_string = "";
					for (int i = 0; i < wordsList.length; ++i) {
						temp = wordsList[i]
								.replaceAll("[^\\w\\s]+", "");
						if(temp.length() > 0){
							StringBuilder word = new StringBuilder(temp);
							word.setCharAt(0, Character.toUpperCase(word.charAt(0)));
							wordsList[i] = word.toString();
							nameString += wordsList[i] + " ";
						}
					}
				}
			}
		}
		nameString = nameString.trim();
	}
	
	// Calculates minimum number of insertions, deletions and substitutions
	// required to convert str1 to str2 based on Levenshtein's Algorithm
	private int LevenshteinDistance(String str1, String str2) {

		int[][] distance = new int[str1.length() + 1][str2.length() + 1];
		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = Minimum(
						distance[i - 1][j] + 1,//above cell - insertion
						distance[i][j - 1] + 1,//left cell - deletion
						distance[i - 1][j - 1] //above-left cell - match (or) mismatch
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));

		return distance[str1.length()][str2.length()];
	}

	// Finds minimum of three values
	private int Minimum(int a, int b, int c) {

		return Math.min(Math.min(a, b), c);
	}

	// Checks if string is a number
	private boolean IsInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException er) {
			return false;
		}
	}

	// Clears all helper lists for next segment
	private void ClearLists() {
		address_linesList.clear();
		numbers_linesList.clear();
		email_linesList.clear();
		site_linesList.clear();
		linesList.clear();
		addressList.clear();
		emailList.clear();
		numberList.clear();
		siteList.clear();
		lineCount = 0;
	}
}
