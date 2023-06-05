package omlwarmingup.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

import io.opencaesar.oml.Aspect;
import io.opencaesar.oml.KeyAxiom;
import io.opencaesar.oml.OmlFactory;
import io.opencaesar.oml.OmlPackage;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.resource.OmlJsonResourceFactory;
import io.opencaesar.oml.resource.OmlXMIResourceFactory;

@ExtendWith(InjectionExtension.class)
//@InjectWith(OmlInjectorProvider.class)

class TestOML {

	
	@Inject
	ParseHelper<Ontology> parseHelper;
	
	/***
	 * Test loading oml.ecore
	 */
	@Test
	public void loadingOmlEcore() {
		File modelFile = new File("oml.ecore");
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(OmlPackage.eNS_URI, OmlPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI.createFileURI(modelFile.getAbsolutePath()), null);

		try {
			resource.load(null);
			assertThat(resource.getContents().size()).isGreaterThan(0);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * parse OML file.
	 * @throws Exception 
	 */
	@Test
	public void parseOmlFile() throws Exception {
		Ontology result = parseHelper.parse(""
				+ "vocabulary <http://test#> as test {\n"
				+ "		concept c\n"
				+ "		aspect  a\n"
				+ "		relation entity R [\n"
				+ "			from c\n"
				+ "			to a\n"
				+ "			forward r\n"
				+ "		]\n"
				+ "}\n");
		Assertions.assertNotNull(result);
		List<Diagnostic> errors = result.eResource().getErrors();
	}

	/***
	 * Test creating a 'pizza' <b>Concept</b> in XMI programmatically
	 */
	@Test
	public void createConcept() {

		File modelFile = new File("pizza.xmi");
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(OmlPackage.eNS_URI, OmlPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("oml", new OmlXMIResourceFactory());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", new OmlJsonResourceFactory());
		Resource resource = resourceSet.createResource(URI.createFileURI(modelFile.getAbsolutePath()), null);

		OmlFactory factory = OmlFactory.eINSTANCE;

		// create vocabulary pizza
		Vocabulary vocab = factory.createVocabulary();
		vocab.setNamespace("http://example.com/tutorial1/vocabulary/pizza#");
		vocab.setPrefix("pizza");

		// create aspect IdentifiedThing
		Aspect aspect = factory.createAspect();
		aspect.setOwningVocabulary(vocab);
		aspect.setName("IdentifiedThing");

		// create scalar property hasId
		ScalarProperty scalarProperty = factory.createScalarProperty();
		scalarProperty.setOwningVocabulary(vocab);
		scalarProperty.setName("hasId");
		scalarProperty.getDomains().add(aspect);
		scalarProperty.setFunctional(true);
		
//		scalarProperty.getRanges().add();

		KeyAxiom key = factory.createKeyAxiom();
		key.getProperties().add(scalarProperty);
		aspect.getOwnedKeys().add(key);

		// add all previous statements to vocab
		vocab.getOwnedStatements().add(aspect);
		vocab.getOwnedStatements().add(scalarProperty);

		// add vocav to resource
		resource.getContents().add(vocab);

		try {
			resource.save(null);
			String content = Files.readString(modelFile.toPath());
			assertThat(content.contains(vocab.getNamespace())).isTrue();
			assertThat(content.contains(aspect.getName())).isTrue();
			assertThat(content.contains(scalarProperty.getName())).isTrue();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
