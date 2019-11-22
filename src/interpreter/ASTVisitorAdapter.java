package interpreter;

import cop5556fa19.AST.*;
import cop5556fa19.Token;

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
		for (int index = 0; index < block.stats.size(); index++) {
			Stat stat = block.stats.get(index);
			try {
				Object visit = stat.visit(this, arg);
				if (visit != null) return visit;
			} catch (GotoException e) {
				if (e.statLabel.enclosingBlock == block) index = e.statLabel.index; // Jump to index
				else throw e; // Label is in outer block
			}
		}
		return null;
	}

	@Override
	public Object visitExpNil(ExpNil expNil, Object arg) {
		return LuaNil.nil;
	}

	public abstract List<LuaValue> load(Reader r) throws Exception;

	private int toInteger(Object v) throws TypeException {
		if (v instanceof LuaInt) return ((LuaInt) v).v;
		else if (v instanceof LuaString) return Integer.parseInt(((LuaString) v).value);
		throw new TypeException("Expected LuaInteger or LuaString");
	}

	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		stack.push(State.AS_VALUE);
		Object e0 = expBin.e0.visit(this, arg);
		Object e1 = expBin.e1.visit(this, arg);
		stack.pop();

		switch (expBin.op) {
			case OP_PLUS:
				return new LuaInt(toInteger(e0) + toInteger(e1));
			case OP_MINUS:
				return new LuaInt(toInteger(e0) - toInteger(e1));
			case OP_TIMES:
				return new LuaInt(toInteger(e0) * toInteger(e1));
			case OP_DIV:
				return new LuaInt(toInteger(e0) / toInteger(e1));
			case OP_MOD:
				return new LuaInt(toInteger(e0) % toInteger(e1));
			case OP_POW:
				return new LuaInt((int) Math.pow(toInteger(e0), toInteger(e1)));
			case OP_DIVDIV:
				return new LuaInt(Math.floorDiv(toInteger(e0), toInteger(e1)));
			case DOTDOT:
				if (e0 instanceof LuaString) {
					if (e1 instanceof LuaString || e1 instanceof LuaInt) {
						return new LuaString(e0.toString() + e1.toString());
					}
					throw new TypeException("Second exp should be either LuaString or LuaInt");
				}
				throw new TypeException("First param should be LuaString");
			case REL_EQEQ:
				return new LuaBoolean(e0.equals(e1));
			case REL_NOTEQ:
				return new LuaBoolean(!e0.equals(e1));
			case REL_GT:
				if (e0 instanceof LuaInt && e1 instanceof LuaInt) {
					return new LuaBoolean(toInteger(e0) > toInteger(e1));
				} else if (e0 instanceof LuaString && e1 instanceof LuaString) {
					return ((LuaString) e0).value.compareTo(((LuaString) e1).value) > 0
							? new LuaBoolean(true)
							: new LuaBoolean(false);
				} else {
					throw new TypeException("Expecting something else");
				}
			case REL_LT:
				if (e0 instanceof LuaInt && e1 instanceof LuaInt) {
					return new LuaBoolean(toInteger(e0) < toInteger(e1));
				} else if (e0 instanceof LuaString && e1 instanceof LuaString) {
					return ((LuaString) e0).value.compareTo(((LuaString) e1).value) < 0
							? new LuaBoolean(true)
							: new LuaBoolean(false);
				} else {
					throw new TypeException("Expecting something else");
				}
			case REL_GE:
				if (e0 instanceof LuaInt && e1 instanceof LuaInt) {
					return new LuaBoolean(toInteger(e0) >= toInteger(e1));
				} else if (e0 instanceof LuaString && e1 instanceof LuaString) {
					return ((LuaString) e0).value.compareTo(((LuaString) e1).value) >= 0
							? new LuaBoolean(true)
							: new LuaBoolean(false);
				} else {
					throw new TypeException("Expecting something else");
				}
			case REL_LE:
				if (e0 instanceof LuaInt && e1 instanceof LuaInt) {
					return new LuaBoolean(toInteger(e0) <= toInteger(e1));
				} else if (e0 instanceof LuaString && e1 instanceof LuaString) {
					return ((LuaString) e0).value.compareTo(((LuaString) e1).value) <= 0
							? new LuaBoolean(true)
							: new LuaBoolean(false);
				} else {
					throw new TypeException("Expecting something else");
				}
			case KW_and:
				return new LuaBoolean(asBoolean(e0) && asBoolean(e1));
			case KW_or:
				return new LuaBoolean(asBoolean(e0) || asBoolean(e1));
			case BIT_XOR:
				return new LuaInt(toInteger(e0) ^ toInteger(e1));
			case BIT_OR:
				return new LuaInt(toInteger(e0) | toInteger(e1));
			case BIT_AMP:
				return new LuaInt(toInteger(e0) & toInteger(e1));
			case BIT_SHIFTL:
				return new LuaInt(toInteger(e0) << toInteger(e1));
			case BIT_SHIFTR:
				return new LuaInt(toInteger(e0) >> toInteger(e1));
		}
		throw new IllegalArgumentException();
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
		stack.push(State.AS_VALUE);
		Object visit = unExp.e.visit(this, arg);
		stack.pop();
		switch (unExp.op) {
			case OP_MINUS:
				return new LuaInt(-1 * toInteger(visit));
			case KW_not:
				return new LuaBoolean(!asBoolean(visit));
			case OP_HASH:
				if (visit instanceof LuaString) return new LuaInt(((LuaString) visit).value.length());
				if (visit instanceof LuaTable) return new LuaInt(0); // TODO
				throw new TypeException("Expected LuaString or LuaTable");
			case BIT_XOR:
				return new LuaInt(~toInteger(visit));
		}
		throw new IllegalArgumentException("Unknown unary operator " + unExp.op);
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
		throw new BreakException();
	}

	@Override
	public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
		throw new GotoException(statGoto.label);
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

		for (int index = 0; index < statAssign.varList.size(); index++) {
			Exp var = statAssign.varList.get(index);
			Exp exp = index >= statAssign.expList.size() ? ExpNil.expNilConst : statAssign.expList.get(index);

			if (var instanceof ExpTableLookup) {
				ExpTableLookup expTableLookup = (ExpTableLookup)  var;
				stack.push(State.AS_VALUE);
				LuaTable internalTable = (LuaTable) expTableLookup.table.visit(this, arg);
				Object key = expTableLookup.key.visit(this, arg);
				Object value = exp.visit(this, arg);
				stack.pop();
				internalTable.put((LuaValue) key, (LuaValue) value);
			} else {
				Object key = var.visit(this, arg);
				Object value = exp.visit(this, arg);

				table.put((LuaValue) key, (LuaValue) value);
			}
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
		stack.push(State.AS_VALUE);
		Object expTable = expTableLookup.table.visit(this, arg);
		Object key = expTableLookup.key.visit(this, arg);
		stack.pop();

		if (!(expTable instanceof LuaTable)) throw new TypeException(nonNullFirstToken(expTableLookup.firstToken), "");
		return ((LuaTable)expTable).get((LuaValue) key);
	}

	private Token nonNullFirstToken(Token token) {
		return token == null ? new Token(Token.Kind.EOF, "", 0, 0) : token;
	}

	private static class BreakException extends Exception {
	}

	@Override
	public Object visitLabel(StatLabel statLabel, Object ar) {
		return null;
	}

	private static class GotoException extends Exception {
		final StatLabel statLabel;

		private GotoException(StatLabel statLabel) {
			this.statLabel = statLabel;
		}
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
