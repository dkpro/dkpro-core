package de.tudarmstadt.ukp.dkpro.core.arktools;

/**
 * Copyright 2007-2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;

import cmu.arktweetnlp.Twokenize;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ArktweetTokenizer
    extends CasAnnotator_ImplBase
{

    private Type tokenType;
    private Type sentenceType;

    @Override
    public void typeSystemInit(TypeSystem aTypeSystem)
        throws AnalysisEngineProcessException
    {
        super.typeSystemInit(aTypeSystem);

        tokenType = aTypeSystem.getType(Token.class.getName());
        sentenceType = aTypeSystem.getType(Sentence.class.getName());
    }

    @Override
    public void process(CAS cas)
        throws AnalysisEngineProcessException
    {
        String text = cas.getDocumentText();
        String normalizedText = normalizeText(text);

        // FIXME: Implement proper sentence boundary detection
        AnnotationFS sentenceAnno = cas.createAnnotation(sentenceType, 0, normalizedText.length());
        cas.addFsToIndexes(sentenceAnno);

        List<String> tokens = Twokenize.tokenizeRawTweetText(normalizedText);

        int start = 0;
        int end = 0;
        int searchOffset = 0;
        int totalSubstitutionLength = 0;
        for (String token : tokens) {

            int tokenOffset = -1;
            if (token.equals("&")) {
                // HTML escaped '&' starts unfortunately with a '&'
                // so we do nothing - we would find the normalized token in the
                // unnormalized text and erroneously assume the token remained untouched
                // There is a situation we have to undo 'this' ommittance (see comments below)
            }
            else {
                tokenOffset = text.indexOf(token, searchOffset);
            }

            int startPos = 0;
            int endPos = 0;
            int substitutionLength = 0;
            if (tokenOffset == -1) {
                startPos = normalizedText.indexOf(token, searchOffset) + totalSubstitutionLength;

                // In case of omitted spaces a htlm escaped sequence might occur in the middle of
                // what was recognized as token e.g. 'such a smart&quot;move&quot;my friend'
                // We thus compare if start position of token equals start position of the '&'
                // character and use the latter for determining the length of the substituted
                // sequence.
                int startPosAND = text.indexOf("&", startPos);
                endPos = text.indexOf(";", startPos);
                if (endPos == -1) {
                    // If for any reason no closing ';' is found some truncation seemed to have
                    // occurred and we try to find the token in the unnormalized text (as this case
                    // might have been skipped earlier)
                    tokenOffset = text.indexOf(token, searchOffset);
                }
                else {
                    substitutionLength = endPos
                            - ((startPos == startPosAND) ? startPos : startPosAND);
                    tokenOffset = startPos;
                }
            }

            start = tokenOffset;
            end = tokenOffset + token.length() + substitutionLength;
            totalSubstitutionLength += substitutionLength;
            createTokenAnnotation(cas, start, end);
            // System.out.println(start + " " + end + " " + text.substring(start, end));
            searchOffset = end - totalSubstitutionLength;
        }

    }

    private void createTokenAnnotation(CAS cas, int start, int end)
    {
        AnnotationFS tokenAnno = cas.createAnnotation(tokenType, start, end);
        cas.addFsToIndexes(tokenAnno);

    }

    /**
     * Twitter text comes HTML-escaped, so unescape it. We also first unescape &amp;'s, in case the
     * text has been buggily double-escaped.
     */
    public static String normalizeText(String text)
    {
        text = text.replaceAll("&amp;", "&");
        text = StringEscapeUtils.unescapeHtml(text);
        return text;
    }

}
