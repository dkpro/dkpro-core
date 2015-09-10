---
title: MaltParser dependency parsing pipeline writing to CONLL format
subheadline: Analytics
---

Reads all text files (`*.txt`) in the specified folder and prints dependencies, one per line.

Call with `pipeline <inputfolder> <language> <outputfolder>`, e.g. `pipeline input en output`.

{% highlight groovy %}
#!/usr/bin/env groovy
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.maltparser-asl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.io.text-asl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.io.conll-asl', 
      version='1.5.0')

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;
import de.tudarmstadt.ukp.dkpro.core.maltparser.*;
import de.tudarmstadt.ukp.dkpro.core.io.conll.*;
import de.tudarmstadt.ukp.dkpro.core.io.text.*;

// Assemble and run pipeline
runPipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, args[0], // first command line parameter
    TextReader.PARAM_LANGUAGE, args[1], // second command line parameter
    TextReader.PARAM_PATTERNS, "[+]*.txt"),
  createEngineDescription(StanfordSegmenter),
  createEngineDescription(StanfordPosTagger),
  createEngineDescription(MaltParser),
  createEngineDescription(Conll2006Writer,
    Conll2006Writer.PARAM_TARGET_LOCATION, args[2])); // third command line parameter);
{% endhighlight %}

Example output:

{% highlight text %}
1   The _   DT  DT  _   4   det _   _
2   quick   _   JJ  JJ  _   4   amod    _   _
3   brown   _   JJ  JJ  _   4   amod    _   _
4   fox _   NN  NN  _   5   nsubj   _   _
5   jumps   _   VBZ VBZ _   0   _   _   _
6   over    _   IN  IN  _   5   prep    _   _
7   the _   DT  DT  _   9   det _   _
8   lazy    _   JJ  JJ  _   9   amod    _   _
9   dog _   NN  NN  _   6   pobj    _   _
10  .   _   .   .   _   5   punct   _   _
{% endhighlight %}