/* *
 * Developed  for the class project in COP5556 Programming Language Principles
 * at the University of Florida, Fall 2019.
 *
 * This software is solely for the educational benefit of students
 * enrolled in the course during the Fall 2019 semester.
 *
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 *
 *  @Beverly A. Sanders, 2019
 */


package cop5556fa19;

import cop5556fa19.AST.*;
import cop5556fa19.ExpressionParser.SyntaxException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static cop5556fa19.Token.Kind.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpressionParserTest {

    // To make it easy to print objects and turn this output on and off
    static final boolean doPrint = true;

    private void show(Object input) {
        if (doPrint) {
            System.out.println(input.toString());
        }
    }


    // creates a scanner, parser, and parses the input.
    Exp parseAndShow(String input) throws Exception {
        show("parser input:\n" + input); // Display the input
        Reader r = new StringReader(input);
        Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
        ExpressionParser parser = new ExpressionParser(scanner);  // Create a parser
        Exp e = parser.exp(); // Parse and expression
        show("e=" + e);  //Show the resulting AST
        return e;
    }


    @Test
    void testIdent0() throws Exception {
        String input = "function () end";
        Exp e = parseAndShow(input);
    }

    @Test
    void testIdent1() throws Exception {
        String input = "(x)";
        Exp e = parseAndShow(input);
        assertEquals(ExpName.class, e.getClass());
        assertEquals("x", ((ExpName) e).name);
    }

    @Test
    void testString() throws Exception {
        String input = "\"string\"";
        Exp e = parseAndShow(input);
        assertEquals(ExpString.class, e.getClass());
        assertEquals("string", ((ExpString) e).v);
    }

    @Test
    void testBoolean0() throws Exception {
        String input = "true";
        Exp e = parseAndShow(input);
        assertEquals(ExpTrue.class, e.getClass());
    }

    @Test
    void testBoolean1() throws Exception {
        String input = "false";
        Exp e = parseAndShow(input);
        assertEquals(ExpFalse.class, e.getClass());
    }


    @Test
    void testBinary0() throws Exception {
        String input = "1 + 2";
        Exp e = parseAndShow(input);
        Exp expected = Expressions.makeBinary(1, OP_PLUS, 2);
        show("expected=" + expected);
        assertEquals(expected, e);
    }

    @Test
    void testUnary0() throws Exception {
        String input = "-2";
        Exp e = parseAndShow(input);
        Exp expected = Expressions.makeExpUnary(OP_MINUS, 2);
        show("expected=" + expected);
        assertEquals(expected, e);
    }

    @Test
    void testUnary1() throws Exception {
        String input = "-*2\n";
        assertThrows(SyntaxException.class, () -> {
            Exp e = parseAndShow(input);
        });
    }


    @Test
    void testRightAssoc() throws Exception {
        String input = "\"concat\" .. \"is\"..\"right associative\"";
        Exp e = parseAndShow(input);
        Exp expected = Expressions.makeBinary(
                Expressions.makeExpString("concat")
                , DOTDOT
                , Expressions.makeBinary("is", DOTDOT, "right associative"));
        show("expected=" + expected);
        assertEquals(expected, e);
    }

    @Test
    void testLeftAssoc() throws Exception {
        String input = "\"minus\" - \"is\" - \"left associative\"";
        Exp e = parseAndShow(input);
        Exp expected = Expressions.makeBinary(
                Expressions.makeBinary(
                        Expressions.makeExpString("minus")
                        , OP_MINUS
                        , Expressions.makeExpString("is")), OP_MINUS,
                Expressions.makeExpString("left associative"));
        show("expected=" + expected);
        assertEquals(expected, e);

    }

    @Test
    void testMultiUnaryOperator() throws Exception {
        String input = "not #~1";
        Exp e = parseAndShow(input);
    }

    @Test
    void testEmpty1() throws Exception {
        String input = "";
        Block b = parseBlockAndShow(input);
        Block expected = Expressions.makeBlock();
        Assert.assertEquals(expected, b);
    }

    @Test
    void testEmpty2() throws Exception {
        String input = "";
        ASTNode n = parseAndShow(input);
        Block b = Expressions.makeBlock();
        Chunk expected = new Chunk(b.firstToken, b);
        Assert.assertEquals(expected, n);
    }

    @Test
    void testAssign1() throws Exception {
        String input = "a=b";
        Block b = parseBlockAndShow(input);
        List<Exp> lhs = Expressions.makeExpList(Expressions.makeExpName("a"));
        List<Exp> rhs = Expressions.makeExpList(Expressions.makeExpName("b"));
        StatAssign s = Expressions.makeStatAssign(lhs, rhs);
        Block expected = Expressions.makeBlock(s);
        Assert.assertEquals(expected, b);
    }

    @Test
    void testAssignChunk1() throws Exception {
        String input = "a=b";
        ASTNode c = parseAndShow(input);
        List<Exp> lhs = Expressions.makeExpList(Expressions.makeExpName("a"));
        List<Exp> rhs = Expressions.makeExpList(Expressions.makeExpName("b"));
        StatAssign s = Expressions.makeStatAssign(lhs, rhs);
        Block b = Expressions.makeBlock(s);
        Chunk expected = new Chunk(b.firstToken, b);
        Assert.assertEquals(expected, c);
    }


    @Test
    void testMultiAssign1() throws Exception {
        String input = "a,c=8,9";
        Block b = parseBlockAndShow(input);
        List<Exp> lhs = Expressions.makeExpList(
                Expressions.makeExpName("a")
                , Expressions.makeExpName("c"));
        Exp e1 = Expressions.makeExpInt(8);
        Exp e2 = Expressions.makeExpInt(9);
        List<Exp> rhs = Expressions.makeExpList(e1, e2);
        StatAssign s = Expressions.makeStatAssign(lhs, rhs);
        Block expected = Expressions.makeBlock(s);
        Assert.assertEquals(expected, b);
    }


    @Test
    void testMultiAssign3() throws Exception {
        String input = "a,c=8,f(x)";
        Block b = parseBlockAndShow(input);
        List<Exp> lhs = Expressions.makeExpList(
                Expressions.makeExpName("a")
                , Expressions.makeExpName("c"));
        Exp e1 = Expressions.makeExpInt(8);
        List<Exp> args = new ArrayList<>();
        args.add(Expressions.makeExpName("x"));
        Exp e2 = Expressions.makeExpFunCall(Expressions.makeExpName("f"), args, null);
        List<Exp> rhs = Expressions.makeExpList(e1, e2);
        StatAssign s = Expressions.makeStatAssign(lhs, rhs);
        Block expected = Expressions.makeBlock(s);
        Assert.assertEquals(expected, b);
    }


    @Test
    void testAssignToTable() throws Exception {
        String input = "g.a.b = 3";
        Block bl = parseBlockAndShow(input);
        ExpName g = Expressions.makeExpName("g");
        ExpString a = Expressions.makeExpString("a");
        Exp gtable = Expressions.makeExpTableLookup(g, a);
        ExpString b = Expressions.makeExpString("b");
        Exp v = Expressions.makeExpTableLookup(gtable, b);
        Exp three = Expressions.makeExpInt(3);
        Stat s = Expressions.makeStatAssign(Expressions.makeExpList(v), Expressions.makeExpList(three));
        ;
        Block expected = Expressions.makeBlock(s);
        Assert.assertEquals(expected, bl);
    }

    @Test
    void testAssignTableToVar() throws Exception {
        String input = "x = g.a.b";
        Block bl = parseBlockAndShow(input);
        ExpName g = Expressions.makeExpName("g");
        ExpString a = Expressions.makeExpString("a");
        Exp gtable = Expressions.makeExpTableLookup(g, a);
        ExpString b = Expressions.makeExpString("b");
        Exp e = Expressions.makeExpTableLookup(gtable, b);
        Exp v = Expressions.makeExpName("x");
        Stat s = Expressions.makeStatAssign(Expressions.makeExpList(v), Expressions.makeExpList(e));
        ;
        Block expected = Expressions.makeBlock(s);
        Assert.assertEquals(expected, bl);
    }

    // creates a scanner, parser, and parses the input by calling block()
    Block parseBlockAndShow(String input) throws Exception {
        show("parser input:\n" + input); // Display the input
        Reader r = new StringReader(input);
        Scanner scanner = new Scanner(r); // Create a Scanner and initialize it
        ExpressionParser parser = new ExpressionParser(scanner);
        Method method = ExpressionParser.class.getDeclaredMethod("block");
        method.setAccessible(true);
        Block b = (Block) method.invoke(parser);
        show("b=" + b);
        return b;
    }


    @Test
    void testmultistatements6() throws Exception {
        String input = "x = g.a.b ; ::mylabel:: do  y = 2 goto mylabel f=a(0,200) end break"; //same as testmultistatements0 except ;
        ASTNode c = parseAndShow(input);
        ExpName g = Expressions.makeExpName("g");
        ExpString a = Expressions.makeExpString("a");
        Exp gtable = Expressions.makeExpTableLookup(g, a);
        ExpString b = Expressions.makeExpString("b");
        Exp e = Expressions.makeExpTableLookup(gtable, b);
        Exp v = Expressions.makeExpName("x");
        Stat s0 = Expressions.makeStatAssign(v, e);
        StatLabel s1 = Expressions.makeStatLabel("mylabel");
        Exp y = Expressions.makeExpName("y");
        Exp two = Expressions.makeExpInt(2);
        Stat s2 = Expressions.makeStatAssign(y, two);
        Stat s3 = Expressions.makeStatGoto("mylabel");
        Exp f = Expressions.makeExpName("f");
        Exp ae = Expressions.makeExpName("a");
        Exp zero = Expressions.makeExpInt(0);
        Exp twohundred = Expressions.makeExpInt(200);
        List<Exp> args = Expressions.makeExpList(zero, twohundred);
        ExpFunctionCall fc = Expressions.makeExpFunCall(ae, args, null);
        StatAssign s4 = Expressions.makeStatAssign(f, fc);
        StatDo statdo = Expressions.makeStatDo(s2, s3, s4);
        StatBreak statBreak = Expressions.makeStatBreak();
        Block expectedBlock = Expressions.makeBlock(s0, s1, statdo, statBreak);
        Chunk expectedChunk = new Chunk(expectedBlock.firstToken, expectedBlock);
        Assert.assertEquals(expectedChunk, c);
    }

}
