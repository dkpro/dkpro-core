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
