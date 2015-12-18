---
title: MaltParser dependency parsing pipeline with direct access to results
subheadline: Embedding
---

Reads the specified file and prints dependencies, one per line. Multiple files can be specified using a wildcard, e.g. '*.txt' (the single quotes are part of the argument to avoid the shell expanding the wildcard!).

Call with `pipeline <foldername> <language>`, e.g. `pipeline myFolder en`.

{% highlight python %}
#!/usr/bin/env jython
# Fix classpath scanning - otherise uimaFIT will not find the UIMA types
from java.lang import Thread
from org.python.core.imp import *
Thread.currentThread().contextClassLoader = getSyspathJavaLoader()

# Dependencies and imports for DKPro modules
from jip.embed import require
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.1')
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.maltparser-asl:1.6.1')
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.io.text-asl:1.6.1')
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.io.conll-asl:1.6.1')
from de.tudarmstadt.ukp.dkpro.core.opennlp import *
from de.tudarmstadt.ukp.dkpro.core.maltparser import *
from de.tudarmstadt.ukp.dkpro.core.io.text import *
from de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency import *

# uimaFIT imports
from org.apache.uima.fit.util.JCasUtil import *
from org.apache.uima.fit.pipeline.SimplePipeline import *
from org.apache.uima.fit.factory.AnalysisEngineFactory import *
from org.apache.uima.fit.factory.CollectionReaderFactory import *

# Access to commandline arguments
import sys

# Assemble and run pipeline
pipeline = iteratePipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_SOURCE_LOCATION, sys.argv[1], # 1st commandline parameter
    TextReader.PARAM_LANGUAGE, sys.argv[2]),       # 2nd commandline parameter
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger),
  createEngineDescription(MaltParser))

for jcas in pipeline:
  for dep in select(jcas, Dependency): 
    print "dep: [" + dep.dependencyType +"] \t gov: [" + dep.governor.coveredText + "] \t dep: [" + dep.dependent.coveredText + "]" 
{% endhighlight %}

Example output:

{% highlight text %}
dep: [det]   gov: [jumps]    dep: [The]
dep: [amod]      gov: [jumps]    dep: [quick]
dep: [amod]      gov: [jumps]    dep: [brown]
dep: [nn]    gov: [jumps]    dep: [fox]
dep: [prep]      gov: [jumps]    dep: [over]
dep: [det]   gov: [dog]      dep: [the]
dep: [amod]      gov: [dog]      dep: [lazy]
dep: [pobj]      gov: [over]     dep: [dog]
dep: [punct]     gov: [jumps]    dep: [.]
{% endhighlight %}
