package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclOrderedSet;
import fr.enseeiht.ocl.xtext.utils.SetUniqueArrayList;
public class OclAsOrderedSet implements IOclOperation {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
	    SetUniqueArrayList orderedSet = new SetUniqueArrayList();
	    orderedSet.addAll((Collection) source); // dédup + ordre d'insertion
	    return orderedSet;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collectType) {
			return new OclOrderedSet(collectType.getSubtype());
		}
		return new OclOrderedSet(new OclAny());
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
		return "asOrderedSet";
	}
}
