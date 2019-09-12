
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
		Token mayBeNext;
		int current;
		char currentChar;
		int currentPos = pos;
		pos++;

		if (lastChar.isPresent()) {
			currentChar = lastChar.get();
			lastChar = Optional.empty();
		} else {
			current = r.read();

			if (current == -1)
				return new Token(EOF, "eof", currentPos, 0);

			currentChar = (char) current;
		}

		switch (currentChar) {
		case '0':
			return new Token(Kind.INTLIT, "0", currentPos, line);
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return findIntegerLit(currentChar, currentPos);

		// Other Tokens
		case '+':
			return new Token(Kind.OP_PLUS, "+", currentPos, line);
		case '-':
			return new Token(Kind.OP_MINUS, "-", currentPos, line);
		case '*':
			return new Token(Kind.OP_TIMES, "*", currentPos, line);
		case '/':
			mayBeNext = new Token(Kind.OP_DIV, "/", currentPos, line);

			if (isNextOtherToken('/'))
				return new Token(Kind.OP_DIVDIV, "//", currentPos, line);

			return mayBeNext;
		case '%':
			return new Token(Kind.OP_MOD, "%", currentPos, line);
		case '^':
			return new Token(Kind.OP_POW, "^", currentPos, line);
		case '#':
			return new Token(Kind.OP_HASH, "#", currentPos, line);
		case '&':
			return new Token(Kind.BIT_AMP, "&", currentPos, line);
		case '~':
			mayBeNext = new Token(Kind.BIT_XOR, "~", currentPos, line);

			if (isNextOtherToken('='))
				return new Token(Kind.REL_NOTEQ, "~=", currentPos, line);

			return mayBeNext;
		case '|':
			return new Token(Kind.BIT_OR, "|", currentPos, line);
		case '<':
			mayBeNext = new Token(Kind.REL_LT, "<", currentPos, line);

			if (isNextOtherToken('<'))
				return new Token(Kind.BIT_SHIFTL, "<<", currentPos, line);
			if (isNextOtherToken('='))
				return new Token(Kind.REL_LE, "<=", currentPos, line);
			return mayBeNext;
		case '>':
			mayBeNext = new Token(Kind.REL_GT, ">", currentPos, line);

			if (isNextOtherToken('>'))
				return new Token(Kind.BIT_SHIFTR, ">>", currentPos, line);
			if (isNextOtherToken('='))
				return new Token(Kind.REL_GE, ">=", currentPos, line);
			return mayBeNext;
		case '=':
			mayBeNext = new Token(Kind.ASSIGN, "=", currentPos, line);
			if (isNextOtherToken('='))
				return new Token(Kind.REL_EQEQ, "==", currentPos, line);
			return mayBeNext;
		case '(':
			return new Token(Kind.LPAREN, "(", currentPos, line);
		case ')':
			return new Token(Kind.RPAREN, ")", currentPos, line);
		case '{':
			return new Token(Kind.LCURLY, "{", currentPos, line);
		case '}':
			return new Token(Kind.RCURLY, "}", currentPos, line);
		case '[':
			return new Token(Kind.LSQUARE, "[", currentPos, line);
		case ']':
			return new Token(Kind.RSQUARE, "]", currentPos, line);
		case ';':
			return new Token(Kind.SEMI, ";", currentPos, line);
		case ':':
			mayBeNext = new Token(Kind.COLON, ":", currentPos, line);
			if (isNextOtherToken(':'))
				return new Token(Kind.COLONCOLON, "::", currentPos, line);
			return mayBeNext;
		case ',':
			return new Token(Kind.COMMA, ",", currentPos, line);
		case '.':
			mayBeNext = new Token(Kind.DOT, ".", currentPos, line);

			if (isNextOtherToken('.')) {
				mayBeNext = new Token(Kind.DOTDOT, "..", currentPos, line);

				if (isNextOtherToken('.')) {
					return new Token(Kind.DOTDOTDOT, "...", currentPos, line);
				}
			}
			return mayBeNext;
		default:
			throw new LexicalException("Handle all the cases");
		}
	}

	private Token findIntegerLit(char currentChar, int currentPos) throws IOException, LexicalException {
		StringBuilder sb = new StringBuilder();
		sb.append(currentChar);

		int current;
		while ((current = r.read()) != -1) {
			currentChar = (char) current;

			if (isInteger(String.valueOf(currentChar))) {
				pos++;
				sb.append(currentChar);
			} else {
				lastChar = Optional.of(currentChar);
				break;
			}
		}

		String result = sb.toString();
		if (isInteger(result))
			return new Token(Kind.INTLIT, result, currentPos, line);
		else
			throw new LexicalException(result + " is out of range.");

	}

	private boolean isInteger(String input) {
		try {
			Integer.parseInt(String.valueOf(input));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * It is only use for checking while other tokens
	 * 
	 * @param c - maybe next char
	 * @return true if next char is 'c'
	 * @throws IOException
	 */
	private boolean isNextOtherToken(char c) throws IOException {
		if (lastChar.isPresent()) {
			if (lastChar.get() == c) {
				pos++;
				lastChar = Optional.empty();
				return true;
			} else
				return false;
		}

		int current;
		if ((current = r.read()) != -1) {
			char currentChar = (char) current;

			if ((char) current == c) {
				pos++;
				return true;
			}

			lastChar = Optional.of(new Character(currentChar));
		}
		return false;
	}
}
