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
package de.tudarmstadt.ukp.dkpro.core.doc;

import static groovy.io.FileType.FILES;
import groovy.json.*;
import groovy.text.XmlTemplateEngine;
import groovy.transform.Field;
import groovy.util.XmlParser;
import org.dkpro.meta.core.MetadataAggregator;
import org.dkpro.meta.core.maven.ContextHolder;
import org.dkpro.meta.core.model.MetadataModel;

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

class DocumentationBuilder {
    public void run() {
        File dkproCorePath = new File(ContextHolder.basedir, '..');
        
        MetadataModel model = new MetadataAggregator().build(dkproCorePath);
            
        def templateBinding = [
                project: ContextHolder.project,
                log: ContextHolder.log,
                engines: model.engines,
                formats: model.formats,
                models: model.models,
                datasets: model.datasets,
                tagsets: model.tagsets,
                typesystems: model.typesystems,
                typesystemMappings: model.typesystemMappings,
                inputOutputTypes: model.inputOutputTypes];
        
        def te = new groovy.text.SimpleTemplateEngine(this.class.classLoader);
        new File("${ContextHolder.basedir}/src/main/script/templates/").eachFile(FILES) { tf ->
            if (tf.name.endsWith('.adoc')) {
                ContextHolder.log.info("Processing ${tf.name}...");
                try {
                    def template = te.createTemplate(tf.getText("UTF-8"));
                    def result = template.make(templateBinding);
                    def output = new File("${ContextHolder.basedir}/target/generated-adoc/${tf.name}");
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
        
        new File("${ContextHolder.basedir}/src/main/script/templates/").eachFile(FILES) { tf ->
            if (tf.name.endsWith('.groovy')) {
                ContextHolder.log.info("Processing ${tf.name}...");
                def shell = new GroovyShell(new Binding(templateBinding));
                shell.evaluate(tf.getText("UTF-8"));
            }
        }
    }
    
    public static void main(String... args) {
        new DocumentationBuilder().run();
    }
}