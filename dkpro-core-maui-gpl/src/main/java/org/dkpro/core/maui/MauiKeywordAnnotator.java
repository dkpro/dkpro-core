package org.dkpro.core.maui;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.MetaDataStringField;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.ResourceInput;
import eu.openminted.share.annotations.api.ResourceOutput;
import eu.openminted.share.annotations.api.constants.AnnotationType;
import eu.openminted.share.annotations.api.constants.OperationType;
import eu.openminted.share.annotations.api.constants.ProcessingResourceType;

/**
 * The Maui tool assigns keywords to documents. The keywords can optionally come from controlled
 * vocabulary. The keywords are stored in DKPro Core {@link MetaDataStringField} 
 * annotations with the key {@code http://purl.org/dc/terms/subject}.
 * 
 * @see <a href="https://github.com/zelandiya/maui-standalone">Maui</a>
 */
@Component(OperationType.DOCUMENT_CLASSIFIER)
@ResourceMetaData(name = "Maui Keyword Annotator")
@ResourceInput(
        type = ProcessingResourceType.DOCUMENT)
@ResourceOutput(
        type = ProcessingResourceType.DOCUMENT,
        annotationLevel = AnnotationType.KEYWORD)
@TypeCapability(
        outputs = { 
            "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.MetaDataStringField" })
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

    /**
     * Location of the vocabulary file. Normally, this location is derived from the model
     * location by replacing the model extension {@code .ser} with {@code .rdf.gz.}
     */
    public static final String PARAM_VOCABULARY_LOCATION = "vocabularyLocation";
    @ConfigurationParameter(name = PARAM_VOCABULARY_LOCATION, mandatory = false)
    private String vocabularyLocation;

    /**
     * Format of the vocabulary file. Normally, this information is obtained from the key
     * {@code vocabulary.format} in the model metadata. Only {@code skos} and leaving the
     * parameter unset (i.e. no vocabulary) are currently supported.
     */
    public static final String PARAM_VOCABULARY_FORMAT = "vocabularyFormat";
    @ConfigurationParameter(name = PARAM_VOCABULARY_FORMAT, mandatory = false)
    private String vocabularyFormat;

    /**
     * Encoding of the vocabulary file. Normally, this information is obtained from the key
     * {@code vocabulary.encoding} in the model metadata.
     */
    public static final String PARAM_VOCABULARY_ENCODING = "vocabularyEncoding";
    @ConfigurationParameter(name = PARAM_VOCABULARY_ENCODING, mandatory = false, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String vocabularyEncoding;

    /**
     * Minimum similarity score to a variable require to count as a match (0-1).
     */
    public static final String PARAM_SCORE_THRESHOLD = "scoreThreshold";
    @ConfigurationParameter(name = PARAM_SCORE_THRESHOLD, defaultValue = "0.5")
    private double scoreThreshold;

    /**
     * Maximum number of keywords to assign to a document.
     */
    public static final String PARAM_MAX_TOPICS = "maxTopics";
    @ConfigurationParameter(name = PARAM_MAX_TOPICS, defaultValue = "10")
    private int maxTopics;

    private ModelProviderBase<MauiWrapper> modelProvider;
    
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        modelProvider = new ModelProviderBase<MauiWrapper>(this, "keywords")
        {
            @Override
            protected MauiWrapper produceResource(URL aUrl)
                throws IOException
            {
                Properties props = getAggregatedProperties();
                Properties metadata = getResourceMetaData();
                
                MauiFilter filter;
                
                try (InputStream is = aUrl.openStream()) {
                    InputStream iis = is;
                    if (aUrl.toString().endsWith(".gz")) {
                        iis = new GZIPInputStream(is);
                    }
                    filter = (MauiFilter) new ObjectInputStream(iis).readObject();
                }
                catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }
                

                Vocabulary vocabulary = null;
                String vocabFormat = defaultString(vocabularyFormat,
                        metadata.getProperty("vocabulary.format"));
                String vocabEncoding = defaultString(vocabularyEncoding,
                        metadata.getProperty("vocabulary.encoding", "UTF-8"));
                
                if (vocabFormat != null) {
                    String vocabLocation = defaultString(vocabularyLocation,
                            substringBefore(aUrl.toString(), ".ser") + ".rdf.gz");
                    
                    vocabulary = new Vocabulary();
                    vocabulary.setVocabularyName(vocabLocation);
                    vocabulary.setStemmer(new SremovalStemmer());
                    vocabulary.setLanguage(props.getProperty(LANGUAGE));
                    vocabulary.setStopwords(
                            StopwordsFactory.makeStopwords(props.getProperty(LANGUAGE)));
                    
                    try (InputStream is = ResourceUtils.resolveLocation(vocabLocation)
                            .openStream()) {
                        InputStream iis = is;
                        if (vocabLocation.endsWith(".gz")) {
                            iis = new GZIPInputStream(is);
                        }
                        
                        switch (vocabFormat) {
                        case "skos": {
                            Model model = ModelFactory.createDefaultModel();
                            model.read(new InputStreamReader(iis, vocabEncoding), "");
                            vocabulary.initializeFromModel(model);
                            break;
                        }
                        case "text":
                            // Maui supports this, but we presently do not.
                            throw new IllegalArgumentException(
                                    "Unknown format: [" + vocabFormat + "]");
                        default:
                            throw new IllegalArgumentException(
                                    "Unknown format: [" + vocabFormat + "]");
                        }
                    }
                    
                    filter.setVocabulary(vocabulary);
                    filter.setVocabularyFormat(vocabFormat);
                    filter.setVocabularyName(vocabLocation);
                }
                
                return new MauiWrapper(vocabulary, filter);
            }
        };
    }
    
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());

        ArrayList<Topic> topics;
        try {
            MauiWrapper wrapper = modelProvider.getResource();
            topics = wrapper.extractTopicsFromText(aJCas.getDocumentText(), maxTopics);
        }
        catch (MauiFilterException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        for (Topic t : topics) {
            if (t.getProbability() < scoreThreshold) {
                continue;
            }
            
            MetaDataStringField mdf = new MetaDataStringField(aJCas);
            mdf.setKey("http://purl.org/dc/terms/subject");
            mdf.setValue(t.getTitle());
            mdf.addToIndexes();
            //getLogger().info(String.format("[%s]\t[%s]\t[%f]%n", t.getId(), t.getTitle(),
            //        t.getProbability()));
        }
    }
}
