---
title: OpenNLP Part-of-speech tagging & parsing without reader
subheadline: Embedding
---

This pipeline internally creates data, processes it, and writes results to the console.

Mind to provide more memory to Groovy using the command `export JAVA_OPTS="-Xmx1g"` before trying to run this.

{% highlight groovy %}
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl', 
      version='1.5.0')

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.util.JCasUtil.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import org.apache.uima.fit.factory.JCasFactory;

import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.*;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.*;

def jcas = JCasFactory.createJCas();
jcas.documentText = "This is a test";
jcas.documentLanguage = "en";

runPipeline(jcas,
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger),
  createEngineDescription(OpenNlpParser,
    OpenNlpParser.PARAM_WRITE_PENN_TREE, true));

select(jcas, Token).each { println "${it.coveredText} ${it.pos.posValue}" }

select(jcas, PennTree).each { println it.pennTree }
{% endhighlight %}

Example output:

{% highlight text %}
This DT
is VBZ
a DT
test NN
(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (DT a) (NN test)))))
{% endhighlight %}
