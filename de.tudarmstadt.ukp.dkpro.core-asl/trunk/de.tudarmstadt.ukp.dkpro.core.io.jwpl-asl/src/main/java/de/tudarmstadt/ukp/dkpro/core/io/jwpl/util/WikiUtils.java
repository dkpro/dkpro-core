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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.util;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;


/**
 *
 * @author zesch
 * @author oferschke
 */
public class WikiUtils
{

//    /**
//     * A fast alternative to the JWPL Parser for converting MediaWikiMarkup to plain text.
//     *
//     * @param markup The string with markup.
//     * @return The cleaned string.
//     * @throws IOException
//     */
//    public static String mediaWikiMarkup2PlainText(String markup) throws IOException {
//
//        StringWriter writer = new StringWriter();
//
//        HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
//        builder.setEmitAsDocument(false);
//
//        MarkupParser parser = new MarkupParser(new MediaWikiDialect());
//        parser.setBuilder(builder);
//        parser.parse(markup);
//
//        final String html = writer.toString();
//        final StringBuilder cleaned = new StringBuilder();
//
//        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
//                @Override
//                public void handleText(char[] data, int pos) {
//                    cleaned.append(new String(data)).append(' ');
//                }
//        };
//        new ParserDelegator().parse(new StringReader(html), callback, false);
//
//        return cleaned.toString();
//    }

    /**
     * Clean a string from left-over WikiMarkup (most parsers do not work 100% correct).
     *
     * @param text A string with rests of WikiMarkup.
     * @return The cleaned string.
     */
    public static String cleanText(String text)
    {
        String plainText = text;

        plainText = plainText.replaceAll("<.+?>", " ");
        plainText = plainText.replaceAll("__.+?__", " ");
        plainText = plainText.replaceAll("\\[http.+?\\]", " ");
        plainText = plainText.replaceAll("\\{\\|.+?\\|\\}", " ");
        plainText = plainText.replaceAll("\\{\\{.+?\\}\\}", " ");
        plainText = plainText.replaceAll(" - ", " ");

        plainText = plainText.replace('"', ' ');
        plainText = plainText.replace('\'', ' ');
        plainText = plainText.replace('[', ' ');
        plainText = plainText.replace(']', ' ');
        plainText = plainText.replace('=', ' ');
        plainText = plainText.replace('*', ' ');
        plainText = plainText.replace('|', ' ');
        plainText = plainText.replace(':', ' ');
        plainText = plainText.replace('{', ' ');
        plainText = plainText.replace('}', ' ');
        plainText = plainText.replace('(', ' ');
        plainText = plainText.replace(')', ' ');
        plainText = plainText.replaceAll("\\s{2,}", " ");

        return plainText;
    }

	/**
	 * Creates a Wikipedia object from a DBConfig annotation without the need to
	 * manually create the intermediary DatabaseConfiguration.
	 *
	 * @param confAnnotation
	 *            annotation containing the db credentials
	 * @return a Wikipedia object
	 * @throws WikiApiException
	 *             if the Wikipedia object could not be created
	 */
	public static Wikipedia getWikipedia(DBConfig confAnnotation)
		throws WikiApiException
	{
		DatabaseConfiguration config = new DatabaseConfiguration();
		config.setHost(confAnnotation.getHost());
		config.setDatabase(confAnnotation.getDB());
		config.setUser(confAnnotation.getUser());
		config.setPassword(confAnnotation.getPassword());
		config.setLanguage(Language.valueOf(confAnnotation.getLanguage()));
		return new Wikipedia(config);
    }
	
	public static String jwplLanguage2dkproLanguage(Language jwplLanguage) {
	    if (jwpl2dkproLanguageMap.containsKey(jwplLanguage.name())) {
	        return jwpl2dkproLanguageMap.get(jwplLanguage.name());    

	    }
	    else {
	        System.err.println("Do not know DKPro language for JWPL language: " + jwplLanguage.name());
	        return "x-unknown"; 
	    }
	}
	
