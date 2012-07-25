AQMAR Arabic Tagger

This package provides a sequence tagger implementation customized for Arabic features,
including a named entity detection model especially intended for Arabic Wikipedia.
It was trained on labeled ACE and ANER data as well as an unlabeled Wikipedia corpus.
Learning is with the structured perceptron, optionally in a cost-augmented fashion.
Feature extraction is handled as a preprocessing step prior to learning/decoding.

The tagger was used for the experiments reported in

  Behrang Mohit, Nathan Schneider, Rishav Bhowmick, Kemal Oflazer, and Noah A. Smith (2012),
  Recall-Oriented Learning of Named Entities in Arabic Wikipedia. Proceedings of EACL.

and accompanies the AQMAR Arabic Wikipedia Named Entity Corpus also described in that work;
both can be obtained at

  http://www.ark.cs.cmu.edu/AQMAR/

The Java tagger was adapted from Michael Heilman's supersense tagger implementation
for English (http://www.ark.cs.cmu.edu/mheilman/questions/). It requires a minimum
Java version of 1.6. Feature extraction uses Python and depends on the MADA toolkit
(http://www1.ccls.columbia.edu/MADA/; version 3.1 was used for the Named Entity Corpus).

The AQMAR Arabic Tagger is released under the GNU General Public License (GPL) version 3 or
later; see LICENSE. (Michael Heilman's supersense tagger, which we modify, was originally
released in 2011 under GPL version 2 or later; the JSAP library, which we link to, was
originally released by Martian Software in 2011 under the Lesser GNU Public License.)


= CONTENTS =

eval/
  README and scripts for NER evaluation.
  
featExtract/
  README and scripts for feature extraction.
  
lib/
  External libraries required for the Java tagger.
  
model/
  Serialized tagging models, namely the best Arabic Wikipedia tagger reported in the EACL paper.

src/
  Java source files for the tagger.
  
arabic-tagger.jar
  Compiled Java program for training and decoding with the tagger.

build.sh
  Script for compiling the Java sources.
  
sample.properties
  An example properties file that can be used to specify options for the tagger.
  Options may alternatively be passed as command-line flags; if an option is specified
  in both places, the command-line value will take precedence.

LICENSE
README
VERSION


= USAGE =

Extracting features for text data: See featExtract/README.txt

Running the Arabic named entity tagger:

For example, the following command will use the existing named entity model in the model/ directory:

  java -Xmx8000m -XX:+UseCompressedOops -jar arabic-tagger.jar
   --load model/arabic-ner-superROP200.selfROP100.ser.gz
--test-predict featExtract/sample.bio.nerFeats --usePrevLabel true
--properties sample.properties > predictions.out

Training a tagging model:

Here is an example command for training a model on the sample feature-extracted data:

  java -Xmx8000m -XX:+UseCompressedOops -jar arabic-tagger.jar
   --save model/sample-model.ser.gz --iters 10
--labels featExtract/sample.labels --train featExtract/sample.nerFeats --debug --disk --weights
--properties sample.properties > weights.out

or boundaries only:

  java -Xmx8000m -XX:+UseCompressedOops -jar arabic-tagger.jar
   --save model/sample-model.ser.gz --iters 10
--labels featExtract/bio.labels --train featExtract/sample.bio.nerFeats --debug --disk --weights
--properties sample.properties > weights.out

For details about options, run
  java -jar arabic-tagger.jar --help