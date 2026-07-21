package fr.enseeiht.ocl.testsLauncher.test.ok.guilhem;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import fr.enseeiht.ocl.xtext.ocl.OclInvariant;
import fr.enseeiht.yaoi.ValidationError;
import fr.enseeiht.yaoi.ValidationResult;

@DisplayName("Tests SimplePDL de Guilhem OK")
class SimplePDLTest {
	private static Map<String, ValidationResult> resultMap;
	private static List<OclInvariant> invs;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Path workspacePath = Paths.get(new File(".").getAbsolutePath()).getParent().getParent();
		resultMap = LauncherUtils.run(workspacePath, "SimplePDL-Guilhem-ok", "SimplePDL.mocl", "SimplePDL.ecore", "Process-blocage.xmi", "Process-concurrence.xmi", "Process-developpement.xmi", "Process-patisserie.xmi", "Process-penurie.xmi");
		invs = LauncherUtils.getInvariants(workspacePath, "SimplePDL-Guilhem-ok", "SimplePDL.mocl");
	}
	
	@ParameterizedTest(name="{0}")
	@ArgumentsSource(InvsArgumentsProvider.class)
	@DisplayName("Process-blocage")
	void testNetworkBlocageEMF(String invName, List<ValidationError> errors) {
		LauncherUtils.assertErrorsSize(invName, errors, 0);
	}
	
	@ParameterizedTest(name="{0}")
	@ArgumentsSource(InvsArgumentsProvider.class)
	@DisplayName("Process-concurrence")
	void testNetworkDeveloppementEMF(String invName, List<ValidationError> errors) {
		LauncherUtils.assertErrorsSize(invName, errors, 0);
	}
	
	@ParameterizedTest(name="{0}")
	@ArgumentsSource(InvsArgumentsProvider.class)
	@DisplayName("Process-developpement")
	void testNetworkPatisserieEMF(String invName, List<ValidationError> errors) {
		LauncherUtils.assertErrorsSize(invName, errors, 0);
	}
	
	@ParameterizedTest(name="{0}")
	@ArgumentsSource(InvsArgumentsProvider.class)
	@DisplayName("Process-patisserie")
	void testNetworkReadarc(String invName, List<ValidationError> errors) {
		LauncherUtils.assertErrorsSize(invName, errors, 0);
	}
	
	@ParameterizedTest(name="{0}")
	@ArgumentsSource(InvsArgumentsProvider.class)
	@DisplayName("Process-penurie")
	void testNetworkSaisons(String invName, List<ValidationError> errors) {
		LauncherUtils.assertErrorsSize(invName, errors, 0);
	}
	
	static class InvsArgumentsProvider implements ArgumentsProvider {
		
	    @Override
	    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
	    	ValidationResult result = resultMap.get(context.getDisplayName() + ".xmi");
			List<Arguments> arguments = new ArrayList<Arguments>();
	    	for (OclInvariant inv : invs) {
	    		arguments.add(Arguments.of(inv.getName(), result.getInvariantErrors(inv)));
			}
	        return Stream.of(arguments.toArray(new Arguments[0]));
	    }
	}
}