---
layout: page-fullwidth
title: "Releases"
permalink: "/releases/"
---

{% assign stable-releases = (site.data.releases | where:"status", "stable") %}
{% for release in stable-releases %}
## DKPro Core {{ release.version }}

Release date: {{ release.date }}

{% if release.tag %}
* [Release notes](https://github.com/dkpro/dkpro-core/releases/tag/{{ release.tag }}){% 
else %}
* [Release notes](https://github.com/dkpro/dkpro-core/releases/tag/{{ release.groupId }}-{{ release.version }}){% 
endif %}
{% for doclink in release.doclinks
%}* [{{ doclink.title }}]({{ site.url }}/releases/{{ release.version }}/{{ doclink.url }})
{% 
endfor %}

{% endfor %}
