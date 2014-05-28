package com.perseus.smsdataanalysis;

public class Contact implements Comparable<Contact>{
	public String contactName;
	public String id;
	public Boolean isContactChecked = false,isHeader=false;
	
	public Contact(){
		contactName = "";
		id = "";
	}
	public Contact(String contactName, String num){
		this.contactName = contactName;
		this.id = num;
	}

	public int compareTo(Contact anotherContact){
		return contactName.compareTo(anotherContact.contactName);
	}
}