	@SuppressWarnings("serial")
    private static Map<String,String> jwpl2dkproLanguageMap = new HashMap<String,String>() {{
//        abkhazian,
//        afar,
//        afrikaans,
//        akan,
//        albanian,
//        alemannic,
//        amharic,
//        anglo_saxon,
//        arabic,
//        aragonese,
//        armenian,
//        aromanian,
//        assamese,
//        assyrian_neo_aramaic,
//        asturian,
//        avar,
//        aymara,
//        azeri,
//        bambara,
//        banyumasan,
//        bashkir,
//        basque,
//        bavarian,
//        belarusian,
//        belarusian_tarashkevitsa,
//        bengali,
//        bihari,
//        bishnupriya_manipuri,
//        bislama,
//        bosnian,
//        breton,
//        buginese,
//        bulgarian,
//        burmese,
//        buryat_russia,
//        cantonese,
//        catalan,
//        cebuano,
//        central_bicolano,
//        chamorro,
//        chechen,
//        cherokee,
//        cheyenne,
//        chichewa,
//        chinese,
//        choctaw,
//        chuvash,
//        classical_chinese,
//        cornish,
//        corsican,
//        cree,
//        crimean_tatar,
//        croatian,
//        czech,
//        danish,
//        divehi,
//        dutch,
//        dutch_low_saxon,
//        dzongkha,
//        emilian_romagnol,
        put("english", "en");
//        esperanto,
//        estonian,
//        ewe,
//        faroese,
//        fijian,
//        finnish,
//        franco_provencal_arpitan,
//        french,
//        friulian,
//        fula,
//        galician,
//        georgian,
        put("german", "de");
//        gilaki,
//        gothic,
        put("greek", "el");
//        greenlandic,
//        guarani,
//        gujarati,
//        haitian,
//        hakka,
//        hausa,
//        hawaiian,
//        hebrew,
//        herero,
//        hindi,
//        hiri_motu,
//        hungarian,
//        icelandic,
//        ido,
//        igbo,
//        ilokano,
//        indonesian,
//        interlingua,
//        interlingue,
//        inuktitut,
//        inupiak,
//        irish,
//        italian,
//        japanese,
//        javanese,
//        kabyle,
//        kalmyk,
//        kannada,
//        kanuri,
//        kapampangan,
//        kashmiri,
//        kashubian,
//        kazakh,
//        khmer,
//        kikuyu,
//        kinyarwanda,
//        kirghiz,
//        kirundi,
//        klingon,
//        komi,
//        kongo,
//        korean,
//        kuanyama,
//        kurdish,
//        ladino,
//        lak,
//        lao,
//        latin,
//        latvian,
//        ligurian,
//        limburgian,
//        lingala,
//        lithuanian,
//        lojban,
//        lombard,
//        low_saxon,
//        lower_sorbian,
//        luganda,
//        luxembourgish,
//        macedonian,
//        malagasy,
//        malay,
//        malayalam,
//        maltese,
//        manx,
//        maori,
//        marathi,
//        marshallese,
//        mazandarani,
//        min_dong,
//        min_nan,
//        moldovan,
//        mongolian,
//        muscogee,
//        nahuatl,
//        nauruan,
//        navajo,
//        ndonga,
//        neapolitan,
//        nepali,
//        newar_nepal_bhasa,
//        norfolk,
//        norman,
//        northern_sami,
//        norwegian_bokmal,
//        norwegian_nynorsk,
//        novial,
//        occitan,
//        old_church_slavonic,
//        oriya,
//        oromo,
//        ossetian,
//        pali,
//        pangasinan,
//        papiamentu,
//        pashto,
//        pennsylvania_german,
//        persian,
//        piedmontese,
//        polish,
//        portuguese,
//        punjabi,
//        quechua,
//        ripuarian,
//        romani,
//        romanian,
//        romansh,
//        russian,
//        samoan,
//        samogitian,
//        sango,
//        sanskrit,
//        sardinian,
//        saterland_frisian,
//        scots,
//        scottish_gaelic,
//        serbian,
//        serbo_croatian,
//        sesotho,
//        shona,
//        sichuan_yi,
//        sicilian,
//        simple_english,
//        sindhi,
//        sinhalese,
//        slovak,
//        slovenian,
//        somali,
//        spanish,
//        sundanese,
//        swahili,
//        swati,
//        swedish,
//        tagalog,
//        tahitian,
//        tajik,
//        tamil,
//        tarantino,
//        tatar,
//        telugu,
//        tetum,
//        thai,
//        tibetan,
//        tigrinya,
//        tok_pisin,
//        tokipona,
//        tongan,
//        tsonga,
//        tswana,
//        tumbuka,
//        turkish,
//        turkmen,
//        twi,
//        udmurt,
//        ukrainian,
//        upper_sorbian,
//        urdu,
//        uyghur,
//        uzbek,
//        venda,
//        venetian,
//        vietnamese,
//        volapuek,
//        voro,
//        walloon,
//        waray_waray,
//        welsh,
//        west_flemish,
//        west_frisian,
//        wolof,
//        wu,
//        xhosa,
//        yiddish,
//        yoruba,
//        zamboanga_chavacano,
//        zazaki,
//        zealandic,
//        zhuang,
//        zulu,
	    put("_test", "en");
    }};
}
