package org.dkpro.core.maui;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.entopix.maui.filters.MauiFilter;
import com.entopix.maui.filters.MauiFilter.MauiFilterException;
import com.entopix.maui.main.MauiWrapper;
import com.entopix.maui.stemmers.SremovalStemmer;
import com.entopix.maui.stopwords.StopwordsFactory;
import com.entopix.maui.util.Topic;
import com.entopix.maui.vocab.Vocabulary;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;

public class MauiKeywordAnnotator
    extends JCasAnnotator_ImplBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;

    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    public static final String PARAM_VOCABULARY_LOCATION = "vocabularyLocation";
    @ConfigurationParameter(name = PARAM_VOCABULARY_LOCATION, mandatory = false)
    private String vocabularyLocation;

    public static final String PARAM_VOCABULARY_FORMAT = "vocabularyFormat";
    @ConfigurationParameter(name = PARAM_VOCABULARY_FORMAT, mandatory = false)
    private String vocabularyFormat;

    public static final String PARAM_VOCABULARY_ENCODING = "vocabularyEncoding";
    @ConfigurationParameter(name = PARAM_VOCABULARY_ENCODING, mandatory = false, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String vocabularyEncoding;

    public static final String PARAM_MAX_TOPICS = "maxTopics";
    @ConfigurationParameter(name = PARAM_MAX_TOPICS, defaultValue = "10")
    private int maxTopics;

    private ModelProviderBase<MauiFilter> modelProvider;
    private ModelProviderBase<Vocabulary> vocabularyProvider;
    
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        modelProvider = new ModelProviderBase<MauiFilter>(this, "tagger")
        {
            @Override
            protected MauiFilter produceResource(InputStream aStream)
                throws Exception
            {
                return (MauiFilter) new ObjectInputStream(aStream).readObject();
            }
        };
        
        vocabularyProvider = new ModelProviderBase<Vocabulary>()
        {
            {
                setContextObject(MauiKeywordAnnotator.this);

                setDefault(GROUP_ID, "org.dkpro.core");
                setDefault(ARTIFACT_ID,
                        "org.dkpro.core.maui-model-vocabulary-${language}-${variant}");

                setDefault(LOCATION, "classpath:/org/dkpro/core/maui/lib/" +
                        "vocabulary-${language}-${variant}.properties");
                setDefault(VARIANT, "default");

                setOverride(LOCATION, vocabularyLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }
            
            @Override
            protected Vocabulary produceResource(URL aUrl)
                throws IOException
            {
                Vocabulary vocabulary = new Vocabulary();
                vocabulary.setVocabularyName(vocabularyLocation);
                vocabulary.setStemmer(new SremovalStemmer());
                vocabulary.setLanguage(getAggregatedProperties().getProperty(LANGUAGE));
                vocabulary.setStopwords(StopwordsFactory.makeStopwords(language));
                
                try (InputStream is = aUrl.openStream()) {
                    InputStream iis = is;
                    if (aUrl.toString().endsWith(".gz")) {
                        iis = new GZIPInputStream(is);
                    }
                    
                    switch (vocabularyFormat) {
                    case "skos": {
                        Model model = ModelFactory.createDefaultModel();
                        model.read(new InputStreamReader(iis, vocabularyEncoding), "");
                        vocabulary.initializeFromModel(model);
                        break;
                    }
                    case "text":
                        // Maui supports this, but we presently do not.
                        throw new IllegalArgumentException(
                                "Unknown format: [" + vocabularyFormat + "]");
                    default:
                        throw new IllegalArgumentException(
                                "Unknown format: [" + vocabularyFormat + "]");
                    }
                }
                
                return vocabulary;
            }
        };
    }
    
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());

        MauiFilter extractionModel = modelProvider.getResource();

        Vocabulary vocabulary = null;
        if (isNotBlank(vocabularyLocation)) {
            vocabularyProvider.configure(aJCas.getCas());
            vocabulary = vocabularyProvider.getResource();
            extractionModel.setVocabulary(vocabulary);
        }

        ArrayList<Topic> topics;
        try {
            MauiWrapper wrapper = new MauiWrapper(vocabulary, extractionModel);
            topics = wrapper.extractTopicsFromText(aJCas.getDocumentText(), maxTopics);
        }
        catch (MauiFilterException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        for (Topic t : topics) {
            System.out.printf("[%s]\t[%s]\t[%f]%n", t.getId(), t.getTitle(), t.getProbability());
        }
    }
}
