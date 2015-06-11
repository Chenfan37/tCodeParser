package com.tencent.compile.resultmodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.tencent.compile.models.JavaFieldModel;
import com.tencent.compile.models.JavaInnerClassResultModel;
import com.tencent.compile.models.JavaMethodModel;
import com.tencent.compile.models.TokenModel;

public class JavaClassResultModel extends BaseResultModel{
	
	//����
	private String className = new String("");
	//����list
	private List<JavaMethodModel> methodList = new ArrayList<JavaMethodModel>();
	//field list
	private List<JavaFieldModel> fieldList = new ArrayList<JavaFieldModel>();
	//����
	private String packageName = new String("");
	//import �б� ������ƥ��������������
	private List<String> importList = new ArrayList<String>();
	//�̳�����
	private String extendsClassName = new String("");
	//ʵ���б�
	private List<String> implementsList = new ArrayList<String>();
	//�Ƿ��ǳ�����
	private boolean isAbstract = false;
	//�Ƿ��ǽӿ�
	private boolean isInterface = false;
	//token list, ��ѡ
	private List<TokenModel> tokenList = new ArrayList<TokenModel>();
	//�Ƿ���token
	private boolean isReserveToken = false;
	//�ڲ����б�
	private List<JavaInnerClassResultModel> innerClassModelList = new ArrayList<JavaInnerClassResultModel>();
	//catch clause ����
	private List<Integer> catchClauseLines = new ArrayList<Integer>();

	@Override
	public String getResultByLineNumber(int lineNumber) {
		// TODO Auto-generated method stub
		String result = null;
		int rangeSize = -1;
		for(JavaMethodModel model:methodList){
			if(model.getBeginLineNumber()<=lineNumber&&model.getEndLineNumber()>=lineNumber){
				if(rangeSize==-1||(model.getEndLineNumber()-model.getBeginLineNumber())<=rangeSize){
					rangeSize = model.getEndLineNumber()-model.getBeginLineNumber();
					result = model.getFullName();
				}
			}
		}
		return result;
	}

	public List<JavaMethodModel> getMethodList(){
		return this.methodList;
	}
	
	public void setMethodList(List<JavaMethodModel> list){
		this.methodList = list;
	}
	
	public void addMethodModel(JavaMethodModel model){
		this.methodList.add(model);
	}
	
	public List<JavaMethodModel> getMethodModelByName(String name){
		
		List<JavaMethodModel> result = new ArrayList<JavaMethodModel>();
		for(JavaMethodModel model : this.methodList){
			if(model.getFullName().equals(name)){
				result.add(model);
			}
		}
		return result;
	}
	
	public List<JavaInnerClassResultModel> getInnerClassList(){
		return this.innerClassModelList;
	}
	
	public void addInnerClassModel(JavaInnerClassResultModel innerClassModel){
		this.innerClassModelList.add(innerClassModel);
	}
	
	public List<JavaFieldModel> getFieldList(){
		return this.fieldList;
	}
	
	public void addFieldModel(JavaFieldModel model){
		this.fieldList.add(model);
	}
	
	public void setFieldList(List<JavaFieldModel> list){
		this.fieldList = list;
	}
	
	public List<TokenModel> getTokenList(){
		if(this.isReserveToken){
			return this.tokenList;
		}else{
			System.out.println("token list was not reserved when parsing, choose another parsing mode");
			return null;
		}
	}
	
	public void setTokenList(List<TokenModel> list){
		this.tokenList = list;
		this.isReserveToken = true;
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
		for(JavaFieldModel fieldModel : this.fieldList){
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
	
	public void setImportList(List<String> list){
		this.importList = list;
	}
	
	public String getExtends(){
		return this.extendsClassName;
	}
	
	public void setExtends(String name){
		this.extendsClassName = name;
	}
	
	public List<String> getImplementsList(){
		return this.implementsList;
	}
	
	public void setImplementsList(List<String> list){
		this.implementsList = list;
	}
	
	public void setPackageName(String packageName){
		this.packageName = packageName;
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
		String className = new String(packageName+"/"+this.className);
		return className.replace('.', '/');
	}
	
	public String getClassName(){
		String className = new String(packageName+"."+this.className);
		return className;
	}
	
	public void setClassName(String name){
		this.className = name;
	}
	
	public void setIsAbstract(boolean isAbstract){
		this.isAbstract = isAbstract;
	}
	
	public boolean getIsAbstract(){
		return this.isAbstract;
	}
	
	public void setIsInterface(boolean isInterface){
		this.isInterface = isInterface;
	}
	
	public boolean getIsInterface(){
		return this.isInterface;
	}
	
	public void addCatchClauseLine(int lineNum){
		if(!catchClauseLines.contains(lineNum)){
			catchClauseLines.add(lineNum);
		}
	}
	
	public List<Integer> getCatchClauseLines(){
		return this.catchClauseLines;
	}
}
