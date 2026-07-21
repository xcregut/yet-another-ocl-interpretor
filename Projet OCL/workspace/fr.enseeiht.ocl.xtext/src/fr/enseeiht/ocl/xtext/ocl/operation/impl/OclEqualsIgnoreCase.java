package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclBoolean;
import fr.enseeiht.ocl.xtext.types.OclString;

/**
 * Opération equalsIsIgnoreCase(s) : compare deux chaînes sans tenir compte de la casse.
 * NB : le nom reprend la constante existante du registre (equalsIsIgnoreCase) ;
 * la spécification du projet l'appelle equalsIgnoreCase.
 */
public class OclEqualsIgnoreCase implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		return ((String) source).equalsIgnoreCase((String) args.get(0));
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		return new OclBoolean();
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		return Arrays.asList(new OclString());
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
		return "equalsIsIgnoreCase";
	}
}
