package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclBoolean;
import fr.enseeiht.ocl.xtext.types.OclCollection;

public class OclExcludes implements IOclOperation {

	@SuppressWarnings("rawtypes")
	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		return !((Collection) source).contains(args.get(0));
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		return new OclBoolean();
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collectType) {
			if (collectType.getSubtype() != null)
				return Arrays.asList(collectType.getSubtype());
		}
		return Arrays.asList(new OclAny());
	}

	@Override
	public int getArgsAmount() {
		return 1;
	}

	@Override
	public OclType getSourceType() {
		return new OclCollection(new OclAny());
	}

	@Override
	public String getName() {
		return "excludes";
	}
}
