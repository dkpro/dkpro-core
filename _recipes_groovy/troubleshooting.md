---
title: Groovy trouble-shooting
hidden: true
---

* TOC
{:toc}

## Making a Groovy script executable on Linux/OS X

To make a Groovy script executable on Linux or OS X, add the following line as the very first line
in the script:

{% highlight text %}
#!/usr/bin/env groovy
{% endhighlight %} 

Then make the file executable using the `chmod` command, e.g.

{% highlight text %}
$ chmod +x myscript.groovy
{% endhighlight %} 

## Out of memory

If a script complains about not having enough heap, try the command 

{% highlight text %}
Linux:    export JAVA_OPTS="-Xmx1g"
{% endhighlight %} 

and then run the script again.

## Verbose dependency resolving

Normally, grape resolves dependencies quietly. If a script has many dependencies, that can mean the script may be running for a long time without any visible output on screen, looking like it is hanging. What it really does is downloading the dependencies. To enable verbose output during the dependency resolving phase, set JAVA_OPTS:

{% highlight text %}
Linux:    export JAVA_OPTS="-Dgroovy.grape.report.downloads=true $JAVA_OPTS"
Windows:  set JAVA_OPTS="-Dgroovy.grape.report.downloads=true %JAVA_OPTS%"
{% endhighlight %} 

## Flush caches

The scripts download required models and libraries automatically. Sometimes it may be necessary to flush the cache folders. There are two cache folders that you can clear to force re-downloading the dependencies:

   * `~/.groovy/grapes` - dependencies referenced from the Groovy scripts are stored here
   * `~/.ivy2/cache` - models and resources dynamically downloaded by DKPro Core components

If you use Maven for software development, we recommend that you separate the caches (see below). If you did not separate the Groovy cache from the Maven cache, you might also want to consider deleting `~/.m2/repository`.

## Separate Groovy Grape Cache from Maven Cache

On some systems, Groovy per default re-uses artifacts that have already been downloaded by Maven. To make sure the Groovy Grape cache is fully separate from the Maven cache, create a file called `grapeConfig.xml` in your `~\.groovy` folder with this content

{% highlight xml %}
<?xml version="1.0"?>
<ivysettings>
    <settings defaultResolver="downloadGrapes"/>
    <resolvers>
        <chain name="downloadGrapes">
            <!-- todo add 'endorsed groovy extensions' resolver here -->
            <filesystem name="cachedGrapes">
                <ivy pattern="${user.home}/.groovy/grapes/[organisation]/[module]/ivy-[revision].xml"/>
                <artifact pattern="${user.home}/.groovy/grapes/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]"/>
            </filesystem>
            <ibiblio name="codehaus" root="http://repository.codehaus.org/" m2compatible="true"/>
            <ibiblio name="ibiblio" m2compatible="true"/>
            <ibiblio name="java.net2" root="http://download.java.net/maven/2/" m2compatible="true"/>
        </chain>
    </resolvers>
</ivysettings>
{% endhighlight %} 

Then flush the cache by deleting the folder `~/.groovy/grapes`. Mind that the next time you run a Groovy script, it will take some more time, because the cache needs to be repopulated.

## Groovy 2.3.0 aka _"JCas type used in Java code,  but was not declared in the XML type descriptor"_

When running our scripts within Groovy 2.3.0, uimaFIT's automatic type detection mechanism does not work. This leads to an error message like this:

{% highlight text %}
JCas type "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" used in Java code,  
  but was not declared in the XML type descriptor.
{% endhighlight %} 

This problem does not occur with Groovy 2.1.x, 2.2.x or 2.3.1 and higher.

Thanks to Evan for [reporting](http://stackoverflow.com/questions/23504261/dkpro-groovy-usage-and-installation-with-uima) this problem.

## Using SNAPSHOT versions

To use SNAPSHOT versions of DKPro Core, add the following line before the first `@Grab` line:

{% highlight groovy %}
@GrabResolver(name='ukp-oss-snapshots',
      root='http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-snapshots')
{% endhighlight %} 

Then change the versions of all DKPro Core components to the SNAPSHOT version that you wish to use. It is strongly recommended not to mix versions!