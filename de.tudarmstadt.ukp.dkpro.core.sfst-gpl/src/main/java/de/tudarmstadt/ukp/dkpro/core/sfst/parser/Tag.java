/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.sfst.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * The different tags assigned by the TRMorph tool.
 * Tags are organized into types according to {@link TagType}.
 * 
 * @author zesch
 *
 */
public enum Tag
{
    ////////////////
    // POS        //
    ////////////////
    /**
     * Adjective
     */
    adj(TagType.POS),        
    
    /**
     * Adverb
     */
    adv(TagType.POS),    
    
    /**
     * Conjunction
     */
    cnj(TagType.POS),          
    
    /**
     * Interjection
     */
    ij(TagType.POS),
    
    /**
     * Noun
     */
    n(TagType.POS),      
    
    /**
     * Proper Name
     */
    np(TagType.POS),       
    
    /**
     * Number
     */
    num(TagType.POS),
    
    /**
     * Punctuation
     */
    pnct(TagType.POS),       
    
    /**
     * Postposition
     */
    postp(TagType.POS),
    
    /**
     * Pronoun
     */
    prn(TagType.POS),
    
    /**
     * Verb
     */
    v(TagType.POS),
    
    /**
     * Auxiliary Verb
     */
    vaux(TagType.POS),   
    
    abl(TagType.MORPH),    // Ablative case        ev-den `from the house'
    acc(TagType.MORPH),    // Accusative case      ev-i
    dat(TagType.MORPH),    // Dative case          ev-e `to the house'
    gen(TagType.MORPH),    // Genitive case        ev-in `the one that belongs the house'
    ins(TagType.MORPH),    // Instrumental/comitative case
                           // This one also has a clitic equivalent `ile', and is not considered as a case for most grammar books.
                           // Probably this has three different functions.
                           //   (1) and:               `araba-yla evi sattık' (we sold the car and the house).
                           //   (2) instrumental case: `araba-yla eve gittik' (we went home with the car).
                           //   (3) comitative case:   `Ali-yle eve gittik' (we went home with Ali).
                           // Current version of TRmorph uses single symbol for all three senses.ev-le `with the house'
    loc(TagType.MORPH),    // Locative case        ev-de `in/on/at the house'
    
    p1(TagType.PERSON),     //  1st person plural           gör-dü-k `We saw'
    s1(TagType.PERSON),     //  1st person singular         gör-dü-m `I saw'
    p2(TagType.PERSON),     //  2nd person plural/formal    gör-dü-nüz `you saw'
    s2(TagType.PERSON),     //  2nd person singular         gör-dü-n `you saw'
    p3(TagType.PERSON),     //  3rd person plural           gör-dü-ler `they saw'
    s3(TagType.PERSON),     //  3rd person singular         gör-dü `he/she/it saw' (null morpheme)

    p1p(TagType.POSSESSIVE),    // 1st person pulural possessive   ev-imiz `our house'
    p1s(TagType.POSSESSIVE),    // 1st person singular possessive  ev-im `my house'
    p2p(TagType.POSSESSIVE),    // 2nd person pulural possessive   ev-iniz `your house'
    p2s(TagType.POSSESSIVE),    // 2nd person singular possessive  ev-in `your house'
    p3p(TagType.POSSESSIVE),    // 3rd person pulural possessive   ev-leri `their house'
    p3s(TagType.POSSESSIVE),    // 3rd person singular possessive  ev-i `his/her/its house'

    caus(TagType.VOICE),    // Causative   Causative suffix can be attached to the same stem multiple times.
                            //             yika-t-tır `to make someone have (something) washed'
    pass(TagType.VOICE),    // Passive     sev-il `to be loved'
    rec(TagType.VOICE),     // Reciprocal  sev `to love' -> sev-iş `to love each other' or `make love'
    ref(TagType.VOICE),     // Reflexive   yıka `to wash' -> yıka-n `to wash oneself'

