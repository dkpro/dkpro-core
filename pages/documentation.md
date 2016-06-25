---
layout: page-fullwidth
title: "Documentation"
permalink: "/documentation/"
---

## User Documentation

### Introduction

* [Using DKPro Core with Java]({{ site.url }}/pages/java-intro/)
* [Using DKPro Core with Groovy]({{ site.url }}/pages/groovy-intro/)
* [Using DKPro Core with Jython]({{ site.url }}/pages/jython-intro/)
* [User setup]({{ site.url }}/pages/setup-user/)

### Recipes: Specific Use Cases

* [Java recipes]({{ site.url }}/java/recipes/)
* [Groovy recipes]({{ site.url }}/groovy/recipes/)
* [Jython recipes]({{ site.url }}/jython/recipes/)

{% assign stable = (site.data.releases | where:"status", "stable" | first) %}
### DKPro Core {{ stable.version }}
_latest release_

{% for link in stable.doclinks %}
* [{{ link.title }}]({{ link.url }}){% endfor %}

## Developer Documentation

* [Release Guide]({{ site.url }}/pages/release-guide/)
* [How to contribute](http://dkpro.github.io/contributing/)
* [Unintegratable software]({{ site.url }}/pages/unintegratable/)

{% for unstable in site.data.releases reversed %}
{% if unstable.status == 'unstable' %}
### DKPro Core {{ unstable.version }}
_upcoming release - links may be temporarily broken while a build is in progress_

{% for link in unstable.doclinks %}
* [{{ link.title }}]({{ link.url }}){% endfor %}
{% endif %}
{% endfor %}
