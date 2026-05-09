---
layout: page-fullwidth
title: "Setting up Maven and Eclipse"
---

## System Requirements

* A Java Development Kit (JDK) version 17 or higher (e.g. from [Adoptium][JDK]).
* [Eclipse][ECLIPSE] 2023-09 or higher. We recommend the _Eclipse IDE for Java Developers_ distribution.

Different versions of DKPro Core have different version requirements.

### For Windows Users

*Note:* On your machine the Java path may be different, e.g. because you are using a localized Windows version it may be `C:\Programme\...` -or- because you have a different Java version installed.

* Edit your `eclipse.ini` and add/change the following lines (the `-vmargs` line should be present already):

~~~
-vm
C:/Program Files/Java/jdk-17/bin/javaw.exe
-vmargs
~~~

  * Make sure that the linebreaks are as shown above (the formatting is actually necessary)
* Open Eclipse
  * Open the *preferences*
  * Go to *Java -> Installed JREs*
  * Click *Search* and choose your Java directory
  * *Close* the preferences and *re-open* them
  * *Select* the JDK you added (this should match the entry you added in the eclipse.ini) as your JRE

## Maven Setup

DKPro Core is available from [Maven Central][MAVEN_CENTRAL].

If you do not want to rely on our automatic download mechanism for models and instead want to add models as dependencies to your Maven POM, it is necessary to configure the [public UKP Maven repository]({{ site.url }}/pages/setup-maven/).

[JDK]: https://adoptium.net/
[ECLIPSE]: https://eclipse.org/
[MAVEN_CENTRAL]: https://central.sonatype.com/search?namespace=org.dkpro.core
[M2E]: https://eclipse.dev/m2e/