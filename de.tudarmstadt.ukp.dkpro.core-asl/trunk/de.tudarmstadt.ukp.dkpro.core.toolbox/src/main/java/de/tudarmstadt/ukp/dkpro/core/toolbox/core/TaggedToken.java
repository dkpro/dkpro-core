package de.tudarmstadt.ukp.dkpro.core.toolbox.core;

public class TaggedToken
{

    private String token;
    private Tag pos;
    
    public TaggedToken(String token, Tag pos)
    {
        super();
        this.token = token;
        this.pos = pos;
    }

    public String getToken()
    {
        return token;
    }
    public void setToken(String token)
    {
        this.token = token;
    }
    public Tag getPos()
    {
        return pos;
    }
    public void setPos(Tag pos)
    {
        this.pos = pos;
    }
    
    @Override
    public String toString()
    {
    	return token + " (" + pos.toString() + ")";
    }
}
