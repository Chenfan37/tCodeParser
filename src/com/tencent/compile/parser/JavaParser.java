package com.tencent.compile.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tencent.compile.models.JavaFieldModel;
import com.tencent.compile.models.JavaInnerClassResultModel;
import com.tencent.compile.models.JavaMethodModel;
import com.tencent.compile.models.TokenModel;
import com.tencent.compile.models.TokenModel.TokenType;
import com.tencent.compile.resultmodels.BaseResultModel;
import com.tencent.compile.resultmodels.JavaClassResultModel;
import com.tencent.compile.resultmodels.JavaClassResultSimpleModel;
import com.tencent.compile.tokenizer.JTokenizer;

/**
 * �µ�java�����﷨���������������Ż�
 * 1����Ϊ�ϸ�ذ��յݹ��½�����������Ϊʹ��getNextTokenԤ�У����ƶ�
 * 2��ͳһ���࣬�����������������߼�
 * 3��֧�ָ��������ĸ������Խ���
 * 4��֧�ַ����Ĳ������ͽ���
 * 5���Ż��������﷨����
 * @author frankcfliu
 *
 */
public class JavaParser extends AbstractParser{
	
	public static final String JAVA_PARSING_MODE = "JAVA_PARSING_MODE";
	public static final int SIMPLE_MODE = 1;
	public static final int NORMAL_MODE = 2;
	public static final int RESERVE_TOKEN_MODE = 3;

	//��ǰɨ�赽�ڼ���token
	private int tokenScanCount=0;
	//token list
	private List<TokenModel> tokenList;
	//����
	private String packageName = new String("");
	//import �б� ������ƥ��������������
	private List<String> importList = new ArrayList<String>();
	//��ǰ����
	private String currentClassName = new String("");
	
	private static JavaParser instance = null;
	
	private JavaClassResultModel result;

	private JavaParser(){}
	
	public static synchronized JavaParser getInstance(){
		if(instance ==null){
			instance = new JavaParser();
		}
		return instance;
	}
	
	@Override
	protected void init() {
		tokenScanCount=0;		
		tokenList = null;
		packageName = new String("");
		importList = new ArrayList<String>();
		currentClassName = new String("");
		result = new JavaClassResultModel();
		result.setIsAbstract(false);
		result.setIsInterface(false);
	}
	
	/**
	 * �﷨�����������������ط������
	 */
	@Override
	public BaseResultModel parsing(ByteArrayOutputStream outStream,
			ParsingConfig config) {

		
		init();
		
		JTokenizer tokenizer = new JTokenizer(outStream);
		//JTokenizer tokenizer = new JTokenizer("AppDetailActivity.java");
		try {
			tokenizer.DFA();
			this.tokenList = tokenizer.tokenResultList;			
			//����ȫע�͵����ļ������ؿյ�result
			if(this.tokenList.size()==0){
				if(config==null){
					return result;
				}
				return config.getAtrribute(JAVA_PARSING_MODE)!=SIMPLE_MODE?result:new JavaClassResultSimpleModel(result);
			}
			initPackageName();
			//package֮��Ϊ�յ������������Խ��
			if(isLastToken()){
				if(config==null){
					return result;
				}
				return config.getAtrribute(JAVA_PARSING_MODE)!=SIMPLE_MODE?result:new JavaClassResultSimpleModel(result);
			}
			//��ʱ�����߼�������û��import�Ĵ��뵼�µ�����
			if(getNextToken().getContent().equals("import")){
				initImportList();
			}
			//import֮��Ϊ�յ������������Խ��
			if(isLastToken()){
				if(config==null){
					return result;
				}
				return config.getAtrribute(JAVA_PARSING_MODE)!=SIMPLE_MODE?result:new JavaClassResultSimpleModel(result);
			}
			declarationParsing(0,0);
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());		
		} catch (IOException e) {
			e.printStackTrace();
//			System.out.println("in file "+fileName);
//			System.out.println("�ʷ���������");
			return null;
		}
		//��ģʽֻ�������ͼ̳�ʵ�ֹ�ϵ
		if(config!=null&&config.getAtrribute(JAVA_PARSING_MODE)==SIMPLE_MODE){
			return new JavaClassResultSimpleModel(result);
		}
		
