---
#
# Use the widgets beneath and the content will be
# inserted automagically in the webpage. To make
# this work, you have to use › layout: frontpage
#
layout: frontpage
title: "DKPro Statistics"
#header:
#	title: DKPro Core
#   image_fullwidth: "header_unsplash_12.jpg"
header-1:
    title: A collection of open-licensed statistical tools
    text: 

---

DKPro Statistics is a collection of open-licensed statistical tools written in Java. The software library is divided into the following modules:

	* **DKPro Agreement** (dkpro-statistics-agreement) is a module for computing multiple inter-rater agreement measures using a shared interface and data model. Based on this model, the software allows for analyzing coding (i.e., assigning categories to fixed items) and unitizing setups (i.e., segmenting the data into codable units). The software has been described in our COLING 2014 demo paper. 

    Take a look at our tutorial slides for getting started with the software. 

	* **DKPro Correlation** (dkpro-statistics-correlation) is a module for computing correlation and association measures. It is currently under construction. 

	* **DKPro Significance** (dkpro-statistics-significance) is a module for assessing statistical significance. It is currently under construction. 


Availability
------------

The latest version of DKPro Statistics is available via [Maven Central][5]. If you use Maven as your build tool, then you can add DKPro Statistics as a dependency in your pom.xml file:

<dependency>
   <groupId>de.tudarmstadt.ukp.dkpro.statistics</groupId>
   <artifactId>dkpro-statistics-MODULE_NAME</artifactId>
   <version>2.0.0</version>
</dependency>

In addition to that, you can add each of the modules described above separately (e.g., artifactId dkpro-statistics-agreement).


How to cite
-----------

A more detailed description of DKPro Statistics is available in our scientific articles:

> Christian M. Meyer, Margot Mieskes, Christian Stab, and Iryna Gurevych: **DKPro Agreement: An Open-Source Java Library for Measuring Inter-Rater Agreement**. In: *Proceedings of the 25th International Conference on Computational Linguistics* (COLING), pp. 105–109, August 2014. Dublin, Ireland.
[(pdf)][1] [(bib)][2]


Please cite our COLING 2014 paper if you use the software in your scientific work. 


License
-------

DKPro Statistics is available as open-source software under the [Apache License 2.0 (ASL)][3]. The software thus comes "as is" without any warranty (see license text for more details). 


About DKPro Statistics
----------------------

Prior to being available as open source software, DKPro Statistics has been a research project at the [Ubiquitous Knowledge Processing (UKP) Lab][4] of the Technische Universität Darmstadt, Germany. The following people have mainly contributed to this project (in alphabetical order):

	* Richard Eckart de Castilho
    * Iryna Gurevych
    * Christian M. Meyer
    * Margot Mieskes
    * Christian Stab
    * Torsten Zesch 


[1]: http://www.aclweb.org/anthology/C/C14/C14-2023.pdf
[2]: http://www.aclweb.org/anthology/C/C14/C14-2023.bib
[3]: http://www.apache.org/licenses/LICENSE-2.0
[4]: http://www.ukp.tu-darmstadt.de/
[5]: http://search.maven.org/#search%7Cga%7C1%7Cde.tudarmstadt.ukp.dkpro.statistics

