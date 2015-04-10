---
#
# Use the widgets beneath and the content will be
# inserted automagically in the webpage. To make
# this work, you have to use › layout: frontpage
#
layout: frontpage
title: "dkpro-core-asl"
#header:
#	title: DKPro Core
#   image_fullwidth: "header_unsplash_12.jpg"
widget-1:
    title: "Components"
    url: 'http://phlow.github.io/feeling-responsive/blog/'
    text: 'Find out more about our bundled components.'
    image: IconComponents.png
widget-2:
    title: "Models"
    url: 'http://phlow.github.io/feeling-responsive/info/'
    text: 'A list of models that we provide accompanying the components.'
    image: IconModels.png
widget-3:
    title: "Formats"
    url: 'https://github.com/Phlow/feeling-responsive'
    text: 'With DKPro, reading and writing of various formats is just one line of code away.'
    image: IconFormatBlank.png
widget-4:
    title: "Typesystem"
    url: 'https://github.com/Phlow/feeling-responsive'
    text: 'Our typesystem tries to be comprehensive, but yet simple.'
    image: IconTypeSystem.png
widget-5:
    title: "DKPro with Java"
    url: 'https://github.com/Phlow/feeling-responsive'
    text: 'The original flavour. Use DKPro in your Java projects.'
    image: LogoJava.png
widget-6:
    title: "DKPro with Groovy"
    url: 'https://github.com/Phlow/feeling-responsive'
    text: 'Create self-contained scripts using DKPro and Groovy!'
    image: LogoGroovy.png
widget-7:
    title: "DKPro with Jython"
    url: 'https://github.com/Phlow/feeling-responsive'
    text: 'Working with Python? Use Jython to easily integrate DKPro into your projects!'
    image: LogoPython.png
header-1:
    title: "A collection of software components for natural language processing (NLP) based on the Apache [UIMA][14] framework."
    text: "Many NLP tools are already freely available in the NLP research community. DKPro Core provides UIMA components wrapping these tools (and some original tools) so they can be used interchangeably in UIMA processing pipelines. DKPro Core builds heavily on [uimaFIT][15] which allows for rapid and easy development of NLP processing pipelines, for wrapping existing tools and for creating original UIMA components."
---


How to cite
-----------

Many of the wrapped third-party components and the models used by them should be cited individually. We currently do not provide a comprehensive overview over citable publications. We encourage you to track down citable publications for these dependencies. However, you might find pointers to some relevant publications in the Model overview of the DKPro Core release you are using or in the JavaDoc of individual components.

Please cite DKPro Core itself as:

> Eckart de Castilho, R. and Gurevych, I. (2014). **A broad-coverage collection of portable NLP components for building shareable analysis pipelines**. In Proceedings of the Workshop on Open Infrastructures and Analysis Frameworks for HLT (OIAF4HLT) at COLING 2014, to be published, Dublin, Ireland.
[(pdf)][1] [(bib)][2]

License
-------

All components in DKPro Core ASL are licensed under the [Apache Software License (ASL) version 2][3] - but their dependencies may not be:

**IMPORTANT LICENSE NOTE** - It must be pointed out that while the component's source code itself is licensed under the ASL, individual components might make use of third-party libraries or products that are not licensed under the ASL, such as LGPL libraries or libraries which are free for research but may not be used in commercial scenarios. Please be aware of the third party licenses and respect them.

About DKPro Core
----------------

This project was initiated by the Ubiquitous Knowledge Processing Lab (UKP) at the Technische Universität Darmstadt, Germany under the auspices of Prof. Dr. Iryna Gurevych.

It is now jointly developed at UKP Lab, Technische Universität Darmstadt and Language Technology Lab, Universität Duisburg-Essen.



<span class="footnotes">[LogoJava.png][4] by Christian F. Burprich, Creative Commons (Attribution-Noncommercial-Share Alike 3.0 Unported); [LogoPython.png][5] by IFA; [LogoGroovy.png][6] by pictonic.co; [IconComponents.png][7], [IconModels.png][8] by [Visual Pharm][9]; [IconFormatText.png][10], [IconFormatBlank.png][11] by [Honza Dousek][12]; [IconTypeSystem.png][13] by Designmodo</span>

[1]: https://www.ukp.tu-darmstadt.de/fileadmin/user_upload/Group_UKP/OIAF4HLT2014DKProCore_cameraready.pdf
[2]: https://www.ukp.tu-darmstadt.de/publications/details/?no_cache=1&tx_bibtex_pi1%5Bpub_id%5D=TUD-CS-2014-0864&type=99&tx_bibtex_pi1%5Bbibtex%5D=yes
[3]: http://www.apache.org/licenses/LICENSE-2.0
[4]: https://www.iconfinder.com/icons/16890/java_icon#size=128
[5]: https://www.iconfinder.com/icons/282803/logo_python_icon#size=128
[6]: http://findicons.com/icon/576242/pl_groovy_02?id=576242
[7]: https://www.iconfinder.com/icons/175334/services_icon#size=128
[8]: https://www.iconfinder.com/icons/174880/database_icon#size=128
[9]: http://icons8.com/
[10]: https://www.iconfinder.com/icons/199323/extension_file_format_txt_icon#size=128
[11]: https://www.iconfinder.com/icons/199231/blank_extension_file_format_icon#size=128
[12]: https://www.iconfinder.com/iconsets/lexter-flat-colorfull-file-formats
[13]: https://www.iconfinder.com/icons/115791/tag_icon#size=128
[14]: http://uima.apache.org
[15]: http://uima.apache.org/uimafit
