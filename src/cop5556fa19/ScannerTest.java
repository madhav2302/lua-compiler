/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites or repositories,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */

package cop5556fa19;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import cop5556fa19.Scanner.LexicalException;
import cop5556fa19.Token.Kind;

import static cop5556fa19.Token.Kind.*;

class ScannerTest {

	// I like this to make it easy to print objects and turn this output on and off
	static boolean doPrint = false;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	// ***********************************
	// Provided examples
	// ***********************************

	/**
	 * Example showing how to get input from a Java string literal.
	 * 
	 * In this case, the string is empty. The only Token that should be returned is
	 * an EOF Token.
	 * 
	 * This test case passes with the provided skeleton, and should also pass in
	 * your final implementation. Note that calling getNext again after having
	 * reached the end of the input should just return another EOF Token.
	 * 
	 */
	@Test
	void test0() throws Exception {
		Reader r = new StringReader("");
		Scanner s = new Scanner(r);
		Token t;
		show(t = s.getNext());
		assertEquals(EOF, t.kind);
		show(t = s.getNext());
		assertEquals(EOF, t.kind);
	}

	/**
	 * Example showing how to create a test case to ensure that an exception is
	 * thrown when illegal input is given.
	 * 
	 * This "@" character is illegal in the final scanner (except as part of a
	 * String literal or comment). So this test should remain valid in your complete
	 * Scanner.
	 */
	@Test
	void test1() throws Exception {
		Reader r = new StringReader("@");
		Scanner s = new Scanner(r);
		assertThrows(LexicalException.class, () -> {
			s.getNext();
		});
	}

	/**
	 * Example showing how to read the input from a file. Otherwise it is the same
	 * as test1.
	 *
	 */
	@Test
	void test2() throws Exception {
		String file = "testInputFiles/test2.input";
		Reader r = new BufferedReader(new FileReader(file));
		Scanner s = new Scanner(r);
		assertThrows(LexicalException.class, () -> {
			s.getNext();
		});
	}

	/**
	 * Another example. This test case will fail with the provided code, but should
	 * pass in your completed Scanner.
	 * 
	 * @throws Exception
	 */
	@Test
	void test3() throws Exception {
		Reader r = new StringReader(",,::==");
		Scanner s = new Scanner(r);
		Token t;
		show(t = s.getNext());
		assertEquals(t.kind, COMMA);
		assertEquals(t.text, ",");
		show(t = s.getNext());
		assertEquals(t.kind, COMMA);
		assertEquals(t.text, ",");

		show(t = s.getNext());
		assertEquals(t.kind, COLONCOLON);
		assertEquals(t.text, "::");

		show(t = s.getNext());
		assertEquals(t.kind, REL_EQEQ);
		assertEquals(t.text, "==");
	}
	
	@Test
	void testWithEverything() throws Exception {
		Reader r = new StringReader("+-*/%^#&~|<<>>//==00~=<=>1234567890=<>(22=){}[]::;:,.120.012.,,..,.");
		Scanner s = new Scanner(r);
		
		int pos = 0;
		assertToken(s, OP_PLUS, "+", pos++);
		assertToken(s, OP_MINUS, "-", pos++);
		assertToken(s, OP_TIMES, "*", pos++);
		assertToken(s, OP_DIV, "/", pos++);
		assertToken(s, OP_MOD, "%", pos++);
		assertToken(s, OP_POW, "^", pos++);
		assertToken(s, OP_HASH, "#", pos++);
		assertToken(s, BIT_AMP, "&", pos++);
		assertToken(s, BIT_XOR, "~", pos++);
		assertToken(s, BIT_OR, "|", pos++);
		assertToken(s, BIT_SHIFTL, "<<", pos++);
		pos++;

		assertToken(s, BIT_SHIFTR, ">>", pos++);
		pos++;

		assertToken(s, OP_DIVDIV, "//", pos++);
		pos++;

		assertToken(s, REL_EQEQ, "==", pos++);
		pos++;
		
		assertToken(s, INTLIT, "0", pos++);
		assertToken(s, INTLIT, "0", pos++);

		assertToken(s, REL_NOTEQ, "~=", pos++);
		pos++;

		assertToken(s, REL_LE, "<=", pos++);
		pos++;

		assertToken(s, REL_GT, ">", pos++);
		
		assertToken(s, INTLIT, "1234567890", pos);
		pos = pos + 10;

		
		assertToken(s, ASSIGN, "=", pos++);
		assertToken(s, REL_LT, "<", pos++);
		assertToken(s, REL_GT, ">", pos++);
		assertToken(s, LPAREN, "(", pos++);
		
		assertToken(s, INTLIT, "22", pos++);
		pos++;
		
		assertToken(s, ASSIGN, "=", pos++);
		assertToken(s, RPAREN, ")", pos++);
		assertToken(s, LCURLY, "{", pos++);
		assertToken(s, RCURLY, "}", pos++);
		assertToken(s, LSQUARE, "[", pos++);
		assertToken(s, RSQUARE, "]", pos++);
		assertToken(s, COLONCOLON, "::", pos++);
		pos++;

		assertToken(s, SEMI, ";", pos++);
		assertToken(s, COLON, ":", pos++);
		assertToken(s, COMMA, ",", pos++);
		assertToken(s, DOT, ".", pos++);
		
		assertToken(s, INTLIT, "120", pos++);
		pos++;
		pos++;
		
		assertToken(s, DOT, ".", pos++);
		assertToken(s, INTLIT, "0", pos++);
		assertToken(s, INTLIT, "12", pos++);
		pos++;
		assertToken(s, DOT, ".", pos++);

		assertToken(s, COMMA, ",", pos++);
		assertToken(s, COMMA, ",", pos++);
		assertToken(s, DOTDOT, "..", pos++);
		pos++;

		assertToken(s, COMMA, ",", pos++);
		assertToken(s, DOT, ".", pos++);
	}
	
