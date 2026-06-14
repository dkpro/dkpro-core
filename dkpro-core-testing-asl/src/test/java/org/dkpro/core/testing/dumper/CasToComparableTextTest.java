/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.dkpro.core.testing.dumper;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.shuffle;
import static org.apache.uima.cas.CAS.TYPE_NAME_ANNOTATION;
import static org.apache.uima.cas.CAS.TYPE_NAME_STRING;
import static org.dkpro.core.testing.dumper.CasToComparableText.OutputFormat.HTML;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.resource.metadata.FeatureDescription;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CasToComparableTextTest
{
    private static final String TYPE_NAME_AKOF = "akof";
    private static final String FEATURE_BASE_NAME_AKOF_INT = "akofInt";
    private static final String FEATURE_BASE_NAME_AKOF_FS = "akofFs";
    private static final String FEATURE_BASE_NAME_AKOF_FLOAT = "akofFloat";
    private static final String FEATURE_BASE_NAME_AKOF_DOUBLE = "akofDouble";
    private static final String FEATURE_BASE_NAME_AKOF_LONG = "akofLong";
    private static final String FEATURE_BASE_NAME_AKOF_SHORT = "akofShort";
    private static final String FEATURE_BASE_NAME_AKOF_BYTE = "akofByte";
    private static final String FEATURE_BASE_NAME_AKOF_BOOLEAN = "akofBoolean";
    private static final String FEATURE_BASE_NAME_AKOF_STRING = "akofStr";
    private static final String FEATURE_BASE_NAME_AKOF_INT_ARRAY = "akofAInt";
    private static final String FEATURE_BASE_NAME_AKOF_FS_ARRAY = "akofAFs";
    private static final String FEATURE_BASE_NAME_AKOF_FLOAT_ARRAY = "akofAFloat";
    private static final String FEATURE_BASE_NAME_AKOF_DOUBLE_ARRAY = "akofADouble";
    private static final String FEATURE_BASE_NAME_AKOF_LONG_ARRAY = "akofALong";
    private static final String FEATURE_BASE_NAME_AKOF_SHORT_ARRAY = "akofAShort";
    private static final String FEATURE_BASE_NAME_AKOF_BYTE_ARRAY = "akofAByte";
    private static final String FEATURE_BASE_NAME_AKOF_BOOLEAN_ARRAY = "akofABoolean";
    private static final String FEATURE_BASE_NAME_AKOF_STRING_ARRAY = "akofAStr";

    @Test
    void thatPrimitiveValuesWork() throws Exception
    {
        var tsd = UIMAFramework.getResourceSpecifierFactory().createTypeSystemDescription();

        var akofTD = tsd.addType(TYPE_NAME_AKOF, null, CAS.TYPE_NAME_TOP);

        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_INT, null, CAS.TYPE_NAME_INTEGER);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FLOAT, null, CAS.TYPE_NAME_FLOAT);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_DOUBLE, null, CAS.TYPE_NAME_DOUBLE);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_LONG, null, CAS.TYPE_NAME_LONG);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_SHORT, null, CAS.TYPE_NAME_SHORT);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_BYTE, null, CAS.TYPE_NAME_BYTE);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_BOOLEAN, null, CAS.TYPE_NAME_BOOLEAN);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_STRING, null, TYPE_NAME_STRING);

        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FS, null, CAS.TYPE_NAME_TOP);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FS_ARRAY, null, CAS.TYPE_NAME_FS_ARRAY);

        var cas = CasCreationUtils.createCas(tsd, null, null, null);

        var t = cas.getTypeSystem().getType(TYPE_NAME_AKOF);
        var fs = cas.createFS(t);

        fs.setIntValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_INT), 1);
        fs.setFloatValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_FLOAT), 10.2321321f);
        fs.setDoubleValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_DOUBLE), 10.2321321);
        fs.setLongValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_LONG), 10_000_000);
        fs.setShortValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_SHORT), (short) 31000);
        fs.setByteValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_BYTE), (byte) 64);
        fs.setBooleanValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_BOOLEAN), true);
        fs.setStringValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_STRING), "dummy");

        cas.addFsToIndexes(fs);

        var result = CasToComparableText.toComparableString(cas);

        assertThat(result).contains("akof*,true,64,10.2321321,10.232132,1,10000000,31000,dummy");
    }

    @Test
    void thatPrimitiveWork() throws Exception
    {
        var tsd = UIMAFramework.getResourceSpecifierFactory().createTypeSystemDescription();

        var akofTD = tsd.addType(TYPE_NAME_AKOF, null, CAS.TYPE_NAME_TOP);

        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_INT, null, CAS.TYPE_NAME_INTEGER);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FS, null, CAS.TYPE_NAME_TOP);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FLOAT, null, CAS.TYPE_NAME_FLOAT);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_DOUBLE, null, CAS.TYPE_NAME_DOUBLE);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_LONG, null, CAS.TYPE_NAME_LONG);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_SHORT, null, CAS.TYPE_NAME_SHORT);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_BYTE, null, CAS.TYPE_NAME_BYTE);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_BOOLEAN, null, CAS.TYPE_NAME_BOOLEAN);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_STRING, null, CAS.TYPE_NAME_STRING);
        // akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FS_ARRAY, null, CAS.TYPE_NAME_FS_ARRAY);

        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_INT_ARRAY, null, CAS.TYPE_NAME_INTEGER_ARRAY);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FLOAT_ARRAY, null, CAS.TYPE_NAME_FLOAT_ARRAY);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_DOUBLE_ARRAY, null, CAS.TYPE_NAME_DOUBLE_ARRAY);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_LONG_ARRAY, null, CAS.TYPE_NAME_LONG_ARRAY);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_SHORT_ARRAY, null, CAS.TYPE_NAME_SHORT_ARRAY);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_BYTE_ARRAY, null, CAS.TYPE_NAME_BYTE_ARRAY);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_BOOLEAN_ARRAY, null, CAS.TYPE_NAME_BOOLEAN_ARRAY);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_STRING_ARRAY, null, CAS.TYPE_NAME_STRING_ARRAY);

        var cas = CasCreationUtils.createCas(tsd, null, null, null);

        var t = cas.getTypeSystem().getType(TYPE_NAME_AKOF);
        FeatureStructure fs = cas.createFS(t);

        var intArrayFs = cas.createIntArrayFS(1);
        intArrayFs.set(0, 1);
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_INT_ARRAY), intArrayFs);

        var floatArrayFs = cas.createFloatArrayFS(1);
        floatArrayFs.set(0, 10.2321321f);
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_FLOAT_ARRAY),
                floatArrayFs);

        var doubleArrayFs = cas.createDoubleArrayFS(1);
        doubleArrayFs.set(0, 10.2321321);
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_DOUBLE_ARRAY),
                doubleArrayFs);

        var longArrayFs = cas.createLongArrayFS(1);
        longArrayFs.set(0, 10_000_000);
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_LONG_ARRAY), longArrayFs);

        var shortArrayFs = cas.createShortArrayFS(1);
        shortArrayFs.set(0, (short) 31000);
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_SHORT_ARRAY),
                shortArrayFs);

        var byteArrayFs = cas.createByteArrayFS(1);
        byteArrayFs.set(0, (byte) 64);
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_BYTE_ARRAY), byteArrayFs);

        var booleanArrayFs = cas.createBooleanArrayFS(1);
        booleanArrayFs.set(0, true);
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_BOOLEAN_ARRAY),
                booleanArrayFs);

        var stringArrayFs = cas.createStringArrayFS(1);
        stringArrayFs.set(0, "dummy");
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_STRING_ARRAY),
                stringArrayFs);

        cas.addFsToIndexes(fs);

        var result = CasToComparableText.toComparableString(cas);

        assertThat(result).contains(
                "akof*,[true],[64],[10.2321321],[10.232132],[1],[10000000],[31000],[dummy]");
    }

    @Test
    void thatReferenceValuesAndArraysWork() throws Exception
    {
        var tsd = UIMAFramework.getResourceSpecifierFactory().createTypeSystemDescription();

        var akofTD = tsd.addType(TYPE_NAME_AKOF, null, CAS.TYPE_NAME_TOP);

        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FS, null, CAS.TYPE_NAME_TOP);
        akofTD.addFeature(FEATURE_BASE_NAME_AKOF_FS_ARRAY, null, CAS.TYPE_NAME_FS_ARRAY);

        var cas = CasCreationUtils.createCas(tsd, null, null, null);

        var t = cas.getTypeSystem().getType(TYPE_NAME_AKOF);
        var fs = cas.createFS(t);

        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_FS), fs);

        var arrayFs = cas.createArrayFS(1);
        arrayFs.set(0, fs);
        fs.setFeatureValue(t.getFeatureByBaseName(FEATURE_BASE_NAME_AKOF_FS_ARRAY), arrayFs);

        cas.addFsToIndexes(fs);

        var result = CasToComparableText.toComparableString(cas);

        assertThat(result).contains("akof*,[akof*],akof*");
    }

    @Nested
    class TokenBasedTests
    {

        private TypeSystemDescription tsd;
        private TypeDescription t1TypeDesc;
        private TypeDescription t2TypeDesc;
        private FeatureDescription t2f1Desc;
        private FeatureDescription t2f2Desc;
        private FeatureDescription t2f3Desc;
        private FeatureDescription t2f4Desc;
        private FeatureDescription t1f1Desc;
        private CAS cas;
        private Type t1;
        private Feature t1f1;
        private Type t2;
        private Feature t2f1;
        private Feature t2f2;
        private Feature t2f3;
        private Feature t2f4;

        @BeforeEach
        void setup() throws Exception
        {
            tsd = UIMAFramework.getResourceSpecifierFactory().createTypeSystemDescription();

            t1TypeDesc = tsd.addType("T1", null, TYPE_NAME_ANNOTATION);
            t1f1Desc = t1TypeDesc.addFeature("label", null, TYPE_NAME_STRING);

            t2TypeDesc = tsd.addType("T2", null, TYPE_NAME_ANNOTATION);
            t2f1Desc = t2TypeDesc.addFeature("f1", null, TYPE_NAME_STRING);
            t2f2Desc = t2TypeDesc.addFeature("f2", null, TYPE_NAME_STRING);
            t2f3Desc = t2TypeDesc.addFeature("f3", null, TYPE_NAME_STRING);
            t2f4Desc = t2TypeDesc.addFeature("f4", null, t1TypeDesc.getName());

            cas = CasCreationUtils.createCas(tsd, null, null, null);

            t2 = cas.getTypeSystem().getType(t2TypeDesc.getName());
            t2f1 = t2.getFeatureByBaseName(t2f1Desc.getName());
            t2f2 = t2.getFeatureByBaseName(t2f2Desc.getName());
            t2f3 = t2.getFeatureByBaseName(t2f3Desc.getName());
            t2f4 = t2.getFeatureByBaseName(t2f4Desc.getName());

            t1 = cas.getTypeSystem().getType(t1TypeDesc.getName());
            t1f1 = t1.getFeatureByBaseName(t1f1Desc.getName());
        }

        @Test
        void thatColumnsAndHeadersMatch() throws Exception
        {

            cas.setDocumentText("document");

            var token = cas.createAnnotation(t2, 0, 8);
            token.setFeatureValueFromString(t2f1, "f1");
            token.setFeatureValueFromString(t2f2, "f2");
            token.setFeatureValueFromString(t2f3, "f3");
            cas.addFsToIndexes(token);

            var cas2Text = new CasToComparableText(cas);
            var result = cas2Text.toString(token);

            var lines = result.split("\n");
            var headers = lines[1].trim().split(",");
            var values = lines[2].trim().split(",");

            assertThat(headers[0]).isEqualTo("<ANCHOR>");
            assertThat(values[0]).isEqualTo("T2[0-8]*@_InitialView");

            assertThat(headers[1]).isEqualTo("<COVERED_TEXT>");
            assertThat(values[1]).isEqualTo(cas.getDocumentText());

            var componentIdFeatureIndex = asList(headers).indexOf("f1");
            assertThat(headers[componentIdFeatureIndex]).isEqualTo(values[componentIdFeatureIndex]);

            var normalizedFeatureIndex = asList(headers).indexOf("f2");
            assertThat(headers[normalizedFeatureIndex]).isEqualTo(values[normalizedFeatureIndex]);

            var tokenClassFeatureIndex = asList(headers).indexOf("f3");
            assertThat(headers[tokenClassFeatureIndex]).isEqualTo(values[tokenClassFeatureIndex]);
        }

        @Test
        void thatColumnsAndHeadersMatchWithExclusions() throws Exception
        {

            cas.setDocumentText("document");

            var ann = cas.createAnnotation(t2, 0, 8);
            ann.setFeatureValueFromString(t2f1, "f1");
            ann.setFeatureValueFromString(t2f2, "f2");
            cas.addFsToIndexes(ann);

            var cas2Text = new CasToComparableText(cas);
            cas2Text.addExcludeFeaturePatterns(".*:" + t2f1.getShortName());
            var result = cas2Text.toString(ann);

            var lines = result.split("\n");
            var headers = lines[1].trim().split(",");
            var values = lines[2].trim().split(",");

            var componentIdFeatureIndex = asList(headers).indexOf("f1");
            assertThat(componentIdFeatureIndex).isEqualTo(-1);

            var tokenClassFeatureIndex = asList(headers).indexOf("f2");
            assertThat(headers[tokenClassFeatureIndex]).isEqualTo(values[tokenClassFeatureIndex]);
        }

        @Test
        void thatHtmlOutputWorks() throws Exception
        {

            cas.setDocumentText("document");

            var ann = cas.createAnnotation(t2, 0, 8);
            ann.setFeatureValueFromString(t2f1, "f1");
            ann.setFeatureValueFromString(t2f2, "f2");
            cas.addFsToIndexes(ann);

            var cas2Text = new CasToComparableText(cas, HTML);
            cas2Text.setOmitXmlDeclaration(false);
            var result = cas2Text.toString(ann);

            assertThat(result.trim().replace("\r\n", "\n")) //
                    .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<html>") //
                    .endsWith("</html>");
        }

        @Test
        void thatHtmlOutputWorksWithoutXmlDeclaration() throws Exception
        {

            cas.setDocumentText("document");

            var ann = cas.createAnnotation(t2, 0, 8);
            ann.setFeatureValueFromString(t2f1, "f1");
            ann.setFeatureValueFromString(t2f2, "f2");
            cas.addFsToIndexes(ann);

            var cas2Text = new CasToComparableText(cas, HTML);
            cas2Text.setOmitXmlDeclaration(true);
            var result = cas2Text.toString(ann);

            assertThat(result.trim().replace("\r\n", "\n")) //
                    .startsWith("<html>") //
                    .endsWith("</html>");
        }

        @Test
        void thatOrderOfIndexedAndNonIndexedFeatureStructuresIsStable() throws Exception
        {

            cas.setDocumentText("document");

            var indexFlags = new boolean[] { //
                    false, false, false, false, false, true, true, true, true, true //
            };
            shuffle(indexFlags);

            for (var indexFlag : indexFlags) {
                var t1Ann = cas.createAnnotation(t1, 0, 8);
                t1Ann.setFeatureValueFromString(t1f1, "N");
                if (indexFlag) {
                    cas.addFsToIndexes(t1Ann);
                }

                var t2Ann = cas.createAnnotation(t2, 0, 8);
                t2Ann.setFeatureValue(t2f4, t1Ann);
                cas.addFsToIndexes(t2Ann);
            }

            var cas2Text = new CasToComparableText(cas);
            cas2Text.setAnchorFeatureHash(true);
            cas2Text.setUniqueAnchors(false);
            assertThat(cas2Text.toString().lines().filter(s -> s.startsWith("T1[0-8]"))) //
                    .extracting(s -> s.charAt(7)) //
                    .containsExactly('*', '*', '*', '*', '*', '@', '@', '@', '@', '@');
        }
    }
}
