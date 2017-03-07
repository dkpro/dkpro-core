---
layout: page-fullwidth
title: "Setting up Maven repository"
---

## Configuring a project for access to models

Add the following repository section to your project in order to get access to the models:

{% highlight xml %}
<repositories>
  <repository>
    <id>ukp-oss-model-releases</id>
    <url>http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local</url>
   </repository>
</repositories>
{% endhighlight xml %}

If you have a multi-module project, it is sufficient to add this section to the POM of the top-level project.

## Configuring Maven for full access to the UKP Maven Repository

We have set up a Maven repository which provides third-party libraries that are not available via Maven Central as well as SNAPSHOT versions our open source projects. To use this repository, we suggest you use the [settings.xml](http://maven.apache.org/settings.html) in the hidden subdirectory `.m2` of your home directory and augment it with the following sections:

{% highlight xml %}
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <profiles>
    <profile>
      <id>ukp-oss-releases</id>
      <repositories>
        <repository>
          <id>ukp-oss-releases</id>
          <url>http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-releases/</url>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
            <checksumPolicy>warn</checksumPolicy>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
      	<pluginRepository>
          <id>ukp-oss-releases</id>
          <url>http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-releases/</url>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
            <checksumPolicy>warn</checksumPolicy>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
    <profile>
      <id>ukp-oss-snapshots</id>
      <repositories>
        <repository>
          <id>ukp-oss-snapshots</id>
          <url>http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-snapshots/</url>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>ukp-oss-releases</activeProfile>
    <!-- Uncomment the following entry if you need SNAPSHOT versions. -->
    <!--activeProfile>ukp-oss-snapshots</activeProfile-->
  </activeProfiles>
</settings>
{% endhighlight xml %}
