#!/usr/bin/env groovy

import static groovy.io.FileType.FILES;
import groovy.json.*;
import groovy.transform.Field;
import static org.apache.uima.UIMAFramework.getXMLParser;
import static org.apache.uima.fit.factory.ResourceCreationSpecifierFactory.*;
import static org.apache.uima.util.CasCreationUtils.mergeTypeSystems;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.XMLInputSource;

@Field def engines = [:];

@Field def formats = [:];

@Field def typesystems = [];

def locatePom(path) {
    def pom = new File(path, "pom.xml");
    if (pom.exists()) {
        return pom;
    }
    else if (path.getParentFile() != null) {
        return locatePom(path.getParentFile());
    }
    else {
        return null;
    }
}

def addFormat(format, kind, pom, spec, clazz) {
    if (!formats[format]) {
        formats[format] = [
            groupId: pom.groupId ? pom.groupId.text() : pom.parent.groupId.text(),
            artifactId: pom.artifactId.text(),
            version: pom.version ? pom.version.text() : pom.parent.version.text(),
            pom: pom
        ];
    }
    formats[format][kind+'Class'] = clazz;
    formats[format][kind+'Spec'] = spec;
}

def getRole(componentName, spec) {
    def outputs = spec.analysisEngineMetaData?.capabilities?.collect { 
        it.outputs?.collect { it.name } }.flatten().sort().unique()
    
    switch (componentName) {
    case { 'de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain' in outputs }: 
        return "Coreference resolver";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity' in outputs }: 
        return "Named Entity Recognizer";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly' in outputs ||
           'de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly' in outputs }: 
        return "Checker";
    case { it.endsWith("Transformer") || it.endsWith("Normalizer") }: 
        return "Transformer";
    case { it.endsWith("Chunker") }: 
        return "Chunker";
    case { it.endsWith("Tagger") }: 
        return "Part-of-speech tagger";
    case { it.endsWith("Parser") }: 
        return "Parser";
    case { it.endsWith("Segmenter") }: 
        return "Segmenter";
    case { it.endsWith("Normalizer") }: 
        return "Normalizer";
    case { it.endsWith("Lemmatizer") }: 
        return "Lemmatizer";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument' in outputs }: 
        return "Semantic role labeller";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures' in outputs }: 
        return "Morphological analyzer";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem' in outputs }: 
        return "Stemmer";
    default:
        return "Other";
    }
}

new File(properties['baseDir'], '..').eachFileRecurse(FILES) {
    if (
        it.name.endsWith('.xml') && 
        it.path.contains('/src/main/resources/')
//        !it.path.contains('src/test/') && 
//        !it.path.contains('/target/surefire-reports/') && 
//        !it.path.contains('/target/test-classes/') && 
//        !it.path.contains('/target/test-output/') && 
//        !it.path.contains('/.settings/') && 
//        !it.path.endsWith('/build.xml') && 
//        !it.path.endsWith('/pom.xml') && 
//        !it.path.contains('core.testing-asl')
    ) {
        def processed = false;
        try {
            def spec = createResourceCreationSpecifier(it.path, null);
            if (spec instanceof AnalysisEngineDescription) {
                // println "AE " + it;
                def implName = spec.annotatorImplementationName;
                def uniqueName = implName.substring(implName.lastIndexOf('.')+1);
                def pomFile = locatePom(it);
                def pom = new XmlParser().parse(pomFile);

                if (!implName.contains('$')) {
                    if (implName.endsWith('Writer')) {
                        def format = uniqueName[0..-7];
                        addFormat(format, 'writer', pom, spec, spec.annotatorImplementationName);
                    }
                    else {
                        engines[uniqueName] = [
                            groupId: pom.groupId ? pom.groupId.text() : pom.parent.groupId.text(),
                            artifactId: pom.artifactId.text(),
                            version: pom.version ? pom.version.text() : pom.parent.version.text(),
                            pom: pom,
                            spec: spec,
                            role: getRole(uniqueName, spec)
                        ];
                    }
                }
            }
            else if (spec instanceof CollectionReaderDescription) {
                def implName = spec.implementationName;
                if (implName.endsWith('Reader') && !implName.contains('$')) {
                    def uniqueName = implName.substring(implName.lastIndexOf('.')+1);
                    def pomFile = locatePom(it);
                    def pom = new XmlParser().parse(pomFile);
                    def format = uniqueName[0..-7];
                    addFormat(format, 'reader', pom, spec, implName);
                }
            }
            else {
                // println "?? " + it;
            }
            processed = true;
        }
        catch (org.apache.uima.util.InvalidXMLException e) {
            // Ignore
        }
        
        if (!processed) {
            try {
                typesystems << getXMLParser().parseTypeSystemDescription(
                    new XMLInputSource(it.path));
            }
            catch (org.apache.uima.util.InvalidXMLException e) {
                // Ignore
            }
        }
    }
}

def inputOutputTypes = [];
engines.each {
    it.value.spec.analysisEngineMetaData?.capabilities?.each { capability ->
        capability?.inputs.each { inputOutputTypes << it.name};
        capability?.outputs.each { inputOutputTypes << it.name};
    }
}
inputOutputTypes = inputOutputTypes.sort().unique();


def te = new groovy.text.SimpleTemplateEngine();
new File("${properties['baseDir']}/src/main/script/templates/").eachFile(FILES) { tf ->
    def template = te.createTemplate(tf.getText("UTF-8"));
    def result = template.make([
        engines: engines,
        formats: formats,
        typesystems: typesystems,
        inputOutputTypes: inputOutputTypes]);
    def output = new File("target/generated-adoc/${tf.name}");
    output.parentFile.mkdirs();
    output.setText(result.toString(), 'UTF-8');
}
