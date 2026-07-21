package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclClassifier;
import fr.enseeiht.ocl.xtext.types.OclEClass;

public class OclOclType implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		// Le type d'un objet du modèle, à l'exécution, est son EClass
		// (même représentation que l'argument-type reçu par oclIsKindOf/oclIsTypeOf).
		return ((EObject) source).eClass();
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		// oclType() renvoie un "type vu comme valeur" : un OclClassifier.
		return new OclClassifier(new OclEClass(null));
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
		return new OclEClass(null);
	}

	@Override
	public String getName() {
		return "oclType";
	}
}
