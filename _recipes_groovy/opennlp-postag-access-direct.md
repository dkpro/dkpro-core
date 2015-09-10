---
title: OpenNLP Part-of-speech tagging pipeline with direct access to results
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

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.util.JCasUtil.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.io.text.*;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.*;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.*;

def pipeline = iteratePipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, args[0],
    TextReader.PARAM_LANGUAGE, args[1],
    TextReader.PARAM_PATTERNS, "[+]*.txt"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger));

for (def jcas : pipeline) {
  select(jcas, Token).each { println "${it.coveredText} ${it.pos.posValue}" }
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