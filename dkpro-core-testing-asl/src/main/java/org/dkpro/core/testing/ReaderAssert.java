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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.canParameterBeSet;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.getParameterSettings;
import static org.apache.uima.fit.factory.ConfigurationParameterFactory.setParameter;
import static org.dkpro.core.api.parameter.ComponentParameters.PARAM_SOURCE_LOCATION;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.CasIOUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.internal.Failures;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.FileCopy;
import org.dkpro.core.api.resources.FileGlob;
import org.dkpro.core.testing.IOTestRunner.Validator;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.dkpro.core.testing.validation.checks.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class ReaderAssert
    extends AbstractAssert<ReaderAssert, CollectionReaderDescription>
{
    private Logger LOG = LoggerFactory.getLogger(getClass());

    private Object requestedSourceLocation;
    
    private AnalysisEngineDescription[] engines;
    
    private boolean stripDocumentMetadata = true;
    private boolean validate = true;
    private TestOptions validationOptions = new TestOptions();
    
    private DkproTestContext testContext = new DkproTestContext();

    public ReaderAssert(CollectionReaderDescription aReader)
    {
        super(aReader, ReaderAssert.class);
        
        isNotNull();
    }

    public static ReaderAssert assertThat(Class<? extends CollectionReader> aReaderClass,
            Object... aConfigurationData)
        throws ResourceInitializationException
    {
        return assertThat(createReaderDescription(aReaderClass, aConfigurationData));
    }
    
    public static ReaderAssert assertThat(CollectionReaderDescription aReader)
    {
        return new ReaderAssert(aReader);
    }
    
    /**
     * Configure the reader to read from the given file.
     * 
     * @param aLocation
     *            a file location.
     * @return the assert for chaining.
    * @see #readingFrom(String)
      */
    public ReaderAssert readingFrom(File aLocation)
    {
        return _readingFrom(aLocation, null);
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
    
    public ReaderAssert readingFrom(String aLocation, Boolean removeRefFiles)
    {
        return _readingFrom(aLocation, removeRefFiles);
    }
        
    public ReaderAssert readingFrom(String aLocation)
    {
        return readingFrom(aLocation, null);
    }
    
    protected ReaderAssert _readingFrom(Object aLocation, Boolean removeRefFiles) 
    {
        isNotNull();
        
        if (removeRefFiles == null) {
            removeRefFiles = false;
        }
        
        if (requestedSourceLocation != null) {
            failWithMessage("Source location has already been set to [%s]",
                    requestedSourceLocation);
        }

        requestedSourceLocation = aLocation;
        
        copySourceLocationFilesToTestInputsDir(removeRefFiles);
        
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
        
//        setParameter(actual, PARAM_SOURCE_LOCATION, requestedSourceLocation);

        File paramSourceLocation = null;
        try {
            paramSourceLocation = DkproTestContext.get().getTestInputFolder();
        } catch (IOException e) {
            failWithMessage("Could not get the test inputs folder"+e.getMessage());
        }                
        File requestedSourceLocationFile = new File(requestedSourceLocation.toString());
        if (!requestedSourceLocationFile.isDirectory() &&
            !requestedSourceLocationFile.toString().contains("*")) {
            // Requested source location is a single document
            paramSourceLocation = new File(paramSourceLocation, 
                                           requestedSourceLocationFile.getName());
        }
        setParameter(actual, PARAM_SOURCE_LOCATION, paramSourceLocation);
      
        return this;
    }
    
    private void copySourceLocationFilesToTestInputsDir(Boolean removeRefFiles) 
    {     
        Path inputsDir = null;
        try {
            inputsDir = DkproTestContext.get().getTestInputFolder().toPath();
            File sourceLocation = new File(requestedSourceLocation.toString());
            File sourceLocationDir = sourceLocation;
            if (!sourceLocation.isDirectory()) {
                sourceLocationDir = sourceLocation.getParentFile();
            }
            FileCopy.copyFolder(sourceLocationDir, inputsDir.toFile());
        } catch (IOException e) {
            failWithMessage("Unable to copy files from "+requestedSourceLocation+" to test inputs directory.\n"+e.getMessage());
        }
        
        // Delete the -ref files from the inputs dir
        if (removeRefFiles) {
            String pattern = new File(inputsDir.toFile(), "*-ref*").toString();
            FileGlob.deleteFiles(pattern);
        }
    }

    public ReaderAssert deleteSourceLocationFiles(String pattern) {
        if (requestedSourceLocation != null) {
            failWithMessage("Source location has not yet been set");
        }
        
        return this;
    }
    
    public ReaderAssert usingEngines(AnalysisEngineDescription... aEngines)
    {
        isNotNull();
        
        engines = aEngines;
        
        return this;
    }

    public WriterAssert usingWriter(Class<? extends AnalysisComponent> aComponentClass,
            Object... aConfigurationData)
        throws ResourceInitializationException
    {

        AnalysisEngineDescription engDescr = createEngineDescription(aComponentClass, aConfigurationData);
        return usingWriter(engDescr);
    }
        
    public WriterAssert usingWriter(AnalysisEngineDescription aWriter)
    {
        isNotNull();
                        
        try {
            return WriterAssert.assertThat(aWriter).consuming(toJCasIterable());
        }
        catch (ResourceInitializationException e) {
            AssertionError error = Failures.instance()
                    .failure(String.format("Error constucting reading pipeline."));
            error.initCause(e);
            throw error;
        }
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
                        + DkproTestContext.get().getTestWorkspaceFolderName();
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
    
    public ListAssert<JCas> asJCasList()
    {
        List<JCas> casses = new ArrayList<>();
        
        try {
            for (JCas jcas : toJCasIterable()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                CasIOUtils.save(jcas.getCas(), bos, SerialFormat.SERIALIZED_TSI);
                CAS copy = CasCreationUtils.createCas((TypeSystemDescription) null, null, null);
                CasIOUtils.load(new ByteArrayInputStream(bos.toByteArray()), copy);
                casses.add(copy.getJCas());
            }
        }
        catch (Exception e) {
            AssertionError error = Failures.instance()
                    .failure(String.format("Pipeline execution failed: %s", e.getMessage()));
            error.initCause(e);
            throw error;
        }
        
        return new ListAssert<>(casses);
    }
    
    public JCasIterable toJCasIterable() throws ResourceInitializationException
    {
        // Obtains the actual source location, also ensuring that it was actually defined.
        Object actualSourceLocation = sourceLocation();

        LOG.debug("Reading from source location: {}", actualSourceLocation);

        List<AnalysisEngineDescription> allProcessors = new ArrayList<>();
        allProcessors.addAll(processors());
        if (engines != null) {
            allProcessors.addAll(Arrays.asList(engines));
        }

        return new JCasIterable(actual,
                allProcessors.stream().toArray(AnalysisEngineDescription[]::new));
    }
}
