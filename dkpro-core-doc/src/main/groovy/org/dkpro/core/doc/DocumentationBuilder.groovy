/*
 * Copyright 2017
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
package org.dkpro.core.doc;

class DocumentationBuilder {
    public void run() {
        File dkproCorePath = new File(ContextHolder.basedir, '..');
        
        MetadataModel model = new MetadataAggregator().build(dkproCorePath);
            
        Yaml.any();
        
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