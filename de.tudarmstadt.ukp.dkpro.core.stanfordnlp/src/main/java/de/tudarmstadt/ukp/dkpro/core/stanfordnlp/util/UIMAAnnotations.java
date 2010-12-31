/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util;

import java.util.Collection;

import edu.stanford.nlp.ling.CoreAnnotation;

/**
 * Annotations of the type "UIMAAnnotations" should contain a Collection
 * of org.apache.uima.jcas.tcas.Annotation objects.<br/>
 *
 * @author Oliver Ferschke
 *
 */
public class UIMAAnnotations
	implements CoreAnnotation<Collection>
{
	@Override
	public Class<Collection> getType()
	{
		return Collection.class;
	}


}
