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

@DisplayName("Tests SimplePDL de Guilhem KO")
class SimplePDLTest {
	// Clés indexées par "Contexte::invariant" (un même nom existe dans plusieurs contextes).
	private static Map<String, Integer> expectedNumberOfErrors = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put("Process::nomValide", 1);
			put("Process::nomUniqueWD", 1);
			put("Process::nomUniqueR", 1);
			put("WorkSequence::nonReflexif", 1);
			put("WorkSequence::successeurEtPredecesseurDansMemeProcess", 1);
			put("WorkDefinition::nomPasCourt", 2);
			put("WorkDefinition::nomValide", 2);
			put("WorkDefinition::unSeulNeedParRessource", 2);
			put("Ressource::quantitePositive", 1);
			put("Ressource::nomPasCourt", 2);
			put("Ressource::nomValide", 2);
			put("RessourceNeed::quantiteStrictPositive", 1);
		}
	};

	private static Map<String, ValidationResult> resultMap;
	private static List<OclInvariant> invs;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Path workspacePath = Paths.get(new File(".").getAbsolutePath()).getParent().getParent();
		resultMap = LauncherUtils.run(workspacePath, "SimplePDL-Guilhem-ko", "SimplePDL.mocl", "SimplePDL.ecore", "Process-batard.xmi");
		invs = LauncherUtils.getInvariants(workspacePath, "SimplePDL-Guilhem-ko", "SimplePDL.mocl");
	}

	@ParameterizedTest(name="{0}")
	@ArgumentsSource(InvsArgumentsProvider.class)
	@DisplayName("Process-batard")
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