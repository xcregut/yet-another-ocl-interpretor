package fr.enseeiht.ocl.xtext.ocl.iterators.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.IteratorExp;
import fr.enseeiht.ocl.xtext.ocl.iterators.OclIterator;
import fr.enseeiht.ocl.xtext.types.OclBoolean;
import fr.enseeiht.ocl.xtext.utils.ConstructorInstanciator;
import fr.enseeiht.ocl.xtext.utils.Pair;

public class OclIteratorReject implements OclIterator {

	@Override
	public Object getReturnValue(List<Pair<List<Object>, Object>> iteratorBodyValues, IteratorExp iteratorExp, Class<?> sourceCollectionClass) {
		// "The subcollection of the source collection for which body is false."
		// (complément de select) - from https://www.omg.org/spec/OCL/2.4/PDF

		// Instancie une collection vide du même type (Bag, Set, ...) que la source
		Object rejectObj = null;
		try {
			rejectObj = ConstructorInstanciator.instantiateParameterlessConstructor(sourceCollectionClass);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		Collection<Object> reject = (Collection<Object>) rejectObj;

		for (Pair<List<Object>, Object> pair : iteratorBodyValues) {
			Object body = pair.getValue();
			if (body instanceof Boolean bodyBool && !bodyBool) {
				reject.add(pair.getKey().get(0));
			}
		}

		return reject;
	}

	@Override
	public OclType getReturnType(OclType sourceType, OclType bodyType) {
		return sourceType;
	}

	@Override
	public OclType getExpectedBodyType() {
		return new OclBoolean();
	}

	@Override
	public int getMinIteratorAmount() {
		return 0;
	}

	@Override
	public int getMaxIteratorAmount() {
		return 1;
	}

	@Override
	public String getName() {
		return "reject";
	}
}
