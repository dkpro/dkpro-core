/*******************************************************************************
 * Copyright 2013

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
 *******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.decompounding.splitter;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.uni_leipzig.asv.utils.Pretree;

public class AsvToolboxSplitterAlgorithm
implements SplitterAlgorithm
{
	private final Zerleger2 splitter;

	private Log logger;

	public AsvToolboxSplitterAlgorithm()
			throws ResourceInitializationException
			{
		logger = LogFactory.getLog(this.getClass());

		splitter = new Zerleger2();
		try {
			File kompVVicTree = ResourceUtils.getUrlAsFile(getClass().getResource(
					"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/splitter/de/"
							+ "asv/kompVVic.tree"), false);

			File kompVHic = ResourceUtils.getUrlAsFile(getClass().getResource(
					"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/splitter/de/"
							+ "asv/kompVHic.tree"), false);

			File grfExt = ResourceUtils.getUrlAsFile(getClass().getResource(
					"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/splitter/de/"
							+ "asv/grfExt.tree"), false);

			splitter.init(kompVVicTree.getAbsolutePath(), kompVHic.getAbsolutePath(),
					grfExt.getAbsolutePath());
		}
		catch (IOException ioException) {
			throw new ResourceInitializationException(ioException);
		}

			}

	@Override
	public DecompoundingTree split(String aWord) throws ResourceInitializationException
	{
		// splitter.kZerlegung("katalogleichen");
		// splitter.kZerlegung("nischenthemen");
		// splitter.kZerlegung("brennbaukästen");
		// splitter.kZerlegung("autokorrelationszeit");
		// splitter.kZerlegung("providerdaten");
		// splitter.kZerlegung("zahnärzten");

		logger.debug("SPLITTING WORD: "+aWord);
		Vector<String> split = splitter.kZerlegung(aWord);
		String joined = StringUtils.join(split, "").replace("(", "").replace(")", "");
		if (!joined.equals(aWord)) {
			logger.error("Failed while splitting " + aWord + " into " + split);
		}

		if (StringUtils.join(split, "").contains("()")) {
			logger.error(aWord + " -> " + split);
			throw new ResourceInitializationException("Failed while splitting " + aWord + " into " + split, null);
		}

		StringBuilder splitStr = new StringBuilder();
		for (int i = 0; i < split.size(); i++) {
			if (splitStr.length() > 0 && !split.get(i).startsWith("(")) {
				splitStr.append("+");
			}
			splitStr.append(split.get(i));
		}

		return new DecompoundingTree(splitStr.toString());
	}

	@Override
	public void setDictionary(Dictionary aDict)
	{
		// Nothing to do
	}

	@Override
	public void setLinkingMorphemes(LinkingMorphemes aMorphemes)
	{
		// Nothing to do
	}

	@Override
	public void setMaximalTreeDepth(int aDepth)
	{
		// Nothing to do
	}

	public class Zerleger2
	{
		Pretree kompvvTree = new Pretree();
		Pretree kompvhTree = new Pretree();
		Pretree grfTree = new Pretree();
		String anweisungGrf = new String();
		String anweisungKomp = new String();
		//        boolean d = true; // debugguing

		String reverse(String torev)
		{
			String ret = new String();
			for (int i = torev.length(); i > 0; i--) {
				ret += torev.substring(i - 1, i);
			}
			return ret;
		}

		public Vector<String> kZerlegung(String aAktwort)
		{
			//            if (d) {
			//                logger.debug("grf: " + aAktwort + "->");
			//            }
			String aktwort = grundFormReduktion(aAktwort);
			//            if (d) {
			//                logger.debug(aktwort);
			//            }
			Vector<String> retvec = new Vector<String>();
			String classvv = new String();
			String classvh = new String();
			String zahlStrvv = "", zahlStrvh = "", suffixvv = "", suffixvh = "", vvteil1 = "", vhteil1 = "", vvteil2 = "", vhteil2 = "";
			Vector<String> zervh = new Vector<String>();
			Vector<String> zervv = new Vector<String>();
			int zahlvv = 0, zahlvh = 0;
			boolean vhOk, vvOk;
			//            if (d) {
			//                logger.debug("Zerlege " + aktwort);
			//            }
			classvv = kompvvTree.classify(aktwort + "<");
			classvh = kompvhTree.classify(reverse(aktwort) + "<");
			//            if (d) {
			//                logger.debug("VV liefert " + classvv);
			//            }
			//            if (d) {
			//                logger.debug("VH liefert " + classvh);
			//            }

			zervv = new Vector<String>();
			zervh = new Vector<String>();
			zervv.addElement(aktwort);
			zervh.addElement(aktwort);
			vvOk = true;
			vhOk = true;
			if (classvv.equals("undecided")) {
				vvOk = false;
			}
			if (classvh.equals("undecided")) {
				vhOk = false;
			}

			if (vvOk) {
				for (int i = 0; i < classvv.length(); i++) {
					char c = classvv.charAt(i);
					//                    if (d) {
					//                        logger.debug("Parse: " + c + " " + (int) c);
					//                    }
					if ((c < 58) && (c > 47)) {
						zahlStrvv += c;
					}
					else {
						suffixvv += c;
					}
				} // rof i
			}
			if (vhOk) {
				for (int i = 0; i < classvh.length(); i++) {
					char c = classvh.charAt(i);
					//                    if (d) {
					//                        logger.info("Parse: " + c + " " + (int) c);
					//                    }
					if ((c < 58) && (c > 47)) {
						zahlStrvh += c;
					}
					else {
						suffixvh += c;
					}
				} // rof i
			}

			if (vvOk) {
				zahlvv = new Integer(zahlStrvv).intValue();
			}
			if (vhOk) {
				zahlvh = new Integer(zahlStrvh).intValue();
			}

			if (vvOk) {
				if (zahlvv >= aktwort.length()) {
					vvOk = false;
				}
			}
			;
			if (vhOk) {
				if (zahlvh >= aktwort.length()) {
					vhOk = false;
				}
			}
			;

			if (vvOk) {
				for (int i = 0; i < suffixvv.length(); i++) {
					//                    if (d) {
						//                        logger.debug("VV matche " + suffixvv.charAt(i) + " und "
								//                                + aktwort.charAt(zahlvv + i));
					//                    }
					if (aktwort.length() > (zahlvv + i)) {
						if (suffixvv.charAt(i) != aktwort.charAt(zahlvv + i)) {
							vvOk = false;
						}
					}
					else {
						vvOk = false;
					}
				}
			}
			if (vhOk) {
				for (int i = 0; i < suffixvh.length(); i++) {
					if (suffixvh.charAt(i) != aktwort.charAt(zahlvh + 1 + i)) {
						vvOk = false;
					}
				}
			}

			// nun abschneiden durchf�hren
			if (vvOk) {
				zervv.removeElement(aktwort);
				vvteil1 = aktwort.substring(0, zahlvv);
				vvteil2 = aktwort.substring(zahlvv + suffixvv.length(), aktwort.length());
				zervv.addElement(vvteil1);
				zervv.addElement(vvteil2);
				//                if (d) {
					//                    logger.debug("VV zerlegt in " + vvteil1 + " " + vvteil2);
				//                }
				if (vvteil2.length() <= 3) {
					vvOk = false;
				}

			}
			if (vhOk) {
				zervh.removeElement(aktwort);
				vhteil1 = aktwort.substring(0, aktwort.length() - zahlvh);
				vhteil2 = aktwort.substring(aktwort.length() - (zahlvh + suffixvh.length()),
						aktwort.length());
				zervh.addElement(vhteil1);
				zervh.addElement(vhteil2);
				//                if (d) {
					//                    logger.debug("VH zerlegt in " + vhteil1 + " " + vhteil2);
				//                }

				if (vhteil1.length() <= 3) {
					vhOk = false;
				}

			}
			if (vvOk && vhOk) { // beide ok
				if (vvteil1.equals(vhteil1)) {
					retvec.addElement(vvteil1);
					if (vhteil2.length() < vvteil2.length()) {
						retvec.addElement(vhteil2);
					}
					else if (vhteil2.length() > vvteil2.length()) {
						retvec.addElement(vvteil2);
					}
				}
				else if ((vhteil1.length() - vvteil1.length()) < 3) {
					retvec.addElement(vvteil1);
					if (vhteil2.length() < vvteil2.length()) {
						retvec.addElement(vhteil2);
					}
					else if (vhteil2.length() > vvteil2.length()) {
						retvec.addElement(vvteil2);
					}
				}
				// sonst 3 teile
				else {
					retvec.addElement(vvteil1);
					retvec.addElement(aktwort.substring(vvteil1.length() + suffixvv.length(),
							aktwort.length() - zahlvh));
					retvec.addElement(vhteil2);
				}
				if (vvteil2.equals(vhteil2)) {
					retvec.addElement(vvteil2);
				}

			}
			else if (vvOk && !vhOk) { // nur vvOK
				retvec.addElement(vvteil1);
				retvec.addElement(vvteil2);
			}
			else if (vhOk && !vvOk) { // nur vhOK
				retvec.addElement(vhteil1);
				retvec.addElement(vhteil2);
			}
			else { // keine Zerlegung gefunden -> lassen
				retvec.addElement(aktwort);
			}

			//            if (d) {
			//                logger.debug("Pre-Ergebnis: [" + aAktwort + "] -> " + retvec);
			//            }

			if (retvec.size() == 1) {
				// If there was no split, return verbatim
				retvec.clear();
				retvec.add(aAktwort);
			}
			else if (retvec.size() == 2) {
				String w1 = retvec.get(0);
				String w2 = retvec.get(1);
				retvec.clear();

				if (!aAktwort.startsWith(w1)) {
					// throw new
					// IllegalStateException("Bad assumption: first split not changed by
					// grundFormReduktion");
					logger.error("Unable to map split " + asList(w1, w2)
							+ " back to original " + aAktwort + "... no splitting");
					retvec.add(aAktwort);
				}
				else {
					retvec.add(w1);
					int restBegin = w1.length();
					handleLastSplit(aAktwort, restBegin, w2, retvec);
				}
			}
			else if (retvec.size() == 3) {
				String w1 = retvec.get(0);
				String w2 = retvec.get(1);
				String w3 = retvec.get(2);
				retvec.clear();

				if (!aAktwort.startsWith(w1)) {
					// throw new
					// IllegalStateException("Bad assumption: first split not changed by
					// grundFormReduktion");
					logger.error("Unable to map split " + asList(w1, w2, w3)
							+ " back to original " + aAktwort + "... no splitting");
					retvec.add(aAktwort);
				}
				else {
					retvec.add(w1);
					int morphi = aAktwort.indexOf(w2, w1.length());
					if (morphi == -1) {
						// throw new
						// IllegalStateException("Bad assumption: second split not changed by
						// grundFormReduktion");
						logger.error("Unable to map split " + asList(w1, w2, w3)
								+ " back to original " + aAktwort + "... no splitting");
						retvec.clear();
						retvec.add(aAktwort);
					}
					else {
						if (morphi > w1.length()) {
							retvec.add("(" + aAktwort.substring(w1.length(), morphi) + ")");
						}
						retvec.add(w2);
						int restBegin = w2.length() + morphi;
						handleLastSplit(aAktwort, restBegin, w3, retvec);
					}
				}
			}

			//            if (d) {
			//                logger.debug("Ergebnis: " + retvec);
			//            }

			Vector<String> retvec2 = new Vector<String>();

			if (retvec.size() > 1) {
				for (String aktelement : retvec) {
					if (aktelement.startsWith("(")) {
						// This is a linking morpheme
						retvec2.addElement(aktelement);
						continue;
					}
					Vector<String> zwischen = kZerlegung(aktelement);
					for (String string : zwischen) {
						retvec2.addElement(string);
					}
				}
			} // rof if enum
			else {
				retvec2 = retvec;
			}

			//            if (d) {
			//                logger.debug("Ergebnis2: " + retvec2.toString());
			//            }

			return retvec2;
		} // end kZerlegung

		public void handleLastSplit(String aAktwort, int aSplitBegin, String aSplit,
				Vector<String> retvec)
		{
			boolean found = false;
			for (int i = 0; i < aSplit.length() - 1; i++) {
				int restOffset = aSplitBegin + i;
				String rest = aAktwort.substring(restOffset);
				String restGrund = grundFormReduktion(rest);
				boolean isEqual = aSplit.equals(restGrund) || aSplit.equals(rest);
				boolean isStartsWith = aSplit.startsWith(restGrund) || aSplit.startsWith(rest);
				boolean isInvStartsWith = rest.startsWith(aSplit) || restGrund.startsWith(aSplit);

				if (isEqual || isStartsWith || isInvStartsWith) {
					if (i > 0) {
						retvec.add("(" + aAktwort.substring(aSplitBegin, restOffset) + ")");
					}
				}

				if (isEqual) {
					retvec.add(aAktwort.substring(restOffset));
					found = true;
				}
				else if (aSplit.startsWith(rest)) {
					retvec.add(rest);
					found = true;
				}
				else if (aSplit.startsWith(restGrund)) {
					retvec.add(restGrund);
					retvec.add("(" + rest.substring(restGrund.length()) + ")");
					found = true;
				}
				else if (isInvStartsWith) {
					retvec.add(aSplit);
					retvec.add("(" + rest.substring(aSplit.length()) + ")");
					// retvec.add(restGrund);
					found = true;
				}

				if (found) {
					break;
				}
			}

			if (!found) {
				retvec.add(aAktwort.substring(aSplitBegin));
				// throw new
				// IllegalStateException("Bad assumption: last split does not start a grundform of
				// a suffix of aktwort");
			}
		}

		public String grundFormReduktion(String wort)
		{
			String retwort = wort;
			anweisungGrf = grfTree.classify(reverse(wort));
			// logger.info("Anweisung f�r "+wort+": "+anweisungGrf);
			if (!anweisungGrf.equals("undecided")) {
				StringTokenizer kommatok = new StringTokenizer(anweisungGrf, ",");
				anweisungGrf = kommatok.nextToken(); // nehme bei
				// mehreren
				// nurerstes
				// parsing anweisung
				String zahlStr = new String();
				String suffix = new String();

				for (int i = 0; i < anweisungGrf.length(); i++) {
					char c = anweisungGrf.charAt(i);
					// logger.info("Parse: "+c+" "+(int)c);
					if ((c < 58) && (c > 47)) {
						zahlStr += c;
					}
					else {
						suffix += c;
					}
				} // rof i

				// logger.info(anweisungGrf+"->"+zahlStr+"-"+suffix+"'");

				int cutpos = new Integer(zahlStr).intValue();
				if (cutpos > retwort.length()) {
					cutpos = retwort.length();
				}
				retwort = retwort.substring(0, retwort.length() - cutpos) + suffix;
			}

			String[] alternatives = retwort.split(";");
			if (alternatives.length > 0) {
				retwort = retwort.split(";")[0];
			}
			else {
				retwort = wort;
			}

			return retwort;
		}

		public void init(String kompvv, String kompvh, String gfred)
		{
			// B�ume initialisierung
			// logger.info("Loading from "+grfFile);
			logger.debug("Loading " + kompvv + " ...");
			kompvvTree.load(kompvv);
			//                logger.debug("loaded");
			kompvvTree.setIgnoreCase(true);
			kompvvTree.setThresh(0.51);

			// Kompositazerlegung-Beum initialisieren
			logger.debug("Loading " + kompvh + " ...");

			kompvhTree.load(kompvh);

			//                logger.debug("loaded");
			kompvhTree.setIgnoreCase(true); // Trainingsmenge in
			// lowcase :(
			kompvhTree.setThresh(0.51); // weiss nicht?
			logger.debug("Loading " + gfred + " ...");

			grfTree.load(gfred);
			//                logger.debug("loaded");
			grfTree.setIgnoreCase(true); // Trainingsmenge in lowcase
			// :(
			grfTree.setThresh(0.46); // weiss nicht?

		}

		// inititialisieren mit pretrees
		public void init2(Pretree kompvv, Pretree kompvh, Pretree gfred)
		{
			// B�ume initialisierung

			kompvvTree = kompvv;
			kompvvTree.setIgnoreCase(true);
			kompvvTree.setThresh(0.51);

			// Kompositazerlegung-Beum initialisieren
			kompvhTree = kompvh;
			kompvhTree.setIgnoreCase(true); // Trainingsmenge in lowcase
			// :(
			kompvhTree.setThresh(0.51); // weiss nicht?

			grfTree = gfred;
			grfTree.setIgnoreCase(true); // Trainingsmenge in lowcase :(
			grfTree.setThresh(0.46); // weiss nicht?
		}

	} // end class Zerleger
}
