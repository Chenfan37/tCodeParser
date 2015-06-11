package com.tencent.compile.models;

public class JavaFieldModel {
	
	private String className;
	private String fieldName;
	private String typeName;
	
	private int declareLineNumber;
	
	public void FieldModel(){
		
	}
	
	public void setClassName(String name){
		this.className = name;
	}
	
	public String getClassName(){
		return this.className;
	}
	
	public void setFieldName(String name){
		this.fieldName = name;
	}
	
	public String getFieldName(){
		return this.fieldName;
	}
	
	public void setTypeName(String name){
		this.typeName = name;
	}
	
	public String getTypeName(){
		return this.typeName;
	}
	
	public void setDeclareLineNumber(int number){
		this.declareLineNumber = number;
	}
	
	public int getDeclareLineNumber(){
		return this.declareLineNumber;
	}

	public String getFieldInfo(){
		return new String(this.typeName +" "+this.fieldName);
	}
}
