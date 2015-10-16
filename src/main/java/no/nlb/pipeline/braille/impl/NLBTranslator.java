package no.nlb.pipeline.braille.impl;

import java.net.URI;
import java.util.ArrayList;
import static java.util.Arrays.copyOfRange;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Lists.newArrayList;

import static org.daisy.pipeline.braille.css.Query.parseQuery;
import org.daisy.pipeline.braille.common.AbstractTransform;
import org.daisy.pipeline.braille.common.AbstractTransform.Provider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransform.Provider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransform.Provider.util.Iterables.concat;
import static org.daisy.pipeline.braille.common.AbstractTransform.Provider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransform.Provider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransform.Provider.util.logSelect;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.CSSBlockTransform;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.CSSStyledTextTransform;
import org.daisy.pipeline.braille.common.TextTransform;
import org.daisy.pipeline.braille.common.Transform;
import static org.daisy.pipeline.braille.common.Transform.Provider.util.dispatch;
import static org.daisy.pipeline.braille.common.Transform.Provider.util.memoize;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.Tuple3;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import org.daisy.pipeline.braille.common.XProcTransform;
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
	public class Provider extends AbstractTransform.Provider<NLBTranslator>
	                      implements BrailleTranslator.Provider<NLBTranslator>,
	                                 TextTransform.Provider<NLBTranslator>,
	                                 XProcTransform.Provider<NLBTranslator>,
	                                 CSSBlockTransform.Provider<NLBTranslator> {
		
		private URI href;
		
		@Activate
		private void activate(ComponentContext context, final Map<?,?> properties) {
			href = asURI(context.getBundleContext().getBundle().getEntry("xml/block-translate.xpl"));
		}
		
		private final static String grade0Table = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g0.utb')";
		private final static String grade1Table = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g1.ctb')";
		private final static String grade2Table = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g2.ctb')";
		private final static String grade3Table = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g3.ctb')";
		private final static String grade0Table8dot = "(liblouis-table:'http://www.nlb.no/liblouis/no-no-g0.utb')";
		private final static String hyphenationTable = "(libhyphen-table:'http://www.libreoffice.org/dictionaries/hyphen/hyph_nb_NO.dic')";
		private final static String fallbackHyphenationTable = "(hyphenator:tex)(locale:nb)";
		
		private final static Iterable<NLBTranslator> empty = Iterables.<NLBTranslator>empty();
		
		/**
		 * Recognized features:
		 *
		 * - translator: Will only match if the value is `nlb'.
		 * - locale: Will only match if the language subtag is 'no'.
		 * - grade: `0', `1', `2' or `3'.
		 * - dots: `6', `8'. (default 6)
		 *
		 */
		protected final Iterable<NLBTranslator> _get(final String query) {
			Map<String,Optional<String>> q = new HashMap<String,Optional<String>>(parseQuery(query));
			Optional<String> o;
			if ((o = q.remove("locale")) != null)
				if (!"no".equals(parseLocale(o.get()).getLanguage()))
					return empty;
			if ((o = q.remove("translator")) != null)
				if (o.get().equals("nlb"))
					if ((o = q.remove("grade")) != null) {
						final int grade, dots;
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
						if ((o = q.remove("dots")) != null && o.get().equals("8"))
							dots = 8;
						else
							dots = 6;
						if (q.size() == 0) {
							Iterable<Hyphenator> hyphenators = concat(
								logSelect(hyphenationTable, hyphenatorProvider),
								logSelect(fallbackHyphenationTable, hyphenatorProvider));
							final String liblouisTable;
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
									new Function<String,Iterable<NLBTranslator>>() {
										public Iterable<NLBTranslator> _apply(final String hyphenator) {
											return concat(
												Iterables.transform(
													logSelect(liblouisTable + "(hyphenator:" + hyphenator + ")", liblouisTranslatorProvider),
													new Function<LiblouisTranslator,Iterable<NLBTranslator>>() {
														public Iterable<NLBTranslator> _apply(final LiblouisTranslator translator) {
															return Iterables.transform(
																logSelect(grade0Table + "(hyphenator:" + hyphenator + ")", liblouisTranslatorProvider),
																new Function<LiblouisTranslator,NLBTranslator>() {
																	public NLBTranslator _apply(LiblouisTranslator grade0Translator) {
																		return __apply(
																			logCreate(
																				(NLBTranslator)new TransformImpl(grade, dots, translator, grade0Translator, query)
																			)
																		); }} ); }} )); }} )); }}
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
		
		private class TransformImpl extends AbstractTransform implements NLBTranslator {
			
			private final Tuple3<URI,QName,Map<String,String>> xproc;
			private final LiblouisTranslator translator;
			private final LiblouisTranslator grade0Translator;
			private final int grade;
			private final int dots;
			
			private TransformImpl(int grade, int dots, LiblouisTranslator translator, LiblouisTranslator grade0Translator, String translatorQuery) {
				Map<String,String> options = ImmutableMap.of("text-transform", "(id:" + this.getIdentifier() + ")");
				xproc = new Tuple3<URI,QName,Map<String,String>>(href, null, options);
				this.translator = translator;
				this.grade0Translator = grade0Translator;
				this.grade = grade;
				this.dots = dots;
			}
			
			public TextTransform asTextTransform() {
				return this;
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
			
			private String[] transform(String[] text, String[] cssStyle, boolean[] uncontracted) {
				if (text.length == 0)
					return new String[]{};
				String[] result = new String[text.length];
				boolean uncont = false;
				int j = 0;
				List<String> uncontStyle = null;
				for (int i = 0; i < text.length; i++) {
					Map<String,String> style = cssStyle == null ? null :
						new HashMap<String,String>(CSS_PARSER.split(cssStyle[i]));
					List<String> textTransform = null; {
						if (style != null) {
							String t = style.remove("text-transform");
							if (t != null)
								textTransform = newArrayList(TEXT_TRANSFORM_PARSER.split(t)); }}
					if ((textTransform != null && textTransform.remove("uncontracted"))
					    || uncontracted[i]) {
						if (i > 0 && !uncont)
							for (String s : cssStyle == null ? translator.transform(copyOfRange(text, j, i))
								                             : translator.transform(copyOfRange(text, j, i),
								                                                    copyOfRange(cssStyle, j, i)))
								result[j++] = s;
						if (uncontStyle == null && cssStyle != null)
							uncontStyle = new ArrayList<String>();
						if (uncontStyle != null) {
							String newStyle = "";
							for (Map.Entry<String,String> kv : style.entrySet())
								newStyle += (kv.getKey() + ": " + kv.getValue() + ";");
							if (textTransform != null && textTransform.size() > 0)
								newStyle += ("text-transform: " + join(textTransform, " ") + ";");
							uncontStyle.add(newStyle); }
						uncont = true; }
					else {
						if (i > 0 && uncont) {
							for (String s : uncontStyle == null ? grade0Translator.transform(copyOfRange(text, j, i))
								                                : grade0Translator.transform(copyOfRange(text, j, i),
								                                                             uncontStyle.toArray(new String[i - j])))
								result[j++] = s;
							uncontStyle = null; }
						uncont = false; }}
				if (uncont)
					for (String s : uncontStyle == null ? grade0Translator.transform(copyOfRange(text, j, text.length))
						                                : grade0Translator.transform(copyOfRange(text, j, text.length),
						                                                             uncontStyle.toArray(new String[text.length - j])))
						result[j++] = s;
				else
					for (String s : cssStyle == null ? translator.transform(copyOfRange(text, j, text.length))
						                             : translator.transform(copyOfRange(text, j, text.length),
						                                                    copyOfRange(cssStyle, j, text.length)))
						result[j++] = s;
				return result;
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
					brailleSegments = transform(segments, null, computer);
				else {
					String[] segmentsStyle = new String[segments.length];
					for (int i = 0; i < segments.length; i++)
						segmentsStyle[i] = cssStyle[mapping[i]];
					brailleSegments = transform(segments, segmentsStyle, computer); }
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
	
		private List<Transform.Provider<LiblouisTranslator>> liblouisTranslatorProviders
		= new ArrayList<Transform.Provider<LiblouisTranslator>>();
		private Transform.Provider.MemoizingProvider<LiblouisTranslator> liblouisTranslatorProvider
		= memoize(dispatch(liblouisTranslatorProviders));
		
		@Reference(
			name = "HyphenatorProvider",
			unbind = "unbindHyphenatorProvider",
			service = Hyphenator.Provider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		@SuppressWarnings(
			"unchecked" // safe cast to Transform.Provider<Hyphenator>
		)
		protected void bindHyphenatorProvider(Hyphenator.Provider<?> provider) {
			hyphenatorProviders.add((Transform.Provider<Hyphenator>)provider);
		}
	
		protected void unbindHyphenatorProvider(Hyphenator.Provider<?> provider) {
			hyphenatorProviders.remove(provider);
			hyphenatorProvider.invalidateCache();
		}
		
		private List<Transform.Provider<Hyphenator>> hyphenatorProviders
		= new ArrayList<Transform.Provider<Hyphenator>>();
		private Transform.Provider.MemoizingProvider<Hyphenator> hyphenatorProvider
		= memoize(dispatch(hyphenatorProviders));
		
	}
}
