package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import java.io.File;
import java.io.IOException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;
import de.tudarmstadt.ukp.dkpro.core.toolbox.util.TazSentenceIterable;

// FIXME does not work at the moment
/**
 * A wrapper for the Taz newspaper corpus. It searches a DKPRO_HOME workspace.
 * The actual data file is assumed to be gzipped.
 * 
 * @author zesch
 * 
 */
public class TazCorpus
    implements Corpus
{
    
    private final static String NAME = "taz";
    private final static String LANG_CODE = "de";
    private final static String CHARSET = "UTF-8";
//    private final static String CHARSET = "ISO-8859-15";
    
    private final File tazPath;
    
    public TazCorpus() throws IOException
    {
        this("taz_corpus");
    }

    /**
     * @param workspace
     *            The DKPRO_HOME workspace
     * @param corpus
     *            The name of the corpus, i.e. the name of the folder in the
     *            given workspace.
     * @throws IOException 
     */
    public TazCorpus(String workspace) throws IOException
    {
        tazPath = new File(
                DKProContext.getContext().getWorkspace(workspace).getAbsolutePath() +
                "/taz_corpus.txt.gz"
        );
    }

    @Override
    public Iterable<Sentence> getSentences()
        throws Exception
    {
        return new TazSentenceIterable(tazPath, CHARSET);
    }

    @Override
    public Iterable<TaggedToken> getTaggedTokens()
        throws Exception
    {
        // FIXME add implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Tag> getTags()
        throws Exception
    {
        // FIXME add implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<String> getTokens()
        throws Exception
    {
        // FIXME add implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Text> getTexts()
        throws Exception
    {
        // FIXME add implementation
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getLanguage()
    {
        return LANG_CODE;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}