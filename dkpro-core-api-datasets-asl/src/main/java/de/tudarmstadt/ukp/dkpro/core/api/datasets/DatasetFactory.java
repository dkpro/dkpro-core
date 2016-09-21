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
package de.tudarmstadt.ukp.dkpro.core.api.datasets;

import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.ActionDescriptionImpl;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.ArtifactDescriptionImpl;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.DatasetDescriptionImpl;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.LicenseDescriptionImpl;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.LoadedDataset;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.actions.Action_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.actions.Explode;

public class DatasetFactory
{
    private Map<String, DatasetDescriptionImpl> datasets;
    
    private Map<String, Class<? extends Action_ImplBase>> actionRegistry;

    private final Log LOG = LogFactory.getLog(getClass());

    private Path cacheRoot;
    
    {
        actionRegistry = new HashMap<>();
        actionRegistry.put("explode", Explode.class);
    }

    public DatasetFactory()
    {
        // Nothing to do
    }

    public DatasetFactory(Path aCacheRoot)
    {
        cacheRoot = aCacheRoot;
    }

    public DatasetFactory(File aCacheRoot)
    {
        this(aCacheRoot.toPath());
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
        DatasetDescription desc = getDescription(aId);
        if (desc == null) {
            throw new IllegalArgumentException("Unknown dataset [" + aId + "]");
        }
        materialize(desc);
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
            LOG.debug("Loading [" + res + "]");
            try (InputStream is = res.getInputStream()) {
                String id = FilenameUtils.getBaseName(res.getFilename());
                DatasetDescriptionImpl ds = yaml.loadAs(is, DatasetDescriptionImpl.class);
                ds.setId(id);
                ds.setOwner(this);
                
                // Inject artifact names into artifacts
                for (Entry<String, ArtifactDescription> e : ds.getArtifacts().entrySet()) {
                    ((ArtifactDescriptionImpl) e.getValue()).setName(e.getKey());
                }
                
                sets.put(ds.getId(), ds);
            }
        }
        
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
            // Shared artifacts stored in a folder named by their SHA1
            return cacheRoot.resolve("shared").resolve(aArtifact.getSha1())
                    .resolve(aArtifact.getName());
        }
        else {
            // Unshared artifacts are stored in the dataset folder
            return resolve(aDataset).resolve(aArtifact.getName());
        }
    }
    
    /**
     * Verify/download/update artifact in cache. Execute post-download actions.
     */
    private void materialize(DatasetDescription aDataset)
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
            
            if (artifact.getSha1() != null) {
                String actual = getDigest(cachedFile, "SHA1");
                if (!artifact.getSha1().equals(actual)) {
                    LOG.info("Local SHA1 hash mismatch on [" + cachedFile + "] - expected ["
                            + artifact.getSha1() + "] - actual [" + actual + "]");
                    reload = true;
                    break packageValidationLoop;
                }
                else {
                    LOG.info("Local SHA1 hash verified on [" + cachedFile + "] - [" + actual + "]");
                }
            }
        }
        
        // If any of the packages are outdated, clear the cache and download again
        if (reload) {
            LOG.info("Clearing local cache for [" + root + "]");
            FileUtils.deleteQuietly(root.toFile());
        }
        
        for (ArtifactDescription artifact : artifacts) {
            Path cachedFile = resolve(aDataset, artifact);
            
            if (Files.exists(cachedFile)) {
                continue;
            }

            
            if (artifact.getText() != null) {
                Files.createDirectories(cachedFile.getParent());
                
                LOG.info("Creating [" + cachedFile + "]");
                try (Writer out = Files.newBufferedWriter(cachedFile, StandardCharsets.UTF_8)) {
                    out.write(artifact.getText());
                }
            }
            
            if (artifact.getUrl() != null) {
                Files.createDirectories(cachedFile.getParent());
                
                MessageDigest sha1;
                try {
                    sha1 = MessageDigest.getInstance("SHA1");
                }
                catch (NoSuchAlgorithmException e) {
                    throw new IOException(e);
                }
    
                URL source = new URL(artifact.getUrl());
    
                LOG.info("Fetching [" + cachedFile + "]");
                
                URLConnection connection = source.openConnection();
                connection.setRequestProperty("User-Agent", "Java");
                
                try (InputStream is = connection.getInputStream()) {
                    DigestInputStream sha1Filter = new DigestInputStream(is, sha1);
                    Files.copy(sha1Filter, cachedFile);
    
                    if (artifact.getSha1() != null) {
                        String sha1Hex = new String(
                                Hex.encodeHex(sha1Filter.getMessageDigest().digest()));
                        if (!artifact.getSha1().equals(sha1Hex)) {
                            String message = "SHA1 mismatch. Expected [" + artifact.getSha1()
                                    + "] but got [" + sha1Hex + "].";
                            LOG.error(message);
                            throw new IOException(message);
                        }
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
                            LOG.info("Post-download action [" + action.getAction() + "]");
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
                    catch (IllegalStateException e) {
                        throw e;
                    }
                    catch (IOException e) {
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
    
    private String getDigest(Path aFile, String aDigest) throws IOException
    {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(aDigest);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (InputStream is = Files.newInputStream(aFile)) {
            DigestInputStream digestFilter = new DigestInputStream(is, digest);
            IOUtils.copy(digestFilter, new NullOutputStream());
            return new String(Hex.encodeHex(digestFilter.getMessageDigest().digest()));
        }
    }
}
