package com.tencent.compile.tokenizer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.tencent.compile.models.TokenModel;

/**
 * java词法分析器(伪)
 * @author frankcfliu
 *
 */
public class JTokenizer extends BaseTokenizer{
	
	//关键字
	String keyWords[]={  
	    "abstract","boolean","break","byte","case","catch","char","class",  
	    "const","continue","default","do","double","else","extends","false",  
	    "final","finally","float","for","goto","if","implements","import",  
	    "instanceof","int","interface","long","native","new","null","package",  
	    "private","protected","public","return","short","static","super","switch",  
	    "synchronized","this","throw","throws","transient","true","try","void",  
	    "volatile","while","enum"
	};  
	

	public JTokenizer(String fileName){
		super(fileName);
	}
	
	public JTokenizer(ByteArrayOutputStream outStream){
		super(outStream);
	}
	
	@Override
	protected void outputTokenModel(int index)  
	{  
	    //如果遇到了终结状态，将结果输出  
		TokenModel model = new TokenModel();
		model.setLineNumber(lineNumber);
		model.setContent(String.valueOf(scanWord).trim());
		switch(index)  
	    {  
	    case 1:  
	        model.setTokenType(TokenModel.TokenType.error); 
	        break;  
	    case 2:  
	    	model.setTokenType(TokenModel.TokenType.comments); 
	        break;  
	    case 4:  
	    	model.setTokenType(TokenModel.TokenType.keyword); 
	        break;  
	    case 5:  
	    	model.setTokenType(TokenModel.TokenType.identifier); 
	        break;  
	    case 6:  
	    	//布尔常量，暂不区分
	    	model.setTokenType(TokenModel.TokenType.constant); 
	        break;  
	    case 7:  
	    	//字符常量，暂不区分 
	        model.setTokenType(TokenModel.TokenType.constant);  
	        break;  
	    case 8:  
	    	//整型常量，暂不区分 
	        model.setTokenType(TokenModel.TokenType.constant);    
	        break;  
	    case 9:  
	    	//浮点型常量，暂不区分 
	        model.setTokenType(TokenModel.TokenType.constant);  
	        break;  
	    case 10:  
	    	//字符串常量，暂不区分 
	        model.setTokenType(TokenModel.TokenType.constant);  
	        break;  
	    case 11:  
	    	// "=、+=、-=、*=、/=、%=、&=、^=、|=、>>=、<<=、>>>="
	        model.setTokenType(TokenModel.TokenType.binocular_operators);   
	        break;  
	    case 12:  
	    	//?:
	        model.setContent("?:");
	        model.setTokenType(TokenModel.TokenType.multiple_operators);   
	        break;  
	    case 13:  
	    	// ||，暂不区分 
	        model.setTokenType(TokenModel.TokenType.binocular_operators);
	        break;  
	    case 14:  
	    	// &&，暂不区分 
	        model.setTokenType(TokenModel.TokenType.binocular_operators);
	        break;  
	    case 15:  
	    	// |，暂不区分 
	        model.setTokenType(TokenModel.TokenType.binocular_operators);
	        break;  
	    case 16:  
	    	// ^，暂不区分 
	        model.setTokenType(TokenModel.TokenType.binocular_operators);
	        break;  
	    case 17:  
	    	// &，暂不区分 
	        model.setTokenType(TokenModel.TokenType.binocular_operators); 
	        break;  
	    case 18:  
	    	// "==、!="
	    	model.setTokenType(TokenModel.TokenType.binocular_operators); 
	        break;  
	    case 19:  
	    	//"<、>、<=、>="
	    	model.setTokenType(TokenModel.TokenType.binocular_operators); 
	        break;  
	    case 20:  
	    	//"<<、>>、>>>"
	    	model.setTokenType(TokenModel.TokenType.binocular_operators);   
	        break;  
	    case 21:  
	    	//"+、-"
	    	model.setTokenType(TokenModel.TokenType.binocular_operators);    
	        break;  
	    case 22:  
	    	//"*、/、%"
	    	model.setTokenType(TokenModel.TokenType.binocular_operators);  
	        break;  
	    case 23:  
	    	//"++、--、+(正)、–(负)、!、~"
	    	model.setTokenType(TokenModel.TokenType.unary_operators);   
	        break;  
	    case 24:  
	    	//"[]、()、."
	    	model.setTokenType(TokenModel.TokenType.parentheses);    
	        break;  
	    case 25:  
	    	// ","
	    	model.setTokenType(TokenModel.TokenType.separator);    
	        break;  
	    case 26:  
	    	//"{}"
	    	model.setTokenType(TokenModel.TokenType.brace);   
	        break;  
	    case 27:  
	    	// ";"
	    	model.setTokenType(TokenModel.TokenType.separator);
	        break; 
	    case 28:  
	    	// 修饰符
	    	model.setTokenType(TokenModel.TokenType.modifier);
	        break;
	    case 29:  
	    	// 继承基类
	    	model.setTokenType(TokenModel.TokenType.declarebase);
	        break;
	    case 30:
	    	// 类定义
	    	model.setTokenType(TokenModel.TokenType.declareclass);
	        break;
	    case 31:
	    	// new 对象
	    	model.setTokenType(TokenModel.TokenType.declarenew);
	        break;
	    case 32:
	    	// 类型修饰符
	    	model.setTokenType(TokenModel.TokenType.typemodifier);
	        break;
	    }  
		if(model.getType()!=TokenModel.TokenType.comments){
			this.tokenResultList.add(model);  
		}
	    super.ReInit(); 
	}  
	
