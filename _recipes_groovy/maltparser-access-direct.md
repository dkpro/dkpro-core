---
title: MaltParser dependency parsing pipeline with direct access to results
subheadline: Embedding
---

Reads the specified file and prints dependencies, one per line. Multiple files can be specified using a wildcard, e.g. '*.txt' (the single quotes are part of the argument to avoid the shell expanding the wildcard!).

This recipe was motivated by a [question on Stack Overflow](http://stackoverflow.com/questions/17392790/parse-raw-text-with-maltparser-in-java) on how to parse raw text using the MaltParser.

Call with `pipeline <foldername> <language>`, e.g. `pipeline myFolder en`.

{% highlight groovy %}
#!/usr/bin/env groovy
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl', 
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
import static org.apache.uima.fit.util.JCasUtil.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.maltparser.*;
import de.tudarmstadt.ukp.dkpro.core.io.text.*;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.*;

// Assemble and run pipeline
def pipeline = iteratePipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_SOURCE_LOCATION, args[0], // first command line parameter
    TextReader.PARAM_LANGUAGE, args[1]), // second command line parameter
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger),
  createEngineDescription(MaltParser));

for (def jcas : pipeline) {
  select(jcas, Dependency).each { 
    println "dep: [${it.dependencyType}] \t gov: [${it.governor.coveredText}] \t dep: [${it.dependent.coveredText}]" 
  }
}
{% endhighlight %}

Example output:

{% highlight groovy %}
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