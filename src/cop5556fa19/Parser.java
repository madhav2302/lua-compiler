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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cop5556fa19.Token.Kind.*;

public class Parser {

    final Scanner scanner;
    Token t;  //invariant:  this is the next token

    public Parser(Scanner s) throws Exception {
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
        List<Exp> concatExps = new ArrayList<>();

        Token first = t;
        Exp e0 = weakCalcExp();

        concatExps.add(e0);

        while (isKind(DOTDOT)) {
            consume();
            Exp e1 = weakCalcExp();
            concatExps.add(e1);
        }

        return constructRightAssociativityExp(concatExps, first, DOTDOT);
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

    // otherCalcExp ::= unaryExp {(/|*|//|%) unaryExp}
    private Exp otherCalcExp() throws Exception {
        Token first = t;
        Exp e0 = unaryExp();

        while (isKind(OP_DIV, OP_DIVDIV, OP_TIMES, OP_MOD)) {
            Token op = consume();
            Exp e1 = unaryExp();
            e0 = new ExpBinary(first, e0, op, e1);
        }

        return e0;
    }

    // unaryExp ::= (not|-|~|#) powerExp | powerExp
    private Exp unaryExp() throws Exception {
        if (isKind(KW_not, OP_MINUS, OP_HASH, BIT_XOR)) {
            Token first = consume();
            // Multi Unary like (- -2) or (not #~1) - Failed in assignment 2
            return new ExpUnary(first, first.kind, unaryExp());
        } else {
            return powerExp();
        }
    }

    // powerExp ::= otherExp {^ otherExp}
    private Exp powerExp() throws Exception {
        List<Exp> powerExps = new ArrayList<>();

        Token first = t;
        Exp e0 = otherExp();

        powerExps.add(e0);

        while (isKind(OP_POW)) {
            consume();
            Exp e1 = otherExp();
            powerExps.add(e1);
        }

        return constructRightAssociativityExp(powerExps, first, OP_POW);
    }

    private Exp otherExp() throws Exception {
        if (isKind(KW_nil)) return new ExpNil(consume());
        if (isKind(KW_false)) return new ExpFalse(consume());
        if (isKind(KW_true)) return new ExpTrue(consume());
        if (isKind(INTLIT)) return new ExpInt(consume());
        if (isKind(STRINGLIT)) return new ExpString(consume());
        if (isKind(DOTDOTDOT)) return new ExpVarArgs(consume());

        // Function Def
        if (isKind(KW_function)) {
            Token firstToken = consume(); // consume `function`
            return new ExpFunction(firstToken, functionBody());
        }

        // Prefix Exp
        if (isKind(NAME, LPAREN)) return prefixExp();

        // Table Constructor
        if (isKind(LCURLY)) {
            Token firstToken = consume(); // consume LCURLY
            if (isKind(RCURLY)) {
                consume();
                return new ExpTable(firstToken, Collections.emptyList());
            }

            List<Field> fieldList = fieldList();
            match(RCURLY);
            return new ExpTable(firstToken, fieldList);
        }

        throw new SyntaxException(t, "Illegal token found");
    }

    private FuncBody functionBody() throws Exception {
        Token firstToken = match(LPAREN);
        ParList parList = isKind(RPAREN) ? new ParList(t, Collections.emptyList(), false) : parList();
        match(RPAREN);
        Block block = block();
        match(KW_end);

        return new FuncBody(firstToken, parList, block);
    }

    private ParList parList() throws Exception {
        if (isKind(DOTDOTDOT)) return new ParList(consume(), Collections.emptyList(), true);

        boolean hasVarArgs = false;
        Token firstToken = match(NAME);

        List<Name> nameList = new ArrayList<>();
        nameList.add(new Name(firstToken, firstToken.getName()));

        while (isKind(COMMA)) {
            consume();
            if (isKind(NAME)) {
                Token nameToken = consume();
                nameList.add(new Name(nameToken, nameToken.getName()));
            } else {
                match(DOTDOTDOT);
                hasVarArgs = true;
                break;
            }
        }
        return new ParList(firstToken, nameList, hasVarArgs);
    }

    private Block block() throws Exception {
        Token first = t;
        List<Stat> stats = statList();

        if (isKind(KW_return)) stats.add(returnStat());

        return new Block(first, stats);
    }

    private List<Field> fieldList() throws Exception {
        List<Field> fieldList = new ArrayList<>();
        fieldList.add(field());

        while (isKind(COMMA, SEMI)) {
            consume();

            if (isKind(LSQUARE, NAME, KW_nil, KW_false, KW_true, INTLIT, STRINGLIT, DOTDOTDOT, KW_function, LPAREN, LCURLY)) {
                fieldList.add(field());
            } else break;
        }

        return fieldList;
    }

    private Field field() throws Exception {
        if (isKind(LSQUARE)) {
            Token firstToken = consume();
            Exp key = exp();
            match(RSQUARE);
            match(ASSIGN);
            Exp value = exp();
            return new FieldExpKey(firstToken, key, value);
        }

        if (isKind(NAME)) {
            Token nameToken = consume();

            if (isKind(ASSIGN)) {
                match(ASSIGN);
                Exp exp = exp();
                return new FieldNameKey(nameToken, new Name(nameToken, nameToken.getName()), exp);
            } else {
                return new FieldImplicitKey(nameToken, prefixExpTail(new ExpName(nameToken)));
            }
        }

        return new FieldImplicitKey(null, exp());
    }

    public List<Exp> expList() throws Exception {
        List<Exp> result = new ArrayList<>();
        result.add(exp());

        while (isKind(COMMA)) {
            consume();
            result.add(exp());
        }

        return result;
    }

    /*
    stat ::=    ‘;’ | varlist ‘=’ explist | label | break | goto Name |
                do block end | while exp do block end | repeat block until exp |
                if exp then block { elseif exp then block } [ else block ] end |
                for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end |
                for namelist in explist do block end | function funcname funcbody |
                local function Name funcbody | local namelist [‘=’ explist]
     */
    public List<Stat> statList() throws Exception {
        List<Stat> result = new ArrayList<>();

        while (true) {
            //  ‘;’
            if (isKind(SEMI)) {
                consume();
            }
            // label
            else if (isKind(COLONCOLON)) {
                consume();
                Token label = match(NAME);
                result.add(new StatLabel(label, new Name(label, label.text)));
                match(COLONCOLON);
            }
            // break
            else if (isKind(KW_break)) {
                result.add(new StatBreak(consume()));
            }
            // goto Name
            else if (isKind(KW_goto)) {
                Token first = consume();
                Token name = match(NAME);
                result.add(new StatGoto(first, new Name(name, name.text)));
            }
            // do block end
            else if (isKind(KW_do)) {
                Token first = consume();
                Block b = block();
                match(KW_end);
                result.add(new StatDo(first, b));
            }
            // while exp do block end
            else if (isKind(KW_while)) {
                Token first = consume();
                Exp e = exp();
                match(KW_do);
                Block b = block();
                match(KW_end);
                result.add(new StatWhile(first, e, b));
            }
            // repeat block until exp
            else if (isKind(KW_repeat)) {
                Token first = consume();
                Block b = block();
                match(KW_until);
                Exp e = exp();
                result.add(new StatRepeat(first, b, e));
            }
            // if exp then block { elseif exp then block } [ else block ] end
            else if (isKind(KW_if)) {
                List<Exp> exps = new ArrayList<>();
                List<Block> blocks = new ArrayList<>();
                Token first = consume();
                exps.add(exp());
                match(KW_then);
                blocks.add(block());

                while (isKind(KW_elseif)) {
                    consume();
                    exps.add(exp());
                    match(KW_then);
                    blocks.add(block());
                }

                if (isKind(KW_else)) {
                    consume();
                    blocks.add(block());
                }

                match(KW_end);
                result.add(new StatIf(first, exps, blocks));
            }
            // for Name ‘=’ exp ‘,’ exp [‘,’ exp] do block end
            // for namelist in explist do block end
            else if (isKind(KW_for)) {
                Token first = consume();
                Token name = match(NAME);

                if (isKind(ASSIGN)) {
                    consume();
                    Exp e1 = exp();

                    match(COMMA);

                    Exp e2 = exp();

                    Exp e3 = null;
                    if (isKind(COMMA)) {
                        consume();
                        e3 = exp();
                    }

                    match(KW_do);
                    Block b = block();
                    match(KW_end);

                    result.add(new StatFor(first, new ExpName(name), e1, e2, e3, b));
                } else {
                    List<ExpName> nameList = new ArrayList<>();
                    nameList.add(new ExpName(name));

                    while (isKind(COMMA)) {
                        consume();
                        Token nameToken = match(NAME);
                        nameList.add(new ExpName(nameToken));
                    }

                    match(KW_in);

                    List<Exp> exps = expList();
                    match(KW_do);

                    Block b = block();
                    match(KW_end);

                    result.add(new StatForEach(first, nameList, exps, b));
                }
            }
            // function funcname funcbody
            else if (isKind(KW_function)) {
                Token first = consume();
                FuncName funcName = functionName();
                FuncBody funcBody = functionBody();

                result.add(new StatFunction(first, funcName, funcBody));
            }
            // local function Name funcbody
            // local namelist [‘=’ explist]
            else if (isKind(KW_local)) {
                Token first = consume();
                if (isKind(KW_function)) {
                    Token name = match(NAME);
                    FuncBody funcBody = functionBody();
                    result.add(new StatLocalFunc(first, new FuncName(name, new ExpName(name)), funcBody));
                } else {
                    List<ExpName> nameList = new ArrayList<>();
                    nameList.add(new ExpName(match(NAME)));

                    while (isKind(COMMA)) {
                        consume();
                        nameList.add(new ExpName(match(NAME)));
                    }

                    List<Exp> expList = new ArrayList<>();

                    if (isKind(ASSIGN)) {
                        consume();

                        expList = expList();
                    }

                    result.add(new StatLocalAssign(first, nameList, expList));
                }
            }
            // varlist ‘=’ explist
            else if (isKind(NAME, LPAREN)) {
                List<Exp> varList = varList();
                match(ASSIGN);
                List<Exp> expList = expList();
                result.add(new StatAssign(null, varList, expList));
            } else {
                break;
            }
        }

        return result;
    }

    public List<Exp> varList() throws Exception {
        List<Exp> result = new ArrayList<>();
        result.add(prefixExp());

        while (isKind(COMMA)) {
            consume();
            result.add(prefixExp());
        }

        return result;
    }

    public Exp prefixExp() throws Exception {
        if (isKind(NAME)) {
            Token match = match(NAME);
            ExpName expName = new ExpName(match);
            return prefixExpTail(expName);
        } else {
            match(LPAREN);
            Exp exp = exp();
            match(RPAREN);

            return prefixExpTail(exp);
        }
    }

    public Exp prefixExpTail(Exp exp) throws Exception {
        while (isKind(LSQUARE, DOT, LPAREN, LCURLY, STRINGLIT, COLON)) {
            if (isKind(LSQUARE)) {
                Token first = consume();
                Exp exp1 = exp();
                exp = new ExpTableLookup(first, exp, exp1);
                match(RSQUARE);
            } else if (isKind(DOT)) {
                Token first = consume();
                Token match = match(NAME);
                exp = new ExpTableLookup(first, exp, new ExpString(match));
            } else if (isKind(COLON)) {
                Token first = consume();
                ExpString name = new ExpString(match(NAME));
                ExpTableLookup table = new ExpTableLookup(first, exp, name);

                Token match = match(LPAREN, LCURLY, STRINGLIT);

                if (match.kind == LPAREN) {
                    List<Exp> args = new ArrayList<>();
                    args.add(exp);
                    if (isKind(RPAREN)) {
                        consume();
                    } else {
                        args = expList();
                        match(RPAREN);
                    }
                    exp = new ExpFunctionCall(first, table, args);
                } else if (match.kind == LCURLY) {
                    List<Field> fields = new ArrayList<>();
                    if (isKind(RCURLY)) {
                        consume();
                    } else {
                        fields = fieldList();
                        match(RCURLY);
                    }

                    ExpTable expTable = new ExpTable(first, fields);

                    exp = new ExpFunctionCall(first, table, Arrays.asList(exp, expTable));
                } else {
                    exp = new ExpFunctionCall(match, table, Arrays.asList(exp, new ExpString(match)));
                }
            } else {
                if (isKind(LPAREN)) {
                    Token first = consume();
                    List<Exp> args = new ArrayList<>();
                    if (isKind(RPAREN)) {
                        consume();
                    } else {
                        args = expList();
                        match(RPAREN);
                    }
                    exp = new ExpFunctionCall(first, exp, args);
                } else if (isKind(LCURLY)) {
                    Token first = consume();
                    List<Field> fields = new ArrayList<>();
                    if (isKind(RCURLY)) {
                        consume();
                    } else {
                        fields = fieldList();
                        match(RCURLY);
                    }
                    ExpTable expTable = new ExpTable(first, fields);

                    exp = new ExpFunctionCall(first, exp, Collections.singletonList(expTable));
                } else {
                    Token string = match(NAME);
                    exp = new ExpFunctionCall(string, exp, Collections.singletonList(new ExpString(string)));
                }
            }
        }
        return exp;


    }

    // retstat ::= return [explist] [‘;’]
    public RetStat returnStat() throws Exception {
        Token first = match(KW_return);
        List<Exp> expList = new ArrayList<>();
        if (!isKind(SEMI, KW_end, KW_else, KW_elseif, KW_until)) {
            expList.add(exp());

            while (isKind(COMMA)) {
                consume();
                expList.add(exp());
            }
        }

        if (isKind(SEMI)) consume();
        return new RetStat(first, expList);
    }

    // funcname ::= Name {‘.’ Name} [‘:’ Name]
    public FuncName functionName() throws Exception {
        List<ExpName> names = new ArrayList<>();
        names.add(new ExpName(match(NAME)));

        while (isKind(DOT)) {
            consume();
            names.add(new ExpName(match(NAME)));
        }

        ExpName nameAfterColon = null;
        if (isKind(COLON)) {
            consume();
            nameAfterColon = new ExpName(match(NAME));
        }

        return new FuncName(null, names, nameAfterColon);
    }

    public Chunk parse() throws Exception {
        Chunk c = new Chunk(t, block());
        match(EOF);
        return c;
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

    private static Exp constructRightAssociativityExp(List<Exp> powerExps, Token first, Kind opPow) {
        Exp eLast = powerExps.get(powerExps.size() - 1);
        for (int i = powerExps.size() - 2; i >=0; i--) {
            Exp eSecondLast = powerExps.get(i);
            eLast = new ExpBinary(first, eSecondLast, opPow, eLast);
        }

        return eLast;
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
