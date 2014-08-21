/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.segmentation;

import static org.apache.uima.fit.util.CasUtil.getType;
import static org.apache.uima.fit.util.CasUtil.select;

import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * @author Richard Eckart de Castilho
 */
public abstract
class SegmenterBase
extends JCasAnnotator_ImplBase
{
    /**
     * A list of type names used for zoning.
     */
	public final static String PARAM_ZONE_TYPES = "zoneTypes";
	@ConfigurationParameter(name=PARAM_ZONE_TYPES, mandatory=false)
    private String[] zoneTypes;

    /**
     * Strict zoning causes the segmentation to be applied only within the
     * boundaries of a zone annotation. This works only if a single zone type
     * is specified (the zone annotations should NOT overlap) or if no zone
     * type is specified - in which case the whole document is taken as a zone.
     * If strict zoning is turned off, multiple zone types can be specified.
     * A list of all zone boundaries (start and end) is created and segmentation
     * happens between them.
     */
	public final static String PARAM_STRICT_ZONING = "StrictZoning";
	@ConfigurationParameter(name=PARAM_STRICT_ZONING, mandatory=true, defaultValue="true")
    private boolean strictZoning;

	/**
	 * The language.
	 */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name=PARAM_LANGUAGE, mandatory=false)
    private String language;

	/**
	 * Create {@link Token} annotations.
	 */
    public static final String PARAM_CREATE_TOKENS = "createTokens";
	@ConfigurationParameter(name=PARAM_CREATE_TOKENS, mandatory=true, defaultValue="true")
    private boolean createTokens;

	/**
	 * Create {@link Sentence} annotations.
	 */
    public static final String PARAM_CREATE_SENTENCES = "createSentences";
	@ConfigurationParameter(name=PARAM_CREATE_SENTENCES, mandatory=true, defaultValue="true")
    private boolean createSentences;

	//private int sentenceCount;
	private int tokenCount;

    public boolean isStrictZoning()
	{
		return strictZoning;
	}

    public boolean isCreateSentences()
	{
		return createSentences;
	}

    public boolean isCreateTokens()
	{
		return createTokens;
	}

    public String[] getZoneTypes()
	{
		return zoneTypes;
	}

    @Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
    {
    	//sentenceCount = 0;
    	tokenCount = 0;

		String text = jcas.getDocumentText();

		String[] zones = getZoneTypes();
		if (isStrictZoning()) {
			if (zones == null || zones.length == 0) {
				process(jcas, text.substring(0, text.length()), 0);
			}
			else if (zones.length != 1) {
				throw new AnalysisEngineProcessException(new IllegalStateException(
				"Strict zoning cannot use multiple zone types"));
			} else {
				AtomicInteger begin = new AtomicInteger();
				AtomicInteger end = new AtomicInteger();
				CAS cas = jcas.getCas();
				for (AnnotationFS zone : select(cas, getType(cas, zones[0]))) {
					begin.set(zone.getBegin());
					end.set(zone.getEnd());
					limit(text, begin, end, true);
					process(jcas, text.substring(begin.get(), end.get()), begin.get());
				}
			}
		}
		else {
			// This set collects all zone boundaries.
			SortedSet<Integer> boundarySet = new TreeSet<Integer>();
			boundarySet.add(0); // Add start boundary
			boundarySet.add(text.length()); // Add end boundary

			// If zoneTypes have been define then get the boundaries, otherwise we will
			// simply have one big zone covering the whole document.
			if (zones != null) {
				// Iterate over all the zone indices and create sentences respecting
				// the zone boundaries. If the zoneTypes overlap... well... bad luck!
				for (String zoneName : zones) {
					AtomicInteger begin = new AtomicInteger();
					AtomicInteger end = new AtomicInteger();
					CAS cas = jcas.getCas();
					for (AnnotationFS zone : select(cas, getType(cas, zoneName))) {
						begin.set(zone.getBegin());
						end.set(zone.getEnd());
						limit(text, begin, end, false);
						boundarySet.add(begin.get());
						boundarySet.add(end.get());
					}
				}
			}

			// Now process all zoneTypes. There will be at least two entries in the
			// boundary set (see above).
			Iterator<Integer> bi = boundarySet.iterator();
			int begin = bi.next();
			while (bi.hasNext()) {
				int end = bi.next();
				process(jcas, text.substring(begin, end), begin);
				begin = end;
			}
		}
    }

    /**
     * Adjust the values in the two numeric arguments to be within the limits
     * of the specified text. If the limits have to be adjusted, a warning is
     * issued to the log. Illegal zone boundaries hint to a bug in the AE that
     * produced the zone annotations.
     *
     * @param text the text.
     * @param aBegin the zone begin.
     * @param aEnd the zone end.
     */
	protected void limit(String text, AtomicInteger aBegin, AtomicInteger aEnd,
			boolean aEndPlusOne)
    {
		// checking to avoid out-of-bounds
    	int maxEnd = text.length() + (aEndPlusOne ? 0 : -1);
		int begin = aBegin.get() < 0 ? 0 : aBegin.get();
		begin = begin > maxEnd ? maxEnd : begin;

		int end = aEnd.get() < 0 ? 0 : aEnd.get();
		end = end > maxEnd ? maxEnd : end;

		if (begin != aBegin.get() || end != aEnd.get()) {
			getLogger().warn("Adjusted " + "out-of-bounds zone [" + aBegin.get() + "-" + aEnd.get()
							+ "] to [" + begin + "-" + end + "]");
		}

		aBegin.set(begin);
		aEnd.set(end);
    }

	protected Annotation createSentence(final JCas aJCas, final int aBegin,
			final int aEnd)
	{
		int[] span = new int[] { aBegin, aEnd };
		trim(aJCas.getDocumentText(), span);
		if (!isEmpty(span[0], span[1]) && isCreateSentences()) {
			Sentence seg = new Sentence(aJCas, span[0], span[1]);
			seg.addToIndexes(aJCas);
			//sentenceCount++;
			tokenCount = 0;
			return seg;
		}
		else {
			return null;
		}
	}

	protected Annotation createToken(final JCas aJCas, final int aBegin,
			final int aEnd)
	{
		Annotation a = createToken(aJCas, aBegin, aEnd, tokenCount);
		if (a != null) {
			tokenCount++;
		}
		return a;
	}

	protected Annotation createToken(final JCas aJCas, final int aBegin,
			final int aEnd, final int aIndex)
	{
		int[] span = new int[] { aBegin, aEnd };
		trim(aJCas.getDocumentText(), span);
		if (!isEmpty(span[0], span[1]) && isCreateTokens()) {
			Annotation seg = new Token(aJCas, span[0], span[1]);
			seg.addToIndexes(aJCas);
			return seg;
		}
		else {
			return null;
		}
	}

	protected abstract void process(JCas aJCas, String text, int zoneBegin)
		throws AnalysisEngineProcessException;

	/**
	 * Remove trailing or leading whitespace from the annotation.
	 */
	public void trim(String aText, int[] aSpan)
	{
		int begin = aSpan[0];
		int end = aSpan[1]-1;

		String data = aText;
		while (
				(begin < (data.length()-1))
				&& trimChar(data.charAt(begin))
		) {
			begin ++;
		}
		while (
				(end > 0)
				&& trimChar(data.charAt(end))
		) {
			end --;
		}

		end++;

		aSpan[0] = begin;
		aSpan[1] = end;
	}

	public boolean isEmpty(int aBegin, int aEnd)
	{
		return aBegin >= aEnd;
	}

	public boolean trimChar(final char aChar)
	{
		switch (aChar) {
		case '\n':     return true; // Line break
		case '\r':     return true; // Carriage return
		case '\t':     return true; // Tab
		case '\u200E': return true; // LEFT-TO-RIGHT MARK
		case '\u200F': return true; // RIGHT-TO-LEFT MARK
		case '\u2028': return true; // LINE SEPARATOR
		case '\u2029': return true; // PARAGRAPH SEPARATOR
		default:
			return  Character.isWhitespace(aChar);
		}
	}

	public String getLanguage(JCas aJCas)
	{
        if (language != null) {
            return language;
        }
        else {
            return aJCas.getDocumentLanguage();
        }
	}
	
	/**
	 * Get the locale from the parameter, then from the document if available.
	 * If no locale is set get the default locale from the VM.
	 */
	public Locale getLocale(JCas aJCas)
	{
	    String lang = getLanguage(aJCas);
	    if (lang != null) {
	        return new Locale(lang);
	    }
	    else {
	        return Locale.getDefault();
	    }
	}
}
