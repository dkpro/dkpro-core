/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util;

import java.util.HashMap;
import java.util.Map;

public class StanfordParserPosMapping
{
	/**
	 * Get the DKPro POS-Name for the given tag in the given language.
	 *
	 * @param aLanguage
	 * @param aTag
	 * @return the
	 */
	public static String getTagClass(final String aLanguage, final String aTag)
	{
		Map<String, String> tagMap;
		if (aLanguage.equals("en")) {
			tagMap = StanfordParserPosMapping.getEnglishTagMap();
		}
		else if (aLanguage.equals("de")) {
			tagMap = StanfordParserPosMapping.getGermanTagMap();
		}
		else if (aLanguage.equals("ru")) {
			tagMap = StanfordParserPosMapping.getRussianTagMap();
		}
		else if (aLanguage.equals("fr")) {
			tagMap = StanfordParserPosMapping.getFrenchTagMap();
		}
		else {
			throw new IllegalArgumentException("There is no tag mapping for " +
					"language ["+aLanguage+"]");
		}

		if (tagMap.containsKey(aTag)) {
			return tagMap.get(aTag);
		}
		else {
			return "O"; // return default "Other" tag for this tag
		}
	}

	/**
	 * Maps the Penn Treebank Word-Level constituents as produced by the
	 * StanfordParser to our POS typesytem.
	 */
	private final static Map<String, String> englishTagMap = new HashMap<String, String>() {

			private static final long serialVersionUID = 1L;

			{
	            put("CC"   , "CONJ");
	            put("CD"   , "CARD");
	            put("DT"   , "ART" );
	            put("EX"   , "O" );  //not mappable to our typesystem
	            put("FW"   , "O" );  //not mappable to our typesystem
	            put("IN"   , "PP"  );
	            put("JJ"   , "ADJ" );
	            put("JJR"  , "ADJ" );
	            put("JJS"  , "ADJ" );
	            put("MD"   , "V"   );
	            put("NN"   , "NN"  );
	            put("NNS"  , "NN"  );
	            put("NP"   , "NP"  );
	            put("NNP"   , "NP"  );
	            put("NNPS"   , "NP"  );
	            put("NPS"  , "NP"  );
	            put("PDT"  , "ART"  );
	            put("POS"     , "O"  );//not mappable to our typesystem
	            put("PRP"   , "PR"  );
	            put("PRP$"  , "PR"  );
	            put("RB"   , "ADV"  );
	            put("RBR"  , "ADV"  );
	            put("RBS"  , "ADV"  );
	            put("RP"   , "PP"   );
	            put("TO"   , "O"     );
	            put("SYM" , "PUNC" );
	            put("." , "PUNC" );
	            put("," , "PUNC" );
	            put("!" , "PUNC" );
	            put("?" , "PUNC" );
	            put("-" , "PUNC" );
	            put(":" , "PUNC" );
	            put(";" , "PUNC" );
	            put("UH"   , "O"    );//not mappable to our typesystem
	            put("VB"   , "V"    );
	            put("VBD"  , "V"    );
	            put("VBG"  , "V"    );
	            put("VBN"  , "V"    );
	            put("VBP"  , "V"    );
	            put("VBZ"  , "V"    );
	            put("VH"   , "V"    );
	            put("VHD"  , "V"    );
	            put("VHG"  , "V"    );
	            put("VHP"  , "V"    );
	            put("VHN"  , "V"    );
	            put("VHZ"  , "V"    );
	            put("VV"   , "V"    );
	            put("VVD"  , "V"    );
	            put("VVG"  , "V"    );
	            put("VVN"  , "V"    );
	            put("VVP"  , "V"    );
	            put("VVZ"  , "V"    );
	            put("WDT"  , "ART"  );
	            put("WP"   , "PR"   );
	            put("WP$"  , "PR"   );
	            put("WRB"  , "ADV"  );

	        }
	    };

	    public static Map<String, String> getEnglishTagMap(){
	    	return englishTagMap;
	    }

