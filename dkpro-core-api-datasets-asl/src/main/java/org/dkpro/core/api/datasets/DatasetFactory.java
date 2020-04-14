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
package org.dkpro.core.api.datasets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.dkpro.core.api.datasets.internal.ActionDescriptionImpl;
import org.dkpro.core.api.datasets.internal.ArtifactDescriptionImpl;
import org.dkpro.core.api.datasets.internal.DatasetDescriptionImpl;
import org.dkpro.core.api.datasets.internal.LicenseDescriptionImpl;
import org.dkpro.core.api.datasets.internal.LoadedDataset;
import org.dkpro.core.api.datasets.internal.actions.Action_ImplBase;
import org.dkpro.core.api.datasets.internal.actions.Explode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class DatasetFactory
{
    public static final String PROP_DATASET_VERIFICATION_POLICY = "dkpro.dataset.verification.policy";
    
    private static final DatasetValidationPolicy defaultVerificationPolicy = DatasetValidationPolicy
            .valueOf(System.getProperty(PROP_DATASET_VERIFICATION_POLICY,
                    DatasetValidationPolicy.STRICT.name()));
    
    private Map<String, DatasetDescriptionImpl> datasets;
    
    private final Map<String, Class<? extends Action_ImplBase>> actionRegistry;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Path cacheRoot;
    
    private ClassLoader classLoader;
    
    {
        actionRegistry = new HashMap<>();
        actionRegistry.put("explode", Explode.class);
    }

    public DatasetFactory()
    {
        // Nothing to do
    }

    public DatasetFactory(File aCacheRoot)
    {
        this(aCacheRoot.toPath());
    }

    public DatasetFactory(Path aCacheRoot)
    {
        this(aCacheRoot, null);
    }

    public DatasetFactory(Path aCacheRoot, ClassLoader aClassLoader)
    {
        cacheRoot = aCacheRoot;
        classLoader = aClassLoader;
    }

    public Path getCacheRoot()
    {
        return cacheRoot;
    }

    public List<String> listIds()
        throws IOException
    {
        return unmodifiableList(new ArrayList<>(registry().keySet()));
    }
    
    public DatasetDescription getDescription(String aId)
        throws IOException
    {
        return registry().get(aId);
    }

    public Dataset load(String aId)
        throws IOException
    {
        return load(aId, defaultVerificationPolicy);
    }
    
    public Dataset load(String aId, DatasetValidationPolicy aPolicy)
        throws IOException
    {
        DatasetDescription desc = getDescription(aId);
        if (desc == null) {
            throw new IllegalArgumentException("Unknown dataset [" + aId + "]");
        }
        materialize(desc, aPolicy);
        return new LoadedDataset(this, desc);
    }

    private Map<String, DatasetDescriptionImpl> registry()
        throws IOException
    {
        // If no cache was set, create one and make sure to clean it up on exit
        if (cacheRoot == null) {
            cacheRoot = Files.createTempDirectory("dkpro-dataset-cache");
            cacheRoot.toFile().deleteOnExit();
        }

        // Load datesets only once
        if (datasets == null) {
            // Load the YAML descriptions
            datasets = loadFromYaml();
        }

        return datasets;
    }
    
    private Map<String, DatasetDescriptionImpl> loadFromYaml()
        throws IOException
    {
        // Scan for locators
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] locators = resolver
                .getResources("classpath:META-INF/org.dkpro.core/datasets.txt");

        // Read locators
        Set<String> patterns = new LinkedHashSet<>();
        for (Resource locator : locators) {
            try (InputStream is = locator.getInputStream()) {
                IOUtils.lineIterator(is, "UTF-8").forEachRemaining(l -> patterns.add(l));
            }
        }
        
        // Scan for YAML dataset descriptions
        List<Resource> resources = new ArrayList<>();
        for (String pattern : patterns) {
            for (Resource r : resolver.getResources(pattern)) {
                resources.add(r);
            }
        }

        // Configure YAML deserialization
        Constructor datasetConstructor = new Constructor(DatasetDescriptionImpl.class);
        TypeDescription datasetDesc = new TypeDescription(DatasetDescriptionImpl.class);
        datasetDesc.putMapPropertyType("artifacts", String.class, ArtifactDescriptionImpl.class);
        datasetDesc.putListPropertyType("licenses", LicenseDescriptionImpl.class);
        datasetConstructor.addTypeDescription(datasetDesc);
        TypeDescription artifactDesc = new TypeDescription(ArtifactDescriptionImpl.class);
        artifactDesc.putListPropertyType("actions", ActionDescriptionImpl.class);
        datasetConstructor.addTypeDescription(artifactDesc);
        Yaml yaml = new Yaml(datasetConstructor);
        
        // Ensure that there is a fixed order (at least if toString is correctly implemented)
        Collections.sort(resources, (a, b) -> {
            return a.toString().compareTo(b.toString());
        });
        
        // Load the YAML descriptions
        Map<String, DatasetDescriptionImpl> sets = new LinkedHashMap<>();
        for (Resource res : resources) {
            log.debug("Loading [{}]", res);
            try (InputStream is = res.getInputStream()) {
                String id = FilenameUtils.getBaseName(res.getFilename());
                DatasetDescriptionImpl ds = yaml.loadAs(is, DatasetDescriptionImpl.class);
                ds.setId(id);
                ds.setOwner(this);
                
                // Inject artifact names into artifacts
                for (Entry<String, ArtifactDescription> e : ds.getArtifacts().entrySet()) {
                    ((ArtifactDescriptionImpl) e.getValue()).setName(e.getKey());
                    ((ArtifactDescriptionImpl) e.getValue()).setDataset(ds);
                }
                
                sets.put(ds.getId(), ds);
            }
        }
        
        log.debug("Loaded [{}] dataset description", sets.size());

        
        return sets;
    }

    public Path resolve(DatasetDescription aDataset)
    {
        return cacheRoot.resolve(aDataset.getId());
    }

    /**
     * Get the cache location for the given artifact.
     */
    private Path resolve(DatasetDescription aDataset, ArtifactDescription aArtifact)
    {
        if (aArtifact.isShared()) {
            // Shared artifacts stored in a folder named by their hash
            // Prefere SHA1 for the time being to avoid users having to re-download too much as
            // we slowly switch over to SHA512
            if (aArtifact.getSha1() != null) {
                return cacheRoot.resolve("shared").resolve(aArtifact.getSha1())
                        .resolve(aArtifact.getName());
            }
            else {
                return cacheRoot.resolve("shared").resolve(aArtifact.getSha512())
                        .resolve(aArtifact.getName());
            }
        }
        else {
            // Unshared artifacts are stored in the dataset folder
            return resolve(aDataset).resolve(aArtifact.getName());
        }
    }
    
    /**
     * Verify/download/update artifact in cache. Execute post-download actions.
     */
    private void materialize(DatasetDescription aDataset, DatasetValidationPolicy aPolicy)
        throws IOException
    {
        Path root = resolve(aDataset);
        Collection<ArtifactDescription> artifacts = aDataset.getArtifacts().values();
        
        // First validate if local copies are still up-to-date
        boolean reload = false;
        packageValidationLoop: for (ArtifactDescription artifact : artifacts) {
            Path cachedFile = resolve(aDataset, artifact);
            if (!Files.exists(cachedFile)) {
                continue;
            }
            
            if (artifact.getUrl() != null) {
                boolean verificationOk = checkDigest(cachedFile, artifact);
                if (!verificationOk) {
                    reload = true;
                    break packageValidationLoop;
                }
            }
        }
        
        // If any of the packages are outdated, clear the cache and download again
        if (reload) {
            if (!DatasetValidationPolicy.DESPERATE.equals(aPolicy)) {
                log.info("Clearing local cache for [{}]", root);
                FileUtils.deleteQuietly(root.toFile());
            }
            else {
                log.info("DESPERATE policy in effect. Not clearing local cache for [{}]", root);
            }
        }
        
        for (ArtifactDescription artifact : artifacts) {
            if (artifact.getText() != null) {
                materializeEmbeddedText(artifact);
            }
            else if (artifact.getUrl() != null) {
                try {
                    materializeRemoteFileArtifact(artifact, aPolicy);
                }
                catch (Exception e) {
                    if (artifact.isOptional()) {
                        if (log.isDebugEnabled()) {
                            log.warn("Skipping optional artifact [{}]",
                                    artifact.getName(), e);
                        }
                        else {
                            log.warn("Skipping optional artifact [{}]: {}",
                                    artifact.getName(), e.getMessage());
                        }
                    }
                    else {
                        throw e;
                    }
                }
            }
        }
                 
        // Perform a post-fetch action such as unpacking
        Path postActionCompleteMarker = resolve(aDataset).resolve(".postComplete");
        if (!Files.exists(postActionCompleteMarker)) {
            for (ArtifactDescription artifact : artifacts) {
                Path cachedFile = resolve(aDataset, artifact);

                List<ActionDescription> actions = artifact.getActions();
                if (actions != null && !actions.isEmpty()) {
                    try {
                        for (ActionDescription action : actions) {
                            log.info("Post-download action [{}]", action.getAction());
                            Class<? extends Action_ImplBase> implClass = actionRegistry
                                    .get(action.getAction());
                            
                            if (implClass == null) {
                                throw new IllegalStateException(
                                        "Unknown or unsupported action [" + action.getAction() + "]");
                            }
                            
                            Action_ImplBase impl = implClass.newInstance();
                            impl.apply(action, aDataset, artifact, cachedFile);
                        }
                    }
                    catch (IllegalStateException | IOException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            Files.createFile(postActionCompleteMarker);
        }
    }
    
    private void materializeRemoteFileArtifact(ArtifactDescription artifact,
            DatasetValidationPolicy aPolicy)
        throws IOException
    {
        Path cachedFile = resolve(artifact.getDataset(), artifact);
        
        if (Files.exists(cachedFile)) {
            return;
        }
        
        Files.createDirectories(cachedFile.getParent());
        
        ResourceLoader resourceLoader = new DefaultResourceLoader(classLoader);
        Resource res = resourceLoader.getResource(artifact.getUrl());
        if (!res.exists()) {
            throw new FileNotFoundException("Resource not found at [" + artifact.getUrl() + "]");
        }

        if (log.isDebugEnabled()) {
            log.debug("Fetching [{}] from [{}]", cachedFile, artifact.getUrl());
        }
        else {
            log.info("Fetching [{}]", cachedFile);
        }
        
        URLConnection connection = res.getURL().openConnection();
        connection.setRequestProperty("User-Agent", "Java");
        
        try (InputStream is = connection.getInputStream()) {
            Files.copy(is, cachedFile);
        }

        boolean verificationOk = checkDigest(cachedFile, artifact);
        if (!verificationOk) {
            switch (aPolicy) {
            case STRICT:
                throw new IOException("Checksum verification failed on [" + cachedFile
                        + "] STRICT policy in effect. Bailing out.");
            case CONTINUE:
                log.warn(
                        "Checksum verification failed on [{}] CONTINUE policy in effect. Ignoring mismatch.",
                        cachedFile);
                break;
            case DESPERATE:
                log.warn(
                        "Checksum verification failed on [{}] DESPERATE policy in effect. Ignoring mismatch.",
                        cachedFile);
                break;
            default:
                throw new IllegalArgumentException("Unknown policy: " + aPolicy);
            }
        }
    }
    
    private void materializeEmbeddedText(ArtifactDescription artifact) throws IOException
    {
        Path cachedFile = resolve(artifact.getDataset(), artifact);
        
        // Check if file on disk corresponds to text stored in artifact description
        if (Files.exists(cachedFile)) {
            String text = FileUtils.readFileToString(cachedFile.toFile(), UTF_8);
            text = StringUtils.normalizeSpace(text);
            if (StringUtils.normalizeSpace(artifact.getText()).equals(text)) {
                return;
            }
        }
        
        Files.createDirectories(cachedFile.getParent());
        
        log.info("Creating [{}]", cachedFile);
        try (Writer out = Files.newBufferedWriter(cachedFile, StandardCharsets.UTF_8)) {
            out.write(artifact.getText());
        }
    }

    private InputStream getDigestInputStream(Path aFile, ArtifactDescription aArtifact)
        throws IOException
    {
        switch (aArtifact.getVerificationMode()) {
        case BINARY:
            return Files.newInputStream(aFile);
        case TEXT:
            String text = FileUtils.readFileToString(aFile.toFile(), UTF_8);
            text = StringUtils.normalizeSpace(text);
            return IOUtils.toInputStream(text, UTF_8);
        default:
            throw new IllegalArgumentException(
                    "Unknown verification mode [" + aArtifact.getVerificationMode() + "]");
        }
    }
    
    private boolean checkDigest(Path aFile, ArtifactDescription aArtifact) throws IOException
    {
        MessageDigest sha1;
        MessageDigest sha512;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
            sha512 = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        
        try (InputStream is = getDigestInputStream(aFile, aArtifact)) {
            DigestInputStream sha1Filter = new DigestInputStream(is, sha1);
            DigestInputStream sha512Filter = new DigestInputStream(sha1Filter, sha512);
            IOUtils.copy(sha512Filter, new NullOutputStream());
            String sha1Hash = new String(Hex.encodeHex(sha1Filter.getMessageDigest().digest()));
            String sha512Hash = new String(Hex.encodeHex(sha512Filter.getMessageDigest().digest()));
            
            if (aArtifact.getSha1() != null) {
                if (!sha1Hash.equals(aArtifact.getSha1())) {
                    log.info(
                            "Local SHA1 hash mismatch for artifact [{}] in dataset [{}] - expected [{}] - actual [{}] (mode: {})",
                            aArtifact.getName(), aArtifact.getDataset().getId(),
                            aArtifact.getSha1(), sha1Hash, aArtifact.getVerificationMode());
                    return false;
                }
                else if (aArtifact.getSha512() == null) {
                    log.info(
                            "Local SHA1 hash verified for artifact [{}] in dataset [{}] (mode: {})",
                            aArtifact.getName(), aArtifact.getDataset().getId(),
                            aArtifact.getVerificationMode());
                }
            }

            if (aArtifact.getSha512() != null) {
                if (!sha512Hash.equals(aArtifact.getSha512())) {
                    log.info(
                            "Local SHA512 hash mismatch for artifact [{}] in dataset [{}] - expected [{}] - actual [{}] (mode: {})",
                            aArtifact.getName(), aArtifact.getDataset().getId(),
                            aArtifact.getSha512(), sha512Hash, aArtifact.getVerificationMode());
                    return false;
                }
                else {
                    log.info(
                            "Local SHA512 hash verified for artifact [{}] in dataset [{}] (mode: {})",
                            aArtifact.getName(), aArtifact.getDataset().getId(),
                            aArtifact.getVerificationMode());
                }
            }
            else {
                log.info(
                        "No SHA512 hash for artifact [{}] in dataset [{}] - it is recommended to add it: [{}]",
                        aArtifact.getName(), aArtifact.getDataset().getId(), sha512Hash);
            }

            return true;
        }
    }
}
