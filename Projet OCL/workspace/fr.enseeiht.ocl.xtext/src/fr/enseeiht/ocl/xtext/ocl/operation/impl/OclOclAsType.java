package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclClassifier;
import fr.enseeiht.ocl.xtext.types.OclEClass;

public class OclOclAsType implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		// Transtypage : dans l'interprète dynamique, l'objet est renvoyé tel quel.
		return source;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		// Le résultat prend le type passé en argument (le type représenté par le classifier).
		if (!argsType.isEmpty() && argsType.get(0) instanceof OclClassifier classifier) {
			return classifier.getRepresentedType();
		}
		return sourceType;
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		// L'argument est un type (un classifier), comme pour oclIsKindOf / oclIsTypeOf.
		return Arrays.asList(new OclClassifier(new OclEClass(null)));
	}

	@Override
	public int getArgsAmount() {
		return 1;
	}

	@Override
	public OclType getSourceType() {
		return new OclEClass(null);
	}

	@Override
	public String getName() {
		return "oclAsType";
	}
}
