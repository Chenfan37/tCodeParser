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
	
	//类名
	private String className = new String("");
	//方法list
	private List<JavaMethodModel> methodList = new ArrayList<JavaMethodModel>();
	//field list
	private List<JavaFieldModel> fieldList = new ArrayList<JavaFieldModel>();
	//包名
	private String packageName = new String("");
	//import 列表 ，用来匹配类型完整名字
	private List<String> importList = new ArrayList<String>();
	//继承类名
	private String extendsClassName = new String("");
	//实现列表
	private List<String> implementsList = new ArrayList<String>();
	//是否是抽象类
	private boolean isAbstract = false;
	//是否是接口
	private boolean isInterface = false;
	//token list, 可选
	private List<TokenModel> tokenList = new ArrayList<TokenModel>();
	//是否保留token
	private boolean isReserveToken = false;
	//内部类列表
	private List<JavaInnerClassResultModel> innerClassModelList = new ArrayList<JavaInnerClassResultModel>();
	//catch clause 行数
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
	 * 用|分隔的域的类型列表
	 * @return
	 */
	public String getFieldClassNameStringList(){
		String result = new String("");
		if(this.fieldList==null)return null;
		//用集合去重
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
