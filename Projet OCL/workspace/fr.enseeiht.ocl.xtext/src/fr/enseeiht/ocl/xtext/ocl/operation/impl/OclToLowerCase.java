package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclString;

public class OclToLowerCase implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		return ((String) source).toLowerCase();
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		return new OclString();
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
		return new OclString();
	}

	@Override
	public String getName() {
		return "toLowerCase";
	}
}
