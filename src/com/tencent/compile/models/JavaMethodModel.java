package com.tencent.compile.models;

import java.util.ArrayList;
import java.util.List;

public class JavaMethodModel {
	
	private String className;
	private String methodName;
	
	private boolean isConstruct = false;
	private String returnType;
	private List<String> parameterList = new ArrayList<String>();
	
	private int beginLineNumber;
	private int endLineNumber;
	
	private int beginTokenIndex;
	private int endTokenIndex;
	
	public JavaMethodModel(){
		
	}
	
	public void setBeginTokenIndex(int index){
		this.beginTokenIndex = index;
	}
	
	public int getBeginTokenIndex(){
		return this.beginTokenIndex;
	}
	
	public void setEndTokenIndex(int index){
		this.endTokenIndex = index;
	}
	
	public int getEndTokenIndex(){
		return this.endTokenIndex;
	}
	
	public void setIsConstructMethod(boolean isConstruct){
		this.isConstruct = isConstruct;
	}
	
	public void setParameterList(List<String> parameterList){
		this.parameterList = parameterList;
	}
	
	public List<String>getParameterList(){
		return this.parameterList;
	}
	
	public void setReturnType(String returnType){
		this.returnType = returnType;
	}
	
	public String getReturnType(){
		return this.returnType;
	}
	
	public boolean isConstructMethod(){
		return this.isConstruct;
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
	
	/**
	 * 类名+方法名+参数
	 * 基本可以做到准确区分了
	 * @return
	 */
	public String getFullName(){
		if(!isConstruct){
			return this.className+"."+this.methodName+this.getParametersInStringFormat();
		}else {
			return this.className+"."+"<init>"+this.getParametersInStringFormat();
		}
	}
	
	/**
	 * 参数列表的通用字符串格式
	 * 类似：(com.tencent.assistant.component.RankRecommendListView,int,int,boolean,boolean)
	 * @return
	 */
	public String getParametersInStringFormat(){
		String result = new String("");
//		if(this.parameterList==null)return null;
//		for(String str : this.parameterList){
//			result+=str+" | ";
//		}
//		if(result.endsWith(" | ")){
//			result = result.substring(0, result.length()-3);
//		}
		if(this.parameterList==null)return result;
		result+="(";
		for(String str : this.parameterList){
			result+=str+",";
		}
		if(result.endsWith(",")){
			result = result.substring(0, result.length()-1);
		}		
		result+=")";
		return result;
	}
	
	/**
	 * 方法在emma报告中的格式
	 * @return
	 */
	public String getEmmaFormat(){
		String result = new String("");
		//TODO: 方法在emma报告中的格式
		return result;
	}
	
	public int getBeginLineNumber(){
		return this.beginLineNumber;
	}
	
	public int getEndLineNumber(){
		return this.endLineNumber;
	}

}
