package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.adapter.IndexOutOfBondInvalid;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclInteger;
import fr.enseeiht.ocl.xtext.types.OclSequence;

/**
 * Opération at(i) sur Sequence : élément à la position i (indexation OCL à partir de 1).
 * Hors bornes -> Invalid.
 */
public class OclAtSequence implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		@SuppressWarnings("rawtypes")
		List sourceCollection = (List) source;
		int index = (Integer) args.get(0);
		if (index < 1 || index > sourceCollection.size()) {
			return new IndexOutOfBondInvalid(sourceCollection, index, sourceCollection.size());
		}
		return sourceCollection.get(index - 1);
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
		return Arrays.asList(new OclInteger());
	}

	@Override
	public int getArgsAmount() {
		return 1;
	}

	@Override
	public OclType getSourceType() {
		return new OclSequence(new OclAny());
	}

	@Override
	public String getName() {
		return "at";
	}
}
