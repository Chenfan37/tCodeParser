package com.tencent.javacompile.models.jce;

public class StructFieldModel {

	private String fieldName;
	private boolean isRequired;
	private String typeName;
	private int declareLineNumber;
	
	public void setFieldName(String fieldName){
		this.fieldName = fieldName;
	}
	
	public void setIsRequired(boolean isRequired){
		this.isRequired = isRequired;
	}
	
	public void setTypeName(String typeName){
		this.typeName = typeName;
	}
	
	public String getFieldName(){
		return this.fieldName;
	}
	
	public boolean getIsRequired(){
		return this.isRequired;
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

}
