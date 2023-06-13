/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.resolver.ivy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.util.Message;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.dkpro.core.api.resources.ResourceObjectProviderBase;
import org.dkpro.core.api.resources.ResourceObjectResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class IvyResourceObjectResolver
    implements ResourceObjectResolver
{
    public static final String PROP_REPO_OFFLINE = "dkpro.model.repository.offline";
    public static final String PROP_REPO_ID = "dkpro.model.repository.id";
    public static final String PROP_REPO_URL = "dkpro.model.repository.url";
    public static final String PROP_REPO_CACHE = "dkpro.model.repository.cache";

    public static final String FORCE_AUTO_LOAD = "forceAutoLoad";

    private static final String DEFAULT_REPO_ID = "ukp-model-releases";

    private static final String DEFAULT_REPO_URL = "http://zoidberg.ukp.informatik.tu-darmstadt.de/"
            + "artifactory/public-model-releases-local";

    private final Log log = LogFactory.getLog(ResourceObjectProviderBase.class);

    private DependencyResolver getModelResolver()
    {
        IBiblioResolver ukpModels = new IBiblioResolver();
        ukpModels.setName(System.getProperty(PROP_REPO_ID, DEFAULT_REPO_ID));
        ukpModels.setRoot(System.getProperty(PROP_REPO_URL, DEFAULT_REPO_URL));
        ukpModels.setM2compatible(true);
        return ukpModels;
    }

    @Override
    public List<File> resolveResoureArtifact(String aGroupId, String aArtifactId, String aVersion)
        throws IOException
    {
        if ("true".equals(System.getProperty(PROP_REPO_OFFLINE))) {
            log.debug("Offline mode active - attempt to download missing resource automatically "
                    + "is skipped.");
            return Collections.emptyList();
        }

        // Configure Ivy
        Message.setDefaultLogger(new ApacheCommonsLoggingAdapter(log));
        IvySettings ivySettings = new IvySettings();
        try {
            ivySettings.loadDefault();
        }
        catch (ParseException e) {
            throw new IOException(e);
        }
        ivySettings.configureRepositories(true);
        ivySettings.configureDefaultVersionMatcher();
        if (System.getProperties().containsKey(PROP_REPO_CACHE)) {
            ivySettings.setDefaultCache(new File(System.getProperty(PROP_REPO_CACHE)));
        }

        // Add a resolver for the UKP model repository
        DependencyResolver modelResolver = getModelResolver();
        modelResolver.setSettings(ivySettings);
        ivySettings.addResolver(modelResolver);
        ((ChainResolver) ivySettings.getResolver("main")).add(modelResolver);

        // Initialize Ivy
        Ivy ivy = Ivy.newInstance(ivySettings);

        // Create a dummy module which has the desired artifact as a dependency
        // The dummy module is kept in the Ivy cache only temporary, so make use it is unique
        // using a UUID and make sure it is removed from the cache at the end.
        UUID uuid = UUID.randomUUID();
        ModuleRevisionId moduleId = ModuleRevisionId
                .newInstance("dkpro", uuid.toString(), "working");
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(moduleId);
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md,
                ModuleRevisionId.newInstance(aGroupId, aArtifactId, aVersion), false, false, true);
        dd.addDependencyConfiguration("default", "default");
        md.addDependency(dd);

        ResolveReport report;
        try {
            // Resolve the temporary module
            ResolveOptions options = new ResolveOptions();
            if (log.isDebugEnabled()) {
                options.setLog(LogOptions.LOG_DEFAULT);
            }
            else if (log.isInfoEnabled()) {
                options.setLog(LogOptions.LOG_DOWNLOAD_ONLY);
            }
            else {
                options.setLog(LogOptions.LOG_QUIET);
            }
            options.setConfs(new String[] { "default" });
            report = ivy.resolve(md, options);
        }
        catch (ParseException e) {
            throw new IOException(e);
        }
        finally {
            // Remove temporary module
            String resid = ResolveOptions.getDefaultResolveId(md);
            ivy.getResolutionCacheManager().getResolvedIvyFileInCache(moduleId).delete();
            ivy.getResolutionCacheManager().getResolvedIvyPropertiesInCache(moduleId).delete();
            ivy.getResolutionCacheManager().getConfigurationResolveReportInCache(resid, "default")
                    .delete();
        }

        // Get the artifact and all its transitive dependencies
        List<File> files = new ArrayList<File>();
        for (ArtifactDownloadReport rep : report.getAllArtifactsReports()) {
            files.add(rep.getLocalFile());
        }
        return files;
    }

    @Override
    public String resolveResourceArtifactVersion(String aComponentGroupId, String aModelGroupId,
            String aModelArtifactId, Class<?> aClass)
        throws IOException
    {
        List<URL> urls = getPomUrlsForClass(aComponentGroupId, aModelArtifactId, aClass);

        for (URL pomUrl : urls) {
            // Parse the POM
            Model model;
            try {
                model = new MavenXpp3Reader().read(pomUrl.openStream());
            }
            catch (Exception e) {
                throw new IOException(e);
            }

            // Extract the version of the model artifact
            if ((model.getDependencyManagement() != null)
                    && (model.getDependencyManagement().getDependencies() != null)) {
                List<Dependency> deps = model.getDependencyManagement().getDependencies();
                for (Dependency dep : deps) {
                    if (
                            StringUtils.equals(dep.getGroupId(), aModelGroupId) && 
                            StringUtils.equals(dep.getArtifactId(), aModelArtifactId)
                    ) {
                        return dep.getVersion();
                    }
                }
            }
        }

        // Bail out if no version information for that artifact could be found
        throw new IllegalStateException("No version information found.");
    }

    protected List<URL> getPomUrlsForClass(String aComponentGroupId, String aModelArtifactId,
            Class<?> aClass)
        throws IOException
    {
        if (aClass == null) {
            throw new IllegalArgumentException("No context class specified");
        }
        
        // Try to determine the location of the POM file belonging to the context object
        URL url = aClass.getResource(aClass.getSimpleName() + ".class");
        String classPart = aClass.getName().replace(".", "/") + ".class";
        String base = url.toString();
        base = base.substring(0, base.length() - classPart.length());

        List<String> lookupPatterns = new ArrayList<>();
        List<URL> urls = new LinkedList<URL>();

        String extraNotFoundInfo = "";
        if ("file".equals(url.getProtocol()) && base.endsWith("target/classes/")) {
            // This is an alternative strategy when running during a Maven build. In a normal
            // Maven build, the Maven descriptor in META-INF is only created during the
            // "package" phase, so we try looking in the project directory.
            // See also: http://jira.codehaus.org/browse/MJAR-76

            base = base.substring(0, base.length() - "target/classes/".length());
            File pomFile = new File(new File(URI.create(base)), "pom.xml");
            if (pomFile.exists()) {
                urls.add(pomFile.toURI().toURL());
            }
            else {
                extraNotFoundInfo = " Since it looks like you are running a Maven build, it POM "
                        + "file was also searched for at [" + pomFile
                        + "], but it doesn't exist there.";
            }
        }

        // If the class is in a JAR (that should be the normal case), try deriving the 
        // POM location from the JAR file name.
        if (urls.isEmpty()) {
            Pattern pattern = Pattern.compile(
                    ".*/(?<ID>([a-zA-Z0-9-_]+\\.)*[a-zA-Z0-9-_]+)-([0-9]+\\.)*[0-9]+(-[a-zA-Z]+)?\\.jar!/.*");
            Matcher matcher = pattern.matcher(base);
            if (matcher.matches()) {
                String artifactIdAndVersion = matcher.group("ID");
                String pomPattern = base + "META-INF/maven/" + aComponentGroupId + "/"
                        + artifactIdAndVersion + "/pom.xml";
                lookupPatterns.add(pomPattern);
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                Resource[] resources = resolver.getResources(pomPattern);
                for (Resource r : resources) {
                    urls.add(r.getURL());
                }
            }
        }
        
        // Legacy lookup strategy deriving the POM location from the model artifact ID. This
        // fails if a module is re-using models from another module (e.g. CoreNLP re-using 
        // models from the StanfordNLP module).
        if (urls.isEmpty()) {
            // This is the default strategy supposed to look in the JAR
            String moduleArtifactId = aModelArtifactId.split("-")[0];
            String pomPattern = base + "META-INF/maven/" + aComponentGroupId + "/"
                    + moduleArtifactId + "*/pom.xml";
            lookupPatterns.add(pomPattern);
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(pomPattern);

            for (Resource r : resources) {
                urls.add(r.getURL());
            }
        }
        
        // Bail out if no POM was found
        if (urls.isEmpty()) {
            throw new FileNotFoundException("No POM file found using the patterns " + lookupPatterns
                    + ". " + extraNotFoundInfo);
        }
        
        return urls;
    }
}
