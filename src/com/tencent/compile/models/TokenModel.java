package com.tencent.compile.models;

public class TokenModel {

	//属性类别  
	public enum TokenType {
		keyword,               //目前暂时不重要的关键字
		identifier,            //标识符
		modifier,              //修饰符
		typemodifier,          //基础类型修饰符
		declarenew,            //就是new
		declareclass,          //class或interface
		declarebase,           //继承关系声明即extends和implements
		unary_operators,       //单目运算符
		binocular_operators,   //双目运算符
		multiple_operators,    //多目运算符
		constant,              //常量
		constantint,           //整型常量
		brace,                 //大括号
		parentheses,           //其他括号
		escape,                //转义符
		separator,             //界限符
		notype,                //没有类型
		comments,              //注释类型
		error,                 //错误类型
		
		jce_is_require      //jce require信息
	};
	
	//类型
	private TokenType tokenType;
	//内容
	private String tokenContent;
	//所在行
	private int tokenLineNumber;
	
	public TokenModel(){
		
	}
	
	public void setContent(String content){
		this.tokenContent = content;
	}
	
	public void setTokenType(TokenType type){
		this.tokenType = type;
	}
	
	public void setLineNumber(int lineNumber){
		this.tokenLineNumber = lineNumber;
	}
	
	public String getContent(){
		return this.tokenContent;
	}
	
	public int getLineNumber(){
		return this.tokenLineNumber;
	}
	
	public TokenType getType(){
		return this.tokenType;
	}
}
