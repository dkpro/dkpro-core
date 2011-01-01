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
package de.tudarmstadt.ukp.dkpro.core.io.pdf.convert;

import java.util.LinkedList;

/**
 * Cluster values into buckets. A new bucket is opened if a new value is added that differs from the
 * average value of each of the existing buckets by more than a certain threshold.
 *
 * @author Richard Eckart de Castilho
 */
public class Buckets
{
	private final LinkedList<Bucket> buckets = new LinkedList<Bucket>();
	private final double tolerance;
	private boolean modified = true;
	private Bucket cachedBest = null;

	Buckets(final double aTolerance)
	{
		tolerance = aTolerance;
	}

	public void put(final double aValue)
	{
		modified = true;

		// Empty so far
		if (buckets.size() == 0) {
			newBucket(aValue);
			return;
		}

		Bucket best = buckets.getFirst();
		double best_diff = Math.abs(best.getValue() - aValue);
		for (final Bucket b : buckets) {
			final double cur_diff = Math.abs(b.getValue() - aValue);

			// Bail out on exact match
			if (cur_diff == 0.0) {
				b.add(aValue);
				return;
			}

			// Found better match?
			if (cur_diff < best_diff) {
				best = b;
				best_diff = cur_diff;
			}
		}

		// Add to existing bucket if within tolerance, otherwise create new one
		if (best_diff < tolerance) {
			best.add(aValue);
		}
		else {
			newBucket(aValue);
		}
	}

	private void newBucket(final double aValue)
	{
		buckets.add(new Bucket(aValue));
	}

	public Bucket getBest()
	{
		if (modified == false) {
			return cachedBest;
		}

		Bucket best = buckets.getFirst();
		for (final Bucket b : buckets) {
			if (best.size() < b.size()) {
				best = b;
			}
		}
		cachedBest = best;

		return best;
	}

	@Override
	public String toString()
	{
		return buckets.toString();
	}
}

class Bucket
{
	private final LinkedList<Double> values = new LinkedList<Double>();
	private double cached_avg = 0.0;
	private boolean modified = true;

	double getValue()
	{
		if (!modified) {
			return cached_avg;
		}

		modified = false;

		double avg = 0.0;
		for (final Double v : values) {
			avg += v.doubleValue();
		}
		cached_avg = avg / values.size();
		return cached_avg;
	}

	int size()
	{
		return values.size();
	}

	Bucket(final double aValue)
	{
		values.add(aValue);
	}

	void add(final double aValue)
	{
		modified = true;
		values.add(aValue);
	}

	@Override
	public String toString()
	{
		return "[" + getValue() + " : " + values.size() + "]";
	}
}
