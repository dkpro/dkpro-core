---
layout: page-fullwidth
title: "Intro using Groovy"
---

<div class="flex-video">
<iframe width="560" height="315" src="https://www.youtube.com/embed/v51BJEQohoY" frameborder="0" allowfullscreen></iframe>
</div>

## Installing Java and Groovy

These steps install the basis system requirements needed to implement DKPro Core pipelines using the [http://groovy.codehaus.org Groovy] language. They need to be performed only once.

   * Download and install the Java SE Development Kit 7 from the [Oracle Java Site][1]
   * Windows: download and run the Windows Installer from the [Groovy homepage][2]
   * Linux/OS X: Open a terminal which we will use to install Groovy using [gvm][3]
      * `curl -s "https://get.sdkman.io" | bash`
      * Open a new terminal window to activate gvm and in the new window enter
      * `gvm install groovy`

## Running the pipeline

For a start, let's try a simple analysis pipeline:

   * Read an English text file called "document.txt"
   * Perform tokenization and sentence boundary detection using OpenNLP
   * Perform lemmatization using LanguageTool
   * Perform dependency parsing using MaltParser
   * Write the result to disk in CoNLL 2006 format

Here is how to run that:

   * Open a text editor and copy/paste the following script into it.
   * Save the file under the name *pipeline.groovy*.
   * Create another text file in the editor, write some English text into it, and save under the name *document.txt*.
   * Open a command line in the directory to which you saved the two files
   * Invoke the script using the command `groovy pipeline.groovy`
      * This will take quite a while the first time because the software components and models are downloaded

{% highlight groovy %}
#!/usr/bin/env groovy
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.6.2',
      module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl')
import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.6.2',
      module='de.tudarmstadt.ukp.dkpro.core.languagetool-asl')
import de.tudarmstadt.ukp.dkpro.core.languagetool.*;
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.6.2',
      module='de.tudarmstadt.ukp.dkpro.core.maltparser-asl')
import de.tudarmstadt.ukp.dkpro.core.maltparser.*;
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.6.2',
      module='de.tudarmstadt.ukp.dkpro.core.io.text-asl')
import de.tudarmstadt.ukp.dkpro.core.io.text.*;
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.6.2',
      module='de.tudarmstadt.ukp.dkpro.core.io.conll-asl')
import de.tudarmstadt.ukp.dkpro.core.io.conll.*;

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;

runPipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_SOURCE_LOCATION, "document.txt",
    TextReader.PARAM_LANGUAGE, "en"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger),
  createEngineDescription(LanguageToolLemmatizer),
  createEngineDescription(MaltParser),
  createEngineDescription(Conll2006Writer,
    Conll2006Writer.PARAM_TARGET_LOCATION, "."));
{% endhighlight %}

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
{% endhighlight %}

## Where to go from here?

You can find many more examples of what you can do with DKPro Core and Groovy on our [Groovy recipes for DKPro Core pipelines][recipes] page

[1]: http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html 
[2]: http://www.groovy-lang.org/download.html
[3]: http://gvmtool.net
[recipes]: {{ site.url }}/groovy/recipes/
