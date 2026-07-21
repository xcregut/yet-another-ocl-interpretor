package fr.enseeiht.yaoi.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.enseeiht.ocl.xtext.ocl.Import;
import fr.enseeiht.ocl.xtext.ocl.Module;
import fr.enseeiht.yaoi.OclInterpretor;
import fr.enseeiht.yaoi.ValidationError;
import fr.enseeiht.yaoi.ValidationResult;
import fr.enseeiht.yaoi.ui.others.ScrollableDialog;
import fr.enseeiht.yaoi.ui.others.ScrollableDialog.Status;
import fr.enseeiht.yaoi.ui.others.YaoiConsole;

/**
 * Handler for Validating MOCL resources against the selected XMI.
 * <p>
 * Récupère les modules MOCL en parcourant les ressources .mocl présentes dans le
 * ResourceSet de l'éditeur (ajoutées par le handler Load), les valide contre le
 * modèle XMI sélectionné, puis affiche les résultats dans une fenêtre.
 * </p>
 */
public class Validate extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
			reg.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

			// Récupère l'éditeur actif et son ResourceSet
			IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
			if (!(editor instanceof IEditingDomainProvider)) {
				throw new RuntimeException("Not an editing-domain-based editor!");
			}
			EditingDomain editorDomain = ((IEditingDomainProvider) editor).getEditingDomain();
			ResourceSet resourceSet = editorDomain.getResourceSet();
			Shell shell = HandlerUtil.getActiveShell(event);

			// Récupère les modules MOCL depuis les ressources .mocl de l'éditeur
			List<Module> modules = new ArrayList<Module>();
			for (Resource r : resourceSet.getResources()) {
				if (r.getURI() != null && "mocl".equals(r.getURI().fileExtension())
						&& !r.getContents().isEmpty()
						&& r.getContents().get(0) instanceof Module) {
					modules.add((Module) r.getContents().get(0));
				}
			}
			if (modules.isEmpty()) {
				MessageDialog.openError(shell, "Missing Mocl Resource",
						"Please load an .mocl file first by right-clicking on the model root and selecting 'MOCL -> Load'.");
				return null;
			}

			// Enregistre le métamodèle de chaque import (nsURI -> EPackage)
			for (Module module : modules) {
				for (Import eImport : module.getImports()) {
					EPackage ePackage = eImport.getPackage();
					if (ePackage != null && ePackage.getNsURI() != null) {
						EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
					}
				}
			}

			// Récupère la ressource XMI : via la sélection d'arbre, sinon depuis l'éditeur
			Resource xmiResource = null;
			Object selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
			if (selection instanceof IStructuredSelection) {
				Object firstElement = ((IStructuredSelection) selection).getFirstElement();
				if (firstElement instanceof XMIResourceImpl) {
					xmiResource = (Resource) firstElement;
				} else if (firstElement instanceof EObject) {
					xmiResource = ((EObject) firstElement).eResource();
				}
			}
			if (xmiResource == null) {
				for (Resource r : resourceSet.getResources()) {
					String ext = r.getURI().fileExtension();
					if (ext != null && ext.equals("xmi")) {
						xmiResource = r;
						break;
					}
				}
			}
			if (xmiResource == null) {
				MessageDialog.openError(shell, "Missing XMI", "No .xmi model found in the editor.");
				return null;
			}

			// Recharge le XMI à neuf pour qu'il utilise le métamodèle enregistré
			try {
				ResourceSet freshSet = new ResourceSetImpl();
				freshSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
						.put("xmi", new XMIResourceFactoryImpl());
				Resource freshXmi = freshSet.getResource(xmiResource.getURI(), true);
				EcoreUtil.resolveAll(freshXmi);
				if (!freshXmi.getContents().isEmpty()) {
					xmiResource = freshXmi;
				}
			} catch (Exception ignore) {
				// en cas d'échec, on garde la ressource de l'éditeur
			}

			// Validation + construction du message de résultat
			StringBuilder sb = new StringBuilder();
			boolean hasErrors = false;
			for (Module module : modules) {
				ValidationResult res = OclInterpretor.validate(xmiResource, module);

				String label = (module.eResource() != null) ? module.eResource().getURI().toString() : "mocl";
				sb.append(label + ":\n");

				if (!res.getErrors().isEmpty()) {
					hasErrors = true;
					for (ValidationError error : res.getErrors()) {
						sb.append(error.toString() + "\n");
					}
				} else {
					sb.append("The model conforms to all OCL constraints defined in the MOCL file.\n");
					sb.append("No violations were detected during validation.\n");
				}
				sb.append("\n");
			}

			String dialogTitle = hasErrors ? "Validation Results" : "Validation Success";
			String dialogMessage = hasErrors ? "The following validation errors were detected:"
					: "All validations passed successfully!";
			Status status = hasErrors ? Status.ERROR : Status.SUCCESS;

			ScrollableDialog dialog = new ScrollableDialog(shell, dialogTitle, dialogMessage, sb.toString(), status);
			dialog.open();
		} catch (Exception e) {
			YaoiConsole.printStackTrace(e);
		}

		return null;
	}
}