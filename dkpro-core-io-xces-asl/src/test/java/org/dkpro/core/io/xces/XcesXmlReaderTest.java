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
package org.dkpro.core.io.xces;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.xces.XcesBasicXmlReader;
import org.dkpro.core.io.xces.XcesXmlReader;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import junit.framework.Assert;

public class XcesXmlReaderTest
{
    
    @Test
    public void xcesOneWayBasicTest()
        throws Exception
    {
        testOneWay(
                createReaderDescription(XcesBasicXmlReader.class,
                        XcesBasicXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",                        
                        XcesBasicXmlReader.PARAM_LANGUAGE, "el"),
                "xces-basic.xml.dump", "xces-basic.xml");
    }

    @Test
    public void xcesOneWayTest()
        throws Exception
    {
        testOneWay(
                createReaderDescription(XcesXmlReader.class,
                        XcesXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",                        
                        XcesXmlReader.PARAM_LANGUAGE, "el"),
                "xces-complex.xml.dump", "xces-complex.xml");
    }

}
