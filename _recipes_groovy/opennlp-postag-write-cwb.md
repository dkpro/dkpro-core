---
title: OpenNLP Part-of-speech tagging pipeline writing to IMS Open Corpus Workbench format
subheadline: Analytics
---

Reads all text files (`*.txt`) in the specified folder and writes to the specified file.

Call with `pipeline <foldername> <language> <outputfile>`, e.g. `pipeline myFolder en output.tsv`.

{% highlight groovy %}
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.io.text-asl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.io.imscwb-asl', 
      version='1.5.0')

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.io.text.*;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.*;

// Assemble and run pipeline
runPipeline(  
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, args[0], // first command line parameter
    TextReader.PARAM_LANGUAGE, args[1], // second command line parameter
    TextReader.PARAM_PATTERNS, "[+]*.txt"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger),
  createEngineDescription(ImsCwbWriter,
    ImsCwbWriter.PARAM_TARGET_LOCATION, args[2])); // third command line parameter
{% endhighlight %}
