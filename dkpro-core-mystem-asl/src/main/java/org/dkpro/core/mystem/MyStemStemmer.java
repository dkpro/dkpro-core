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
package org.dkpro.core.mystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathAnnotatorBase;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * This MyStem stemmer implementation only works with the Russian language.
 */
@Component(OperationType.STEMMER)
@ResourceMetaData(name = "MyStem Stemmer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability("ru")
@TypeCapability(inputs = { 
    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" }, 
    outputs = {
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem" })
public class MyStemStemmer extends FeaturePathAnnotatorBase {

    private RuntimeProvider runtimeProvider;

	@Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        runtimeProvider = new RuntimeProvider(
                "classpath:/org/dkpro/core/mystem/bin/");
    }
	
    @Override
    protected Set<String> getDefaultPaths()
    {
        return Collections.singleton(Token.class.getName());
    }	
	
    @Override
    protected void generateAnnotations(JCas aJCas) 
            throws FeaturePathException, AnalysisEngineProcessException {
    	
    	PlatformDetector pd = new PlatformDetector();
        String platform = pd.getPlatformId();
        getLogger().info("Load binary for platform: [" + platform + "]");

        File executableFile = getExecutable();
        
        File inputFile = prepareInputfile(aJCas);
        File outputFile = prepareOutputFile();

        List<String> cmd = new ArrayList<>();
        cmd.add(executableFile.getAbsolutePath());
        cmd.add("-n"); // one word per line output
        cmd.add(inputFile.getAbsolutePath());
        cmd.add(outputFile.getAbsolutePath());
        
        runProcess(cmd);
        
        try {
			List<String> l = FileUtils.readLines(outputFile, "utf-8");
			l.forEach(x->System.err.println(x));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        inputFile.delete();
        outputFile.delete();
        
//
//        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(),
//                getEncoding()));
//        reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
//                getEncoding()));

    }

	private void runProcess(List<String> cmd) throws AnalysisEngineProcessException {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO();
			pb.command(cmd);
			pb.start();
		} catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private File prepareOutputFile() throws AnalysisEngineProcessException {
		try {
			File file = FileUtil.createTempFile("mystemOutput" + System.currentTimeMillis(), ".txt");
			file.deleteOnExit();
			return file;
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private File prepareInputfile(JCas aJCas) throws AnalysisEngineProcessException {
		File inputTmp = null;
		try {
			inputTmp = FileUtil.createTempFile("mystemInput" + System.currentTimeMillis(), ".txt");

			try (BufferedWriter wrt = new BufferedWriter(new FileWriterWithEncoding(inputTmp, "utf-8"))) {
				Iterator<Token> iterator = JCasUtil.select(aJCas, Token.class).iterator();
				while (iterator.hasNext()) {
					Token next = iterator.next();
					wrt.write(next.getCoveredText());
					if(iterator.hasNext()) {
						wrt.write(" ");
					}
				}
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

		if (inputTmp != null) {
			inputTmp.deleteOnExit();
		}
		return inputTmp;
	}

	private File getExecutable() throws AnalysisEngineProcessException {
		File exec=null;
		try {
			exec = runtimeProvider.getFile("mystem");
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		return exec;
	}
}
