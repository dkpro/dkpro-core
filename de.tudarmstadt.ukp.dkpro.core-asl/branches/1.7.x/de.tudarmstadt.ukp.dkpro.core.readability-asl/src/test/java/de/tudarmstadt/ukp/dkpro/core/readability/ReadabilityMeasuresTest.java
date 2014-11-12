/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.readability;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures;

public class ReadabilityMeasuresTest
{
    
    private final static double EPSILON = 0.001; 

	@Test
	public void testKincaid() 
		throws Exception	
	{
		ReadabilityMeasures  rm = new ReadabilityMeasures();
		Method method = rm.getClass().getDeclaredMethod("kincaid",  new Class[]{Integer.class, Integer.class, Integer.class});
		method.setAccessible(true);
		double dScore = (Double)method.invoke(rm, new Object[]{new Integer(292), new Integer(415), new Integer(17)});
		method.setAccessible(false);
		System.out.println("kincaid:" + dScore);
		assertEquals(dScore, 7.879, EPSILON);
	}
	
	@Test
	public void testAri() 
		throws Exception	
	{
		ReadabilityMeasures  rm = new ReadabilityMeasures();
		Method method = rm.getClass().getDeclaredMethod("ari",  new Class[]{Integer.class, Integer.class, Integer.class});
		method.setAccessible(true);
		double dScore = (Double)method.invoke(rm, new Object[]{new Integer(1359), new Integer(292), new Integer(17)});
		method.setAccessible(false);
		System.out.println("ari:" + dScore);
        assertEquals(dScore, 9.079, EPSILON);
	}
	
	@Test
	public void testColeman_liau() 
		throws Exception	
	{
		ReadabilityMeasures  rm = new ReadabilityMeasures();
		Method method = rm.getClass().getDeclaredMethod("coleman_liau",  new Class[]{Integer.class, Integer.class, Integer.class});
		method.setAccessible(true);
		double dScore = (Double)method.invoke(rm, new Object[]{new Integer(1359), new Integer(292), new Integer(17)});
		method.setAccessible(false);
		System.out.println("coleman_liau:" + dScore);
        assertEquals(dScore, 11.612, EPSILON);
	}
	
	@Test
	public void testFlesch() 
		throws Exception	
	{
		ReadabilityMeasures  rm = new ReadabilityMeasures();
		Method method = rm.getClass().getDeclaredMethod("flesch",  new Class[]{Integer.class, Integer.class, Integer.class});
		method.setAccessible(true);
		double dScore = (Double)method.invoke(rm, new Object[]{new Integer(415), new Integer(292), new Integer(17)});
		method.setAccessible(false);
		System.out.println("flesch:" + dScore);
        assertEquals(dScore, 69.165, EPSILON);
	}
	
}
