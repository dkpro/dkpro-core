---
title: Convert a corpus in Penn Treebank bracketed format to Tiger XML
subheadline: Conversion
---

Reads each Penn Treebank bracketed format file from the corpus in the specified folder and writes them to the target folder with Tiger XML format.

Copy the script to a file called "penn2tiger.groovy" and call it e.g. using `groovy penn2tiger.groovy pennTreebankFile.txt .`. This creates a file called `pennTrebankFile.xml` in Tiger XML format in the current directory.

{% highlight groovy %}
#!/usr/bin/env groovy
@GrabResolver(name='ukp-oss-snapshots',
     root='http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-snapshots')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
  module='de.tudarmstadt.ukp.dkpro.core.io.tiger-asl', 
  version='1.7.1-SNAPSHOT')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
  module='de.tudarmstadt.ukp.dkpro.core.io.penntree-asl', 
  version='1.7.1-SNAPSHOT')
 
import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.io.tiger.*;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.*;
 
// Assemble and run pipeline
runPipeline(
  createReaderDescription(PennTreebankCombinedReader,
    PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, args[0]),
  createEngineDescription(TigerXmlWriter,
    TigerXmlWriter.PARAM_TARGET_LOCATION, args[1],
    TigerXmlWriter.PARAM_STRIP_EXTENSION, true))
{% endhighlight %}

**Note:** If the script fails, check that any line that does not start a sentence is indented. If necessary, add a space at the beginning of a line.

**Note:** This script uses DKPro Core 1.7.1-SNAPSHOT because of the following two issues present in version 1.7.0.

   * If the Penn Treebank file is malformed, an !EmptyStackException will be thrown (Issue 613)
   * A file is malformed e.g.:
      * if the brackets do not balance
      * if the tree is not properly indented, in particular, if a line that is not indented is not the start of a tree (alleviated but not fixed by Issue 612)