	//判断读取到的字符串是关键字、布尔常量、标识符  
	int JudgeStringType()  
	{  
	    int index=0;  
	    String scanWordString = String.valueOf(scanWord).trim();
        if (scanWordString.equals("true")||scanWordString.equals("false")||scanWordString.equals("TRUE")||scanWordString.equals("FALSE"))  
        {  
            return 6;  
        }else if(scanWordString.equals("public")||scanWordString.equals("private")||
        		scanWordString.equals("protected")||scanWordString.equals("static")||
        		scanWordString.equals("final")||scanWordString.equals("synchronized")||
        		scanWordString.equals("abstract")||scanWordString.equals("native")||
        		scanWordString.equals("volatile")||scanWordString.equals("transient")){
        	return 28;
        	
        }else if(scanWordString.equals("extends")||scanWordString.equals("implements")){
        	return 29;
        }else if(scanWordString.equals("class")||scanWordString.equals("interface")||scanWordString.equals("enum")){
        	return 30;
        }else if(scanWordString.equals("new")){
        	return 31;
        }else if(scanWordString.equals("int")||scanWordString.equals("char")||
        		scanWordString.equals("void")||scanWordString.equals("byte")||
        		scanWordString.equals("boolean")||scanWordString.equals("float")||
        		scanWordString.equals("double")||scanWordString.equals("short")||
        		scanWordString.equals("long")){
        	return 32;
        }
	    for (index=0;index<51;index++)  
	    {  
	    	if(scanWordString.equals(String.valueOf(keyWords[index]).trim()))  
	        {  
	            return 4;  
	        }  
	    }  
	    return 5;  
	}  
	
	//判断是否是间隔符{}、[]/()/. 跳转到对应的dfa状态  
	int JudgeDelimeterType()  
	{  
		String scanWordString = String.valueOf(scanWord).trim();
		if (scanWordString.equals("{")||scanWordString.equals("}"))  
	    {  
	        return 26;  
	    }  
	    else if (scanWordString.equals("[")||scanWordString.equals("]")||scanWordString.equals("(")||scanWordString.equals(")")||scanWordString.equals("."))  
	    {  
	        return 24;  
	    }  
	    else if (scanWordString.equals(","))  
	    {  
	        return 25;  
	    }  
	    else  
	    {  
	        return 27;  
	    }  
	}  
	
