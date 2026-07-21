package fr.enseeiht.yaoi.ui.handlers;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import fr.enseeiht.ocl.xtext.OclStandaloneSetup;
import fr.enseeiht.ocl.xtext.ocl.Import;
import fr.enseeiht.ocl.xtext.ocl.Module;

import fr.enseeiht.yaoi.ui.others.YaoiConsole;

/**
 * Handler for loading MOCL resources.
 * <p>
 * Charge le .mocl avec le moteur Xtext (pour résoudre ses références), lie son
 * import au métamodèle .ecore chargé dans l'éditeur, puis AJOUTE la ressource
 * .mocl au ResourceSet de l'éditeur. Ainsi le handler Validate peut la retrouver
 * en parcourant les ressources de l'éditeur (pas de champ statique).
 * </p>
 */
public class Load extends AbstractHandler {

	private static Injector xtextInjector;

	private static synchronized Injector getXtextInjector() {
		if (xtextInjector == null) {
			xtextInjector = new OclStandaloneSetup().createInjectorAndDoEMFRegistration();
		}
		return xtextInjector;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Récupère l'éditeur courant et son ResourceSet
			IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
			if (!(editor instanceof IEditingDomainProvider)) {
				throw new RuntimeException("Not an editing-domain-based editor!");
			}
			EditingDomain domain = ((IEditingDomainProvider) editor).getEditingDomain();
			ResourceSet editorResourceSet = domain.getResourceSet();

			Shell shell = HandlerUtil.getActiveShell(event);

			// URI du métamodèle .ecore chargé dans l'éditeur (via Load Resource)
			URI ecoreUri = findEcoreUri(editorResourceSet);
			if (ecoreUri == null) {
				MessageDialog.openWarning(shell, "Metamodel not found",
						"Le métamodèle (.ecore) n'est pas chargé dans l'éditeur. "
						+ "Chargez-le d'abord : clic droit sur la racine du modèle -> Load Resource.");
				return null;
			}

			// Filtre : n'afficher que les .mocl
			List<ViewerFilter> filters = Collections.singletonList(new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					return (element instanceof IContainer)
							|| ((element instanceof File) && ((File) element).getFileExtension().equals("mocl"));
				}
			});

			IFile[] files = WorkspaceResourceDialog.openFileSelection(shell, "Load Resource", "Select resource(s)",
					true, null, filters);

			if (files != null && files.length > 0) {
				Injector injector = getXtextInjector();

				for (IFile file : files) {
					URI moclUri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);

					// Retire une éventuelle ancienne version de ce .mocl déjà présente dans l'éditeur
					Resource previous = editorResourceSet.getResource(moclUri, false);
					if (previous != null) {
						editorResourceSet.getResources().remove(previous);
					}

					// Charge le .mocl avec le moteur Xtext (résout ses références)
					XtextResourceSet xtextSet = injector.getInstance(XtextResourceSet.class);
					Resource moclResource = xtextSet.getResource(moclUri, true);
					EcoreUtil.resolveAll(moclResource);

					if (moclResource.getContents().isEmpty()
							|| !(moclResource.getContents().get(0) instanceof Module)) {
						MessageDialog.openError(shell, "Invalid Mocl", "This file is not a valid MOCL module.");
						continue;
					}
					Module mocl = (Module) moclResource.getContents().get(0);

					// Charge le métamodèle dans le même ResourceSet Xtext et lie l'import
					Resource ecoreResource = xtextSet.getResource(ecoreUri, true);
					EcoreUtil.resolveAll(ecoreResource);
					if (ecoreResource.getContents().isEmpty()
							|| !(ecoreResource.getContents().get(0) instanceof EPackage)) {
						MessageDialog.openError(shell, "Invalid Ecore", "The metamodel is not a valid EPackage.");
						continue;
					}
					EPackage ecorePackage = (EPackage) ecoreResource.getContents().get(0);

					moclResource.getContents().add(EcoreUtil.copy(ecorePackage));
					for (Import eImport : mocl.getImports()) {
						eImport.setPackage(ecorePackage);
					}
					EcoreUtil.resolveAll(moclResource);

					// AJOUTE la ressource .mocl (déjà résolue) au ResourceSet de l'éditeur.
					// C'est ainsi que Validate la retrouvera.
					editorResourceSet.getResources().add(moclResource);
				}
			}
		} catch (Exception e) {
			YaoiConsole.printStackTrace(e);
		}
		return null;
	}

	/** URI de la ressource .ecore (EPackage à nsURI non-null) chargée dans l'éditeur. */
	private static URI findEcoreUri(ResourceSet editorResourceSet) {
		for (Resource res : editorResourceSet.getResources()) {
			if (!res.getContents().isEmpty() && res.getContents().get(0) instanceof EPackage) {
				EPackage p = (EPackage) res.getContents().get(0);
				if (p.getNsURI() != null) {
					return res.getURI();
				}
			}
		}
		return null;
	}
}