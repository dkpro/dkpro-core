---
layout: page-fullwidth
title: "Maven artifacts"
permalink: "/maven/"
---

Get DKPro Core from [Maven Central][1]! Just use this snippet in your pom for the current version of DKPro Core:

	<dependency>
		<artifact>dkpro-core</artifact>
		<group>dkpro-core<group>
		<version>{{site.date.releases[0].version}}</version>
	</dependency>

[1]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.tudarmstadt.ukp.dkpro.core%22%20AND%20v%3A%22{{site.data.releases[0].version}}%22