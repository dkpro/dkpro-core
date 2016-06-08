---
title: NLTK intro examples ported to DKPro Core using Groovy
subheadline: NLTK examples
---

In this section, we pick up some of the [examples](http://www.nltk.org NLTK) and implement them using DKPro Core.

### Tokenize and tag some text

Original NLTK example:

{% highlight python %}
>>> import nltk
>>> sentence = """At eight o'clock on Thursday morning
... Arthur didn't feel very good."""
>>> tokens = nltk.word_tokenize(sentence)
>>> tokens
['At', 'eight', "o'clock", 'on', 'Thursday', 'morning',
'Arthur', 'did', "n't", 'feel', 'very', 'good', '.']
>>> tagged = nltk.pos_tag(tokens)
>>> tagged[0:6]
[('At', 'IN'), ('eight', 'CD'), ("o'clock", 'JJ'), ('on', 'IN'),
('Thursday', 'NNP'), ('morning', 'NN')]
{% endhighlight %}

DKPro Core Groovy version:

{% highlight groovy %}
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.5.0',
      module='de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl')
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.5.0',
      module='de.tudarmstadt.ukp.dkpro.core.io.text-asl')
import de.tudarmstadt.ukp.dkpro.core.io.text.*;

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.util.JCasUtil.*;

import de.tudarmstadt.ukp.dkpro.core.io.text.*;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.*;

def sentence = """At eight o'clock on Thursday morning 
Arthur didn't feel very good."""

def result = iteratePipeline(
  createReaderDescription(StringReader,
    StringReader.PARAM_DOCUMENT_TEXT, sentence,
    StringReader.PARAM_LANGUAGE, "en"),
  createEngineDescription(StanfordSegmenter),
  createEngineDescription(StanfordPosTagger));

result.each { println select(it, Token).collect { it.coveredText } }

result.each { doc ->
  println select(doc, Token).collect { token ->
    [token.coveredText, token.pos.posValue ] }[0..5] }
{% endhighlight %}

Output: 

{% highlight text %}
[At, eight, o'clock, on, Thursday, morning, Arthur, did, n't, feel, very, good, .]
[[At, IN], [eight, CD], [o'clock, RB], [on, IN], [Thursday, NNP], [morning, NN]]
{% endhighlight %}

### Identify named entities

Original NLTK example (must be run immediately after the previous example):

{% highlight python %}
>>> entities = nltk.chunk.ne_chunk(tagged)
>>> entities
Tree('S', [('At', 'IN'), ('eight', 'CD'), ("o'clock", 'JJ'),
           ('on', 'IN'), ('Thursday', 'NNP'), ('morning', 'NN'),
       Tree('PERSON', [('Arthur', 'NNP')]),
           ('did', 'VBD'), ("n't", 'RB'), ('feel', 'VB'),
           ('very', 'RB'), ('good', 'JJ'), ('.', '.')])
{% endhighlight %}

DKPro Core Groovy version:

{% highlight groovy %}
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.5.0',
      module='de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl')
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.factory.JCasFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.util.JCasUtil.*;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.*;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.*;

def doc = createJCas();
doc.documentText = """At eight o'clock on Thursday morning 
Arthur didn't feel very good."""
doc.documentLanguage = "en";

runPipeline(doc,
  createEngineDescription(StanfordSegmenter),
  createEngineDescription(StanfordPosTagger),
  createEngineDescription(StanfordNamedEntityRecognizer));

println select(doc, Token).collect { it.coveredText }

println select(doc, Token).collect { 
  [it.coveredText, it.pos.posValue ] }[0..5]

println select(doc, Token).collect { 
  [it.coveredText, selectCovering(NamedEntity, it).collect { it.value } ] }
{% endhighlight %}

Output:

{% highlight text %}
[At, eight, o'clock, on, Thursday, morning, Arthur, did, n't, feel, very, good, .]
[[At, IN], [eight, CD], [o'clock, RB], [on, IN], [Thursday, NNP], [morning, NN]]
[[At, []], [eight, []], [o'clock, []], [on, []], [Thursday, []], [morning, []], [Arthur, [PERSON]], [did, []], [n't, []], [feel, []], [very, []], [good, []], [., []]]
{% endhighlight %}

### Comparison NLTK vs. DKPro Core

We notice that the NLTK examples are much shorter, even though they include the output of the commands. The NTLK examples are run in an interactive Python shell where we just have to hack in a couple of commands. The DKPro Core examples are comparatively longish scripts - nothing that one would want to hack into a shell to play around with and explore NLP tools. 

With a single `import nltk` we get access to a lot of functionality in NLTK, e.g. a default tokenizer, tagger, named entity recognizer, etc. However, all of these are for English only. In DKPro Core we need to first add dependencies on all the modules using `@Grab`and then import the actual tools from the modules. We also need to add several unrelated imports to get access to necessary functions like `select`or `createEngineDescription`. 

In NLTK we only have convenient access to a few tools for English. The script does not know what version of NLTK it is supposed to run with. With DKPro Core, we have access to a wide array of integrated tools and we know exactly which version of each tool we use. Also most of the tools do not only support English, but also additional languages.

In NLTK we can nicely execute one analysis step after the other and always explore the intermediate results. The DKPro Core scripts are more suitable for the batch-processing of larger amounts of documents.
