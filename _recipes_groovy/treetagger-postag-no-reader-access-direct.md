---
title: TreeTagger Part-of-speech tagging & parsing without reader or writer
subheadline: Embedding
---

This is an example of how to use the DKPro Core !TreeTaggerPosTagger component with a manually
downloaded !TreeTagger executable and model.

{% highlight groovy %}
#!/usr/bin/env groovy
/**
 * SYNOPSIS: treetagger.groovy [executable] [model]
 *
 * EXAMPLE:  ./treetagger.groovy /usr/local/bin/tree-tagger english-par-linux-3.2-utf8.bin
 *
 * Annotates an English text using treetagger.
 */
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.treetagger-asl', 
      version='1.7.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.tokit-asl', 
      version='1.7.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.io.text-asl', 
      version='1.7.0')

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosTagger;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

def ttExecutable = args[0];
def ttModel = args[1]
def ttModelEncoding = "UTF-8";
def ttTagset = "ptb";
def language = "en";

// Assemble pipeline
def pipeline = iteratePipeline(
  createReaderDescription(StringReader,
    StringReader.PARAM_DOCUMENT_TEXT, "The quick brown fox jumps over the lazy dog.",
    StringReader.PARAM_LANGUAGE, language),
  createEngineDescription(BreakIteratorSegmenter),
  createEngineDescription(TreeTaggerPosTagger,
    TreeTaggerPosTagger.PARAM_EXECUTABLE_PATH, ttExecutable,
    TreeTaggerPosTagger.PARAM_MODEL_LOCATION, ttModel ,
    TreeTaggerPosTagger.PARAM_MODEL_ENCODING, ttModelEncoding,
    TreeTaggerPosTagger.PARAM_POS_MAPPING_LOCATION, 
      "classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/" +
        "${language}-${ttTagset}-pos.map"))

// Run the pipeline
for (doc in pipeline) {
  for (t in select(doc, Token)) {
    println "${t.coveredText}\t${t.pos.posValue}"
  }
}
{% endhighlight %}

Example output:

{% highlight groovy %}
The DT
quick   JJ
brown   JJ
fox NN
jumps   NNS
over    IN
the DT
lazy    JJ
dog NN
.   SENT
{% endhighlight %}
