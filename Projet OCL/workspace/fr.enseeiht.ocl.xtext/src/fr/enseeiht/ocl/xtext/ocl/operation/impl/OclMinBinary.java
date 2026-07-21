package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclInteger;
import fr.enseeiht.ocl.xtext.types.OclReal;

/**
 * Opération min(x) sur les nombres : renvoie le plus petit de source et de l'argument.
 * Integer.max(Integer) -> Integer ; sinon -> Real.
 */
public class OclMinBinary implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		Object arg = args.get(0);
		if (source instanceof Integer a && arg instanceof Integer b) {
			return Math.min(a, b);
		}
		double a = (source instanceof Integer i) ? i : (Double) source;
		double b = (arg instanceof Integer i) ? i : (Double) arg;
		return Math.min(a, b);
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclInteger && !argsType.isEmpty() && argsType.get(0) instanceof OclInteger) {
			return new OclInteger();
		}
		return new OclReal();
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		return Arrays.asList(new OclReal());
	}

	@Override
	public int getArgsAmount() {
		return 1;
	}

	@Override
	public OclType getSourceType() {
		return new OclReal();
	}

	@Override
	public String getName() {
		return "min";
	}
}