	//判断 '?' 之后 ':' 是否存在  
	//只适用于非嵌套的情况  
	private boolean FindColon()  
	{  
		for (int index=0;index<HALF_BUF_SIZE;index++)
	    {  
	        if (inputBuf[index+scanCount]==':')  
	        {  
	            colonTypeFlag=1;  
	            return true;  
	        }  
	    }  
	    return false;  
	}  
	
	@Override
	public void DFA() throws IOException  
	{  
	    int tempType=0;  
	    state=0;  
	    char scanCode = 0;  
	    while((int)scanCode>=0&&(int)scanCode!=65535)  
	    {  
	        //每次读取一个字符，然后在DFA内跳转  
//	    	System.out.println("本次state为： "+state);
//	    	System.out.println("本次行数为： "+lineNumber);
//	    	System.out.println("本次scanCount为： "+scanCount);
	        scanCode = ReadChar();  
//	        System.out.println("本次scanCode ascii为： "+(int)scanCode);
//	        System.out.println("本次scanCode为： "+scanCode);
//	        System.out.println(" ");
	        switch(state)  
	        {  
	        case 0:  
	            switch(scanCode)  
	            {  
	            case 'a':  
	            case 'b':  
	            case 'c':  
	            case 'd':  
	            case 'e':  
	            case 'f':  
	            case 'g':  
	            case 'h':  
	            case 'i':  
	            case 'j':  
	            case 'k':  
	            case 'l':  
	            case 'm':  
	            case 'n':  
	            case 'o':  
	            case 'p':  
	            case 'q':  
	            case 'r':  
	            case 's':  
	            case 't':  
	            case 'u':  
	            case 'v':  
	            case 'w':  
	            case 'x':  
	            case 'y':  
	            case 'z':  
	            case 'A':  
	            case 'B':  
	            case 'C':  
	            case 'D':  
	            case 'E':  
	            case 'F':  
	            case 'G':  
	            case 'H':  
	            case 'I':  
	            case 'J':  
	            case 'K':  
	            case 'L':  
	            case 'M':  
	            case 'N':  
	            case 'O':  
	            case 'P':  
	            case 'Q':  
	            case 'R':  
	            case 'S':  
	            case 'T':  
	            case 'U':  
	            case 'V':  
	            case 'W':  
	            case 'X':  
	            case 'Y':  
	            case 'Z':  
	            case '$':  
	            case '_':  
	                state=1;  
	                break;  
	            case '0':  
	                state=3;  
	                break;  
	            case '1':  
	            case '2':  
	            case '3':  
	            case '4':  
	            case '5':  
	            case '6':  
	            case '7':  
	            case '8':  
	            case '9':  
	                state=20;  
	                break;  
	            case '\'':  
	                state=23;  
	                break;  
	            case '"':  
	                state=26;  
	                break;  
	            case '{':  
	            case '}':  
	            case '[':  
	            case ']':  
	            case ',':  
	            case '(':  
	            case ')':  
	            case ';':  
	                state=40;  
	                break;  
	            case '.':  
	                state=41;  
	                break;  
	            case '/':  
	                state=43;  
	                break;  
	            case '+':  
	                state=51;  
	                break;  
	            case '-':  
	                state=55;  
	                break;  
	            case '*':  
	                state=59;  
	                break;  
	            case '%':  
	                state=62;  
	                break;  
	            case '?':  
	                state=65;  
	                break;  
	            case '=':  
	                state=66;  
	                break;  
	            case '~':  
	                state=69;  
	                break;  
	            case '>':  
	                state=68;  
	                break;  
	            case '^':  
	                state=79;  
	                break;  
	            case '&':  
	                state=82;  
	                break;  
	            case '|':  
	                state=86;  
	                break;  
	            case '!':  
	                state=90;  
	                break;  
	            case '<':  
	                state=93;  
	                break;  
	            case '\n':  
	                state=102;  
	                break;  
	            case ' ':  
	            case '\t':  
	            case '\r':  
	            case '\f':  
	            case '\b':  
	            case ':':  
	            case '@':  
	            case '#':  
	                state=103;  
	                break;  
	            default:  
	                state=100;  
	            }  
	            break;  
	            //关键字、布尔常量、标识符  
	        case 1:  
	            if((scanCode>='a'&&scanCode<='z')||(scanCode>='A'&&scanCode<='Z')||(scanCode>='0'&&scanCode<='9')||(scanCode=='$')||(scanCode=='_'))  
	            {  
	                state=1;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=2;  
	            }  
	            break;  
	        case 2:  
	            UntRead();  
	            tempType=JudgeStringType();  
	            outputTokenModel(tempType);  
	            state=0;  
	            break;  
	            //常量  
	        case 3:  
	            if (scanCode=='X'||scanCode=='x')  
	            {  
	                state=4;  
	            }  
	            else if (scanCode>='0'&&scanCode<='7')  
	            {  
	                state=7;  
	            }  
	            else if (scanCode=='.')  
	            {  
	                state=10;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=9;  
	            }  
	            break;  
	        case 4:  
	            if ((scanCode>='0'&&scanCode<='9')||(scanCode>='a'&&scanCode<='f')||(scanCode>='A'&&scanCode<='Z'))  
	            {  
	                state=5;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=100;  
	            }  
	            break;  
	        case 5:  
	            if((scanCode>='0'&&scanCode<='9')||(scanCode>='a'&&scanCode<='f')||(scanCode>='A'&&scanCode<='Z'))  
	            {  
	                state=5;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=6;  
	            }  
	            break;  
	        case 6:  
	            UntRead();  
	            outputTokenModel(8);  
	            state=0;  
	            break;  
	        case 7:  
	            if(scanCode>='0'&&scanCode<='7')  
	            {  
	                state=7;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=8;  
	            }  
	            break;  
	        case 8:  
	            UntRead();  
	            outputTokenModel(8);  
	            state=0;  
	            break;  
	        case 9:  
	            UntRead();  
	            outputTokenModel(8);  
	            state=0;  
	            break;  
	        case 10:  
	            if (scanCode>='0'&&scanCode<='9')  
	            {  
	                state=11;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=100;  
	            }  
	            break;  
	        case 11:  
	            if (scanCode>='0'&&scanCode<='9')  
	            {  
	                state=11;  
	            }  
	            else if (scanCode=='F'||scanCode=='f')  
	            {  
	                state=12;  
	            }  
	            else if (scanCode=='E'||scanCode=='e')  
	            {  
	                state=14;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=13;  
	            }  
	            break;  
	        case 12:  
	            UntRead();  
	            outputTokenModel(9);  
	            state=0;  
	            break;  
	        case 13:  
	            UntRead();  
	            outputTokenModel(9);  
	            state=0;  
	            break;  
	        case 14:  
	            if (scanCode>='0'&&scanCode<='9')  
	            {  
	                state=15;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=100;  
	            }  
	            break;  
	        case 15:  
	            if (scanCode>='0'&&scanCode<='9')  
	            {  
	                state=15;  
	            }  
	            else if (scanCode=='.')  
	            {  
	                state=17;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=16;  
	            }  
	            break;  
	        case 16:  
	            UntRead();  
	            outputTokenModel(9);  
	            state=0;  
	            break;  
	        case 17:  
	            if (scanCode>='0'&&scanCode<='9')  
	            {  
	                state=18;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=100;  
	            }  
	            break;  
	        case 18:  
	            if (scanCode>='0'&&scanCode<='9')  
	            {  
	                state=18;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=19;  
	            }  
	            break;  
	        case 19:  
	            UntRead();  
	            outputTokenModel(9);  
	            state=0;  
	            break;  
	        case 20:  
	            if (scanCode>='0'&&scanCode<='9')  
	            {  
	                state=20;  
	            }  
	            else if (scanCode=='L'||scanCode=='l')  
	            {  
	                state=22;  
	            }  
	            else if (scanCode=='.')  
	            {  
	                state=10;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=21;  
	            }  
	            break;  
	        case 21:  
	            UntRead();  
	            outputTokenModel(8);  
	            state=0;  
	            break;  
	        case 22:  
	            UntRead();  
	            outputTokenModel(8);  
	            state=0;  
	            break;  
	            //字符型  
	        case 23:  
	            if (scanCode=='/')  
	            {  
	                state=28;  
	            }  
	            else  
	            {  
	                state=24;  
	            }  
	            break;  
	        case 24:  
	            if (scanCode=='\'')  
	            {  
	                state=25;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=100;  
	            }  
	            break;  
	        case 25:  
	            UntRead();  
	            outputTokenModel(7);  
	            state=0;  
	            break;  
	            //字符串  
	        case 26:  
	            if (scanCode=='"')  
	            {  
	                state=27;  
	            }  
	            //else if (scanCode=='/')  //亲，java的转义字符是\不是/啊!!!!! WTF
	            else if (scanCode=='\\')  
	            {  
	                state=30;  
	            }  
	            else  
	            {  
	                state=26;  
	            }  
	            break;  
	        case 27:  
	            UntRead();  
	            outputTokenModel(10);  
	            state=0;  
	            break;  
	        case 28:  
	            if (scanCode=='\''||scanCode=='"'||scanCode=='/'||scanCode=='t'||scanCode=='n'||scanCode=='r'||scanCode=='f'||scanCode=='b')  
	            {  
	                state=29;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=100;  
	            }  
	            break;  
	        case 29:  
	            if (scanCode=='\'')  
	            {  
	                state=25;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=100;  
	            }  
	            break;  
	        case 30:  
	        	//30到31这个状态跳转非常奇怪，看起来他认为 "\"; "\") 和"\", 是不转义的，实际上貌似不是，希望是他真的搞错了
	            if (scanCode=='"')  
	            {  
	                //state=31;  
	            	state=26;
	            }  
	            else  
	            {  
	                state=26;  
	            }  
	            break;  
	        case 31:  
	            if (scanCode==';'||scanCode==')'||scanCode==',')  
	            {  
	                UntRead();  
	                state=27;  
	            }  
	            else  
	            {  
	                state=26;  
	            }  
	            break;  
	            //间隔符  
	        case 40:  
	            UntRead();  
	            tempType=JudgeDelimeterType();  
	            outputTokenModel(tempType);  
	            state=0;  
	            break;  
	            // '.' 间隔符  
	        case 41:  
	            if (scanCode>='0'&&scanCode<='9')  
	            {  
	                state=42;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=40;  
	            }  
	            break;  
	        case 42:  
	            UntRead();  
	            outputTokenModel(24);  
	            state=0;  
	            break;  
	        case 43:  
	            if (scanCode=='=')  
	            {  
	                state=45;  
	            }  
	            else if (scanCode=='/')  
	            {  
	                // "//" 注释  
	                state=46;  
	            }  
	            else if (scanCode=='*')  
	            {  
	                // "/**/" 注释  
	                state=48;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=44;  
	            }  
	            break;  
	        case 44:  
	            UntRead();  
	            outputTokenModel(22);  
	            state=0;  
	            break;  
	        case 45:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 46:  
	            if(scanCode=='\n')  
	            {  
	                lineNumber++;  
	                numberOfLines++;  
	                state=47;  
	            }  
	            else  
	            {  
	                state=46;  
	            }  
	            break;  
	        case 47:  
	            UntRead();  
	            outputTokenModel(2);  
	            state=0;  
	            break;  
	        case 48:  
	            if (scanCode=='*')  
	            {  
	                state=49;  
	            }  
	            else if (scanCode=='\n')  
	            {  
	                lineNumber++;  
	                numberOfLines++;  
	                state=48;  
	            }  
	            else  
	            {  
	                state=48;  
	            }  
	            break;  
	        case 49:  
	            if (scanCode=='/')  
	            {  
	                state=50;  
	            }
	            else if(scanCode=='*'){
	            	state=49;
	            }
	            else if (scanCode=='\n')  
	            {  
	                lineNumber++;  
	                numberOfLines++;  
	                state=48;  
	            }  
	            else  
	            {  
	                state=48;  
	            }  
	            break;  
	        case 50:  
	            UntRead();  
	            outputTokenModel(2);  
	            state=0;  
	            break;  
	            //运算符  
	        case 51:  
	            if (scanCode=='+')  
	            {  
	                state=53;  
	            }  
	            else if (scanCode=='=')  
	            {  
	                state=54;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=52;  
	            }  
	            break;  
	        case 52:  
	            UntRead();  
	            UntRead();  
	            UntRead();  
	            scanCode=ReadChar();  
	            //判断 '+' 是正负号还是加减号  
	            if (!(scanCode>='0'&&scanCode<='9'))  
	            {  
	                ReInit();  
	                scanCode=ReadChar();  
	                outputTokenModel(23);  
	            }  
	            else  
	            {  
	                ReInit();  
	                scanCode=ReadChar();  
	                outputTokenModel(21);  
	            }  
	            state=0;  
	            break;  
	        case 53:  
	            UntRead();  
	            outputTokenModel(23);  
	            state=0;  
	            break;  
	        case 54:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 55:  
	            if (scanCode=='-')  
	            {  
	                state=57;  
	            }  
	            else if (scanCode=='=')  
	            {  
	                state=58;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=56;  
	            }  
	            break;  
	        case 56:  
	            UntRead();  
	            UntRead();  
	            UntRead();  
	            scanCode=ReadChar();  
	            //判断 '-' 是正负号还是加减号  
	            if (!(scanCode>='0'&&scanCode<='9'))  
	            {  
	                ReInit();  
	                scanCode=ReadChar();  
	                outputTokenModel(23);  
	            }  
	            else  
	            {  
	                ReInit();  
	                scanCode=ReadChar();  
	                outputTokenModel(21);  
	            }  
	            state=0;  
	            break;  
	        case 57:  
	            UntRead();  
	            outputTokenModel(23);  
	            state=0;  
	            break;  
	        case 58:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 59:  
	            if (scanCode=='=')  
	            {  
	                state=61;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=60;  
	            }  
	            break;  
	        case 60:  
	            UntRead();  
	            outputTokenModel(22);  
	            state=0;  
	            break;  
	        case 61:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 62:  
	            if (scanCode=='=')  
	            {  
	                state=64;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=63;  
	            }  
	            break;  
	        case 63:  
	            UntRead();  
	            outputTokenModel(22);  
	            state=0;  
	            break;  
	        case 64:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 65:  
	            //if (FindColon()==true)  //这里有缓冲区bug，反正不用三目算符，先干掉
	        	if(false)
	            {  
	                state=99;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=100;  
	            }  
	            break;  
	        case 66:  
	            if (scanCode=='=')  
	            {  
	                state=68;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=67;  
	            }  
	            break;  
	        case 67:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 68:  
	            UntRead();  
	            outputTokenModel(18);  
	            state=0;  
	            break;  
	        case 69:  
	            UntRead();  
	            outputTokenModel(23);  
	            state=0;  
	            break;  
	        case 70:  
	            if (scanCode=='=')  
	            {  
	                state=72;  
	            }  
	            else if (scanCode=='>')  
	            {  
	                state=73;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=71;  
	            }  
	            break;  
	        case 71:  
	            UntRead();  
	            outputTokenModel(19);  
	            state=0;  
	            break;  
	        case 72:  
	            UntRead();  
	            outputTokenModel(19);  
	            state=0;  
	            break;  
	        case 73:  
	            if (scanCode=='=')  
	            {  
	                state=75;  
	            }  
	            else if (scanCode=='>')  
	            {  
	                state=76;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=74;  
	            }  
	            break;  
	        case 74:  
	            UntRead();  
	            outputTokenModel(20);  
	            state=0;  
	            break;  
	        case 75:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 76:  
	            if (scanCode=='=')  
	            {  
	                state=78;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=77;  
	            }  
	            break;  
	        case 77:  
	            UntRead();  
	            outputTokenModel(20);  
	            state=0;  
	            break;  
	        case 78:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 79:  
	            if (scanCode=='=')  
	            {  
	                state=80;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=81;  
	            }  
	            break;  
	        case 80:  
	            UntRead();  
	            outputTokenModel(16);  
	            state=0;  
	            break;  
	        case 81:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 82:  
	            if (scanCode=='&')  
	            {  
	                state=84;  
	            }  
	            else if (scanCode=='=')  
	            {  
	                state=85;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=83;  
	            }  
	            break;  
	        case 83:  
	            UntRead();  
	            outputTokenModel(17);  
	            state=0;  
	            break;  
	        case 84:  
	            UntRead();  
	            outputTokenModel(14);  
	            state=0;  
	            break;  
	        case 85:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 86:  
	            if (scanCode=='|')  
	            {  
	                state=88;  
	            }  
	            else if (scanCode=='=')  
	            {  
	                state=89;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=87;  
	            }  
	            break;  
	        case 87:  
	            UntRead();  
	            outputTokenModel(15);  
	            state=0;  
	            break;  
	        case 88:  
	            UntRead();  
	            outputTokenModel(13);  
	            state=0;  
	            break;  
	        case 89:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 90:  
	            if (scanCode=='=')  
	            {  
	                state=92;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=91;  
	            }  
	            break;  
	        case 91:  
	            UntRead();  
	            outputTokenModel(23);  
	            state=0;  
	            break;  
	        case 92:  
	            UntRead();  
	            outputTokenModel(18);  
	            state=0;  
	            break;  
	        case 93:  
	            if (scanCode=='=')  
	            {  
	                state=95;  
	            }  
	            else if (scanCode=='<')  
	            {  
	                state=96;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=94;  
	            }  
	            break;  
	        case 94:  
	            UntRead();  
	            outputTokenModel(19);  
	            state=0;  
	            break;  
	        case 95:  
	            UntRead();  
	            outputTokenModel(19);  
	            state=0;  
	            break;  
	        case 96:  
	            if (scanCode=='=')  
	            {  
	                state=97;  
	            }  
	            else  
	            {  
	                UntRead();  
	                state=98;  
	            }  
	            break;  
	        case 97:  
	            UntRead();  
	            outputTokenModel(20);  
	            state=0;  
	            break;  
	        case 98:  
	            UntRead();  
	            outputTokenModel(11);  
	            state=0;  
	            break;  
	        case 99:  
	            UntRead();  
	            outputTokenModel(12);  
	            state=0;  
	            break;  
	        case 102:  
	            UntRead();  
//	            System.out.println("第 "+ lineNumber +"行，单词数"+ numberOfWords);  
	            lineNumber++;  
	            numberOfLines++;  
	            numberOfWords=0;  
	            ReInit();  
	            state=0;  
	            break;  
	        case 103:  
	            UntRead();  
	            ReInit();  
	            state=0;  
	            break;  
	        case 100:  
	            UntRead();  
	            outputTokenModel(1);  
	            state=0;  
	            break;  
	        }  
	    }  
	}  
}
