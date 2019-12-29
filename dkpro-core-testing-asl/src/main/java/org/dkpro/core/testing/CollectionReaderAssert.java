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
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.canParameterBeSet;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.getParameterSettings;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.setParameter;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.dkpro.core.api.parameter.ComponentParameters.PARAM_SOURCE_LOCATION;
import static org.dkpro.core.api.parameter.ComponentParameters.PARAM_TARGET_LOCATION;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.FileAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.StringAssert;
import org.assertj.core.internal.Failures;
import org.assertj.core.util.Files;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.testing.IOTestRunner.Validator;
import org.dkpro.core.testing.validation.checks.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class CollectionReaderAssert
    extends AbstractAssert<CollectionReaderAssert, CollectionReaderDescription>
{
    public static final String VAR_TARGET = "${TARGET}";

    // See JCasFileWriter_ImplBase
    private static final String PARAM_SINGULAR_TARGET = "singularTarget";

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private Object requestedSourceLocation;
    private Object requestedTargetLocation;
    private boolean singularTargetAnnounced = false;
    
    private AnalysisEngineDescription[] engines;
    private AnalysisEngineDescription writer;
    
    private boolean stripDocumentMetadata = true;
    private boolean validate = true;
    private TestOptions validationOptions = new TestOptions();

    public CollectionReaderAssert(CollectionReaderDescription aReader)
    {
        super(aReader, CollectionReaderAssert.class);
        
        isNotNull();
    }

    public static CollectionReaderAssert assertThat(Class<? extends CollectionReader> aReaderClass,
            Object... aConfigurationData)
        throws ResourceInitializationException
    {
        return assertThat(createReaderDescription(aReaderClass, aConfigurationData));
    }
    
    public static CollectionReaderAssert assertThat(CollectionReaderDescription aReader)
    {
        return new CollectionReaderAssert(aReader);
    }
    
    /**
     * Configure the reader to read from the given file.
     * 
     * @param aLocation
     *            a file location.
     * @return the assert for chaining.
    * @see #readingFrom(String)
      */
    public CollectionReaderAssert readingFrom(File aLocation)
    {
        return _readingFrom(aLocation);
    }

    /**
     * Configure the reader to read from the given location. The source location can either be
     * configured using this method or by setting {@link ComponentParameters#PARAM_SOURCE_LOCATION}
     * in the reader description.
     * 
     * @param aLocation
     *            a location.
     * @return the assert for chaining.
     */
    public CollectionReaderAssert readingFrom(String aLocation)
    {
        return _readingFrom(aLocation);
    }

    protected CollectionReaderAssert _readingFrom(Object aLocation)
    {
        isNotNull();
        
        if (requestedSourceLocation != null) {
            failWithMessage("Source location has already been set to [%s]",
                    requestedSourceLocation);
        }

        requestedSourceLocation = aLocation;
        
        if (!canParameterBeSet(actual, PARAM_SOURCE_LOCATION)) {
            failWithMessage("Parameter [%s] cannot be set on reader [%s]",
                    PARAM_SOURCE_LOCATION, actual.getImplementationName());
        }

        // Is the source location defined in the reader parameters?
        Map<String, Object> readerParameters = getParameterSettings(actual);
        if (readerParameters.containsKey(PARAM_SOURCE_LOCATION)) {
            throw Failures.instance().failure(String.format(
                    "Source location [%s] already defined in the reader parameters.",
                    readerParameters.get(PARAM_SOURCE_LOCATION)));
        }
        
        setParameter(actual, PARAM_SOURCE_LOCATION, requestedSourceLocation);
        
        return this;
    }

    public CollectionReaderAssert usingEngines(AnalysisEngineDescription... aEngines)
    {
        isNotNull();
        
        engines = aEngines;
        
        return this;
    }

    public CollectionReaderAssert usingWriter(Class<? extends AnalysisComponent> aComponentClass,
            Object... aConfigurationData)
        throws ResourceInitializationException
    {
        return usingWriter(createEngineDescription(aComponentClass, aConfigurationData));
    }
        
    public CollectionReaderAssert usingWriter(AnalysisEngineDescription aWriter)
    {
        isNotNull();
        
        if (!aWriter.isPrimitive()) {
            failWithMessage("Writer cannot be an aggregate. Use `usingEngine` if you need to add "
                    + "additional analysis engines or secondary writers.");
        }
        
        writer = aWriter;
        
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
    public CollectionReaderAssert writingTo(File aLocation)
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
    public CollectionReaderAssert writingTo(String aLocation)
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
    public CollectionReaderAssert writingToSingular(String aLocation)
    {
        singularTargetAnnounced = true;

        // If the parameter can be set on the component, set it - otherwise assume that the
        // component implicitly creates a singular target from the target location
        if (canParameterBeSet(writer, PARAM_SINGULAR_TARGET)) {
            Map<String, Object> writerParameters = getParameterSettings(writer);
            if (Boolean.TRUE.equals(writerParameters.get(PARAM_SINGULAR_TARGET))) {
                failWithMessage("PARAM_SINGULAR_TARGET already set in the writer parameters.");

            }
            setParameter(writer, PARAM_SINGULAR_TARGET, true);
        }

        return _writingTo(aLocation);
    }

    public CollectionReaderAssert _writingTo(Object aLocation)
    {
        isNotNull();
        
        if (writer == null) {
            failWithMessage("Writer must be set first.");
        }
        
        if (requestedTargetLocation != null) {
            failWithMessage("Target location has already been set to [%s]",
                    requestedTargetLocation);
        }
        
        requestedTargetLocation = aLocation;
        
        if (!canParameterBeSet(writer, PARAM_TARGET_LOCATION)) {
            failWithMessage("Parameter [%s] cannot be set on writer [%s]",
                    PARAM_TARGET_LOCATION, writer.getImplementationName());
        }
        
        // Is the target location defined in the writer parameters?
        Map<String, Object> writerParameters = getParameterSettings(writer);
        if (writerParameters.containsKey(PARAM_TARGET_LOCATION)) {
            throw Failures.instance().failure(String.format(
                    "Target location [%s] already defined in the writer parameters.",
                    writerParameters.get(PARAM_TARGET_LOCATION)));
        }

        requestedTargetLocation = resolvePlaceholders(requestedTargetLocation);
        
        setParameter(writer, PARAM_TARGET_LOCATION, requestedTargetLocation);
        
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
                
                File contextOutputFolder = new File("target/test-output/"
                        + DkproTestContext.get().getTestOutputFolderName());
                if (contextOutputFolder.exists()) {
                    FileUtils.deleteQuietly(contextOutputFolder);
                }
                
                return (T) replaceOnce(location, VAR_TARGET, contextOutputFolder.getPath());
            }
        }
        
        return aLocation;
    }
    
    /**
     * Normally fields such as {@link DocumentMetaData#getDocumentUri()} which include the full
     * document path and which are not consistent between different test environments are cleared.
     * If this is not desired, invoke this method.
     */
    public void keepDocumentMetadata()
    {
        stripDocumentMetadata = false;
    }

    /**
     * Normally, the output of the reader is sanity-checked. If this is not desired, invoke this
     * method.
     */
    public void skipValidation()
    {
        validate = false;
    }
    
    /**
     * Skip the given checks during reader output validation.
     * 
     * @param aCheck
     *            the checks to skip.
     */
    public void skipChecks(Class<? extends Check> aCheck)
    {
        validationOptions.skipCheck(aCheck);
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
            
            return Arrays.asList(fileLocation.listFiles());
        }

        throw Failures.instance().failure(String
                .format("Target location [%s] cannot be interpreted as a directory.", location));
    }
    
    /**
     * Infers the actual source location.
     * 
     * @return the source location.
     */
    protected Object sourceLocation()
    {
        Map<String, Object> readerParameters = getParameterSettings(actual);
        
        // Was the source location set explicitly?
        if (requestedSourceLocation == null) {
            // Is the target location known from the reader parameters?
            if (readerParameters.containsKey(PARAM_SOURCE_LOCATION)) {
                return readerParameters.get(PARAM_SOURCE_LOCATION);
            }
            
            // Can we get one from the DKPro Core test context?
            if (DkproTestContext.get() == null) {
                String contextOutputFolderName = "target/test-output/"
                        + DkproTestContext.get().getTestOutputFolderName();
                readingFrom(contextOutputFolderName);
                return contextOutputFolderName;
            }
            
            // No success?
            throw Failures.instance()
                    .failure(String.format("Unable to determine source location. Use a @Rule "
                            + DkproTestContext.class.getSimpleName()
                            + " or set the location using `readingWith()"));
        }
        else {
            return requestedSourceLocation;
        }
    }
    
    /**
     * Infers the actual target location.
     * 
     * @return the target location.
     */
    protected Object targetLocation()
    {
        Map<String, Object> writerParameters = getParameterSettings(writer);
        
        // Was the target location set explicitly?
        if (requestedTargetLocation == null) {
            // Is the target location known from the writer parameters?
            if (writerParameters.containsKey(PARAM_TARGET_LOCATION)) {
                return writerParameters.get(PARAM_TARGET_LOCATION);
            }
            
            // Can we get one from the DKPro Core test context?
            if (DkproTestContext.get() != null) {
                writingTo(VAR_TARGET);
                return getParameterSettings(writer).get(PARAM_TARGET_LOCATION);
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
    
    protected List<AnalysisEngineDescription> processors() throws ResourceInitializationException
    {
        List<AnalysisEngineDescription> processors = new ArrayList<>();

        // By default we sanity-check the output of the reader.
        if (validate) {
            processors.add(createEngineDescription(Validator.class));
            Validator.options = validationOptions;
        }

        // By default, we strip the document metadata if no options are specified
        if (stripDocumentMetadata) {
            processors.add(createEngineDescription(DocumentMetaDataStripper.class));
        }
        
        return processors;
    }
    
    protected boolean isSingularTarget()
    {
        Map<String, Object> writerParameters = getParameterSettings(writer);
        
        if (Boolean.TRUE.equals(writerParameters.get(PARAM_SINGULAR_TARGET))) {
            return true;
        }
        
        return singularTargetAnnounced;
    }
    
    protected void configureWriter()
    {
        // If the target location is specified in the writer descriptor only, replace any variable
        // in it if possible
        if (canParameterBeSet(writer, PARAM_TARGET_LOCATION)) {
            Map<String, Object> writerParameters = getParameterSettings(writer);
            if (writerParameters.containsKey(PARAM_TARGET_LOCATION)) {
                Object location = writerParameters.get(PARAM_TARGET_LOCATION);
                setParameter(writer, PARAM_TARGET_LOCATION, resolvePlaceholders(location));
            }
        }
    }
    
    protected void run()
    {
        configureWriter();
        
        // Obtains the actual source and target location, also ensuring that they were actually
        // defined.
        Object actualSourceLocation = sourceLocation();
        Object actualTargetLocation = targetLocation();
        
        LOG.debug("Reading from source location: {}", actualSourceLocation);
        LOG.debug("Writing to target location  : {}", actualTargetLocation);
        LOG.debug("- is singular target        : {}", isSingularTarget());
        
        try {
            List<AnalysisEngineDescription> allProcessors = new ArrayList<>();
            allProcessors.addAll(processors());
            if (engines != null) {
                allProcessors.addAll(Arrays.asList(engines));
            }
            allProcessors.add(writer);
            
            runPipeline(actual, allProcessors.stream().toArray(AnalysisEngineDescription[]::new));
        }
        catch (Exception e) {
            AssertionError error = Failures.instance().failure(String.format(
                    "Pipeline execution failed: %s", e.getMessage()));
            error.initCause(e);
            throw error;
        }

        if (validate) {
            AssertAnnotations.assertValid(Validator.messages);
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
    @Override
    public StringAssert asString()
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
}
