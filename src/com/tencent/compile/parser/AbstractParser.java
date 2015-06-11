package com.tencent.compile.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.tencent.compile.resultmodels.BaseResultModel;

public abstract class AbstractParser {

	public abstract BaseResultModel parsing(ByteArrayOutputStream outStream, ParsingConfig config);
	
	public abstract BaseResultModel parsing(String filePath, ParsingConfig config);
	
	protected abstract void init();
	
	public class ParsingConfig{
		
		public static final int ERROR = -1;
		
		private Map<String, Integer> parsingConfig = new HashMap<String, Integer>();
		
		public ParsingConfig(){
			
		}
		
		public void putAttribute(String field, int value){
			parsingConfig.put(field, value);
		}
		
		public int getAtrribute(String field){
			
			if(parsingConfig.get(field)==null)
				return -1;
			
			return parsingConfig.get(field);
		}
	}
}
