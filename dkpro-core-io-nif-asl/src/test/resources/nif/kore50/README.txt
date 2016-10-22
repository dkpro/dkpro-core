Resource: https://datahub.io/dataset/kore-50-nif-ner-corpus

License: Creative Commons Attribution-ShareAlike 4.0 International

KORE 50[1] (AIDA) is a subset of the larger AIDA corpus, which is based on the dataset of the CoNLL 
2003 NER task. The dataset aims to capture hard to disambiguate mentions of entities and it contains
a large number of first names referring to persons, whose identity needs to be deduced from the
given context. It comprises 50 sentences from different domains, such as music, celebrities, and
business and is provided in a clear TSV format.

The corpus was converted to NLP Interchange Format (NIF).

[1] J. Hoffart, S. Seufert, D. B. Nguyen, M. Theobald, and G. Weikum. KORE: Keyphrase overlap
relatedness for entity disambiguation. In Proc. of the 21st ACM international conference on
Information and knowledge management, pages 545{554. ACM, 2012

File: https://datahub.io/dataset/kore-50-nif-ner-corpus/resource/e19a9e7c-4b68-4901-b6cd-0c62776c3b4e

The kore50 file has a problem with the escaped quotes in the sentence

"Sam, the co-founder of Equity International, was given the nickname of \"the grave dancer\" 
because of his ability to buy businesses that others thought were dead."

The offsets in kore50 are calculated as if the quotes were not escaped, i.e. each quote counts
two characters instead of one. To fix this problem the kore50-cooked.ttl file has been modified
such that the quotes remain escaped in the final document text i.e. `\"` has been replaced with
`\\\"`.