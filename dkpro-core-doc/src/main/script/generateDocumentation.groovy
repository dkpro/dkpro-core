#!/usr/bin/env groovy

import static groovy.io.FileType.FILES;
import groovy.json.*;
import groovy.transform.Field;
import static org.apache.uima.UIMAFramework.getXMLParser;
import static org.apache.uima.fit.factory.ResourceCreationSpecifierFactory.*;
import static org.apache.uima.util.CasCreationUtils.mergeTypeSystems;
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.XMLInputSource;
import org.yaml.snakeyaml.Yaml;

def loadTypeSystemMapping() {
  return new File(project.basedir, "src/main/script/mappings/typesystemmapping.yaml").withInputStream { 
    new Yaml().load(it) };
}

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

def addFormat(aTarget, format, kind, pom, spec, clazz) {
    if (!aTarget[format]) {
        aTarget[format] = [
            name: format,
            groupId: pom.groupId ? pom.groupId.text() : pom.parent.groupId.text(),
            artifactId: pom.artifactId.text(),
            version: pom.version ? pom.version.text() : pom.parent.version.text(),
            pom: pom
        ];
    }
    aTarget[format][kind+'Class'] = clazz;
    aTarget[format][kind+'Spec'] = spec;
}

def roleNames = [
    coref:          'Coreference resolver',
    tagger:         'Part-of-speech tagger',
    parser:         'Parser',
    chunker:        'Chunker',
    segmenter:      'Segmenter',
    checker:        'Checker',
    lemmatizer:     'Lemmatizer',
    srl:            'Semantic role labeler',
    morph:          'Morphological analyzer',
    transformer:    'Transformer',
    stem:           'Stemmer',
    ner:            'Named Entity Recognizer',
    langdetect:     'Language Identifier',
    transcriptor:   'Phonetic Transcriptor',
    topicmodel:     'Topic Model',
    embeddings:     'Embeddings',
    gazeteer:       'Gazeteer',
    other:          'Other' ];

/**
 * Get a short tool type identifier for the given component. This may be used to resolve tagset
 * mappings, model identifiers, etc.
 */
def getTool(componentName, spec) {
    def outputs = spec.analysisEngineMetaData?.capabilities?.collect { 
        it.outputs?.collect { it.name } }.flatten().sort().unique()
    
    def baseComponentName = componentName.endsWith("Trainer") ? 
        componentName[0..-8] : componentName;
        
    switch (baseComponentName) {
    case { 'de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain' in outputs }: 
        return "coref";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity' in outputs }: 
        return "ner";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly' in outputs ||
           'de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly' in outputs }: 
        return "checker";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures' in outputs }: 
        return "morph";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument' in outputs ||
           'de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg' in outputs}: 
        return "srl";
    case { 'de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem' in outputs }: 
        return "stem";
    case { 
            it.endsWith("Transformer") || 
            it.endsWith("Normalizer") ||
            it.endsWith("ApplyChangesAnnotator") ||
            it.endsWith("Backmapper") ||
            spec.annotatorImplementationName.contains('.textnormalizer.transformation.') }: 
        return "transformer";
    case { it.endsWith("Chunker") }: 
        return "chunker";
    case { it.endsWith("LanguageIdentifier") || it.contains("LanguageDetector") }: 
        return "langdetect";
    case { it.endsWith("NamedEntityRecognizer") }: 
        return "ner";
    case { it.endsWith("Tagger") }: 
        return "tagger";
    case { it.endsWith("Parser") }: 
        return "parser";
    case { 
            it.endsWith("Segmenter") || 
            it.endsWith("Tokenizer") ||
            it.endsWith("Sentence") ||
            it.endsWith("Token") ||
            spec.annotatorImplementationName.contains('.tokit.')}: 
        return "segmenter";
    case { it.endsWith("Lemmatizer") }: 
        return "lemmatizer";
    case { it.endsWith("PhoneticTranscriptor") }: 
        return "transcriptor";
    case { it.contains("TopicModel") }:
        return "topicmodel";
    case { it.contains("DictionaryAnnotator") }:
        return "gazeteer";
    case { it.contains("Embeddings") }:
        return "embeddings";
    case { it.contains("DependencyConverter") }:
        return "parser";
    default:
        return "other";
    }
}

