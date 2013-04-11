package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

public class Message implements Serializable{
	
	String key;
	String value;
	String type;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	

}