	// ***********************************
	// Test For Integer Literals
	// ***********************************
	@Test
	void shouldThrowException_NumberOutOfRange() throws Exception {
		String outOfRangeNumber = String.valueOf(Long.MAX_VALUE);
		
		Reader r = new StringReader(outOfRangeNumber);
		Scanner s = new Scanner(r);
		
		String expectedMessage = outOfRangeNumber + " is out of range.";
		String messageInException = "";
		try {
			s.getNext();
		} catch (LexicalException e) {
			messageInException = e.getMessage();
		}
		
		assertEquals(messageInException, expectedMessage);
	}
	
	@Test
	void handleIntegerLiteral() throws Exception {
		Reader r = new StringReader("012345678");
		Scanner s = new Scanner(r);
		
		int pos = 0;
		assertToken(s, INTLIT, "0", pos++);
		assertToken(s, INTLIT, "12345678", pos++);
	}
	
	@Test
	void handleNonZeroDigitIntegerLiteral() throws Exception {
		Reader r = new StringReader("12345678");
		Scanner s = new Scanner(r);
		
		int pos = 0;
		assertToken(s, INTLIT, "12345678", pos++);
	}
	
	@Test
	void handleZeroDigitIntegerLiteral() throws Exception {
		Reader r = new StringReader("00000");
		Scanner s = new Scanner(r);
		
		int pos = 0;
		assertToken(s, INTLIT, "0", pos++);
		assertToken(s, INTLIT, "0", pos++);
		assertToken(s, INTLIT, "0", pos++);
		assertToken(s, INTLIT, "0", pos++);
		assertToken(s, INTLIT, "0", pos++);
		
	}
	
	// ***********************************
	// Test For Other Tokens
	// ***********************************	

	@Test
	void handleOtherTokens() throws Exception {
		Reader r = new StringReader("+-*/%^#&~|<<>>//==~=<=>=<>(=){}[]::;:,...,,..,.");
		Scanner s = new Scanner(r);

		int pos = 0;
		assertToken(s, OP_PLUS, "+", pos++);
		assertToken(s, OP_MINUS, "-", pos++);
		assertToken(s, OP_TIMES, "*", pos++);
		assertToken(s, OP_DIV, "/", pos++);
		assertToken(s, OP_MOD, "%", pos++);
		assertToken(s, OP_POW, "^", pos++);
		assertToken(s, OP_HASH, "#", pos++);
		assertToken(s, BIT_AMP, "&", pos++);
		assertToken(s, BIT_XOR, "~", pos++);
		assertToken(s, BIT_OR, "|", pos++);
		assertToken(s, BIT_SHIFTL, "<<", pos++);
		pos++;

		assertToken(s, BIT_SHIFTR, ">>", pos++);
		pos++;

		assertToken(s, OP_DIVDIV, "//", pos++);
		pos++;

		assertToken(s, REL_EQEQ, "==", pos++);
		pos++;

		assertToken(s, REL_NOTEQ, "~=", pos++);
		pos++;

		assertToken(s, REL_LE, "<=", pos++);
		pos++;

		assertToken(s, REL_GE, ">=", pos++);
		pos++;

		assertToken(s, REL_LT, "<", pos++);
		assertToken(s, REL_GT, ">", pos++);
		assertToken(s, LPAREN, "(", pos++);
		assertToken(s, ASSIGN, "=", pos++);
		assertToken(s, RPAREN, ")", pos++);
		assertToken(s, LCURLY, "{", pos++);
		assertToken(s, RCURLY, "}", pos++);
		assertToken(s, LSQUARE, "[", pos++);
		assertToken(s, RSQUARE, "]", pos++);
		assertToken(s, COLONCOLON, "::", pos++);
		pos++;

		assertToken(s, SEMI, ";", pos++);
		assertToken(s, COLON, ":", pos++);
		assertToken(s, COMMA, ",", pos++);
		assertToken(s, DOTDOTDOT, "...", pos++);
		pos++;
		pos++;

		assertToken(s, COMMA, ",", pos++);
		assertToken(s, COMMA, ",", pos++);
		assertToken(s, DOTDOT, "..", pos++);
		pos++;

		assertToken(s, COMMA, ",", pos++);
		assertToken(s, DOT, ".", pos++);
	}

	// ***********************************
	// Common methods
	// ***********************************
	
	void assertToken(Scanner s, Kind kind, String text, int pos) throws Exception {
		assertToken(s, kind, text, pos, 0);
	}
	
	void assertToken(Scanner s, Kind kind, String text, int pos, int line) throws Exception {
		assertToken(s, new Token(kind, text, pos, line));
	}

	void assertToken(Scanner s, Token expected) throws Exception {
		Token found;
		show(found = s.getNext());
		assertEquals(expected.kind, found.kind);
		assertEquals(expected.text, found.text);
		assertEquals(expected.pos, found.pos);
		assertEquals(expected.line, found.line);
	}
}
