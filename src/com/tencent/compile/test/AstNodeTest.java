package com.tencent.compile.test;

import java.util.ArrayList;
import java.util.List;

import com.tencent.compile.models.JavaFieldModel;
import com.tencent.compile.models.JavaMethodModel;
import com.tencent.compile.models.TokenModel;
import com.tencent.compile.parser.JavaParser;
import com.tencent.compile.resultmodels.JavaClassResultModel;

public class AstNodeTest {

	
	//��ǰɨ�赽�ڼ���token
	private static int tokenScanCount=0;
	//token list
	private static List<TokenModel> tokenList;
	//����list
	private List<JavaMethodModel> methodList = new ArrayList<JavaMethodModel>();
	//field list
	private List<JavaFieldModel> fieldList = new ArrayList<JavaFieldModel>();
	//����
	private String packageName = new String("");
	//import �б� ������ƥ��������������
	private List<String> importList = new ArrayList<String>();
	//�̳�����
	private String extendsClassName;
	//ʵ���б�
	private List<String> implementsList = new ArrayList<String>();
	//��ǰ����
	private String currentClassName = new String("");
	//��ǰ��ȡ���ķ�����
	private String currentMethodName = new String("");
	
	private static JavaParser instance = null;
	
	private JavaClassResultModel result;
	
	public static String typeParsing(){
		moveToNextToken();	
		String type = new String(getCurrentToken().getContent());
		if(getNextToken().getContent().equals("[")){//������������
			while(!getNextToken().getContent().equals("]")){
				moveToNextToken();
				type=type+new String(getCurrentToken().getContent());
			}
			//��ǰ�ƶ���]����ĩβ
			moveToNextToken();
			type=type+new String(getCurrentToken().getContent());
		}else if(getNextToken().getContent().equals("<")){//�����������ͣ��ݹ�һ��ʵ��Ƕ�׵����ͱ��ʽ����
			moveToNextToken();
			type = type+"<";
			type = type+typeParsing();
			while(getNextToken().getContent().equals(",")){//<T,P>ģ���డ��������T_T
				type = type +",";
				moveToNextToken();
				type = type+typeParsing();
			}
			while(!getNextToken().getContent().equals(">")){
				moveToNextToken();
			}
			type = type+">";
			moveToNextToken();
		}else if(getNextToken().getContent().equals(".")){//�������ڲ�����ShortCutCallback.Stub
			do{
				moveToNextToken();
				type=type+new String(getCurrentToken().getContent()); //TODO:.�ź����Ӧ��Ҳ�ǽ������ͣ����ܼ򵥵�ƴ����һ��token����ʵ��
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
	 * �﷨�����ı�׼��ʽ����Ӧ����ǰ�ȿ�һ�£��پ����Ƿ������ƣ�Ŀǰȫ����ֱ���ƹ�ȥ���ܲ���ѧ2014.10.23
	 * TODO:Ҫ�ĵõط��ܶ࣬��������2014.10.23
	 */
	private static TokenModel getNextToken(){
		return tokenList.get(tokenScanCount+1);
	}
	
	private static void moveToNextToken(){
		tokenScanCount++;
		//debug��
//		if(tokenScanCount == 16446){
//			return;
//		}
	}
}
