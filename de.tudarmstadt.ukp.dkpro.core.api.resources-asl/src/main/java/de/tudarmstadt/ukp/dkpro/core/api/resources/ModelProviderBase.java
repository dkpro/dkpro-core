package de.tudarmstadt.ukp.dkpro.core.api.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.AggregateTagset;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.Tagset;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription;

public class ModelProviderBase<M>
    extends CasConfigurableStreamProviderBase<M>
    implements HasTagsets
{
    private AggregateTagset tagsets = new AggregateTagset();
    private Set<String> skipRecord = new HashSet<String>();

    @Override
    public void configure(CAS aCas)
        throws AnalysisEngineProcessException
    {
        super.configure(aCas);
        
        try {
            recordTagsets(aCas);
        }
        catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
    
    @Override
    protected M produceResource(InputStream aStream)
        throws Exception
    {
        return null;
    }

    @Override
    public Tagset getTagset()
    {
        return tagsets;
    }

    protected void addTagset(Tagset aProvider)
    {
        addTagset(aProvider, true);
    }    
    
    protected void addTagset(Tagset aProvider, boolean aRecord)
    {
        tagsets.add(aProvider);
        if (!aRecord) {
            skipRecord.addAll(aProvider.getLayers().keySet());
        }
    }

    protected void recordTagsets(CAS aCas)
        throws CASException
    {
        JCas jcas = aCas.getJCas();

        if (this instanceof HasTagsets) {
            Tagset provider = ((HasTagsets) this).getTagset();

            for (Entry<String, String> e : provider.getLayers().entrySet()) {
                if (skipRecord.contains(e.getKey())) {
                    continue;
                }
                
                TagsetDescription tsd = new TagsetDescription(jcas, 0, aCas.size());
                tsd.setLayer(e.getKey());
                tsd.setName(e.getValue());

                List<TagDescription> tags = new ArrayList<TagDescription>();
                for (String tag : provider.listTags(e.getKey(), e.getValue())) {
                    TagDescription td = new TagDescription(jcas);
                    td.setName(tag);
                    tags.add(td);
                }

                tsd.setTags(FSCollectionFactory.createFSArray(jcas, tags));
                tsd.addToIndexes();
            }
        }
    }
}
