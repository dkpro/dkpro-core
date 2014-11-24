/*******************************************************************************
 * Copyright 2014
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
 *******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.decompounding.ranking;

import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;

public class DummyRanker extends AbstractRanker
{

    @Override
    public DecompoundedWord highestRank(ValueNode<DecompoundedWord> aParent,
            List<DecompoundedWord> aPath)
    {
        if (aPath != null) {
            aPath.add(aParent.getValue());
        }

        List<DecompoundedWord> children = aParent.getChildrenValues();
        if (children.size() == 0) {
            return aParent.getValue();
        }

        return children.get(children.size()-1);
    }

}
