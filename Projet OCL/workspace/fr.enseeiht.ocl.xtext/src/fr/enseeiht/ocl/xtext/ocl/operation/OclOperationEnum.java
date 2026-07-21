package fr.enseeiht.ocl.xtext.ocl.operation;

import java.util.Arrays;
import java.util.List;

import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclAbs;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclAllInstances;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclAsBag;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclAsOrderedSet;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclAsSequence;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclAsSet;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclAtSequence;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclAtString;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclBooleanToString;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclConcat;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclCount;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclEqualsIgnoreCase;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclExcludes;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclExcludesAll;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclExcluding;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclFirstSequence;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclFlatten;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclFloor;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclIncludes;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclIncludesAll;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclIncluding;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclIndexOf;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclIntersection;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclIsEmpty;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclLastSequence;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclMatches;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclMaxBinary;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclMaxCollection;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclMinBinary;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclMinCollection;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclNotEmpty;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclNumberToString;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclOclAsSet;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclOclAsType;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclOclIsKindOf;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclOclIsTypeOf;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclOclIsUndefined;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclOclType;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclReverse;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclRound;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclSize;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclSizeString;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclSubString;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclSum;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclToBoolean;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclToInteger;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclToLowerCase;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclToReal;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclToUpperCase;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclUnionBagToBag;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclUnionBagToSet;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclUnionSetToBag;
import fr.enseeiht.ocl.xtext.ocl.operation.impl.OclUnionSetToSet;

public enum OclOperationEnum {
	allInstances(Arrays.asList(new OclAllInstances())),
	oclAsSet(Arrays.asList(new OclOclAsSet())),
	oclIsUndefined(Arrays.asList(new OclOclIsUndefined())),
	oclAsType(Arrays.asList(new OclOclAsType())),
	oclIsTypeOf(Arrays.asList(new OclOclIsTypeOf())),
	oclIsKindOf(Arrays.asList(new OclOclIsKindOf())),
	oclType(Arrays.asList(new OclOclType())),
	toString(Arrays.asList(new OclNumberToString(), new OclBooleanToString())),
	max(Arrays.asList(new OclMaxBinary(), new OclMaxCollection())),
	min(Arrays.asList(new OclMinBinary(), new OclMinCollection())),
	abs(Arrays.asList(new OclAbs())),
	floor(Arrays.asList(new OclFloor())),
	round(Arrays.asList(new OclRound())),
	concat(Arrays.asList(new OclConcat())),
	substring(Arrays.asList(new OclSubString())),
	toInteger(Arrays.asList(new OclToInteger())), //TODO + UnlimitedNatural
	toReal(Arrays.asList(new OclToReal())), //TODO
	toBoolean(Arrays.asList(new OclToBoolean())),
	toUpperCase(Arrays.asList(new OclToUpperCase())),
	toLowerCase(Arrays.asList(new OclToLowerCase())),
	indexOf(Arrays.asList(new OclIndexOf())), //TODO variante OrderedSet
	at(Arrays.asList(new OclAtString(), new OclAtSequence())),
	equalsIsIgnoreCase(Arrays.asList(new OclEqualsIgnoreCase())),
	characters(null), //TODO 
	matches(Arrays.asList(new OclMatches())),
	size(Arrays.asList(new OclSize(), new OclSizeString())),
	includes(Arrays.asList(new OclIncludes())),
	excludes(Arrays.asList(new OclExcludes())),
	includesAll(Arrays.asList(new OclIncludesAll())),
	excludesAll(Arrays.asList(new OclExcludesAll())),
	count(Arrays.asList(new OclCount())),
	isEmpty(Arrays.asList(new OclIsEmpty())),
	notEmpty(Arrays.asList(new OclNotEmpty())),
	sum(Arrays.asList(new OclSum())),
	product(null), //TODO 
	selectByKind(null), //TODO 
	selectByType(null), //TODO 
	asSet(Arrays.asList(new OclAsSet())),
	asOrderedSet(Arrays.asList(new OclAsOrderedSet())),
	asSequence(Arrays.asList(new OclAsSequence())),
	asBag(Arrays.asList(new OclAsBag())),
	flatten(Arrays.asList(new OclFlatten())),
	union(Arrays.asList(new OclUnionBagToBag(), new OclUnionBagToSet(), new OclUnionSetToSet(), new OclUnionSetToBag())),
	intersection(Arrays.asList(new OclIntersection())),
	including(Arrays.asList(new OclIncluding())),
	excluding(Arrays.asList(new OclExcluding())),
	symmetricDifference(null), //TODO 
	append(null), //TODO 
	prepend(null), //TODO 
	insertAt(null), //TODO 
	subOrderedSet(null), //TODO 
	first(Arrays.asList(new OclFirstSequence())), //TODO OrderedSet
	last(Arrays.asList(new OclLastSequence())),
	reverse(Arrays.asList(new OclReverse()));

	private List<IOclOperation> opList;

	private OclOperationEnum(List<IOclOperation> opList) {
		this.opList = opList;
	}

	public List<IOclOperation> getOperations(){
		return this.opList;
	}

	public static List<IOclOperation> getOperations(String name) {
		return OclOperationEnum.valueOf(name).getOperations();
	}
}
