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