		//�����ִ�ģʽ�ᱣ�����������еĴʷ��������
		if(config!=null&&config.getAtrribute(JAVA_PARSING_MODE)==RESERVE_TOKEN_MODE){
			
			List<TokenModel> resultTokenModel = new ArrayList<TokenModel>();
			resultTokenModel.addAll(tokenList);
			result.setTokenList(resultTokenModel);
		}
		return result;
	}
	
	/**
	 * �﷨�����������������ط������
	 */
	@Override
	public BaseResultModel parsing(String filepath,
			ParsingConfig config) {		
		init();
		
		JTokenizer tokenizer = new JTokenizer(filepath);
		try {
			tokenizer.DFA();
			this.tokenList = tokenizer.tokenResultList;			
			//����ȫע�͵����ļ������ؿյ�result
			if(this.tokenList.size()==0){
				return config.getAtrribute(JAVA_PARSING_MODE)!=SIMPLE_MODE?result:new JavaClassResultSimpleModel(result);
			}
			initPackageName();
			//��ʱ�����߼�������û��import�Ĵ��뵼�µ�����
			if(getNextToken().getContent().equals("import")){
				initImportList();
			}
			declarationParsing(0,0);
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());		
		} catch (IOException e) {
			e.printStackTrace();
//			System.out.println("in file "+fileName);
//			System.out.println("�ʷ���������");
			return null;
		}
		//��ģʽֻ�������ͼ̳�ʵ�ֹ�ϵ
		if(config!=null&&config.getAtrribute(JAVA_PARSING_MODE)==SIMPLE_MODE){
			return new JavaClassResultSimpleModel(result);
		}
		
		//�����ִ�ģʽ�ᱣ�����������еĴʷ��������
		if(config!=null&&config.getAtrribute(JAVA_PARSING_MODE)==RESERVE_TOKEN_MODE){
			
			List<TokenModel> resultTokenModel = new ArrayList<TokenModel>();
			resultTokenModel.addAll(tokenList);
			result.setTokenList(resultTokenModel);
		}
		return result;
	}
	
	/**
	 * ����import �б�
	 * ���ó�����ͽӿڱ�־���߼�Ҳ��ʱ��������
	 * ��ʼλ��Ϊimport��俪ʼ��ǰһ��token
	 * ���ƶ���������࿪ʼ֮��
	 */
	private void initImportList(){
		do{
			//typeParsing()������ǰ��tokenΪ�ֺ�����Ҫ��ǰ�ƶ�һ��
			if(getNextToken().getContent().equals("import")){
				moveToNextToken();
				if(getNextToken().getContent().equals("static")){
					moveToNextToken();
				}
				importList.add(typeParsing());
				//�ƶ����ֺ�
				moveToNextToken();
			}else{
				break;
			}
		}while(getNextToken().getContent().equals("import"));
		
		while(!getNextToken().getType().equals(TokenType.modifier)&&!getNextToken().getType().equals(TokenType.declareclass)){
			moveToNextToken();
		}

	}
	
	/**
	 * ��ʼ������
	 * �ƶ���package��������֮��
	 */
	private void initPackageName(){
		
		while(!isLastToken()&&!getCurrentToken().getContent().trim().equals("package")){
			moveToNextToken();
		}
		if(getCurrentToken().getContent().equals("package")){
			moveToNextToken();
			while(!getCurrentToken().getContent().equals(";")){
				packageName = packageName + getCurrentToken().getContent();
				moveToNextToken();
			}
		}
//		System.out.println(packageName);
		//�ƶ���class������
//		while(!getCurrentToken().getType().equals(TokenType.declareclass)){
//			moveToNextToken();
//		}
	}
	
	/**
	 * ���������Ľ������������������
	 * ��ǰtokenΪdeclarationǰһ��token
	 * ��̽�⵽��һ��token���������﷨ʱִ�д��߼�
	 * ��������ʱ����Ҫ����Ƕ�ײ�Σ����������������Ĵ����ʱ�ټ�1
	 * ���ؽ�����ǰ��Ƕ�ײ�ε�������count
	 * @param declarationLevel
	 */
	private int declarationParsing(int nestLevel,int anonymousClassIndex){
		
		DelarationModifierInfo info = new DelarationModifierInfo();
		
		//������Ҫ�������������������
		int anonymousClassCount = anonymousClassIndex;
		
		if(nestLevel == 0){//Ƕ�ײ㼶Ϊ0����������
			info.visibilityModifier = new String("public");      //���ڲ���һ����public
			while(!getNextToken().getType().equals(TokenType.declareclass)){//������ʶ����class/interface��֮ǰ
				if(getNextToken().getContent().equals("final")){
					info.finalModifier = new String("final");
				}else if(getNextToken().getContent().equals("abstract")){
					info.abstractModifier = new String("abstract");
				}
				moveToNextToken();
			}
			moveToNextToken();
			if(getCurrentToken().getContent().equals("class")){
				info.classModifier = new String("class");
			}else{
				info.classModifier = new String("interface");
			}
			//��������
			currentClassName = currentClassName + typeParsing();
			if(currentClassName.contains("<")){
				//������Ϣ��������Ϊandroguard��������в���������
				currentClassName = currentClassName.replaceAll("<.*>", "");
			}
			info.identifierName = new String(currentClassName);
			while(!getNextToken().getContent().equals("{")){//�����̳���Ϣ
				if(getNextToken().getContent().equals("extends")&&info.classModifier.equals("class")){
					moveToNextToken();
					info.extendsClassName = new String(getTypeFullClassName(typeParsing()));
				}else if(getNextToken().getContent().equals("implements")
						||(info.classModifier.equals("interface")&&getNextToken().getContent().equals("extends"))){
					do{
						moveToNextToken();
						//Ϊ�˷����̳й�ϵ��������Ϣ����������
						info.implementsList.add(new String(getTypeFullClassName(typeParsing())));
					}while(getNextToken().getContent().equals(","));			
				}
			}
			//��ʼ�����ⲿ������,Ƕ�ײ��+1
			result = classBlockParsing(info,nestLevel+1,0);
		}else{//�����ڵĸ�����������
			
			while(getNextToken().getType().equals(TokenType.modifier))          //�Ƚ���������η�������Լ
			{
				moveToNextToken();
				if(getCurrentToken().getContent().equals("public")
						||getCurrentToken().getContent().equals("protected")
						||getCurrentToken().getContent().equals("private")){
					
					info.visibilityModifier = getCurrentToken().getContent();
				}else if(getCurrentToken().getContent().equals("abstract")){
					info.abstractModifier = getCurrentToken().getContent();
				}else if(getCurrentToken().getContent().equals("final")){
					info.finalModifier = getCurrentToken().getContent();
				}else if(getCurrentToken().getContent().equals("static")){
					info.staticModifier = getCurrentToken().getContent();
				}else if(getCurrentToken().getContent().equals("synchronized")){
					info.synchronizedModifier = getCurrentToken().getContent();
				}else if(getCurrentToken().getContent().equals("native")){
					info.nativeModifier = getCurrentToken().getContent();
				}else if(getCurrentToken().getContent().equals("transient")){
					info.transientModifier = getCurrentToken().getContent();
				}else if(getCurrentToken().getContent().equals("volatile")){
					info.volatileModifier = getCurrentToken().getContent();
				}
			}
			if(getNextToken().getType().equals(TokenType.declareclass)){      //��Լ���ڲ�������
				moveToNextToken();
				if(getCurrentToken().getContent().equals("class")){
					info.classModifier = new String("class");
				}else{
					info.classModifier = new String("interface");
				}
				//��������
				currentClassName = currentClassName +"$"+typeParsing();
				if(currentClassName.contains("<")){
					//������Ϣ��������Ϊandroguard��������в���������
					currentClassName = currentClassName.replaceAll("<.*>", "");
				}
				info.identifierName = new String(currentClassName);
				while(!getNextToken().getContent().equals("{")){//�����̳���Ϣ
					if(getNextToken().getContent().equals("extends")&&info.classModifier.equals("class")){
						moveToNextToken();
						info.extendsClassName = new String(getTypeFullClassName(typeParsing()));
					}else if(getNextToken().getContent().equals("implements")
							||(info.classModifier.equals("interface")&&getNextToken().getContent().equals("extends"))){
						do{
							moveToNextToken();
							//Ϊ�˷����̳й�ϵ��������Ϣ����������
							info.implementsList.add(getTypeFullClassName(typeParsing()));
						}while(getNextToken().getContent().equals(","));			
					}
				}
				//��ʼ�����ⲿ������,Ƕ�ײ��+1
				result.addInnerClassModel((JavaInnerClassResultModel) classBlockParsing(info,nestLevel+1,0));	
				
			}else if(getNextToken().getContent().equals("{")){      //��Լ�������
				
				anonymousClassCount=blockParsing(nestLevel+1,anonymousClassCount);
				
			}else if(getNextToken().getType().equals(TokenType.identifier)
					||getNextToken().getType()==TokenType.typemodifier){  //��Լ������field����
//				if(getNextToken().getType().equals(TokenType.identifier)){
//					info.typeModifier = getTypeFullClassName(typeParsing());
//				}else{
//					moveToNextToken();
//					info.typeModifier = getCurrentToken().getContent();
//				}
				if(getNextToken().getType()==TokenType.typemodifier){
					info.typeModifier = typeParsing();
				}else{
					info.typeModifier = getTypeFullClassName(typeParsing());
				}
				if(getNextToken().getContent().equals("(")){  //��Լ�����췽������
					moveToNextToken();
					if(!getNextToken().getContent().equals(")")){//�в���
						do{
							if(getNextToken().getType()==TokenType.typemodifier){
								info.parameterList.add(typeParsing());
							}else{
								info.parameterList.add(getTypeFullClassName(typeParsing()));
							}
							moveToNextToken();
							moveToNextToken();
						}while(getCurrentToken().getContent().equals(","));
						
					}
					while(getNextToken().getType()!=TokenType.brace){  //���췽���������ǳ����
						moveToNextToken();
					}
					info.constructTag ="construct";
					anonymousClassCount = methodBlockParsing(info,nestLevel,anonymousClassCount);				
				}else{                                        //��Լ���ǹ��췽������		
					moveToNextToken();
					//��������
					info.identifierName = getCurrentToken().getContent();
					if(getNextToken().getContent().equals("(")){    
						moveToNextToken();
						if(!getNextToken().getContent().equals(")")){//�в���
							do{
								while(getNextToken().getType()==TokenType.modifier){
									//����ǰҲ���������η�
									moveToNextToken();
								}
								if(getNextToken().getType()==TokenType.typemodifier){
									info.parameterList.add(typeParsing());
								}else{
									info.parameterList.add(getTypeFullClassName(typeParsing()));
								}
								moveToNextToken();
								moveToNextToken();
							}while(getCurrentToken().getContent().equals(","));
							
						}
						while(getNextToken().getType()!=TokenType.brace&&!getNextToken().getContent().equals(";")){
							moveToNextToken();
						}
						if(getNextToken().getType()==TokenType.brace){   //�������abstract����
							//������������,�����п�����������ͷ����ڲ��࣬����Ҫ�������������,���������ӵ���������index
							anonymousClassCount = methodBlockParsing(info,nestLevel,anonymousClassCount);
						}else{
							//TODO:���󷽷�Ҳ
						}
					}else{     //��Լ��field����
						result.addFieldModel(fieldDelarationParsing(info));
					}
				}
			}
		}
		return anonymousClassCount;
	}
	
	/**
	 * ��Ĵ�����������ʼλ��Ϊ{ǰһ��token
	 * ����λ��Ϊ}
	 * @param nestLevel
	 * @param anonymousClassIndex  ������������࣬anonymousClassIndexȡ0������ȡ��ǰ��������count+1
	 */
	private JavaClassResultModel classBlockParsing(DelarationModifierInfo info,int nestLevel,int anonymousClassIndex){
		
		JavaClassResultModel result=null;
		//��������
		int braceCount =  0;
		//��������
		int anonymousClassCount = 0;
		if(anonymousClassIndex == 0){//����������
			if(nestLevel==1){ //��ʱ�Ѿ������������飬�����ⲿ�������Ƕ�ײ��Ϊ1���ڲ���Ϊ����1
				result = this.result;
				result.setImportList(importList);
			}else if(nestLevel>1){
				result = new JavaInnerClassResultModel();
				JavaInnerClassResultModel innerModel = (JavaInnerClassResultModel)result;
				innerModel.setIsAnonymous(false);
			}else{
				System.out.println("error: nest level should never be less than 1 in class block parsing");
			}
			result.setClassName(info.identifierName);
			result.setExtends(info.extendsClassName);
			result.setPackageName(packageName);
			result.setImplementsList(info.implementsList);
			result.setIsAbstract(info.abstractModifier!=null&&info.abstractModifier.equals("abstract"));
			result.setIsInterface(info.classModifier.equals("interface"));			
		}else{
			result = new JavaInnerClassResultModel();
			JavaInnerClassResultModel innerModel = (JavaInnerClassResultModel)result;
			innerModel.setIsAnonymous(true);
			result.setClassName(info.identifierName);
		}
		
		//����һ�µ�ǰ�����ͷ��������Ա�ݹ�����෵�غ�ָ�
		String saveClassName = new String(currentClassName);
		if(getNextToken().getContent().equals("{")){
			moveToNextToken();
			braceCount++;
		}else{
			System.out.println("error: next token should be {");
		}
		//��ʼ���������
		while(braceCount>0){
//			String debug = getCurrentToken().getContent();
			if(getNextToken().getContent().equals("}")){//�����Ҵ����ţ����������
//				System.out.println("class "+currentClassName+" parsing end");
				braceCount--;
			}else if(getNextToken().getContent().equals("{")){//���õĴ����
				//���������
				anonymousClassCount = blockParsing(nestLevel,anonymousClassCount);
				//������п�����������ͷ����ڲ��࣬���Խ�����Ҫ�ָ��ֳ�
				currentClassName = new String(saveClassName);
				//�˴��������ļ���continue����ΪĿǰ�﷨��Ԫ��������ʱ��ǰtokenΪ�˵�Ԫ���һ��token
				//���������������ǰ�ƶ��Ļ������ܻ���������һ��token
				continue;
			}else if(getNextToken().getType()==TokenType.modifier){//�����η������ܳ���field,method,������class������
				//���������������������������ȥ
				anonymousClassCount = declarationParsing(nestLevel,anonymousClassCount);
				//����������������ʲô���ָ��ֳ�
				currentClassName = new String(saveClassName);
				//�˴��������ļ���continue����ΪĿǰ�﷨��Ԫ��������ʱ��ǰtokenΪ�˵�Ԫ���һ��token
				//���������������ǰ�ƶ��Ļ������ܻ���������һ��token
				continue;
			}else if(getCurrentToken().getType()==TokenType.declarenew){//new ��һ�����󣬿��ܳ���������
				//����new���
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount);
				//������ɣ��ָ��ֳ�
				currentClassName = new String(saveClassName);
				//�˴��������ļ���continue����ΪĿǰ�﷨��Ԫ��������ʱ��ǰtokenΪ�˵�Ԫ���һ��token
				//���������������ǰ�ƶ��Ļ������ܻ���������һ��token
				continue;
			}else if(getNextToken().getType()==TokenType.identifier){
				//TODO:���������η�����������������ǳ�����������ΪĿǰfield�������ʵ����û�������
			}else if(getNextToken().getType()==TokenType.declareclass&&!getCurrentToken().getContent().equals(".")){
				//�ڲ�������ǰ���Բ������η�,ֻ��declaration�﷨�н�����©����������Ҳ�����ڲ���Ľ����߼�
				//Ҫ�ر�.class�����
				moveToNextToken();
				DelarationModifierInfo innnerInfo = new DelarationModifierInfo();
				if(getCurrentToken().getContent().equals("class")){
					innnerInfo.classModifier = new String("class");
				}else{
					innnerInfo.classModifier = new String("interface");
				}
				//��������
				currentClassName = currentClassName +"$"+typeParsing();
				if(currentClassName.contains("<")){
					//������Ϣ��������Ϊandroguard��������в���������
					currentClassName = currentClassName.replaceAll("<.*>", "");
				}
				innnerInfo.identifierName = new String(currentClassName);
				while(!getNextToken().getContent().equals("{")){//�����̳���Ϣ
					if(getNextToken().getContent().equals("extends")){
						moveToNextToken();
						innnerInfo.extendsClassName = new String(getTypeFullClassName(typeParsing()));
					}else if(getNextToken().getContent().equals("implements")){
						do{
							moveToNextToken();
							//Ϊ�˷����̳й�ϵ��������Ϣ����������
							innnerInfo.implementsList.add(new String(getTypeFullClassName(typeParsing())));
						}while(getNextToken().getContent().equals(","));			
					}
				}
				//��ʼ����������������,Ƕ�ײ��+1
				this.result.addInnerClassModel((JavaInnerClassResultModel) classBlockParsing(innnerInfo,nestLevel+1,0));
				//�˴��������ļ���continue����ΪĿǰ�﷨��Ԫ��������ʱ��ǰtokenΪ�˵�Ԫ���һ��token
				//���������������ǰ�ƶ��Ļ������ܻ���������һ��token
				continue;
			}
			moveToNextToken();
		}		
		return result;
	}
	
	private JavaFieldModel fieldDelarationParsing(DelarationModifierInfo info){
		
		JavaFieldModel result = new JavaFieldModel();
		result.setClassName(currentClassName);
		result.setTypeName(info.typeModifier);
		result.setFieldName(info.identifierName);
		result.setDeclareLineNumber(getCurrentToken().getLineNumber());
		return result;
	}
	
	/**
	 * ���������Ľ���
	 * ��ǰtokenΪ{ǰһ��token
	 * ����tokenΪ}
	 * @param info
	 * @param nestLevel
	 * @param anonymousClassIndex
	 * @return
	 */
	private int methodBlockParsing(DelarationModifierInfo info,int nestLevel,int anonymousClassIndex){
		
		int braceCount = 0;
		//������Ҫ�������������������
		int anonymousClassCount = anonymousClassIndex;
		//����һ�µ�ǰ�����ͷ��������Ա�ݹ�����෵�غ�ָ�
		String saveClassName = new String(currentClassName);
		//��ʼ��
		int beginLineNumber = getCurrentToken().getLineNumber();
		//������
		int endLineNumber = 0;
		//��ʼ��token index
		int beginTokenIndex = tokenScanCount;
		//������token index
		int endTokenIndex = 0;
		
		int catchClauseFlag = -1;
		
		int catchBeginLine = -1;
		
		moveToNextToken();
		//��ǰӦ�����������
		if(getCurrentToken().getContent().equals("{"))braceCount++;
		//��ʼ����
		while(braceCount>0){
			moveToNextToken();
			//��Ϊ������statement��������ϸ���﷨�������������򵥵ؿ���һ�´�����
			if((getCurrentToken().getContent().equals("{"))){
				braceCount++;
			}else if((getCurrentToken().getContent().equals("}"))){
				braceCount--;
				if(braceCount==catchClauseFlag){
					catchClauseFlag=-1;
//					System.out.println("find catch clause begin at line "+catchBeginLine+" end at line "+getCurrentToken().getLineNumber());
					for(int lineNum = catchBeginLine;lineNum<=getCurrentToken().getLineNumber();lineNum++){
						result.addCatchClauseLine(lineNum);
					}
					catchBeginLine = -1;
				}			
			}else if((getCurrentToken().getType()==TokenType.declarenew)){ //���������new,�Ϳ�����������
				//����new���
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount);
				//������ɣ��ָ��ֳ�
				currentClassName = new String(saveClassName);
			}else if((getCurrentToken().getContent().equals("catch"))&&catchClauseFlag==-1){
				catchClauseFlag=braceCount;
				catchBeginLine=getCurrentToken().getLineNumber();
			}
		}
		//�����������Ž�����б���
		endLineNumber = getCurrentToken().getLineNumber();
		endTokenIndex = tokenScanCount;
		JavaMethodModel mModel = new JavaMethodModel();
		mModel.setClassName(packageName+"."+currentClassName);
		if(info.constructTag!=null&&info.constructTag.equals("construct")){
			mModel.setIsConstructMethod(true);
		}else{
			mModel.setMethodName(info.identifierName);
			mModel.setReturnType(info.typeModifier);
		}
		mModel.setParameterList(info.parameterList);
		mModel.setBeginLineNumber(beginLineNumber);
		mModel.setEndLineNumber(endLineNumber);
		mModel.setBeginTokenIndex(beginTokenIndex);
		mModel.setEndTokenIndex(endTokenIndex);
		result.addMethodModel(mModel);
		
		//debug��
