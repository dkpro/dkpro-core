---
title: Decompounding without a ranker resource
subheadline: Analytics
---
Uses the `LeftToRightSplitter` as the splitter resource and no ranker resource, decompounds the compounds in a sentence after tokenizing it, then print the tokens and each compound part.

{% highlight groovy %}
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.7.0',
     module='de.tudarmstadt.ukp.dkpro.core.decompounding-asl')
@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.7.0',
     module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl')

import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.annotator.*;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.*;

import de.tudarmstadt.ukp.dkpro.core.opennlp.*;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.*;

import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.factory.JCasFactory.*;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.factory.ExternalResourceFactory.*;
import static org.apache.uima.fit.util.JCasUtil.*;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.*;

def doc = createJCas();
doc.documentText = "Wir brauchen einen Aktionsplan."
doc.documentLanguage = "de";

runPipeline(doc,
  createEngineDescription(OpenNlpSegmenter),
  createEngineDescription(
    CompoundAnnotator,
    CompoundAnnotator.PARAM_SPLITTING_ALGO,
      createExternalResourceDescription(
        LeftToRightSplitterResource,
        (Object) LeftToRightSplitterResource.PARAM_DICT_RESOURCE,
          createExternalResourceDescription(SharedDictionary),
        LeftToRightSplitterResource.PARAM_MORPHEME_RESOURCE,
          createExternalResourceDescription(SharedLinkingMorphemes))));

println select(doc, Token).collect { it.coveredText }

println select(doc, CompoundPart).collect { it.coveredText }
{% endhighlight %}

Example output:

{% highlight text %}
Jul 02, 2014 4:52:49 PM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase info
Information: :: loading settings :: url = jar:file:/usr/share/groovy/lib/ivy.jar!/org/apache/ivy/core/settings/ivysettings.xml
Jul 02, 2014 4:52:49 PM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase configure
Information: Producing resource from [jar:file:/home/santos/.ivy2/cache/de.tudarmstadt.ukp.dkpro.core/de.tudarmstadt.ukp.dkpro.core.opennlp-upstream-sentence-de-maxent/jars/de.tudarmstadt.ukp.dkpro.core.opennlp-upstream-sentence-de-maxent-20120616.jar!/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-de-maxent.bin] redirected from [jar:file:/home/santos/.ivy2/cache/de.tudarmstadt.ukp.dkpro.core/de.tudarmstadt.ukp.dkpro.core.opennlp-model-sentence-de-maxent/jars/de.tudarmstadt.ukp.dkpro.core.opennlp-model-sentence-de-maxent-20120616.1.jar!/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-de-maxent.properties]
Jul 02, 2014 4:52:49 PM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase loadResource
Information: Producing resource took 55ms
Jul 02, 2014 4:52:49 PM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase info
Information: :: loading settings :: url = jar:file:/usr/share/groovy/lib/ivy.jar!/org/apache/ivy/core/settings/ivysettings.xml
Jul 02, 2014 4:52:50 PM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase configure
Information: Producing resource from [jar:file:/home/santos/.ivy2/cache/de.tudarmstadt.ukp.dkpro.core/de.tudarmstadt.ukp.dkpro.core.opennlp-upstream-token-de-maxent/jars/de.tudarmstadt.ukp.dkpro.core.opennlp-upstream-token-de-maxent-20120616.jar!/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/token-de-maxent.bin] redirected from [jar:file:/home/santos/.ivy2/cache/de.tudarmstadt.ukp.dkpro.core/de.tudarmstadt.ukp.dkpro.core.opennlp-model-token-de-maxent/jars/de.tudarmstadt.ukp.dkpro.core.opennlp-model-token-de-maxent-20120616.1.jar!/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/token-de-maxent.properties]
Jul 02, 2014 4:52:50 PM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase loadResource
Information: Producing resource took 257ms
[Wir, brauchen, einen, Aktionsplan, .]
[Aktion, plan]
{% endhighlight %}