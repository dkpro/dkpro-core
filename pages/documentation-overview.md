---
layout: page-fullwidth
title: "Documentation"
permalink: "/documentation/"
---

{% assign stable = (site.data.releases | where:"status", "stable" | first) %}
{% assign unstable = (site.data.releases | where:"status", "unstable" | first) %}

## Introduction

* [Using DKPro Core with Java]({{ site.url }}/pages/java-intro.html)
* [Using DKPro Core with Groovy]({{ site.url }}/pages/groovy-intro.html)
* [Using DKPro Core with Jython]({{ site.url }}/pages/jython-intro.html)

## Recipes

* [Groovy recipes]({{ site.url }}/groovy/recipes/)

## Reference Documentation

### Setup

* [User setup]({{ site.url }}/pages/setup-user.html)

### DKPro Core {{ stable.version }}
_latest release_


* [Release notes](https://github.com/dkpro/dkpro-core/releases/tag/de.tudarmstadt.ukp.dkpro.core-asl-{{ stable.version }})
* [Components]({{ site.url }}/releases/{{ stable.version }}/components.html)
* [Formats]({{ site.url }}/releases/{{ stable.version }}/formats.html)
* [Models]({{ site.url }}/releases/{{ stable.version }}/models.html)
* [Type system]({{ site.url }}/releases/{{ stable.version }}/typesystem.html)
* [API documentation]({{ site.url }}/releases/{{ stable.version }}/apidocs/index.html)

### DKPro Core {{ unstable.version }}
_upcoming release - links may be temporarily broken while a build is in progress_

* [User Guide]({{ unstable.user_guide_url }})
* [Developer Guide]({{ unstable.developer_guide_url }})

## Developer Documentation

* [Release Guide]({{ site.url }}/pages/release-guide.html)
* [How to contribute]({{ site.url }}/pages/contributing.html)