/**
 * Scan the UIMA type system descriptors.
 */
def scanUimaTypeSystemDescriptors(File aDirectory) {
    def ts = []
    aDirectory.eachFileRecurse(FILES) {
        if (
            it.name.endsWith('.xml') &&
            // No testing module
            !it.path.contains('/dkpro-core-testing-asl/') &&
            // For the typesystem descriptors
            it.path.contains('/src/main/resources/')
        ) {
            try {
                ts << getXMLParser().parseTypeSystemDescription(
                    new XMLInputSource(it.path));
            }
            catch (org.apache.uima.util.InvalidXMLException e) {
                // Ignore
            }
        }
    }
    return ts;
}

/**
 * Scan the UIMA component descriptors.
 */
def scanUimaComponentDescriptors(File aDirectory, Map<String, String> aRoleNames) {
    def es = [:];
    def fs = [:];
    aDirectory.eachFileRecurse(FILES) {
        if (
            it.name.endsWith('.xml') &&
            // No testing module
            !it.path.contains('/dkpro-core-testing-asl/') &&
            // For the analysis engine and reader descriptions
            it.path.contains('/target/classes/')
        ) {
            try {
                def spec = createResourceCreationSpecifier(it.path, null);
                if (spec instanceof AnalysisEngineDescription) {
                    // println "AE " + it;
                    def implName = spec.annotatorImplementationName;
                    def uniqueName = implName.substring(implName.lastIndexOf('.')+1);
                    def pomFile = locatePom(it);
                    def pom = new XmlParser().parse(pomFile);
                    def module = it.path[project.basedir.path.length()+4..-1];
                    module = module[0..module.indexOf('/')-1]
    
                    if (!implName.contains('$')) {
                        if (implName.endsWith('Writer')) {
                            def format = uniqueName[0..-7];
                            addFormat(fs, format, 'writer', pom, spec, spec.annotatorImplementationName);
                        }
                        else {
                            es[uniqueName] = [
                                name: uniqueName,
                                implName: implName,
                                groupId: pom.groupId ? pom.groupId.text() : pom.parent.groupId.text(),
                                artifactId: pom.artifactId.text(),
                                version: pom.version ? pom.version.text() : pom.parent.version.text(),
                                module: module,
                                pom: pom,
                                spec: spec,
                                role: aRoleNames[getTool(uniqueName, spec)],
                                tool: getTool(uniqueName, spec)
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
                        addFormat(fs, format, 'reader', pom, spec, implName);
                    }
                }
                else {
                    // println "?? " + it;
                }
            }
            catch (org.apache.uima.util.InvalidXMLException e) {
                // Ignore
            }
        }
    }
    return [es, fs];
}

/**
 * Scan the build.xml files used for packaging models.
 */
def scanModelBuilderFiles(File aDirectory, def aEngines) {
    def ms = [];
    aDirectory.eachFileRecurse(FILES) {
        if (it.path.endsWith('/src/scripts/build.xml')) {
            def buildXml = new XmlSlurper().parse(it);
            def modelXmls = buildXml.'**'.findAll{ node -> node.name() in [
                'install-stub-and-upstream-file', 'install-stub-and-upstream-folder',
                'install-upstream-file', 'install-upstream-folder' ]};
            
            // Extrack package
            def pack = buildXml.'**'.find { it.name() == 'property' && it.@name == 'outputPackage' }.@value as String;
            if (pack.endsWith('/')) {
                pack = pack[0..-2];
            }
            if (pack.endsWith('/lib')) {
                pack = pack[0..-5];
            }
            pack = pack.replaceAll('/', '.');
            
            // Auto-generate some additional attributes for convenience!
            modelXmls.each { model ->
                def shortBase = model.@artifactIdBase.text().tokenize('.')[-1];
                model.@shortBase = shortBase as String;
                model.@shortArtifactId = "${shortBase}-model-${model.@tool}-${model.@language}-${model.@variant}" as String;
                model.@artifactId = "${model.@artifactIdBase}-model-${model.@tool}-${model.@language}-${model.@variant}" as String;
                model.@package = pack as String;
                model.@version = "${model.@upstreamVersion}.${model.@metaDataVersion}" as String;
                
                def engine = aEngines.values()
                    .findAll { engine ->
                        def clazz = engine.spec.annotatorImplementationName;
                        def enginePack = clazz.substring(0, clazz.lastIndexOf('.'));
                        enginePack == pack;
                    }
                    .find { engine ->
                        // There should be only one tool matching here - at least we don't have models
                        // yet that apply to multiple tools... I believe - REC
                        switch (model.@tool as String) {
                        case 'token':
                            return engine.tool == 'segmenter';
                        case 'sentence':
                            return engine.tool == 'segmenter';
                        // Special handling for langdetect models which use wrong tool designation
                        case 'languageidentifier':
                            return engine.tool == 'langdetect';
                        // Special handling for MateTools models which use wrong tool designation
                        case 'morphtagger':
                            return engine.tool == 'morph';
                        // Special handling for ClearNLP lemmatizer because dictionary is actually
                        // used in multiple places
                        case 'dictionary':
                            return engine.tool == 'lemmatizer';
                        // Required to handle CoreNLP "depparser" models because depparser component
                        // is categorized as "parser" not as "depparser"
                        case 'depparser':
                            return engine.tool == 'parser';
                        default:
                            return engine.tool == (model.@tool as String);
                        }
                    };
                if (engine) {
                    model.@engine = engine.name;
                }
                else {
                    log.warn("No engine found for model ${model.@shortArtifactId}");
                }
                
            }
            ms.addAll(modelXmls);
        }
    }
    
    ms = ms.sort { a,b ->
        (a.@language as String) <=> (b.@language as String) ?: 
        (a.@tool as String) <=> (b.@tool as String) ?: 
        (a.@engine as String) <=> (b.@engine as String) ?: 
        (a.@variant as String) <=> (b.@variant as String)  
    }; 

    return ms;
}

/**
 * Scan DKPro Core tagset mappings
 */
def scanTagsetMappings(File aDirectory) {
    def typesystems = [:];
    
    aDirectory.eachFileRecurse(FILES) {
        if (
            it.path.contains('/src/main/resources/') &&
            it.path.contains('/tagset/') &&
            it.path.endsWith('.map') &&
            !it.name.startsWith('TEMPLATE')
        ) {
            def config = new PropertiesConfiguration();
            config.setFile(it);
            config.setEncoding("UTF-8");
            config.setListDelimiter(0 as char);
            config.load();
            
            // Remove .map and split
            def parts = it.name[0..-5].tokenize('-');
    
            // Skip legacy default mappings that were only layer + language.
            if (parts.size <= 2) {
                return;
            }
            
            def lang = parts[0];
            def name = parts[1..-2].join('-');
            def tool = parts[-1];
            
            // Skip the morphological features mapping for now because the files have completely
            // different semantics from the other mapping files.
            if (tool == "morph") {
                return;
            }
            
            // Fix the currently bad practice of naming mappings for constituent parse types
            if (tool == "constituency") {
                tool = "constituent"
            }
            
            // Try extracting the long tagset name
            def longName = config.layout.getCanonicalHeaderComment(true);
            if (longName) {
                def lines = longName.split('\n');
                if (lines.size() > 0) {
                    longName = lines[0];
                }
                
                if (longName.startsWith('#')) {
                    longName = longName.length() > 1 ? longName[1..-1].trim() : '';
                }
            }
            
            typesystems["${lang}-${name}-${tool}"] = [
                id: "${lang}-${name}-${tool}",
                lang: lang,
                name: name,
                longName: longName ?: name,
                tool: tool,
                mapping: config,
                source: it,
                url: 'https://github.com/dkpro/dkpro-core/edit/master/' +
                    it.canonicalPath[aDirectory.canonicalPath.length()..-1]
                ];
        }
    }
    
    typesystems = typesystems.sort { a,b ->
        (a.value.tool as String) <=> (b.value.tool as String) ?:
        (a.value.lang as String) <=> (b.value.lang as String) ?:
        (a.value.tagset as String) <=> (b.value.tagset as String)
    };
    
    return typesystems;
}

/**
 * Scan DKPro Core dataset definitions
 */
def scanDatasets(File aDirectory) {
    def datasets = [:];
    def dsd = 'dkpro-core-api-datasets-asl/src/main/resources/de/tudarmstadt/ukp/dkpro/core/api/datasets/lib';
    new File(aDirectory, dsd).eachFileRecurse(FILES) {
        if (it.path.endsWith('.yaml')) {
          def ds = it.withInputStream { new Yaml().load(it) };
          datasets[FilenameUtils.removeExtension(it.name)] = ds;
          datasets[FilenameUtils.removeExtension(it.name)]['githubUrl'] = 
            'https://github.com/dkpro/dkpro-core/edit/master/' + 
            it.canonicalPath[aDirectory.canonicalPath.length()..-1]
        }
    }

    datasets = datasets.sort { a,b ->
        (a.value.name as String) <=> (b.value.name as String)
    };

    return datasets;
}

def collectInputOutputTypes(aEngines) {
    def inputOutputTypes = [];
    aEngines.each {
        it.value.spec.analysisEngineMetaData?.capabilities?.each { capability ->
            capability?.inputs.each { inputOutputTypes << it.name};
            capability?.outputs.each { inputOutputTypes << it.name};
        }
    }
    inputOutputTypes = inputOutputTypes.sort().unique();
    return inputOutputTypes;
}

def typesystems = scanUimaTypeSystemDescriptors(new File(project.basedir, '..'));
log.info("Found ${typesystems.size()} typesystems");

def (engines, formats) = scanUimaComponentDescriptors(new File(project.basedir, '..'), roleNames);
log.info("Found ${engines.size()} components");
log.info("Found ${formats.size()} formats");

def models = scanModelBuilderFiles(new File(project.basedir, '..'), engines);
log.info("Found ${models.size()} models");

def tagsets = scanTagsetMappings(new File(project.basedir, '..'));
log.info("Found ${tagsets.size()} tagsets");

def inputOutputTypes = collectInputOutputTypes(engines);
log.info("Found ${inputOutputTypes.size()} input/output types");

def typesystemMappings = loadTypeSystemMapping();
log.info("Found ${typesystemMappings.size()} type mappings");

def datasets = scanDatasets(new File(project.basedir, '..'));
log.info("Found ${datasets.size()} datasets");

def te = new groovy.text.SimpleTemplateEngine(this.class.classLoader);
new File("${project.basedir}/src/main/script/templates/").eachFile(FILES) { tf ->
    if (tf.name.endsWith('.adoc')) {
        log.info("Processing ${tf.name}...");
        try {
            def template = te.createTemplate(tf.getText("UTF-8"));
            def result = template.make([
                project: project,
                engines: engines,
                formats: formats,
                models: models,
                datasets: datasets,
                log: log,
                tagsets: tagsets,
                typesystems: typesystems,
                typesystemMappings: typesystemMappings,
                inputOutputTypes: inputOutputTypes]);
            def output = new File("${project.basedir}/target/generated-adoc/${tf.name}");
            output.parentFile.mkdirs();
            output.setText(result.toString(), 'UTF-8');
        }
        catch (Exception e) {
            te.setVerbose(true);
            te.createTemplate(tf.getText("UTF-8"));
            throw e;
        }
    }
}

new File("${project.basedir}/src/main/script/templates/").eachFile(FILES) { tf ->
    if (tf.name.endsWith('.groovy')) {
        log.info("Processing ${tf.name}...");
        def shell = new GroovyShell(new Binding([
            project: project,
            engines: engines,
            formats: formats,
            models: models,
            datasets: datasets,
            log: log,
            tagsets: tagsets,
            typesystems: typesystems,
            typesystemMappings: typesystemMappings,
            inputOutputTypes: inputOutputTypes]));
        shell.evaluate(tf.getText("UTF-8"));
    }
}
