package de.tudarmstadt.ukp.dkpro.core.api.metadata;

import static java.util.Collections.singletonMap;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class SingletonTagset
    extends TagsetBase
{
    private String layer;
    private String tagset;
    private Set<String> tags;
    
    public SingletonTagset(Class<?> aLayer, String aTagsetName)
    {
        layer = aLayer.getName();
        tagset = aTagsetName;
        tags = new TreeSet<String>();
    }

    @Override
    public Map<String, String> getLayers()
    {
        return singletonMap(layer, tagset);
    }

    @Override
    public Set<String> listTags(String aLayer, String aTagsetName)
    {
        return tags;
    }

    public void add(String aTag)
    {
        tags.add(aTag);
    }

    public void addAll(Collection<String> aTags)
    {
        tags.addAll(aTags);
    }
    
    public void removeAll(SingletonTagset aOther)
    {
        Entry<String, String> entry = aOther.getLayers().entrySet().iterator().next();
        tags.removeAll(aOther.listTags(entry.getKey(), entry.getValue()));
    }
}
