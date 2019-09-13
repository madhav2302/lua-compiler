
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

import java.io.IOException;
import java.io.Reader;
import cop5556fa19.Token.Kind;

public class Scanner {

	private enum State {
		START, AFTER_DIV, AFTER_XOR, AFTER_LT, AFTER_GT, AFTER_EQ, AFTER_COLON, AFTER_DOT, AFTER_DOTDOT
	}

	Reader r;
	private int currentPos = -1;
	private int currentLine = -1;

	int ch;

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
		// Read value for the first time
		if (currentPos == -1 && currentLine == -1) {
			getChar();
			currentPos = 0;
			currentLine = 0;
		}

		Token t = null;
		State state = State.START;

		int pos = -1;
		int line = -1;

		while (t == null) {
			switch (state) {
			case START:
				pos = currentPos;
				line = currentLine;

				switch (ch) {
				case '+':
					t = new Token(Kind.OP_PLUS, "+", pos, line);
					getChar();
					break;
				case '-':
					// TODO : Handle Comments
					t = new Token(Kind.OP_MINUS, "-", pos, line);
					getChar();
					break;
				case '*':
					t = new Token(Kind.OP_TIMES, "*", pos, line);
					getChar();
					break;
				case '/':
					state = State.AFTER_DIV;
					getChar();
					break;
				case '%':
					t = new Token(Kind.OP_MOD, "%", pos, line);
					getChar();
					break;
				case '^':
					t = new Token(Kind.OP_POW, "^", pos, line);
					getChar();
					break;
				case '#':
					t = new Token(Kind.OP_HASH, "#", pos, line);
					getChar();
					break;
				case '&':
					t = new Token(Kind.BIT_AMP, "&", pos, line);
					getChar();
					break;
				case '~':
					state = State.AFTER_XOR;
					getChar();
					break;
				case '|':
					t = new Token(Kind.BIT_OR, "|", pos, line);
					getChar();
					break;
				case '<':
					state = State.AFTER_LT;
					getChar();
					break;
				case '>':
					state = State.AFTER_GT;
					getChar();
					break;
				case '=':
					state = State.AFTER_EQ;
					getChar();
					break;
				case '(':
					t = new Token(Kind.LPAREN, "(", pos, line);
					getChar();
					break;
				case ')':
					t = new Token(Kind.RPAREN, ")", pos, line);
					getChar();
					break;
				case '{':
					t = new Token(Kind.LCURLY, "{", pos, line);
					getChar();
					break;
				case '}':
					t = new Token(Kind.RCURLY, "}", pos, line);
					getChar();
					break;
				case '[':
					t = new Token(Kind.LSQUARE, "[", pos, line);
					getChar();
					break;
				case ']':
					t = new Token(Kind.RSQUARE, "]", pos, line);
					getChar();
					break;
				case ';':
					t = new Token(Kind.SEMI, ";", pos, line);
					getChar();
					break;
				case ':':
					state = State.AFTER_COLON;
					getChar();
					break;
				case ',':
					t = new Token(Kind.COMMA, ",", pos, line);
					getChar();
					break;
				case '.':
					state = State.AFTER_DOT;
					getChar();
					break;
				default:
					// TODO: Implement things

				}
				break;
			case AFTER_DIV:
				if (ch == '/') {
					t = new Token(Kind.OP_DIVDIV, "//", pos, line);
					getChar();
				} else {
					t = new Token(Kind.OP_DIV, "/", pos, line);
				}
				break;
			case AFTER_XOR:
				if (ch == '=') {
					t = new Token(Kind.REL_NOTEQ, "~=", pos, line);
					getChar();
				} else {
					t = new Token(Kind.BIT_XOR, "~", pos, line);
				}
				break;
			case AFTER_LT:
				if (ch == '<') {
					t = new Token(Kind.BIT_SHIFTL, "<<", pos, line);
					getChar();
				} else if (ch == '=') {
					t = new Token(Kind.REL_LE, "<=", pos, line);
					getChar();
				} else {
					t = new Token(Kind.REL_LT, "<", pos, line);
				}
				break;
			case AFTER_GT:
				if (ch == '>') {
					t = new Token(Kind.BIT_SHIFTR, ">>", pos, line);
					getChar();
				} else if (ch == '=') {
					t = new Token(Kind.REL_GE, ">=", pos, line);
					getChar();
				} else {
					t = new Token(Kind.REL_GT, ">", pos, line);
				}
				break;
			case AFTER_EQ:
				if (ch == ('=')) {
					t = new Token(Kind.REL_EQEQ, "==", pos, line);
					getChar();
				} else {
					t = new Token(Kind.ASSIGN, "=", pos, line);
				}
				break;
			case AFTER_COLON:
				if (ch == ':') {
					t = new Token(Kind.COLONCOLON, "::", pos, line);
					getChar();
				} else {
					t = new Token(Kind.COLON, ":", pos, line);
				}
				break;
			case AFTER_DOT:
				if (ch == '.') {
					state = State.AFTER_DOTDOT;
					getChar();
				} else {
					t = new Token(Kind.DOT, ".", pos, line);
				}
				break;
			case AFTER_DOTDOT:
				if (ch == '.') {
					t = new Token(Kind.DOTDOTDOT, "...", pos, line);
					getChar();
				} else {
					t = new Token(Kind.DOTDOT, "..", pos, line);
				}
				break;
			default:
				// TODO
				break;
			}
		}

		return t;
	}

	void getChar() throws IOException {
		ch = r.read();
		currentPos++;
	}
}
