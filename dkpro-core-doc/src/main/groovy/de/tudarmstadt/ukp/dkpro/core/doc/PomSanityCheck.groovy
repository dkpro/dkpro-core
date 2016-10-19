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
package de.tudarmstadt.ukp.dkpro.core.doc

import static groovy.io.FileType.FILES

import org.dkpro.meta.core.maven.ContextHolder;

class PomSanityCheck {
    public void run() {
        ContextHolder.log.info("Running POM sanity check ${ContextHolder.basedir}...")
        
        def te = new groovy.text.SimpleTemplateEngine(this.class.classLoader)
        new File(ContextHolder.basedir, '..').eachFileRecurse(FILES) { pomFile ->
            if (pomFile.name == 'pom.xml' && !pomFile.path.contains('/target/')) {
                ContextHolder.log.info("Checking ${pomFile}...")
                def pom = new XmlParser().parse(pomFile)

                if (pom.dependencies.dependency) {
                    pom.dependencies.dependency.each { dependency ->
                        // println "Checking ${dependency.artifactId.text()}";

                        // if (dependency.artifactId.text().contains('-model-')) {
                        //    println "Model dependency: $dependency.artifactId.text()";
                        // }

                        if (
                            dependency.artifactId.text().contains('-model-') &&
                            dependency.version
                        ) {
                            ContextHolder.log.warn "Model dependencies should NOT be declared in the dependency section: ${dependency.artifactId.text()}"
                        }

                        if (
                            dependency.artifactId.text().contains('-model-') &&
                            'test' != dependency.scope.text()
                        ) {
                            ContextHolder.log.warn "Model dependency should have scope 'test': ${dependency.artifactId.text()}"
                        }
                    }
                }
            }
        }
    }
    
    public static void main(String... args) {
        new PomSanityCheck().run()
    }
}

