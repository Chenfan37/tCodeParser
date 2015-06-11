package com.tencent.compile.resultmodels;

import java.util.List;

public class JavaClassResultSimpleModel extends BaseResultModel{
	
	private String className = null;
	private String extendsName = null;
	private List<String> implementsList = null;

	public JavaClassResultSimpleModel(JavaClassResultModel model){
		
		if(model.getClassName()!=null)
			this.className = new String(model.getClassName());
		if(model.getExtends()!=null)
			this.extendsName = new String(model.getExtends());
		if(model.getImplementsList()!=null)
			this.implementsList = model.getImplementsList();	
		
	}
	
	@Override
	public String getResultByLineNumber(int lineNumber) {

		System.out.println("warning: 简化编译结果无法根据行数获取信息");
		
		return null;
	}
	
	public String getClassName(){
		return this.className;
	}
	
	public String getExtends(){
		return this.extendsName;
	}
	
	public List<String> getImplementsList(){
		return this.implementsList;
	}

}
