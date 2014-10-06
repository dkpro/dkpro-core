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

import java.util.ArrayList;
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

        // FIXME: Implement proper sentence boundary detection
        AnnotationFS sentenceAnno = cas.createAnnotation(sentenceType, 0, text.length());
        cas.addFsToIndexes(sentenceAnno);

        // The 3rd party tokenization might do some normalization. In order to can trace these if
        // they occur we normalize the raw string and denormalize each token afterwards to enalbe a
        // mapping between normalized tokens to our CAS-unnormalized text
        List<String> normTokens = Twokenize.tokenizeRawTweetText(text);
        List<String> escapedTokens = new ArrayList<String>();

        for (String s : normTokens) {
            String escapeHtml = StringEscapeUtils.escapeHtml(s);
            escapedTokens.add(escapeHtml);
        }

        int offset = 0;
        for (String s : escapedTokens) {
            int start = text.indexOf(s, offset);
            int end = start + s.length();

            if (start == -1) {
                // This weirdo occurs in json data retrieved directly from Twitter's streaming
                // service at the end of some tweets. We have to manually override this case - there
                // might be more
                if (s.equals("&amp;")) {
                    start = text.indexOf("&", offset);
                    end = start + 1;
                }
                // Some chars seem not to be covered by the escaping of Arktweet
                if (s.contains("'")) {
                    s = s.replaceAll("'", "&#039;");
                    start = text.indexOf(s, offset);
                    end = start + s.length();

                }
            }

            createTokenAnnotation(cas, start, end);
            offset = end;
        }

        // createTokenAnnotation(cas, start, end);
        // System.out.println(start + " " + end + " " + text.substring(start, end));
        // searchOffset = end;

    }

    private void createTokenAnnotation(CAS cas, int start, int end)
    {
        AnnotationFS tokenAnno = cas.createAnnotation(tokenType, start, end);
        cas.addFsToIndexes(tokenAnno);

    }

}
