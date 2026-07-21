package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclBag;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclSet;

/**
 * Opération intersection(c2) : éléments communs aux deux collections.
 * Typage : Set si l'un des deux est un Set ; Bag si les deux sont des Bag.
 */
public class OclIntersection implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		Collection<Object> result = OclIncluding.copyOf(source);
		result.retainAll((Collection<?>) args.get(0));
		return result;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		OclType subtype = new OclAny();
		if (sourceType instanceof OclCollection collectType && collectType.getSubtype() != null) {
			subtype = collectType.getSubtype();
		}
		boolean sourceIsSet = sourceType instanceof OclSet;
		boolean argIsSet = !argsType.isEmpty() && argsType.get(0) instanceof OclSet;
		if (sourceIsSet || argIsSet) {
			return new OclSet(subtype);
		}
		boolean sourceIsBag = sourceType instanceof OclBag;
		boolean argIsBag = !argsType.isEmpty() && argsType.get(0) instanceof OclBag;
		if (sourceIsBag && argIsBag) {
			return new OclBag(subtype);
		}
		return sourceType;
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collectType && collectType.getSubtype() != null) {
			return Arrays.asList(new OclCollection(collectType.getSubtype()));
		}
		return Arrays.asList(new OclCollection(new OclAny()));
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
		return "intersection";
	}
}
