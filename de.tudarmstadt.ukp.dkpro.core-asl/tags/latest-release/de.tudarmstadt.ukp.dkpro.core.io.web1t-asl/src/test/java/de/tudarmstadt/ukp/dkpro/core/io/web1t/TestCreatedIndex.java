/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.io.web1t;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;

public class TestCreatedIndex
{

	@Ignore
	@Test
	// Assumes index created with data from amisch wikipedia for 1 to 3 grams
	public void testCreatedIndex()
		throws Exception
	{
		Web1TFileAccessProvider web = new Web1TFileAccessProvider(new File(
				"target/Index/"), 1, 3);

		assertEquals(200162, web.getNrOfNgrams(1));
		assertEquals(200162, web.getNrOfTokens());

		assertGreater(-1, web.getNrOfNgrams(1));
		assertGreater(-1, web.getNrOfNgrams(2));
		assertGreater(-1, web.getNrOfNgrams(3));
		assertEquals(-1, web.getNrOfNgrams(4));

		double l = web.getProbability("Amisch");
		assertEquals(0.002582907, l, 0.00000001);
	}

	private void assertGreater(long i, long nrOfNgrams)
	{

		if (nrOfNgrams <= i)
			fail("Value is not greater");
	}

}
