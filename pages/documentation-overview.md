---
layout: page-fullwidth
title: "Documentation"
permalink: "/documentation/"
---

{% assign stable = (site.data.releases | where:"status", "stable" | first) %}

## Introduction

* [Using DKPro Core with Java]({{ site.url }}/pages/java-intro/)
* [Using DKPro Core with Groovy]({{ site.url }}/pages/groovy-intro/)
* [Using DKPro Core with Jython]({{ site.url }}/pages/jython-intro/)

## Recipes: Specific Use Cases

* [Java recipes]({{ site.url }}/java/recipes/)
* [Groovy recipes]({{ site.url }}/groovy/recipes/)
* [Jython recipes]({{ site.url }}/jython/recipes/)

## Reference Documentation

### Setup

* [User setup]({{ site.url }}/pages/setup-user/)

### DKPro Core {{ stable.version }}
_latest release_

* [Release notes](https://github.com/dkpro/dkpro-core/releases/tag/de.tudarmstadt.ukp.dkpro.core-asl-{{ stable.version }})
* [User Guide and Reference]({{ site.url }}/releases/{{ stable.version }}/generated/docs/user-guide/)
* [Developer Guide]({{ site.url }}/releases/{{ stable.version }}/generated/docs/developer-guide/)
* [Component Reference]({{ site.url }}/releases/{{ stable.version }}/generated/component-reference/)
* [Type System Reference]({{ site.url }}/releases/{{ stable.version }}/generated/typesystem-reference/)
* [Model Reference]({{ site.url }}/releases/{{ stable.version }}/generated/model-reference/)
* [Format Reference]({{ site.url }}/releases/{{ stable.version }}/generated/format-reference/)
* [Tagset Mapping Reference]({{ site.url }}/releases/{{ stable.version }}/generated/format-reference/)

{% for unstable in site.data.releases reversed %}
{% if unstable.status == 'unstable' %}
### DKPro Core {{ unstable.version }}
_upcoming release - links may be temporarily broken while a build is in progress_

{% for link in unstable.doclinks %}
* [{{ link.title }}]({{ link.url }}){% endfor %}
{% endif %}
{% endfor %}

## Developer Documentation

* [Release Guide]({{ site.url }}/pages/release-guide/)
* [How to contribute](http://dkpro.github.io/contributing/)
* [Unintegratable software]({{ site.url }}/pages/unintegratable/)
