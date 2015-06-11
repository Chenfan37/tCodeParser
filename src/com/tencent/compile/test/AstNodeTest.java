package com.tencent.compile.test;

import java.util.ArrayList;
import java.util.List;

import com.tencent.compile.models.JavaFieldModel;
import com.tencent.compile.models.JavaMethodModel;
import com.tencent.compile.models.TokenModel;
import com.tencent.compile.parser.JavaParser;
import com.tencent.compile.resultmodels.JavaClassResultModel;

public class AstNodeTest {

	
	//当前扫描到第几个token
	private static int tokenScanCount=0;
	//token list
	private static List<TokenModel> tokenList;
	//方法list
	private List<JavaMethodModel> methodList = new ArrayList<JavaMethodModel>();
	//field list
	private List<JavaFieldModel> fieldList = new ArrayList<JavaFieldModel>();
	//包名
	private String packageName = new String("");
	//import 列表 ，用来匹配类型完整名字
	private List<String> importList = new ArrayList<String>();
	//继承类名
	private String extendsClassName;
	//实现列表
	private List<String> implementsList = new ArrayList<String>();
	//当前类名
	private String currentClassName = new String("");
	//当前获取到的方法名
	private String currentMethodName = new String("");
	
	private static JavaParser instance = null;
	
	private JavaClassResultModel result;
	
	public static String typeParsing(){
		moveToNextToken();	
		String type = new String(getCurrentToken().getContent());
		if(getNextToken().getContent().equals("[")){//处理数组类型
			while(!getNextToken().getContent().equals("]")){
				moveToNextToken();
				type=type+new String(getCurrentToken().getContent());
			}
			//向前移动将]加入末尾
			moveToNextToken();
			type=type+new String(getCurrentToken().getContent());
		}else if(getNextToken().getContent().equals("<")){//处理容器类型，递归一下实现嵌套的类型表达式解析
			moveToNextToken();
			type = type+"<";
			type = type+typeParsing();
			while(getNextToken().getContent().equals(",")){//<T,P>模版类啊。。坑死T_T
				type = type +",";
				moveToNextToken();
				type = type+typeParsing();
			}
			while(!getNextToken().getContent().equals(">")){
				moveToNextToken();
			}
			type = type+">";
			moveToNextToken();
		}else if(getNextToken().getContent().equals(".")){//处理公共内部类如ShortCutCallback.Stub
			do{
				moveToNextToken();
				type=type+new String(getCurrentToken().getContent()); //TODO:.号后面的应该也是解析类型，不能简单地拼上下一个token，待实现
				moveToNextToken();
				type=type+new String(getCurrentToken().getContent());
			}while(getNextToken().getContent().equals("."));
		}
		return type;
	}
	
	
	private static TokenModel getCurrentToken(){
		return tokenList.get(tokenScanCount);
	}
	
	/**
	 * 语法分析的标准方式还是应该向前先看一下，再决定是否往下移，目前全都是直接移过去，很不科学2014.10.23
	 * TODO:要改得地方很多，慢慢来吧2014.10.23
	 */
	private static TokenModel getNextToken(){
		return tokenList.get(tokenScanCount+1);
	}
	
	private static void moveToNextToken(){
		tokenScanCount++;
		//debug用
//		if(tokenScanCount == 16446){
//			return;
//		}
	}
}
