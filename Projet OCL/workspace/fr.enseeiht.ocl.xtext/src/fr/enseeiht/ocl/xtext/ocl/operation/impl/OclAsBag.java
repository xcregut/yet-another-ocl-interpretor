package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.bag.HashBag;
import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclBag;
import fr.enseeiht.ocl.xtext.types.OclCollection;

public class OclAsBag implements IOclOperation {

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		HashBag bag = new HashBag();
		bag.addAll((Collection) source);
		return bag;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collectType) {
			return new OclBag(collectType.getSubtype());
		}
		return new OclBag(new OclAny());
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
		return new OclCollection(new OclAny());
	}

	@Override
	public String getName() {
		return "asBag";
	}
}