//		System.out.println(mModel.getFullName());
		
		return anonymousClassCount;
	}
	
	/**
	 * ����new���
	 * ������new���������ĩβ
	 * ����������࣬+1�������������
	 * @param nestLevel
	 * @param anonymousClassIndex
	 * @param saveClassName
	 * @param saveMethodName
	 * @return
	 */
	private int newStatementParsing(int nestLevel, int anonymousClassIndex){
		
		//����ֵ
		int anonymousClassCount = anonymousClassIndex;
		moveToNextToken();
		//��ʼ��Ϊnew����һ������Բ���ű�ʶ���캯���������Ǹ�λ��
		//��ʵ���ϲ�һ�������� new String[]{}���������ʼ�������������new a[n]���ֹ������
		//֮��ƥ��)��]���������е����ǲ�����
		//TODO:��ȷ�ķ�������Ӧ�ù淶�����﷨�������Ƚ������ͣ��ٿ�������,����Ժ�ʵ�� 2014.10.23
		while(!getCurrentToken().getContent().equals(")")&&!getCurrentToken().getContent().equals("]")){
			moveToNextToken();
		}
		if(getCurrentToken().getContent().equals(")")){// )��������new ����
			//�������Ҫ�ȿ��º�����ʲô����Ȼ�Ͳ��ÿ���new statement��������ʱ��λ��
	//		moveToNextToken();
			if(getNextToken().getContent().equals("{")){ //�������������ţ���˵�����ڲ���		
				DelarationModifierInfo info = new DelarationModifierInfo();
				anonymousClassCount++;
				info.identifierName =currentClassName +"$"+ anonymousClassCount;
				currentClassName = info.identifierName;
				classBlockParsing(info,nestLevel+1,anonymousClassCount);
				
			}else{ //û������������

			}
		}else{//���������ʼ��,�ƶ�����ʼ�����ĩβ,�����˴�ѭ��
			//����һ��token�������ǲ���{,���������new String[]{}���ֳ�ʼ��,��Ҫ�ƶ�����ʼ�����ĩβ	
			//�������Ҫ�ȿ��º�����ʲô����Ȼ�Ͳ��ÿ���new statement��������ʱ��λ��
	//		moveToNextToken();
			if(getNextToken().getContent().equals("{")){
				moveToNextToken();
				int tempBraceCount = 1;
				while(tempBraceCount>0){
					moveToNextToken();
					if(getCurrentToken().getContent().equals("{"))tempBraceCount++;
					else if(getCurrentToken().getContent().equals("}"))tempBraceCount--;
				}
			}
		}
		return anonymousClassCount;
	}
	
	/**
	 * ������������
	 * ��ʼλ��Ϊ{ǰһ��token
	 * ����λ��Ϊ}
	 * @param nestLevel
	 * @param anonymousClassIndex
	 * @return
	 */
	private int blockParsing(int nestLevel,int anonymousClassIndex){
		
		moveToNextToken();
		
		int braceCount = 0;
		//������Ҫ�������������������
		int anonymousClassCount = anonymousClassIndex;
		//����һ�µ�ǰ�����ͷ��������Ա�ݹ�����෵�غ�ָ�
		String saveClassName = new String(currentClassName);
		//��ǰӦ�����������
		if(getCurrentToken().getContent().equals("{"))braceCount++;
		//��ʼ����
		while(braceCount>0){
			moveToNextToken();
			//��Ϊ������statement��������ϸ���﷨�������������򵥵ؿ���һ�´�����
			if((getCurrentToken().getContent().equals("{"))){
				braceCount++;
			}else if((getCurrentToken().getContent().equals("}"))){
				braceCount--;
				
			}else if((getCurrentToken().getType()==TokenType.declarenew)){ //���������new,�Ϳ�����������
				//����new���
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount);
				//������ɣ��ָ��ֳ�
				currentClassName = new String(saveClassName);
			}
		}
		return anonymousClassCount;
	}
	
	private TokenModel getCurrentToken(){
		return tokenList.get(tokenScanCount);
	}
	
	private TokenModel getNextToken(){
		return tokenList.get(tokenScanCount+1);
	}
	
	private void moveToNextToken(){
		tokenScanCount++;
		//debug��
//		if(tokenScanCount == 16446){
//			return;
//		}
	}
	
	/**
	 * ������ʾ������
	 * ��ʼλ��Ϊ��������ʾ����ǰһ��token
	 * ��������ʱ,λ��Ϊ������ʾ��������token
	 * @return ���ؽ�����������
	 */
	private String typeParsing(){
		
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
			//TODO:��ʱ�޸���ʩ���Ը��������е�ŵ����⣬���ͽ����߼�Ҫ����
			if(getNextToken().getContent().equals(".")){
				moveToNextToken();
				
				if(getNextToken().getContent().equals(".")){//�����ĵ�Ŵ�������������
					type=type+"...";
					moveToNextToken();
					moveToNextToken();
				}else{//�����ź�Ҳ�ǽ�������
					type=type+"."+typeParsing();
				}
	//			type=type+"."+typeParsing();
			}
			
		}else if(getNextToken().getContent().equals(".")){//�������ڲ�����ShortCutCallback.Stub
//			boolean isChangablePara = false;
//			do{
//				moveToNextToken();
//				//typeParsing�ڷ����������ͽ���Ҳ���õ��������ģ��ǲ�����������,��ʵ����ڴʷ�������...��Ϊtoken��һ��
//				if(getNextToken().getContent().equals(".")){
//					isChangablePara = true;
//					break;
//				}
//				type=type+new String(getCurrentToken().getContent()); 
//				moveToNextToken();
//				type=type+new String(getCurrentToken().getContent());
//			}while(getNextToken().getContent().equals("."));			
//			if(isChangablePara){
//				type=type+"...";
//				moveToNextToken();
//				moveToNextToken();
//			}
			moveToNextToken();
			if(getNextToken().getContent().equals(".")){//�����ĵ�Ŵ�������������
				type=type+"...";
				moveToNextToken();
				moveToNextToken();
			}else{//�����ź�Ҳ�ǽ�������
				type=type+"."+typeParsing();
			}
		}

		return type;
	}
	
	/**
	 * ��������Ϣת��Ϊ��������������+������,ͬʱ��ȥ��������Ϣ
	 * @param type
	 * @return
	 */
	private String getTypeFullClassName(String type){
		//System.out.println("get full type for "+type);
		//��װ��İ���java.lang���������import��������Ҫ�ر���
		if(type.equals("String")||type.equals("StringBuffer")
				||type.equals("Integer")||type.equals("Integer[]")
				||type.equals("Short")||type.equals("Short[]")
				||type.equals("Long")||type.equals("Long[]")
				||type.equals("Float")||type.equals("Float[]")
				||type.equals("Double")||type.equals("Double[]")
				||type.equals("Character")||type.equals("Character[]")
				||type.equals("Boolean")||type.equals("Boolean[]")
				||type.equals("Void")||type.equals("CharSequence")
				||type.equals("CharSequence")||type.equals("CharSequence[]")){
			return "java.lang."+type;
		}
		//raw ����Classֱ�ӷ���
		if(type.equals("Class"))return type;
		
		String fullClassName; 
		if(!(type.startsWith("com.")||type.startsWith("java.")||type.startsWith("android."))){//ͬĿ¼�µ��಻����import�б��У�����Ĭ���ȼ��ϰ���
			//TODO:������Ϣ�ᵼ��ƥ�䲻�ϣ���ȥ�����Ժ�Ľ�
			fullClassName = new String(packageName+"."+type.replaceAll("<.*>", ""));
		}else{//TODO:com.��ͷ��ʾ�Ѿ�����������������ת�����жϵ��߼��൱�����ף��Ժ�Ľ�
			fullClassName = new String(type);
			return fullClassName;
		}
		
		//
		type = type.replace("...", "").replaceAll("<.*>", "");
		
		//����importlist��ƥ�䵽�ĸ���Ϊimport��ƥ�䵽����
		for(int i=0; i<importList.size();i++){
			String [] importItemArray= importList.get(i).split("\\.");
			int itemLength = importItemArray.length;
			String [] typeArray = type.split("\\.");
			int typeLength = typeArray.length;
			boolean flag = true;

			if(itemLength<typeLength)continue;
			for(int j=0;j<typeLength;j++){
				if(!typeArray[j].replaceAll("<.*>", "").equals(importItemArray[itemLength-typeLength+j])){
					flag=false;
					break;
				}
			}
			if(flag){
				fullClassName = new String(importList.get(i));
				break;
			}
//			if(importList.get(i).endsWith(type.replaceAll("<.*>", ""))){
//				fullClassName = new String(importList.get(i));
//				break;
//			}
		}
		
		return fullClassName;
	}
	
	private boolean isLastToken(){
		return tokenScanCount+1>=tokenList.size()?true:false;
	}
	
	private class DelarationModifierInfo{
		
		public String visibilityModifier = null;
		public String abstractModifier = null;
		public String finalModifier = null;
		public String synchronizedModifier = null;
		public String staticModifier = null;
		public String nativeModifier = null;
		public String transientModifier = null;
		public String volatileModifier = null;
		public String typeModifier = null;
		
		public String classModifier = null;
		public String extendsClassName = null;
		public List<String> implementsList = new ArrayList<String>();
		
		public String identifierName = null;
		
		public String packageName = null;
		
		public String constructTag = null;
		public List<String> parameterList = new ArrayList<String>();
	}
	
}
