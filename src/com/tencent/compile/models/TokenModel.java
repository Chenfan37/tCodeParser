package com.tencent.compile.models;

public class TokenModel {

	//�������  
	public enum TokenType {
		keyword,               //Ŀǰ��ʱ����Ҫ�Ĺؼ���
		identifier,            //��ʶ��
		modifier,              //���η�
		typemodifier,          //�����������η�
		declarenew,            //����new
		declareclass,          //class��interface
		declarebase,           //�̳й�ϵ������extends��implements
		unary_operators,       //��Ŀ�����
		binocular_operators,   //˫Ŀ�����
		multiple_operators,    //��Ŀ�����
		constant,              //����
		constantint,           //���ͳ���
		brace,                 //������
		parentheses,           //��������
		escape,                //ת���
		separator,             //���޷�
		notype,                //û������
		comments,              //ע������
		error,                 //��������
		
		jce_is_require      //jce require��Ϣ
	};
	
	//����
	private TokenType tokenType;
	//����
	private String tokenContent;
	//������
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
