package com.perseus.smsdataanalysis;

public class Contact implements Comparable<Contact>{
	public String contactName;
	public String num;
	public Boolean isContactChecked = false,isHeader=false;

	public int compareTo(Contact anotherContact){
		return contactName.compareTo(anotherContact.contactName);
	}
}
