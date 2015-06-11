package com.tencent.javacompile.models.jce;

import java.util.HashMap;
import java.util.Map;

public class EnumModel {

	private String enumName;
	private Map<String,Integer> map = new HashMap<String,Integer>();
	private int beginLineNumber;
	private int endLineNumber;
	
	public void setEnumName(String name){
		this.enumName = name;
	}
	
	public String getEnumName(){
		return this.enumName;
	}
	
	public void putKeyValuePair(String key, int value){
		this.map.put(key, value);
	}
	
	public Map<String,Integer> getElements(){
		return this.map;
	}
	
	public void setBeginLineNumber(int number){
		this.beginLineNumber = number;
	}
	
	public void setEndLineNumber(int number){
		this.endLineNumber = number;
	}
	
	public int getBeginLineNumber(){
		return this.beginLineNumber;
	}
	
	public int getEndLineNumber(){
		return this.endLineNumber;
	}
}
