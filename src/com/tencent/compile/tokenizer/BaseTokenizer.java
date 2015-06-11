package com.tencent.compile.tokenizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.tencent.compile.models.TokenModel;

public abstract class BaseTokenizer {
	
	protected static final int BUF_SIZE = 128;
	protected static final int HALF_BUF_SIZE = 64;
	protected static final int MAX_WORD_SIZE = 128;
	//输入文件  
	protected File inFile;
	//输入reader
	protected Reader inReader;
	//输入缓冲区  
	protected char[] inputBuf = new char[BUF_SIZE];  
	//扫描到的单词  
	protected char[] scanWord = new char[MAX_WORD_SIZE];  
	//属性值  
	protected  String wordValue;   
	//输入缓冲区编号  
	protected  int numberOfBuf=0;  
	//每行单词数  
	protected  int numberOfWords=0;  
	//总单词数  
	protected  int numberOfAllWords=0;  
	//行号  
	protected  int lineNumber=1;  
	//总行数  
	protected  int numberOfLines=1;  
	//缓冲区当前位置的字符  
	protected  int scanCount=0;  
	//单词的索引  
	protected  int indexOfWord=0;  
	//状态机状态  
	protected  int state=0;  
	//缓冲区读取标志  
	protected  int bufReadFlag=0;  
	// ':' 类型标志，0为 '：'，1为 '？：' 标识   
	protected  int colonTypeFlag=0; 
	//结果 list
	public List<TokenModel> tokenResultList= new ArrayList<TokenModel>(); 
	
	public BaseTokenizer(){
		
	}
	
	//读取文件数据到输入缓冲区  
	//两个半缓冲区，每次读入半个缓冲区大小  
	public BaseTokenizer(String fileName){
		inFile = new File(fileName);
		try {
			//坑啊，一定要用utf-8格式解码，不然可能会有注释符号混淆导致词法分析错误
			inReader = new InputStreamReader(new FileInputStream(inFile),"UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public BaseTokenizer(ByteArrayOutputStream outStream){
		try {
			byte[] buffer = outStream.toByteArray();
			//坑啊，一定要用utf-8格式解码，不然可能会有注释符号混淆导致词法分析错误
			inReader = new InputStreamReader(new ByteArrayInputStream(buffer),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void Scanner() throws IOException {  
	    char inChar;  
	    int index=0;  
	    //读取缓冲区前半区  
	    if (numberOfBuf==0)  
	    {  
	        index=0;  
	        while (index<HALF_BUF_SIZE)  
	        {  
	            inChar=(char) inReader.read();  
	            if (inChar != '\r') {
//					System.out.print(inChar);
					inputBuf[index]=inChar;  
					index++;  
	            }
	        }  
	        scanCount=0;  
	        bufReadFlag=1;  
	    }  
	    //读取缓冲区后半区  
	    if (numberOfBuf==1)  
	    {  
	        index=0;  
	        while (index<HALF_BUF_SIZE)  
	        {  
	        	inChar=(char) inReader.read();  
	            if (inChar != '\r') {
//					System.out.print(inChar);
					inputBuf[index+HALF_BUF_SIZE]=inChar;  
					index++;  
	            }
	        }  
	        scanCount=HALF_BUF_SIZE;  
	        bufReadFlag=0;  
	    }  
	}  
	
	protected char ReadChar() throws IOException{
		
		char inChar;  
	    //如果已经读完缓冲区的前/后半区  
	    //只有在缓冲区的数据没有被读取的前提下才可以读取  
	    if (scanCount==0&&bufReadFlag==0)  
	    {  
	        numberOfBuf=0;  
	        Scanner();  
	    }  
	    if (scanCount==HALF_BUF_SIZE&&bufReadFlag==1)  
	    {  
	        numberOfBuf=1;  
	        Scanner();  
	    }  
	    //读取缓冲区数据  
	    inChar=inputBuf[scanCount];  
	    scanCount=(scanCount+1)%BUF_SIZE;  
	    //记录读取的字符  
	    //终态的时候，scanWord记录的字符串就是读取的单词  
	    scanWord[indexOfWord]=inChar;  
	    indexOfWord=(indexOfWord+1)%MAX_WORD_SIZE;  
	    return inChar;  
	}
	
	protected void UntRead(){
		scanCount--;  
	    indexOfWord--;  
	    if (scanCount<0)  
	    {  
	        scanCount=BUF_SIZE-1;  
	    }  
	    if (indexOfWord<0)  
	    {  
	        indexOfWord=0;  
	    }  
	    scanWord[indexOfWord]=0;  
	}
	
	protected void ReInit(){
		scanWord = null;
		scanWord = new char[MAX_WORD_SIZE];
		indexOfWord=0; 
	}
	
	protected void outputTokenModel(int index){
		numberOfWords++;  
	    numberOfAllWords++;  
		ReInit();
	}
	
	public abstract void DFA() throws IOException;
}
