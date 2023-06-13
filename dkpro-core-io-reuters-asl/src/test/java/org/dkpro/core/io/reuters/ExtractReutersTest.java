/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.reuters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ExtractReutersTest
{
    private static final String REUTERS_DIR = "src/test/resources/reuters-sgml";

    @Test
    public void testExtract()
            throws Exception
    {
        int expectedDocs = 1000;
        String expectedTitleFirst = "BAHIA COCOA REVIEW";
        Date expectedDateFirst = new GregorianCalendar(1987, 1, 26, 15, 1, 1).getTime();
        String expectedBodyFirst = "Showers";
        String expectedTopicFirst = "cocoa";
        ReutersDocument.LEWISSPLIT expectedlLewissplitFirst = ReutersDocument.LEWISSPLIT.TRAIN;
        ReutersDocument.CGISPLIT expectedCgisplitFirst = ReutersDocument.CGISPLIT.TRAINING_SET;
        int oldIdFirst = 5544;
        int newIdFirst = 1;

        String expectedTitle4 = "NATIONAL AVERAGE PRICES FOR FARMER-OWNED RESERVE";
        Date expectedDate4 = new GregorianCalendar(1987, 1, 26, 15, 10, 44).getTime();
        String expectedBody4 = "The U.S. Agriculture Department";
        Set<String> expectedTopic4 = new HashSet<>(
                Arrays.asList(
                        new String[] { "grain", "wheat", "corn", "barley", "oat", "sorghum" }));
        ReutersDocument.LEWISSPLIT expectedlLewissplit4 = ReutersDocument.LEWISSPLIT.TRAIN;
        ReutersDocument.CGISPLIT expectedCgisplit4 = ReutersDocument.CGISPLIT.TRAINING_SET;
        int oldId4 = 5548;
        int newId4 = 5;

        String expectedTitleLast = "NATIONAL AMUSEMENTS AGAIN UPS VIACOM <VIA> BID";
        Date expectedDateLast = new GregorianCalendar(1987, 2, 3, 9, 17, 32).getTime();
        String expectedBodyLast = "Viacom International Inc said ";
        String expectedTopicLast = "acq";
        ReutersDocument.LEWISSPLIT expectedlLewissplitLast = ReutersDocument.LEWISSPLIT.TRAIN;
        ReutersDocument.CGISPLIT expectedCgisplitLast = ReutersDocument.CGISPLIT.TRAINING_SET;
        int oldIdLast = 16320;
        int newIdLast = 1000;

        List<ReutersDocument> docs = ExtractReuters.extract(new File(REUTERS_DIR).toPath());
        assertEquals(expectedDocs, docs.size());

        /* assert first doc */
        ReutersDocument doc0 = docs.get(0);
        assertEquals(expectedTitleFirst, doc0.getTitle());
        assertEquals(expectedDateFirst.toString(), doc0.getDate().toString());
        assertTrue(doc0.getTopics().contains(expectedTopicFirst));
        assertTrue(doc0.getBody().startsWith(expectedBodyFirst));
        assertEquals(expectedlLewissplitFirst, doc0.getLewissplit());
        assertEquals(expectedCgisplitFirst, doc0.getCgisplit());
        assertEquals(oldIdFirst, doc0.getOldid());
        assertEquals(newIdFirst, doc0.getNewid());

        ReutersDocument doc4 = docs.get(4);
        assertEquals(expectedTitle4, doc4.getTitle());
        assertEquals(expectedDate4.toString(), doc4.getDate().toString());
        assertEquals(expectedTopic4, doc4.getTopics());
        assertTrue(doc0.getBody().startsWith(expectedBodyFirst));
        assertEquals(expectedlLewissplit4, doc4.getLewissplit());
        assertEquals(expectedCgisplit4, doc4.getCgisplit());
        assertEquals(oldId4, doc4.getOldid());
        assertEquals(newId4, doc4.getNewid());

        /* assert last doc */
        ReutersDocument doc999 = docs.get(999);
        assertEquals(expectedTitleLast, doc999.getTitle());
        assertEquals(expectedDateLast.toString(), doc999.getDate().toString());
        assertTrue(doc999.getTopics().contains(expectedTopicLast));
        assertTrue(doc999.getBody().startsWith(expectedBodyLast));
        assertEquals(expectedlLewissplitLast, doc999.getLewissplit());
        assertEquals(expectedCgisplitLast, doc999.getCgisplit());
        assertEquals(oldIdLast, doc999.getOldid());
        assertEquals(newIdLast, doc999.getNewid());
    }
}
