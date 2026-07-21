package fr.enseeiht.ocl.xtext.ocl.iterators.impl;

import java.util.ArrayList;
import java.util.List;

import fr.enseeiht.ocl.xtext.OclType;
import fr.enseeiht.ocl.xtext.ocl.IteratorExp;
import fr.enseeiht.ocl.xtext.ocl.adapter.Invalid;
import fr.enseeiht.ocl.xtext.ocl.iterators.OclIterator;
import fr.enseeiht.ocl.xtext.types.OclAny;
import fr.enseeiht.ocl.xtext.types.OclCollection;
import fr.enseeiht.ocl.xtext.types.OclSequence;
import fr.enseeiht.ocl.xtext.utils.Pair;

/**
 * Itérateur sortedBy(x | corps) : renvoie les éléments de la collection triés
 * par ordre croissant de la valeur du corps. Résultat ordonné (Sequence).
 */
public class OclIteratorSortedBy implements OclIterator {

	@Override
	public Object getReturnValue(List<Pair<List<Object>, Object>> iteratorBodyValues, IteratorExp iteratorExp, Class<?> sourceCollectionClass) {
		for (Pair<List<Object>, Object> pair : iteratorBodyValues) {
			if (pair.getValue() instanceof Invalid) {
				return pair.getValue();
			}
		}
		List<Pair<List<Object>, Object>> sorted = new ArrayList<>(iteratorBodyValues);
		sorted.sort((p1, p2) -> compareBodies(p1.getValue(), p2.getValue()));
		List<Object> result = new ArrayList<>();
		for (Pair<List<Object>, Object> pair : sorted) {
			result.add(pair.getKey().get(0));
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareBodies(Object a, Object b) {
		if (a instanceof Number na && b instanceof Number nb) {
			return Double.compare(na.doubleValue(), nb.doubleValue());
		}
		if (a instanceof String sa && b instanceof String sb) {
			return sa.compareTo(sb);
		}
		if (a instanceof Comparable ca && a.getClass().isInstance(b)) {
			return ca.compareTo(b);
		}
		return String.valueOf(a).compareTo(String.valueOf(b));
	}

	@Override
	public OclType getReturnType(OclType sourceType, OclType bodyType) {
		if (sourceType instanceof OclCollection collectType && collectType.getSubtype() != null) {
			return new OclSequence(collectType.getSubtype());
		}
		return new OclSequence(new OclAny());
	}

	@Override
	public OclType getExpectedBodyType() {
		return new OclAny();
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
		return "sortedBy";
	}
}
