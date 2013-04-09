package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * CAS-configurable provider produces a resource from a stream instead of an URL. The provider
 * implementation does not have to care about opening/closing the stream.
 * 
 * @author Richard Eckart de Castilho
 *
 * @param <M> the type of resource to produce.
 */
public abstract class CasConfigurableStreamProviderBase<M>
	extends CasConfigurableProviderBase<M>
{
	@Override
	protected M produceResource(URL aUrl)
		throws IOException
	{
		InputStream is = null;
		try {
			is = aUrl.openStream();
			return produceResource(is);
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		finally {
			closeQuietly(is);
		}
	}

	protected abstract M produceResource(InputStream aStream) throws Exception;

}
