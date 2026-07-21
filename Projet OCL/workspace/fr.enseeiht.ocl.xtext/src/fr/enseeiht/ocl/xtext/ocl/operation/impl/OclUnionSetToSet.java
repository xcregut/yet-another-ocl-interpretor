package fr.enseeiht.ocl.xtext.ocl.operation.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.adapter.ParameterInvalid;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclSet;

public class OclUnionSetToSet extends OclUnion {

	@Override
	public Object getReturnValue(Object source, List<Object> args, EObject contextTarget) {
		if (source instanceof Collection collectionSource) {
			Object other = args.get(0);
			if (other instanceof Set otherSet) {
				Set<Object> result = new HashSet<Object>(otherSet);
				result.addAll(collectionSource);
				return result;
			}
		}
		return new ParameterInvalid(args, getName() + "(" + this.getClass().getSimpleName() + ")", source);
	}

	@Override
	public List<OclType> getArgsType(OclType sourceType, List<OclType> argsType) {
		if (sourceType instanceof OclCollection collecType) {
			return Arrays.asList(new OclSet(collecType.getSubtype()));
		}
		return Arrays.asList(new OclSet(new OclAny()));
	}

	@Override
	public OclType getSourceType() {
		return new OclSet(new OclAny());
	}

}
