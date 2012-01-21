package de.tudarmstadt.ukp.dkpro.core.api.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

public abstract class JCasResourceCollectionReader_ImplBase
	extends ResourceCollectionReaderBase
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
	
	protected void initCas(JCas aJCas, Resource aResource)
	{
		super.initCas(aJCas.getCas(), aResource);
	}
	
	protected void initCas(JCas aJCas, Resource aResource, String aQualifier)
	{
		super.initCas(aJCas.getCas(), aResource, aQualifier);
	}
}
