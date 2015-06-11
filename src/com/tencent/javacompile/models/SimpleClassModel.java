package com.tencent.javacompile.models;

import java.util.List;

import com.tencent.javacompile.parser.JParser;

/**
 * 简单类模型，避免全工程分析时oom用
 * @author frankcfliu
 *
 */
public class SimpleClassModel {
	
	private String filePath = null;
	private String className = null;
	private String extendsName = null;
	private List<String> implementsList = null;

	public SimpleClassModel(String className,String extendsName,List<String> implementsList){
		this.className = className;
		this.extendsName = extendsName;
		this.implementsList = implementsList;
	}
	
	public SimpleClassModel(JParser parser){
		if(parser.getClassName()!=null)
			this.className = new String(parser.getClassName());
		if(parser.getExtends()!=null)
			this.extendsName = new String(parser.getExtends());
		if(parser.getImplementsList()!=null)
			this.implementsList = parser.getImplementsList();
	}
	
	public String getClassName(){
		return this.className;
	}
	
	public String getExtendsName(){
		return this.extendsName;
	}
	
	public List<String> getImplementList(){
		return this.implementsList;
	}
}
