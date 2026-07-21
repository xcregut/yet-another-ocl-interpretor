package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.adapter.IndexOutOfBondInvalid;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclInteger;
import fr.enseeiht.ocl.xtext.types.OclReal;

/**
 * Opération min() sur une collection de nombres : renvoie le plus petit élément.
 * Collection vide -> Invalid.
 */
public class OclMinCollection implements IOclOperation {

	@SuppressWarnings("rawtypes")
	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		Collection sourceCollection = (Collection) source;
		if (sourceCollection.isEmpty()) {
			return new IndexOutOfBondInvalid(sourceCollection, 0, 0);
		}
		boolean isReal = false;
		double dBest = Double.POSITIVE_INFINITY;
		int iBest = Integer.MAX_VALUE;
		for (Object o : sourceCollection) {
			if (o instanceof Double d) {
				isReal = true;
				if (d < dBest) dBest = d;
			} else if (o instanceof Integer i) {
				if (i < iBest) iBest = i;
				if (i < dBest) dBest = i;
			}
		}
		return isReal ? (Object) dBest : (Object) iBest;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collectType && collectType.getSubtype() instanceof OclInteger) {
			return new OclInteger();
		}
		return new OclReal();
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
		return new OclCollection(new OclReal());
	}

	@Override
	public String getName() {
		return "min";
	}
}
