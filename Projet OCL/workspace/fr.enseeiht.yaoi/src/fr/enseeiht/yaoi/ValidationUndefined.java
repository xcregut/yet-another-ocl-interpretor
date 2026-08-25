package fr.enseeiht.yaoi;

import org.eclipse.emf.ecore.EObject;

import fr.enseeiht.ocl.xtext.ocl.OclInvariant;

public class ValidationUndefined implements ValidationError {
	private OclInvariant failedInvariant;
	private EObject testedObject;
	private String message;
	
	/**
	 * Create an ValidationFailed that indicate the failure of an invariant by an EObject 
	 * @param failedInvariant
	 * @param testedObject
	 * @param message
	 */
	public ValidationUndefined(OclInvariant failedInvariant, EObject testedObject, String message) {
		this.failedInvariant = failedInvariant;
		this.testedObject = testedObject;
		this.message = message;
	}
	
	/**
	 * Get the invariant that was violated
	 * @return
	 */
	@Override
	public OclInvariant getFailedInvariant() {
		return this.failedInvariant;
	}
	
	/**
	 * Get the object that violated the invariant
	 * @return
	 */
	@Override
	public EObject getTestedObject() {
		return this.testedObject;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	@Override
	public String toString() {
		return this.failedInvariant.getName() + " : indefini pour "
				+ ValidationFailed.describe(this.testedObject) + " (" + this.message + ")";
	}
}