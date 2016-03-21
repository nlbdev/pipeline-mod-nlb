import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;

import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.BrailleTranslator.CSSStyledText;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.datatypes.DatatypeRegistry;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.calabashConfigFile;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.pipelineModule;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;
import static org.daisy.pipeline.pax.exam.Options.xprocspec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class NLBTest {
	
	@Inject
	private BundleContext context;
	
	@Test
	public void testEmail() throws Exception {
		List<BrailleTranslatorProvider<BrailleTranslator>> providers = new ArrayList<BrailleTranslatorProvider<BrailleTranslator>>();
		for (ServiceReference<? extends BrailleTranslatorProvider> ref : context.getServiceReferences(BrailleTranslatorProvider.class, null))
			providers.add(context.getService(ref));
		BrailleTranslator translator = dispatch(providers).get(query("(translator:nlb)(grade:1)")).iterator().next();
		assertEquals(
			braille("⠋⠕⠕ ⠣⠋⠕⠕⠃⠁⠗⠈⠝⠇⠃⠄⠝⠕⠜ ⠃⠼"),
			translator.fromStyledTextToBraille()
			          .transform(styledText("foo foobar@nlb.no bar", "")));
	}
	
	@Test
	public void testBrailleTranslatorUncontracted() throws Exception {
		List<BrailleTranslatorProvider<BrailleTranslator>> providers = new ArrayList<BrailleTranslatorProvider<BrailleTranslator>>();
		for (ServiceReference<? extends BrailleTranslatorProvider> ref : context.getServiceReferences(BrailleTranslatorProvider.class, null))
			providers.add(context.getService(ref));
		BrailleTranslator translator = dispatch(providers).get(query("(translator:nlb)(grade:1)")).iterator().next();
		assertEquals(
			braille("⠋⠕⠕⠃⠼ ","⠋⠕⠕⠃⠁⠗"),
			translator.fromStyledTextToBraille()
			          .transform(styledText("foobar ", "",
			                                "foobar",  "text-transform: uncontracted")));
	}
	
	@Inject
	private DatatypeRegistry datatypeRegistry;
	
	@Test
	@org.junit.Ignore
	public void testDatatypes() {
		assertTrue(datatypeRegistry.getDatatype("{http://www.nlb.no/pipeline/modules/braille/xml-to-pef.xpl}braille-standard").isPresent());
	}
	
	@Inject
	private XProcSpecRunner xprocspecRunner;
	
	@Test
	public void runXProcSpec() throws Exception {
		File baseDir = new File(PathUtils.getBaseDir());
		boolean success = xprocspecRunner.run(new File(baseDir, "src/test/xprocspec"),
		                                      new File(baseDir, "target/xprocspec-reports"),
		                                      new File(baseDir, "target/surefire-reports"),
		                                      new File(baseDir, "target/xprocspec"),
		                                      new XProcSpecRunner.Reporter.DefaultReporter());
		assertTrue("XProcSpec tests should run with success", success);
	}
	
	private static boolean onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			calabashConfigFile(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			thisBundle(),
			junitBundles(),
			systemPackage("javax.xml.stream;version=\"1.0.1\""),
			mavenBundlesWithDependencies(
				mavenBundle("org.daisy.pipeline:framework-core:?"),
				brailleModule("css-utils"),
				brailleModule("pef-utils"),
				brailleModule("liblouis-core"),
				brailleModule("liblouis-utils"),
				brailleModule("liblouis-tables"),
				brailleModule("liblouis-native").forThisPlatform(),
				brailleModule("libhyphen-core"),
				brailleModule("libhyphen-libreoffice-tables"),
				onWindows ? null : brailleModule("libhyphen-native").forThisPlatform(),
				brailleModule("dotify-formatter"),
				brailleModule("dtbook-to-pef"),
				brailleModule("epub3-to-pef"),
				brailleModule("html-to-pef"),
				brailleModule("xml-to-pef"),
				pipelineModule("file-utils"),
				pipelineModule("common-utils"),
				pipelineModule("html-utils"),
				pipelineModule("zip-utils"),
				pipelineModule("mediatype-utils"),
				pipelineModule("file-utils"),
				pipelineModule("fileset-utils"),
				logbackClassic(),
				xprocspec(),
				mavenBundle("org.daisy.maven:xproc-engine-daisy-pipeline:?")
			)
		);
	}
	
	private Iterable<CSSStyledText> styledText(String... textAndStyle) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		String text = null;
		boolean textSet = false;
		for (String s : textAndStyle) {
			if (textSet)
				styledText.add(new CSSStyledText(text, s));
			else
				text = s;
			textSet = !textSet; }
		if (textSet)
			throw new RuntimeException();
		return styledText;
	}
	
	private Iterable<String> braille(String... text) {
		return Arrays.asList(text);
	}
}
