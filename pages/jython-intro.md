---
layout: page-fullwidth
title: "Intro using Jython"
---

## Installing Java, Jython, and jip

These steps install the basis system requirements needed to implement DKPro Core pipelines  using the Python language. They need to be performed only once.

   * Download and install the Java SE Development Kit 7 from the [Oracle Java Site][jdk]
   * Download the *Jython 2.7beta 3* traditional installer from [here][jython-installer]
   * Double-click on the *jython-installer-2.7-b3.jar* to start the installer
   * Install with all the default settings. This should install Jython to `C:\jython2.7b3`  
   * Download [jip][jip] (version 0.8.3 or higher)
   * Unpack `jip-0.8.3.zip ` to `C:\`
   * Open a command line window
      * Go to the jip folder: `cd C:\jip` 
      * Install jip: `C:\jython2.7b3\jython setup.py install`
      * Close the window
      * Now you can delete the folder `C:\jip` and the file `jip-0.8.3.zip ` again

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

{% highlight python %}
#!/usr/bin/env jython
# Fix classpath scanning - otherise uimaFIT will not find the UIMA types
from java.lang import Thread
from org.python.core.imp import *
Thread.currentThread().contextClassLoader = getSyspathJavaLoader()

# Dependencies and imports for DKPro modules
from jip.embed import require
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.2')
from de.tudarmstadt.ukp.dkpro.core.opennlp import *
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.languagetool-asl:1.6.2')
from de.tudarmstadt.ukp.dkpro.core.languagetool import *
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.maltparser-asl:1.6.2')
from de.tudarmstadt.ukp.dkpro.core.maltparser import *
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.io.text-asl:1.6.2')
from de.tudarmstadt.ukp.dkpro.core.io.text import *
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.io.conll-asl:1.6.2')
from de.tudarmstadt.ukp.dkpro.core.io.conll import *

# uimaFIT imports
from org.apache.uima.fit.pipeline.SimplePipeline import *
from org.apache.uima.fit.factory.AnalysisEngineFactory import *
from org.apache.uima.fit.factory.CollectionReaderFactory import *

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
{% endhighlight python %}

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


## Where to go from here?

You can find many more examples of what you can do with DKPro Core and Python on our  [Python scripts for DKPro Core pipelines][jython-cookbook] page

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
[jip]: https://pypi.python.org/pypi/jip
[jython-installer]: http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.7-b3/jython-installer-2.7-b3.jar
[jython-cookbook]: jython-cookbook.html

