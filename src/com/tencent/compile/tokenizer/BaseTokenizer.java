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
	//�����ļ�  
	protected File inFile;
	//����reader
	protected Reader inReader;
	//���뻺����  
	protected char[] inputBuf = new char[BUF_SIZE];  
	//ɨ�赽�ĵ���  
	protected char[] scanWord = new char[MAX_WORD_SIZE];  
	//����ֵ  
	protected  String wordValue;   
	//���뻺�������  
	protected  int numberOfBuf=0;  
	//ÿ�е�����  
	protected  int numberOfWords=0;  
	//�ܵ�����  
	protected  int numberOfAllWords=0;  
	//�к�  
	protected  int lineNumber=1;  
	//������  
	protected  int numberOfLines=1;  
	//��������ǰλ�õ��ַ�  
	protected  int scanCount=0;  
	//���ʵ�����  
	protected  int indexOfWord=0;  
	//״̬��״̬  
	protected  int state=0;  
	//��������ȡ��־  
	protected  int bufReadFlag=0;  
	// ':' ���ͱ�־��0Ϊ '��'��1Ϊ '����' ��ʶ   
	protected  int colonTypeFlag=0; 
	//��� list
	public List<TokenModel> tokenResultList= new ArrayList<TokenModel>(); 
	
	public BaseTokenizer(){
		
	}
	
	//��ȡ�ļ����ݵ����뻺����  
	//�����뻺������ÿ�ζ�������������С  
	public BaseTokenizer(String fileName){
		inFile = new File(fileName);
		try {
			//�Ӱ���һ��Ҫ��utf-8��ʽ���룬��Ȼ���ܻ���ע�ͷ��Ż������´ʷ���������
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
			//�Ӱ���һ��Ҫ��utf-8��ʽ���룬��Ȼ���ܻ���ע�ͷ��Ż������´ʷ���������
			inReader = new InputStreamReader(new ByteArrayInputStream(buffer),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void Scanner() throws IOException {  
	    char inChar;  
	    int index=0;  
	    //��ȡ������ǰ����  
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
	    //��ȡ�����������  
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
	    //����Ѿ����껺������ǰ/�����  
	    //ֻ���ڻ�����������û�б���ȡ��ǰ���²ſ��Զ�ȡ  
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
	    //��ȡ����������  
	    inChar=inputBuf[scanCount];  
	    scanCount=(scanCount+1)%BUF_SIZE;  
	    //��¼��ȡ���ַ�  
	    //��̬��ʱ��scanWord��¼���ַ������Ƕ�ȡ�ĵ���  
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
