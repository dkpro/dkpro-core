/*******************************************************************************
 * Copyright 2012
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
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
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * Base class for resource providers that produce a resource from some URL depending on changing
 * parameters such as language.
 * <p>
 * A component using such a provider sets defaults and overrides. The defaults should be set to
 * sensible values with which the component should be able to work out of the box. For example the
 * {@link #LOCATION} may be set to "classpath:/resources/${language}/model.ser.gz".
 * <p>
 * The overrides should only be set if the user explicitly wants to override some settings.
 * <p>
 * Finally, parameters that may change, e.g. depending on the CAS content should be returned from
 * {@link #getProperties()}.
 * <p>
 * The {@link #LOCATION} may contain variables referring to any of the other settings, e.g.
 * <code>"${language}"</code>.
 * <p>
 * It is possible a different default variant needs to be used depending on the language. This can
 * be configured by placing a properties file in the classpath and setting its location using
 * {@link #setDefaultVariantsLocation(String)} or by using {@link #setDefaultVariants(Properties)}.
 * The key in the properties is the language and the value is used a default variant.
 *
 * @author Richard Eckart de Castilho
 *
 * @param <M>
 *            the kind of resource produced
 */
public abstract class ResourceObjectProviderBase<M>
    implements HasResourceMetadata
{
    private final Log log = LogFactory.getLog(getClass());

    public static final String PROP_REPO_ID = "dkpro.model.repository.id";
    public static final String PROP_REPO_URL = "dkpro.model.repository.url";

    private static final String DEFAULT_REPO_ID = "ukp-model-releases";

    private static final String DEFAULT_REPO_URL = "http://zoidberg.ukp.informatik.tu-darmstadt.de/"
            + "artifactory/public-model-releases-local";

    public static final String NOT_REQUIRED = "-=* NOT REQUIRED *=-";

    /**
     * The language.
     */
    public static final String LANGUAGE = "language";

    /**
     * The variant. (optional)
     */
    public static final String VARIANT = "variant";

    /**
     * The location from which the resource should be read. Variables in the location are resolved
     * when {@link #configure()} is called.
     *
     * @see ResourceUtils#resolveLocation(String, Object, org.apache.uima.UimaContext)
     */
    public static final String LOCATION = "location";

    /**
     * The group ID of the Maven artifact containing a resource. Variables in the location are
     * resolved when {@link #configure()} is called. (optional)
     */
    public static final String GROUP_ID = "groupId";

    /**
     * The artifact ID of the Maven artifact containing a resource. Variables in the location are
     * resolved when {@link #configure()} is called. (optional)
     */
    public static final String ARTIFACT_ID = "artifactId";

    /**
     * The version of the Maven artifact containing a resource. Variables in the location are
     * resolved when {@link #configure()} is called. (optional)
     */
    public static final String VERSION = "version";

    public static final String PACKAGE = "package";

    private Properties resourceMetaData;
    private URL resourceUrl;
    private URL initialResourceUrl;
    private String lastModelLocation;
    private M resource;

    private Object contextObject;

    private Properties overrides = new Properties();
    private Properties defaults = new Properties();
    private Properties defaultVariants = null;

    private String defaultVariantsLocation;

    private Map<String, HasResourceMetadata> imports = new HashMap<String, HasResourceMetadata>();

    private ExtensibleURLClassLoader loader = new ExtensibleURLClassLoader(getClass()
            .getClassLoader());

    private PropertyPlaceholderHelper pph = new PropertyPlaceholderHelper("${", "}", null, false);

    {
        init();
    }

    protected void init()
    {
        setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
    }

    public void setOverride(String aKey, String aValue)
    {
        if (aValue == null) {
            overrides.remove(aKey);
        }
        else {
            overrides.setProperty(aKey, aValue);
        }
    }

    public String getOverride(String aKey)
    {
        return (String) overrides.get(aKey);
    }

    public void removeOverride(String aKey)
    {
        overrides.remove(aKey);
    }

    public void setDefault(String aKey, String aValue)
    {
        if (aValue == null) {
            defaults.remove(aKey);
        }
        else {
            defaults.setProperty(aKey, aValue);
        }
    }

    public String getDefault(String aKey)
    {
        return (String) defaults.get(aKey);
    }

    public void removeDefault(String aKey)
    {
        defaults.remove(aKey);
    }

    public void addImport(String aString, HasResourceMetadata aSource)
    {
        imports.put(aString, aSource);
    }

    public void removeImport(String aString)
    {
        imports.remove(aString);
    }

    /**
     * Set the location in the classpath from where to load the language-dependent default variants
     * properties file. The key in the properties is the language and the value is used a default
     * variant.
     *
     * @param aLocation
     *            a location in the form "some/package/name/tool-default-variants.properties". This
     *            is always a classpath location. This location may not contain variables.
     */
    public void setDefaultVariantsLocation(String aLocation)
    {
        defaultVariantsLocation = aLocation;
    }

    /**
     * Sets language-dependent default variants. The key in the properties is the language and the
     * value is used a default variant.
     *
     * @param aDefaultVariants
     *            the default variant per language
     */
    public void setDefaultVariants(Properties aDefaultVariants)
    {
        if (aDefaultVariants.size() == 0) {
            log.warn("setDefaultVariants called with zero-sized variants map.");
            defaultVariants = null;
        }
        else {
            defaultVariants = new Properties();
            defaultVariants.putAll(aDefaultVariants);
        }
    }

    /**
     * Set an object which can be used to try finding a Maven POM from which resource version
     * information could be extracted.
     *
     * @param aObject
     *            a context object, usually the object creating the provider.
     */
    public void setContextObject(Object aObject)
    {
        contextObject = aObject;
        setDefault(PACKAGE, aObject.getClass().getPackage().getName().replace('.', '/'));
    }

    /**
     * Tries to get the version of the required model from the dependency management section of the
     * Maven POM belonging to the context object.
     *
     * @throws IOException
     *             if there was a problem loading the POM file
     * @throws FileNotFoundException
     *             if no POM could be found
     * @throws IllegalStateException
     *             if more than one POM was found, if the version information could not be found in
     *             the POM, or if no context object was set.
     */
    protected String getModelVersionFromMavenPom()
        throws IOException
    {
        if (contextObject == null) {
            throw new IllegalStateException("No context object specified");
        }

        // Get the properties and resolve the artifact coordinates
        Properties props = getAggregatedProperties();
        String modelArtifact = pph.replacePlaceholders(props.getProperty(ARTIFACT_ID), props);
        String modelGroup = pph.replacePlaceholders(props.getProperty(GROUP_ID), props);

        // Try to determine the location of the POM file belonging to the context object
        URL url = contextObject.getClass().getResource(
                contextObject.getClass().getSimpleName() + ".class");
        String classPart = contextObject.getClass().getName().replace(".", "/") + ".class";
        String base = url.toString();
        base = base.substring(0, base.length() - classPart.length());

        URL pomUrl = null;

        String extraNotFoundInfo = "";
        if ("file".equals(url.getProtocol()) && base.endsWith("target/classes/")) {
            // This is an alternative strategy when running during a Maven build. In a normal
            // Maven build, the Maven descriptor in META-INF is only created during the
            // "package" phase, so we try looking in the project directory.
            // See also: http://jira.codehaus.org/browse/MJAR-76

            base = base.substring(0, base.length() - "target/classes/".length());
            File pomFile = new File(new File(URI.create(base)), "pom.xml");
            if (pomFile.exists()) {
                pomUrl = pomFile.toURI().toURL();
            }
            else {
                extraNotFoundInfo = " Since it looks like you are running a Maven build, it POM "
                        + "file was also searched for at [" + pomFile
                        + "], but it doesn't exist there.";
            }
        }

        if (pomUrl == null) {
            // This is the default strategy supposed to look in the JAR
            String pomPattern = base + "META-INF/maven/" + modelGroup + "/*/pom.xml";
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(pomPattern);

            // Bail out if no POM was found
            if (resources.length == 0) {
                throw new FileNotFoundException("No POM file found using [" + pomPattern + "]"
                        + extraNotFoundInfo);
            }

            // Bail out if more than one POM was found (we could also just use the first one or the
            // highest version of the model artifact referenced in any of them.
            if (resources.length > 2) {
                throw new IllegalStateException("Found more than one POM file found using ["
                        + pomPattern + "]");
            }

            pomUrl = resources[0].getURL();
        }

        // Parser the POM
        Model model;
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(pomUrl.openStream());

        }
        catch (XmlPullParserException e) {
            throw new IOException(e);
        }

        // Extract the version of the model artifact
        if ((model.getDependencyManagement() != null)
                && (model.getDependencyManagement().getDependencies() != null)) {
            List<Dependency> deps = model.getDependencyManagement().getDependencies();
            for (Dependency dep : deps) {
                if (StringUtils.equals(dep.getGroupId(), modelGroup)
                        && StringUtils.equals(dep.getArtifactId(), modelArtifact)) {
                    return dep.getVersion();
                }
            }
        }

        // Bail out if no version information for that artifact could be found
        throw new IllegalStateException("No version information found in [" + pomUrl + "]");
    }

    /**
     * For use in test cases.
     */
    protected String getModelLocation()
        throws IOException
    {
        return getModelLocation(null);
    }

    protected String getModelLocation(Properties aProperties)
        throws IOException
    {
        Properties props = aProperties;
        if (props == null) {
            props = getAggregatedProperties();
        }

        try {
            return pph.replacePlaceholders(props.getProperty(LOCATION), props);
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unable to resolve the model location ["
                    + props.getProperty(LOCATION) + "]: " + e.getMessage() + ". Possibly there is "
                    + "no default model configured for the specified language ["
                    + props.getProperty(LANGUAGE) + "] or the language is set incorrectly.");
        }
    }

    /**
     * Configure a resource using the current configuration. The resource can be fetched then using
     * {@link #getResource()}.
     * <p>
     * Call this method after all configurations have been made. A already configured resource will
     * only be recreated if the URL from which the resource is generated has changed due to
     * configuration changes.
     *
     * @throws IOException
     *             if the resource cannot be created.
     */
    public void configure()
        throws IOException
    {
        boolean success = false;

        Properties props = getAggregatedProperties();
        String modelLocation = getModelLocation(props);
        boolean modelLocationChanged = !StringUtils.equals(modelLocation, lastModelLocation);
        lastModelLocation = modelLocation;

        try {
            if (NOT_REQUIRED.equals(modelLocation)) {
                resourceUrl = null;
                initialResourceUrl = null;
                if (modelLocationChanged) {
                    log.info("Producing resource from thin air");
                    resource = produceResource(null);
                }
            }
            else {
                URL initialUrl;
                try {
                    initialUrl = resolveLocation(modelLocation, loader, null);
                }
                catch (IOException e) {
                    if (modelLocationChanged) {
                        // Try resolving the dependency and adding the stuff to the loader
                        try {
                            resolveDependency(props);
                        }
                        catch (Throwable re) {
                            // Ignore - if we cannot resolve, we cannot resolve. Re-throw the
                            // original exception
                            throw handleResolvingError(e, lastModelLocation, props);
                        }

                        try {
                            initialUrl = resolveLocation(modelLocation, loader, null);
                        }
                        catch (Throwable re) {
                            throw handleResolvingError(e, lastModelLocation, props);
                        }
                    }
                    else {
                        throw handleResolvingError(e, lastModelLocation, props);
                    }
                }

                if (!equals(initialResourceUrl, initialUrl)) {
                    initialResourceUrl = initialUrl;
                    resourceMetaData = new Properties();
                    resourceUrl = followRedirects(initialResourceUrl);
                    loadMetadata();
                    loadResource();
                }
            }
            success = true;
        }
        finally {
            if (!success) {
                resourceUrl = null;
                resource = null;
            }
        }
    }

    private static boolean equals(URL aUrl1, URL aUrl2)
    {
        if (aUrl1 == aUrl2) {
            return true;
        }

        if ((aUrl1 == null) || (aUrl2 == null)) {
            return false;
        }

        return aUrl1.toString().equals(aUrl2.toString());
    }

    protected URL followRedirects(URL aUrl) throws IOException
    {
        URL url = aUrl;

        // If the model points to a properties file, try to find a new location in that
        // file. If that points to a properties file again, repeat the process.
        while (url.getPath().endsWith(".properties")) {
            Properties tmpResourceMetaData = PropertiesLoaderUtils.loadProperties(new UrlResource(
                    url));

            // Values in the redirecting properties override values in the redirected-to
            // properties - except LOCATION
            resourceMetaData.remove(LOCATION);
            mergeProperties(resourceMetaData, tmpResourceMetaData);

            String redirect = resourceMetaData.getProperty(LOCATION);
            if (redirect == null) {
                throw new IOException("Model URL resolves to properties at [" + url
                        + "] but no redirect property [" + LOCATION + "] found there.");
            }
            url = resolveLocation(redirect, loader, null);
        }

        return url;
    }

    protected void loadMetadata() throws IOException
    {
        Properties modelMetaData = null;

        // Load resource meta data if present, look directly next to the resolved model
        try {
            String modelMetaDataLocation = getModelMetaDataLocation(resourceUrl.toString());
            URL modelMetaDataUrl = resolveLocation(modelMetaDataLocation, loader, null);
            modelMetaData = PropertiesLoaderUtils.loadProperties(new UrlResource(
                    modelMetaDataUrl));
        }
        catch (FileNotFoundException e) {
            // Ignore
        }

        // Try in the again, this time derive the metadata location first an then resolve.
        // This can help if the metadata is in another artifact, e.g. because the migration
        // to proxy/resource mode is not completed yet and the provide still looks for the model
        // and not for the properties file.
        if (modelMetaData == null) {
            try {
                String modelMetaDataLocation = getModelMetaDataLocation(lastModelLocation);
                URL modelMetaDataUrl = resolveLocation(modelMetaDataLocation, loader, null);
                modelMetaData = PropertiesLoaderUtils.loadProperties(new UrlResource(
                        modelMetaDataUrl));
            }
            catch (FileNotFoundException e2) {
                // If no metadata was found, just leave the properties empty.
            }
        }

        // Values in the redirecting properties override values in the redirected-to
        // properties.
        if (modelMetaData != null) {
            mergeProperties(resourceMetaData, modelMetaData);
        }
    }

    private String getModelMetaDataLocation(String aLocation)
    {
        String baseLocation = aLocation;
        if (baseLocation.toLowerCase().endsWith(".gz")) {
            baseLocation = baseLocation.substring(0, baseLocation.length() - 3);
        }
        else if (baseLocation.toLowerCase().endsWith(".bz2")) {
            baseLocation = baseLocation.substring(0, baseLocation.length() - 4);
        }

        String modelMetaDataLocation = FilenameUtils.removeExtension(baseLocation)
                + ".properties";

        return modelMetaDataLocation;
    }


    protected void loadResource() throws IOException
    {
        if (initialResourceUrl.equals(resourceUrl)) {
            log.info("Producing resource from " + resourceUrl);
        }
        else {
            log.info("Producing resource from [" + resourceUrl + "] redirected from [" + initialResourceUrl
                    + "]");
        }
        resource = produceResource(resourceUrl);
    }

    /**
     * Tries to figure out which artifact contains the desired resource, tries to acquire it and
     * add it to the loader. The dependencyManagement information from the POM of the caller is
     * taken into account if possible.
     */
    private void resolveDependency(Properties aProps)
        throws IOException, IllegalStateException
    {
        Set<String> names = aProps.stringPropertyNames();
        if (names.contains(ARTIFACT_ID) && names.contains(GROUP_ID)) {
            String artifactId = pph.replacePlaceholders(aProps.getProperty(ARTIFACT_ID), aProps);
            String groupId = pph.replacePlaceholders(aProps.getProperty(GROUP_ID), aProps);
            String version = pph.replacePlaceholders(aProps.getProperty(VERSION, ""), aProps);
            // Try getting better information about the model version.
            try {
                version = getModelVersionFromMavenPom();
            }
            catch (IOException e) {
                // Ignore - this will be tried and reported again later by handleResolvingError
            }
            catch (IllegalStateException e) {
                // Ignore - this will be tried and reported again later by handleResolvingError
            }

            // Register files with loader
            try {
                List<File> files = resolveWithIvy(groupId, artifactId, version);
                for (File file : files) {
                    loader.addURL(file.toURI().toURL());
                }
            }
            catch (ParseException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    protected DependencyResolver getModelResolver()
    {
        IBiblioResolver ukpModels = new IBiblioResolver();
        ukpModels.setName(System.getProperty(PROP_REPO_ID, DEFAULT_REPO_ID));
        ukpModels.setRoot(System.getProperty(PROP_REPO_URL, DEFAULT_REPO_URL));
        ukpModels.setM2compatible(true);
        return ukpModels;
    }

    /**
     * Try to fetch an artifact and its dependencies from the UKP model repository or from
     * Maven Central.
     */
    private List<File> resolveWithIvy(String aGroupId, String aArtifactId, String aVersion)
        throws ParseException, IOException
    {
        // Configure Ivy
        Message.setDefaultLogger(new ApacheCommonsMessageLogger());
        IvySettings ivySettings = new IvySettings();
        ivySettings.loadDefault();
        ivySettings.configureRepositories(true);
        ivySettings.configureDefaultVersionMatcher();

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

    private IOException handleResolvingError(Throwable aCause, String aLocation, Properties aProps)
    {
        StringBuilder sb = new StringBuilder();

        Set<String> names = aProps.stringPropertyNames();
        if (names.contains(ARTIFACT_ID) && names.contains(GROUP_ID)) {
            String artifactId = pph.replacePlaceholders(aProps.getProperty(ARTIFACT_ID), aProps);
            String groupId = pph.replacePlaceholders(aProps.getProperty(GROUP_ID), aProps);
            String version = pph.replacePlaceholders(aProps.getProperty(VERSION, ""), aProps);

            // Try getting better information about the model version.
            String extraErrorInfo = "";
            try {
                version = getModelVersionFromMavenPom();
            }
            catch (IOException ex) {
                extraErrorInfo = ExceptionUtils.getRootCauseMessage(ex);
            }
            catch (IllegalStateException ex) {
                extraErrorInfo = ExceptionUtils.getRootCauseMessage(ex);
            }

            // Tell user how to add model dependency
            sb.append("\nPlease make sure that [").append(artifactId).append(']');
            if (StringUtils.isNotBlank(version)) {
                sb.append(" version [").append(version).append(']');
            }

            sb.append(" is on the classpath.\n");

            if (StringUtils.isNotBlank(version)) {
                sb.append("If the version ").append(
                        "shown here is not available, try a recent version.\n");
                sb.append('\n');
                sb.append("If you are using Maven, add the following dependency to your pom.xml file:\n");
                sb.append('\n');
                sb.append("<dependency>\n");
                sb.append("  <groupId>").append(groupId).append("</groupId>\n");
                sb.append("  <artifactId>").append(artifactId).append("</artifactId>\n");
                sb.append("  <version>").append(version).append("</version>\n");
                sb.append("</dependency>\n");
            }
            else {
                sb.append(
                        "I was unable to determine which version of the desired model is "
                                + "compatible with this component:\n").append(extraErrorInfo)
                        .append("\n");
            }

        }

        if (NOT_REQUIRED.equals(aLocation)) {
            return new IOException("Unable to load resource: \n"
                    + ExceptionUtils.getRootCauseMessage(aCause) + "\n" + sb.toString());
        }
        else {
            return new IOException("Unable to load resource [" + aLocation + "]: \n"
                    + ExceptionUtils.getRootCauseMessage(aCause) + "\n" + sb.toString());
        }
    }

    /**
     * Get the currently configured resources. Before this can be used, {@link #configure()} needs
     * to be called once or whenever the configuration changes. Mind that sub-classes may provide
     * alternative configuration methods that may need to be used instead of {@link #configure()}.
     *
     * @return the currently configured resources.
     */
    public M getResource()
    {
        return resource;
    }

    /**
     * Builds the aggregated configuration from defaults and overrides.
     *
     * @return the aggregated effective configuration.
     * @throws IOException
     *             if the language-dependent default variants location is set but cannot be read.
     */
    protected Properties getAggregatedProperties()
        throws IOException
    {
        Properties defaultValues = new Properties(defaults);
        Properties props = getProperties();
        if (props != null) {
            defaultValues.putAll(props);
        }

        Properties importedValues = new Properties(defaultValues);
        for (Entry<String, HasResourceMetadata> e : imports.entrySet()) {
            String value = e.getValue().getResourceMetaData().getProperty(e.getKey());
            if (value != null) {
                importedValues.setProperty(e.getKey(), value);
            }
        }

        Properties overriddenValues = new Properties(importedValues);
        overriddenValues.putAll(overrides);

        if ((defaultVariants == null) && (defaultVariantsLocation != null)) {
            String dvl = pph.replacePlaceholders(defaultVariantsLocation, overriddenValues);
            setDefaultVariants(PropertiesLoaderUtils.loadAllProperties(dvl));
        }

        String language = overriddenValues.getProperty(LANGUAGE);
        if ((defaultVariants != null) && defaultVariants.containsKey(language)) {
            defaultValues.setProperty(VARIANT, defaultVariants.getProperty(language));
        }

        return overriddenValues;
    }

    protected abstract Properties getProperties();

    protected abstract M produceResource(URL aUrl)
        throws IOException;

    @Override
    public Properties getResourceMetaData()
    {
        return resourceMetaData;
    }

    /**
     * Copy all properties that not already exist in target from source.
     */
    private void mergeProperties(Properties aTarget, Properties aSource)
    {
        for (Object key : aSource.keySet()) {
            if (!aTarget.containsKey(key)) {
                aTarget.put(key, aSource.get(key));
            }
        }
    }

    private static final class ExtensibleURLClassLoader
        extends URLClassLoader
    {
        public ExtensibleURLClassLoader(ClassLoader aParent)
        {
            super(new URL[0], aParent);
        }

        @Override
        public void addURL(URL aUrl)
        {
            super.addURL(aUrl);
        }
    }
}
