---
title: TreeTagger part-of-speech tagging and lemmatizing
subheadline: Analytics
---

Reads files from the specified directory and prints the result to the console.

### TreeTagger Installation for Linux
 * Go to the [TreeTagger website](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/)
 * From the _download_ section, download the correct tagger package, i.e. _PC-Linux_
    * Extract the .gz archive
    * Copy the `tree-tagger-linux-3.2/bin/tree-tagger` file and place it in the same folder as the script `treetagger.py`
 * From the _parameter file_ section, download the correct model. For the example below download _English parameter file_ (`english-par-linux-3.2-utf8.bin.gz`)
    * Unzip the file (e.g. `gunzip english-par-linux-3.2-utf8.bin.gz`)
    * Copy the file `english-par-linux-3.2-utf8.bin` into the same folder as the `treetagger.py` script. Ensure that the name for the model is `english-par-linux-3.2-utf8.bin`

### TreeTagger Installation for Windows 7
 * Ensure that you have a program to unzip `.gz` files. For example you can use [http://www.7-zip.org 7zip]
 * Go to the [TreeTagger website](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/)
 * In the _Windows_ section, you find the download link for the `tree-tagger-windows-3.2.zip` file.
    * Extract the zip-archive
    * Copy the `tree-tagger-windows-3.2/bin/tree-tagger.exe` to your folder with with the `treetagger.py` script
 * From the _parameter file_ section, download the correct model. For the example below download _English parameter file_ (`english-par-linux-3.2-utf8.bin.gz`)
    * Unzip the file (e.g. by using [7zip](http://www.7-zip.org))
    * Copy the file `english-par-linux-3.2-utf8.bin` into the same folder as the `treetagger.py` script. Ensure that the name for the model is `english-par-linux-3.2-utf8.bin`
 * In the script below, you find a line `TreeTaggerPosLemmaTT4J.PARAM_EXECUTABLE_PATH, "tree-tagger"`, change the value `tree-tagger` to `tree-tagger.exe` 

If you already have TreeTagger installed on your system and or if you want to use another model file, you can also set in the script the parameters `PARAM_EXECUTABLE_PATH` and `PARAM_MODEL_PATH` to their respective locations.

Call with `C:\jython-2.7b1\jython treetagger.py <foldername> <language>`, e.g. `C:\jython-2.7b1\jython treetagger.py  C:\example_folder\ en`.

{% highlight python %}
#!/usr/bin/env jython
# Fix classpath scanning - otherise uimaFIT will not find the UIMA types
from java.lang import Thread
from org.python.core.imp import *
Thread.currentThread().contextClassLoader = getSyspathJavaLoader()

# Dependencies and imports for DKPro modules
from jip.embed import require
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.1')
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.treetagger-asl:1.6.1')
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.io.text-asl:1.6.1')
from de.tudarmstadt.ukp.dkpro.core.opennlp import *
from de.tudarmstadt.ukp.dkpro.core.treetagger import *
from de.tudarmstadt.ukp.dkpro.core.io.text import *
from de.tudarmstadt.ukp.dkpro.core.api.segmentation.type import *

# uimaFIT imports
from org.apache.uima.fit.util.JCasUtil import *
from org.apache.uima.fit.pipeline.SimplePipeline import *
from org.apache.uima.fit.factory.CollectionReaderFactory import *
from org.apache.uima.fit.factory.AnalysisEngineFactory import *

# Access to commandline arguments
import sys

# Assemble and run pipeline
pipeline = iteratePipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, sys.argv[1],
    TextReader.PARAM_LANGUAGE, sys.argv[2],
    TextReader.PARAM_ENCODING, "ISO-8859-1",
    TextReader.PARAM_PATTERNS, "*.txt"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(TreeTaggerPosLemmaTT4J,
    TreeTaggerPosLemmaTT4J.PARAM_EXECUTABLE_PATH, "tree-tagger", #!! Change to "tree-tagger.exe" if the script is executed under windows !!
    TreeTaggerPosLemmaTT4J.PARAM_MODEL_PATH, "english-par-linux-3.2-utf8.bin",
    TreeTaggerPosLemmaTT4J.PARAM_MODEL_ENCODING, "UTF-8"));

for jcas in pipeline:
  for token in select(jcas, Token):
    print token.coveredText + " " + token.pos.posValue + " " + token.lemma.value
{% endhighlight  %}

Example output:

{% highlight text %}
The DT the
quick JJ quick
brown JJ brown
fox NN fox
jumps NNS jump
over IN over
the DT the
lazy JJ lazy
dog NN dog
. SENT .
{% endhighlight %}

