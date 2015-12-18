---
title: OpenNLP Part-of-speech tagging & parsing without reader
subheadline: Embedding
---

This script is not using a reader. Instead the input is given as a Jython string (see `jcas.documentText`).

For this string, the pipeline performs part-of-speech tagging and parses it, using the !OpenNlpParser. The results are written to the console. 

To execute the script, save it as `pos_parser.jpy` and run `C:\jython2.7b1\jython pos_parser.jpy`. 

Parsing can require a lot of memory. To increase the available memory for your script, execute `set JAVA_OPTS="-Xmx1G"` in the command line and try to execute the script again.

{% highlight python %}
#!/usr/bin/env jython
# Fix classpath scanning - otherise uimaFIT will not find the UIMA types
from java.lang import Thread
from org.python.core.imp import *
Thread.currentThread().contextClassLoader = getSyspathJavaLoader()

# Dependencies and imports for DKPro modules
from jip.embed import require
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.1')
from de.tudarmstadt.ukp.dkpro.core.opennlp import *
from de.tudarmstadt.ukp.dkpro.core.api.segmentation.type import *
from de.tudarmstadt.ukp.dkpro.core.api.syntax.type import *

# uimaFIT imports
from org.apache.uima.fit.util.JCasUtil import *
from org.apache.uima.fit.pipeline.SimplePipeline import *
from org.apache.uima.fit.factory.AnalysisEngineFactory import *
from org.apache.uima.fit.factory import JCasFactory

# Assemble and run pipeline
jcas = JCasFactory.createJCas()
jcas.documentText = "This is a test"
jcas.documentLanguage = "en"

runPipeline(jcas,
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger),
  createEngineDescription(OpenNlpParser,
    OpenNlpParser.PARAM_WRITE_PENN_TREE, True));

for token in select(jcas, Token):
  print token.coveredText + " " + token.pos.posValue

for tree in select(jcas, PennTree):
  print tree.pennTree
{% endhighlight %}

Example output:

{% highlight text %}
This DT
is VBZ
a DT
test NN
(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (DT a) (NN test)))))
{% endhighlight %}