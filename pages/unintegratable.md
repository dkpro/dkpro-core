---
layout: page-fullwidth
title: "Unintegratable software"
---

This page provides an overview over software that we will most likely not integrate and distribute with DKPro Core for one or more of the following reasons:

* **Service only** - the software is only available as a web-service. DKPro Core aims to be a collection of portable NLP components that can easily be deployed on any Linux, Windows or OS X machine. Software that is only available as a service is problematic for various reasons. E.g. the data must be transferred to the service for being processed. For large amounts of data or for sensitive data, this may not be possible. DKPro Core components are versioned and each of the versions is available indefinitely (or until Maven Central goes away). Hosting the artifacts requires comparatively few resources. Hosting a live service consumes comparatively many resources. Therefore it is not possible to host all versions of a service. Thus, a service may easily change due to upgrades or go away due to lack of funding.
* **Not redistributable** - the software may not be distributed from other parties than the original provider. All DKPro Core components are meant to be self-contained and should not require the user to install additional software on a machine where it is executed. If a DKPro Core component requires a library or tool, it should be able to acquire it automatically from Maven Central or from the UKP Lab OSS Maven repository. Some licenses prohibit redistribution, thus we cannot upload them either of these Maven repositories. A typical phrase in the license that prohibits redistribution is this: _The licensee has no right to give or sell the system to third parties without written permission from the licenser._
* **Not statically compilable** - the software is written in a non-Java language and cannot be compiled in such a way that it runs on a reasonably large set of machines. E.g. it may depend on too many dynamically linked libraries.
* **Not portable** - the software is not available for all major platforms (Linux, OS X, Windows).
* **Requires runtime** - the software is written in a non-Java language that requires a runtime environment which cannot easily be deployed automatically a machine by DKPro Core. This includes software written in Python, Perl, or similar languages.
* **Loss of information** - while processing, the software looses information, in particular the information on how to relate its output back to its input. A typical example is making changes to the text (_normalization_) while at the same time not anchoring the resulting annotations via offsets to the original text. Fixing such problems typically requires changing the source code of the tool. If such a change is not adopted by the upstream developers, it would mean that we would have to maintain a patch or fork against the upstream code which would cause additional overhead for us.


| *Software* | *Reason* | *Comment* |
|---+---+---|
| [Enju parser](http://www.nactem.ac.uk/enju/) | [Not redistributable](http://www.nactem.ac.uk/tsujii//downloads/files/enju/COPYING.txt) | |
| [magyarlanc](http://www.inf.u-szeged.hu/rgai/nlp?lang=en&page=magyarlanc) | [Not redistributable](http://www.inf.u-szeged.hu/rgai/magyarlanc_license) | |
| [MXPOST](https://sites.google.com/site/adwaitratnaparkhi/publications) | [Not redistributable](http://morphix-nlp.berlios.de/manual/node43.html) | |
| [TreeTagger](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/) | [Not redistributable](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/Tagger-Licence) | DKPro Core supports TreeTagger, but users have to download binaries and models themselves. DKPro Core runs TreeTagger in a separate process, thus there are no compile-time dependencies on any part of TreeTagger. |
| [LX-Tagger](http://lxcenter.di.fc.ul.pt/tools/en/conteudo/LXTagger.html) | [Not redistributable](http://lxcenter.di.fc.ul.pt/tools/en/conteudo/LX-Tagger_License.pdf) | |
| [LX-Parser](http://lxcenter.di.fc.ul.pt/tools/pt/conteudo/LXParser.html) | [Not redistributable](http://lxcenter.di.fc.ul.pt/tools/en/conteudo/LX-Parser_License.pdf) | Additionally, the LX-Parser requires an ancient version of the Stanford Parser. It is not kept up-to-date with recent Stanford releases. |
| [LX-Tokenizer](http://lxcenter.di.fc.ul.pt/tools/pt/conteudo/LXTokenizer.html) | [Not redistributable](http://lxcenter.di.fc.ul.pt/tools/en/conteudo/LX-Tokenizer_License.pdf) | Additionally, the LX-Tokenizer is only available as a binary for Linux. No Windows or OS X support. |
| [UAIC NLP Tools](http://nlptools.infoiasi.ro/Software.jsp) | Service only | |
| [AlchemyAPI](http://www.alchemyapi.com) | Service only | Commercial service |
| [OpenCalais](http://www.opencalais.com) | Service only | Commercial service |
| [SVMTool](http://www.lsi.upc.edu/~nlp/SVMTool/) | Requires runtime (Perl) | |
| [PDTB Parser](http://wing.comp.nus.edu.sg/~linzihen/parser/index.html) | Loss of information | Tokenization, text normalization (but in principle can be integrated using JRuby quite easily) |
| [BART (de)](https://github.com/sebastianruder/BART/tree/master/BART) | Unclear preprocessing | Preprocessing for German requires several third party tools in various languages (Python scripts & packages, C tools). Preprocessing sparsely documented, required C-libraries throw a memory exception. High effort required to add the required preprocessing to DKPro (as of 12/2014)  |