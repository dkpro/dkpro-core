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
        println "Writing tagset: ${type.name}"
        
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
