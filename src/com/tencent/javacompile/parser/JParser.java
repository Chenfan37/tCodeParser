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
 * ����java�ʷ���������α���Ľ����ִ���﷨����
 * �����﷨����������������
 * ��.class�ļ��еĸ�ʽ������з����������ڵ�����
 * @author frankcfliu
 *
 */
public class JParser {

	//��ǰɨ�赽�ڼ���token
	private int tokenScanCount=0;
	//token list
	private List<TokenModel> tokenList;
	//����list
	private List<MethodModel> methodList = new ArrayList<MethodModel>();
	//field list
	private List<FieldModel> fieldList = new ArrayList<FieldModel>();
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

	public JParser(List<TokenModel> list){
		
		this.tokenList = list;
//		resultList = new ArrayList<MethodModel>();
		tokenScanCount = 0;
		initPackageName();
		//����ȫע�͵����ļ�
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
			//����ȫע�͵����ļ�
			if(isLastToken())return;
			classParsing(0,0);
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());		
			this.tokenList = null;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("in file "+fileName);
			System.out.println("�ʷ���������");
			return;
		}
	}
	
	/**
	 * ����sharon�����
	 * @param outStream
	 */
	public JParser(ByteArrayOutputStream outStream){
		JTokenizer tokenizer = new JTokenizer(outStream);
		//JTokenizer tokenizer = new JTokenizer("AppDetailActivity.java");
		try {
			tokenizer.DFA();
			this.tokenList = tokenizer.tokenResultList;
			initPackageName();
			//����ȫע�͵����ļ�
			if(isLastToken())return;
			initImportList();
			classParsing(0,0);
//			for(int i=0;i<50;i++)
//				System.out.println(tokenList.get(3152+i).getContent());		
			//���������Ͳ�Ҫ��token�������ˣ�ռ�ܶ��ڴ�
			this.tokenList = null;
		} catch (IOException e) {
			e.printStackTrace();
//			System.out.println("in file "+fileName);
//			System.out.println("�ʷ���������");
			return;
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
	 * ��ʼ��import �б�
	 * ���ƶ���������࿪ʼ֮��
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
	 * �﷨�����ı�׼��ʽ����Ӧ����ǰ�ȿ�һ�£��پ����Ƿ������ƣ�Ŀǰȫ����ֱ���ƹ�ȥ���ܲ���ѧ2014.10.23
	 * TODO:Ҫ�ĵõط��ܶ࣬��������2014.10.23
	 */
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
	
	private boolean isLastToken(){
		return tokenScanCount+1>=tokenList.size()?true:false;
	}
	
	/**
	 * ���������ǰtoken Ϊclass��interface���������������
	 * �������ʱ�ĵ�ǰtokenΪ�������ʱ���Ҵ�����
	 * @param nestLevel Ƕ�ײ㼶
	 * @param anonymousClassIndex Ϊ0��ʾ����������
	 */
	private void classParsing(int nestLevel,int anonymousClassIndex){
		//��������
		int braceCount =  0;
		//��������
		int anonymousClassCount = 0;
		//��������
		if(anonymousClassIndex == 0){
			//�ƶ�������
			moveToNextToken();
			//�ⲿ��ֱ����class name, �ڲ����$����
			if(nestLevel==0){
				currentClassName = currentClassName + typeParsing();
				if(currentClassName.contains("<")){
					//������Ϣ��������Ϊandroguard��������в���������
					currentClassName = currentClassName.replaceAll("<.*>", "");
				}
			}else{
				currentClassName = currentClassName +"$"+getCurrentToken().getContent();
			}
		}else{
			currentClassName = currentClassName +"$"+ anonymousClassIndex;
		}
		//����һ�µ�ǰ�����ͷ��������Ա�ݹ�����෵�غ�ָ�
		String saveClassName = new String(currentClassName);
//		System.out.println("current class is "+currentClassName);
		
		if(nestLevel == 0){//�ⲿ������̳к�ʵ����Ϣ	
			//moveToNextToken();
			//�ƶ�����һ��������ţ��������м���ڼ̳к�ʵ�ֵ�����
			while(!getCurrentToken().getContent().equals("{")){	
				if(getCurrentToken().getContent().equals("extends")){ //java ֻ�����̳�
					moveToNextToken();
					//Ϊ�˷����̳й�ϵ��������Ϣ����������
					extendsClassName = getTypeFullClassName(typeParsing());
				}else if(getCurrentToken().getContent().equals("implements")){ // ������ʵ�ֶ���ӿ�
					do{
						moveToNextToken();
						//Ϊ�˷����̳й�ϵ��������Ϣ����������
						implementsList.add(new String(getTypeFullClassName(typeParsing())));
					}while(getCurrentToken().getContent().equals(","));
				}else { //�����������������ע�ͣ�����Ҫ��ǰ�ƶ�����Ȼ����ѭ��
					moveToNextToken();
				}
			}
		}else{//�ڲ��಻�����̳к�ʵ����Ϣ
			//�ƶ�����һ��������ţ���Ϊ�������ʼλ�ã������м���ڼ̳к�ʵ�ֵ�����
			while(!getCurrentToken().getContent().equals("{")){
				moveToNextToken();
			}
		}
		braceCount++;
		//��ʼ���������
		while(braceCount>0){
			moveToNextToken();
//			String debug = getCurrentToken().getContent();
			if(getCurrentToken().getContent().equals("}")){//�����Ҵ����ţ����������
//				System.out.println("class "+currentClassName+" parsing end");
				braceCount--;
			}else if(getCurrentToken().getContent().equals("{")){//���õĴ����
				//���������
				anonymousClassCount = blockParsing(nestLevel,anonymousClassCount);
				//������п�����������ͷ����ڲ��࣬���Խ�����Ҫ�ָ��ֳ�
				currentClassName = new String(saveClassName);
			}else if(getCurrentToken().getType()==TokenType.modifier){//�����η������ܳ���field,method,������class�Ķ���
				moveToNextToken();
				//�����������η�
				while(getCurrentToken().getType()==TokenType.modifier){
					moveToNextToken();
				}
				if(getCurrentToken().getContent().equals("{")){//���õĴ����
					//���������
					anonymousClassCount = blockParsing(nestLevel,anonymousClassCount);
					//������п�����������ͷ����ڲ��࣬���Խ�����Ҫ�ָ��ֳ�
					currentClassName = new String(saveClassName);
				}else if(getCurrentToken().getType()==TokenType.declareclass){ //���ڲ���Ķ��壬�ݹ����֮			
					classParsing(nestLevel+1,0);
					//�ݹ�����ָ��ֳ�
					currentClassName = new String(saveClassName);
				}else if(getCurrentToken().getType()==TokenType.identifier||getCurrentToken().getType()==TokenType.typemodifier){ //���������η���������field��method�Ķ���
					String type;			
					if(getCurrentToken().getType()==TokenType.identifier){//������ǻ������ͣ�������������ʾ���������������������
						type =getTypeFullClassName(typeParsing()) ;
					}else{//�������;Ͳ�Ҫ�����ͽ�����
						type = getCurrentToken().getContent();
						moveToNextToken();
					}
//					System.out.println(type);
					//�ȴ�������
					String tempName = new String(getCurrentToken().getContent());
					moveToNextToken();
					if(getCurrentToken().getContent().equals("(")){ //������˵���Ƿ������壬���뷽������״̬
						//��ǰ������
						currentMethodName = tempName;
						//�ƶ����������뿪ʼ���������
						//abstract�����Ķ��壬û�д����ţ�����Ҫ������, abstract �������岻��¼
						while(getCurrentToken().getType()!=TokenType.brace&&!getCurrentToken().getContent().equals(";")){
							moveToNextToken();
						}
						if(getCurrentToken().getType()==TokenType.brace){   //�������abstract����
							//������������,�����п�����������ͷ����ڲ��࣬����Ҫ�������������,���������ӵ���������index
							anonymousClassCount = methodParsing(nestLevel,anonymousClassCount);
							//�����п�����������ͷ����ڲ��࣬���Խ�����Ҫ�ָ��ֳ�
							currentClassName = new String(saveClassName);
						}
						
//						if(currentMethodName.equals("setHolderViewVisible")){ //debug��
//							System.out.print("");
//						}
											
					}else if(!tempName.equals("(")){  //��field���壬����field��tempName�õ�����˵���ǹ��캯��
						FieldModel model= new FieldModel();
						model.setClassName(currentClassName);
						//field���ʹ���������
						model.setTypeName(type);
						model.setFieldName(tempName);
						model.setDeclareLineNumber(getCurrentToken().getLineNumber());
						fieldList.add(model);
					}		
				}
			}else if(getCurrentToken().getType()==TokenType.declarenew){//new ��һ�����󣬿��ܳ���������
				//����new���
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount,saveClassName,null);
				//������ɣ��ָ��ֳ�
				currentClassName = new String(saveClassName);
			}
		}		
	}
	
	/**
	 * ������ʾ������
	 * ��ʼλ��Ϊ��������ʾ���ĵ�һ��token
	 * ��������ʱ,λ��Ϊ������ʾ����������һ��token
	 * @return ���ؽ�����������
	 */
	private String typeParsing(){
		String type = new String(getCurrentToken().getContent());
		moveToNextToken();	
		if(getCurrentToken().getContent().equals("[")){//������������
			do{
				type=type+new String(getCurrentToken().getContent());
				moveToNextToken();
			}while(!getCurrentToken().getContent().equals("]"));
			type=type+new String(getCurrentToken().getContent());
			moveToNextToken();	
		}else if(getCurrentToken().getContent().equals("<")){//�����������ͣ��ݹ�һ��ʵ��Ƕ�׵����ͱ��ʽ����
			do{
				//type=type+new String(getCurrentToken().getContent());
				//moveToNextToken();
				
				type = type+typeParsing();
				while(getCurrentToken().getContent().equals(",")){//<T,P>ģ���డ��������T_T
					type = type +",";
					moveToNextToken();
					type = type+typeParsing();
				}
			}while(!getCurrentToken().getContent().equals(">"));
			type=type+new String(getCurrentToken().getContent());
			moveToNextToken();	
		}else if(getCurrentToken().getContent().equals(".")){//�������ڲ�����ShortCutCallback.Stub
			do{
				type=type+new String(getCurrentToken().getContent()); //TODO:.�ź����Ӧ��Ҳ�ǽ������ͣ����ܼ򵥵�ƴ����һ��token����ʵ��
				moveToNextToken();
				type=type+new String(getCurrentToken().getContent());
				moveToNextToken();
			}while(getCurrentToken().getContent().equals("."));
		}
		return type;
	}
	
	
	/**
	 * block��������ǰtokenӦΪ�������
	 * ��������ʱ��ǰtokenΪ��block�������Ҵ�����
	 * @param nestLevel
	 * @param anonymousClassIndex
	 * @return
	 */
	private int blockParsing(int nestLevel,int anonymousClassIndex){ //���õģ�����ֻ�Ǵ�����һ�֣��������ִ����
		int braceCount = 0;
		//������Ҫ�������������������
		int anonymousClassCount = anonymousClassIndex;
		//����һ�µ�ǰ�����ͷ��������Ա�ݹ�����෵�غ�ָ�
		String saveClassName = new String(currentClassName);
		//��һ�·�����
		String saveMethodName = new String(currentMethodName);
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
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount,saveClassName,saveMethodName);
				//������ɣ��ָ��ֳ�
				currentClassName = new String(saveClassName);
				//������ɣ��ָ��ֳ�
				currentMethodName = new String(saveMethodName);
			}
		}
		return anonymousClassCount;
	}
	
	/**
	 * ������������ǰtokenӦΪ�������
	 * ��������ʱ��ǰtokenΪ�˷����������Ҵ�����
	 * Ҫ���ط��������Ժ������ӵ�������index
	 * @param nestLevel
	 * @param anonymousClassIndex
	 */
	private int methodParsing(int nestLevel,int anonymousClassIndex){
		int braceCount = 0;
		//������Ҫ�������������������
		int anonymousClassCount = anonymousClassIndex;
		//����һ�µ�ǰ�����ͷ��������Ա�ݹ�����෵�غ�ָ�
		String saveClassName = new String(currentClassName);
		//��һ�·�����
		String saveMethodName = new String(currentMethodName);
		//��ʼ��
		int beginLineNumber = getCurrentToken().getLineNumber();
		//������
		int endLineNumber = 0;
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
				anonymousClassCount = newStatementParsing(nestLevel, anonymousClassCount,saveClassName,saveMethodName);
				//������ɣ��ָ��ֳ�
				currentClassName = new String(saveClassName);
				//������ɣ��ָ��ֳ�
				currentMethodName = new String(saveMethodName);
			}
		}
		//�����������Ž�����б���
		endLineNumber = getCurrentToken().getLineNumber();
		MethodModel mModel = new MethodModel();
		mModel.setClassName(packageName+"."+currentClassName);
		mModel.setMethodName(currentMethodName);
		mModel.setBeginLineNumber(beginLineNumber);
		mModel.setEndLineNumber(endLineNumber);
		methodList.add(mModel);
		
		//debug��
