package com.perseus.smsdataanalysis;

public class Contact implements Comparable<Contact>{
	public String contactName;
	public String id;
	public Boolean isContactChecked = false,isHeader=false;
	
	public Contact(){
		contactName = "";
		id = "";
	}
	public Contact(String contactName, String id){
		this.contactName = contactName;
		this.id = id;
	}

	public int compareTo(Contact anotherContact){
		return contactName.compareTo(anotherContact.contactName);
	}
}
