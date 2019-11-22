package interpreter;

import cop5556fa19.AST.*;
import cop5556fa19.Token;

import java.util.*;

public class StaticAnalysis implements ASTVisitor {

    private final Stack<Integer> scopeStack = new Stack<>();
    private final Map<String, List<Data>> map = new HashMap<>();
    private boolean savingLabels = true;
    private int currentScope, nextScope;

    StaticAnalysis() {
        currentScope = 0;
        nextScope = 1;
        scopeStack.push(0);
    }

    private void resetScope() {
        scopeStack.clear();

        currentScope = 0;
        nextScope = 1;
        scopeStack.push(0);
    }

    private void enterScope() {
        currentScope = nextScope++;
        scopeStack.push(currentScope);
    }

    private void leaveScope() {
        scopeStack.pop();
        currentScope = scopeStack.peek();
    }

    private void insert(StatLabel stat) throws StaticSemanticException {
        Data data = new Data(currentScope, stat);
        map.putIfAbsent(stat.label.name, new ArrayList<>());
        if (map.get(stat.label.name).contains(data)) {
            throw new StaticSemanticException(stat.firstToken == null ? new Token(Token.Kind.EOF, "", 0, 0) : stat.firstToken, "Multiple declaration of label in same scope");
        }
        map.get(stat.label.name).add(data);
    }

    private StatLabel lookup(StatGoto sGoto) throws StaticSemanticException {
        if (!map.containsKey(sGoto.name.name))
            throw new StaticSemanticException(sGoto.firstToken == null ? new Token(Token.Kind.EOF, "", 0, 0) : sGoto.firstToken, "no visible label '" + sGoto.name + "'");

        Data data = null;
        List<Data> labels = map.get(sGoto.name.name);
        for (int index = labels.size() - 1; index >= 0; index--) {
            int depth = scopeStack.search(labels.get(index).scope);
            if (depth > -1 && (data == null || depth < scopeStack.search(data.scope))) {
                data = labels.get(index);
            }
        }
        if (data != null) return data.label;
        throw new StaticSemanticException(sGoto.firstToken == null ? new Token(Token.Kind.EOF, "", 0, 0) : sGoto.firstToken, "");
    }

    @Override
    public Object visitExpNil(ExpNil expNil, Object arg) throws Exception {
        return LuaNil.nil;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws Exception {
        enterScope();
        for (int index = 0; index < block.stats.size(); index++) {
            block.stats.get(index).visit(this, new Pair<>(block, index));
        }
        leaveScope();
        return null;
    }

    @Override
    public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpInt(ExpInt expInt, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpString(ExpString expString, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpList(ExpList expList, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitParList(ParList parList, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitFunDef(ExpFunction funcDec, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitName(Name name, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
        if (savingLabels) return null;
        statGoto.label = lookup(statGoto);
        return null;
    }

    @Override
    public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
        return null;
    }

    @Override
    public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
        statDo.b.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
        statWhile.b.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
        statRepeat.b.visit(this, arg);
        return null;
    }

    @Override
    public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
        for (Block b : statIf.bs) {
            b.visit(this, arg);
        }
        return null;
    }

    @Override
    public Object visitChunk(Chunk chunk, Object arg) throws Exception {
        chunk.block.visit(this, arg);
        savingLabels = false;

        resetScope();
        return null;
    }

    @Override
    public Object visitStatFor(StatFor statFor1, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatForEach(StatForEach statForEach, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitFuncName(FuncName funcName, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatFunction(StatFunction statFunction, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatLocalFunc(StatLocalFunc statLocalFunc, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatLocalAssign(StatLocalAssign statLocalAssign, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitLabel(StatLabel statLabel, Object ar) throws Exception {
        if (!savingLabels) return null;

        Pair<Block, Integer> pair = (Pair) ar;
        statLabel.enclosingBlock = pair.getKey();
        statLabel.index = pair.getValue();
        insert(statLabel);
        return null;
    }

    @Override
    public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpTrue(ExpTrue expTrue, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpFalse(ExpFalse expFalse, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitFuncBody(FuncBody funcBody, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpVarArgs(ExpVarArgs expVarArgs, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
        return null;
    }

    private static class Data {
        public final int scope;
        public final StatLabel label;

        public Data(int scope, StatLabel label) {
            this.scope = scope;
            this.label = label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data data = (Data) o;
            return scope == data.scope &&
                    Objects.equals(label, data.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scope, label);
        }
    }

    @Override
    public Object visitFieldList(FieldList fieldList, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitExpName(ExpName expName, Object arg) throws Exception {
        return null;
    }
}
