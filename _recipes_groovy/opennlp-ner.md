---
title: OpenNLP Named Entity Recognition pipeline
subheadline: Analytics
---

Reads all text files (`*.txt`) in the specified folder and prints the named entities contained in the file

Call with `groovy pipeline <inputfolder> <language>`, e.g. `pipeline input en`. 

Mind that using `.` as the input folder is currently not supported.

{% highlight groovy %}
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.io.text-asl', 
      version='1.8.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl', 
      version='1.8.0')
import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.util.JCasUtil.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.io.text.*;
import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.*;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.*;

// Assemble and run pipeline
def pipeline = iteratePipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, args[0],     //first command line parameter
    TextReader.PARAM_LANGUAGE, args[1], //second command line parameter
    TextReader.PARAM_PATTERNS, "[+]*.txt"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpNamedEntityRecognizer, "modelVariant", "person"),
  createEngineDescription(OpenNlpNamedEntityRecognizer, "modelVariant", "organization"),
  createEngineDescription(OpenNlpNamedEntityRecognizer, "modelVariant", "location"));

for (def document : pipeline) {
  def dmd = DocumentMetaData.get(document);
  println "${dmd.documentUri}:";
  for (def ne : select(document, NamedEntity)) {
    println "  ${ne.coveredText}";
  }
} 
{% endhighlight %}

Example inpu `example.txt`:

{% highlight text %}
John Miller works at the IBM headquarters in the United States.
{% endhighlight %}

Example output:

{% highlight text %}
file:/Users/john/example.txt:
  John Miller
  IBM
  United States
{% endhighlight %}