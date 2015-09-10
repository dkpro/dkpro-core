---
title: OpenNLP Part-of-speech tagging pipeline using custom writer component
subheadline: Analytics
---

Reads all text files (`*.txt`) in the specified folder and prints part-of-speech tags, one per line.

Call with `pipeline <foldername> <language>`, e.g. `pipeline myFolder en`.

{% highlight groovy %}
#!/usr/bin/env groovy
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.io.text-asl', 
      version='1.5.0')

import org.apache.uima.jcas.JCas;

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.util.JCasUtil.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.io.text.*;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.*;

// Assemble and run pipeline
runPipeline(  
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, args[0], // first command line parameter
    TextReader.PARAM_LANGUAGE, args[1], // second command line parameter
    TextReader.PARAM_PATTERNS, "[+]*.txt"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger),
  createEngineDescription(Writer));

// Custom writer class used at the end of the pipeline to write results to screen
class Writer extends org.apache.uima.fit.component.JCasAnnotator_ImplBase {
  void process(JCas jcas) {
    select(jcas, Token).each { println "${it.coveredText} ${it.pos.posValue}" }
  }
}
{% endhighlight %}

Example output:

{% highlight text %}
The DT
quick JJ
brown JJ
fox NN
jumps NNS
over IN
the DT
lazy JJ
dog NN
. .
{% endhighlight %}
