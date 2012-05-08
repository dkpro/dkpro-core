package de.tudarmstadt.ukp.dkpro.core.api.resources;

import java.util.Properties;

import org.apache.uima.cas.CAS;

public abstract class CasConfigurableProviderBase<M> extends ResourceObjectProviderBase<M>
{
	private String language;
	
	public void configure(CAS aCas)
	{
		language = aCas.getDocumentLanguage();
		super.configure();
	}

	@Override
	protected Properties getProperties()
	{
		Properties props = new Properties();
		if (language != null) {
			props.setProperty(LANGUAGE, language);
		}
		
		return props;
	}
}
