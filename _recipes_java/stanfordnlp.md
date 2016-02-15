---
title: Stanford CoreNLP Intro
subheadline: Analytics
---

This page describes processing a small paragraph with Stanford CoreNLP components: StanfordSegmenter, StanfordNamedEntityRecognizer, StanfordParser.

* TOC
{:toc}

## Introduction

This page describes processing a small paragraph with Stanford CoreNLP components (*StanfordSegmenter*, *StanfordNamedEntityRecognizer*, *StanfordParser*) and writing out the *noun phrases* (NP) and *Named Entities* (NE) occurring in the NPs to the console output, such as e.g.

{% highlight text %}
NP   the new product of UKP Lab   NE UKP Lab
NP   the latest announcements in the news   NE -
{% endhighlight %}

All these components are UIMA annotators for the [Stanford CoreNLP](http://www-nlp.stanford.edu/software/corenlp.shtml) software.

## Pre-requisites

Either, create a new Maven project or incorporate the following to your existing Maven project. It is expected that you read the [introductory material]({{ site.url }}/pages/java-intro/). You'll need the following dependencies.

Dependencies:

* de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl
* de.tudarmstadt.ukp.dkpro.core.io.text-asl
* de.tudarmstadt.ukp.dkpro.core.stanfordnlp-model-ner-en-all.3class.distsim.crf (for example)
* de.tudarmstadt.ukp.dkpro.core.stanfordnlp-model-parser-en-pcfg (for example)

## Reader

For example, put all your sentences in a text file and read them with a default reader from DkPro. (The directory containing the file is given as the first command line argument.) 

{% highlight java %}
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

CollectionReaderDescription reader = createReaderDescription(
    TextReader.class,
    TextReader.PARAM_SOURCE_LOCATION, "src/test/resources",
    TextReader.PARAM_PATTERNS, "*.txt",
    TextReader.PARAM_LANGUAGE, "en");
{% endhighlight %}

It is *important* to set the language (two letter ISO 639-1 code) at the reader.

### Example sentences

As examples, the following sentences are used:

{% highlight text %}
Have you seen Anna?
Last year we visited the beautiful New York.
We live in Berlin.
The UN resides in New York City.
{% endhighlight %}

## StanfordSegmenter

The Stanford Segmenter performs tokenizations and makes sentence and token annotations and can serve as a high quality alternative for other segmenter components like the _BreakIteratorSegmenter_.

### Pipeline

Include the component into your pipeline this way:

{% highlight java %}
AnalysisEngineDescription seg = createEngineDescription(StanfordSegmenter.class);
{% endhighlight %}

### Output

An example annotations output may look like this:

{% highlight text %}
INFO: === CAS ===
-- Document Text --
The UN resides in New York City.
-- Annotations --
[DocumentAnnotation] (0, 32) The UN resides in New York City.
[Sentence] (0, 32) The UN resides in New York City.
[Token] (0, 3) The
[Token] (4, 6) UN
[Token] (7, 14) resides
[Token] (15, 17) in
[Token] (18, 21) New
[Token] (22, 26) York
[Token] (27, 31) City
[Token] (31, 32) .
{% endhighlight %}

## StanfordNamedEntityRecognizer

The [Stanford NER](http://nlp.stanford.edu/software/CRF-NER.shtml) is a CRFClassifier implementation of a Named Entity Recognizer to label sequences of words in a text which are the names of things, such as person and company names.

### Models and types

Included with the Stanford NER are a 4 class model trained for CoNLL, a 7 class model trained for MUC, and a 3 class model trained on both data sets for the intersection of those class sets.

* 3 class  Location, Person, Organization
* 4 class  Location, Person, Organization, Misc
* 7 class  Time, Location, Organization, Person, Money, Percent, Date

The corresponding UIMA annotation types are called "Person", "Organization", "Location" etc., from the package *de.tudarmstadt.ukp.dkpro.core.type.ner*.

### Pipeline

Include the component into your pipeline this way:

{% highlight java %}
AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class);
{% endhighlight %}

The default variant for English is _all.3class.distsim.crf_, other variants can be set by _PARAM_VARIANT_.

### Output

An example annotations output may look like this:

{% highlight text %}
INFO: === CAS ===
-- Document Text --
The headquarters of the UN is a complex in New York City.
-- Annotations --
[DocumentAnnotation] (0, 57) The headquarters of the UN is a complex in New York City.
[Organization] (24, 26) UN
[Location] (43, 56) New York City
{% endhighlight %}

## StanfordParser

The [Stanford Parser](http://www-nlp.stanford.edu/software/lex-parser.shtml) is a program that works out the grammatical structure of sentences. There are models available for many languages, e.g. Englisch and German.

### Models

There are different models for various languages, parsers include a PCFG (probabilistic context-free grammar) parser and a factored parser.

### Pipeline

Include the component into your pipeline this way:

{% highlight java %}
AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class);
{% endhighlight %}

The default variant for English is _factored_.

### Output

An example annotations output may look like this:

{% highlight text %}
INFO: === CAS ===
-- Document Text --
The UN resides in New York City.
-- Annotations --
[DocumentAnnotation] (0, 32) The UN resides in New York City.
[ROOT] (0, 32) The UN resides in New York City.
[DET] (0, 32) The UN resides in New York City.
[POBJ] (0, 32) The UN resides in New York City.
[S] (0, 32) The UN resides in New York City.
[NSUBJ] (0, 32) The UN resides in New York City.
[PREP] (0, 32) The UN resides in New York City.
[Sentence] (0, 32) The UN resides in New York City.
[NN] (0, 32) The UN resides in New York City.
[NN] (0, 32) The UN resides in New York City.
[NP] (0, 6) The UN
[ART] (0, 3) The
[NP] (4, 6) UN
[VP] (7, 31) resides in New York City
[V] (7, 14) resides
[PP] (15, 31) in New York City
[PP] (15, 17) in
[NP] (18, 31) New York City
[NP] (18, 21) New
[NP] (22, 26) York
[NP] (27, 31) City
[PUNC] (31, 32) .
{% endhighlight %}

## Consumer

{% highlight java %}
package de.tudarmstadt.ukp.dkpro.core.examples;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP;

public class NPNEWriter extends JCasConsumer_ImplBase {
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {

    /* all sentences */
    for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
 
      /* all Noun Phrases within that sentence */
      for (NP nounphrase : JCasUtil.selectCovered(aJCas, NP.class, sentence)) {
 
        /* all Named Entities within that noun phrase */
        for (NamedEntity ne : JCasUtil.selectCovered(aJCas, NamedEntity.class, nounphrase)) {

            System.out.printf("NP: '%s'\tNE: '%s'\n", nounphrase.getCoveredText(), ne.getCoveredText());

        } /* for each NamedEntity within the noun phrase */
      } /* for each noun phrase within the sentence */
    } /* for each sentence */
  } /* process() */
} /* class */
{% endhighlight %}

