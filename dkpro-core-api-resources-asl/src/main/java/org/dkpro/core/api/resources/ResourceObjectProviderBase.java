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
package org.dkpro.core.api.resources;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.dkpro.core.api.resources.ResourceUtils.resolveLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.time.StopWatch;
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
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.fit.internal.ReflectionUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dkpro.core.api.resources.internal.ApacheCommonsLoggingAdapter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePatternResolver;
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
 * {@code getProperties()}.
 * <p>
 * The {@link #LOCATION} may contain variables referring to any of the other settings, e.g.
 * <code>"${language}"</code>.
 * <p>
 * It is possible a different default variant needs to be used depending on the language. This can
 * be configured by placing a properties file in the classpath and setting its location using
 * {@link #setDefaultVariantsLocation(String)} or by using {@link #setDefaultVariants(Properties)}.
 * The key in the properties is the language and the value is used a default variant.
 *
 * @param <M>
 *            the kind of resource produced
 */
public abstract class ResourceObjectProviderBase<M>
    implements HasResourceMetadata
{
    private final Log log = LogFactory.getLog(ResourceObjectProviderBase.class);

    public static final String PROP_REPO_OFFLINE = "dkpro.model.repository.offline";
    public static final String PROP_REPO_ID = "dkpro.model.repository.id";
    public static final String PROP_REPO_URL = "dkpro.model.repository.url";
    public static final String PROP_REPO_CACHE = "dkpro.model.repository.cache";
    
    public static final String FORCE_AUTO_LOAD = "forceAutoLoad";

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
     * A URI pointing to the artifact. Currently, this URI expected to be in the format
     * {@code mvn:${groupId}:${artifactId}:${version}}.
     */
    public static final String ARTIFACT_URI = "artifactUri";
    
    /**
     * The version of the Maven artifact containing a resource. Variables in the location are
     * resolved when {@link #configure()} is called. (optional)
     */
    public static final String VERSION = "version";

    public static final String PACKAGE = "package";

    /**
     * If this property is set to {@code true}, resources loaded through this provider are
     * remembered by the provider using a weak reference. If the same resource is requested by
     * another instance of this provider class, the same resource is returned.
     */
    public static final String SHARABLE = "sharable";

    public static final String CATCH_ALL = "*";
    
    private Properties resourceMetaData;
    private URL resourceUrl;
    private URL initialResourceUrl;
    private String lastModelLocation;
    private M resource;

    private Class<?> contextClass;

    private Properties overrides = new Properties();
    private Properties defaults = new Properties();
    private Properties defaultVariants = null;

    private String defaultVariantsLocation;

    private Map<String, HasResourceMetadata> imports = new HashMap<String, HasResourceMetadata>();

    private ExtensibleURLClassLoader loader = new ExtensibleURLClassLoader(getClass()
            .getClassLoader());

    private PropertyPlaceholderHelper pph = new PropertyPlaceholderHelper("${", "}", null, false);

    private static Map<ResourceHandle, Object> cache = new WeakHashMap<ResourceHandle, Object>();

    private Map<String, String> autoOverrides = new HashMap<>();
    
    /**
     * Maintain a reference to the handle for the currently loaded resource. This handle is used
     * as a key in the resource cache and makes sure that the resource is not removed from the
     * cache while it is still considered as "loaded" by this provider.
     */
    @SuppressWarnings("unused")
    private ResourceHandle resourceHandle;

    {
        init();
    }

    protected void init()
    {
        setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
        setDefault(ARTIFACT_URI,
                "mvn:${" + GROUP_ID + "}:${" + ARTIFACT_ID + "}:${" + VERSION + "}");
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
        setContextClass(aObject.getClass());

        // Experimental: allow forcing any resource provider to allow sharing its resource
        // Enable sharing the model between multiple instances of this AE. This is an experimental
        // parameter for advanced users. Sharing the model can lead to unexpected results because
        // some parameters affect the model when it is initialized. Thus, only the settings from the
        // first instance using the model will initialize the model, but not the next instance which
        // finds the already-loaded model and simply reuses it. Sharing models can also lead to
        // unexpected results or crashes in multi-threaded environments.
        // Allowed values: "true" and "false"
        String key = "dkpro.core.resourceprovider.sharable." + aObject.getClass().getName();
        if (System.getProperty(key) != null) {
            setDefault(SHARABLE, System.getProperty(key));
        }
    }

    /**
     * Set a class which can be used to try finding a Maven POM from which resource version
     * information could be extracted.
     *
     * @param aClass
     *            a context class, usually the class creating the provider.
     */
    public void setContextClass(Class<?> aClass)
    {
        contextClass = aClass;
        setDefault(PACKAGE, contextClass.getPackage().getName().replace('.', '/'));
    }
    
    public Class<?> getContextClass()
    {
        return contextClass;
    }

    public Map<String, String> getAutoOverrides()
    {
        return autoOverrides;
    }
    
    public void addAutoOverride(String aParameter, String aProperty)
    {
        autoOverrides.put(aParameter, aProperty);
    }
    
    public void applyAutoOverrides(Object aObject)
    {
        for (Field field : ReflectionUtil.getFields(aObject)) {
            if (ConfigurationParameterFactory.isConfigurationParameterField(field)) {
                String parameterName = ConfigurationParameterFactory
                        .getConfigurationParameterName(field);
                
                // Check if there is an auto-override for this parameter
                String property = autoOverrides.get(parameterName);
                
                if (property != null) {
                    try {
                        Object value = FieldUtils.readField(field, aObject, true);
                        setOverride(property, value != null ? value.toString() : null);
                    }
                    catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }
    
    protected List<URL> getPomUrlsForClass(String aModelGroup, String aModelArtifact,
            Class<?> aClass)
        throws IOException
    {
        if (aClass == null) {
            throw new IllegalArgumentException("No context class specified");
        }
        
        // Try to determine the location of the POM file belonging to the context object
        URL url = contextClass.getResource(contextClass.getSimpleName() + ".class");
        String classPart = contextClass.getName().replace(".", "/") + ".class";
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
                String pomPattern = base + "META-INF/maven/" + aModelGroup + "/"
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
            String moduleArtifactId = aModelArtifact.split("-")[0];
            String pomPattern = base + "META-INF/maven/" + aModelGroup + "/" + moduleArtifactId +
                    "*/pom.xml";
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
     * @return the version of the required model.
     */
    protected String getModelVersionFromMavenPom(String aModelGroup, String aModelArtifact,
            Class<?> aClass)
        throws IOException
    {
        List<URL> urls = getPomUrlsForClass(aModelGroup, aModelArtifact, contextClass);

        for (URL pomUrl : urls) {
            // Parse the POM
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
                    if (
                            StringUtils.equals(dep.getGroupId(), aModelGroup) && 
                            StringUtils.equals(dep.getArtifactId(), aModelArtifact)
                    ) {
                        return dep.getVersion();
                    }
                }
            }
        }

        // Bail out if no version information for that artifact could be found
        throw new IllegalStateException("No version information found.");
    }

    /**
     * For use in test cases.
     * @return the location of the model.
     * @throws IOException
     *             if the language-dependent default variants location is set but cannot be read.
     */
    protected String getModelLocation()
        throws IOException
    {
        return getModelLocation(null);
    }

    protected String getLastModelLocation()
    {
        return lastModelLocation;
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
            if (modelLocation.startsWith(NOT_REQUIRED)) {
                resourceUrl = null;
                initialResourceUrl = null;
                if (modelLocationChanged) {
                    log.info("Producing resource from thin air");
                    loadResource(props);
                }
            }
            else {
                URL initialUrl;
                try {
                    if (FORCE_AUTO_LOAD.equals(System.getProperty(PROP_REPO_OFFLINE))) {
                        throw new IOException("Auto-loading forced");
                    }
                    initialUrl = resolveLocation(modelLocation, loader, null);
                }
                catch (IOException e) {
                    if (modelLocationChanged) {
                        // Try resolving the dependency and adding the stuff to the loader
                        Properties resolved = props;
                        try {
                            resolved = resolveDependency(props);
                            initialUrl = resolveLocation(modelLocation, loader, null);
                        }
                        catch (Throwable re) {
                            throw handleResolvingError(re, lastModelLocation, resolved);
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
                    if (resourceUrl == null) {
                        initialResourceUrl = null;
                        if (modelLocationChanged) {
                            log.info("Producing resource from thin air");
                            loadResource(props);
                        }
                    } 
                    else {
                        loadMetadata();
                        if (initialResourceUrl.equals(resourceUrl)) {
                            log.info("Producing resource from " + resourceUrl);
                        }
                        else {
                            log.info("Producing resource from [" + resourceUrl + "] redirected from ["
                                    + initialResourceUrl + "]");
                        }
                        loadResource(props);
                    }
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
        // If at some point the location is marked as not required return null.
        while (url != null && url.getPath().endsWith(".properties")) {
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
            else if (redirect.startsWith(NOT_REQUIRED)) {
                url = null;
            } 
            else {
                url = resolveLocation(redirect, loader, null);
            }
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


    @SuppressWarnings("unchecked")
    protected synchronized void loadResource(Properties aProperties) throws IOException
    {
        boolean sharable = "true".equals(aProperties.getProperty(SHARABLE, "false"));

        ResourceHandle handle = null;
        resource = null;

        // Check the cache
        if (sharable) {
            // We need to scan the cache manually because in the end we need to keep a reference
            // to exactly the same key object that was used to store the resource in the cache.
            handle = new ResourceHandle(getClass(), resourceUrl != null ? resourceUrl.toString()
                    : null);
            for (Entry<ResourceHandle, Object> e : cache.entrySet()) {
                if (handle.equals(e.getKey())) {
                    resourceHandle = e.getKey();
                    resource = (M) e.getValue();
                    log.info("Used resource from cache");
                }
            }
        }

        // If there was nothing in the cache or if the cache is disabled, produce new
        if (resource == null) {
            StopWatch sw = new StopWatch();
            sw.start();
            resource = produceResource(resourceUrl);
            sw.stop();
            log.info("Producing resource took " + sw.getTime() + "ms");

            // If cache is enabled, update the cache
            if (sharable) {
                cache.put(handle, resource);
                resourceHandle = handle;
            }
        }
    }

    /**
     * Tries to figure out which artifact contains the desired resource, tries to acquire it and
     * add it to the loader. The dependencyManagement information from the POM of the caller is
     * taken into account if possible.
     *
     * @param aProps the properties.
     * @throws IOException if dependencies cannot be resolved.
     * @throws IllegalStateException if
     */
    private Properties resolveDependency(Properties aProps)
        throws IOException, IllegalStateException
    {
        String artifactUri = null;

        Properties resolved = new Properties(aProps);
        
        // Try to get model version from POM if it has not been set explicitly yet
        if (
                resolved.getProperty(ARTIFACT_URI, "").contains("${" + VERSION + "}") && 
                isNull(resolved.getProperty(VERSION))
        ) {
            String groupId = pph.replacePlaceholders(aProps.getProperty(GROUP_ID), resolved);
            String artifactId = pph.replacePlaceholders(aProps.getProperty(ARTIFACT_ID), resolved);
            try {
                // If the version is to be auto-detected, then we must have a groupId and artifactId
                resolved.put(VERSION,
                        getModelVersionFromMavenPom(groupId, artifactId, contextClass));
            }
            catch (Throwable e) {
                log.error("Unable to obtain version from POM", e);
                // Ignore - this will be tried and reported again later by handleResolvingError
            }
        }

        // Fetch the artifact URI from the properties
        Set<String> names = aProps.stringPropertyNames();
        if (names.contains(ARTIFACT_URI)) {
            artifactUri = pph.replacePlaceholders(aProps.getProperty(ARTIFACT_URI), resolved);
        }

        // Register files with loader
        if (artifactUri != null) {
            if (artifactUri.startsWith("mvn:")) {
                try {
                    String[] parts = artifactUri.split(":");
                    String groupId = parts[1];
                    String artifactId = parts[2];
                    String version = parts[3];
                    
                    List<File> files = resolveWithIvy(groupId, artifactId, version);
                    for (File file : files) {
                        loader.addURL(file.toURI().toURL());
                    }
                }
                catch (ParseException e) {
                    throw new IllegalStateException(e);
                }
            }
            else {
                throw new IOException("Unknown URI format: [" + artifactUri + "]");
            }
        }
        
        return resolved;
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
     *
     * @param aGroupId the group ID.
     * @param aArtifactId the artifact ID.
     * @param aVersion the version
     * @return a list of dependencies.
     * @throws ParseException if Ivy settings cannot be parsed.
     * @throws IOException if the dependencies cannot be resolved.
     */
    private List<File> resolveWithIvy(String aGroupId, String aArtifactId, String aVersion)
        throws ParseException, IOException
    {
        if ("true".equals(System.getProperty(PROP_REPO_OFFLINE))) {
            log.debug("Offline mode active - attempt to download missing resource automatically "
                    + "is skipped.");
            return Collections.emptyList();
        }

        // Configure Ivy
        Message.setDefaultLogger(new ApacheCommonsLoggingAdapter(log));
        IvySettings ivySettings = new IvySettings();
        ivySettings.loadDefault();
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
        if (!aProps.getProperty(ARTIFACT_URI, "").contains("$")) {
            sb.append("Unable to load the model from the artifact ["
                    + aProps.getProperty(ARTIFACT_URI) + "]");
        }
        else if (names.contains(ARTIFACT_ID) && names.contains(GROUP_ID)) {
            // Fetch the groupdId/artifactId/version from the properties
            String artifactId = pph.replacePlaceholders(aProps.getProperty(ARTIFACT_ID), aProps);
            String groupId = pph.replacePlaceholders(aProps.getProperty(GROUP_ID), aProps);
            String version = pph.replacePlaceholders(aProps.getProperty(VERSION, ""), aProps);
            
            if (isBlank(version)) {
                sb.append("I was unable to determine which version of the desired model is "
                        + "compatible with this component.");
            }
            else {
                sb.append("\nPlease make sure that [").append(artifactId).append(']');
                if (StringUtils.isNotBlank(version)) {
                    sb.append(" version [").append(version).append(']');
                }

                sb.append(" is on the classpath.\n");
            }
            
            // Tell user how to add model dependency
            sb.append("If the version shown here is not available, try a recent version.\n");
            sb.append('\n');
            sb.append("If you are using Maven, add the following dependency to your pom.xml file:\n");
            sb.append('\n');
            sb.append("<dependency>\n");
            sb.append("  <groupId>").append(groupId).append("</groupId>\n");
            sb.append("  <artifactId>").append(artifactId).append("</artifactId>\n");
            sb.append("  <version>").append(version).append("</version>\n");
            sb.append("</dependency>\n");
            sb.append('\n');
            sb.append("Please consider that the model you are trying to use may not be publicly\n");
            sb.append("distributable. Please refer to the DKPro Core User Guide for instructions\n");
            sb.append("on how to package non-redistributable models.");
        }
                                    

        if (NOT_REQUIRED.equals(aLocation)) {
            return new IOException("Unable to load resource: " + sb.toString(), aCause);
        }
        else {
            return new IOException("Unable to load resource [" + aLocation + "]: " + sb.toString(),
                    aCause);
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

    public ExtensibleURLClassLoader getClassLoader()
    {
        return loader;
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

        // Load default variants if available and not already loaded
        if ((defaultVariants == null) && (defaultVariantsLocation != null)) {
            String dvl = pph.replacePlaceholders(defaultVariantsLocation, overriddenValues);
            setDefaultVariants(PropertiesLoaderUtils.loadAllProperties(dvl));
        }

        // Apply default variant
        String language = overriddenValues.getProperty(LANGUAGE);
        if ((defaultVariants != null)) {
            if (defaultVariants.containsKey(language)) {
                defaultValues.setProperty(VARIANT, defaultVariants.getProperty(language));
            }
            else if (defaultVariants.containsKey(CATCH_ALL)) {
                defaultValues.setProperty(VARIANT, defaultVariants.getProperty(CATCH_ALL));
            }
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
     *
     * @param aTarget the properties to merge into.
     * @param aSource the properties to merge from.
     */
    protected void mergeProperties(Properties aTarget, Properties aSource)
    {
        for (Object key : aSource.keySet()) {
            if (!aTarget.containsKey(key)) {
                aTarget.put(key, aSource.get(key));
            }
        }
    }

    private static final class ResourceHandle
    {
        private String url;

        private Class<?> owner;

        public ResourceHandle(Class<?> aOwner, String aUrl)
        {
            owner = aOwner;
            url = aUrl;
        }

        public String getUrl()
        {
            return url;
        }

        public Class<?> getOwner()
        {
            return owner;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((owner == null) ? 0 : owner.hashCode());
            result = (prime * result) + ((url == null) ? 0 : url.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ResourceHandle other = (ResourceHandle) obj;
            if (owner == null) {
                if (other.owner != null) {
                    return false;
                }
            }
            else if (!owner.equals(other.owner)) {
                return false;
            }
            if (url == null) {
                if (other.url != null) {
                    return false;
                }
            }
            else if (!url.equals(other.url)) {
                return false;
            }
            return true;
        }
    }
    
    public static final class ArtifactCoordinates
    {
        private String groupId;
        private String artifactId;
        private String version;
        
        public ArtifactCoordinates(String aGroupId, String aArtifactId, String aVersion)
        {
            super();
            groupId = aGroupId;
            artifactId = aArtifactId;
            version = aVersion;
        }

        public String getGroupId()
        {
            return groupId;
        }

        public void setGroupId(String aGroupId)
        {
            groupId = aGroupId;
        }

        public String getArtifactId()
        {
            return artifactId;
        }

        public void setArtifactId(String aArtifactId)
        {
            artifactId = aArtifactId;
        }

        public String getVersion()
        {
            return version;
        }

        public void setVersion(String aVersion)
        {
            version = aVersion;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
            result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
            result = prime * result + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ArtifactCoordinates other = (ArtifactCoordinates) obj;
            if (artifactId == null) {
                if (other.artifactId != null) {
                    return false;
                }
            }
            else if (!artifactId.equals(other.artifactId)) {
                return false;
            }
            if (groupId == null) {
                if (other.groupId != null) {
                    return false;
                }
            }
            else if (!groupId.equals(other.groupId)) {
                return false;
            }
            if (version == null) {
                if (other.version != null) {
                    return false;
                }
            }
            else if (!version.equals(other.version)) {
                return false;
            }
            return true;
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

        @Override
        public URL getResource(String name)
        {
            if (FORCE_AUTO_LOAD.equals(System.getProperty(PROP_REPO_OFFLINE))) {
                URL url = findResource(name);
                if (url == null) {
                    url = super.getResource(name);
                }
                return url;
            }
            else {
                return super.getResource(name);
            }
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
        {
            if (FORCE_AUTO_LOAD.equals(System.getProperty(PROP_REPO_OFFLINE))) {
                synchronized (getClassLoadingLock(name)) {
                    // First, check if the class has already been loaded
                    Class<?> c = findLoadedClass(name);
                    if (c == null) {
                        try {
                            c = findClass(name);
                        }
                        catch (ClassNotFoundException e) {
                            // ClassNotFoundException thrown if class not found
                        }
                        
                        if (c == null) {
                            c = super.loadClass(name, false);
                        }
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
            }
            else {
                return super.loadClass(name, resolve);
            }
        }
    }
}
