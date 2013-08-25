package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

public class CorpusException
    extends Exception
{

    static final long serialVersionUID = 1L;

    public CorpusException()
    {
        super();
    }

    public CorpusException(String txt)
    {
        super(txt);
    }

    public CorpusException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CorpusException(Throwable cause)
    {
        super(cause);
    }

}
