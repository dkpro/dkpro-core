#!/usr/bin/env groovy

import static groovy.io.FileType.FILES;
import org.apache.commons.configuration.PropertiesConfiguration;

def te = new groovy.text.SimpleTemplateEngine(this.class.classLoader);
new File("${project.basedir}/../").eachFileRecurse(FILES) { pomFile ->
    if (pomFile.name == 'pom.xml' && !pomFile.path.contains('/target/')) {
        log.info("Checking ${pomFile}...");
        def pom = new XmlParser().parse(pomFile);
        
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
                    log.warn "Model dependencies should NOT be declared in the dependency section: ${dependency.artifactId.text()}";
                }
                
                if (
                    dependency.artifactId.text().contains('-model-') &&
                    'test' != dependency.scope.text()
                ) {
                    log.warn "Model dependency should have scope 'test': ${dependency.artifactId.text()}";
                }
            }
        }
    }
}
