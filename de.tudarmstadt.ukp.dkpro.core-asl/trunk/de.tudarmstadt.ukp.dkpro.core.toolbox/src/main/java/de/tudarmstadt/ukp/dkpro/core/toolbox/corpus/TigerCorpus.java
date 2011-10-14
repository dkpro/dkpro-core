package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;


/**
 * Tiger Corpus
 * 
 * @author zesch
 *
 */
public class TigerCorpus {

    private static final String LANGUAGE = "de";
    private static final String NAME = "Tiger";
    
    private JCasIterable jcasIterable;

    public TigerCorpus() throws Exception
    {
        String tigerFile = DKProContext.getContext().getWorkspace("dkpro_teaching").getAbsolutePath() +
        "/corpora/tiger_export/tiger_release_dec05.export";
        
        initialize(tigerFile);
    }

    public TigerCorpus(String tigerFile) throws Exception
    {
        initialize(tigerFile);
    }

    private void initialize(String tigerFile) throws Exception {
        CollectionReader reader = createCollectionReader(
                NegraExportReader.class,
                NegraExportReader.PARAM_INPUT_FILE, tigerFile,
                NegraExportReader.PARAM_ENCODING, "ISO-8859-15",
                NegraExportReader.PARAM_LANGUAGE, LANGUAGE
        );

        jcasIterable = new JCasIterable(reader);
    }
    
    public JCasIterable getJCasIterable() {
        return jcasIterable;
    }
    
    public boolean hasNextText() {
        return jcasIterable.hasNext();
    }
    
    public String getNextText()
        throws Exception
    {
        if (jcasIterable.hasNext()) {
            return jcasIterable.next().getDocumentText();
        }
        else {
            return null;
        }
    }

    public String getLanguage()
    {
        return LANGUAGE;
    }

    public String getName()
    {
        return NAME;
    }
}
