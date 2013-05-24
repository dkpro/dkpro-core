/*******************************************************************************
 * Copyright 2010
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

package de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.igerman98;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Affix data model
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class Affix
{

	/**
	 * Key for prefixed in affix files
	 */
	private static final String PREFIX_KEY = "PFX";

	/**
	 * Key for suffixes in the affix files
	 */
	private static final String SUFFIX_KEY = "SFX";

	private static final String PREFIX_CONDITION_REGEX_PATTERN = "%s.*";
	private static final String SUFFIX_CONDITION_REGEX_PATTERN = ".*%s";

	private AffixType type;
	private char flag;
	private String stripping;
	private String affix;
	private String condition;
	private Pattern conditionPattern;
	private boolean crossProduct;

	public Affix(AffixType aType)
	{
		type = aType;
	}

	public Affix(String aKey)
	{
		if (aKey.equals(PREFIX_KEY)) {
			type = AffixType.PREFIX;
		}
		else if (aKey.equals(SUFFIX_KEY)) {
			type = AffixType.SUFFIX;
		}
		else {
			throw new RuntimeException(aKey + " do not exist");
		}
	}

	public boolean isCrossProduct()
	{
		return crossProduct;
	}

	public void setCrossProduct(boolean aCrossProduct)
	{
		crossProduct = aCrossProduct;
	}

	public AffixType getType()
	{
		return type;
	}

	public void setType(AffixType aType)
	{
		type = aType;
	}

	public char getFlag()
	{
		return flag;
	}

	public void setFlag(char aFlag)
	{
		flag = aFlag;
	}

	public String getStripping()
	{
		return stripping;
	}

	public void setStripping(String aStripping)
	{
		stripping = aStripping;
	}

	public String getAffix()
	{
		return affix;
	}

	public void setAffix(String aAffix)
	{
		affix = aAffix;
	}

	public String getCondition()
	{
		return condition;
	}

	public void setCondition(String aCondition)
	{
		condition = aCondition;

		String regExp;

		switch (type) {
		case PREFIX:
			regExp = String.format(PREFIX_CONDITION_REGEX_PATTERN, aCondition);
			break;
		case SUFFIX:
			regExp = String.format(SUFFIX_CONDITION_REGEX_PATTERN, aCondition);
			break;
		default:
			throw new RuntimeException(type.toString()
					+ " is not supported");
		}

		conditionPattern = Pattern.compile(regExp);
	}

	/**
	 * Adopt this affix on a given word
	 * 
	 * @param aWord
	 * @return The word with a change prefix or affix
	 */
	public String handleWord(String aWord)
	{
		Matcher m = conditionPattern.matcher(aWord);

		if (m != null && m.matches()) {
			if (type.equals(AffixType.PREFIX)) {
				return handlePrefix(aWord);
			}
			else if (type.equals(AffixType.SUFFIX)) {
				return handleSuffix(aWord);
			}
		}

		return null;
	}

	private String handlePrefix(String aWord)
	{
		if (stripping.equals("0") || aWord.startsWith(stripping)) {
			int start = 0;
			if (!stripping.equals("0") && aWord.startsWith(stripping)) {
				start = aWord.length() - stripping.length();
			}

			return affix + aWord.substring(start);
		}

		return null;
	}

	private String handleSuffix(String aWord)
	{
		if (stripping.equals("0") || aWord.endsWith(stripping)) {
			int end = aWord.length();
			if (!stripping.equals("0") && aWord.endsWith(stripping)) {
				end = aWord.length() - stripping.length();
			}

			return aWord.substring(0, end) + affix;
		}

		return null;
	}
}
