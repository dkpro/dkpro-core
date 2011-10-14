package de.tudarmstadt.ukp.dkpro.core.toolbox.core;

import java.net.MalformedURLException;

import de.tudarmstadt.ukp.dkpro.core.toolbox.util.TagUtil;

public class Tag
{

    private String tag;
    private String simplifiedTag;

    public Tag(String tag, String language) throws MalformedURLException
    {
        super();
        this.tag = tag;
        this.simplifiedTag = TagUtil.getSimplifiedTag(tag, language);
    }
    
    public String getTag()
    {
        return tag;
    }
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    public String getSimplifiedTag()
    {
        return simplifiedTag;
    }
    public void setSimplifiedTag(String simplifiedTag)
    {
        this.simplifiedTag = simplifiedTag;
    }

    @Override
    public String toString()
    {
        return this.tag + "/" + this.simplifiedTag; 
    }
}