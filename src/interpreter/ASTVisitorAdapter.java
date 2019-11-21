package interpreter;

import cop5556fa19.AST.*;
import cop5556fa19.Token;
import javafx.util.Pair;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class ASTVisitorAdapter implements ASTVisitor {

	private final Stack<State> stack = new Stack<>();
	private int loopCount = 0;

	@Override
	public Object visitExpInt(ExpInt expInt, Object arg) {
		return new LuaInt(expInt.v);
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		for (Stat stat : block.stats) {
			if (stat instanceof StatBreak) throw new BreakException();
			if (stat instanceof RetStat) {
				return stat.visit(this, arg);
			} else {
				Object visit = stat.visit(this, arg);
				if (visit != null) return visit;

			}
		}
		return null;
	}

	@Override
	public Object visitExpNil(ExpNil expNil, Object arg) {
		return LuaNil.nil;
	}

	public abstract List<LuaValue> load(Reader r) throws Exception;

	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		stack.push(State.AS_VALUE);
		Object e0 = expBin.e0.visit(this, arg);
		Object e1 = expBin.e1.visit(this, arg);
		stack.pop();

		switch (expBin.op) {
			case REL_GT:
				assert e0 instanceof LuaInt;
				assert e1 instanceof LuaInt;
				return new LuaBoolean(((LuaInt) e0).v > ((LuaInt) e1).v);
			case REL_LT:
				assert e0 instanceof LuaInt;
				assert e1 instanceof LuaInt;
				return new LuaBoolean(((LuaInt) e0).v < ((LuaInt) e1).v);
			case OP_PLUS:
				if (e0 instanceof LuaInt && e1 instanceof LuaInt) {
					return new LuaInt(((LuaInt) e0).v + ((LuaInt) e1).v);
				} else {
					throw new RuntimeException();// TODO
				}
			case OP_MINUS:
				if (e0 instanceof LuaInt && e1 instanceof LuaInt) {
					return new LuaInt(((LuaInt) e0).v - ((LuaInt) e1).v);
				} else {
					throw new RuntimeException();// TODO
				}


		}
		throw new RuntimeException();
	}

	@Override
	public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
		try {
			return statDo.b.visit(this, arg);
		} catch (BreakException e) {
			if (loopCount == 0) return null;
			else throw e;
		}
	}

	@Override
	public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpString(ExpString expString, Object arg) {
		return new LuaString(expString.v);
	}

	@Override
	public Object visitExpTable(ExpTable expTableConstr, Object arg) throws Exception {
		LuaTable table = new LuaTable();
		for (Field f : expTableConstr.fields) {
			stack.push(State.AS_VALUE);
			Object visit = f.visit(this, arg);
			if (visit instanceof Pair) {
				table.put((LuaValue) ((Pair) visit).getKey(), (LuaValue) ((Pair) visit).getValue());
			} else {
				table.putImplicit((LuaValue) visit);
			}
			stack.pop();
		}
		return table;
	}

	@Override
	public Object visitName(Name name, Object arg) {
		return new LuaString(name.name);
	}

	@Override
	public Object visitExpList(ExpList expList, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitParList(ParList parList, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFunDef(ExpFunction funcDec, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	private boolean asBoolean(Object visit) {
		return visit instanceof LuaBoolean ? ((LuaBoolean) visit).value : !(visit instanceof LuaNil);
	}

	@Override
	public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
		for (int i = 0; i < statIf.es.size(); i++) {
			stack.push(State.AS_VALUE);
			Object visit = statIf.es.get(i).visit(this, arg);
			stack.pop();

			if (asBoolean(visit)) {
				Object visit1 = statIf.bs.get(i).visit(this, arg);
				if (visit1 != null) return visit1;
				break;
			}
		}
		return null;
	}

	@Override
	public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
		List<Object> result = new ArrayList<>();
		for (Exp exp : retStat.el) {
			stack.push(State.AS_VALUE);
			result.add(exp.visit(this, arg));
			stack.pop();
		}
		return result;
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
		loopCount++;
		Object visit = null;
		while (true) {
			Object condition = statWhile.e.visit(this, arg);
			if (!asBoolean(condition)) break;
			try {
				visit = statWhile.b.visit(this, arg);
				if (visit != null) {
					break;
				}
			} catch (BreakException e) {
				break;
			}
		}
		loopCount--;
		return visit;
	}

	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		stack.push(State.AS_EXP);
		Object visit = chunk.block.visit(this, arg);
		stack.pop();
		return visit;
	}

	@Override
	public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {
		LuaTable table = (LuaTable) arg;
		if (statAssign.expList.size() != statAssign.varList.size()) throw new RuntimeException();

		for (int index = 0; index < statAssign.varList.size(); index++) {
			Exp var = statAssign.varList.get(index);
			Exp exp = statAssign.expList.get(index);

			Object key = var.visit(this, arg);
			Object value = exp.visit(this, arg);

			table.put((LuaValue) key, (LuaValue) value);
		}
		return null;
	}

	@Override
	public Object visitStatFor(StatFor statFor1, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatForEach(StatForEach statForEach, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFuncName(FuncName funcName, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatFunction(StatFunction statFunction, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatLocalFunc(StatLocalFunc statLocalFunc, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatLocalAssign(StatLocalAssign statLocalAssign, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpName(ExpName expName, Object arg) {
		LuaTable table = (LuaTable) arg;
		LuaString l = new LuaString(expName.name);

		return stack.peek() == State.AS_VALUE ? table.get(l) : l;
	}

	@Override
	public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object object) throws Exception {
		stack.push(State.AS_VALUE);
		Object key = fieldExpKey.key.visit(this, object);
		Object value = fieldExpKey.value.visit(this, object);
		stack.pop();
		return new Pair<>(key, value);
	}

	@Override
	public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {
		stack.push(State.AS_VALUE);
		Object key = fieldNameKey.name.visit(this, arg);
		Object value = fieldNameKey.exp.visit(this, arg);
		stack.pop();
		return new Pair<>(key, value);
	}

	@Override
	public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
		return fieldImplicitKey.exp.visit(this, arg);
	}

	@Override
	public Object visitExpTrue(ExpTrue expTrue, Object arg) {
		return new LuaBoolean(true);
	}

	@Override
	public Object visitExpFalse(ExpFalse expFalse, Object arg) {
		return new LuaBoolean(false);
	}

	@Override
	public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
		LuaTable table = (LuaTable) arg;
		Object functionName = expFunctionCall.f.visit(this, arg);
		List<LuaValue> args = new ArrayList<>();
		for (Exp v : expFunctionCall.args) {
			stack.push(State.AS_VALUE);
			args.add((LuaValue) v.visit(this, arg));
			stack.pop();
		}

		((JavaFunction) table.get((LuaString) functionName)).call(args);
		return null;
	}

	@Override
	public Object visitFuncBody(FuncBody funcBody, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpVarArgs(ExpVarArgs expVarArgs, Object arg) {
		throw new UnsupportedOperationException();
	}

	enum State {
		AS_VALUE,
		AS_EXP,
	}

	@Override
	public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	private static class BreakException extends Exception {
	}

	@Override
	public Object visitLabel(StatLabel statLabel, Object ar) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFieldList(FieldList fieldList, Object arg) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("serial")
	public static class TypeException extends Exception {

		public TypeException(String msg) {
			super(msg);
		}

		public TypeException(Token first, String msg) {
			super(first.line + ":" + first.pos + " " + msg);
		}

	}



}
