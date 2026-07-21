package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclSequence;

/**
 * Opération reverse() : renvoie la séquence en ordre inverse.
 */
public class OclReverse implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		List<Object> reversed = new ArrayList<Object>((Collection<?>) source);
		Collections.reverse(reversed);
		return reversed;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		return sourceType;
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
		return new OclSequence(new OclAny());
	}

	@Override
	public String getName() {
		return "reverse";
	}
}
