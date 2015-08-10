---
layout: page-fullwidth
title: "Setting up Maven and Eclipse for DKPro Core development"
---

## System Requirements

* [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version 1.8 or higher.
* [Eclipse][ECLIPSE] version 4.4 or higher. We recommend the _Eclipse IDE for Java Developers_ distribution.

Different version of DKPro Core have different version requirements.

### For Windows Users

*Note:* On your machine the Java path may be different, e.g. because you are using a localized Windows version it may be `C:\Programme\...` -or- because you may have a Java version other than 1.8.0.51.

* Edit your `eclipse.ini` and add/change the following lines (the `-vmargs` line should be present already):

~~~
-vm
C:/Program Files/Java/jdk1.8.0_51/bin/javaw.exe
-vmargs
~~~

  * Make sure that the linebreaks are as shown above (the formatting is actually necessary)
* Open Eclipse
  * Open the *preferences*
  * Go to *Java -> Installed JREs*
  * Click *Search* and choose your Java directory
  * *Close* the preferences and *re-open* them
  * *Select* _jdk1.8.0_51_ as your JRE (this should match the entry you added in the eclipse.ini)

## Maven Setup

*Since version 1.4.0, DKPro Core is available from [Maven Central][MAVEN_CENTRAL].* 

If you do not want rely on our automatic download mechanism for models and instead want to add models as dependencies to your Maven POM, it is necessary to configure the [public UKP Maven repository]({{ site.url }}/pages/setup-maven.html).

*Only if you are using DKPro Core 1.4.0 or older:* If you want to use our pre-packaged models, it is necessary to configure the [public UKP Maven repository]({{ site.url }}/pages/setup-maven.html).

*Only if you are using DKPro Core 1.3.0 or older:* If you need to use DKPro Core versions 1.3.0 or older, it is necessary to configure the [public UKP Maven repository]({{ site.url }}/pages/setup-maven.html).

[ECLIPSE]: http://eclipse.org/
[MAVEN_CENTRAL]: http://search.maven.org/#search%7Cga%7C1%7Cde.tudarmstadt.ukp.dkpro
[M2E]: http://m2eclipse.sonatype.org/