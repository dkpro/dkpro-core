package de.tudarmstadt.ukp.dkpro.core.api.io;

import java.io.IOException;

import org.apache.tools.ant.types.resources.FileResource;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

public abstract class JCasFileSetCollectionReader_ImplBase
	extends FileSetCollectionReaderBase
{
	// This method should not be overwritten. Overwrite getNext(JCas) instead.
	@Override
	public final void getNext(CAS cas)
		throws IOException, CollectionException
	{
		try {
			getNext(cas.getJCas());
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}
	}

	/**
	 * Subclasses implement this method rather than {@link #getNext(CAS)}
	 * 
	 * @param aJCas
	 * @throws IOException
	 * @throws CollectionException
	 */
	public abstract void getNext(JCas aJCas)
		throws IOException, CollectionException;
	
	protected void initCas(JCas aJCas, FileResource aResource)
	{
		super.initCas(aJCas.getCas(), aResource);
	}
}
