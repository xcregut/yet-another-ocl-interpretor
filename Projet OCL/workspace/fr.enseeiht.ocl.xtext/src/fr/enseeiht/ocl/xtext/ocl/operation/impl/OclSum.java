package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclInteger;
import fr.enseeiht.ocl.xtext.types.OclReal;

public class OclSum implements IOclOperation {

	@SuppressWarnings("rawtypes")
	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		boolean isReal = false;
		double dSum = 0;
		int iSum = 0;
		for (Object o : (Collection) source) {
			if (o instanceof Double d) {
				isReal = true;
				dSum += d;
			} else if (o instanceof Integer i) {
				iSum += i;
				dSum += i;
			}
		}
		// Somme d'entiers -> Integer ; dès qu'un Real apparaît -> Real
		return isReal ? (Object) dSum : (Object) iSum;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collectType && collectType.getSubtype() instanceof OclInteger) {
			return new OclInteger();
		}
		return new OclReal();
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		return new ArrayList<OclType>();
	}

	@Override
	public int getArgsAmount() {
		return 0;
	}

	@Override
	public OclType getSourceType() {
		return new OclCollection(new OclReal());
	}

	@Override
	public String getName() {
		return "sum";
	}
}
