// Copyright 2018
// Ubiquitous Knowledge Processing (UKP) Lab
// Technische Universität Darmstadt
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import groovy.json.*;
import org.yaml.snakeyaml.Yaml;
import java.util.regex.Pattern;

def simpleTypes = [:];

template: {
    def templateType = [:];
    templateType.name = "org.dummy.Example";
    templateType.features = [:];
    templateType.externalReferences = [[
        source: "LAPPS",
        id: "http://some/example",
        rel: "similar"]];
    simpleTypes['TEMPLATE'] = templateType;
}

typesystems
    .collect { it.types }
    .flatten()
    .sort { it.name }
    .unique { it.name }
    .each { type ->
        def simpleType = [:];
        simpleType.features = [:];
        simpleType.externalReferences = [];
        
        type.features.each { feature ->
            simpleType.features[feature.name] = [:];
            def simpleFeature = simpleType.features[feature.name];
            simpleFeature.externalReferences = [];
        }        
        
        simpleTypes[type.name] = simpleType;
    }
    
Yaml yaml = new Yaml()
File target = new File("target/typesystemmappings.yaml");
target.getParentFile().mkdirs();
target.withPrintWriter("UTF-8", { it.print yaml.dump(simpleTypes) });
