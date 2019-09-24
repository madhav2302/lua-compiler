/**
 * Developed  for the class project in COP5556 Programming Language Principles
 * at the University of Florida, Fall 2019.
 * <p>
 * This software is solely for the educational benefit of students
 * enrolled in the course during the Fall 2019 semester.
 * <p>
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 *
 * @Beverly A. Sanders, 2019
 */

package cop5556fa19;

import cop5556fa19.AST.*;
import cop5556fa19.Token.Kind;

import java.util.Arrays;

import static cop5556fa19.Token.Kind.*;

public class ExpressionParser {

    final Scanner scanner;
    Token t;  //invariant:  this is the next token

    ExpressionParser(Scanner s) throws Exception {
        this.scanner = s;
        t = scanner.getNext(); //establish invariant
    }

    // exp ::= andExp {or andExp}
    Exp exp() throws Exception {
        Token first = t;
        Exp e0 = andExp();
        while (isKind(KW_or)) {
            Token op = consume();
            Exp e1 = andExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }
        return e0;
    }

    // andExp ::= condExp {and condExp}
    private Exp andExp() throws Exception {
        Token first = t;
        Exp e0 = condExp();

        while (isKind(KW_and)) {
            Token op = consume();
            Exp e1 = condExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // condExp ::= bitOrExp {(<|>|<=|>=|~=|==) bitOrExp}
    private Exp condExp() throws Exception {
        Token first = t;
        Exp e0 = bitOrExp();

        while (isKind(REL_LT, REL_GT, REL_LE, REL_GE, REL_NOTEQ, REL_EQEQ)) {
            Token op = consume();
            Exp e1 = bitOrExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // bitOrExp ::= xOrExp {| xOrExp}
    private Exp bitOrExp() throws Exception {
        Token first = t;
        Exp e0 = xOrExp();

        while (isKind(BIT_OR)) {
            Token op = consume();
            Exp e1 = xOrExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // xOrExp ::= bitAmpExp {~ bitAmpExp}
    private Exp xOrExp() throws Exception {
        Token first = t;
        Exp e0 = bitAmpExp();

        while (isKind(BIT_XOR)) {
            Token op = consume();
            Exp e1 = bitAmpExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // bitAmpExp ::= shiftExp {& shiftExp}
    private Exp bitAmpExp() throws Exception {
        Token first = t;
        Exp e0 = shiftExp();

        while (isKind(BIT_AMP)) {
            Token op = consume();
            Exp e1 = shiftExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // shiftExp ::= concatExp {(<<|>>) concatExp}
    private Exp shiftExp() throws Exception {
        Token first = t;
        Exp e0 = concatExp();

        while (isKind(BIT_SHIFTL, BIT_SHIFTR)) {
            Token op = consume();
            Exp e1 = concatExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // concatExp ::= weakCalcExp {.. weakCalcExp} // Right Associativity
    private Exp concatExp() throws Exception {
        Token first = t;
        Exp e0 = weakCalcExp();

        while (isKind(DOTDOT)) {
            Token op = consume();
            Exp e1 = weakCalcExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // weakCalcExp ::= otherCalcExp {(+|-) otherCalcExp}
    private Exp weakCalcExp() throws Exception {
        Token first = t;
        Exp e0 = otherCalcExp();

        while (isKind(OP_PLUS, OP_MINUS)) {
            Token op = consume();
            Exp e1 = otherCalcExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // otherCalcExp ::= otherExp {(/|*|//|%) otherExp}
    private Exp otherCalcExp() throws Exception {
        Token first = t;
        Exp e0 = otherExp();

        while (isKind(OP_DIV, OP_DIVDIV, OP_TIMES, OP_MOD)) {
            Token op = consume();
            Exp e1 = otherExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    private Exp otherExp() throws Exception {
        if (isKind(INTLIT)) return new ExpInt(consume());
        if (isKind(STRINGLIT)) return new ExpString(consume());
        if (isKind(KW_nil)) return new ExpNil(consume());
        if (isKind(KW_false)) return new ExpFalse(consume());
        if (isKind(KW_true)) return new ExpTrue(consume());
        if (isKind(DOTDOTDOT)) return new ExpVarArgs(consume());
        if (isKind(NAME)) return new ExpName(consume());

        if (isKind(LPAREN)) {
            consume();
            Exp result = exp();
            match(RPAREN);
            return result;
        }

        throw new UnsupportedOperationException();
    }


    private Block block() {
        return new Block(null);  //this is OK for Assignment 2
    }

    protected boolean isKind(Kind kind) {
        return t.kind == kind;
    }

    protected boolean isKind(Kind... kinds) {
        for (Kind k : kinds) {
            if (k == t.kind)
                return true;
        }
        return false;
    }

    /**
     * @param kind
     * @return
     * @throws Exception
     */
    Token match(Kind kind) throws Exception {
        Token tmp = t;
        if (isKind(kind)) {
            consume();
            return tmp;
        }
        error(kind);
        return null; // unreachable
    }

    /**
     * @param kinds
     * @return
     * @throws Exception
     */
    Token match(Kind... kinds) throws Exception {
        Token tmp = t;
        if (isKind(kinds)) {
            consume();
            return tmp;
        }
        StringBuilder sb = new StringBuilder();
        for (Kind kind1 : kinds) {
            sb.append(kind1).append(kind1).append(" ");
        }
        error(kinds);
        return null; // unreachable
    }

    Token consume() throws Exception {
        Token tmp = t;
        t = scanner.getNext();
        return tmp;
    }

    void error(Kind... expectedKinds) throws SyntaxException {
        String kinds = Arrays.toString(expectedKinds);
        String message;
        if (expectedKinds.length == 1) {
            message = "Expected " + kinds + " at " + t.line + ":" + t.pos;
        } else {
            message = "Expected one of" + kinds + " at " + t.line + ":" + t.pos;
        }
        throw new SyntaxException(t, message);
    }

    void error(Token t, String m) throws SyntaxException {
        String message = m + " at " + t.line + ":" + t.pos;
        throw new SyntaxException(t, message);
    }

    @SuppressWarnings("serial")
    class SyntaxException extends Exception {
        Token t;

        public SyntaxException(Token t, String message) {
            super(t.line + ":" + t.pos + " " + message);
        }
    }


}
