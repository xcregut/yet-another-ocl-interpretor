package fr.enseeiht.ocl.xtext.ocl.iterators;

import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorAny;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorCollect;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorCollectNested;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorExists;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorForAll;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorIsUnique;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorOne;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorReject;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorSelect;
import fr.enseeiht.ocl.xtext.ocl.iterators.impl.OclIteratorSortedBy;

public enum OclIteratorEnum {
	any(new OclIteratorAny()),
	exists(new OclIteratorExists()),
	forAll(new OclIteratorForAll()),
	one(new OclIteratorOne()),
	collect(new OclIteratorCollect()),
	select(new OclIteratorSelect()),
	reject(new OclIteratorReject()),
	collectNested(new OclIteratorCollectNested()),
	isUnique(new OclIteratorIsUnique()),
	sortedBy(new OclIteratorSortedBy()),
	;

	private OclIterator iterator;

	private OclIteratorEnum(OclIterator iterator) {
		this.iterator = iterator;
	}

	public OclIterator getIterator() {
		return this.iterator;
	}

	public static OclIterator getIterator(String name) {
		OclIterator res = null;
		try {
			res = OclIteratorEnum.valueOf(name).getIterator();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return res;
	}
}