## Create your experiment

The entire pipeline may look like this:

{% highlight java %}
package de.tudarmstadt.ukp.dkpro.core.examples;

import java.io.IOException;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

public class StanfordCoreComponents 
{
  public static void main(String[] args) throws UIMAException, IOException 
  {
    CollectionReader reader = createReader(
        TextReader.class,
        TextReader.PARAM_SOURCE_LOCATION, "src/main/resources",
        TextReader.PARAM_LANGUAGE, "en",
        TextReader.PARAM_PATTERNS, new String[] { "[+]*.txt" });

    AnalysisEngineDescription seg = createEngineDescription(StanfordSegmenter.class);

    AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

    AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class);

    AnalysisEngineDescription writer = createEngineDescription(NPNEWriter.class);

    SimplePipeline.runPipeline(reader, seg, ner, parser, writer);
  } /* main() */
} /* class */
{% endhighlight %}

### Output

The final output prints the noun phrases (NP) and the named entities (NE) within them. An example may look like this:

{% highlight text %}
NP Anna NE Anna
NP the beautiful New York   NE New York
NP Berlin   NE Berlin
NP The UN   NE UN
NP New York City    NE New York City
{% endhighlight %}

## Source

You can find the current sources for this recipe [here](https://github.com/dkpro/dkpro-core-examples).