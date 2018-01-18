/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.api.parameter;

public final class MimeTypes
{
    // Standard application types (http://www.iana.org/assignments/media-types/media-types.xhtml)
    public final static String APPLICATION_XML = "application/xml";
    public final static String APPLICATION_XHTML = "application/xhtml+xml";
    public final static String APPLICATION_PDF = "application/pdf";
    public final static String APPLICATION_RTF = "application/rtf";
    public final static String APPLICATION_TEI_XML = "application/tei+xml";
    public final static String APPLICATION_VND_XMI_XML = "application/vnd.xmi+xml";
    
    
    // DKPro application types
    public final static String APPLICATION_X_ANCORA_XML = "application/x.org.dkpro.ancora+xml";
    public final static String APPLICATION_X_DITOP = "application/x.org.dkpro.ditop";
    public final static String APPLICATION_X_FANGORN = "application/x.org.dkpro.fangorn";
    public final static String APPLICATION_X_GATE_XML = "application/x.org.dkpro.gate+xml";
    public final static String APPLICATION_X_GRAF_XML = "application/x.org.dkpro.graf+xml";
    public final static String APPLICATION_X_UIMA_JSON = "application/x.org.dkpro.uima+json";
    public final static String APPLICATION_X_UIMA_XMI = "application/x.org.dkpro.uima+xmi";
    public final static String APPLICATION_X_UIMA_BINARY = "application/x.org.dkpro.uima+binary";
    public final static String APPLICATION_X_LIF_JSON = "application/x.org.dkpro.lif+json";
    public final static String APPLICATION_X_LXF_JSON = "application/x.org.dkpro.lxf+json";
    public final static String APPLICATION_X_NEGRA3 = "application/x.org.dkpro.negra3";
    public final static String APPLICATION_X_NEGRA4 = "application/x.org.dkpro.negra4";
    public final static String APPLICATION_X_NIF_TURTLE = "application/x.org.dkpro.nif+turtle";
    public final static String APPLICATION_X_UIMA_RDF = "application/x.org.dkpro.uima+rdf";
    public final static String APPLICATION_X_REUTERS21578_SGML = "application/x.org.dkpro.reuters21578+sgml";
    public final static String APPLICATION_X_TGREP2 = "application/x.org.dkpro.tgrep2";
    public final static String APPLICATION_X_TIGER_XML = "application/x.org.dkpro.tiger+xml";
    public final static String APPLICATION_X_SEMEVAL_2010_XML = "application/x.org.dkpro.semeval-2010+xml";
    public final static String APPLICATION_X_TUEPP_XML = "application/x.org.dkpro.tuepp+xml";
    public final static String APPLICATION_X_TUEBADZ_CHUNK = "application/x.org.dkpro.tuebadz-chunk";
    
    // Standard text types (http://www.iana.org/assignments/media-types/media-types.xhtml)
    public final static String TEXT_CSV = "text/csv";
    public final static String TEXT_HTML = "text/html";
    public final static String TEXT_PLAIN = "text/plain";
    public final static String TEXT_RTF = "text/rtf";
    public final static String TEXT_XML = "text/xml";
    public final static String TEXT_TAB_SEPARATED_VALUES = "text/tab-separated-values";

    // Non-standard text types
    public final static String TEXT_TCF = "text/tcf+xml";
    
    // DKPro text types
    public final static String TEXT_X_CONLL_2000 = "text/x.org.dkpro.conll-2000";
    public final static String TEXT_X_CONLL_2002 = "text/x.org.dkpro.conll-2002";
    public final static String TEXT_X_CONLL_2003 = "text/x.org.dkpro.conll-2003";
    public final static String TEXT_X_CONLL_2006 = "text/x.org.dkpro.conll-2006";
    public final static String TEXT_X_CONLL_2008 = "text/x.org.dkpro.conll-2008";
    public final static String TEXT_X_CONLL_2009 = "text/x.org.dkpro.conll-2009";
    public final static String TEXT_X_CONLL_2012 = "text/x.org.dkpro.conll-2012";
    public final static String TEXT_X_CONLL_U = "text/x.org.dkpro.conll-u";
    public final static String TEXT_X_IMSCWB = "text/x.org.dkpro.imscwb";
    public final static String TEXT_X_GERMEVAL_2014 = "text/x.org.dkpro.germeval-2014";
    public final static String TEXT_X_LCC = "text/x.org.dkpro.lcc";
    public final static String TEXT_X_NGRAM = "text/x.org.dkpro.ngram";
    public final static String TEXT_X_PTB_CHUNKED = "text/x.org.dkpro.ptb-chunked";
    public final static String TEXT_X_PTB_COMBINED = "text/x.org.dkpro.ptb-combined";
    public final static String TEXT_X_REUTERS21578 = "text/x.org.dkpro.reuters21578";

    // OpenNLP model types
    public final static String APPLICATION_X_OPENNLP_CHUNK = "application/x.org.dkpro.core.opennlp.chunk";
    public final static String APPLICATION_X_OPENNLP_LEMMA = "application/x.org.dkpro.core.opennlp.lemma";
    public final static String APPLICATION_X_OPENNLP_NER = "application/x.org.dkpro.core.opennlp.ner";
    public final static String APPLICATION_X_OPENNLP_PARSER = "application/x.org.dkpro.core.opennlp.parser";
    public final static String APPLICATION_X_OPENNLP_TAGGER = "application/x.org.dkpro.core.opennlp.tagger";
    public final static String APPLICATION_X_OPENNLP_SENT = "application/x.org.dkpro.core.opennlp.sent";
    public final static String APPLICATION_X_OPENNLP_TOKEN = "application/x.org.dkpro.core.opennlp.token";

    // StanfordNLP model types
    public final static String APPLICATION_X_STANFORDNLP_NER = "application/x.org.dkpro.core.stanfordnlp.ner";
    public final static String APPLICATION_X_STANFORDNLP_TAGGER = "application/x.org.dkpro.core.stanfordnlp.tagger";

    // LingPipe model types
    public final static String APPLICATION_X_LINGPIPE_NER = "application/x.org.dkpro.core.lingpipe.ner";

    private MimeTypes()
    {
        // No instances
    }
}
