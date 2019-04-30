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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.dkpro.core.io.reuters.ReutersDocument;
import org.junit.Test;

public class ReutersDocumentTests
{
    @Test
    public void test()
            throws ParseException
    {
        ReutersDocument doc = new ReutersDocument();
        String title = "test";

        doc.set("TITLE", title);
        assertEquals(doc.getTitle(), title);
    }
}
