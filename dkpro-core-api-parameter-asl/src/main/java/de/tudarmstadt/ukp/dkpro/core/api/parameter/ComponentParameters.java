/*
 * Copyright 2011
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.api.parameter;


public final class ComponentParameters
{
	/**
	 * For analysis engines: Use this language instead of the document language to resolve the model
	 * and tag set mapping.
	 *
	 * For readers: Set this as the language of the produced documents.
	 */
	public static final String PARAM_LANGUAGE = "language";

	/**
	 * Variant of the model. Used to address a specific model if here are multiple models
	 * for one language.
	 */
	public static final String PARAM_PATTERNS = "patterns";

	/**
	 * Variant of a model the model. Used to address a specific model if here are multiple models
	 * for one language.
	 */
	public static final String PARAM_VARIANT = "modelVariant";

	/**
	 * Location from which the model is read.
	 */
	public static final String PARAM_MODEL_LOCATION = "modelLocation";

	/**
     * Location from which the segmentation model is read.
     */
    public static final String PARAM_SEGMENTATION_MODEL_LOCATION = "segmentationModelLocation";

    /**
     * Location from which the tokenization model is read.
     */
    public static final String PARAM_TOKENIZATION_MODEL_LOCATION = "tokenizationModelLocation";

	/**
	 * The character encoding used by the model.
	 */
	public static final String PARAM_MODEL_ENCODING = "modelEncoding";

	/**
	 * Location from which the input is read.
	 */
	public static final String PARAM_SOURCE_LOCATION = "sourceLocation";

	/**
	 * Character encoding of the input data.
	 */
	public static final String PARAM_SOURCE_ENCODING = "sourceEncoding";

	/**
	 * Location to which the output is written.
	 */
	public static final String PARAM_TARGET_LOCATION = "targetLocation";

	/**
	 * Character encoding of the output data.
	 */
	public static final String PARAM_TARGET_ENCODING = "targetEncoding";

    /**
     * Use this filename extension.
     */
    public static final String PARAM_FILENAME_EXTENSION = "filenameExtension";

    /**
     * Remove the original extension.
     */
    public static final String PARAM_STRIP_EXTENSION = "stripExtension";

	/**
	 * Log the tag set(s) when a model is loaded.
	 */
	public static final String PARAM_PRINT_TAGSET = "printTagSet";

	/**
	 * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
	 * spamming the heap with thousands of strings representing only a few different tags.
	 */
	public static final String PARAM_INTERN_TAGS = "internTags";

	/**
	 * When splitting an annotation into multiple parts, e.g. when splitting a token that is a
	 * compound word into multiple tokens, each representing a part of the word, this parameter
	 * controls if the original annotation is kept or removed.
	 */
	public static final String PARAM_DELETE_COVER = "deleteCover";

	/**
	 * Maximal sentence length in tokens that is still being processed.
	 */
	public static final String PARAM_MAX_SENTENCE_LENGTH = "maxSentenceLength";

    /**
     * The number of threads to use for components that implement multi-threading
     */
    public static final String PARAM_NUM_THREADS = "numThreads";
    /**
     * Use smart number of threads if PARAM_NUM_THREADS is set to this value
     */
    public static final String AUTO_NUM_THREADS = "0";

    /**
     * Compute the number of threads to use for components that can make use of multi-threading.
     * <ul>
     * <li>for positive values: use the given number of threads, with the number of available CPUs maximum.</li>
     * <li>for negative value: use the number of available CPUs minus the given value, minimum 1.</li>
     * <li>for {@link #AUTO_NUM_THREADS} (0): use the number of available CPUs minus one.</li>
     * </ul>
     *
     * @param value the user-proposed number of threads (positive, negative, or 0)
     * @return the actual number of threads to use.
     */
    public static int computeNumThreads(int value)
    {
        int cpus = Runtime.getRuntime().availableProcessors();

        if (value > 0) {
            return Math.min(cpus, value);
        }
        else if (value < 0) {
            return Math.max(1, cpus + value);
        }
        else {
            return Math.max(1, cpus - 1);
        }
    }

	// =============================================================================================
	// Annotation types
	// =============================================================================================

    private static final String PARAGRAPH = "Paragraph";

	private static final String SENTENCE = "Sentence";

    private static final String FORM = "Form";

    private static final String TOKEN = "Token";

	private static final String LEMMA = "Lemma";

	private static final String POS = "POS";

    private static final String CPOS = "CPOS";

    private static final String MORPH = "Morph";

	private static final String CHUNK = "Chunk";

	private static final String CONSTITUENT = "Constituent";

    private static final String COREFERENCE = "Coreference";

	private static final String PENN_TREE = "PennTree";

	private static final String DEPENDENCY = "Dependency";

	private static final String NAMED_ENTITY = "NamedEntity";

	// =============================================================================================
	// Verbs for parameters
	// =============================================================================================

	private static final String READ = "read";

	private static final String WRITE = "write";

	// =============================================================================================
	// Nouns for parameters
	// =============================================================================================

	private static final String TAG_SET = "TagSet";

	private static final String MAPPING_LOCATION = "MappingLocation";

	// =============================================================================================
	// Enable / disable reading or writing of particular annotation types.
	// =============================================================================================

    public static final String PARAM_READ_PARAGRAPH = READ + PARAGRAPH;

	public static final String PARAM_READ_SENTENCE = READ + SENTENCE;

	public static final String PARAM_READ_TOKEN = READ + TOKEN;

    public static final String PARAM_READ_FORM = READ + FORM;

	public static final String PARAM_READ_LEMMA = READ + LEMMA;

	public static final String PARAM_READ_POS = READ + POS;

    public static final String PARAM_READ_CPOS = READ + CPOS;

    public static final String PARAM_READ_CHUNK = READ + CHUNK;

    public static final String PARAM_READ_MORPH = READ + MORPH;

	public static final String PARAM_READ_CONSTITUENT = READ + CONSTITUENT;

    public static final String PARAM_READ_COREFERENCE = READ + COREFERENCE;

	public static final String PARAM_READ_PENN_TREE = READ + PENN_TREE;

	public static final String PARAM_READ_DEPENDENCY = READ + DEPENDENCY;

	public static final String PARAM_READ_NAMED_ENTITY = READ + NAMED_ENTITY;

    public static final String PARAM_WRITE_PARAGRAPH = WRITE + PARAGRAPH;

	public static final String PARAM_WRITE_SENTENCE = WRITE + SENTENCE;

	public static final String PARAM_WRITE_TOKEN = WRITE + TOKEN;

    public static final String PARAM_WRITE_FORM = WRITE + FORM;
    
	public static final String PARAM_WRITE_LEMMA = WRITE + LEMMA;

	public static final String PARAM_WRITE_POS = WRITE + POS;

    public static final String PARAM_WRITE_CPOS = WRITE + CPOS;

    public static final String PARAM_WRITE_CHUNK = WRITE + CHUNK;

    public static final String PARAM_WRITE_MORPH = WRITE + MORPH;

	public static final String PARAM_WRITE_CONSTITUENT = WRITE + CONSTITUENT;

    public static final String PARAM_WRITE_COREFERENCE = WRITE + COREFERENCE;

	public static final String PARAM_WRITE_PENN_TREE = WRITE + PENN_TREE;

	public static final String PARAM_WRITE_DEPENDENCY = WRITE + DEPENDENCY;

	public static final String PARAM_WRITE_NAMED_ENTITY = WRITE + NAMED_ENTITY;

	// =============================================================================================
	// Configure tag sets for different kinds of annotations.
	//
	// Not using the type constants here because they are capitalized for use with verbs
	// =============================================================================================

	/**
	 * Use this part-of-speech tag set to use to resolve the tag set mapping instead of using the
	 * tag set defined as part of the model meta data. This can be useful if a custom model is
	 * specified which does not have such meta data, or it can be used in readers.
	 */
	public static final String PARAM_POS_TAG_SET = POS + TAG_SET;

    /**
     * Use this chunk tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_CHUNK_TAG_SET = CHUNK + TAG_SET;

    public static final String PARAM_CONSTITUENT_TAG_SET = CONSTITUENT + TAG_SET;

    public static final String PARAM_MORPH_TAG_SET = MORPH + TAG_SET;

	// =============================================================================================
	// Configure mapping of tags to annotation types for different kinds of annotations.
	//
	// Not using the type constants here because they are capitalized for use with verbs
	// =============================================================================================

	/**
	 * Location of the mapping file for part-of-speech tags to UIMA types.
	 */
	public static final String PARAM_POS_MAPPING_LOCATION = POS + MAPPING_LOCATION;

    /**
     * Location of the mapping file for constituent tags to UIMA types.
     */
    public static final String PARAM_CONSTITUENT_MAPPING_LOCATION = CONSTITUENT + MAPPING_LOCATION;

	/**
	 * Location of the mapping file for chunk tags to UIMA types.
	 */
	public static final String PARAM_CHUNK_MAPPING_LOCATION = CHUNK + MAPPING_LOCATION;

	/**
	 * Location of the mapping file for named entity tags to UIMA types.
	 */
	public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = NAMED_ENTITY + MAPPING_LOCATION;

    /**
     * Location of the mapping file for morphological analysis strings to features.
     */
    public static final String PARAM_MORPH_MAPPING_LOCATION = MORPH + MAPPING_LOCATION;

	/**
     * Location of the mapping file for dependency tags to UIMA types.
     */
    public static final String PARAM_DEPENDENCY_MAPPING_LOCATION = DEPENDENCY + MAPPING_LOCATION;

	private ComponentParameters()
	{
		// No instances of this class
	}
}
