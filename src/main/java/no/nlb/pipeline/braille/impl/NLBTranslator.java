package no.nlb.pipeline.braille.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import static org.daisy.pipeline.braille.css.Query.parseQuery;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.CSSBlockTransform;
import org.daisy.pipeline.braille.common.CSSStyledTextTransform;
import org.daisy.pipeline.braille.common.TextTransform;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.Transform.AbstractTransform;
import static org.daisy.pipeline.braille.common.Transform.Provider.util.dispatch;
import static org.daisy.pipeline.braille.common.Transform.Provider.util.logCreate;
import static org.daisy.pipeline.braille.common.Transform.Provider.util.logSelect;
import static org.daisy.pipeline.braille.common.Transform.Provider.util.memoize;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Tuple3;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import org.daisy.pipeline.braille.common.WithSideEffect;
import org.daisy.pipeline.braille.common.XProcTransform;
import org.daisy.pipeline.braille.libhyphen.LibhyphenHyphenator;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;

public interface NLBTranslator extends BrailleTranslator, CSSStyledTextTransform, CSSBlockTransform, XProcTransform {
	
	@Component(
		name = "no.nlb.pipeline.braille.impl.NLBTranslator.Provider",
		service = {
			BrailleTranslator.Provider.class,
			TextTransform.Provider.class,
			XProcTransform.Provider.class,
			CSSBlockTransform.Provider.class
		}
	)
	public class Provider implements BrailleTranslator.Provider<NLBTranslator>,
	                                 TextTransform.Provider<NLBTranslator>,
	                                 XProcTransform.Provider<NLBTranslator>,
	                                 CSSBlockTransform.Provider<NLBTranslator> {
		
		private URI href;
		
		@Activate
		private void activate(ComponentContext context, final Map<?,?> properties) {
			href = asURI(context.getBundleContext().getBundle().getEntry("xml/block-translate.xpl"));
		}
		
		/**
		 * Recognized features:
		 *
		 * - translator: Will only match if the value is `nlb'.
		 * - locale: Will only match if the language subtag is 'no'.
		 * - grade: `0', `1', `2' or `3'.
		 *
		 */
		public Iterable<NLBTranslator> get(String query) {
			 return impl.get(query);
		 }
	
		public Transform.Provider<NLBTranslator> withContext(Logger context) {
			return impl.withContext(context);
		}
	
		private Transform.Provider.MemoizingProvider<NLBTranslator> impl = new ProviderImpl(null);
	
		private class ProviderImpl extends AbstractProvider<NLBTranslator> {
			
			private final static String grade0Table = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g0.utb')";
			private final static String grade1Table = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g1.ctb')";
			private final static String grade2Table = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g2.ctb')";
			private final static String grade3Table = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g3.ctb')";
			private final static String hyphenationTable = "(libhyphen-table:'http://www.libreoffice.org/dictionaries/hyphen/hyph_nb_NO.dic')";
			
			private ProviderImpl(Logger context) {
				super(context);
			}
			
			protected Transform.Provider.MemoizingProvider<NLBTranslator> _withContext(Logger context) {
				return new ProviderImpl(context);
			}
			
			protected final Iterable<WithSideEffect<NLBTranslator,Logger>> __get(final String query) {
				Map<String,Optional<String>> q = new HashMap<String,Optional<String>>(parseQuery(query));
				Optional<String> o;
				if ((o = q.remove("locale")) != null)
					if (!"no".equals(parseLocale(o.get()).getLanguage()))
						return empty;
				if ((o = q.remove("translator")) != null)
					if (o.get().equals("nlb"))
						if ((o = q.remove("grade")) != null) {
							final int grade;
							if (o.get().equals("0"))
								grade = 0;
							else if (o.get().equals("1"))
								grade = 1;
							else if (o.get().equals("2"))
								grade = 2;
							else if (o.get().equals("3"))
								grade = 3;
							else
								return empty;
							if (q.size() == 0) {
								Iterable<WithSideEffect<LibhyphenHyphenator,Logger>> hyphenators
									= logSelect(hyphenationTable, libhyphenHyphenatorProvider.get(hyphenationTable));
								final String liblouisTable = grade == 0 ? grade0Table : grade == 1 ? grade1Table : grade == 2 ? grade2Table : grade3Table;
								return transform(
									hyphenators,
									new WithSideEffect.Function<LibhyphenHyphenator,NLBTranslator,Logger>() {
										public NLBTranslator _apply(LibhyphenHyphenator h) {
											String translatorQuery = liblouisTable + "(hyphenator:" + h.getIdentifier() + ")";
											LiblouisTranslator translator;
											try {
												translator = applyWithSideEffect(
													logSelect(
														translatorQuery,
														liblouisTranslatorProvider.get(translatorQuery)).iterator().next()); }
											catch (NoSuchElementException e) {
												throw new NoSuchElementException(); }
											return applyWithSideEffect(
												logCreate(
													(NLBTranslator)new TransformImpl(grade, translator, query))); }}); }}
				return empty;
			}
		}
	
		private final static Iterable<WithSideEffect<NLBTranslator,Logger>> empty
		= Optional.<WithSideEffect<NLBTranslator,Logger>>absent().asSet();
		
		// mimicking liblouis behavior
		private final static Pattern COMPUTER = Pattern.compile(
			"(?<=^|[\\x20\t\\n\\r\\u2800\\xA0])[^\\x20\t\\n\\r\\u2800\\xA0]*"
			+ "(?://|www\\.|\\.com|\\.edu|\\.gov|\\.mil|\\.net|\\.org|\\.no|\\.nu|\\.se|\\.dk|\\.fi|\\.ini|\\.doc|\\.docx"
			+ "|\\.xml|\\.xsl|\\.htm|\\.html|\\.tex|\\.txt|\\.gif|\\.jpg|\\.png|\\.wav|\\.mp3|\\.m3u|\\.tar|\\.gz|\\.bz2|\\.zip)"
			+ "[^\\x20\t\\n\\r\\u2800\\xA0]*"
			+ "(?=[\\x20\t\\n\\r\\u2800\\xA0]|$)"
		);
		private final static String OPEN_COMPUTER = "\u2823"; // dots 126 (<)
		private final static String CLOSE_COMPUTER = "\u281C"; // dots 345 (>)
		
		private class TransformImpl extends AbstractTransform implements NLBTranslator {
			
			private final Tuple3<URI,QName,Map<String,String>> xproc;
			private final LiblouisTranslator translator;
			private final int grade;
			
			private TransformImpl(int grade, LiblouisTranslator translator, String translatorQuery) {
				Map<String,String> options = ImmutableMap.of("text-transform", translatorQuery); // "(id:" + this.getIdentifier() + ")"
				xproc = new Tuple3<URI,QName,Map<String,String>>(href, null, options);
				this.translator = translator;
				this.grade = grade;
			}
			
			public Tuple3<URI,QName,Map<String,String>> asXProc() {
				return xproc;
			}
			
			public boolean isHyphenating() {
				return translator.isHyphenating();
			}
			
			public String transform(String text) {
				return transform(new String[]{text})[0];
			}
			
			public String[] transform(String[] text) {
				return transform(text, null);
			}
			
			public String transform(String text, String cssStyle) {
				return transform(new String[]{text}, new String[]{cssStyle})[0];
			}
			
			public String[] transform(String[] text, String[] cssStyle) {
				String[] segments;
				// which segments are an url or e-mail address
				boolean[] computer;
				// mapping from index in segments to index in text
				int[] mapping; {
					List<String> l1 = new ArrayList<String>();
					List<Boolean> l2 = new ArrayList<Boolean>();
					List<Integer> l3 = new ArrayList<Integer>();
					for (int i = 0; i < text.length; i++) {
						String t = text[i];
						if (t.isEmpty()) {
							l1.add(t);
							l2.add(false);
							l3.add(i); }
						else {
							Matcher m = COMPUTER.matcher(t);
							int j = 0;
							String s;
							while (m.find()) {
								s = t.substring(j, m.start());
								if (!s.isEmpty()) {
									l1.add(s);
									l2.add(false);
									l3.add(i); }
								s = m.group();
								if (!s.isEmpty()) {
									l1.add(s);
									l2.add(true);
									l3.add(i); }
								j = m.end(); }
							s = t.substring(j);
							if (!s.isEmpty()) {
								l1.add(s);
								l2.add(false);
								l3.add(i); }}}
					int len = l1.size();
					segments = new String[len];
					computer = new boolean[len];
					mapping = new int[len];
					for (int i = 0; i < len; i++) {
						segments[i] = l1.get(i);
						computer[i] = l2.get(i);
						mapping[i] = l3.get(i); }}
				String[] brailleSegments;
				if (cssStyle == null)
					brailleSegments = translator.transform(segments);
				else {
					String[] segmentsStyle = new String[segments.length];
					for (int i = 0; i < segments.length; i++)
						segmentsStyle[i] = cssStyle[mapping[i]];
					brailleSegments = translator.transform(segments, segmentsStyle); }
				String braille[] = new String[text.length];
				for (int i = 0; i < braille.length; i++)
					braille[i] = "";
				for (int i = 0; i < brailleSegments.length; i++)
					if (computer[i])
						braille[mapping[i]] += OPEN_COMPUTER + brailleSegments[i] + CLOSE_COMPUTER;
					else
						braille[mapping[i]] += brailleSegments[i];
				return braille;
			}
			
			@Override
			public String toString() {
				return toStringHelper(NLBTranslator.class.getSimpleName())
					.add("grade", grade)
					.toString();
			}
		}
		
		@Reference(
			name = "LiblouisTranslatorProvider",
			unbind = "unbindLiblouisTranslatorProvider",
			service = LiblouisTranslator.Provider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindLiblouisTranslatorProvider(LiblouisTranslator.Provider provider) {
			liblouisTranslatorProviders.add(provider);
		}
	
		protected void unbindLiblouisTranslatorProvider(LiblouisTranslator.Provider provider) {
			liblouisTranslatorProviders.remove(provider);
			liblouisTranslatorProvider.invalidateCache();
		}
	
		private List<Transform.Provider<LiblouisTranslator>> liblouisTranslatorProviders
		= new ArrayList<Transform.Provider<LiblouisTranslator>>();
		private Transform.Provider.MemoizingProvider<LiblouisTranslator> liblouisTranslatorProvider
		= memoize(dispatch(liblouisTranslatorProviders));
		
		@Reference(
			name = "LibhyphenHyphenatorProvider",
			unbind = "unbindLibhyphenHyphenatorProvider",
			service = LibhyphenHyphenator.Provider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindLibhyphenHyphenatorProvider(LibhyphenHyphenator.Provider provider) {
			libhyphenHyphenatorProviders.add(provider);
		}
	
		protected void unbindLibhyphenHyphenatorProvider(LibhyphenHyphenator.Provider provider) {
			libhyphenHyphenatorProviders.remove(provider);
			libhyphenHyphenatorProvider.invalidateCache();
		}
	
		private List<Transform.Provider<LibhyphenHyphenator>> libhyphenHyphenatorProviders
		= new ArrayList<Transform.Provider<LibhyphenHyphenator>>();
		private Transform.Provider.MemoizingProvider<LibhyphenHyphenator> libhyphenHyphenatorProvider
		= memoize(dispatch(libhyphenHyphenatorProviders));
		
	}
}
