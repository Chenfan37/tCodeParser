package com.tencent.javacompile.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.tencent.compile.models.TokenModel;
import com.tencent.compile.models.TokenModel.TokenType;
import com.tencent.compile.tokenizer.JTokenizer;
import com.tencent.javacompile.models.FieldModel;
import com.tencent.javacompile.models.MethodModel;

/**
 * 根据java词法分析器（伪）的结果，执行语法分析
 * 采用语法树分析方法（大雾）
 * 按.class文件中的格式输出所有方法和其所在的行数
 * @author frankcfliu
 *
 */
public class JParser {

	//当前扫描到第几个token
	private int tokenScanCount=0;
	//token list
	private List<TokenModel> tokenList;
	//方法list
	private List<MethodModel> methodList = new ArrayList<MethodModel>();
	//field list
	private List<FieldModel> fieldList = new ArrayList<FieldModel>();
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

	public JParser(List<TokenModel> list){
		
		this.tokenList = list;
//		resultList = new ArrayList<MethodModel>();
		tokenScanCount = 0;
		initPackageName();
		//兼容全注释掉的文件
		if(isLastToken())return;
		classParsing(0,0);
//		for(int i=0;i<50;i++)
//			System.out.println(tokenList.get(500+i).getContent());
	}
	
	public JParser(String fileName){
		JTokenizer tokenizer = new JTokenizer(fileName);
		//JTokenizer tokenizer = new JTokenizer("AppDetailActivity.java");
		try {
			tokenizer.DFA();
			this.tokenList = tokenizer.tokenResultList;
			initPackageName();
			initImportList();
			//兼容全注释掉的文件
			if(isLastToken())return;
			classParsing(0,0);
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());		
			this.tokenList = null;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("in file "+fileName);
			System.out.println("词法分析错误");
			return;
		}
	}
	
	/**
	 * 兼容sharon的输出
	 * @param outStream
	 */
	public JParser(ByteArrayOutputStream outStream){
		JTokenizer tokenizer = new JTokenizer(outStream);
		//JTokenizer tokenizer = new JTokenizer("AppDetailActivity.java");
		try {
			tokenizer.DFA();
			this.tokenList = tokenizer.tokenResultList;
			initPackageName();
			//兼容全注释掉的文件
			if(isLastToken())return;
			initImportList();
			classParsing(0,0);
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());		
			//解析结束就不要留token的引用了，占很多内存
			this.tokenList = null;
		} catch (IOException e) {
			e.printStackTrace();
//			System.out.println("in file "+fileName);
//			System.out.println("词法分析错误");
			return;
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
	 * 初始化import 列表
	 * 并移动到最外层类开始之处
	 */
	private void initImportList(){
		do{
			moveToNextToken();
			if(getCurrentToken().getContent().equals("import")){
				moveToNextToken();
				importList.add(typeParsing());
			}else{
				break;
			}
		}while(getCurrentToken().getContent().equals(";"));
		
		while(!getCurrentToken().getType().equals(TokenType.declareclass)){
//			System.out.println(getCurrentToken().getContent());
//			System.out.println(getCurrentToken().getType());
			moveToNextToken();
		}
	}
	
	private TokenModel getCurrentToken(){
		return tokenList.get(tokenScanCount);
	}
	
	/**
	 * 语法分析的标准方式还是应该向前先看一下，再决定是否往下移，目前全都是直接移过去，很不科学2014.10.23
	 * TODO:要改得地方很多，慢慢来吧2014.10.23
	 */
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
	
	private boolean isLastToken(){
		return tokenScanCount+1>=tokenList.size()?true:false;
	}
	
	/**
	 * 类解析，当前token 为class或interface，或匿名类大括号
	 * 解析完成时的当前token为此类结束时的右大括号
	 * @param nestLevel 嵌套层级
	 * @param anonymousClassIndex 为0表示不是匿名类
	 */
	private void classParsing(int nestLevel,int anonymousClassIndex){
		//大括号数
		int braceCount =  0;
		//匿名类数
		int anonymousClassCount = 0;
		//解析类名
		if(anonymousClassIndex == 0){
			//移动到类名
			moveToNextToken();
			//外部类直接上class name, 内部类加$符号
			if(nestLevel==0){
				currentClassName = currentClassName + typeParsing();
				if(currentClassName.contains("<")){
					//容器信息丢掉，因为androguard分析结果中不带。。。
					currentClassName = currentClassName.replaceAll("<.*>", "");
				}
			}else{
				currentClassName = currentClassName +"$"+getCurrentToken().getContent();
			}
		}else{
			currentClassName = currentClassName +"$"+ anonymousClassIndex;
		}
		//保存一下当前类名和方法名，以便递归分析类返回后恢复
		String saveClassName = new String(currentClassName);
//		System.out.println("current class is "+currentClassName);
		
		if(nestLevel == 0){//外部类解析继承和实现信息	
			//moveToNextToken();
			//移动到下一个左大括号，并解析中间关于继承和实现的描述
			while(!getCurrentToken().getContent().equals("{")){	
				if(getCurrentToken().getContent().equals("extends")){ //java 只允许单继承
					moveToNextToken();
					//为了分析继承关系，容器信息丢掉。。。
					extendsClassName = getTypeFullClassName(typeParsing());
				}else if(getCurrentToken().getContent().equals("implements")){ // 但可以实现多个接口
					do{
						moveToNextToken();
						//为了分析继承关系，容器信息丢掉。。。
						implementsList.add(new String(getTypeFullClassName(typeParsing())));
					}while(getCurrentToken().getContent().equals(","));
				}else { //如果独到其他，比如注释，这里要向前移动，不然会死循环
					moveToNextToken();
				}
			}
		}else{//内部类不解析继承和实现信息
			//移动到下一个左大括号，此为类代码起始位置，跳过中间关于继承和实现的描述
			while(!getCurrentToken().getContent().equals("{")){
				moveToNextToken();
			}
		}
		braceCount++;
		//开始解析类代码
		while(braceCount>0){
			moveToNextToken();
//			String debug = getCurrentToken().getContent();
			if(getCurrentToken().getContent().equals("}")){//遇到右大括号，解析完成啦
//				System.out.println("class "+currentClassName+" parsing end");
				braceCount--;
			}else if(getCurrentToken().getContent().equals("{")){//你妹的代码块
				//解析代码块
				anonymousClassCount = blockParsing(nestLevel,anonymousClassCount);
				//代码块中可能有匿名类和方法内部类，所以结束后要恢复现场
				currentClassName = new String(saveClassName);
			}else if(getCurrentToken().getType()==TokenType.modifier){//是修饰符，可能出现field,method,代码块或class的定义
				moveToNextToken();
				//跳过其他修饰符
				while(getCurrentToken().getType()==TokenType.modifier){
					moveToNextToken();
				}
				if(getCurrentToken().getContent().equals("{")){//你妹的代码块
					//解析代码块
					anonymousClassCount = blockParsing(nestLevel,anonymousClassCount);
					//代码块中可能有匿名类和方法内部类，所以结束后要恢复现场
					currentClassName = new String(saveClassName);
				}else if(getCurrentToken().getType()==TokenType.declareclass){ //是内部类的定义，递归分析之			
					classParsing(nestLevel+1,0);
					//递归结束恢复现场
					currentClassName = new String(saveClassName);
				}else if(getCurrentToken().getType()==TokenType.identifier||getCurrentToken().getType()==TokenType.typemodifier){ //是类型修饰符，可能是field或method的定义
					String type;			
					if(getCurrentToken().getType()==TokenType.identifier){//如果不是基础类型，解析下类型提示符，兼容数组和容器类型
						type =getTypeFullClassName(typeParsing()) ;
					}else{//基础类型就不要做类型解析了
						type = getCurrentToken().getContent();
						moveToNextToken();
					}
//					System.out.println(type);
					//先存下名字
					String tempName = new String(getCurrentToken().getContent());
					moveToNextToken();
					if(getCurrentToken().getContent().equals("(")){ //左括号说明是方法定义，进入方法解析状态
						//当前方法名
						currentMethodName = tempName;
						//移动到函数代码开始的左大括号
						//abstract方法的定义，没有大括号，这里要做处理, abstract 方法定义不记录
						while(getCurrentToken().getType()!=TokenType.brace&&!getCurrentToken().getContent().equals(";")){
							moveToNextToken();
						}
						if(getCurrentToken().getType()==TokenType.brace){   //如果不是abstract方法
							//解析方法代码,方法中可能有匿名类和方法内部类，所以要带入类解析参数,返回新增加到的匿名类index
							anonymousClassCount = methodParsing(nestLevel,anonymousClassCount);
							//方法中可能有匿名类和方法内部类，所以结束后要恢复现场
							currentClassName = new String(saveClassName);
						}
						
//						if(currentMethodName.equals("setHolderViewVisible")){ //debug用
//							System.out.print("");
//						}
											
					}else if(!tempName.equals("(")){  //是field定义，存入field表，tempName拿到括号说明是构造函数
						FieldModel model= new FieldModel();
						model.setClassName(currentClassName);
						//field类型存完整类名
						model.setTypeName(type);
						model.setFieldName(tempName);
						model.setDeclareLineNumber(getCurrentToken().getLineNumber());
						fieldList.add(model);
					}		
				}
			}else if(getCurrentToken().getType()==TokenType.declarenew){//new 了一个对象，可能出现匿名类
				//解析new语句
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount,saveClassName,null);
				//解析完成，恢复现场
				currentClassName = new String(saveClassName);
			}
		}		
	}
	
	/**
	 * 类型提示符解析
	 * 起始位置为此类型提示符的第一个token
	 * 解析结束时,位置为类型提示符结束的下一个token
	 * @return 返回解析出的类型
	 */
	private String typeParsing(){
		String type = new String(getCurrentToken().getContent());
		moveToNextToken();	
		if(getCurrentToken().getContent().equals("[")){//处理数组类型
			do{
				type=type+new String(getCurrentToken().getContent());
				moveToNextToken();
			}while(!getCurrentToken().getContent().equals("]"));
			type=type+new String(getCurrentToken().getContent());
			moveToNextToken();	
		}else if(getCurrentToken().getContent().equals("<")){//处理容器类型，递归一下实现嵌套的类型表达式解析
			do{
				//type=type+new String(getCurrentToken().getContent());
				//moveToNextToken();
				
				type = type+typeParsing();
				while(getCurrentToken().getContent().equals(",")){//<T,P>模版类啊。。坑死T_T
					type = type +",";
					moveToNextToken();
					type = type+typeParsing();
				}
			}while(!getCurrentToken().getContent().equals(">"));
			type=type+new String(getCurrentToken().getContent());
			moveToNextToken();	
		}else if(getCurrentToken().getContent().equals(".")){//处理公共内部类如ShortCutCallback.Stub
			do{
				type=type+new String(getCurrentToken().getContent()); //TODO:.号后面的应该也是解析类型，不能简单地拼上下一个token，待实现
				moveToNextToken();
				type=type+new String(getCurrentToken().getContent());
				moveToNextToken();
			}while(getCurrentToken().getContent().equals("."));
		}
		return type;
	}
	
	
	/**
	 * block解析，当前token应为左大括号
	 * 解析结束时当前token为此block结束的右大括号
	 * @param nestLevel
	 * @param anonymousClassIndex
	 * @return
	 */
	private int blockParsing(int nestLevel,int anonymousClassIndex){ //你妹的，方法只是代码块的一种，还有三种代码块
		int braceCount = 0;
		//方法里要继续计算匿名类的数量
		int anonymousClassCount = anonymousClassIndex;
		//保存一下当前类名和方法名，以便递归分析类返回后恢复
		String saveClassName = new String(currentClassName);
		//存一下方法名
		String saveMethodName = new String(currentMethodName);
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
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount,saveClassName,saveMethodName);
				//解析完成，恢复现场
				currentClassName = new String(saveClassName);
				//解析完成，恢复现场
				currentMethodName = new String(saveMethodName);
			}
		}
		return anonymousClassCount;
	}
	
	/**
	 * 方法解析，当前token应为左大括号
	 * 解析结束时当前token为此方法结束的右大括号
	 * 要返回方法分析以后新增加的匿名类index
	 * @param nestLevel
	 * @param anonymousClassIndex
	 */
	private int methodParsing(int nestLevel,int anonymousClassIndex){
		int braceCount = 0;
		//方法里要继续计算匿名类的数量
		int anonymousClassCount = anonymousClassIndex;
		//保存一下当前类名和方法名，以便递归分析类返回后恢复
		String saveClassName = new String(currentClassName);
		//存一下方法名
		String saveMethodName = new String(currentMethodName);
		//起始行
		int beginLineNumber = getCurrentToken().getLineNumber();
		//结束行
		int endLineNumber = 0;
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
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount,saveClassName,saveMethodName);
				//解析完成，恢复现场
				currentClassName = new String(saveClassName);
				//解析完成，恢复现场
				currentMethodName = new String(saveMethodName);
			}
		}
		//分析玩啦，放进结果列表里
		endLineNumber = getCurrentToken().getLineNumber();
		MethodModel mModel = new MethodModel();
		mModel.setClassName(packageName+"."+currentClassName);
		mModel.setMethodName(currentMethodName);
		mModel.setBeginLineNumber(beginLineNumber);
		mModel.setEndLineNumber(endLineNumber);
		methodList.add(mModel);
		
		//debug用
