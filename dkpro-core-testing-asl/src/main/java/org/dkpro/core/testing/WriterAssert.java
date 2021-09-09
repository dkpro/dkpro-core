/*
 * Copyright 2019
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
package org.dkpro.core.testing;

import static org.apache.commons.lang3.StringUtils.replaceOnce;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.canParameterBeSet;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.getParameterSettings;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.setParameter;
import static org.dkpro.core.api.parameter.ComponentParameters.PARAM_TARGET_LOCATION;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.LifeCycleUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.FileAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.StringAssert;
import org.assertj.core.internal.Failures;
import org.assertj.core.util.Files;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriterAssert
    extends AbstractAssert<WriterAssert, AnalysisEngineDescription>
{
    private Logger LOG = LoggerFactory.getLogger(getClass());
    
    public static final String VAR_TARGET = "${TARGET}";

    // See JCasFileWriter_ImplBase
    private static final String PARAM_SINGULAR_TARGET = "singularTarget";
    private static final String PARAM_STRIP_EXTENSION = "stripExtension";
    
    private JCasIterable jcasIterable;
    
    private Object requestedTargetLocation;
    private boolean singularTargetAnnounced = false;
    private boolean stripExtension = true;

    public WriterAssert(AnalysisEngineDescription aWriter)
    {
        super(aWriter, WriterAssert.class);
        
        isNotNull();
        
        if (!actual.isPrimitive()) {
            failWithMessage("Writer cannot be an aggregate. Use `usingEngine` if you need to add "
                    + "additional analysis engines or secondary writers.");
        }
    }

    public static WriterAssert assertThat(Class<? extends AnalysisComponent> aWriterClass,
            Object... aConfigurationData)
        throws ResourceInitializationException
    {
        return assertThat(createEngineDescription(aWriterClass, aConfigurationData));
    }
    
    public static WriterAssert assertThat(AnalysisEngineDescription aWriter)
    {
        return new WriterAssert(aWriter);
    }
    
    public WriterAssert consuming(JCasIterable aJCasIterable)
    {
        jcasIterable = aJCasIterable;
        
        return this;
    }
    
    /**
     * By default, the original extension is stripped from the original file name and the writer's
     * extension is then added. By calling this method, the original extension is retained and 
     * in addition the writer's extension is added.
     * 
     * @return the assert for chaining.
     */
    public WriterAssert keepOriginalExtension()
    {
        stripExtension = false;
        
        return this;
    }
    
    /**
     * Configure the writer to write to the given file.
     * 
     * @param aLocation
     *            a location.
     * @return the assert for chaining.
     * @see #writingTo(String)
     */
    public WriterAssert writingTo(File aLocation)
    {
        return _writingTo(aLocation);
    }

    /**
     * Configure the writer to write to the given location. The target location can either be
     * configured using this method or by setting {@link ComponentParameters#PARAM_TARGET_LOCATION}
     * in the reader description.
     * 
     * @param aLocation
     *            a location.
     * @return the assert for chaining.
     */
    public WriterAssert writingTo(String aLocation)
    {
        return _writingTo(aLocation);
    }

    /**
     * Configure the writer to write all output into a single file at the given location. The
     * location is the final file name, not a folder name. The singular target flag can either be
     * configured using this method or by setting {@code PARAM_SINGULAR_TARGET} to {@code true}
     * in the writer description. This method can also be used to indicate that a component
     * implicitly writes a singular target, even if it does not support
     * {@code PARAM_SINGULAR_TARGET}. This affects e.g. how {@link #asFiles()} interprets the
     * target location.
     * 
     * @param aLocation
     *            a location.
     * @return the assert for chaining.
     * @see #writingTo(String)
     */
    public WriterAssert writingToSingular(String aLocation)
    {
        singularTargetAnnounced = true;

        // If the parameter can be set on the component, set it - otherwise assume that the
        // component implicitly creates a singular target from the target location
        if (canParameterBeSet(actual, PARAM_SINGULAR_TARGET)) {
            Map<String, Object> writerParameters = getParameterSettings(actual);
            if (Boolean.TRUE.equals(writerParameters.get(PARAM_SINGULAR_TARGET))) {
                failWithMessage("PARAM_SINGULAR_TARGET already set in the writer parameters.");

            }
            setParameter(actual, PARAM_SINGULAR_TARGET, true);
        }

        return _writingTo(aLocation);
    }

    public WriterAssert _writingTo(Object aLocation)
    {
        isNotNull();
        
        if (requestedTargetLocation != null) {
            failWithMessage("Target location has already been set to [%s]",
                    requestedTargetLocation);
        }
        
        requestedTargetLocation = aLocation;
        
        if (!canParameterBeSet(actual, PARAM_TARGET_LOCATION)) {
            failWithMessage("Parameter [%s] cannot be set on writer [%s]",
                    PARAM_TARGET_LOCATION, actual.getImplementationName());
        }
        
        // Is the target location defined in the writer parameters?
        Map<String, Object> writerParameters = getParameterSettings(actual);
        if (writerParameters.containsKey(PARAM_TARGET_LOCATION)) {
            throw Failures.instance().failure(String.format(
                    "Target location [%s] already defined in the writer parameters.",
                    writerParameters.get(PARAM_TARGET_LOCATION)));
        }

        requestedTargetLocation = resolvePlaceholders(requestedTargetLocation);
        
        setParameter(actual, PARAM_TARGET_LOCATION, requestedTargetLocation);
        
        return this;
    }
    
    protected static <T> T resolvePlaceholders(T aLocation)
    {
        if (aLocation instanceof String) {
            String location = (String) aLocation;
            
            if (location.startsWith(VAR_TARGET)) {
                if (DkproTestContext.get() == null) {
                    throw Failures.instance()
                            .failure(String.format("Cannot substitute `%s` - no %s found.",
                                    VAR_TARGET, DkproTestContext.class.getSimpleName()));
                }
                
//                File contextOutputFolder = new File("target/test-output/"
//                        + DkproTestContext.get().getTestWorkspaceFolderName());                
//                if (contextOutputFolder.exists()) {
//                    FileUtils.deleteQuietly(contextOutputFolder);
//                }

                File contextOutputFolder;
                try {
                    contextOutputFolder = DkproTestContext.get().getTestOutputFolder();
                } catch (IOException e) {
                    throw Failures.instance()
                            .failure("Cannot get test output folder\n" + e.getMessage());
                }
                
                return (T) replaceOnce(location, VAR_TARGET, contextOutputFolder.getPath());
            }
        }
        
        return aLocation;
    }
    
    /**
     * Infers the actual target location.
     * 
     * @return the target location.
     */
    protected Object targetLocation()
    {
        Map<String, Object> writerParameters = getParameterSettings(actual);
        
        // Was the target location set explicitly?
        if (requestedTargetLocation == null) {
            // Is the target location known from the writer parameters?
            if (writerParameters.containsKey(PARAM_TARGET_LOCATION)) {
                return writerParameters.get(PARAM_TARGET_LOCATION);
            }
            
            // Can we get one from the DKPro Core test context?
            if (DkproTestContext.get() != null) {
                writingTo(VAR_TARGET);
                return getParameterSettings(actual).get(PARAM_TARGET_LOCATION);
            }
            
            // No success?
            throw Failures.instance()
                    .failure(String.format("Unable to determine target location. Use a @Rule "
                            + DkproTestContext.class.getSimpleName()
                            + " or set the location using `writingTo()"));
        }
        else {
            return requestedTargetLocation;
        }
    }
    
    protected List<File> listTargetLocationFiles()
    {
        Object location = targetLocation();

        if (location instanceof String) {
            location = new File((String) location);
        }
        
        if (location instanceof File) {
            File fileLocation = (File) location;
            
            if (!fileLocation.exists()) {
                throw Failures.instance().failure(
                        String.format("Target location [%s] does not exist.", fileLocation));
            }
            
            if (isSingularTarget()) {
                return Arrays.asList(fileLocation);
            }
            fileLocation.getAbsoluteFile().listFiles();
            return Arrays.asList(fileLocation.listFiles());
        }

        throw Failures.instance().failure(String
                .format("Target location [%s] cannot be interpreted as a directory.", location));
    }
    
    protected boolean isSingularTarget()
    {
        Boolean isSingular = null;
        Map<String, Object> writerParameters = getParameterSettings(actual);
        
        if (Boolean.TRUE.equals(writerParameters.get(PARAM_SINGULAR_TARGET))) {
            isSingular = true;
        }
        
        if (isSingular == null) {
            File targetLocation = new File((String) writerParameters.get(PARAM_TARGET_LOCATION));
            if (targetLocation.exists()) {
                isSingular = true;
                if (targetLocation.isDirectory()) {
                    isSingular = false;
                }
            }
        }
        
        if (isSingular == null) {
            isSingular = singularTargetAnnounced;
        }
        
        return isSingular;
    }
    
    protected void configureWriter()
    {
        // By default, we strip the original extension when writing to avoid extension accumulation
        if (stripExtension && canParameterBeSet(actual, PARAM_STRIP_EXTENSION)) {
            setParameter(actual, PARAM_STRIP_EXTENSION, true);
        }
        
        // If the target location is specified in the writer descriptor only, replace any variable
        // in it if possible
        if (canParameterBeSet(actual, PARAM_TARGET_LOCATION)) {
            Map<String, Object> writerParameters = getParameterSettings(actual);
            if (writerParameters.containsKey(PARAM_TARGET_LOCATION)) {
                Object location = writerParameters.get(PARAM_TARGET_LOCATION);
                setParameter(actual, PARAM_TARGET_LOCATION, resolvePlaceholders(location));
            }
        }
    }

    /**
     * Gets the output written to the target location as a string. This method fails if more than
     * one output file was created or if no output was created.
     * <p>
     * This method triggers the execution of the text pipeline.
     * 
     * @return the output written to the target location as a string. 
     */
    public StringAssert outputAsString()
    {
        return outputAsString(null);
    }
    
    /**
     * Gets the output written to the target location as a string.
     * <p>
     * This method triggers the execution of the text pipeline.
     * 
     * @param aPathSuffix
     *            a path/filename suffix which uniquely identifies the requested output file.
     * 
     * @return the output written to the target location as a string.
     */
    public StringAssert outputAsString(String aPathSuffix)
    {
        run();
        
        List<File> files = listTargetLocationFiles();
        
        if (aPathSuffix != null) {
            files = files.stream()
                    .filter(file -> file.getPath().endsWith(aPathSuffix))
                    .collect(Collectors.toList());
        }
        
        if (files.isEmpty()) {
            if (aPathSuffix != null) {
                failWithMessage("Not output file ending in [%s] found at target location [%s].",
                        aPathSuffix, requestedTargetLocation);
            }
            else {
                failWithMessage("Not output file found at target location [%s].",
                        requestedTargetLocation);
            }
        }

        if (files.size() > 1) {
            if (aPathSuffix != null) {
                failWithMessage(
                        "Expected single output file ending in [%s] at target location [%s] but "
                                + "found multiple: %s.",
                        aPathSuffix, requestedTargetLocation, files);
            }
            else {
                failWithMessage(
                        "Expected single output file at target location [%s] but found multiple: %s.",
                        requestedTargetLocation, files);
            }
            
        }

        return new StringAssert(Files.contentOf(files.get(0), StandardCharsets.UTF_8));
    }
    /**
     * Gets the output written to the target location as a file. This method fails if more than
     * one output file was created or if no output was created.
     * <p>
     * This method triggers the execution of the text pipeline.
     * 
     * @return the output written to the target location as a file. 
     */
    public FileAssert asFile()
    {
        run();
        
        List<File> files = listTargetLocationFiles();
        
        if (files.isEmpty()) {
            failWithMessage("Not output found at target location [%s].", requestedTargetLocation);
        }

        if (files.size() > 1) {
            failWithMessage(
                    "Expected single output file at target location [%s] but found multiple: %s.",
                    requestedTargetLocation, files);
        }

        return new FileAssert(files.get(0));
    }
    /**
     * Gets the files written to the target location.
     * <p>
     * This method triggers the execution of the text pipeline.
     * 
     * @return the files written to the target location. 
     */
    public ListAssert<File> asFiles()
    {
        run();
        
        return new ListAssert<>(listTargetLocationFiles());
    }
    
    protected void run()
    {
        configureWriter();
        
        // Obtains the actual target location, also ensuring that it was actually defined.
        Object actualTargetLocation = targetLocation();
        
        LOG.debug("Writing to target location  : {}", actualTargetLocation);
        LOG.debug("- is singular target        : {}", isSingularTarget());
        
        AnalysisEngine writer = null;
        try {
            writer = createEngine(actual);
            
            for (JCas jcas : jcasIterable) {
                writer.process(jcas);
            }
            
            LifeCycleUtil.collectionProcessComplete(writer);
        }
        catch (Exception e) {
            AssertionError error = Failures.instance().failure(String.format(
                    "Pipeline execution failed: %s", e.getMessage()));
            error.initCause(e);
            throw error;
        }
        finally {
            LifeCycleUtil.destroy(writer);
        }
    }
    
    public static AnalysisEngineDescription simpleJCasDumper(File targetLocation)
        throws ResourceInitializationException, IOException
    {
        
        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class, 
                CasDumpWriter.PARAM_TARGET_LOCATION, 
                        DkproTestContext.get().getTestOutputFile(targetLocation),
                CasDumpWriter.PARAM_SORT, true);
        
        return writer;
    }
}
