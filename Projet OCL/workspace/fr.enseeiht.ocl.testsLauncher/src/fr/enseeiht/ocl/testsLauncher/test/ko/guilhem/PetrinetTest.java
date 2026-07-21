package fr.enseeiht.ocl.testsLauncher.test.ko.guilhem;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import fr.enseeiht.ocl.testsLauncher.util.LauncherUtils;
import fr.enseeiht.ocl.xtext.ocl.OclContextBlock;
import fr.enseeiht.ocl.xtext.ocl.OclInvariant;
import fr.enseeiht.yaoi.ValidationError;
import fr.enseeiht.yaoi.ValidationResult;

@DisplayName("Tests Petrinet de Guilhem KO")
class PetrinetTest {
	// Clés indexées par "Contexte::invariant" : un même nom d'invariant peut exister
	// dans plusieurs contextes (ex. nomPasCourt sur Network ET sur CaseElement) avec
	// des nombres d'erreurs differents.
	private static Map<String, Integer> expectedNumberOfErrors = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("Network::nomPasCourt", 1);
			put("Network::nomValide", 1);
			put("Network::nomUniqueCE", 1);
			put("CaseElement::nomPasCourt", 2);
			put("CaseElement::nomValide", 2);
			put("Place::quantitePositive", 1);
			put("Arc::poidsPositif", 1);
			put("Arc::casesDifferentes", 1);
			put("Arc::readArcValide", 1);
		}
	};

	private static Map<String, ValidationResult> resultMap;
	private static List<OclInvariant> invs;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Path workspacePath = Paths.get(new File(".").getAbsolutePath()).getParent().getParent();
		resultMap = LauncherUtils.run(workspacePath, "PetriNet-Guilhem-ko", "PetriNet.mocl", "PetriNet.ecore", "Network-batard.xmi");
		invs = LauncherUtils.getInvariants(workspacePath, "PetriNet-Guilhem-ko", "PetriNet.mocl");
	}

	@ParameterizedTest(name="{0}")
	@ArgumentsSource(InvsArgumentsProvider.class)
	@DisplayName("Network-batard")
	void testNetworkBlocageEMF(String invKey, List<ValidationError> errors) {
		LauncherUtils.assertErrorsSize(invKey, errors, expectedNumberOfErrors.getOrDefault(invKey, 0));
	}

	static class InvsArgumentsProvider implements ArgumentsProvider {

	    @Override
	    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
	    	ValidationResult result = resultMap.get(context.getDisplayName() + ".xmi");
			List<Arguments> arguments = new ArrayList<Arguments>();
	    	for (OclInvariant inv : invs) {
	    		String key = ((OclContextBlock) inv.eContainer()).getClass_().getName() + "::" + inv.getName();
	    		arguments.add(Arguments.of(key, result.getInvariantErrors(inv)));
			}
	        return Stream.of(arguments.toArray(new Arguments[0]));
	    }
	}
}