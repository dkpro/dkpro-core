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
