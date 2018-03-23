---
layout: page-fullwidth
title: "Downloads"
permalink: "/maven/"
---

{% assign stable = (site.data.releases | where:"status", "stable" | first) %}

## Maven

DKPro Core is available via the Maven infrastructure.

As an example, we take the OpenNlpPosTagger component. To make it available in a
pipeline, we add the following dependency to our POM file:

{% highlight xml %}
<properties>
  <dkpro.core.version>{{stable.version}}</dkpro.core.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>de.tudarmstadt.ukp.dkpro.core</groupId>
      <artifactId>de.tudarmstadt.ukp.dkpro.core-asl</artifactId>
      <version>${dkpro.core.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>de.tudarmstadt.ukp.dkpro.core</groupId>
    <artifactId>de.tudarmstadt.ukp.dkpro.core.opennlp-asl</artifactId>
  </dependency>
</dependencies>
{% endhighlight xml %}

A full list of artifacts is available from [Maven Central][1]! 

## Sources

Get the sources from [GitHub](https://github.com/dkpro/dkpro-core/releases/tag/de.tudarmstadt.ukp.dkpro.core-asl-{{ stable.version }}).

[1]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.tudarmstadt.ukp.dkpro.core%22%20AND%20v%3A%22{{stable.version}}%22
