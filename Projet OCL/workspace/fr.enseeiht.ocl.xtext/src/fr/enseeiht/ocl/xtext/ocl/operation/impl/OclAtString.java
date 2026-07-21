package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.adapter.IndexOutOfBondInvalid;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclInteger;
import fr.enseeiht.ocl.xtext.types.OclString;

/**
 * Opération at(i) sur String : caractère à la position i (indexation OCL à partir de 1).
 * Hors bornes -> Invalid.
 */
public class OclAtString implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		String s = (String) source;
		int index = (Integer) args.get(0);
		if (index < 1 || index > s.length()) {
			return new IndexOutOfBondInvalid(new ArrayList<Object>(), index, s.length());
		}
		return String.valueOf(s.charAt(index - 1));
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		return new OclString();
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		return Arrays.asList(new OclInteger());
	}

	@Override
	public int getArgsAmount() {
		return 1;
	}

	@Override
	public OclType getSourceType() {
		return new OclString();
	}

	@Override
	public String getName() {
		return "at";
	}
}
