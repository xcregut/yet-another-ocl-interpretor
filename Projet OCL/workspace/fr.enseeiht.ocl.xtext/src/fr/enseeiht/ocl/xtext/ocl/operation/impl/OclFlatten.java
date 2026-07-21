package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclBag;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclOrderedSet;
import fr.enseeiht.ocl.xtext.types.OclSequence;
import fr.enseeiht.ocl.xtext.types.OclSet;

/**
 * Opération flatten() : aplatit récursivement une collection de collections.
 */
public class OclFlatten implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		Collection<Object> result;
		try {
			@SuppressWarnings("unchecked")
			Collection<Object> instantiated = (Collection<Object>) fr.enseeiht.ocl.xtext.utils.ConstructorInstanciator
					.instantiateParameterlessConstructor(source.getClass());
			result = instantiated;
		} catch (Exception e) {
			result = new ArrayList<Object>();
		}
		flattenInto(result, (Collection<?>) source);
		return result;
	}

	private static void flattenInto(Collection<Object> result, Collection<?> source) {
		for (Object o : source) {
			if (o instanceof Collection<?> nested) {
				flattenInto(result, nested);
			} else {
				result.add(o);
			}
		}
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		// descend jusqu'au type d'élément le plus profond
		OclType element = new OclAny();
		OclType current = sourceType;
		while (current instanceof OclCollection collectType && collectType.getSubtype() != null) {
			element = collectType.getSubtype();
			current = collectType.getSubtype();
		}
		if (sourceType instanceof OclSet) {
			return new OclSet(element);
		} else if (sourceType instanceof OclOrderedSet) {
			return new OclOrderedSet(element);
		} else if (sourceType instanceof OclSequence) {
			return new OclSequence(element);
		} else if (sourceType instanceof OclBag) {
			return new OclBag(element);
		}
		return new OclCollection(element);
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
		return "flatten";
	}
}
