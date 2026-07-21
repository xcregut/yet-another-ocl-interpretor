package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.operation.IOclOperation;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.utils.ConstructorInstanciator;

/**
 * Opération including(x) : renvoie une copie de la collection avec x ajouté.
 * La copie garde le genre de la collection source (un Set reste un Set : pas de doublon).
 */
public class OclIncluding implements IOclOperation {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		Collection<Object> result = copyOf(source);
		result.add(args.get(0));
		return result;
	}

	@SuppressWarnings("unchecked")
	static Collection<Object> copyOf(Object source) {
		Object copy;
		try {
			copy = ConstructorInstanciator.instantiateParameterlessConstructor(source.getClass());
		} catch (Exception e) {
			copy = new ArrayList<Object>();
		}
		Collection<Object> result = (Collection<Object>) copy;
		result.addAll((Collection<Object>) source);
		return result;
	}

	@Override
	public OclType getReturnType(OclType sourceType, List<OclType> argsType) {
		return sourceType;
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collectType) {
			if (collectType.getSubtype() != null)
				return Arrays.asList(collectType.getSubtype());
		}
		return Arrays.asList(new OclAny());
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
		return "including";
	}
}
