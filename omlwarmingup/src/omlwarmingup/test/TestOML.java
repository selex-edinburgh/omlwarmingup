package omlwarmingup.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.junit.jupiter.api.Test;

import com.google.inject.Injector;

import io.opencaesar.oml.Aspect;
import io.opencaesar.oml.KeyAxiom;
import io.opencaesar.oml.OmlFactory;
import io.opencaesar.oml.OmlPackage;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.Vocabulary;
import io.opencaesar.oml.dsl.OmlStandaloneSetup;
import io.opencaesar.oml.resource.OmlJsonResourceFactory;
import io.opencaesar.oml.resource.OmlXMIResourceFactory;

/***
 * Warming up with Ontological Modeling Language
 * 
 * @author Alfa Yohannis
 *
 */
class TestOML {

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
			EPackage ePackage = (EPackage) resource.getContents().get(0);
			assertThat(ePackage.getName()).isEqualTo("oml");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * parse OML code/file to XMI.
	 * 
	 * @throws Exception
	 */
	@Test
	public void parseOmlFile() throws Exception {

		Injector injector = new OmlStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);

		File omlFile = new File("test.oml");
		File xmiFile = new File("test.xmi");

		String omlCode = "" + //
				"vocabulary <http://test#> as test {\n" + //
				"	concept c\n" + //
				"	aspect  a\n" + //
				"	relation entity R [\n" + //
				"		from c\n" + //
				" 		to a\n" + //
				"		forward r\n" + //
				"	]\n" + //
				"}\n";
		ByteArrayInputStream stream = new ByteArrayInputStream(omlCode.getBytes(StandardCharsets.UTF_8));

		Resource omlResource = resourceSet.createResource(URI.createFileURI(omlFile.getAbsolutePath()), null);

		omlResource.load(stream, null);

		EcoreUtil.resolveAll(omlResource);

		Resource xmiResource = resourceSet.createResource(URI.createFileURI(xmiFile.getAbsolutePath()), null);
		xmiResource.getContents().addAll(EcoreUtil.copyAll(omlResource.getContents()));
		try {
			omlResource.save(null);
			xmiResource.save(null);

			String contentOml = Files.readString(omlFile.toPath());
			assertThat(contentOml.contains("http://test#")).isTrue();

			String contentXmi = Files.readString(xmiFile.toPath());
			assertThat(contentXmi.contains("http://test#")).isTrue();
			
			omlResource.unload();
			xmiResource.unload();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		try {
			xmiResource.load(null);
			omlResource.getContents().addAll(EcoreUtil.copyAll(xmiResource.getContents()));
			omlResource.save(null);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			omlResource.save(baos, null);
			String code = new String(baos.toByteArray(), StandardCharsets.UTF_8);
			System.out.println(code);
			
			String contentOml = Files.readString(omlFile.toPath());
			assertThat(contentOml.contains("http://test#")).isTrue();

			String contentXmi = Files.readString(xmiFile.toPath());
			assertThat(contentXmi.contains("http://test#")).isTrue();
			
			omlResource.unload();
			xmiResource.unload();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * Test creating a 'pizza' <b>Concept</b> in XMI programmatically
	 */
	@Test
	public void createConcept() {

		File modelFileXmi = new File("pizza.xmi");
		File modelFileJson = new File("pizza.json");
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(OmlPackage.eNS_URI, OmlPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new OmlXMIResourceFactory());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", new OmlJsonResourceFactory());

		Resource resourceXmi = resourceSet.createResource(URI.createFileURI(modelFileXmi.getAbsolutePath()), null);
		Resource resourceJson = resourceSet.createResource(URI.createFileURI(modelFileJson.getAbsolutePath()), null);

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
		resourceXmi.getContents().add(vocab);
		resourceJson.getContents().add(EcoreUtil.copy(vocab));

		try {
			resourceXmi.save(null);
			String contentXmi = Files.readString(modelFileXmi.toPath());
			assertThat(contentXmi.contains(vocab.getNamespace())).isTrue();
			assertThat(contentXmi.contains(aspect.getName())).isTrue();
			assertThat(contentXmi.contains(scalarProperty.getName())).isTrue();

			resourceJson.save(null);
			String contentJson = Files.readString(modelFileJson.toPath());
			assertThat(contentJson.contains(vocab.getNamespace())).isTrue();
			assertThat(contentJson.contains(aspect.getName())).isTrue();
			assertThat(contentJson.contains(scalarProperty.getName())).isTrue();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
