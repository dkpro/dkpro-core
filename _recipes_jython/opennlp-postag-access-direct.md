---
title: OpenNLP Part-of-speech tagging pipeline with direct access to results
subheadline: Analytics
---

The following script reads all text files (`*.txt`) in the specified folder and prints part-of-speech tags, one per line.

To execute the script, save it as pos.jpy. Then change in the command line to the folder where you saved the script and typ the command `C:\jython2.7b1\jython pos.jpy <foldername> <language>`, e.g. `C:\jython2.7b1\jython pos.jpy C:\example_folder\ en`. This will read all text files (`*.txt`) located in `C:\example_folder\` and assumes that the language in these files is English. Of course `C:\example_folder\` must exist on your file system and must contain at least one `.txt` file. 

{% highlight python %}
#!/usr/bin/env jython
# Fix classpath scanning - otherise uimaFIT will not find the UIMA types
from java.lang import Thread
from org.python.core.imp import *
Thread.currentThread().contextClassLoader = getSyspathJavaLoader()

# Dependencies and imports for DKPro modules
from jip.embed import require
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.1')
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.io.text-asl:1.6.1')
from de.tudarmstadt.ukp.dkpro.core.opennlp import *
from de.tudarmstadt.ukp.dkpro.core.io.text import *
from de.tudarmstadt.ukp.dkpro.core.api.segmentation.type import *
from de.tudarmstadt.ukp.dkpro.core.api.syntax.type import *

# uimaFIT imports
from org.apache.uima.fit.util.JCasUtil import *
from org.apache.uima.fit.pipeline.SimplePipeline import *
from org.apache.uima.fit.factory.CollectionReaderFactory import *
from org.apache.uima.fit.factory.AnalysisEngineFactory import *
from org.apache.uima.fit.factory.AnalysisEngineFactory import *

# Access to commandline arguments
import sys

# Assemble and run pipeline
pipeline = iteratePipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, sys.argv[1],
    TextReader.PARAM_LANGUAGE, sys.argv[2],
    TextReader.PARAM_PATTERNS, "*.txt"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger));

for jcas in pipeline:
  for token in select(jcas, Token):
    print token.coveredText + " " + token.pos.posValue
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
