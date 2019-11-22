package interpreter;

import cop5556fa19.AST.ASTNode;
import cop5556fa19.AST.Chunk;
import cop5556fa19.Parser;
import cop5556fa19.Scanner;
import interpreter.built_ins.print;
import interpreter.built_ins.println;
import interpreter.built_ins.toNumber;

import java.io.Reader;
import java.util.List;


public class Interpreter extends ASTVisitorAdapter {



	
	LuaTable _G; //global environment

	/* Instantiates and initializes global environment
	 * 
	 * Initially, the "standard library" routines implemented in Java are loaded.  For this assignment,
	 * this is just print and println.  
	 * 
	 * These functions impl
	 */
	void init_G() {
		_G = new LuaTable();
		_G.put("print", new print());
		_G.put("println", new println());
		_G.put("toNumber", new toNumber());
	}
	
	ASTNode root; //useful for debugging
		
	public Interpreter() {
		init_G();
	}
	

	
	@SuppressWarnings("unchecked")
	public List<LuaValue> load(Reader r) throws Exception {
		Scanner scanner = new Scanner(r); 
		Parser parser = new Parser(scanner);
		Chunk chunk = parser.parse();
		root = chunk;
//		Perform static analysis to prepare for goto.  Uncomment after u
		StaticAnalysis sa = new StaticAnalysis();
		chunk.visit(sa,null);
		chunk.visit(sa,null);
		//Interpret the program and return values returned from chunk.visit
		List<LuaValue> vals = (List<LuaValue>) chunk.visit(this,_G);
		return vals;
	}
	


	

}
