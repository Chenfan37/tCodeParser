package com.tencent.javacompile.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tencent.compile.models.TokenModel;
import com.tencent.compile.models.TokenModel.TokenType;
import com.tencent.compile.tokenizer.JceTokenizer;
import com.tencent.javacompile.models.jce.EnumModel;
import com.tencent.javacompile.models.jce.StructFieldModel;
import com.tencent.javacompile.models.jce.StructModel;

public class JceParser {

	//当前扫描到第几个token
	private int tokenScanCount=0;
	//token list
	private List<TokenModel> tokenList;
	//enum list
	private List<EnumModel> resultEnumList = new ArrayList<EnumModel>();
	//struct list
	private List<StructModel> resultStructList = new ArrayList<StructModel>();
	//module name
	private String moduleName = null;
	
	private TokenModel getCurrentToken(){
		return tokenList.get(tokenScanCount);
	}
	
	private void moveToNextToken(){
		tokenScanCount++;
	}
	
	
	public JceParser(List<TokenModel> list){
		
		this.tokenList = list;
//		resultList = new ArrayList<MethodModel>();
		tokenScanCount = 0;
		initializing();
		moduleParsing();
//		for(int i=0;i<50;i++)
//			System.out.println(tokenList.get(500+i).getContent());
	}
	
	public JceParser(String fileName){
		JceTokenizer tokenizer = new JceTokenizer(fileName);
		//JTokenizer tokenizer = new JTokenizer("AppDetailActivity.java");
		JParser parser;
		try {
			tokenizer.DFA();
			this.tokenList = tokenizer.tokenResultList;
			initializing();
			moduleParsing();
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("in file "+fileName);
			System.out.println("词法分析错误");
			return;
		}
	}
	
	public JceParser(ByteArrayOutputStream outStream){
		
		JceTokenizer tokenizer = new JceTokenizer(outStream);
		//JTokenizer tokenizer = new JTokenizer("AppDetailActivity.java");
		JceParser parser;
		try {
			tokenizer.DFA();
			this.tokenList = tokenizer.tokenResultList;
			initializing();
			moduleParsing();
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			System.out.println("in file "+fileName);
//			System.out.println("词法分析错误");
			return;
		}
	}
	
	private void initializing(){
		while(!getCurrentToken().getContent().equals("module")){
			moveToNextToken();
		}
	}
	
	private void moduleParsing(){
		if(getCurrentToken().getContent().equals("module")){
			while(!getCurrentToken().getContent().equals("}")){
				moveToNextToken();
				if(getCurrentToken().getType().equals(TokenType.identifier)){
					this.moduleName = getCurrentToken().getContent();
				}else if(getCurrentToken().getContent().equals("enum")){
					enumParsing();
				}else if(getCurrentToken().getContent().equals("struct")){
					structParsing();
				}
			}
		}else{
			return;
		}
	}
	
	/**
	 * 起始位置为关键字enum
	 * 
	 */
	private void enumParsing(){
		if(getCurrentToken().getContent().equals("enum")){
			EnumModel model = new EnumModel();
			moveToNextToken();
			model.setEnumName(getCurrentToken().getContent());
			model.setBeginLineNumber(getCurrentToken().getLineNumber());
			boolean isNegaNum = false; 
			String tempName = null;
			Integer tempValue = null;
			while(!getCurrentToken().getContent().equals("}")){
				moveToNextToken();
				if(getCurrentToken().getType().equals(TokenType.identifier)){//
					tempName = new String(getCurrentToken().getContent());			
				}else if(getCurrentToken().getContent().equals("-")){
					isNegaNum = true;
				}else if(getCurrentToken().getType().equals(TokenType.constantint)){	
					tempValue = Integer.parseInt(getCurrentToken().getContent())*(isNegaNum?-1:1);
					isNegaNum = false;
				}else if((getCurrentToken().getContent().equals(","))){
					if(tempName!=null&&tempValue!=null){
						model.putKeyValuePair(tempName, tempValue);
					}
					tempName=null;
					tempValue=null;
				}
			}
			model.setEndLineNumber(getCurrentToken().getLineNumber());
			moveToNextToken();	
			resultEnumList.add(model);
		}else{
			return;
		}
	}
	
	private void structParsing(){
		if(getCurrentToken().getContent().equals("struct")){
			StructModel model = new StructModel();
			moveToNextToken();
			model.setStructName(getCurrentToken().getContent());
			model.setBeginLineNumber(getCurrentToken().getLineNumber());
			boolean isRequired = false;
			String tempName = null;
			String tempType =null;
			while(!getCurrentToken().getContent().equals("}")){
				moveToNextToken();
				if(getCurrentToken().getType().equals(TokenType.jce_is_require)){//
					isRequired = getCurrentToken().getContent().equals("require")?true:false;
				}else if(getCurrentToken().getType().equals(TokenType.identifier)){
					tempName = new String(getCurrentToken().getContent());
				}else if(getCurrentToken().getType().equals(TokenType.typemodifier)){	
					if(getCurrentToken().getContent().equals("vector")){
						tempType = new String(getCurrentToken().getContent());
						moveToNextToken();
						while(!getCurrentToken().getContent().equals(">")){
							tempType = tempType+getCurrentToken().getContent();
							moveToNextToken();
						}
						tempType=tempType+getCurrentToken().getContent();
					}else{
						tempType=new String(getCurrentToken().getContent());
					}
				}else if((getCurrentToken().getContent().equals(";"))){
					if(tempName!=null&&tempType!=null){
						StructFieldModel fieldModel = new StructFieldModel();
						fieldModel.setDeclareLineNumber(getCurrentToken().getLineNumber());
						fieldModel.setFieldName(tempName);
						fieldModel.setIsRequired(isRequired);
						fieldModel.setTypeName(tempType);
						model.addField(fieldModel);
					}
					tempName=null;
					tempType=null;
					isRequired = false;
				}
			}
			model.setEndLineNumber(getCurrentToken().getLineNumber());
			moveToNextToken();
			resultStructList.add(model);
		}else{
			return;
		}
	}
	
	public List<EnumModel> getEnums(){
		return this.resultEnumList;
	}
	
	public List<StructModel> getStructs(){
		return this.resultStructList;
	}
}
