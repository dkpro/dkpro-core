/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.decompounding.uima.resource;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.core.decompounding.ranking.MutualInformationRanker;
import org.dkpro.core.decompounding.splitter.DecompoundedWord;
import org.dkpro.core.decompounding.splitter.DecompoundingTree;
import org.dkpro.core.decompounding.web1t.Finder;

public class MutualInformationRankerResource
    extends RankerResource
{
    @SuppressWarnings({ "rawtypes" })
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        ranker = new MutualInformationRanker();
        return true;
    }

    @Override
    public DecompoundedWord highestRank(DecompoundingTree aSplitTree)
    {
        return ranker.highestRank(aSplitTree);
    }

    @Override
    public void setFinder(Finder aFinder)
    {
        ranker.setFinder(aFinder);
    }
}
