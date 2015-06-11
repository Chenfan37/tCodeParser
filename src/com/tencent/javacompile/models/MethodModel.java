package com.tencent.javacompile.models;

public class MethodModel {
	
	private String className;
	private String methodName;
	
	private int beginLineNumber;
	private int endLineNumber;
	
	public MethodModel(){
		
	}
	
	public void setClassName(String name){
		this.className = name;
	}
	
	public void setMethodName(String name){
		this.methodName = name;
	}
	
	public void setBeginLineNumber(int number){
		this.beginLineNumber = number;
	}
	
	public void setEndLineNumber(int number){
		this.endLineNumber = number;
	}
	
	public String getFullName(){
		return this.className+"."+this.methodName;
	}
	
	public int getBeginLineNumber(){
		return this.beginLineNumber;
	}
	
	public int getEndLineNumber(){
		return this.endLineNumber;
	}

}