    t_aor(TagType.TENSE),   // Aorist  gör-ür `    he/she/it sees (something)'
    t_cond(TagType.TENSE),  // Conditional         It can also give the optative mood.
                            //                     gör-se `if he/she/it sees'
    t_cont(TagType.TENSE),  // Continuous tense    gör-üyor `he/she/it is seeing (something)'
    t_fut(TagType.TENSE),   // Future tense        gör-ecek `he/she/it will se (something)'
    t_makta(TagType.TENSE), // ??Continuos/progressive/imperfective    This is similar to t_cont, but used less frequently. Most of the time it is used in formal situations, and has a more definite progressive sense (-yor can be used for future events as well).
                            //                     gör-mekte `he/she/it is seeing (something)'
    t_narr(TagType.TENSE),  // Narrative (or evidential) past tense    gör-müş `it is evident/said that he/she/it saw (something)'
    t_obl(TagType.TENSE),   // Obligation          gör-meli `he/she/it must see (something)'
    t_opt(TagType.TENSE),   // Optative            Indicates wish and hope, it can also have imperative meaning (archaic). Note that t_cond below may also indicate the same mood.
                            //                     bitir-e `(I) wish/hope/order that he/she/it finishes'
    t_past(TagType.TENSE),  //  Past tense         gör-dü `he/she/it saw (something)'

    cv(TagType.SUBORDINATING),     //  Converb markers     These makers form subordinating clauses with adverbial function. Most of the suffixes that form converbs can also form other forms of subordination (verbal nouns and participles). Normally, the converbial markers has a restricted context, i.e., they serve as cv only if they are followed by certain morphemes or words (particles). Current version of TRmorph does not fully restrict these.
                                   //                      gör-mek için gittim `I went in order to see'.
    part(TagType.SUBORDINATING),   // Participle markers   These are a few morphemes that make non-finite verbs of relative clauses.
                                   //                      gör-düğ-üm film `the movie that I saw'
    vn(TagType.SUBORDINATING),     // Verbal Noun Markers  These are a number of morphemes that form noun clauses from non-finita verbs. Together with Participles and Converbs, previous versions of TRmorph used to assign different analysis symbols to each morpheme. This version does not make this distinction.
                                   //                      gör-mey-e gittim `I went to see'.

    cpl_di(TagType.COPULA),     //  Past copula         gel-iyor `he/she/it is coming', gel-iyor-du `he/she/it was coming'
    cpl_mis(TagType.COPULA),    //  Evidential copula   gel-iyor `he/she/it is coming', gel-iyor-muş `(it is said that) he/she/it was coming'
    cpl_sa(TagType.COPULA),     //  Conditional copula  gel-iyor `he/she/it is coming', gel-iyor-sa `if he/she/it is coming'
    dir(TagType.COPULA),        //  DIr                 This suffix (except a few exceptions) follows person agreement, serves a number of purposes including introducing supposition, nominal predicates (especially in formal language). G&K calls this suffix 'generalized modality marker', Kornfilt treats it (more or less) as a copula.
                                //                      doktor-dur `(supposedly) s/he is a doctor' (note that this -dur is generally not used in spoken language if supposition is not implied)

    abil(TagType.COMPOUND_VERB),   //   Ability        gör-ebil `to be able to see'
    adur(TagType.COMPOUND_VERB),   //   Continuously   This is not very productive.
    agel(TagType.COMPOUND_VERB),   //   Agel           This is not very productive.
    agor(TagType.COMPOUND_VERB),   //   Agor           This is not very productive.
    akal(TagType.COMPOUND_VERB),   //   Akal           This is not very productive.
    akoy(TagType.COMPOUND_VERB),   //   Akal           This is not very productive.
    ayaz(TagType.COMPOUND_VERB),   //   Ayaz           This is not very productive.
    iver(TagType.COMPOUND_VERB),   //   Quickly        yıka-yıver `wash it quickly'

    notAvailable(TagType.NOT_AVAILABLE);
    
    public TagType type;
    
    private Tag(TagType aType)
    {
        type = aType;
    }
    
    public static Tag[] select(TagType aType) {
        List<Tag> l = new ArrayList<Tag>();
        for (Tag t : values()) {
            if (t.type == aType) {
                l.add(t);
            }
        }
        return l.toArray(new Tag[l.size()]); 
    }
}
