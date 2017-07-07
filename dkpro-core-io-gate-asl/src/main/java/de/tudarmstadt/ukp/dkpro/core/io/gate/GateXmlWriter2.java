/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.io.gate;

import static org.apache.uima.fit.util.JCasUtil.selectAll;

import java.io.OutputStream;
import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.ByteArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CommonArrayFS;
import org.apache.uima.cas.DoubleArrayFS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.FloatArrayFS;
import org.apache.uima.cas.IntArrayFS;
import org.apache.uima.cas.LongArrayFS;
import org.apache.uima.cas.ShortArrayFS;
import org.apache.uima.cas.StringArrayFS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.FSUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentExporter;
import gate.FeatureMap;
import gate.corpora.DocumentContentImpl;
import gate.corpora.DocumentImpl;
import gate.corpora.export.GateXMLExporter;
import gate.util.InvalidOffsetException;
import gate.util.SimpleFeatureMapImpl;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

@ResourceMetaData(name="GATE XML Writer (generic)")
@MimeTypeCapability({MimeTypes.APPLICATION_X_GATE_XML})
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class GateXmlWriter2
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.xml</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".xml")
    private String filenameSuffix;

    private DocumentExporter exporter;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        exporter = new GateXMLExporter();
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        Document document = new DocumentImpl();
        document.setContent(new DocumentContentImpl(aJCas.getDocumentText()));

        AnnotationSet as = document.getAnnotations();

        Int2IntMap processed = new Int2IntOpenHashMap();

        Iterator<TOP> fses = selectAll(aJCas).iterator();
        while (fses.hasNext()) {
            TOP fs = fses.next();
            try {
                process(processed, as, fs);
            }
            catch (InvalidOffsetException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            exporter.export(document, docOS);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private Annotation process(Int2IntMap aProcessed, AnnotationSet aAs, FeatureStructure aFS)
        throws InvalidOffsetException
    {
        if (aFS.getCAS().getSofa() == aFS) {
            return null;
        }
        
        int fsAddr = aFS.getCAS().getLowLevelCAS().ll_getFSRef(aFS);
        if (aProcessed.containsKey(fsAddr)) {
            return aAs.get(aProcessed.get(fsAddr));
        }

        FeatureMap fm = new SimpleFeatureMapImpl();
        for (Feature aFeature : aFS.getType().getFeatures()) {
            System.out.printf("Processing %s%n", aFeature.getName());
            
            if (
                    CAS.FEATURE_FULL_NAME_SOFA.equals(aFeature.getName()) ||
                    CAS.FEATURE_FULL_NAME_BEGIN.equals(aFeature.getName()) ||
                    CAS.FEATURE_FULL_NAME_END.equals(aFeature.getName())
            ) {
                continue;
            }

            // Here we store the values before we coerce them into the final target type
            // "target" is actually an array
            Object target;
            int length;

            if (aFeature.getRange().isPrimitive()) {
                switch (aFeature.getRange().getName()) {
                case CAS.TYPE_NAME_BOOLEAN:
                    fm.put(aFeature.getShortName(), aFS.getBooleanValue(aFeature));
                    break;
                case CAS.TYPE_NAME_BYTE:
                    fm.put(aFeature.getShortName(), aFS.getByteValue(aFeature));
                    break;
                case CAS.TYPE_NAME_DOUBLE:
                    fm.put(aFeature.getShortName(), aFS.getDoubleValue(aFeature));
                    break;
                case CAS.TYPE_NAME_FLOAT:
                    fm.put(aFeature.getShortName(), aFS.getFloatValue(aFeature));
                    break;
                case CAS.TYPE_NAME_INTEGER:
                    fm.put(aFeature.getShortName(), aFS.getIntValue(aFeature));
                    break;
                case CAS.TYPE_NAME_LONG:
                    fm.put(aFeature.getShortName(), aFS.getLongValue(aFeature));
                    break;
                case CAS.TYPE_NAME_SHORT:
                    fm.put(aFeature.getShortName(), aFS.getShortValue(aFeature));
                    break;
                case CAS.TYPE_NAME_STRING:
                    fm.put(aFeature.getShortName(), aFS.getStringValue(aFeature));
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unable to convert value of feature [" + aFeature.getName()
                                    + "] with type [" + aFeature.getRange().getName() + "]");
                }
            }
            // "null" case
            else if (aFS.getFeatureValue(aFeature) == null) {
                fm.put(aFeature.getShortName(), null);
            }
            // Handle case where feature is an array
            else if (aFeature.getRange().isArray()) {
                CommonArrayFS source = (CommonArrayFS) aFS.getFeatureValue(aFeature);
                length = source.size();
                switch (aFeature.getRange().getComponentType().getName()) {
                case CAS.TYPE_NAME_BOOLEAN:
                    target = new boolean[length];
                    ((BooleanArrayFS) source).copyToArray(0, (boolean[]) target, 0, length);
                    break;
                case CAS.TYPE_NAME_BYTE:
                    target = new byte[length];
                    ((ByteArrayFS) source).copyToArray(0, (byte[]) target, 0, length);
                    break;
                case CAS.TYPE_NAME_DOUBLE:
                    target = new double[length];
                    ((DoubleArrayFS) source).copyToArray(0, (double[]) target, 0, length);
                    break;
                case CAS.TYPE_NAME_FLOAT:
                    target = new float[length];
                    ((FloatArrayFS) source).copyToArray(0, (float[]) target, 0, length);
                    break;
                case CAS.TYPE_NAME_INTEGER:
                    target = new int[length];
                    ((IntArrayFS) source).copyToArray(0, (int[]) target, 0, length);
                    break;
                case CAS.TYPE_NAME_LONG:
                    target = new long[length];
                    ((LongArrayFS) source).copyToArray(0, (long[]) target, 0, length);
                    break;
                case CAS.TYPE_NAME_SHORT:
                    target = new short[length];
                    ((ShortArrayFS) source).copyToArray(0, (short[]) target, 0, length);
                    break;
                case CAS.TYPE_NAME_STRING:
                    target = new String[length];
                    ((StringArrayFS) source).copyToArray(0, (String[]) target, 0, length);
                    break;
                default:
                    throw new UnsupportedOperationException("Cannot convert FS arrays yet");
                }
            }
            // Handle case where feature is a list
            else if (FSUtil.isMultiValuedFeature(aFS, aFeature)) {
                // Get length of list
                length = 0;
                {
                    FeatureStructure cur = aFS.getFeatureValue(aFeature);
                    // We assume to by facing a non-empty element if it has a "head" feature
                    while (cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD) != null) {
                        length++;
                        cur = cur.getFeatureValue(
                                cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL));
                    }
                }

                switch (aFeature.getRange().getName()) {
                case CAS.TYPE_NAME_FLOAT_LIST: {
                    float[] floatTarget = new float[length];
                    int i = 0;
                    FeatureStructure cur = aFS.getFeatureValue(aFeature);
                    // We assume to by facing a non-empty element if it has a "head" feature
                    while (cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD) != null) {
                        floatTarget[i] = cur.getFloatValue(
                                cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD));
                        cur = cur.getFeatureValue(
                                cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL));
                    }
                    target = floatTarget;
                    break;
                }
                case CAS.TYPE_NAME_INTEGER_LIST: {
                    int[] intTarget = new int[length];
                    int i = 0;
                    FeatureStructure cur = aFS.getFeatureValue(aFeature);
                    // We assume to by facing a non-empty element if it has a "head" feature
                    while (cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD) != null) {
                        intTarget[i] = cur.getIntValue(
                                cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD));
                        cur = cur.getFeatureValue(
                                cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL));
                    }
                    target = intTarget;
                    break;
                }
                case CAS.TYPE_NAME_STRING_LIST: {
                    String[] stringTarget = new String[length];
                    int i = 0;
                    FeatureStructure cur = aFS.getFeatureValue(aFeature);
                    // We assume to by facing a non-empty element if it has a "head" feature
                    while (cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD) != null) {
                        stringTarget[i] = cur.getStringValue(
                                cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_HEAD));
                        cur = cur.getFeatureValue(
                                cur.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_TAIL));
                    }
                    target = stringTarget;
                    break;
                }
                default: {
                    throw new UnsupportedOperationException("Cannot convert FS lists yet");
                }
                }
                
                fm.put(aFeature.getShortName(), target);
            }
            else if (aFS.getCAS().getTypeSystem().subsumes(CasUtil.getType(aFS.getCAS(), TOP.class),
                    aFeature.getRange())) {
                fm.put(aFeature.getShortName(), process(aProcessed, aAs, aFS.getFeatureValue(aFeature)));
            }
            else {
                throw new IllegalArgumentException("Unable to convert value of feature ["
                        + aFeature.getName() + "] with type [" + aFeature.getRange().getName()
                        + "]]");
            }
        }
        
        Long begin = null;
        Long end = null;
        if (aFS instanceof AnnotationFS) {
            begin = Long.valueOf(((AnnotationFS) aFS).getBegin());
            end = Long.valueOf(((AnnotationFS) aFS).getEnd());
        }
        
        Integer aid = aAs.add(begin, end, aFS.getType().getName(), fm);
        aProcessed.put((int) fsAddr, (int) aid);
        return aAs.get(aid);
    }
}
