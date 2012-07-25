/*******************************************************************************
 * Copyright 2008
 * Richard Eckart de Castilho
 * Institut für Sprach- und Literaturwissenschaft
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
package de.tudarmstadt.ukp.dkpro.core.castransformation.alignment;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public
class WeakHashSet<E>
extends AbstractSet<E>
implements Iterable<E>, Set<E>
{
	private final static Object present = new Object();

	private final Map<E, Object> data = new WeakHashMap<E, Object>();

	@Override
	public
	boolean add(
			final E o)
	{
		final int beforeSize = size();
		data.put(o, present);
		return beforeSize != size();
	}

	public
	void remove(
			final E o)
	{
		data.remove(o);
	}

	@Override
	public
	Iterator<E> iterator()
	{
		return data.keySet().iterator();
	}

	@Override
	public
	int size()
	{
		return data.size();
	}
}
