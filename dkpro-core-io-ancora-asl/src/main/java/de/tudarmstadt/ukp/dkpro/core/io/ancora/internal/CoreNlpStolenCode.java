package de.tudarmstadt.ukp.dkpro.core.io.ancora.internal;

public class CoreNlpStolenCode
{
    public static final String EMPTY_LEAF_VALUE = "=NONE=";

    /**
     * Determine the part of speech of the given leaf node.
     *
     * Use some heuristics to make up for missing part-of-speech labels.
     */
    public static String getPOS(boolean simplifiedTagset, String wd, String punct, String pos,
            String namedAttribute, String posType, String gen, String num, String tagName)
    {
        // String pos = node.getAttribute(ATTR_POS);

        // String namedAttribute = node.getAttribute(ATTR_NAMED_ENTITY);
        if (pos.startsWith("np") && pos.length() == 7 && pos.charAt(pos.length() - 1) == '0') {
            // Some nouns are missing a named entity annotation in the final
            // character of their POS tags, but still have a proper named
            // entity annotation in the `ne` attribute. Fix this:
            char annotation = '0';
            if (namedAttribute.equals("location")) {
                annotation = 'l';
            }
            else if (namedAttribute.equals("person")) {
                annotation = 'p';
            }
            else if (namedAttribute.equals("organization")) {
                annotation = 'o';
            }

            pos = pos.substring(0, 6) + annotation;
        }
        else if (pos.equals("")) {
            // Make up for some missing part-of-speech tags
            String word = getWord(wd);
            if (word.equals(".")) {
                return "fp";
            }

            if (namedAttribute.equals("date")) {
                return "w";
            }
            else if (namedAttribute.equals("number")) {
                return "z0";
            }

            // String tagName = node.getTagName();
            if (tagName.equals("i")) {
                return "i";
            }
            else if (tagName.equals("r")) {
                return "rg";
            }
            else if (tagName.equals("z")) {
                return "z0";
            }

            // Handle icky issues related to "que"
            // String posType = node.getAttribute(ATTR_POSTYPE);
            if (tagName.equals("c") && posType.equals("subordinating")) {
                return "cs";
            }
            else if (tagName.equals("p") && posType.equals("relative")
                    && word.equalsIgnoreCase("que")) {
                return "pr0cn000";
            }

            if (tagName.equals("s")
                    && (word.equalsIgnoreCase("de") || word.equalsIgnoreCase("del") || word
                            .equalsIgnoreCase("en"))) {
                return "sps00";
            }
            else if (word.equals("REGRESA")) {
                return "vmip3s0";
            }

            if (simplifiedTagset) {
                // If we are using the simplified tagset, we can make some more
                // broad inferences
                if (word.equals("verme")) {
                    return "vmn0000";
                }
                else if (tagName.equals("a")) {
                    return "aq0000";
                }
                else if (posType.equals("proper")) {
                    return "np00000";
                }
                else if (posType.equals("common")) {
                    return "nc0s000";
                }
                else if (tagName.equals("d") && posType.equals("numeral")) {
                    return "dn0000";
                }
                else if (tagName.equals("d")
                        && (posType.equals("article") || word.equalsIgnoreCase("el") || word
                                .equalsIgnoreCase("la"))) {
                    return "da0000";
                }
                else if (tagName.equals("p") && posType.equals("relative")) {
                    return "pr000000";
                }
                else if (tagName.equals("p") && posType.equals("personal")) {
                    return "pp000000";
                }
                else if (tagName.equals("p") && posType.equals("indefinite")) {
                    return "pi000000";
                }
                else if (tagName.equals("s") && word.equalsIgnoreCase("como")) {
                    return "sp000";
                }
                else if (tagName.equals("n")) {
                    // String gen = node.getAttribute(ATTR_GENDER);
                    // String num = node.getAttribute(ATTR_NUMBER);

                    char genCode = gen == null ? '0' : gen.charAt(0);
                    char numCode = num == null ? '0' : num.charAt(0);
                    return 'n' + genCode + '0' + numCode + "000";
                }
            }

            // if (node.hasAttribute(ATTR_PUNCT)) {
            if (punct != null) {
                if (word.equals("\"")) {
                    return "fe";
                }
                else if (word.equals("'")) {
                    return "fz";
                }
                else if (word.equals("-")) {
                    return "fg";
                }
                else if (word.equals("(")) {
                    return "fpa";
                }
                else if (word.equals(")")) {
                    return "fpt";
                }

                return "fz";
            }
        }

        return pos;
    }

    private static String getWord(String word)
    {
        if (word.equals("")) {
            return EMPTY_LEAF_VALUE;
        }

        return word.trim();
    }

    /**
     * Return a "simplified" version of an original AnCora part-of-speech tag, with much
     * morphological annotation information removed.
     */
    public static String simplifyPOSTag(String pos, boolean retainNER)
    {
        if (pos.length() == 0) {
            return pos;
        }

        switch (pos.charAt(0)) {
        case 'd':
            // determinant (d)
            // retain category, type
            // drop person, gender, number, possessor
            return pos.substring(0, 2) + "0000";
        case 's':
            // preposition (s)
            // retain category, type
            // drop form, gender, number
            return pos.substring(0, 2) + "000";
        case 'p':
            // pronoun (p)
            // retain category, type
            // drop person, gender, number, case, possessor, politeness
            return pos.substring(0, 2) + "000000";
        case 'a':
            // adjective
            // retain category, type, grade
            // drop gender, number, function
            return pos.substring(0, 3) + "000";
        case 'n':
            // noun
            // retain category, type, number, NER label
            // drop type, gender, classification

            char ner = retainNER && pos.length() == 7 ? pos.charAt(6) : '0';
            return pos.substring(0, 2) + '0' + pos.charAt(3) + "00" + ner;
        case 'v':
            // verb
            // retain category, type, mood, tense
            // drop person, number, gender
            return pos.substring(0, 4) + "000";
        default:
            // adverb
            // retain all
            // punctuation
            // retain all
            // numerals
            // retain all
            // date and time
            // retain all
            // conjunction
            // retain all
            return pos;
        }
    }
}
