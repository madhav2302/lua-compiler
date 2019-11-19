package interpreter;

import cop5556fa19.AST.*;

public class StaticAnalysis implements ASTVisitor {
    @Override
    public Object visitExpNil(ExpNil expNil, Object arg) throws Exception {
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
    public Object visitBlock(Block block, Object arg) throws Exception {
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
    public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
        return null;
    }

    @Override
    public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
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
    public Object visitChunk(Chunk chunk, Object arg) throws Exception {
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

    @Override
    public Object visitLabel(StatLabel statLabel, Object ar) throws Exception {
        return null;
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