//		System.out.println(mModel.getFullName());
//		if(currentMethodName.equals("commitRestore")){
//			System.out.println(mModel.getEndLineNumber());
//		}
		
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
	private int newStatementParsing(int nestLevel, int anonymousClassIndex,String saveClassName,String saveMethodName){
		
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
				moveToNextToken();
				classParsing(nestLevel+1,anonymousClassCount+1);
				anonymousClassCount++;		
			}else{ //����������

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
	
	//��������Ϣת��Ϊ��������������+������,ͬʱ��ȥ��������Ϣ
	private String getTypeFullClassName(String type){
		
		//String �İ���java.lang���������import��������Ҫ�ر���
		if(type.equals("String")||type.equals("StringBuffer")||type.equals("String[]")||type.equals("StringBuffer[]")){
			return "java.lang."+type;
		}
		
		String fullClassName; 
		if(!(type.startsWith("com.")||type.startsWith("java.")||type.startsWith("android."))){//ͬĿ¼�µ��಻����import�б��У�����Ĭ���ȼ��ϰ���
			//TODO:������Ϣ�ᵼ��ƥ�䲻�ϣ���ȥ�����Ժ�Ľ�
			fullClassName = new String(packageName+"."+type.replaceAll("<.*>", ""));
		}else{//TODO:com.��ͷ��ʾ�Ѿ�����������������ת�����жϵ��߼��൱�����ף��Ժ�Ľ�
			fullClassName = new String(type);
			return fullClassName;
		}
		//����importlist��ƥ�䵽�ĸ���Ϊƥ�䵽
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
	 * ��|�ָ�����������б�
	 * @return
	 */
	public String getFieldClassNameStringList(){
		String result = new String("");
		if(this.fieldList==null)return null;
		//�ü���ȥ��
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
	 * ��|�ָ���implement list
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
