
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

import static cop5556fa19.Token.Kind.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

import cop5556fa19.Token.Kind;

public class Scanner {
	Reader r;
	private int pos = 0;
	private int line = 0;

	/**
	 * When checking the next value, if it doesn't have any sequence then we need to
	 * save the value of char here because we won't be able to fetch it again using
	 * r.read()
	 */
	private Optional<Character> lastChar = Optional.empty();

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		public LexicalException(String arg0) {
			super(arg0);
		}
	}

	public Scanner(Reader r) throws IOException {
		this.r = r;
	}

	public Token getNext() throws Exception {
		int current = r.read();

		if (current == -1)
			return new Token(EOF, "eof", pos, 0);

		char currentChar = (char) current;
		switch (currentChar) {
		case '+':
			return new Token(Kind.OP_PLUS, "+", pos++, line);
		case '-':
			return new Token(Kind.OP_MINUS, "-", pos++, line);
		case '*':
			return new Token(Kind.OP_TIMES, "*", pos++, line);
		case '/':
			// TODO : Check for //
			return new Token(Kind.OP_DIV, "/", pos++, line);
		case '%':
			return new Token(Kind.OP_MOD, "%", pos++, line);
		case '^':
			return new Token(Kind.OP_POW, "^", pos++, line);
		case '#':
			return new Token(Kind.OP_HASH, "#", pos++, line);
		case '&':
			return new Token(Kind.BIT_AMP, "&", pos++, line);
		case '~':
			// TODO : Check for ~=
			return new Token(Kind.BIT_XOR, "~", pos++, line);
		case '|':
			return new Token(Kind.BIT_OR, "|", pos++, line);
		case '<':
			// TODO : Check for <<
			// TODO : Check for <=
			return new Token(Kind.REL_LT, "<", pos++, line);
		case '>':
			// TODO : Check for >>
			// TODO : Check for >=
			return new Token(Kind.REL_GT, ">", pos++, line);
		case '=':
			// TODO : Check for ==
			return new Token(Kind.ASSIGN, "(", pos++, line);
		case '(':
			return new Token(Kind.LPAREN, "(", pos++, line);
		case ')':
			return new Token(Kind.RPAREN, ")", pos++, line);
		case '{':
			return new Token(Kind.LCURLY, "{", pos++, line);
		case '}':
			return new Token(Kind.RCURLY, "}", pos++, line);
		case '[':
			return new Token(Kind.LSQUARE, "[", pos++, line);
		case ']':
			return new Token(Kind.RSQUARE, "]", pos++, line);
		case ';':
			return new Token(Kind.SEMI, ";", pos++, line);
		case ':':
			// TODO : Check for ::
			return new Token(Kind.COLON, ":", pos++, line);
		case ',':
			return new Token(Kind.COMMA, ",", pos++, line);
		case '.':
			// TODO : Check for ..
			// TODO : Check for ...
			return new Token(Kind.DOT, ".", pos++, line);
		default:
			throw new LexicalException("Handle all the cases");
		}
	}

}
