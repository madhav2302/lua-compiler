
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cop5556fa19.Token.Kind;

public class Scanner {

	@SuppressWarnings("serial")
	private static final Map<String, Kind> KEYWORDS_TO_KIND = new HashMap<String, Kind>() {
		{
			put("and", Kind.KW_and);
			put("break", Kind.KW_break);
			put("do", Kind.KW_do);
			put("else", Kind.KW_else);
			put("elseif", Kind.KW_elseif);
			put("end", Kind.KW_end);
			put("false", Kind.KW_false);
			put("for", Kind.KW_for);
			put("function", Kind.KW_function);
			put("goto", Kind.KW_goto);
			put("if", Kind.KW_if);
			put("in", Kind.KW_in);
			put("local", Kind.KW_local);
			put("nil", Kind.KW_nil);
			put("not", Kind.KW_not);
			put("or", Kind.KW_or);
			put("repeat", Kind.KW_repeat);
			put("return", Kind.KW_return);
			put("then", Kind.KW_then);
			put("true", Kind.KW_true);
			put("until", Kind.KW_until);
			put("while", Kind.KW_while);
		}
	};

	private static final Set<String> KEYWORDS = KEYWORDS_TO_KIND.keySet();

	private enum State {
		START, AFTER_DIV, AFTER_XOR, AFTER_LT, AFTER_GT, AFTER_EQ, AFTER_COLON, AFTER_DOT, AFTER_DOTDOT, IN_DIGIT,
		IN_IDEN, IN_STRING, AFTER_CR, AFTER_HYPHEN;
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

		StringBuilder sb = new StringBuilder();

		int pos = -1;
		int line = -1;

		while (t == null) {
			switch (state) {
			case START:
				pos = currentPos;
				line = currentLine;

				switch (ch) {
				// WHITE SPACES
				case ' ':
				case '\t':
				case '\f':
					getChar();
					break;
				// LINE TERMINATORS
				case '\n':
					currentPos = -1;
					currentLine++;
					getChar();
					break;
				case '\r':
					state = State.AFTER_CR;
					getChar();
					break;
				// OTHER TOKENS
				case '+':
					t = new Token(Kind.OP_PLUS, "+", pos, line);
					getChar();
					break;
				case '-':
					state = State.AFTER_HYPHEN;
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
				case '0':
					t = new Token(Kind.INTLIT, "0", pos, line);
					getChar();
					break;
				// STRING LITERAL
				case '"':
				case '\'':
					state = State.IN_STRING;
					sb.append((char) ch);
					getChar();
					break;
				// EOF
				case -1:
					t = new Token(Kind.EOF, "EOF", pos, line);
					break;
				default:
					// INTEGER LITERAL
					if (Character.isDigit((char) ch)) {
						state = State.IN_DIGIT;
						sb.append(Character.getNumericValue(ch));
						getChar();
						break;
					}
					// NAME and KEYWORD
					if ((ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122)) {
						state = State.IN_IDEN;
						sb.append((char) ch);
						getChar();
						break;
					}

					throw new LexicalException(
							"Illegal character: " + (char) ch + " at line: " + line + ", pos: " + pos);
				}
				break;
			case AFTER_CR:
				if (ch == '\n') {
					currentPos = -1;
					currentLine++;
					getChar();
				} else {
					currentPos = 0;
					currentLine++;
				}

				state = State.START;
				break;
			case AFTER_HYPHEN:
				if (ch == '-') {
					while (!isLineTerminator()) {
						
					}
					state = State.START;
				} else {
					t = new Token(Kind.OP_MINUS, "-", pos, line);
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
			case IN_DIGIT:
				if (Character.isDigit((char) ch)) {
					sb.append(Character.getNumericValue(ch));
					getChar();
				} else {
					String result = sb.toString();
					try {
						Integer.parseInt(result);
					} catch (NumberFormatException e) {
						throw new LexicalException("Number out of range: " + result);
					}
					t = new Token(Kind.INTLIT, result, pos, line);
				}
				break;
			case IN_IDEN:
				if ((ch >= 65 && ch <= 90) || (ch >= 97 && ch <= 122) || (ch >= 48 && ch <= 57) || ch == 95
						|| ch == 36) {
					sb.append((char) ch);
					getChar();
				} else {
					String result = sb.toString();
					t = new Token(KEYWORDS.contains(result) ? KEYWORDS_TO_KIND.get(result) : Kind.NAME, result, pos,
							line);
				}
				break;
			case IN_STRING:
				boolean isDoubleQuoteEnd = sb.charAt(0) == '"';

				if (ch == -1) {
					throw new LexicalException("Unexpected EOF for string literal at line: " + line + ",pos: " + pos);
				}

				if ((isDoubleQuoteEnd && (char) ch == '"') || (!isDoubleQuoteEnd && (char) ch == '\'')) {
					sb.append((char) ch);
					getChar();
					t = new Token(Kind.STRINGLIT, sb.toString(), pos, line);
					break;
				}

				if ((isDoubleQuoteEnd && (char) ch == '\'') || (!isDoubleQuoteEnd && (char) ch == '"')) {
					throw new LexicalException("Illegal Character Found in string literal: " + (char) ch);
				}

				if (ch == '\\') {
					getChar();
					if (ch == -1) {
						throw new LexicalException(
								"Unexpected EOF for string literal at line: " + line + ",pos: " + pos);
					}
					switch (ch) {
					case 'a':
					case 'b':
					case 'f':
					case 'n':
					case 'r':
					case 't':
					case 'v':
					case '\\':
					case '"':
					case '\'':
						sb.append('\\'); // Append the escape character we skipped
						sb.append((char) ch);
						getChar();
						break;
					default:
						throw new LexicalException("Illegal Character Found in string literal \\");

					}
				} else {
					sb.append((char) ch);
					getChar();

				}
				break;
			default:
				throw new LexicalException("Unknown state found: " + state);
			}
		}

		return t;
	}

	private boolean isLineTerminator() throws IOException {
		if (ch == '\n') {
			currentLine++;
			currentPos = -1;
			getChar();
			return true;
		}

		if (ch == '\r') {
			currentLine++;

			getChar();

			if (ch == '\n') {
				currentPos = -1;
				currentLine++;
				getChar();
			} else {
				currentPos = 0;
				currentLine++;
			}
		}
		
		return false;
	}

	void getChar() throws IOException {
		ch = r.read();
		currentPos++;
	}
}
