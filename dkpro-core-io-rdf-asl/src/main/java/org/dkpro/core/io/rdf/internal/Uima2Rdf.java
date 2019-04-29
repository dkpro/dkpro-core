/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.rdf.internal;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.validation.CasAnalysisUtils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class Uima2Rdf
{
    private static Pattern DKPRO_CORE_SCHEME = Pattern
            .compile("(?<LONG>de\\.tudarmstadt\\.ukp\\.dkpro\\.core\\.api\\.(?<MODULE>[^.]+)\\.type(\\.(?<INMODULE>.*))?\\.)[^.]+");
    
    public static void convert(JCas aJCas, OntModel aTarget)
        throws CASException
    {
        // Set up prefix mappings
        TypeSystem ts = aJCas.getTypeSystem();
        aTarget.setNsPrefix("cas", RdfCas.NS_UIMA + "uima.cas.");
        aTarget.setNsPrefix("tcas", RdfCas.NS_UIMA + "uima.tcas.");
        aTarget.setNsPrefix(RdfCas.PREFIX_RDFCAS, RdfCas.NS_RDFCAS);
        
        // Additional prefix mappings for DKPro Core typesystems
        for (Type t : ts.getProperlySubsumedTypes(ts.getTopType())) {
            Matcher nameMatcher = DKPRO_CORE_SCHEME.matcher("");
            String typeName = t.getName();
            if (typeName.endsWith("[]")) {
                typeName = typeName.substring(0, typeName.length() - 2);
            }
            nameMatcher.reset(typeName);
            if (nameMatcher.matches()) {
                String prefix = nameMatcher.group("MODULE");
                if (nameMatcher.group("INMODULE") != null) {
                    prefix = prefix + "-" + nameMatcher.group("INMODULE");
                }
                aTarget.setNsPrefix(prefix, RdfCas.NS_UIMA + nameMatcher.group("LONG"));
            }
        }

        Iterator<JCas> viewIterator = aJCas.getViewIterator();
        while (viewIterator.hasNext()) {
            convertView(viewIterator.next(), aTarget);
        }
    }
    
    private static void convertView(JCas aJCas, OntModel aTarget)
    {
        // Shorten down variable name for model
        OntModel m = aTarget;

        // Set up names
        Resource tView = m.createResource(RdfCas.TYPE_VIEW);
        Resource tFeatureStructure = m.createResource(RdfCas.TYPE_FEATURE_STRUCTURE);
        Property pIndexedIn = m.createProperty(RdfCas.PROP_INDEXED_IN);

        // Get a URI for the document
        DocumentMetaData dmd = DocumentMetaData.get(aJCas);
        String docuri = dmd.getDocumentUri() != null ? dmd.getDocumentUri()
                : "urn:" + dmd.getDocumentId();

        // These only collect a single view... 
        Set<FeatureStructure> reachable = CasAnalysisUtils.collectReachable(aJCas.getCas());
        Set<FeatureStructure> indexed = CasAnalysisUtils.collectIndexed(aJCas.getCas());
        // ... they do not collect the SOFA, so we add that explicitly
        reachable.add(aJCas.getSofa());
        
        // Set up the view itself
        String viewUri = String.format("%s#%d", docuri,
                aJCas.getLowLevelCas().ll_getFSRef(aJCas.getSofa()));
        Individual rdfView = m.createIndividual(viewUri, tView);
        
        for (FeatureStructure uimaFS : reachable) {
            String uri = String.format("%s#%d", docuri, aJCas.getLowLevelCas().ll_getFSRef(uimaFS));
            Individual rdfFS = m.createIndividual(uri, m.createResource(rdfType(uimaFS.getType())));
            
            // The SoFa is not a regular FS - do not mark it as such
            if (uimaFS != aJCas.getSofa()) {
                rdfFS.addOntClass(tFeatureStructure);
            }
            
            // Internal UIMA information
            if (indexed.contains(uimaFS)) {
                rdfFS.addProperty(pIndexedIn, rdfView);
            }
            
            // Convert features
            convertFeatures(docuri, uimaFS, rdfFS);
        }
    }
    
    private static void convertFeatures(String docuri, FeatureStructure uimaFS, Individual rdfFS)
    {
        OntModel m = rdfFS.getOntModel();
        
        for (Feature uimaFeat : uimaFS.getType().getFeatures()) {
            Property rdfFeat = m.createProperty(rdfFeature(uimaFeat));
            if (uimaFeat.getRange().isPrimitive()) {
                switch (uimaFeat.getRange().getName()) {
                case CAS.TYPE_NAME_BOOLEAN:
                    rdfFS.addLiteral(rdfFeat, m.createTypedLiteral(
                            uimaFS.getBooleanValue(uimaFeat), XSDDatatype.XSDboolean));
                    break;
                case CAS.TYPE_NAME_BYTE:
                    rdfFS.addLiteral(rdfFeat, m.createTypedLiteral(
                            uimaFS.getByteValue(uimaFeat), XSDDatatype.XSDbyte));
                    break;
                case CAS.TYPE_NAME_DOUBLE:
                    rdfFS.addLiteral(rdfFeat, m.createTypedLiteral(
                            uimaFS.getDoubleValue(uimaFeat), XSDDatatype.XSDdouble));
                    break;
                case CAS.TYPE_NAME_FLOAT:
                    rdfFS.addLiteral(rdfFeat, m.createTypedLiteral(
                            uimaFS.getFloatValue(uimaFeat), XSDDatatype.XSDfloat));
                    break;
                case CAS.TYPE_NAME_INTEGER:
                    rdfFS.addLiteral(rdfFeat, m.createTypedLiteral(uimaFS.getIntValue(uimaFeat),
                            XSDDatatype.XSDint));
                    break;
                case CAS.TYPE_NAME_LONG:
                    rdfFS.addLiteral(rdfFeat, m.createTypedLiteral(
                            uimaFS.getLongValue(uimaFeat), XSDDatatype.XSDlong));
                    break;
                case CAS.TYPE_NAME_SHORT:
                    rdfFS.addLiteral(rdfFeat, m.createTypedLiteral(
                            uimaFS.getShortValue(uimaFeat), XSDDatatype.XSDshort));
                    break;
                case CAS.TYPE_NAME_STRING: {
                    String s = uimaFS.getStringValue(uimaFeat);
                    if (s != null) {
                        rdfFS.addLiteral(rdfFeat,
                                m.createTypedLiteral(s, XSDDatatype.XSDstring));
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Feature [" + uimaFeat.getName()
                            + "] has unsupported primitive type ["
                            + uimaFeat.getRange().getName() + "]");
                }
            }
            else {
                FeatureStructure targetUimaFS = uimaFS.getFeatureValue(uimaFeat);
                if (targetUimaFS != null) {
                    rdfFS.addProperty(rdfFeat, m.createResource(rdfUri(docuri, targetUimaFS)));
                }
            }
        }
    }
    
    private static String rdfUri(String docuri, FeatureStructure uimaFS)
    {
        return String.format("%s#%d", docuri, uimaFS.getCAS().getLowLevelCAS().ll_getFSRef(uimaFS));
    }
    
    private static String rdfFeature(Feature aUimaFeature)
    {
        return rdfType(aUimaFeature.getDomain()) + "-" + aUimaFeature.getShortName();
    }
    
    private static String rdfType(Type aUimaType)
    {
        return RdfCas.NS_UIMA + aUimaType.getName();
    }
}
