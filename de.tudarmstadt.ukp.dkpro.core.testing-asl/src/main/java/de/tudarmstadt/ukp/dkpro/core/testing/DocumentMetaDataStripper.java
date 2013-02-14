package de.tudarmstadt.ukp.dkpro.core.testing;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Removes fields from the document meta data which may be different depending on the machine a 
 * test is run on.
 * 
 * @author Richard Eckart de Castilho
 */
public class DocumentMetaDataStripper
	extends JCasAnnotator_ImplBase
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		try {
			DocumentMetaData meta = DocumentMetaData.get(aJCas);
			meta.setDocumentBaseUri(null);
			meta.setDocumentUri(null);
			meta.setCollectionId(null);
		}
		catch (IllegalArgumentException e) {
			// No metadata in the CAS.
		}
	}
}
