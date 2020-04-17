/*
 * Copyright 2019
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
package org.dkpro.core.io.brat.internal.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MappingParamTest
{
    @Test
    public void testParsing()
    {
        TypeMapping param = TypeMapping
                .parse("Country -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity");
        
        assertThat(param.matches("Country")).isTrue();
        assertThat(param.apply()).isEqualTo("de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity");
    }
}
