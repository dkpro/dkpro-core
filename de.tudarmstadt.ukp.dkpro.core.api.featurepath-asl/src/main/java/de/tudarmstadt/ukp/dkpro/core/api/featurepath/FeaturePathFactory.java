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
package de.tudarmstadt.ukp.dkpro.core.api.featurepath;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

public class FeaturePathFactory
{
	/**
	 * Select annotation/value pairs matching the given feature path. The path has to start with a
	 * fully qualified type name - the anchor type - followed by a feature path. This function can
	 * be used in extended for-loops.
	 *
	 * @param aCas the CAS to search in.
	 * @param aPath the full feature path.
	 * @return annotation/value pairs.
	 * @throws FeaturePathException
	 */
	public static Iterable<Entry<AnnotationFS, String>> select(CAS aCas, String aPath)
		throws FeaturePathException
	{
		// Separate Typename and featurepath
		String[] segments = aPath.split("/", 2);
		String typeName = segments[0];

		Type t = aCas.getTypeSystem().getType(typeName);
		if (t == null) {
			throw new IllegalStateException("Type [" + typeName + "] not found in type system");
		}

		String path = segments.length > 1 ? segments[1] : "";
		return iterable(FeaturePathIterator.create(aCas, t, path));
	}

	/**
	 * Select annotation/value pairs matching the given feature path. The path has to start with
	 * a fully qualified type name followed by a feature path. This function can be used in
	 * extended for-loops.
	 *
	 * @param <T> the anchor type.
	 * @param aJCas the JCas to search in.
	 * @param aAnchor the anchor type.
	 * @param aPath a feature path relative to the anchor type.
	 * @return annotation/value pairs.
	 * @throws FeaturePathException
	 */
	public static <T extends Annotation> Iterable<Entry<T, String>> select(JCas aJCas, Class<T> aAnchor,
			String aPath)
		throws FeaturePathException
	{
		return iterable(FeaturePathIterator.create(aJCas, aAnchor, aPath));
	}

	private static <T> Iterable<T> iterable(final Iterator<T> aIterator) {
		return new Iterable<T>()
		{
			@Override
			public
			Iterator<T> iterator()
			{
				return aIterator;
			}
		};
	}

	public static class FeaturePathIterator<T extends AnnotationFS>
	implements Iterator<Entry<T, String>>
	{
		private final FeaturePathInfo fp;
		private final FSIterator<T> iterator;

		public FeaturePathIterator(FSIterator<T> aIterator, FeaturePathInfo aFp)
		{
			fp = aFp;
			iterator = aIterator;
		}

		@Override
		public boolean hasNext()
		{
			return iterator.hasNext();
		}

		@Override
		public Entry<T, String> next()
		{
			final T anno = iterator.next();
			return new Entry<T, String>()
			{
				@Override
				public T getKey()
				{
					return anno;
				}

				@Override
				public String getValue()
				{
					return fp.getValue(anno);
				}

				@Override
				public String setValue(String arg0)
				{
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		public static <T extends Annotation> FeaturePathIterator<T> create(JCas aJCas,
				Class<T> aAnchor, String aPath)
			throws FeaturePathException
		{
			Type t = JCasUtil.getType(aJCas, aAnchor);
			FSIterator<T> iterator = ((AnnotationIndex<T>) aJCas.getAnnotationIndex(t)).iterator();
			final FeaturePathInfo fp = new FeaturePathInfo();
			fp.initialize(aPath);
			return new FeaturePathIterator<T>(iterator, fp);
		}

		@SuppressWarnings("unchecked")
		public static <T extends AnnotationFS> FeaturePathIterator<T> create(CAS aCas,
				Type aType, String aPath)
			throws FeaturePathException
		{
			FSIterator<T> iterator = ((AnnotationIndex<T>) aCas.getAnnotationIndex(aType)).iterator();
			final FeaturePathInfo fp = new FeaturePathInfo();
			fp.initialize(aPath);
			return new FeaturePathIterator<T>(iterator, fp);
		}
}
}
