/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.api.io;

import static org.apache.uima.util.Level.FINE;
import static org.apache.uima.util.Level.INFO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Logger;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.uimafit.component.CasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Base class for collection readers that plan to access resources on the file system or in the
 * classpath or basically anywhere where Spring can resolve them. ANT-style patterns are supported
 * to include or exclude particular resources.
 * <p>
 * Example of a hypothetic <code>FooReader</code> that should read only files ending in
 * <code>.foo</code> from in the directory <code>foodata</code> or any subdirectory thereof:
 * <pre>
 * CollectionReader reader = createCollectionReader(FooReader.class, getDKProTypeSystem(),
 *     FileSetCollectionReaderBase.PARAM_LANGUAGE, "en",
 *     FileSetCollectionReaderBase.PARAM_PATH,     new File("some/path").getAbsolutePath(),
 *     FileSetCollectionReaderBase.PARAM_PATTERNS, new String[] { "[+]foodata&#47;**&#47;*.foo" });
 * </pre>
 *
 * @see <a href="http://ant.apache.org/manual/dirtasks.html#patterns">Documentation of <b>ant</b> patterns</a>
 *
 * @author Richard Eckart de Castilho
 * @since 1.0.6
 */
public abstract class ResourceCollectionReaderBase
	extends CasCollectionReader_ImplBase
{
	public static final String INCLUDE_PREFIX = "[+]";
	public static final String EXCLUDE_PREFIX = "[-]";

	public static final String PARAM_PATH = "Path";
	@ConfigurationParameter(name=PARAM_PATH, mandatory=false)
	private String path;

	public static final String PARAM_PATTERNS = "Patterns";
	@ConfigurationParameter(name=PARAM_PATTERNS, mandatory=true)
	private String[] patterns;

	/**
	 * Use the default excludes.
	 */
	public static final String PARAM_USE_DEFAULT_EXCLUDES = "UseDefaultExcludes";
	@ConfigurationParameter(name=PARAM_USE_DEFAULT_EXCLUDES, mandatory=true, defaultValue="true")
	private boolean useDefaultExcludes;

	public static final String PARAM_INCLUDE_HIDDEN = "IncludeHidden";
	@ConfigurationParameter(name=PARAM_INCLUDE_HIDDEN, mandatory=true, defaultValue="false")
	private boolean includeHidden;

	/**
	 * Name of optional configuration parameter that contains the language of
	 * the documents in the input directory. If specified, this information will
	 * be added to the CAS.
	 */
	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name=PARAM_LANGUAGE, mandatory=false)
	private String language;

	private int completed;
	private Collection<Resource> resources;
	private Iterator<Resource> resourceIterator;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		// Parse the patterns and inject them into the FileSet
		List<String> includes = new ArrayList<String>();
		List<String> excludes = new ArrayList<String>();
		for (int i = 0; i < patterns.length; i++) {
			if (patterns[i].startsWith(INCLUDE_PREFIX)) {
				includes.add(patterns[i].substring(INCLUDE_PREFIX.length()));
			}
			else if (patterns[i].startsWith(EXCLUDE_PREFIX)) {
				excludes.add(patterns[i].substring(EXCLUDE_PREFIX.length()));
			}
			else {
				throw new ResourceInitializationException(new IllegalArgumentException(
						"Patterns have to start with " + INCLUDE_PREFIX + " or " + EXCLUDE_PREFIX
								+ "."));
			}
		}

		// These should be the same as documented here: http://ant.apache.org/manual/dirtasks.html
		if (useDefaultExcludes) {
			excludes.add("**/*~");
			excludes.add("**/#*#");
			excludes.add("**/.#*");
			excludes.add("**/%*%");
			excludes.add("**/._*");
			excludes.add("**/CVS");
			excludes.add("**/CVS/**");
			excludes.add("**/.cvsignore");
			excludes.add("**/SCCS");
			excludes.add("**/SCCS/**");
			excludes.add("**/vssver.scc");
			excludes.add("**/.svn");
			excludes.add("**/.svn/**");
			excludes.add("**/.DS_Store");
			excludes.add("**/.git");
			excludes.add("**/.git/**");
			excludes.add("**/.gitattributes");
			excludes.add("**/.gitignore");
			excludes.add("**/.gitmodules");
			excludes.add("**/.hg");
			excludes.add("**/.hg/**");
			excludes.add("**/.hgignore");
			excludes.add("**/.hgsub");
			excludes.add("**/.hgsubstate");
			excludes.add("**/.hgtags");
			excludes.add("**/.bzr");
			excludes.add("**/.bzr/**");
			excludes.add("**/.bzrignore");
		}

		try {
			if (path == null) {
				ListIterator<String> i = includes.listIterator();
				while (i.hasNext()) {
					String p = i.next();
					if (!p.contains(":")) {
						i.set("file:"+p);
					}
				}
				i = excludes.listIterator();
				while (i.hasNext()) {
					String p = i.next();
					if (!p.contains(":")) {
						i.set("file:"+p);
					}
				}
			}
			else if (!path.contains(":")) {
				path = "file:"+path;
			}

			resources = scan(path, includes, excludes);

			// Get the iterator that will be used to actually traverse the FileSet.
			resourceIterator = resources.iterator();

			getUimaContext().getLogger().log(INFO,
					"Found [" + resources.size() + "] resources to be read");
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	protected Collection<Resource> getResources()
	{
		return resources;
	}

	protected Iterator<Resource> getResourceIterator()
	{
		return resourceIterator;
	}

	protected Resource nextFile()
	{
		try {
			return resourceIterator.next();
		}
		finally {
			completed++;
		}
	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(completed, resources.size(), "file") };
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return resourceIterator.hasNext();
	}

	protected Collection<Resource> scan(String aBase, Collection<String> aIncludes,
			Collection<String> aExcludes)
		throws IOException
	{
		Logger log = getUimaContext().getLogger();
		String base = (aBase != null) ? aBase+"/" : "";
		Collection<String> includes;
		Collection<String> excludes;

		if (aIncludes == null || aIncludes.size() == 0) {
			includes = Collections.singleton("**/*");
		}
		else {
			includes = aIncludes;
		}

		if (aExcludes == null || aExcludes.size() == 0) {
			excludes = Collections.emptySet();
		}
		else {
			excludes = aExcludes;
		}

		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		AntPathMatcher matcher = new AntPathMatcher();
		List<Resource> result = new ArrayList<Resource>();

		// E.g. a classpath location may resolve to multiple locations. Thus we collect all the
		// locations to which the base resolves.
		org.springframework.core.io.Resource[] rBases = resolver.getResources(base);
		Set<String> rsBases = new HashSet<String>();
		for (org.springframework.core.io.Resource rBase : rBases) {
			URI uri = getUri(rBase, false);
			if (uri != null) {
				rsBases.add(uri.toString());
			}
		}

		// Now we process the include patterns one after the other
		for (String include : includes) {
			// We resolve the resources for each base+include combination.
			org.springframework.core.io.Resource[] resourceList = resolver.getResources(base+include);
			nextResource: for (org.springframework.core.io.Resource resource : resourceList) {
				URI uResource = getUri(resource, true);
				if (uResource == null) {
					continue;
				}
				String sResource = uResource.toString();

				// Determine the resolved base for this location
				String matchBase = null;
				if (base.length() > 0) {
					for (String b : rsBases) {
						if (!sResource.startsWith(b)) {
							continue;
						}

						// This is the base... at least we define it as being the base.
						// FIXME there may be other bases. Have to define a policy if most or least
						// specific base should be chosen.
						matchBase = b;
						break;
					}

					if (matchBase == null) {
						// This should not happen...
						throw new IllegalStateException("No base found for location ["+sResource+"]");
					}
				}
				else {
					// If no base is set, no need to go through the trouble of finding one.
					matchBase = base;
				}

				// To figure out if the resolved location is excluded, we try to find the part
				// of the location that was determined by the include pattern by substracting the
				// resolved base locations one after the other and looking if the result is
				// matched by the exclude.
				if (excludes != null) {
					for (String exclude : excludes) {
							String rest = sResource.substring(matchBase.length());
							if (matcher.match(exclude, rest)) {
								if (log.isLoggable(FINE)) {
									log.log(FINE, "Excluded: "+sResource);
								}
								continue nextResource;
							}
					}
				}

				// If the resource was not excluded, we add it to the results.
				String p = sResource.substring(matchBase.length());
				Resource r = new Resource(base + p, base, resource.getURI(), matchBase, p, resource);
				result.add(r);
			}
		}

		Collections.sort(result, new Comparator<Resource>()
		{
			@Override
			public int compare(Resource aO1, Resource aO2)
			{
				return aO1.location.compareTo(aO2.location);
			}
		});

		return result;
	}

	/**
	 * Get the URI of the given resource.
	 *
	 * @param aResource a resource
	 * @param aFileOrDir if true try to return only files, if false try to return only dirs
	 * @return the URI of the resource
	 */
	private URI getUri(org.springframework.core.io.Resource aResource, boolean aFileOrDir)
		throws IOException
	{
		try {
			File file = aResource.getFile();

			// Exclude hidden files/dirs if requested
			if (file.isHidden() && !includeHidden) {
				return null;
			}

			// Return only dirs or files...
			if ((aFileOrDir && file.isFile()) || (!aFileOrDir && file.isDirectory())) {
				return aResource.getFile().toURI();
			}
			else {
				return null;
			}
		}
		catch (IOException e) {
			return aResource.getURI();
		}
	}

	/**
	 * Initialize the {@DocumentMetaData}. This must be called before setting the
	 * document text, otherwise the end feature of this annotation will not be set correctly.
	 */
	protected void initCas(CAS aCas, Resource aResource)
	{
		try {
			// Set the document metadata
			DocumentMetaData docMetaData = new DocumentMetaData(aCas.getJCas());
			docMetaData.setDocumentTitle(new File(aResource.getPath()).getName());
			docMetaData.setDocumentUri(aResource.getResolvedUri().toString());
			docMetaData.setDocumentId(aResource.getPath());
			if (aResource.getBase() != null) {
				docMetaData.setDocumentBaseUri(aResource.getResolvedBase());
				docMetaData.setCollectionId(aResource.getResolvedBase());
			}
			docMetaData.addToIndexes();

			// Set the document language
			aCas.setDocumentLanguage(language);
		}
		catch (CASException e) {
			// This should not happen.
			throw new RuntimeException(e);
		}
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	public static class Resource
	{
		private String location;
		private String base;
		private URI resolvedUri;
		private String resolvedBase;
		private String path;
		private org.springframework.core.io.Resource resource;

		public Resource(String aLocation, String aBase, URI aResolvedUri, String aResolvedBaseUri,
				String aPath, org.springframework.core.io.Resource aResource)
		{
			super();
			location = aLocation;
			base = aBase;
			resolvedUri = aResolvedUri;
			resolvedBase = aResolvedBaseUri;
			path = aPath;
			resource = aResource;
		}

		public String getLocation()
		{
			return location;
		}

		public String getBase()
		{
			return base;
		}

		public URI getResolvedUri()
		{
			return resolvedUri;
		}

		public String getResolvedBase()
		{
			return resolvedBase;
		}

		public String getPath()
		{
			return path;
		}

		public org.springframework.core.io.Resource getResource()
		{
			return resource;
		}

		public InputStream getInputStream()
			throws IOException
		{
			return resource.getInputStream();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((resolvedUri == null) ? 0 : resolvedUri.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Resource other = (Resource) obj;
			if (resolvedUri == null) {
				if (other.resolvedUri != null) {
					return false;
				}
			}
			else if (!resolvedUri.equals(other.resolvedUri)) {
				return false;
			}
			return true;
		}
	}
}
