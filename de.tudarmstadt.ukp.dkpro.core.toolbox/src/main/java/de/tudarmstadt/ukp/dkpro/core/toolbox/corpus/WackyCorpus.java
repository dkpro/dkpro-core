package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReader;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbReader;

/**
 * A wrapper for the WaCky large-scale Web corpora. It searches a DKPRO_HOME
 * workspace.
 * 
 * Language editions are assumed to be gzipped and folders shold be  
 * named according to the enum {@link WackyLanguageEdition}.
 * 
 * @author zesch
 * 
 */
@SuppressWarnings("serial")
public class WackyCorpus {
    
    public enum WackyLanguageEdition {
        DEWAC,
        UKWAC
    }
    
    // FIXME are those really the right tagsets for the corpora and isn't there a better method to provide this?
    private static Map<WackyLanguageEdition,String> language2TagsetMap = new HashMap<WackyLanguageEdition, String>() {{
       put(WackyLanguageEdition.DEWAC, "src/main/resources/tagsets/stts.map"); 
       put(WackyLanguageEdition.UKWAC, "src/main/resources/tagsets/en-tagger.map"); 
    }};

    private static final String WORKSPACE = "wacky";
    
    private JCasIterable jcasIterable;
    private WackyLanguageEdition language;
    
    public WackyCorpus(WackyLanguageEdition languageEdition) throws Exception
    {
        String wackyPath = DKProContext.getContext().getWorkspace(WORKSPACE).getAbsolutePath() + "/"
            + languageEdition.name();
        initialize(wackyPath, languageEdition);
    }

    public WackyCorpus(String wackyPath, WackyLanguageEdition languageEdition) throws Exception
    {
        initialize(wackyPath, languageEdition);
    }

    private void initialize(String wackyPath, WackyLanguageEdition languageEdition) throws Exception {
        CollectionReader reader = createCollectionReader(
                ImsCwbReader.class,
                ImsCwbReader.PARAM_PATH, wackyPath,
                ImsCwbReader.PARAM_LANGUAGE, languageEdition.name(),
                ImsCwbReader.PARAM_ENCODING, "ISO-8859-15",
                ImsCwbReader.PARAM_TAGGER_TAGSET, language2TagsetMap.get(languageEdition),
                ImsCwbReader.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.txt.gz" 
                }
        );

        jcasIterable = new JCasIterable(reader);
        language = languageEdition;
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
        switch(this.language) {
            case DEWAC:
                return "de";
            default:
                return "en";
        }
    }

    public String getName()
    {
        return this.language.toString();
    }
}