package com.tencent.compile.astparser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

public class ASTJavaParser {

	
	public CompilationUnit parsing(File file) throws IOException{
		
		FileInputStream fis = new FileInputStream(file);
		String content = loadAFileToString(file);
		ASTParser parser = ASTParser.newParser(AST.JLS3); //initialize    
        parser.setKind(ASTParser.K_COMPILATION_UNIT);     //to parse compilation unit  
        parser.setSource(content.toCharArray());          //content is a string which stores the java source  
        parser.setResolveBindings(true);  
        CompilationUnit result = (CompilationUnit) parser.createAST(null);  
//		ASTJavaParser aparser = new ASTJavaParser();
//		try {
//			CompilationUnit unit = aparser.parsing(new File("AppSearchHotWordsEngine.java"));
//			List<TypeDeclaration> typeDecs = unit.types();
//			for(TypeDeclaration typeDec:typeDecs){
//				MethodDeclaration[] methodDecs = typeDec.getMethods();
//				for(MethodDeclaration methodDec:methodDecs){
//					System.out.println("method name is "+typeDec.getName()+"."+methodDec.getName()+"("+methodDec.parameters().toString()+")");
//				}
//				TypeDeclaration[] subTypeDecs = typeDec.getTypes();
//				for(TypeDeclaration subTypeDec:subTypeDecs){
//					MethodDeclaration[] subMethodDecs = subTypeDec.getMethods();
//					for(MethodDeclaration subMethodDec:subMethodDecs){
//						System.out.println("method name is "+typeDec.getName()+"."+subTypeDec.getName()+"."+subMethodDec.getName()+
//								"("+subMethodDec.parameters().toString()+")");
//					}
//				}
//			}
			 
        result.accept(new ASTVisitor(){
        	@Override
        	public boolean visit(TypeDeclaration node) {
        		return true;
        	}
        	
        	@Override
        	public boolean visit(MethodDeclaration node) {
        		System.out.println("method name is "+"."+node.getName());
        		return true;
        	}
        	@Override
        	public boolean visit(ImportDeclaration node) {
        		return true;
        	}
        	
        	@Override
        	public boolean visit(AnonymousClassDeclaration node) {
        		System.out.println("anonymous class found ");
        		List<BodyDeclaration> bodyList = node.bodyDeclarations();
        		for(BodyDeclaration body:bodyList){
        			if(body instanceof MethodDeclaration){
        				System.out.println(" anony method name is "+((MethodDeclaration)body).getName());
        			}
        		}
        		return true;	
        	}
        	
        	@Override
        	public boolean visit(Assignment node) {
        		return true;
        	}

		            @Override
		            public boolean visit(CatchClause node) {
		                System.out.println(node);
		                return true;
		            }

		            @Override
		            public boolean visit(ConditionalExpression node) {

		                return true;
		            }

		            @Override
		            public boolean visit(DoStatement node) {

		                return true;
		            }

		            @Override
		            public boolean visit(EnhancedForStatement node) {

		                return true;
		            }

		            @Override
		            public boolean visit(EnumDeclaration node) {

		                return true;
		            }

		            @Override
		            public boolean visit(FieldDeclaration node) {

		                return true;
		            }

		            @Override
		            public boolean visit(ForStatement node) {

		                return true;
		            }

		            @Override
		            public boolean visit(IfStatement node) {

		                return true;
		            }

		            @Override
		            public boolean visit(MethodInvocation node) {

		                return true;
		            }

		            @Override
		            public boolean visit(PackageDeclaration node) {

		                return true;
		            }

		            @Override
		            public boolean visit(SwitchStatement node) {

		                return true;
		            }

		            @Override
		            public boolean visit(WhileStatement node) {

		                return true;
		            }

		            @Override
		            public boolean visit(FieldAccess node) {

		                return true;
		            }
			 });
        return result;
	}
	
	private static String loadAFileToString(File f) throws IOException {  
	    InputStream is = null;
	    String ret = null;
	    try {
	    	is = new BufferedInputStream( new FileInputStream(f) );
	    	long contentLength = f.length();
	    	ByteArrayOutputStream outstream = new ByteArrayOutputStream( contentLength > 0 ? (int) contentLength : 1024);
	    	byte[] buffer = new byte[4096];
	    	int len;
	    	while ((len = is.read(buffer)) > 0) {
	    		outstream.write(buffer, 0, len);	
	    	}
	    	outstream.close();
	    	ret = outstream.toString();
	    	//byte[] ba = outstream.toByteArray();
	    	//ret = new String(ba);
	    } finally {
	    	if(is!=null) {try{is.close();} catch(Exception e){} }
	    }
	    return ret;        
	}
}
