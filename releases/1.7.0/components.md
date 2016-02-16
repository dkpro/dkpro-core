---
layout: page-fullwidth
title: "List of components contained in DKPro Core 1.7.0 (ASL/GPL)"

docs-gpl: "/apidocs/index.html?de/tudarmstadt/ukp/dkpro/core/"
docs-asl: "/apidocs/index.html?de/tudarmstadt/ukp/dkpro/core/"
---

Further information on models for particular components can be found in the [list of models](models.html)

## Coreference
  * [StanfordCoreferenceResolver]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}stanfordnlp/StanfordCoreferenceResolver.html)

## Chunker
  * [OpenNlpChunker]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}opennlp/OpenNlpChunker.html)
  * [TreeTaggerChunker]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}treetagger/TreeTaggerChunker.html)

## Decompounding
  * [CompoundAnnotator]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}decompounding/uima/annotator/CompoundAnnotator.html)

## Dictionary Annotation
  * [DictionaryAnnotator]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}dictionaryannotator/DictionaryAnnotator.html)
  * [SemanticFieldAnnotator]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}dictionaryannotator/semantictagging/SemanticFieldAnnotator.html)

## Language Identification
  * [Token ngram-based (Character n-grams)]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}langdetect/LangDetectLanguageIdentifier.html)
  * [LangDect (Token ngram-based)]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}langdect/LanguageDetector.html)
  * [LanguageIdentifier (TextCat)]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}textcat/LanguageIdentifier.html)

## Lemmatization
  * [ClearNlpLemmatizer]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}clearnlp/ClearNlpLemmatizer.html)
  * [GateLemmatizer]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}gate/GateLemmatizer.html)
  * [LanguageToolLemmatizer]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}languagetool/LanguageToolLemmatizer.html)
  * [MateLemmatizer]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}matetools/MateLemmatizer.html)
  * [StanfordLemmatizer]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}stanfordnlp/StanfordLemmatizer.html)
  * [MorphaLemmatizer]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}morpha/MorphaStemmer.html)
  * [TreeTaggerPosLemmaTT4J]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}treetagger/TreeTaggerPosLemmaTT4J.html)

## Morphological Annotation
  * [MateMorphTagger]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}matetools/MateMorphTagger.html)
  * [SfstAnnotator]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}sfst/SfstAnnotator.html)

## Named Entity Recognition
  * [OpenNlpNameFinder]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}opennlp/OpenNlpNameFinder.html)
  * [StanfordNamedEntityRecognizer]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}stanfordnlp/StanfordNamedEntityRecognizer.html)

## Parsing
  * [BerkeleyParser (constituents)]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}berkeleyparser/BerkeleyParser.html)
  * [ClearNlpDependencyParser (dependencies)]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}clearnlp/ClearNlpDependencyParser.html)
  * [OpenNlpParser (constituents)]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}opennlp/OpenNlpParser.html)
  * [MaltParser (dependencies)]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}maltparser/MaltParser.html)
  * [MateParser (dependencies)]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}matetools/MateParser.html) 
  * [MstParser (dependencies)]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}mstparser/MstParser.html)
  * [StanfordParser (constituents, dependencies for some languages)]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}stanfordnlp/StanfordParser.html)

## POS Tagging
  * [ArktweetTagger]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}arktools/ArktweetTagger.html)
  * [ClearNlpPosTagger]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}clearnlp/ClearNlpPosTagger.html)
  * [HunPostTagger]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}hunpos/HunPosTagger.html)
  * [MatePosTagger]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}matetools/MatePosTagger.html)
  * [MeCabTagger]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}mecab/MeCabTagger.html)
  * [OpenNlpPosTagger]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}opennlp/OpenNlpPosTagger.html)
  * [StanfordPosTagger]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}stanfordnlp/StanfordPosTagger.html)
  * [TreeTaggerPosTagger]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}treetagger/TreeTaggerPosTagger.html)

## Segmentation / Tokenization
  * [BreakIteratorSegmenter]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}tokit/BreakIteratorSegmenter.html)
  * [ClearNlpSegmenter]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}clearnlp/ClearNlpSegmenter.html)
  * [LanguageToolSegmenter]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}languagetool/LanguageToolSegmenter.html)
  * [OpenNlpSegmenter]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}opennlp/OpenNlpSegmenter.html)
  * [StanfordSegmenter]({{ site.url }}/releases/1.7.0{{ page.docs-gpl }}stanfordnlp/StanfordSegmenter.html)

## Semantic Role Labeling
  * [ClearNlpSemanticRoleLabeler]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}clearnlp/ClearNlpSemanticRoleLabeler.html)

## Spell/grammar Checking
  * [LanguageToolChecker]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}languagetool/LanguageToolChecker.html)
  * [NorvigSpellingCorrector]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}norvig/NorvigSpellingCorrector.html)
  * [SpellChecker (Jazzy)]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}jazzy/SpellChecker.html)

## Stemming
  * [SnowballStemmer]({{ site.url }}/releases/1.7.0{{ page.docs-asl }}snowball/SnowballStemmer.html)
