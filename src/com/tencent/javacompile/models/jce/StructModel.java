package com.tencent.javacompile.models.jce;

import java.util.ArrayList;
import java.util.List;

public class StructModel {

	private String structName;
	private List<StructFieldModel> fieldList = new ArrayList<StructFieldModel>();
	private int beginLineNumber;
	private int endLineNumber;
	
	public void setStructName(String structName){
		this.structName = structName;
	}
	
	public void addField(StructFieldModel model){
		this.fieldList.add(model);
	}
	
	public String getStructName(){
		return this.structName;
	}
	
	public List<StructFieldModel> getFieldList(){
		return this.fieldList;
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
