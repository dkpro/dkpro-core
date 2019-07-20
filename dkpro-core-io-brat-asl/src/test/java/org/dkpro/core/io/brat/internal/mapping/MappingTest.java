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

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingTest
{
    @Test
    public void testParse() throws Exception
    {
        String json = String.join("\n",
                "{",
                "  'textTypeMapppings': [",
                "    {",
                "      'from': 'NamedEntity',",
                "      'to': 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity',",
                "      'defaultFeatureValues': {",
                "        'identity': 'none'",
                "      }",
                "    }",
                "  ],",
                "  'relationTypeMapppings': [",
                "    {",
                "      'from': 'Dependency',",
                "      'to': 'de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency',",
                "      'defaultFeatureValues': {",
                "        'flavour': 'basic'",
                "      }",
                "    }",
                "  ],",
                "  'spans': [",
                "    {",
                "      'type': 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity',",
                "      'subCatFeature': 'value',",
                "      'defaultFeatureValues': {",
                "        'identity': 'none'",
                "      }",
                "    }",
                "  ],",
                "  'relations': [",
                "    {",
                "      'type': 'de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency',",
                "      'arg1': 'source',",
                "      'arg2': 'target',",
                "      'flags2': 'A',",
                "      'subCatFeature': 'DependencyType',",
                "      'defaultFeatureValues': {",
                "        'flavour': 'basic'",
                "      }",
                "    }",
                "  ],",
                "  'comments': [",
                "    {",
                "      'type': 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity',",
                "      'feature': 'identifier'",
                "    }",
                "  ]",
                "}");
        
        Mapping mapping = parse(json);
    }
    
    private Mapping parse(String aJson) throws JsonParseException, JsonMappingException, IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY));
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        return mapper.readValue(aJson, Mapping.class);
    }
}
