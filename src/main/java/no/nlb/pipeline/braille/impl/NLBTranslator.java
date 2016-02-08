package no.nlb.pipeline.braille.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.copyOfRange;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.concat;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.memoize;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;

public interface NLBTranslator {
	
	@Component(
		name = "no.nlb.pipeline.braille.impl.NLBTranslator.Provider",
		service = {
			BrailleTranslatorProvider.class,
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<BrailleTranslator> implements BrailleTranslatorProvider<BrailleTranslator> {
		
		private URI href;
		
		@Activate
		private void activate(ComponentContext context, final Map<?,?> properties) {
			href = asURI(context.getBundleContext().getBundle().getEntry("xml/block-translate.xpl"));
		}
		
		private final static Query grade0Table = mutableQuery().add("liblouis-table", "http://www.nlb.no/liblouis/no-no-g0.utb");
		private final static Query grade1Table = mutableQuery().add("liblouis-table", "http://www.nlb.no/liblouis/no-no-g1.ctb");
		private final static Query grade2Table = mutableQuery().add("liblouis-table", "http://www.nlb.no/liblouis/no-no-g2.ctb");
		private final static Query grade3Table = mutableQuery().add("liblouis-table", "http://www.nlb.no/liblouis/no-no-g3.ctb");
		private final static Query grade0Table8dot = mutableQuery().add("liblouis-table", "http://www.nlb.no/liblouis/no-no-g0.utb");
		private final static Query hyphenTable = mutableQuery().add("libhyphen-table",
		                                                            "http://www.libreoffice.org/dictionaries/hyphen/hyph_nb_NO.dic");
		private final static Query fallbackHyphenationTable = mutableQuery().add("hyphenator", "tex").add("locale", "nb");
		
		private final static Iterable<BrailleTranslator> empty = Iterables.<BrailleTranslator>empty();
		
		private final static List<String> supportedInput = ImmutableList.of("css","text-css","dtbook","html");
		private final static List<String> supportedOutput = ImmutableList.of("css","braille");
		
		/**
		 * Recognized features:
		 *
		 * - translator: Will only match if the value is `nlb'.
		 * - locale: Will only match if the language subtag is 'no'.
		 * - grade: `0', `1', `2' or `3'.
		 * - dots: `6', `8'. (default 6)
		 *
		 */
		protected final Iterable<BrailleTranslator> _get(Query query) {
			final MutableQuery q = mutableQuery(query);
			for (Feature f : q.removeAll("input"))
				if (!supportedInput.contains(f.getValue().get()))
					return empty;
			for (Feature f : q.removeAll("output"))
				if (!supportedOutput.contains(f.getValue().get()))
					return empty;
			if (q.containsKey("locale"))
				if (!"no".equals(parseLocale(q.removeOnly("locale").getValue().get()).getLanguage()))
					return empty;
			if (q.containsKey("translator"))
				if ("nlb".equals(q.removeOnly("translator").getValue().get()))
					if (q.containsKey("grade")) {
						String v = q.removeOnly("grade").getValue().get();
						final int grade;
						if (v.equals("0"))
							grade = 0;
						else if (v.equals("1"))
							grade = 1;
						else if (v.equals("2"))
							grade = 2;
						else if (v.equals("3"))
							grade = 3;
						else
							return empty;
						final int dots;
						if (q.containsKey("dots") && q.removeOnly("dots").getValue().get().equals("8"))
							dots = 8;
						else
							dots = 6;
						if (q.isEmpty()) {
							Iterable<Hyphenator> hyphenators = concat(
								logSelect(hyphenTable, hyphenatorProvider),
								logSelect(fallbackHyphenationTable, hyphenatorProvider));
							final Query liblouisTable;
							if (dots == 8)
								liblouisTable = grade0Table8dot;
							else
								liblouisTable = grade == 3 ? grade3Table : grade == 2 ? grade2Table : grade == 1 ? grade1Table : grade0Table;
							return concat(
								Iterables.transform(
									concat(
										concat(
											Iterables.transform(
												hyphenators,
												new Function<Hyphenator,String>() {
													public String _apply(Hyphenator h) {
														return h.getIdentifier(); }}),
											"liblouis"),
										"none"),
									new Function<String,Iterable<BrailleTranslator>>() {
										public Iterable<BrailleTranslator> _apply(final String hyphenator) {
											return concat(
												transform(
													logSelect(mutableQuery(liblouisTable).add("hyphenator", hyphenator),
													          liblouisTranslatorProvider),
													new Function<LiblouisTranslator,Iterable<BrailleTranslator>>() {
														public Iterable<BrailleTranslator> _apply(final LiblouisTranslator translator) {
															return Iterables.transform(
																logSelect(mutableQuery(grade0Table).add("hyphenator", hyphenator),
																          liblouisTranslatorProvider),
																new Function<LiblouisTranslator,BrailleTranslator>() {
																	public BrailleTranslator _apply(LiblouisTranslator grade0Translator) {
																		return __apply(
																			logCreate(
																				new TransformImpl(grade, dots, translator, grade0Translator))); }} ); }} )); }} )); }}
			return empty;
		}
		
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
		
		private final static Splitter.MapSplitter CSS_PARSER
		= Splitter.on(';').omitEmptyStrings().withKeyValueSeparator(Splitter.on(':').limit(2).trimResults());
		private final static Splitter TEXT_TRANSFORM_PARSER = Splitter.on(' ').omitEmptyStrings().trimResults();
		
		private class TransformImpl extends AbstractBrailleTranslator {
			
			private final XProc xproc;
			private final FromStyledTextToBraille translator;
			private final FromStyledTextToBraille grade0Translator;
			private final int grade;
			private final int dots;
			
			private TransformImpl(int grade, int dots, LiblouisTranslator translator, LiblouisTranslator grade0Translator) {
				Map<String,String> options = ImmutableMap.<String,String>of(
					"text-transform", mutableQuery().add("id", this.getIdentifier()).toString(),
					"contraction-grade", ""+grade);
				xproc = new XProc(href, null, options);
				this.translator = translator.fromStyledTextToBraille();
				this.grade0Translator = grade0Translator.fromStyledTextToBraille();
				this.grade = grade;
				this.dots = dots;
			}
			
			@Override
			public XProc asXProc() {
				return xproc;
			}
			
			@Override
			public FromStyledTextToBraille fromStyledTextToBraille() {
				return fromStyledTextToBraille;
			}
			
			private final FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
				public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText) {
					int size = size(styledText);
					String[] text = new String[size];
					String[] style = new String[size];
					int i = 0;
					for (CSSStyledText t : styledText) {
						text[i] = t.getText();
						style[i] = t.getStyle();
						i++; }
					return Arrays.asList(TransformImpl.this.transform(text, style));
				}
			};
			
			private String[] transform(String[] text, String[] cssStyle, boolean[] uncontracted) {
				if (text.length == 0)
					return new String[]{};
				String[] result = new String[text.length];
				boolean uncont = false;
				int j = 0;
				List<String> uncontStyle = null;
				for (int i = 0; i < text.length; i++) {
					Map<String,String> style = new HashMap<String,String>(CSS_PARSER.split(cssStyle[i]));
					List<String> textTransform = null; {
						String t = style.remove("text-transform");
						if (t != null)
							textTransform = newArrayList(TEXT_TRANSFORM_PARSER.split(t)); }
					if ((textTransform != null && textTransform.remove("uncontracted"))
					    || uncontracted[i]) {
						if (i > 0 && !uncont)
							for (String s : transformArray(translator,
							                               copyOfRange(text, j, i),
							                               copyOfRange(cssStyle, j, i)))
								result[j++] = s;
						if (uncontStyle == null)
							uncontStyle = new ArrayList<String>();
						String newStyle = "";
						for (Map.Entry<String,String> kv : style.entrySet())
							newStyle += (kv.getKey() + ": " + kv.getValue() + ";");
						if (textTransform != null && textTransform.size() > 0)
							newStyle += ("text-transform: " + join(textTransform, " ") + ";");
						uncontStyle.add(newStyle);
						uncont = true; }
					else {
						if (i > 0 && uncont) {
							for (String s : transformArray(grade0Translator,
							                               copyOfRange(text, j, i),
							                               uncontStyle.toArray(new String[i - j])))
								result[j++] = s;
							uncontStyle = null; }
						uncont = false; }}
				if (uncont)
					for (String s : transformArray(grade0Translator,
					                               copyOfRange(text, j, text.length),
					                               uncontStyle.toArray(new String[text.length - j])))
						result[j++] = s;
				else
					for (String s : transformArray(translator,
					                               copyOfRange(text, j, text.length),
					                               copyOfRange(cssStyle, j, text.length)))
						result[j++] = s;
				return result;
			}
			
			private String[] transform(String[] text, String[] cssStyle) {
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
				String[] segmentsStyle = new String[segments.length];
				for (int i = 0; i < segments.length; i++)
					segmentsStyle[i] = cssStyle[mapping[i]];
				brailleSegments = transform(segments, segmentsStyle, computer);
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
			
			private String[] transformArray(FromStyledTextToBraille translator, String[] text, String[] style) {
				List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
				for (int i = 0; i < text.length; i++)
					styledText.add(new CSSStyledText(text[i], style[i]));
				String[] result = new String[text.length];
				int i = 0;
				for (String s : translator.transform(styledText))
					result[i++] = s;
				return result;
			}
			
			@Override
			public String toString() {
				return Objects.toStringHelper(NLBTranslator.class.getSimpleName())
					.add("grade", grade)
					.add("dots", dots)
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
	
		private List<TransformProvider<LiblouisTranslator>> liblouisTranslatorProviders
		= new ArrayList<TransformProvider<LiblouisTranslator>>();
		private TransformProvider.util.MemoizingProvider<LiblouisTranslator> liblouisTranslatorProvider
		= memoize(dispatch(liblouisTranslatorProviders));
		
		@Reference(
			name = "HyphenatorProvider",
			unbind = "unbindHyphenatorProvider",
			service = Hyphenator.Provider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		@SuppressWarnings(
			"unchecked" // safe cast to TransformProvider<Hyphenator>
		)
		protected void bindHyphenatorProvider(Hyphenator.Provider<?> provider) {
			hyphenatorProviders.add((TransformProvider<Hyphenator>)provider);
		}
	
		protected void unbindHyphenatorProvider(Hyphenator.Provider<?> provider) {
			hyphenatorProviders.remove(provider);
			hyphenatorProvider.invalidateCache();
		}
		
		private List<TransformProvider<Hyphenator>> hyphenatorProviders
		= new ArrayList<TransformProvider<Hyphenator>>();
		private TransformProvider.util.MemoizingProvider<Hyphenator> hyphenatorProvider
		= memoize(dispatch(hyphenatorProviders));
		
	}
}
