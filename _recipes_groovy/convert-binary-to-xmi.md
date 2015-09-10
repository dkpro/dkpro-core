---
title: Convert serialized/binary CAS to XMI
subheadline: Conversion
---

Reads each binary/serialized CAS file from the corpus in the specified folder and writes them to the target folder using the UIMA XMI format.

Copy the script to a file called `binary2xmi.groovy` and call it e.g. using `groovy binary2xmi.groovy binaryCasFile.ser .`. This creates a file called in UIMA XMI format in the current directory. The name of the created file depends on the document metadata encoded in the binary CAS and may well be different from the file name of the binary CAS! A type-system file will be written to the same folder. This will allow you to open load the file e.g. using the UIMA CAS Editor plugin for Eclipse.

{% highlight groovy %}
#!/usr/bin/env groovy
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
  module='de.tudarmstadt.ukp.dkpro.core.io.bincas-asl', 
  version='1.7.0')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', 
  module='de.tudarmstadt.ukp.dkpro.core.io.xmi-asl', 
  version='1.7.0')
 
import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.*;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.*;
 
// Assemble and run pipeline
runPipeline(
  createReaderDescription(BinaryCasReader,
    BinaryCasReader.PARAM_SOURCE_LOCATION, args[0]),
  createEngineDescription(XmiWriter,
    XmiWriter.PARAM_TARGET_LOCATION, args[1],
    XmiWriter.PARAM_STRIP_EXTENSION, true))
{% endhighlight %}