//		System.out.println(mModel.getFullName());
//		if(currentMethodName.equals("commitRestore")){
//			System.out.println(mModel.getEndLineNumber());
//		}
		
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
	private int newStatementParsing(int nestLevel, int anonymousClassIndex,String saveClassName,String saveMethodName){
		
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
				moveToNextToken();
				classParsing(nestLevel+1,anonymousClassCount+1);
				anonymousClassCount++;		
			}else{ //不是匿名类

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
	
	//将类型信息转化为完整类名（包名+类名）,同时会去掉容器信息
	private String getTypeFullClassName(String type){
		
		//String 的包名java.lang不会出现在import里，这种情况要特别处理
		if(type.equals("String")||type.equals("StringBuffer")||type.equals("String[]")||type.equals("StringBuffer[]")){
			return "java.lang."+type;
		}
		
		String fullClassName; 
		if(!(type.startsWith("com.")||type.startsWith("java.")||type.startsWith("android."))){//同目录下的类不会在import列表中，所以默认先加上包名
			//TODO:容器信息会导致匹配不上，先去掉，以后改进
			fullClassName = new String(packageName+"."+type.replaceAll("<.*>", ""));
		}else{//TODO:com.开头表示已经是完整类名，不用转化，判断的逻辑相当不靠谱，以后改进
			fullClassName = new String(type);
			return fullClassName;
		}
		//能在importlist中匹配到的更换为匹配到
		for(int i=0; i<importList.size();i++){
			if(importList.get(i).endsWith(type.replaceAll("<.*>", ""))){
				fullClassName = new String(importList.get(i));
				break;
			}
		}
		return fullClassName;
	}
	
	public List<MethodModel> getMethodList(){
		return this.methodList;
	}
	
	public List<FieldModel> getFieldList(){
		return this.fieldList;
	}
	
	/**
	 * 用|分隔的域的类型列表
	 * @return
	 */
	public String getFieldClassNameStringList(){
		String result = new String("");
		if(this.fieldList==null)return null;
		//用集合去重
		Set<String> fieldTypeSet = new HashSet<String>();
		for(FieldModel fieldModel : this.fieldList){
			fieldTypeSet.add(fieldModel.getTypeName());
		}
		Iterator<String> iter =  fieldTypeSet.iterator();
		while(iter.hasNext()){
			result+=iter.next()+" | ";
		}
		if(result.endsWith(" | ")){
			result = result.substring(0, result.length()-3);
		}
		return result;
	}
	
	public List<String> getImportList(){
		return this.importList;
	}
	
	public String getExtends(){
		return this.extendsClassName;
	}
	
	public List<String> getImplementsList(){
		return this.implementsList;
	}
	
	/**
	 * 用|分隔的implement list
	 * @return
	 */
	public String getImplementsStringList(){
		String result = new String("");
		if(this.implementsList==null)return null;
		for(String str : this.implementsList){
			result+=str+" | ";
		}
		if(result.endsWith(" | ")){
			result = result.substring(0, result.length()-3);
		}
		return result;
	}
	
	public String getClassNameInSlash(){
		String className = new String(packageName+"/"+currentClassName);
		return className.replace('.', '/');
	}
	
	public String getClassName(){
		String className = new String(packageName+"."+currentClassName);
		return className;
	}
}
