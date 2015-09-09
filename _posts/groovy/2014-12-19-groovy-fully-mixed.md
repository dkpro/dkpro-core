---
layout: post
title: Fully mixed pipeline
subheadline: Analytics
sidebar: left
categories:
    - groovy
tags:
    - conversion
    - recipe
---

Reads all text files (`*.txt`) in the specified folder and prints token, part-of-speech, lemma, and named entity in a tab-separated format, followed by the constituent tree as a bracketed structure.

Call with `pipeline <inputfolder> <language>`, e.g. `pipeline input en`.

{% highlight groovy %}
#!/usr/bin/env groovy
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.io.text-asl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.matetools-gpl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.clearnlp-asl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.berkeleyparser-gpl', 
      version='1.5.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
      module='de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl', 
      version='1.5.0')

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.util.JCasUtil.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.io.text.*;
import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.matetools.*;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.*;
import de.tudarmstadt.ukp.dkpro.core.berkeleyparser.*;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.*;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.*;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.*;

// Assemble and run pipeline
def pipeline = iteratePipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, args[0],     //first command line parameter
    TextReader.PARAM_LANGUAGE, args[1], //second command line parameter
    TextReader.PARAM_PATTERNS, "[+]*.txt"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(MatePosTagger),
  createEngineDescription(ClearNlpLemmatizer),
  createEngineDescription(BerkeleyParser,
    BerkeleyParser.PARAM_WRITE_PENN_TREE, true),
  createEngineDescription(StanfordNamedEntityRecognizer));

for (def jcas : pipeline) {
  for (def sentence : select(jcas, Sentence)) {
    println "Token\tPOS\tLemma\tNamed entity";
    selectCovered(Token, sentence).each {
      print "${it.coveredText}\t${it.pos.posValue}\t${it.lemma.value}\t";
      print "${selectCovering(NamedEntity, it)*.value}\n";
    };
    selectCovered(PennTree, sentence).each {println "\n${it.pennTree}"};
  }
} 
{% endhighlight %}

Example output:

{% highlight text %}
Token   POS Lemma   Named entity
Jim NNP jim [PERSON]
bought  VBD buy []
300 CD  0   []
shares  NNS share   []
of  IN  of  []
Acme    NNP acme    [ORGANIZATION]
Corp.   NNP corp.   [ORGANIZATION]
in  IN  in  []
2006    CD  0   []
.   .   .   []

(ROOT (S (NP (NNP Jim)) (VP (VBD bought) (NP (NP (CD 300) (NNS shares))
(PP (IN of) (NP (NNP Acme) (NNP Corp.)))) (PP (IN in) (NP (CD 2006)))) (. .)))
{% endhighlight %}