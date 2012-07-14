package de.tudarmstadt.ukp.dkpro.core.api.resources;

import java.util.Properties;

/**
 * Interface for objects that provide meta data that can be imported by a ResourceObjectProvider.
 * 
 * @author Richard Eckart de Castilho
 */
public interface HasResourceMetadata
{
	Properties getResourceMetaData();
}
