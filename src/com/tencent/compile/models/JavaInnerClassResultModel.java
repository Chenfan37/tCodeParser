package com.tencent.compile.models;

import com.tencent.compile.resultmodels.JavaClassResultModel;

public class JavaInnerClassResultModel extends JavaClassResultModel{
	
	private boolean isAnonymous = false;
	private String externalClassName;
	
	public boolean isAnonymous(){
		return this.isAnonymous;
	}
	
	public void setIsAnonymous(boolean isAnonymous){
		this.isAnonymous = isAnonymous;
	}
	
	public String getExternalClassName(){
		return this.externalClassName;
	}
	
	public void setExternalClassName(String externalClassName){
		this.externalClassName = externalClassName;
	}

}