	    private final static Map<String, String> germanTagMap = new HashMap<String, String>() {
	        private static final long serialVersionUID = 1L;
	        {
	            put("ADJA"   , "ADJ" );
	            put("ADJD"   , "ADJ" );
	            put("ADV"    , "ADV" );
	            put("APPR"   , "PP"  );
	            put("APPRART", "PP"  );
	            put("APPO"   , "PP"  );
	            put("APZR"   , "PP"  );
	            put("ART"    , "ART" );
	            put("CARD"   , "CARD");
	            put("FM"     , "O"   );
	            put("ITJ"    , "O"   );
	            put("KOUI"   , "CONJ");
	            put("KOUS"   , "CONJ");
	            put("KON"    , "CONJ");
	            put("KOKOM"  , "CONJ");
	            put("NN"     , "NN"  );
	            put("NE"     , "NP"  );
	            put("PDS"    , "PR"  );
	            put("PDAT"   , "PR"  );
	            put("PIS"    , "PR"  );
	            put("PIAT"   , "PR"  );
	            put("PIDAT"  , "PR"  );
	            put("PPER"   , "PR"  );
	            put("PPOSS"  , "PR"  );
	            put("PPOSAT" , "PR"  );
	            put("PRELS"  , "PR"  );
	            put("PRELAT" , "PR"  );
	            put("PRF"    , "PR"  );
	            put("PWS"    , "PR"  );
	            put("PWAT"   , "PR"  );
	            put("PWAV"   , "PR"  );
	            put("PAV"    , "PR"  ); // TODO Pronominaladverb "dafuer", "dabei", "deswegen" - PR oder ADV??
	            put("PTKZU"  , "O"   );
	            put("PTKNEG" , "O"   );
	            put("PTKVZ"  , "V"   ); // verb particle treated as verb
	            put("PTKANT" , "O"   );
	            put("PTKA"   , "O"   );
	            put("TRUNC"  , "O"   );
	            put("VVFIN"  , "V"   );
	            put("VVIMP"  , "V"   );
	            put("VVINF"  , "V"   );
	            put("VVIZU"  , "V"   );
	            put("VVPP"   , "V"   );
	            put("VAFIN"  , "V"   );
	            put("VAIMP"  , "V"   );
	            put("VAINF"  , "V"   );
	            put("VAPP"   , "V"   );
	            put("VMFIN"  , "V"   );
	            put("VMINF"  , "V"   );
	            put("VMPP"   , "V"   );
	            put("XY"     , "O"   );
	            put("$,"     , "PUNC");
	            put("$."     , "PUNC");
	            put("$("     , "PUNC");
	        }
	    };

	    public static Map<String, String> getGermanTagMap() {
	        return germanTagMap;
	    }


	    private final static Map<String, String> frenchTagMap = new HashMap<String, String>() {

			private static final long serialVersionUID = 1L;

			{

	            //put("SYM", " ");
	            //put("ABR"   , "???");
	            put("NUM"   , "CARD");
	            put("ADJ"   , "ADJ" );
	            put("ADV"   , "ADV"  );
	            put("DET:ART"   , "ART" );
	            put("INT"   , "O"    );
	            put("NOM"   , "NN"  );
	            put("NAM"   , "NP"  );
	            put("KON"   , "CONJ");
	            put("PRP"   , "PP"  );
	            put("PRP:det"   , "PP"  );

	            put("DET:POS"   , "PR"  );

	            put("PRO"   , "PR"  );
	            put("PRO:DEM"  , "PR"  );
	            put("PRO:IND"  , "PR"  );
	            put("PRO:PER"   , "PR"  );
	            put("PRO:POS"  , "PR"  );
	            put("PRO:REL"  , "PR"  );
	   			put("PUN" , "PUNC" );
	            put("PUN:cit" , "PUNC" );
	            put("SENT" , "PUNC" );
	            put("VER:cond"   , "V"    );
	            put("VER:futu"  , "V"    );
	            put("VER:impe"  , "V"    );
	            put("VER:impf"  , "V"    );
	            put("VER:infi"  , "V"    );
	            put("VER:pper"  , "V"    );
	            put("VER:ppre"   , "V"    );
	            put("VER:pres"  , "V"    );
	            put("VER:simp"  , "V"    );
	            put("VER:subi"  , "V"    );
	            put("VER:subp"  , "V"    );

	        }
	    };

	    public static Map<String, String> getFrenchTagMap(){
	    	return frenchTagMap;
	    }


	    private final static Map<String, String> russianTagMap = new HashMap<String, String>() {
	        private static final long serialVersionUID = 1L;
	        {
	        	put("A","ADJ");
	        	put("A-NUM","ADJ"); //numeral adjective
	            put("ADV","ADV");
	            put("PRAEDIC","NN");
	            put("S","NN");
	            put("S-PRO","PR");
	            put("A-PRO","PR");
	            put("ADV-PRO","PR");
	            put("PRAEDIC-PRO","PR");
	            put("PR","PP");
	            put("CONJ","CONJ");
	            put("PARENTH","PUNC");
	            put("V","V");
	            put("NUM","CARD");
	            put("INTJ","O");
	            put("PART","O");

	            put("PUNCT","PUNC");
	            put("SENT","PUNC");


	        }
	    };

	    public static Map<String, String> getRussianTagMap(){
	    	return russianTagMap;
	    }

}
