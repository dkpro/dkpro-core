---
layout: page-fullwidth
title: "Intro using Java"
---

## Installing Java and Eclipse

These steps install the basis system requirements needed to implement DKPro Core pipelines using the Java language. They need to be performed only once.

   * Download and install the Java SE Development Kit 7 from the [Oracle Java Site][1]
   * Download and install the *Eclipse IDE for Java Developers* from the [Eclipse website][2]
      * The Eclipse IDE for Java Developers already contains support for the Java language and the Maven plugin that we require. Of course you can use any other Eclipse distribution that supports Java and manually install the Maven plugin.

## Running the pipeline

For a start, let's try a simple analysis pipeline:

   * Read an English text file called "document.txt"
   * Perform tokenization and sentence boundary detection using OpenNLP
   * Perform lemmatization using !LanguageTool
   * Perform dependency parsing using !MaltParser
   * Write the result to disk in CoNLL 2006 format

Here is how to run that:

   * Open Eclipse
   * Create a new Maven project
   * Open the file *pom.xml*, switch to the tab *Dependencies* 
   * Add the following dependencies

| *Group Id*                    | *Artifact Id*                                  | *Version* |
| de.tudarmstadt.ukp.dkpro.core | de.tudarmstadt.ukp.dkpro.core.opennlp-asl      | 1.6.2     |
| de.tudarmstadt.ukp.dkpro.core | de.tudarmstadt.ukp.dkpro.core.languagetool-asl | 1.6.2     |
| de.tudarmstadt.ukp.dkpro.core | de.tudarmstadt.ukp.dkpro.core.maltparser-asl   | 1.6.2     |
| de.tudarmstadt.ukp.dkpro.core | de.tudarmstadt.ukp.dkpro.core.io.text-asl      | 1.6.2     |
| de.tudarmstadt.ukp.dkpro.core | de.tudarmstadt.ukp.dkpro.core.io.conll-asl     | 1.6.2     |

   * Create a new class file called *Pipeline.java* in the folder *src/main/java* and copy/paste the code below
   * Create a new text file called *document.txt* in the project root
   * Run the class *Pipeline* in the package *example*
   * Right-click on the project folder and select *Refresh* to see the file created by the pipeline

{% highlight java %}
package example;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Writer;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

public class Pipeline {

  public static void main(String[] args) throws Exception {
    runPipeline(
        createReaderDescription(TextReader.class,
            TextReader.PARAM_SOURCE_LOCATION, "document.txt",
            TextReader.PARAM_LANGUAGE, "en"),
        createEngineDescription(OpenNlpSegmenter.class),
        createEngineDescription(OpenNlpPosTagger.class),
        createEngineDescription(LanguageToolLemmatizer.class),
        createEngineDescription(MaltParser.class),
        createEngineDescription(Conll2006Writer.class,
            Conll2006Writer.PARAM_TARGET_LOCATION, "."));
  }
}
{% endhighlight java %}

The result is written to a file called *document.txt.conll* and could look something like this:

{% highlight text %}
1	Pierre	Pierre	NNP	NNP	_	2	nn	_	_
2	Vinken	Vinken	NNP	NNP	_	9	nsubj	_	_
3	,	,	,	,	_	2	punct	_	_
4	61	61	CD	CD	_	5	num	_	_
5	years	year	NNS	NNS	_	6	measure	_	_
6	old	old	JJ	JJ	_	2	amod	_	_
7	,	,	,	,	_	2	punct	_	_
8	will	will	MD	MD	_	9	aux	_	_
9	join	join	VB	VB	_	0	_	_	_
10	the	the	DT	DT	_	11	det	_	_
11	board	board	NN	NN	_	9	dobj	_	_
12	as	as	IN	IN	_	9	prep	_	_
13	a	a	DT	DT	_	15	det	_	_
14	nonexecutive	nonexecutive	JJ	JJ	_	15	amod	_	_
15	director	director	NN	NN	_	12	pobj	_	_
16	Nov.	Nov.	NNP	NNP	_	15	dep	_	_
17	29	29	CD	CD	_	16	num	_	_
18	.	.	.	.	_	9	punct	_	_
{% endhighlight text %}

[1]: http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
[2]: http://eclipse.org/downloads/
