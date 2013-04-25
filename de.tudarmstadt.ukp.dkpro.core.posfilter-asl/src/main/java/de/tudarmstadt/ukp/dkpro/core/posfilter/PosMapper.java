/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.posfilter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Maps existing POS tags from one tagset to another using a user provided properties file.
 *
 * @author Erik-Lân Do Dinh
 *
 */

@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})

public class PosMapper
	extends JCasAnnotator_ImplBase
{
	/**
	 * A properties file containing POS tagset mappings.
	 */
	public static final String PARAM_MAPPING_FILE = "mappingFile";
	@ConfigurationParameter(name = PARAM_MAPPING_FILE, mandatory = true)
	private File mappingFile;

	/**
	 * A properties file containing mappings from the new tagset to (fully qualified) DKPro POS
	 * classes.<br>
	 * If such a file is not supplied, the DKPro POS classes stay the same regardless of the new POS
	 * tag value, and only the value is changed.
	 */
	public static final String PARAM_DKPRO_MAPPING_LOCATION = "dkproMappingLocation";
	@ConfigurationParameter(name = PARAM_DKPRO_MAPPING_LOCATION, mandatory = false)
	private String dkproMappingLocation;

	private Properties posMap;
	private MappingProvider mappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		posMap = new Properties();
		Reader reader = null;
		try {
			reader = new FileReader(mappingFile);
			posMap.load(reader);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}

		if (dkproMappingLocation != null) {
			mappingProvider = new MappingProvider();
			mappingProvider.setDefault(MappingProvider.LOCATION, dkproMappingLocation);
			mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
			mappingProvider.setDefault("tagger.tagset", "default");
			// mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
			// mappingProvider.addImport("tagger.tagset", modelProvider);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		if (mappingProvider != null) {
			CAS cas = aJCas.getCas();
			mappingProvider.configure(cas);

			for (Token t : JCasUtil.select(aJCas, Token.class)) {
				POS oldPos = t.getPos();
				String newTag = posMap.getProperty(oldPos.getPosValue());

				// replace the POS if the value differs (i.e. if the old value has a mapping)
				if (newTag != null) {
					Type type = mappingProvider.getTagType(newTag);
					int begin = oldPos.getBegin();
					int end = oldPos.getEnd();

					POS newPos = (POS) cas.createAnnotation(type, begin, end);
					newPos.setPosValue(newTag);

					oldPos.removeFromIndexes();
					newPos.addToIndexes();
					t.setPos(newPos);
				}
			}
		}
		// if we don't have a MappingProvider, we only re-set the tags and not the classes
		else {
			for (POS pos : JCasUtil.select(aJCas, POS.class)) {
				String newTag = posMap.getProperty(pos.getPosValue());
				if (newTag != null) {
					pos.setPosValue(newTag);
				}
			}
		}
	}
}
