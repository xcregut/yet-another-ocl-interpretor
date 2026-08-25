package fr.enseeiht.yaoi;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import fr.enseeiht.ocl.xtext.ocl.OclInvariant;
import fr.enseeiht.ocl.xtext.ocl.adapter.Invalid;
import fr.enseeiht.ocl.xtext.ocl.adapter.util.OCLValidationAdapterFactory;

/**
 * Object containig information about the violation of an invariant
 */
public class ValidationFailed implements ValidationError{
	private OclInvariant failedInvariant;
	private EObject testedObject;
	
	/**
	 * Create an ValidationFailed that indicate the failure of an invariant by an EObject 
	 * @param failedInvariant
	 * @param testedObject
	 * @param message
	 */
	public ValidationFailed(OclInvariant failedInvariant, EObject testedObject) {
		this.failedInvariant = failedInvariant;
		this.testedObject = testedObject;
	}
	
	/**
	 * Get the invariant that was violated
	 * @return
	 */
	@Override
	public OclInvariant getFailedInvariant() {
		return failedInvariant;
	}
	
	/**
	 * Get the object that violated the invariant
	 * @return
	 */
	@Override
	public EObject getTestedObject() {
		return testedObject;
	}
	
	@Override
	public String toString() {
		String tostring = "";
		if (this.getFailedInvariant().getErrorMessage() != null) {
			// Send message defined by user
			Object msg = OCLValidationAdapterFactory.INSTANCE.createAdapter(failedInvariant.getErrorMessage()).getValue(testedObject);
			if (msg instanceof Invalid inv) {
				tostring += "[" + inv.getMessage() + "]";
			} else {
				if (msg instanceof String messageString) {
					return messageString;
				} else {
					tostring += "[Failed to evaluate error message]";
				}
			}
		}
		tostring += this.failedInvariant.getName() + " : viole par " + describe(this.testedObject);
		return tostring;
	}

	/**
	 * Decrit un objet du modele de facon lisible : son type, et si possible son
	 * identifiant textuel (name, nom, id, titre ou label).
	 * Exemples : Composant "R1" / Composant (nom vide) / Empreinte
	 * @param obj l'objet a decrire
	 * @return une description lisible de l'objet
	 */
	static String describe(EObject obj) {
		if (obj == null) {
			return "objet inconnu";
		}
		String type = obj.eClass().getName();
		String[] idFeatures = { "name", "nom", "id", "titre", "label" };
		EStructuralFeature firstIdFeature = null;
		for (String idName : idFeatures) {
			for (EStructuralFeature feat : obj.eClass().getEAllStructuralFeatures()) {
				if (feat.getName().toLowerCase().strip().equals(idName)) {
					if (firstIdFeature == null) {
						firstIdFeature = feat;
					}
					Object value = obj.eGet(feat);
					if (value != null && !value.toString().isBlank()) {
						return type + " \"" + value + "\"";
					}
				}
			}
		}
		if (firstIdFeature != null) {
			return type + " (" + firstIdFeature.getName() + " vide)";
		}
		return type;
	}
}