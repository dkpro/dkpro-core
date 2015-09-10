---
title: Normalizing a text with UmlautNormalizer
subheadline: Analytics
---

Takes a text and checks for umlauts written as "ae", "oe", or "ue" and normalizes them if they really are umlauts.

{% highlight groovy %}
#!/usr/bin/env groovy

@GrabResolver(name='ukp-oss-releases',
      root='http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-releases')

@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.6.1',
     module='de.tudarmstadt.ukp.dkpro.core.umlautnormalizer-asl')

@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='1.6.1',
     module='de.tudarmstadt.ukp.dkpro.core.opennlp-asl')

@Grab(group='de.tudarmstadt.ukp.dkpro.core', version='20121116.0',
     module='de.tudarmstadt.ukp.dkpro.core.umlautnormalizer-model-normalizer-de-default')

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription

import org.apache.uima.analysis_engine.AnalysisEngine
import org.apache.uima.fit.factory.AggregateBuilder
import org.apache.uima.jcas.JCas

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData
import de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter
import de.tudarmstadt.ukp.dkpro.core.umlautnormalizer.UmlautNormalizer


def builder = new AggregateBuilder()
builder.add(createEngineDescription(OpenNlpSegmenter))
builder.add(createEngineDescription(UmlautNormalizer))
builder.add(createEngineDescription(ApplyChangesAnnotator), "source",
        "_InitialView", "target", "umlaut_cleaned")

def engine = builder.createAggregate()

def text = "Die Buechsenoeffner koennen oefter benuetzt werden. Neuerscheinungen muss " +
                "der kaeufer kaufen. Schon zum Fruehstueck traf er auf den Maerchenerzaehler, " +
                "den Uebergeek und den Ueberraschungeioeffner. Sein Oeuvre ist beeindruckend."
def jcas = engine.newJCas()
jcas.setDocumentText(text)
jcas.setDocumentLanguage("de")
DocumentMetaData.create(jcas)

engine.process(jcas)

def view = jcas.getView("umlaut_cleaned")
println view.getDocumentText()
{% endhighlight %}

Example output:

{% highlight text %}
Jul 03, 2014 10:29:49 AM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase info
Information: :: loading settings :: url = jar:file:/usr/share/groovy/lib/ivy.jar!/org/apache/ivy/core/settings/ivysettings.xml
Jul 03, 2014 10:29:49 AM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase loadResource
Information: Producing resource from jar:file:/home/santos/.ivy2/cache/de.tudarmstadt.ukp.dkpro.core/de.tudarmstadt.ukp.dkpro.core.opennlp-upstream-sentence-de-maxent/jars/de.tudarmstadt.ukp.dkpro.core.opennlp-upstream-sentence-de-maxent-20120616.jar!/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-de-maxent.bin
Jul 03, 2014 10:29:49 AM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase info
Information: :: loading settings :: url = jar:file:/usr/share/groovy/lib/ivy.jar!/org/apache/ivy/core/settings/ivysettings.xml
Jul 03, 2014 10:29:49 AM de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase loadResource
Information: Producing resource from jar:file:/home/santos/.ivy2/cache/de.tudarmstadt.ukp.dkpro.core/de.tudarmstadt.ukp.dkpro.core.opennlp-upstream-token-de-maxent/jars/de.tudarmstadt.ukp.dkpro.core.opennlp-upstream-token-de-maxent-20120616.jar!/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/token-de-maxent.bin                                                                                                                                                     
Buechsenoeffner - 0                                                                                                                                                                                                                 
Büchsenöffner - 1732                                                                                                                                                                                                                
koennen - 831456                                                                                                                                                                                                                    
können - 97598630                                                                                                                                                                                                                   
oefter - 24194                                                                                                                                                                                                                      
öfter - 988405                                                                                                                                                                                                                      
benuetzt - 2058                                                                                                                                                                                                                     
benützt - 98690                                                                                                                                                                                                                     
Neuerscheinungen - 905024                                                                                                                                                                                                           
Neürscheinungen - 0                                                                                                                                                                                                                 
kaeufer - 2344                                                                                                                                                                                                                      
käufer - 30327                                                                                                                                                                                                                      
Fruehstueck - 104788                                                                                                                                                                                                                
Frühstück - 2249076                                                                                                                                                                                                                 
Maerchenerzaehler - 310                                                                                                                                                                                                             
Märchenerzähler - 15785                                                                                                                                                                                                             
Uebergeek - 0                                                                                                                                                                                                                       
Übergeek - 0                                                                                                                                                                                                                        
Ueberraschungeioeffner - 0                                                                                                                                                                                                          
Überraschungeiöffner - 0                                                                                                                                                                                                            
Oeuvre - 0                                                                                                                                                                                                                          
Öuvre - 0                                                                                                                                                                                                                           
Jul 03, 2014 10:29:50 AM de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator applyChanges(81)                                                                                                                     
Information: Found 10 changes                                                                                                                                                                                                       
Adding from [_InitialView] to [umlaut_cleaned] on [510065709]
Die Büchsenöffner können öfter benützt werden. Neuerscheinungen muss der käufer kaufen. Schon zum Frühstück traf er auf den Märchenerzähler, den Uebergeek und den Ueberraschungeioeffner. Sein Oeuvre ist beeindruckend.
{% endhighlight %}
