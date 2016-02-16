---
title: Jython trouble-shooting
hidden: true
---

* TOC
{:toc}

## Fixing classpath scanning in Jython

Each Jython script require these commands at the beginning of the script to allow uimaFIT access to the dynamically imported dependencies (see below).

{% highlight python %}
# Fix classpath scanning - otherise uimaFIT will not find the UIMA types
from java.lang import Thread
from org.python.core.imp import *
Thread.currentThread().contextClassLoader = getSyspathJavaLoader()
{% endhighlight %}

*Note:* A feature request to set the context classloader automatically when entering a Jython 
context has been filed upstream ([Jython issue 2142](http://bugs.jython.org/issue2142)).

## Out of memory

If a script complains about not having enough heap, try the command 

{% highlight text %}
Linux:    export JAVA_MEM="-Xmx1g"
{% endhighlight %}

and then run the script again.

## expr: syntax error - if jython is a symlink

If you set up a symlink to the `jython`command, you may be seeing the error `expr: syntax error` whenever you run a script. This has been discussed [http://sourceforge.net/p/jython/mailman/message/31323567/ here]. The solution is to open the file `jython`in a text editor, locate the line

{% highlight bash %}
if expr "$link" : '/' > /dev/null; then
{% endhighlight %}

and replace it with the line

{% highlight bash %}
if expr "$link" : '[/]' > /dev/null; then
{% endhighlight %}

## Verbose dependency resolving

Normally, grape resolves dependencies quietly. If a script has many dependencies, that can mean the script may be running for a long time without any visible output on screen, looking like it is hanging. What it really does is downloading the dependencies. To enable verbose output during the dependency resolving phase, set JAVA_OPTS:

{% highlight text %}
Linux:    export JYTHON_OPTS="-Dgroovy.grape.report.downloads=true $JAVA_OPTS"
Windows:  set JYTHON_OPTS="-Dgroovy.grape.report.downloads=true %JAVA_OPTS%"
{% endhighlight %}

## Flush caches

The scripts download required models and libraries automatically. Sometimes it may be necessary to flush the cache folders. There are two cache folders that you can clear to force re-downloading the dependencies:

* `~/.jip/cache` - dependencies referenced from the Jython scripts are store here
* `~/.ivy2/cache` - models and resources dynamically downloaded by DKPro Core components

## Using SNAPSHOT versions

To use SNAPSHOT versions of DKPro Core, add the following lines before the `require(...)` lines used to fetch the dependencies.

{% highlight python %}
# Enable snapshot dependencies
from jip.repository import repos_manager
repos_manager.add_repos("ukp-oss-snapshots", "http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-snapshots", "remote")
{% endhighlight %}

Then change the versions of all DKPro Core components to the SNAPSHOT version that you wish to use. It is strongly recommended not to mix versions!

## Updating Jython to use JDK 7

Running the TreeTagger example might throw an exception like:

{% highlight text %}
java.lang.UnsupportedClassVersionError: java.lang.UnsupportedClassVersionError:
de/tudarmstadt/ukp/dkpro/core/opennlp/OpenNlpSegmenter : Unsupported major.minor version 51.0
{% endhighlight %}

This is due fact that Jython is using JDK 6 instead of JDK 7. Ensure that JDK 7 is installed. For Linux systems, you might need to update the file `/usr/bin/jython`. In the 6th line you find `JAVA_HOME="/usr/lib/jvm/java-6-openjdk-amd64/jre"`. Replace the `java-6` with `java-7` and it should work. To check the Java version that is used by Jython, execute `jython -J-version`

## UnicodeEncodeError: 'charmap' codec can't encode character ....

You may get an exception like _UnicodeEncodeError: 'charmap' codec can't encode character ...._ when writing Unicode texts to the console on systems such as Windows where UTF-8 is not the default system encoding. To fix this, you can override the Jython console encoding:

{% highlight text %}
Windows:  set JYTHON_OPTS="-Dpython.console.encoding=UTF8 %JAVA_OPTS%"
{% endhighlight %}

# Known problems

## Custom UIMA components

It is currently not possible to define new UIMA component classes (readers or analysis engines) in Python. The respective classes cannot be found and instantiated by the UIMA framework. The following script will therefore produce an exception:

{% highlight python %}
#!/usr/bin/env jython
# Fix classpath scanning - otherise uimaFIT will not find the UIMA types
from java.lang import Thread
from org.python.core.imp import *
Thread.currentThread().contextClassLoader = getSyspathJavaLoader()

# Dependencies and imports for DKPro modules
from jip.embed import require
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.1')
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.io.text-asl:1.6.1')
from de.tudarmstadt.ukp.dkpro.core.opennlp import *
from de.tudarmstadt.ukp.dkpro.core.io.text import *
from de.tudarmstadt.ukp.dkpro.core.api.segmentation.type import *
from de.tudarmstadt.ukp.dkpro.core.api.syntax.type import *

# uimaFIT imports
from org.apache.uima.fit.util.JCasUtil import *
from org.apache.uima.fit.pipeline.SimplePipeline import *
from org.apache.uima.fit.factory.CollectionReaderFactory import *
from org.apache.uima.fit.factory.AnalysisEngineFactory import *
from org.apache.uima.fit.factory.AnalysisEngineFactory import *
from org.apache.uima.fit.component import *
from org.apache.uima.jcas import JCas

# Access to commandline arguments
import sys

# Custom writer class used at the end of the pipeline to write results to screen
class Writer(JCasAnnotator_ImplBase):
  def process(self, *args):
    if !isinstance(args[0], JCas):
      apply(JCasAnnotator_ImplBase.read, (self,)+args)

    jcas = args[0]
    for token in select(jcas, Token):
      print token.coveredText + " " + token.pos.posValue

# Pipeline
runPipeline(
  createReaderDescription(TextReader,
    TextReader.PARAM_PATH, sys.argv[1],
    TextReader.PARAM_LANGUAGE, sys.argv[2],
    TextReader.PARAM_PATTERNS, "*.txt"),
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(OpenNlpPosTagger),
  createEngineDescription(Writer))
{% endhighlight %}

The produced exception is this:

{% highlight text %}
org.apache.uima.resource.ResourceInitializationException:
Annotator class "org.python.proxies.__main__$Writer$1" was not found. (Descriptor: <unknown>)
{% endhighlight %}

Related issue: [https://issues.apache.org/jira/browse/UIMA-3692 UIMA-3692]

# Comparison to Groovy scripts

The Python (Jython) scripts are very similar to the Groovy scripts. The main differences (except for the different syntax) are:

### Dependencies

**Jython**

{% highlight python %}
from jip.embed import require # only required once
require('de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.1')
from de.tudarmstadt.ukp.dkpro.core.opennlp import *
{% endhighlight %}

**Groovy**

{% highlight groovy %}
@Grab(group='de.tudarmstadt.ukp.dkpro.core', module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl', version='1.6.1')
import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
{% endhighlight %}

### Custom UIMA components

It is currently not possible to define custom UIMA components in Jython scripts. In Groovy scripts, this is possible.