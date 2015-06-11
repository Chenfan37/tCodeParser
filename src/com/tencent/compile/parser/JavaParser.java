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
 * 新的java声明语法分析器，有以下优化
 * 1：更为严格地按照递归下降分析法，改为使用getNextToken预判，再移动
 * 2：统一了类，方法，变量的声明逻辑
 * 3：支持各种声明的各种属性解析
 * 4：支持方法的参数类型解析
 * 5：优化了类型语法解析
 * @author frankcfliu
 *
 */
public class JavaParser extends AbstractParser{
	
	public static final String JAVA_PARSING_MODE = "JAVA_PARSING_MODE";
	public static final int SIMPLE_MODE = 1;
	public static final int NORMAL_MODE = 2;
	public static final int RESERVE_TOKEN_MODE = 3;

	//当前扫描到第几个token
	private int tokenScanCount=0;
	//token list
	private List<TokenModel> tokenList;
	//包名
	private String packageName = new String("");
	//import 列表 ，用来匹配类型完整名字
	private List<String> importList = new ArrayList<String>();
	//当前类名
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
	 * 语法分析工厂方法，返回分析结果
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
			//兼容全注释掉的文件，返回空的result
			if(this.tokenList.size()==0){
				if(config==null){
					return result;
				}
				return config.getAtrribute(JAVA_PARSING_MODE)!=SIMPLE_MODE?result:new JavaClassResultSimpleModel(result);
			}
			initPackageName();
			//package之后为空的情况，不检查会越界
			if(isLastToken()){
				if(config==null){
					return result;
				}
				return config.getAtrribute(JAVA_PARSING_MODE)!=SIMPLE_MODE?result:new JavaClassResultSimpleModel(result);
			}
			//临时处理逻辑，避免没有import的代码导致的问题
			if(getNextToken().getContent().equals("import")){
				initImportList();
			}
			//import之后为空的情况，不检查会越界
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
//			System.out.println("词法分析错误");
			return null;
		}
		//简单模式只有类名和继承实现关系
		if(config!=null&&config.getAtrribute(JAVA_PARSING_MODE)==SIMPLE_MODE){
			return new JavaClassResultSimpleModel(result);
		}
		
		//保留分词模式会保留解析过程中的词法分析结果
		if(config!=null&&config.getAtrribute(JAVA_PARSING_MODE)==RESERVE_TOKEN_MODE){
			
			List<TokenModel> resultTokenModel = new ArrayList<TokenModel>();
			resultTokenModel.addAll(tokenList);
			result.setTokenList(resultTokenModel);
		}
		return result;
	}
	
	/**
	 * 语法分析工厂方法，返回分析结果
	 */
	@Override
	public BaseResultModel parsing(String filepath,
			ParsingConfig config) {		
		init();
		
		JTokenizer tokenizer = new JTokenizer(filepath);
		try {
			tokenizer.DFA();
			this.tokenList = tokenizer.tokenResultList;			
			//兼容全注释掉的文件，返回空的result
			if(this.tokenList.size()==0){
				return config.getAtrribute(JAVA_PARSING_MODE)!=SIMPLE_MODE?result:new JavaClassResultSimpleModel(result);
			}
			initPackageName();
			//临时处理逻辑，避免没有import的代码导致的问题
			if(getNextToken().getContent().equals("import")){
				initImportList();
			}
			declarationParsing(0,0);
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());		
		} catch (IOException e) {
			e.printStackTrace();
//			System.out.println("in file "+fileName);
//			System.out.println("词法分析错误");
			return null;
		}
		//简单模式只有类名和继承实现关系
		if(config!=null&&config.getAtrribute(JAVA_PARSING_MODE)==SIMPLE_MODE){
			return new JavaClassResultSimpleModel(result);
		}
		
		//保留分词模式会保留解析过程中的词法分析结果
		if(config!=null&&config.getAtrribute(JAVA_PARSING_MODE)==RESERVE_TOKEN_MODE){
			
			List<TokenModel> resultTokenModel = new ArrayList<TokenModel>();
			resultTokenModel.addAll(tokenList);
			result.setTokenList(resultTokenModel);
		}
		return result;
	}
	
	/**
	 * 解析import 列表
	 * 设置抽象类和接口标志的逻辑也暂时放在这里
	 * 起始位置为import语句开始的前一个token
	 * 并移动到最外层类开始之处
	 */
	private void initImportList(){
		do{
			//typeParsing()结束后当前的token为分号所以要向前移动一个
			if(getNextToken().getContent().equals("import")){
				moveToNextToken();
				if(getNextToken().getContent().equals("static")){
					moveToNextToken();
				}
				importList.add(typeParsing());
				//移动到分号
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
	 * 初始化包名
	 * 移动到package声明结束之处
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
		//移动到class声明处
//		while(!getCurrentToken().getType().equals(TokenType.declareclass)){
//			moveToNextToken();
//		}
	}
	
	/**
	 * 各种声明的解析，包括本类的声明
	 * 当前token为declaration前一个token
	 * 当探测到下一个token命中声明语法时执行此逻辑
	 * 解析声明时不需要增加嵌套层次，等真正进入声明的代码块时再加1
	 * 返回结束后当前类嵌套层次的匿名类count
	 * @param declarationLevel
	 */
	private int declarationParsing(int nestLevel,int anonymousClassIndex){
		
		DelarationModifierInfo info = new DelarationModifierInfo();
		
		//方法里要继续计算匿名类的数量
		int anonymousClassCount = anonymousClassIndex;
		
		if(nestLevel == 0){//嵌套层级为0，是类声明
			info.visibilityModifier = new String("public");      //非内部类一定是public
			while(!getNextToken().getType().equals(TokenType.declareclass)){//声明标识符（class/interface）之前
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
			//解析类名
			currentClassName = currentClassName + typeParsing();
			if(currentClassName.contains("<")){
				//容器信息丢掉，因为androguard分析结果中不带。。。
				currentClassName = currentClassName.replaceAll("<.*>", "");
			}
			info.identifierName = new String(currentClassName);
			while(!getNextToken().getContent().equals("{")){//解析继承信息
				if(getNextToken().getContent().equals("extends")&&info.classModifier.equals("class")){
					moveToNextToken();
					info.extendsClassName = new String(getTypeFullClassName(typeParsing()));
				}else if(getNextToken().getContent().equals("implements")
						||(info.classModifier.equals("interface")&&getNextToken().getContent().equals("extends"))){
					do{
						moveToNextToken();
						//为了分析继承关系，容器信息丢掉。。。
						info.implementsList.add(new String(getTypeFullClassName(typeParsing())));
					}while(getNextToken().getContent().equals(","));			
				}
			}
			//开始解析外部类代码块,嵌套层次+1
			result = classBlockParsing(info,nestLevel+1,0);
		}else{//本类内的各种声明解析
			
			while(getNextToken().getType().equals(TokenType.modifier))          //先解析完成修饰符。不归约
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
			if(getNextToken().getType().equals(TokenType.declareclass)){      //归约到内部类声明
				moveToNextToken();
				if(getCurrentToken().getContent().equals("class")){
					info.classModifier = new String("class");
				}else{
					info.classModifier = new String("interface");
				}
				//解析类名
				currentClassName = currentClassName +"$"+typeParsing();
				if(currentClassName.contains("<")){
					//容器信息丢掉，因为androguard分析结果中不带。。。
					currentClassName = currentClassName.replaceAll("<.*>", "");
				}
				info.identifierName = new String(currentClassName);
				while(!getNextToken().getContent().equals("{")){//解析继承信息
					if(getNextToken().getContent().equals("extends")&&info.classModifier.equals("class")){
						moveToNextToken();
						info.extendsClassName = new String(getTypeFullClassName(typeParsing()));
					}else if(getNextToken().getContent().equals("implements")
							||(info.classModifier.equals("interface")&&getNextToken().getContent().equals("extends"))){
						do{
							moveToNextToken();
							//为了分析继承关系，容器信息丢掉。。。
							info.implementsList.add(getTypeFullClassName(typeParsing()));
						}while(getNextToken().getContent().equals(","));			
					}
				}
				//开始解析外部类代码块,嵌套层次+1
				result.addInnerClassModel((JavaInnerClassResultModel) classBlockParsing(info,nestLevel+1,0));	
				
			}else if(getNextToken().getContent().equals("{")){      //归约到代码块
				
				anonymousClassCount=blockParsing(nestLevel+1,anonymousClassCount);
				
			}else if(getNextToken().getType().equals(TokenType.identifier)
					||getNextToken().getType()==TokenType.typemodifier){  //归约方法或field声明
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
				if(getNextToken().getContent().equals("(")){  //归约到构造方法声明
					moveToNextToken();
					if(!getNextToken().getContent().equals(")")){//有参数
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
					while(getNextToken().getType()!=TokenType.brace){  //构造方法不可能是抽象的
						moveToNextToken();
					}
					info.constructTag ="construct";
					anonymousClassCount = methodBlockParsing(info,nestLevel,anonymousClassCount);				
				}else{                                        //归约到非构造方法声明		
					moveToNextToken();
					//方法名字
					info.identifierName = getCurrentToken().getContent();
					if(getNextToken().getContent().equals("(")){    
						moveToNextToken();
						if(!getNextToken().getContent().equals(")")){//有参数
							do{
								while(getNextToken().getType()==TokenType.modifier){
									//参数前也可能有修饰符
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
						if(getNextToken().getType()==TokenType.brace){   //如果不是abstract方法
							//解析方法代码,方法中可能有匿名类和方法内部类，所以要带入类解析参数,返回新增加到的匿名类index
							anonymousClassCount = methodBlockParsing(info,nestLevel,anonymousClassCount);
						}else{
							//TODO:抽象方法也
						}
					}else{     //归约到field声明
						result.addFieldModel(fieldDelarationParsing(info));
					}
				}
			}
		}
		return anonymousClassCount;
	}
	
	/**
	 * 类的代码块解析，起始位置为{前一个token
	 * 结束位置为}
	 * @param nestLevel
	 * @param anonymousClassIndex  如果不是匿名类，anonymousClassIndex取0，否则取当前的匿名类count+1
	 */
	private JavaClassResultModel classBlockParsing(DelarationModifierInfo info,int nestLevel,int anonymousClassIndex){
		
		JavaClassResultModel result=null;
		//大括号数
		int braceCount =  0;
		//匿名类数
		int anonymousClassCount = 0;
		if(anonymousClassIndex == 0){//不是匿名类
			if(nestLevel==1){ //此时已经进入了类代码块，所以外部类代码块的嵌套层次为1，内部类为大于1
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
		
		//保存一下当前类名和方法名，以便递归分析类返回后恢复
		String saveClassName = new String(currentClassName);
		if(getNextToken().getContent().equals("{")){
			moveToNextToken();
			braceCount++;
		}else{
			System.out.println("error: next token should be {");
		}
		//开始解析类代码
		while(braceCount>0){
//			String debug = getCurrentToken().getContent();
			if(getNextToken().getContent().equals("}")){//遇到右大括号，解析完成啦
//				System.out.println("class "+currentClassName+" parsing end");
				braceCount--;
			}else if(getNextToken().getContent().equals("{")){//你妹的代码块
				//解析代码块
				anonymousClassCount = blockParsing(nestLevel,anonymousClassCount);
				//代码块中可能有匿名类和方法内部类，所以结束后要恢复现场
				currentClassName = new String(saveClassName);
				//此处和其他的几个continue是因为目前语法单元解析结束时当前token为此单元最后一个token
				//如果解析结束再向前移动的话，可能会错误地跳过一个token
				continue;
			}else if(getNextToken().getType()==TokenType.modifier){//是修饰符，可能出现field,method,代码块或class的声明
				//解析声明，把匿名类计数器带下去
				anonymousClassCount = declarationParsing(nestLevel,anonymousClassCount);
				//解析结束，不管是什么都恢复现场
				currentClassName = new String(saveClassName);
				//此处和其他的几个continue是因为目前语法单元解析结束时当前token为此单元最后一个token
				//如果解析结束再向前移动的话，可能会错误地跳过一个token
				continue;
			}else if(getCurrentToken().getType()==TokenType.declarenew){//new 了一个对象，可能出现匿名类
				//解析new语句
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount);
				//解析完成，恢复现场
				currentClassName = new String(saveClassName);
				//此处和其他的几个continue是因为目前语法单元解析结束时当前token为此单元最后一个token
				//如果解析结束再向前移动的话，可能会错误地跳过一个token
				continue;
			}else if(getNextToken().getType()==TokenType.identifier){
				//TODO:对于无修饰符的声明解析，这个非常不好做，因为目前field声明语句实际上没解析完成
			}else if(getNextToken().getType()==TokenType.declareclass&&!getCurrentToken().getContent().equals(".")){
				//内部类声明前可以不带修饰符,只在declaration语法中解析会漏，所以这里也加上内部类的解析逻辑
				//要回避.class的情况
				moveToNextToken();
				DelarationModifierInfo innnerInfo = new DelarationModifierInfo();
				if(getCurrentToken().getContent().equals("class")){
					innnerInfo.classModifier = new String("class");
				}else{
					innnerInfo.classModifier = new String("interface");
				}
				//解析类名
				currentClassName = currentClassName +"$"+typeParsing();
				if(currentClassName.contains("<")){
					//容器信息丢掉，因为androguard分析结果中不带。。。
					currentClassName = currentClassName.replaceAll("<.*>", "");
				}
				innnerInfo.identifierName = new String(currentClassName);
				while(!getNextToken().getContent().equals("{")){//解析继承信息
					if(getNextToken().getContent().equals("extends")){
						moveToNextToken();
						innnerInfo.extendsClassName = new String(getTypeFullClassName(typeParsing()));
					}else if(getNextToken().getContent().equals("implements")){
						do{
							moveToNextToken();
							//为了分析继承关系，容器信息丢掉。。。
							innnerInfo.implementsList.add(new String(getTypeFullClassName(typeParsing())));
						}while(getNextToken().getContent().equals(","));			
					}
				}
				//开始解析非匿名类代码块,嵌套层次+1
				this.result.addInnerClassModel((JavaInnerClassResultModel) classBlockParsing(innnerInfo,nestLevel+1,0));
				//此处和其他的几个continue是因为目前语法单元解析结束时当前token为此单元最后一个token
				//如果解析结束再向前移动的话，可能会错误地跳过一个token
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
	 * 方法代码块的解析
	 * 当前token为{前一个token
	 * 结束token为}
	 * @param info
	 * @param nestLevel
	 * @param anonymousClassIndex
	 * @return
	 */
	private int methodBlockParsing(DelarationModifierInfo info,int nestLevel,int anonymousClassIndex){
		
		int braceCount = 0;
		//方法里要继续计算匿名类的数量
		int anonymousClassCount = anonymousClassIndex;
		//保存一下当前类名和方法名，以便递归分析类返回后恢复
		String saveClassName = new String(currentClassName);
		//起始行
		int beginLineNumber = getCurrentToken().getLineNumber();
		//结束行
		int endLineNumber = 0;
		//起始的token index
		int beginTokenIndex = tokenScanCount;
		//结束的token index
		int endTokenIndex = 0;
		
		int catchClauseFlag = -1;
		
		int catchBeginLine = -1;
		
		moveToNextToken();
		//当前应该是左大括号
		if(getCurrentToken().getContent().equals("{"))braceCount++;
		//开始解析
		while(braceCount>0){
			moveToNextToken();
			//因为不解析statement等其他更细的语法树，所以这样简单地控制一下大括号
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
			}else if((getCurrentToken().getType()==TokenType.declarenew)){ //如果出现了new,就可能有匿名类
				//解析new语句
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount);
				//解析完成，恢复现场
				currentClassName = new String(saveClassName);
			}else if((getCurrentToken().getContent().equals("catch"))&&catchClauseFlag==-1){
				catchClauseFlag=braceCount;
				catchBeginLine=getCurrentToken().getLineNumber();
			}
		}
		//分析玩啦，放进结果列表里
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
		
		//debug用
//		System.out.println(mModel.getFullName());
		
		return anonymousClassCount;
	}
	
	/**
	 * 解析new语句
	 * 解析到new后面的语句的末尾
	 * 如果有匿名类，+1返回匿名类计数
	 * @param nestLevel
	 * @param anonymousClassIndex
	 * @param saveClassName
	 * @param saveMethodName
	 * @return
	 */
	private int newStatementParsing(int nestLevel, int anonymousClassIndex){
		
		//返回值
		int anonymousClassCount = anonymousClassIndex;
		moveToNextToken();
		//开始认为new后面一定会有圆括号标识构造函数，跳到那个位置
		//但实际上不一定，会有 new String[]{}这种数组初始化的情况，还有new a[n]这种狗逼情况
		//之后匹配)和]，可以运行但还是不合理
		//TODO:正确的方法还是应该规范按照语法树来，先解析类型，再考虑其他,这个稍后实现 2014.10.23
		while(!getCurrentToken().getContent().equals(")")&&!getCurrentToken().getContent().equals("]")){
			moveToNextToken();
		}
		if(getCurrentToken().getContent().equals(")")){// )表明不是new 数组
			//这里必须要先看下后面是什么，不然就不好控制new statement解析结束时的位置
	//		moveToNextToken();
			if(getNextToken().getContent().equals("{")){ //如果跟了左大括号，就说明有内部类		
				DelarationModifierInfo info = new DelarationModifierInfo();
				anonymousClassCount++;
				info.identifierName =currentClassName +"$"+ anonymousClassCount;
				currentClassName = info.identifierName;
				classBlockParsing(info,nestLevel+1,anonymousClassCount);
				
			}else{ //没有声明匿名类

			}
		}else{//这是数组初始化,移动到初始化块的末尾,结束此次循环
			//看下一个token，看下是不是{,如果是则是new String[]{}这种初始化,需要移动到初始化块的末尾	
			//这里必须要先看下后面是什么，不然就不好控制new statement解析结束时的位置
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
	 * 其他代码块解析
	 * 起始位置为{前一个token
	 * 结束位置为}
	 * @param nestLevel
	 * @param anonymousClassIndex
	 * @return
	 */
	private int blockParsing(int nestLevel,int anonymousClassIndex){
		
		moveToNextToken();
		
		int braceCount = 0;
		//方法里要继续计算匿名类的数量
		int anonymousClassCount = anonymousClassIndex;
		//保存一下当前类名和方法名，以便递归分析类返回后恢复
		String saveClassName = new String(currentClassName);
		//当前应该是左大括号
		if(getCurrentToken().getContent().equals("{"))braceCount++;
		//开始解析
		while(braceCount>0){
			moveToNextToken();
			//因为不解析statement等其他更细的语法树，所以这样简单地控制一下大括号
			if((getCurrentToken().getContent().equals("{"))){
				braceCount++;
			}else if((getCurrentToken().getContent().equals("}"))){
				braceCount--;
				
			}else if((getCurrentToken().getType()==TokenType.declarenew)){ //如果出现了new,就可能有匿名类
				//解析new语句
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount);
				//解析完成，恢复现场
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
		//debug用
//		if(tokenScanCount == 16446){
//			return;
//		}
	}
	
	/**
	 * 类型提示符解析
	 * 起始位置为此类型提示符的前一个token
	 * 解析结束时,位置为类型提示符结束的token
	 * @return 返回解析出的类型
	 */
	private String typeParsing(){
		
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
			//TODO:临时修复措施，对付容器后还有点号的问题，类型解析逻辑要修正
			if(getNextToken().getContent().equals(".")){
				moveToNextToken();
				
				if(getNextToken().getContent().equals(".")){//连续的点号代表不定参数类型
					type=type+"...";
					moveToNextToken();
					moveToNextToken();
				}else{//否则点号后也是解析类型
					type=type+"."+typeParsing();
				}
	//			type=type+"."+typeParsing();
			}
			
		}else if(getNextToken().getContent().equals(".")){//处理公共内部类如ShortCutCallback.Stub
//			boolean isChangablePara = false;
//			do{
//				moveToNextToken();
//				//typeParsing在方法参数类型解析也会用到，连续的，是不定参数类型,其实最好在词法分析把...作为token的一种
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
			if(getNextToken().getContent().equals(".")){//连续的点号代表不定参数类型
				type=type+"...";
				moveToNextToken();
				moveToNextToken();
			}else{//否则点号后也是解析类型
				type=type+"."+typeParsing();
			}
		}

		return type;
	}
	
	/**
	 * 将类型信息转化为完整类名（包名+类名）,同时会去掉容器信息
	 * @param type
	 * @return
	 */
	private String getTypeFullClassName(String type){
		//System.out.println("get full type for "+type);
		//封装类的包名java.lang不会出现在import里，这种情况要特别处理
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
		//raw 类型Class直接返回
		if(type.equals("Class"))return type;
		
		String fullClassName; 
		if(!(type.startsWith("com.")||type.startsWith("java.")||type.startsWith("android."))){//同目录下的类不会在import列表中，所以默认先加上包名
			//TODO:容器信息会导致匹配不上，先去掉，以后改进
			fullClassName = new String(packageName+"."+type.replaceAll("<.*>", ""));
		}else{//TODO:com.开头表示已经是完整类名，不用转化，判断的逻辑相当不靠谱，以后改进
			fullClassName = new String(type);
			return fullClassName;
		}
		
		//
		type = type.replace("...", "").replaceAll("<.*>", "");
		
		//能在importlist中匹配到的更换为import中匹配到的类
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
