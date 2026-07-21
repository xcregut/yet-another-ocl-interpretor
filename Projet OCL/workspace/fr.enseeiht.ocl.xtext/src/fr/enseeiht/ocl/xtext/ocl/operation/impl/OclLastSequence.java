package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.adapter.IndexOutOfBondInvalid;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclSequence;

/**
 * Opération last() : dernier élément d'une séquence. Séquence vide -> Invalid.
 * (Miroir de OclFirstSequence.)
 */
public class OclLastSequence implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		@SuppressWarnings("rawtypes")
		List sourceCollection = (List) source;
		if (sourceCollection.isEmpty()) {
			return new IndexOutOfBondInvalid(sourceCollection, 0, 0);
		}
		return sourceCollection.get(sourceCollection.size() - 1);
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collectType) {
			if (collectType.getSubtype() != null)
				return collectType.getSubtype();
		}
		return new OclAny();
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
		return "last";
	}
}
