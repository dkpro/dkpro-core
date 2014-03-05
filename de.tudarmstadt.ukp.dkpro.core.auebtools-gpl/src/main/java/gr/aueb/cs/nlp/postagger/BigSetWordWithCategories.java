/**
 * Copyright 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * POStagger 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * Koleli Evangelia
 */
package gr.aueb.cs.nlp.postagger;

public class BigSetWordWithCategories {

    private String word;
    private double AtDfMaSgNm;
    private double AtDfMaSgGe;
    private double AtDfMaSgAc;
    private double AtDfMaPlNm;
    private double AtDfMaPlGe;
    private double AtDfMaPlAc;
    private double AtDfFeSgNm;
    private double AtDfFeSgGe;
    private double AtDfFeSgAc;
    private double AtDfFePlNm;
    private double AtDfFePlGe;
    private double AtDfFePlAc;
    private double AtDfNeSgNm;
    private double AtDfNeSgGe;
    private double AtDfNeSgAc;
    private double AtDfNePlNm;
    private double AtDfNePlGe;
    private double AtDfNePlAc;
    private double AtIdMaSgNm;
    private double AtIdMaSgGe;
    private double AtIdMaSgAc;
    private double AtIdMaPlNm;
    private double AtIdMaPlGe;
    private double AtIdMaPlAc;
    private double AtIdFeSgNm;
    private double AtIdFeSgGe;
    private double AtIdFeSgAc;
    private double AtIdNeSgNm;
    private double AtIdNeSgGe;
    private double AtIdNeSgAc;
    private double NoMaSgNm;
    private double NoMaSgGe;
    private double NoMaSgAc;
    private double NoMaPlNm;
    private double NoMaPlGe;
    private double NoMaPlAc;
    private double NoFeSgNm;
    private double NoFeSgGe;
    private double NoFeSgAc;
    private double NoFePlNm;
    private double NoFePlGe;
    private double NoFePlAc;
    private double NoNeSgNm;
    private double NoNeSgGe;
    private double NoNeSgAc;
    private double NoNePlNm;
    private double NoNePlGe;
    private double NoNePlAc;
    private double AjMaSgNm;
    private double AjMaSgGe;
    private double AjMaSgAc;
    private double AjMaPlNm;
    private double AjMaPlGe;
    private double AjMaPlAc;
    private double AjFeSgNm;
    private double AjFeSgGe;
    private double AjFeSgAc;
    private double AjFePlNm;
    private double AjFePlGe;
    private double AjFePlAc;
    private double AjNeSgNm;
    private double AjNeSgGe;
    private double AjNeSgAc;
    private double AjNePlNm;
    private double AjNePlGe;
    private double AjNePlAc;
    private double PnIc;
    private double PnSgNm;
    private double PnSgGe;
    private double PnSgAc;
    private double PnPlNm;
    private double PnPlGe;
    private double PnPlAc;
    private double PnMaSgNm;
    private double PnMaSgGe;
    private double PnMaSgAc;
    private double PnMaPlNm;
    private double PnMaPlGe;
    private double PnMaPlAc;
    private double PnFeSgNm;
    private double PnFeSgGe;
    private double PnFeSgAc;
    private double PnFePlNm;
    private double PnFePlGe;
    private double PnFePlAc;
    private double PnNeSgNm;
    private double PnNeSgGe;
    private double PnNeSgAc;
    private double PnNePlNm;
    private double PnNePlGe;
    private double PnNePlAc;
    private double NmCd;
    private double AtPpFePlAc;
    private double AtPpFeSgAc;
    private double AtPpMaPlAc;
    private double AtPpMaSgAc;
    private double AtPpNePlAc;
    private double AtPpNeSgAc;
    private double AtPpNePlNm;
    private double AtPpNeSgNm;
    private double VbMnPrPlAv;
    private double VbMnPaPlAv;
    private double VbMnXxPlAv;
    private double VbMnPrSgAv;
    private double VbMnPaSgAv;
    private double VbMnXxSgAv;
    private double VbMnPrPlPv;
    private double VbMnPaPlPv;
    private double VbMnXxPlPv;
    private double VbMnPrSgPv;
    private double VbMnPaSgPv;
    private double VbMnXxSgPv;
    private double VbMnNfAv;
    private double VbMnNfPv;
    private double VbPp;
    private double RgOt;
    private double Ad;
    private double AsPp;
    private double Cj;
    private double Pt;
    private double Pu;
    private double RgSy;
    private double RgAb;
    private double RgAn;
    private double RgFw;
    private double AtPpFePlGe;
    private double AtPpFeSgGe;
    private double AtPpMaPlGe;
    private double AtPpMaSgGe;
    private double AtPpNePlGe;
    private double AtPpNeSgGe;
    private double AtPpFePlNm;
    private double AtPpFeSgNm;
    private double AtPpMaPlNm;
    private double AtPpMaSgNm;
    private double AtPpFePlVc;
    private double AtPpFeSgVc;
    private double AtPpMaPlVc;
    private double AtPpMaSgVc;
    private double AtPpNePlVc;
    private double AtPpNeSgVc;
    private double AtIdFeSgVc;
    private double AtIdMaSgVc;
    private double AtIdNeSgVc;
    private double AtDfFePlVc;
    private double AtDfFeSgVc;
    private double AtDfMaPlVc;
    private double AtDfMaSgVc;
    private double AtDfNePlVc;
    private double AtDfNeSgVc;
    private double PnSgVc;
    private double PnPlVc;
    private double PnMaSgVc;
    private double PnMaPlVc;
    private double PnFeSgVc;
    private double PnFePlVc;
    private double PnNeSgVc;
    private double PnNePlVc;
    private double NoMaSgVc;
    private double NoMaPlVc;
    private double NoFeSgVc;
    private double NoFePlVc;
    private double NoNeSgVc;
    private double NoNePlVc;
    private double AjMaSgVc;
    private double AjMaPlVc;
    private double AjFeSgVc;
    private double AjFePlVc;
    private double AjNeSgVc;
    private double AjNePlVc;

    protected BigSetWordWithCategories(String w) {
        word = new String(w);
        AtDfMaSgNm = 0.0;
        AtDfMaSgGe = 0.0;
        AtDfMaSgAc = 0.0;
        AtDfMaPlNm = 0.0;
        AtDfMaPlGe = 0.0;
        AtDfMaPlAc = 0.0;
        AtDfFeSgNm = 0.0;
        AtDfFeSgGe = 0.0;
        AtDfFeSgAc = 0.0;
        AtDfFePlNm = 0.0;
        AtDfFePlGe = 0.0;
        AtDfFePlAc = 0.0;
        AtDfNeSgNm = 0.0;
        AtDfNeSgGe = 0.0;
        AtDfNeSgAc = 0.0;
        AtDfNePlNm = 0.0;
        AtDfNePlGe = 0.0;
        AtDfNePlAc = 0.0;
        AtIdMaSgNm = 0.0;
        AtIdMaSgGe = 0.0;
        AtIdMaSgAc = 0.0;
        AtIdMaPlNm = 0.0;
        AtIdMaPlGe = 0.0;
        AtIdMaPlAc = 0.0;
        AtIdFeSgNm = 0.0;
        AtIdFeSgGe = 0.0;
        AtIdFeSgAc = 0.0;
        AtIdNeSgNm = 0.0;
        AtIdNeSgGe = 0.0;
        AtIdNeSgAc = 0.0;
        NoMaSgNm = 0.0;
        NoMaSgGe = 0.0;
        NoMaSgAc = 0.0;
        NoMaPlNm = 0.0;
        NoMaPlGe = 0.0;
        NoMaPlAc = 0.0;
        NoFeSgNm = 0.0;
        NoFeSgGe = 0.0;
        NoFeSgAc = 0.0;
        NoFePlNm = 0.0;
        NoFePlGe = 0.0;
        NoFePlAc = 0.0;
        NoNeSgNm = 0.0;
        NoNeSgGe = 0.0;
        NoNeSgAc = 0.0;
        NoNePlNm = 0.0;
        NoNePlGe = 0.0;
        NoNePlAc = 0.0;
        AjMaSgNm = 0.0;
        AjMaSgGe = 0.0;
        AjMaSgAc = 0.0;
        AjMaPlNm = 0.0;
        AjMaPlGe = 0.0;
        AjMaPlAc = 0.0;
        AjFeSgNm = 0.0;
        AjFeSgGe = 0.0;
        AjFeSgAc = 0.0;
        AjFePlNm = 0.0;
        AjFePlGe = 0.0;
        AjFePlAc = 0.0;
        AjNeSgNm = 0.0;
        AjNeSgGe = 0.0;
        AjNeSgAc = 0.0;
        AjNePlNm = 0.0;
        AjNePlGe = 0.0;
        AjNePlAc = 0.0;
        PnIc = 0.0;
        PnSgNm = 0.0;
        PnSgGe = 0.0;
        PnSgAc = 0.0;
        PnPlNm = 0.0;
        PnPlGe = 0.0;
        PnPlAc = 0.0;
        PnMaSgNm = 0.0;
        PnMaSgGe = 0.0;
        PnMaSgAc = 0.0;
        PnMaPlNm = 0.0;
        PnMaPlGe = 0.0;
        PnMaPlAc = 0.0;
        PnFeSgNm = 0.0;
        PnFeSgGe = 0.0;
        PnFeSgAc = 0.0;
        PnFePlNm = 0.0;
        PnFePlGe = 0.0;
        PnFePlAc = 0.0;
        PnNeSgNm = 0.0;
        PnNeSgGe = 0.0;
        PnNeSgAc = 0.0;
        PnNePlNm = 0.0;
        PnNePlGe = 0.0;
        PnNePlAc = 0.0;
        NmCd = 0.0;
        AtPpFePlAc = 0.0;
        AtPpFeSgAc = 0.0;
        AtPpMaPlAc = 0.0;
        AtPpMaSgAc = 0.0;
        AtPpNePlAc = 0.0;
        AtPpNeSgAc = 0.0;
        AtPpNePlNm = 0.0;
        AtPpNeSgNm = 0.0;
        VbMnPrPlAv = 0.0;
        VbMnPaPlAv = 0.0;
        VbMnXxPlAv = 0.0;
        VbMnPrSgAv = 0.0;
        VbMnPaSgAv = 0.0;
        VbMnXxSgAv = 0.0;
        VbMnPrPlPv = 0.0;
        VbMnPaPlPv = 0.0;
        VbMnXxPlPv = 0.0;
        VbMnPrSgPv = 0.0;
        VbMnPaSgPv = 0.0;
        VbMnXxSgPv = 0.0;
        VbMnNfAv = 0.0;
        VbMnNfPv = 0.0;
        VbPp = 0.0;
        RgOt = 0.0;
        Ad = 0.0;
        AsPp = 0.0;
        Cj = 0.0;
        Pt = 0.0;
        Pu = 0.0;
        RgSy = 0.0;
        RgAb = 0.0;
        RgAn = 0.0;
        RgFw = 0.0;
        AtPpFePlGe = 0.0;
        AtPpFeSgGe = 0.0;
        AtPpMaPlGe = 0.0;
        AtPpMaSgGe = 0.0;
        AtPpNePlGe = 0.0;
        AtPpNeSgGe = 0.0;
        AtPpFePlNm = 0.0;
        AtPpFeSgNm = 0.0;
        AtPpMaPlNm = 0.0;
        AtPpMaSgNm = 0.0;
        AtPpFePlVc = 0.0;
        AtPpFeSgVc = 0.0;
        AtPpMaPlVc = 0.0;
        AtPpMaSgVc = 0.0;
        AtPpNePlVc = 0.0;
        AtPpNeSgVc = 0.0;
        AtIdFeSgVc = 0.0;
        AtIdMaSgVc = 0.0;
        AtIdNeSgVc = 0.0;
        AtDfFePlVc = 0.0;
        AtDfFeSgVc = 0.0;
        AtDfMaPlVc = 0.0;
        AtDfMaSgVc = 0.0;
        AtDfNePlVc = 0.0;
        AtDfNeSgVc = 0.0;
        PnSgVc = 0.0;
        PnPlVc = 0.0;
        PnMaSgVc = 0.0;
        PnMaPlVc = 0.0;
        PnFeSgVc = 0.0;
        PnFePlVc = 0.0;
        PnNeSgVc = 0.0;
        PnNePlVc = 0.0;
        NoMaSgVc = 0.0;
        NoMaPlVc = 0.0;
        NoFeSgVc = 0.0;
        NoFePlVc = 0.0;
        NoNeSgVc = 0.0;
        NoNePlVc = 0.0;
        AjMaSgVc = 0.0;
        AjMaPlVc = 0.0;
        AjFeSgVc = 0.0;
        AjFePlVc = 0.0;
        AjNeSgVc = 0.0;
        AjNePlVc = 0.0;

    }

    protected BigSetWordWithCategories(BigSetWordWithCategories w) {
        word = w.word;
        AtDfMaSgNm = w.AtDfMaSgNm;
        AtDfMaSgGe = w.AtDfMaSgGe;
        AtDfMaSgAc = w.AtDfMaSgAc;
        AtDfMaPlNm = w.AtDfMaPlNm;
        AtDfMaPlGe = w.AtDfMaPlGe;
        AtDfMaPlAc = w.AtDfMaPlAc;
        AtDfFeSgNm = w.AtDfFeSgNm;
        AtDfFeSgGe = w.AtDfFeSgGe;
        AtDfFeSgAc = w.AtDfFeSgAc;
        AtDfFePlNm = w.AtDfFePlNm;
        AtDfFePlGe = w.AtDfFePlGe;
        AtDfFePlAc = w.AtDfFePlAc;
        AtDfNeSgNm = w.AtDfNeSgNm;
        AtDfNeSgGe = w.AtDfNeSgGe;
        AtDfNeSgAc = w.AtDfNeSgAc;
        AtDfNePlNm = w.AtDfNePlNm;
        AtDfNePlGe = w.AtDfNePlGe;
        AtDfNePlAc = w.AtDfNePlAc;
        AtIdMaSgNm = w.AtIdMaSgNm;
        AtIdMaSgGe = w.AtIdMaSgGe;
        AtIdMaSgAc = w.AtIdMaSgAc;
        AtIdMaPlNm = w.AtIdMaPlNm;
        AtIdMaPlGe = w.AtIdMaPlGe;
        AtIdMaPlAc = w.AtIdMaPlAc;
        AtIdFeSgNm = w.AtIdFeSgNm;
        AtIdFeSgGe = w.AtIdFeSgGe;
        AtIdFeSgAc = w.AtIdFeSgAc;
        AtIdNeSgNm = w.AtIdNeSgNm;
        AtIdNeSgGe = w.AtIdNeSgGe;
        AtIdNeSgAc = w.AtIdNeSgAc;
        NoMaSgNm = w.NoMaSgNm;
        NoMaSgGe = w.NoMaSgGe;
        NoMaSgAc = w.NoMaSgAc;
        NoMaPlNm = w.NoMaPlNm;
        NoMaPlGe = w.NoMaPlGe;
        NoMaPlAc = w.NoMaPlAc;
        NoFeSgNm = w.NoFeSgNm;
        NoFeSgGe = w.NoFeSgGe;
        NoFeSgAc = w.NoFeSgAc;
        NoFePlNm = w.NoFePlNm;
        NoFePlGe = w.NoFePlGe;
        NoFePlAc = w.NoFePlAc;
        NoNeSgNm = w.NoNeSgNm;
        NoNeSgGe = w.NoNeSgGe;
        NoNeSgAc = w.NoNeSgAc;
        NoNePlNm = w.NoNePlNm;
        NoNePlGe = w.NoNePlGe;
        NoNePlAc = w.NoNePlAc;
        AjMaSgNm = w.AjMaSgNm;
        AjMaSgGe = w.AjMaSgGe;
        AjMaSgAc = w.AjMaSgAc;
        AjMaPlNm = w.AjMaPlNm;
        AjMaPlGe = w.AjMaPlGe;
        AjMaPlAc = w.AjMaPlAc;
        AjFeSgNm = w.AjFeSgNm;
        AjFeSgGe = w.AjFeSgGe;
        AjFeSgAc = w.AjFeSgAc;
        AjFePlNm = w.AjFePlNm;
        AjFePlGe = w.AjFePlGe;
        AjFePlAc = w.AjFePlAc;
        AjNeSgNm = w.AjNeSgNm;
        AjNeSgGe = w.AjNeSgGe;
        AjNeSgAc = w.AjNeSgAc;
        AjNePlNm = w.AjNePlNm;
        AjNePlGe = w.AjNePlGe;
        AjNePlAc = w.AjNePlAc;
        PnIc = w.PnIc;
        PnSgNm = w.PnSgNm;
        PnSgGe = w.PnSgGe;
        PnSgAc = w.PnSgAc;
        PnPlNm = w.PnPlNm;
        PnPlGe = w.PnPlGe;
        PnPlAc = w.PnPlAc;
        PnMaSgNm = w.PnMaSgNm;
        PnMaSgGe = w.PnMaSgGe;
        PnMaSgAc = w.PnMaSgAc;
        PnMaPlNm = w.PnMaPlNm;
        PnMaPlGe = w.PnMaPlGe;
        PnMaPlAc = w.PnMaPlAc;
        PnFeSgNm = w.PnFeSgNm;
        PnFeSgGe = w.PnFeSgGe;
        PnFeSgAc = w.PnFeSgAc;
        PnFePlNm = w.PnFePlNm;
        PnFePlGe = w.PnFePlGe;
        PnFePlAc = w.PnFePlAc;
        PnNeSgNm = w.PnNeSgNm;
        PnNeSgGe = w.PnNeSgGe;
        PnNeSgAc = w.PnNeSgAc;
        PnNePlNm = w.PnNePlNm;
        PnNePlGe = w.PnNePlGe;
        PnNePlAc = w.PnNePlAc;
        NmCd = w.NmCd;
        AtPpFePlAc = w.AtPpFePlAc;
        AtPpFeSgAc = w.AtPpFeSgAc;
        AtPpMaPlAc = w.AtPpMaPlAc;
        AtPpMaSgAc = w.AtPpMaSgAc;
        AtPpNePlAc = w.AtPpNePlAc;
        AtPpNeSgAc = w.AtPpNeSgAc;
        AtPpNePlNm = w.AtPpNePlNm;
        AtPpNeSgNm = w.AtPpNeSgNm;
        VbMnPrPlAv = w.VbMnPrPlAv;
        VbMnPaPlAv = w.VbMnPaPlAv;
        VbMnXxPlAv = w.VbMnXxPlAv;
        VbMnPrSgAv = w.VbMnPrSgAv;
        VbMnPaSgAv = w.VbMnPaSgAv;
        VbMnXxSgAv = w.VbMnXxSgAv;
        VbMnPrPlPv = w.VbMnPrPlPv;
        VbMnPaPlPv = w.VbMnPaPlPv;
        VbMnXxPlPv = w.VbMnXxPlPv;
        VbMnPrSgPv = w.VbMnPrSgPv;
        VbMnPaSgPv = w.VbMnPaSgPv;
        VbMnXxSgPv = w.VbMnXxSgPv;
        VbMnNfAv = w.VbMnNfAv;
        VbMnNfPv = w.VbMnNfPv;
        VbPp = w.VbPp;
        RgOt = w.RgOt;
        Ad = w.Ad;
        AsPp = w.AsPp;
        Cj = w.Cj;
        Pt = w.Pt;
        Pu = w.Pu;
        RgSy = w.RgSy;
        RgAb = w.RgAb;
        RgAn = w.RgAn;
        RgFw = w.RgFw;
        AtPpFePlGe = w.AtPpFePlGe;
        AtPpFeSgGe = w.AtPpFeSgGe;
        AtPpMaPlGe = w.AtPpMaPlGe;
        AtPpMaSgGe = w.AtPpMaSgGe;
        AtPpNePlGe = w.AtPpNePlGe;
        AtPpNeSgGe = w.AtPpNeSgGe;
        AtPpFePlNm = w.AtPpFePlNm;
        AtPpFeSgNm = w.AtPpFeSgNm;
        AtPpMaPlNm = w.AtPpMaPlNm;
        AtPpMaSgNm = w.AtPpMaSgNm;
        AtPpFePlVc = w.AtPpFePlVc;
        AtPpFeSgVc = w.AtPpFeSgVc;
        AtPpMaPlVc = w.AtPpMaPlVc;
        AtPpMaSgVc = w.AtPpMaSgVc;
        AtPpNePlVc = w.AtPpNePlVc;
        AtPpNeSgVc = w.AtPpNeSgVc;
        AtIdFeSgVc = w.AtIdFeSgVc;
        AtIdMaSgVc = w.AtIdMaSgVc;
        AtIdNeSgVc = w.AtIdNeSgVc;

        AtDfFePlVc = w.AtDfFePlVc;
        AtDfFeSgVc = w.AtDfFeSgVc;
        AtDfMaPlVc = w.AtDfMaPlVc;
        AtDfMaSgVc = w.AtDfMaSgVc;
        AtDfNePlVc = w.AtDfNePlVc;
        AtDfNeSgVc = w.AtDfNeSgVc;
        PnSgVc = w.PnSgVc;
        PnPlVc = w.PnPlVc;
        PnMaSgVc = w.PnMaSgVc;
        PnMaPlVc = w.PnMaPlVc;
        PnFeSgVc = w.PnFeSgVc;
        PnFePlVc = w.PnFePlVc;
        PnNeSgVc = w.PnNeSgVc;
        PnNePlVc = w.PnNePlVc;
        NoMaSgVc = w.NoMaSgVc;
        NoMaPlVc = w.NoMaPlVc;
        NoFeSgVc = w.NoFeSgVc;
        NoFePlVc = w.NoFePlVc;
        NoNeSgVc = w.NoNeSgVc;
        NoNePlVc = w.NoNePlVc;
        AjMaSgVc = w.AjMaSgVc;
        AjMaPlVc = w.AjMaPlVc;
        AjFeSgVc = w.AjFeSgVc;
        AjFePlVc = w.AjFePlVc;
        AjNeSgVc = w.AjNeSgVc;
        AjNePlVc = w.AjNePlVc;

    }

    protected void setWord(String w) {
        word = w;
    }

    protected void setAtDfMaSgNm(double b) {
        AtDfMaSgNm = b;
    }

    protected void setAtDfMaSgGe(double b) {
        AtDfMaSgGe = b;
    }

    protected void setAtDfMaSgAc(double b) {
        AtDfMaSgAc = b;
    }

    protected void setAtDfMaPlNm(double b) {
        AtDfMaPlNm = b;
    }

    protected void setAtDfMaPlGe(double b) {
        AtDfMaPlGe = b;
    }

    protected void setAtDfMaPlAc(double b) {
        AtDfMaPlAc = b;
    }

    protected void setAtDfFeSgNm(double b) {
        AtDfFeSgNm = b;
    }

    protected void setAtDfFeSgGe(double b) {
        AtDfFeSgGe = b;
    }

    protected void setAtDfFeSgAc(double b) {
        AtDfFeSgAc = b;
    }

    protected void setAtDfFePlNm(double b) {
        AtDfFePlNm = b;
    }

    protected void setAtDfFePlGe(double b) {
        AtDfFePlGe = b;
    }

    protected void setAtDfFePlAc(double b) {
        AtDfFePlAc = b;
    }

    protected void setAtDfNeSgNm(double b) {
        AtDfNeSgNm = b;
    }

    protected void setAtDfNeSgGe(double b) {
        AtDfNeSgGe = b;
    }

    protected void setAtDfNeSgAc(double b) {
        AtDfNeSgAc = b;
    }

    protected void setAtDfNePlNm(double b) {
        AtDfNePlNm = b;
    }

    protected void setAtDfNePlGe(double b) {
        AtDfNePlGe = b;
    }

    protected void setAtDfNePlAc(double b) {
        AtDfNePlAc = b;
    }

    protected void setAtIdMaSgNm(double b) {
        AtIdMaSgNm = b;
    }

    protected void setAtIdMaSgGe(double b) {
        AtIdMaSgGe = b;
    }

    protected void setAtIdMaSgAc(double b) {
        AtIdMaSgAc = b;
    }

    protected void setAtIdMaPlNm(double b) {
        AtIdMaPlNm = b;
    }

    protected void setAtIdMaPlGe(double b) {
        AtIdMaPlGe = b;
    }

    protected void setAtIdMaPlAc(double b) {
        AtIdMaPlAc = b;
    }

    protected void setAtIdFeSgNm(double b) {
        AtIdFeSgNm = b;
    }

    protected void setAtIdFeSgGe(double b) {
        AtIdFeSgGe = b;
    }

    protected void setAtIdFeSgAc(double b) {
        AtIdFeSgAc = b;
    }

    protected void setAtIdNeSgNm(double b) {
        AtIdNeSgNm = b;
    }

    protected void setAtIdNeSgGe(double b) {
        AtIdNeSgGe = b;
    }

    protected void setAtIdNeSgAc(double b) {
        AtIdNeSgAc = b;
    }

    protected void setNoMaSgNm(double b) {
        NoMaSgNm = b;
    }

    protected void setNoMaSgGe(double b) {
        NoMaSgGe = b;
    }

    protected void setNoMaSgAc(double b) {
        NoMaSgAc = b;
    }

    protected void setNoMaPlNm(double b) {
        NoMaPlNm = b;
    }

    protected void setNoMaPlGe(double b) {
        NoMaPlGe = b;
    }

    protected void setNoMaPlAc(double b) {
        NoMaPlAc = b;
    }

    protected void setNoFeSgNm(double b) {
        NoFeSgNm = b;
    }

    protected void setNoFeSgGe(double b) {
        NoFeSgGe = b;
    }

    protected void setNoFeSgAc(double b) {
        NoFeSgAc = b;
    }

    protected void setNoFePlNm(double b) {
        NoFePlNm = b;
    }

    protected void setNoFePlGe(double b) {
        NoFePlGe = b;
    }

    protected void setNoFePlAc(double b) {
        NoFePlAc = b;
    }

    protected void setNoNeSgNm(double b) {
        NoNeSgNm = b;
    }

    protected void setNoNeSgGe(double b) {
        NoNeSgGe = b;
    }

    protected void setNoNeSgAc(double b) {
        NoNeSgAc = b;
    }

    protected void setNoNePlNm(double b) {
        NoNePlNm = b;
    }

    protected void setNoNePlGe(double b) {
        NoNePlGe = b;
    }

    protected void setNoNePlAc(double b) {
        NoNePlAc = b;
    }

    protected void setAjMaSgNm(double b) {
        AjMaSgNm = b;
    }

    protected void setAjMaSgGe(double b) {
        AjMaSgGe = b;
    }

    protected void setAjMaSgAc(double b) {
        AjMaSgAc = b;
    }

    protected void setAjMaPlNm(double b) {
        AjMaPlNm = b;
    }

    protected void setAjMaPlGe(double b) {
        AjMaPlGe = b;
    }

    protected void setAjMaPlAc(double b) {
        AjMaPlAc = b;
    }

    protected void setAjFeSgNm(double b) {
        AjFeSgNm = b;
    }

    protected void setAjFeSgGe(double b) {
        AjFeSgGe = b;
    }

    protected void setAjFeSgAc(double b) {
        AjFeSgAc = b;
    }

    protected void setAjFePlNm(double b) {
        AjFePlNm = b;
    }

    protected void setAjFePlGe(double b) {
        AjFePlGe = b;
    }

    protected void setAjFePlAc(double b) {
        AjFePlAc = b;
    }

    protected void setAjNeSgNm(double b) {
        AjNeSgNm = b;
    }

    protected void setAjNeSgGe(double b) {
        AjNeSgGe = b;
    }

    protected void setAjNeSgAc(double b) {
        AjNeSgAc = b;
    }

    protected void setAjNePlNm(double b) {
        AjNePlNm = b;
    }

    protected void setAjNePlGe(double b) {
        AjNePlGe = b;
    }

    protected void setAjNePlAc(double b) {
        AjNePlAc = b;
    }

    protected void setAtPpFePlAc(double b) {
        AtPpFePlAc = b;
    }

    protected void setAtPpFeSgAc(double b) {
        AtPpFeSgAc = b;
    }

    protected void setAtPpMaPlAc(double b) {
        AtPpMaPlAc = b;
    }

    protected void setAtPpMaSgAc(double b) {
        AtPpMaSgAc = b;
    }

    protected void setAtPpNePlAc(double b) {
        AtPpNePlAc = b;
    }

    protected void setAtPpNeSgAc(double b) {
        AtPpNeSgAc = b;
    }

    protected void setAtPpNePlNm(double b) {
        AtPpNePlNm = b;
    }

    protected void setAtPpNeSgNm(double b) {
        AtPpNeSgNm = b;
    }

    protected void setVbMnPrPlAv(double b) {
        VbMnPrPlAv = b;
    }

    protected void setVbMnPaPlAv(double b) {
        VbMnPaPlAv = b;
    }

    protected void setVbMnXxPlAv(double b) {
        VbMnXxPlAv = b;
    }

    protected void setVbMnPrSgAv(double b) {
        VbMnPrSgAv = b;
    }

    protected void setVbMnPaSgAv(double b) {
        VbMnPaSgAv = b;
    }

    protected void setVbMnXxSgAv(double b) {
        VbMnXxSgAv = b;
    }

    protected void setVbMnPrPlPv(double b) {
        VbMnPrPlPv = b;
    }

    protected void setVbMnPaPlPv(double b) {
        VbMnPaPlPv = b;
    }

    protected void setVbMnXxPlPv(double b) {
        VbMnXxPlPv = b;
    }

    protected void setVbMnPrSgPv(double b) {
        VbMnPrSgPv = b;
    }

    protected void setVbMnPaSgPv(double b) {
        VbMnPaSgPv = b;
    }

    protected void setVbMnXxSgPv(double b) {
        VbMnXxSgPv = b;
    }

    protected void setVbMnNfAv(double b) {
        VbMnNfAv = b;
    }

    protected void setVbMnNfPv(double b) {
        VbMnNfPv = b;
    }

    protected void setVbPp(double b) {
        VbPp = b;
    }

    protected void setRgOt(double b) {
        RgOt = b;
    }

    protected void setPnIc(double b) {
        PnIc = b;
    }

    protected void setPnSgNm(double b) {
        PnSgNm = b;
    }

    protected void setPnSgGe(double b) {
        PnSgGe = b;
    }

    protected void setPnSgAc(double b) {
        PnSgAc = b;
    }

    protected void setPnPlNm(double b) {
        PnPlNm = b;
    }

    protected void setPnPlGe(double b) {
        PnPlGe = b;
    }

    protected void setPnPlAc(double b) {
        PnPlAc = b;
    }

    protected void setPnMaSgNm(double b) {
        PnMaSgNm = b;
    }

    protected void setPnMaSgGe(double b) {
        PnMaSgGe = b;
    }

    protected void setPnMaSgAc(double b) {
        PnMaSgAc = b;
    }

    protected void setPnMaPlNm(double b) {
        PnMaPlNm = b;
    }

    protected void setPnMaPlGe(double b) {
        PnMaPlGe = b;
    }

    protected void setPnMaPlAc(double b) {
        PnMaPlAc = b;
    }

    protected void setPnFeSgNm(double b) {
        PnFeSgNm = b;
    }

    protected void setPnFeSgGe(double b) {
        PnFeSgGe = b;
    }

    protected void setPnFeSgAc(double b) {
        PnFeSgAc = b;
    }

    protected void setPnFePlNm(double b) {
        PnFePlNm = b;
    }

    protected void setPnFePlGe(double b) {
        PnFePlGe = b;
    }

    protected void setPnFePlAc(double b) {
        PnFePlAc = b;
    }

    protected void setPnNeSgNm(double b) {
        PnNeSgNm = b;
    }

    protected void setPnNeSgGe(double b) {
        PnNeSgGe = b;
    }

    protected void setPnNeSgAc(double b) {
        PnNeSgAc = b;
    }

    protected void setPnNePlNm(double b) {
        PnNePlNm = b;
    }

    protected void setPnNePlGe(double b) {
        PnNePlGe = b;
    }

    protected void setPnNePlAc(double b) {
        PnNePlAc = b;
    }

    protected void setNmCd(double b) {
        NmCd = b;
    }

    protected void setAd(double b) {
        Ad = b;
    }

    protected void setAsPp(double b) {
        AsPp = b;
    }

    protected void setCj(double b) {
        Cj = b;
    }

    protected void setPt(double b) {
        Pt = b;
    }

    protected void setPu(double b) {
        Pu = b;
    }

    protected void setRgSy(double b) {
        RgSy = b;
    }

    protected void setRgAb(double b) {
        RgAb = b;
    }

    protected void setRgAn(double b) {
        RgAn = b;
    }

    protected void setRgFw(double b) {
        RgFw = b;
    }

    protected void setAtPpFePlGe(double b) {
        AtPpFePlGe = b;
    }

    protected void setAtPpFeSgGe(double b) {
        AtPpFeSgGe = b;
    }

    protected void setAtPpMaPlGe(double b) {
        AtPpMaPlGe = b;
    }

    protected void setAtPpMaSgGe(double b) {
        AtPpMaSgGe = b;
    }

    protected void setAtPpNePlGe(double b) {
        AtPpNePlGe = b;
    }

    protected void setAtPpNeSgGe(double b) {
        AtPpNeSgGe = b;
    }

    protected void setAtPpFePlNm(double b) {
        AtPpFePlNm = b;
    }

    protected void setAtPpFeSgNm(double b) {
        AtPpFeSgNm = b;
    }

    protected void setAtPpMaPlNm(double b) {
        AtPpMaPlNm = b;
    }

    protected void setAtPpMaSgNm(double b) {
        AtPpMaSgNm = b;
    }

    protected void setAtPpFePlVc(double b) {
        AtPpFePlVc = b;
    }

    protected void setAtPpFeSgVc(double b) {
        AtPpFeSgVc = b;
    }

    protected void setAtPpMaPlVc(double b) {
        AtPpMaPlVc = b;
    }

    protected void setAtPpMaSgVc(double b) {
        AtPpMaSgVc = b;
    }

    protected void setAtPpNePlVc(double b) {
        AtPpNePlVc = b;
    }

    protected void setAtPpNeSgVc(double b) {
        AtPpNeSgVc = b;
    }

    protected void setAtIdFeSgVc(double b) {
        AtIdFeSgVc = b;
    }

    protected void setAtIdMaSgVc(double b) {
        AtIdMaSgVc = b;
    }

    protected void setAtIdNeSgVc(double b) {
        AtIdNeSgVc = b;
    }

    protected void setAtDfFePlVc(double b) {
        AtDfFePlVc = b;
    }

    protected void setAtDfFeSgVc(double b) {
        AtDfFeSgVc = b;
    }

    protected void setAtDfMaPlVc(double b) {
        AtDfMaPlVc = b;
    }

    protected void setAtDfMaSgVc(double b) {
        AtDfMaSgVc = b;
    }

    protected void setAtDfNePlVc(double b) {
        AtDfNePlVc = b;
    }

    protected void setAtDfNeSgVc(double b) {
        AtDfNeSgVc = b;
    }

    protected void setPnSgVc(double b) {
        PnSgVc = b;
    }

    protected void setPnPlVc(double b) {
        PnPlVc = b;
    }

    protected void setPnMaSgVc(double b) {
        PnMaSgVc = b;
    }

    protected void setPnMaPlVc(double b) {
        PnMaPlVc = b;
    }

    protected void setPnFeSgVc(double b) {
        PnFeSgVc = b;
    }

    protected void setPnFePlVc(double b) {
        PnFePlVc = b;
    }

    protected void setPnNeSgVc(double b) {
        PnNeSgVc = b;
    }

    protected void setPnNePlVc(double b) {
        PnNePlVc = b;
    }

    protected void setNoMaSgVc(double b) {
        NoMaSgVc = b;
    }

    protected void setNoMaPlVc(double b) {
        NoMaPlVc = b;
    }

    protected void setNoFeSgVc(double b) {
        NoFeSgVc = b;
    }

    protected void setNoFePlVc(double b) {
        NoFePlVc = b;
    }

    protected void setNoNeSgVc(double b) {
        NoNeSgVc = b;
    }

    protected void setNoNePlVc(double b) {
        NoNePlVc = b;
    }

    protected void setAjMaSgVc(double b) {
        AjMaSgVc = b;
    }

    protected void setAjMaPlVc(double b) {
        AjMaPlVc = b;
    }

    protected void setAjFeSgVc(double b) {
        AjFeSgVc = b;
    }

    protected void setAjFePlVc(double b) {
        AjFePlVc = b;
    }

    protected void setAjNeSgVc(double b) {
        AjNeSgVc = b;
    }

    protected void setAjNePlVc(double b) {
        AjNePlVc = b;
    }

    protected BigSetWordWithCategories getWordWithCategories() {
        return this;
    }


    protected String getWord() {
        return word;
    }

    protected double getAtDfMaSgNm() {
        return AtDfMaSgNm;
    }

    protected double getAtDfMaSgGe() {
        return AtDfMaSgGe;
    }

    protected double getAtDfMaSgAc() {
        return AtDfMaSgAc;
    }

    protected double getAtDfMaPlNm() {
        return AtDfMaPlNm;
    }

    protected double getAtDfMaPlGe() {
        return AtDfMaPlGe;
    }

    protected double getAtDfMaPlAc() {
        return AtDfMaPlAc;
    }

    protected double getAtDfFeSgNm() {
        return AtDfFeSgNm;
    }

    protected double getAtDfFeSgGe() {
        return AtDfFeSgGe;
    }

    protected double getAtDfFeSgAc() {
        return AtDfFeSgAc;
    }

    protected double getAtDfFePlNm() {
        return AtDfFePlNm;
    }

    protected double getAtDfFePlGe() {
        return AtDfFePlGe;
    }

    protected double getAtDfFePlAc() {
        return AtDfFePlAc;
    }

    protected double getAtDfNeSgNm() {
        return AtDfNeSgNm;
    }

    protected double getAtDfNeSgGe() {
        return AtDfNeSgGe;
    }

    protected double getAtDfNeSgAc() {
        return AtDfNeSgAc;
    }

    protected double getAtDfNePlNm() {
        return AtDfNePlNm;
    }

    protected double getAtDfNePlGe() {
        return AtDfNePlGe;
    }

    protected double getAtDfNePlAc() {
        return AtDfNePlAc;
    }

    protected double getAtIdMaSgNm() {
        return AtIdMaSgNm;
    }

    protected double getAtIdMaSgGe() {
        return AtIdMaSgGe;
    }

    protected double getAtIdMaSgAc() {
        return AtIdMaSgAc;
    }

    protected double getAtIdMaPlNm() {
        return AtIdMaPlNm;
    }

    protected double getAtIdMaPlGe() {
        return AtIdMaPlGe;
    }

    protected double getAtIdMaPlAc() {
        return AtIdMaPlAc;
    }

    protected double getAtIdFeSgNm() {
        return AtIdFeSgNm;
    }

    protected double getAtIdFeSgGe() {
        return AtIdFeSgGe;
    }

    protected double getAtIdFeSgAc() {
        return AtIdFeSgAc;
    }

    protected double getAtIdNeSgNm() {
        return AtIdNeSgNm;
    }

    protected double getAtIdNeSgGe() {
        return AtIdNeSgGe;
    }

    protected double getAtIdNeSgAc() {
        return AtIdNeSgAc;
    }

    protected double getNoMaSgNm() {
        return NoMaSgNm;
    }

    protected double getNoMaSgGe() {
        return NoMaSgGe;
    }

    protected double getNoMaSgAc() {
        return NoMaSgAc;
    }

    protected double getNoMaPlNm() {
        return NoMaPlNm;
    }

    protected double getNoMaPlGe() {
        return NoMaPlGe;
    }

    protected double getNoMaPlAc() {
        return NoMaPlAc;
    }

    protected double getNoFeSgNm() {
        return NoFeSgNm;
    }

    protected double getNoFeSgGe() {
        return NoFeSgGe;
    }

    protected double getNoFeSgAc() {
        return NoFeSgAc;
    }

    protected double getNoFePlNm() {
        return NoFePlNm;
    }

    protected double getNoFePlGe() {
        return NoFePlGe;
    }

    protected double getNoFePlAc() {
        return NoFePlAc;
    }

    protected double getNoNeSgNm() {
        return NoNeSgNm;
    }

    protected double getNoNeSgGe() {
        return NoNeSgGe;
    }

    protected double getNoNeSgAc() {
        return NoNeSgAc;
    }

    protected double getNoNePlNm() {
        return NoNePlNm;
    }

    protected double getNoNePlGe() {
        return NoNePlGe;
    }

    protected double getNoNePlAc() {
        return NoNePlAc;
    }

    protected double getAjMaSgNm() {
        return AjMaSgNm;
    }

    protected double getAjMaSgGe() {
        return AjMaSgGe;
    }

    protected double getAjMaSgAc() {
        return AjMaSgAc;
    }

    protected double getAjMaPlNm() {
        return AjMaPlNm;
    }

    protected double getAjMaPlGe() {
        return AjMaPlGe;
    }

    protected double getAjMaPlAc() {
        return AjMaPlAc;
    }

    protected double getAjFeSgNm() {
        return AjFeSgNm;
    }

    protected double getAjFeSgGe() {
        return AjFeSgGe;
    }

    protected double getAjFeSgAc() {
        return AjFeSgAc;
    }

    protected double getAjFePlNm() {
        return AjFePlNm;
    }

    protected double getAjFePlGe() {
        return AjFePlGe;
    }

    protected double getAjFePlAc() {
        return AjFePlAc;
    }

    protected double getAjNeSgNm() {
        return AjNeSgNm;
    }

    protected double getAjNeSgGe() {
        return AjNeSgGe;
    }

    protected double getAjNeSgAc() {
        return AjNeSgAc;
    }

    protected double getAjNePlNm() {
        return AjNePlNm;
    }

    protected double getAjNePlGe() {
        return AjNePlGe;
    }

    protected double getAjNePlAc() {
        return AjNePlAc;
    }

    protected double getAtPpFePlAc() {
        return AtPpFePlAc;
    }

    protected double getAtPpFeSgAc() {
        return AtPpFeSgAc;
    }

    protected double getAtPpMaPlAc() {
        return AtPpMaPlAc;
    }

    protected double getAtPpMaSgAc() {
        return AtPpMaSgAc;
    }

    protected double getAtPpNePlAc() {
        return AtPpNePlAc;
    }

    protected double getAtPpNeSgAc() {
        return AtPpNeSgAc;
    }

    protected double getAtPpNePlNm() {
        return AtPpNePlNm;
    }

    protected double getAtPpNeSgNm() {
        return AtPpNeSgNm;
    }

    protected double getVbMnPrPlAv() {
        return VbMnPrPlAv;
    }

    protected double getVbMnPaPlAv() {
        return VbMnPaPlAv;
    }

    protected double getVbMnXxPlAv() {
        return VbMnXxPlAv;
    }

    protected double getVbMnPrSgAv() {
        return VbMnPrSgAv;
    }

    protected double getVbMnPaSgAv() {
        return VbMnPaSgAv;
    }

    protected double getVbMnXxSgAv() {
        return VbMnXxSgAv;
    }

    protected double getVbMnPrPlPv() {
        return VbMnPrPlPv;
    }

    protected double getVbMnPaPlPv() {
        return VbMnPaPlPv;
    }

    protected double getVbMnXxPlPv() {
        return VbMnXxPlPv;
    }

    protected double getVbMnPrSgPv() {
        return VbMnPrSgPv;
    }

    protected double getVbMnPaSgPv() {
        return VbMnPaSgPv;
    }

    protected double getVbMnXxSgPv() {
        return VbMnXxSgPv;
    }

    protected double getVbMnNfAv() {
        return VbMnNfAv;
    }

    protected double getVbMnNfPv() {
        return VbMnNfPv;
    }

    protected double getVbPp() {
        return VbPp;
    }

    protected double getRgOt() {
        return RgOt;
    }

    protected double getPnIc() {
        return PnIc;
    }

    protected double getPnSgNm() {
        return PnSgNm;
    }

    protected double getPnSgGe() {
        return PnSgGe;
    }

    protected double getPnSgAc() {
        return PnSgAc;
    }

    protected double getPnPlNm() {
        return PnPlNm;
    }

    protected double getPnPlGe() {
        return PnPlGe;
    }

    protected double getPnPlAc() {
        return PnPlAc;
    }

    protected double getPnMaSgNm() {
        return PnMaSgNm;
    }

    protected double getPnMaSgGe() {
        return PnMaSgGe;
    }

    protected double getPnMaSgAc() {
        return PnMaSgAc;
    }

    protected double getPnMaPlNm() {
        return PnMaPlNm;
    }

    protected double getPnMaPlGe() {
        return PnMaPlGe;
    }

    protected double getPnMaPlAc() {
        return PnMaPlAc;
    }

    protected double getPnFeSgNm() {
        return PnFeSgNm;
    }

    protected double getPnFeSgGe() {
        return PnFeSgGe;
    }

    protected double getPnFeSgAc() {
        return PnFeSgAc;
    }

    protected double getPnFePlNm() {
        return PnFePlNm;
    }

    protected double getPnFePlGe() {
        return PnFePlGe;
    }

    protected double getPnFePlAc() {
        return PnFePlAc;
    }

    protected double getPnNeSgNm() {
        return PnNeSgNm;
    }

    protected double getPnNeSgGe() {
        return PnNeSgGe;
    }

    protected double getPnNeSgAc() {
        return PnNeSgAc;
    }

    protected double getPnNePlNm() {
        return PnNePlNm;
    }

    protected double getPnNePlGe() {
        return PnNePlGe;
    }

    protected double getPnNePlAc() {
        return PnNePlAc;
    }

    protected double getNmCd() {
        return NmCd;
    }

    protected double getAd() {
        return Ad;
    }

    protected double getAsPp() {
        return AsPp;
    }

    protected double getCj() {
        return Cj;
    }

    protected double getPt() {
        return Pt;
    }

    protected double getPu() {
        return Pu;
    }

    protected double getRgSy() {
        return RgSy;
    }

    protected double getRgAb() {
        return RgAb;
    }

    protected double getRgAn() {
        return RgAn;
    }

    protected double getRgFw() {
        return RgFw;
    }

    protected double getAtPpFePlGe() {
        return AtPpFePlGe;
    }

    protected double getAtPpFeSgGe() {
        return AtPpFeSgGe;
    }

    protected double getAtPpMaPlGe() {
        return AtPpMaPlGe;
    }

    protected double getAtPpMaSgGe() {
        return AtPpMaSgGe;
    }

    protected double getAtPpNePlGe() {
        return AtPpNePlGe;
    }

    protected double getAtPpNeSgGe() {
        return AtPpNeSgGe;
    }

    protected double getAtPpFePlNm() {
        return AtPpFePlNm;
    }

    protected double getAtPpFeSgNm() {
        return AtPpFeSgNm;
    }

    protected double getAtPpMaPlNm() {
        return AtPpMaPlNm;
    }

    protected double getAtPpMaSgNm() {
        return AtPpMaSgNm;
    }

    protected double getAtPpFePlVc() {
        return AtPpFePlVc;
    }

    protected double getAtPpFeSgVc() {
        return AtPpFeSgVc;
    }

    protected double getAtPpMaPlVc() {
        return AtPpMaPlVc;
    }

    protected double getAtPpMaSgVc() {
        return AtPpMaSgVc;
    }

    protected double getAtPpNePlVc() {
        return AtPpNePlVc;
    }

    protected double getAtPpNeSgVc() {
        return AtPpNeSgVc;
    }

    protected double getAtIdFeSgVc() {
        return AtIdFeSgVc;
    }

    protected double getAtIdMaSgVc() {
        return AtIdMaSgVc;
    }

    protected double getAtIdNeSgVc() {
        return AtIdNeSgVc;
    }

    protected double getAtDfFePlVc() {
        return AtDfFePlVc;
    }

    protected double getAtDfFeSgVc() {
        return AtDfFeSgVc;
    }

    protected double getAtDfMaPlVc() {
        return AtDfMaPlVc;
    }

    protected double getAtDfMaSgVc() {
        return AtDfMaSgVc;
    }

    protected double getAtDfNePlVc() {
        return AtDfNePlVc;
    }

    protected double getAtDfNeSgVc() {
        return AtDfNeSgVc;
    }

    protected double getPnSgVc() {
        return PnSgVc;
    }

    protected double getPnPlVc() {
        return PnPlVc;
    }

    protected double getPnMaSgVc() {
        return PnMaSgVc;
    }

    protected double getPnMaPlVc() {
        return PnMaPlVc;
    }

    protected double getPnFeSgVc() {
        return PnFeSgVc;
    }

    protected double getPnFePlVc() {
        return PnFePlVc;
    }

    protected double getPnNeSgVc() {
        return PnNeSgVc;
    }

    protected double getPnNePlVc() {
        return PnNePlVc;
    }

    protected double getNoMaSgVc() {
        return NoMaSgVc;
    }

    protected double getNoMaPlVc() {
        return NoMaPlVc;
    }

    protected double getNoFeSgVc() {
        return NoFeSgVc;
    }

    protected double getNoFePlVc() {
        return NoFePlVc;
    }

    protected double getNoNeSgVc() {
        return NoNeSgVc;
    }

    protected double getNoNePlVc() {
        return NoNePlVc;
    }

    protected double getAjMaSgVc() {
        return AjMaSgVc;
    }

    protected double getAjMaPlVc() {
        return AjMaPlVc;
    }

    protected double getAjFeSgVc() {
        return AjFeSgVc;
    }

    protected double getAjFePlVc() {
        return AjFePlVc;
    }

    protected double getAjNeSgVc() {
        return AjNeSgVc;
    }

    protected double getAjNePlVc() {
        return AjNePlVc;
    }

    protected boolean equals(BigSetWordWithCategories w) {
        return this.word.equals(w.word);
    }

    protected void setProperties(int index, double value) {
        switch (index) {
            case 0:
                this.setAtDfMaSgNm(value);
            case 1:
                this.setAtDfMaSgGe(value);
            case 2:
                this.setAtDfMaSgAc(value);
            case 3:
                this.setAtDfMaPlNm(value);
            case 4:
                this.setAtDfMaPlGe(value);
            case 5:
                this.setAtDfMaPlAc(value);
            case 6:
                this.setAtDfFeSgNm(value);
            case 7:
                this.setAtDfFeSgGe(value);
            case 8:
                this.setAtDfFeSgAc(value);
            case 9:
                this.setAtDfFePlNm(value);
            case 10:
                this.setAtDfFePlGe(value);
            case 11:
                this.setAtDfFePlAc(value);
            case 12:
                this.setAtDfNeSgNm(value);
            case 13:
                this.setAtDfNeSgGe(value);
            case 14:
                this.setAtDfNeSgAc(value);
            case 15:
                this.setAtDfNePlNm(value);
            case 16:
                this.setAtDfNePlGe(value);
            case 17:
                this.setAtDfNePlAc(value);
            case 18:
                this.setAtIdMaSgNm(value);
            case 19:
                this.setAtIdMaSgGe(value);
            case 20:
                this.setAtIdMaSgAc(value);
            case 21:
                this.setAtIdMaPlNm(value);
            case 22:
                this.setAtIdMaPlGe(value);
            case 23:
                this.setAtIdMaPlAc(value);
            case 24:
                this.setAtIdFeSgNm(value);
            case 25:
                this.setAtIdFeSgGe(value);
            case 26:
                this.setAtIdFeSgAc(value);
            case 27:
                this.setAtIdNeSgNm(value);
            case 28:
                this.setAtIdNeSgGe(value);
            case 29:
                this.setAtIdNeSgAc(value);
            case 30:
                this.setNoMaSgNm(value);
            case 31:
                this.setNoMaSgGe(value);
            case 32:
                this.setNoMaSgAc(value);
            case 33:
                this.setNoMaPlNm(value);
            case 34:
                this.setNoMaPlGe(value);
            case 35:
                this.setNoMaPlAc(value);
            case 36:
                this.setNoFeSgNm(value);
            case 37:
                this.setNoFeSgGe(value);
            case 38:
                this.setNoFeSgAc(value);
            case 39:
                this.setNoFePlNm(value);
            case 40:
                this.setNoFePlGe(value);
            case 41:
                this.setNoFePlAc(value);
            case 42:
                this.setNoNeSgNm(value);
            case 43:
                this.setNoNeSgGe(value);
            case 44:
                this.setNoNeSgAc(value);
            case 45:
                this.setNoNePlNm(value);
            case 46:
                this.setNoNePlGe(value);
            case 47:
                this.setNoNePlAc(value);
            case 48:
                this.setAjMaSgNm(value);
            case 49:
                this.setAjMaSgGe(value);
            case 50:
                this.setAjMaSgAc(value);
            case 51:
                this.setAjMaPlNm(value);
            case 52:
                this.setAjMaPlGe(value);
            case 53:
                this.setAjMaPlAc(value);
            case 54:
                this.setAjFeSgNm(value);
            case 55:
                this.setAjFeSgGe(value);
            case 56:
                this.setAjFeSgAc(value);
            case 57:
                this.setAjFePlNm(value);
            case 58:
                this.setAjFePlGe(value);
            case 59:
                this.setAjFePlAc(value);
            case 60:
                this.setAjNeSgNm(value);
            case 61:
                this.setAjNeSgGe(value);
            case 62:
                this.setAjNeSgAc(value);
            case 63:
                this.setAjNePlNm(value);
            case 64:
                this.setAjNePlGe(value);
            case 65:
                this.setAjNePlAc(value);
            case 66:
                this.setPnIc(value);
            case 67:
                this.setPnSgNm(value);
            case 68:
                this.setPnSgGe(value);
            case 69:
                this.setPnSgAc(value);
            case 70:
                this.setPnPlNm(value);
            case 71:
                this.setPnPlGe(value);
            case 72:
                this.setPnPlAc(value);
            case 73:
                this.setPnMaSgNm(value);
            case 74:
                this.setPnMaSgGe(value);
            case 75:
                this.setPnMaSgAc(value);
            case 76:
                this.setPnMaPlNm(value);
            case 77:
                this.setPnMaPlGe(value);
            case 78:
                this.setPnMaPlAc(value);
            case 79:
                this.setPnFeSgNm(value);
            case 80:
                this.setPnFeSgGe(value);
            case 81:
                this.setPnFeSgAc(value);
            case 82:
                this.setPnFePlNm(value);
            case 83:
                this.setPnFePlGe(value);
            case 84:
                this.setPnFePlAc(value);
            case 85:
                this.setPnNeSgNm(value);
            case 86:
                this.setPnNeSgGe(value);
            case 87:
                this.setPnNeSgAc(value);
            case 88:
                this.setPnNePlNm(value);
            case 89:
                this.setPnNePlGe(value);
            case 90:
                this.setPnNePlAc(value);
            case 91:
                this.setNmCd(value);
            case 92:
                this.setAtPpFePlAc(value);
            case 93:
                this.setAtPpFeSgAc(value);
            case 94:
                this.setAtPpMaPlAc(value);
            case 95:
                this.setAtPpMaSgAc(value);
            case 96:
                this.setAtPpNePlAc(value);
            case 97:
                this.setAtPpNeSgAc(value);
            case 98:
                this.setAtPpNePlNm(value);
            case 99:
                this.setAtPpNeSgNm(value);
            case 100:
                this.setVbMnPrPlAv(value);
            case 101:
                this.setVbMnPaPlAv(value);
            case 102:
                this.setVbMnXxPlAv(value);
            case 103:
                this.setVbMnPrSgAv(value);
            case 104:
                this.setVbMnPaSgAv(value);
            case 105:
                this.setVbMnXxSgAv(value);
            case 106:
                this.setVbMnPrPlPv(value);
            case 107:
                this.setVbMnPaPlPv(value);
            case 108:
                this.setVbMnXxPlPv(value);
            case 109:
                this.setVbMnPrSgPv(value);
            case 110:
                this.setVbMnPaSgPv(value);
            case 111:
                this.setVbMnXxSgPv(value);
            case 112:
                this.setVbMnNfAv(value);
            case 113:
                this.setVbMnNfPv(value);
            case 114:
                this.setVbPp(value);
            case 115:
                this.setRgOt(value);
            case 116:
                this.setAd(value);
            case 117:
                this.setAsPp(value);
            case 118:
                this.setCj(value);
            case 119:
                this.setPt(value);
            case 120:
                this.setPu(value);
            case 121:
                this.setRgSy(value);
            case 122:
                this.setRgAb(value);
            case 123:
                this.setRgAn(value);
            case 124:
                this.setRgFw(value);
            case 125:
                this.setAtPpFePlGe(value);
            case 126:
                this.setAtPpFeSgGe(value);
            case 127:
                this.setAtPpMaPlGe(value);
            case 128:
                this.setAtPpMaSgGe(value);
            case 129:
                this.setAtPpNePlGe(value);
            case 130:
                this.setAtPpNeSgGe(value);
            case 131:
                this.setAtPpFePlNm(value);
            case 132:
                this.setAtPpFeSgNm(value);
            case 133:
                this.setAtPpMaPlNm(value);
            case 134:
                this.setAtPpMaSgNm(value);
            case 135:
                this.setAtPpFePlVc(value);
            case 136:
                this.setAtPpFeSgVc(value);
            case 137:
                this.setAtPpMaPlVc(value);
            case 138:
                this.setAtPpMaSgVc(value);
            case 139:
                this.setAtPpNePlVc(value);
            case 140:
                this.setAtPpNeSgVc(value);
            case 141:
                this.setAtIdFeSgVc(value);
            case 142:
                this.setAtIdMaSgVc(value);
            case 143:
                this.setAtIdNeSgVc(value);
            case 144:
                this.setAtDfFePlVc(value);
            case 145:
                this.setAtDfFeSgVc(value);
            case 146:
                this.setAtDfMaPlVc(value);
            case 147:
                this.setAtDfMaSgVc(value);
            case 148:
                this.setAtDfNePlVc(value);
            case 149:
                this.setAtDfNeSgVc(value);
            case 150:
                this.setPnSgVc(value);
            case 151:
                this.setPnPlVc(value);
            case 152:
                this.setPnMaSgVc(value);
            case 153:
                this.setPnMaPlVc(value);
            case 154:
                this.setPnFeSgVc(value);
            case 155:
                this.setPnFePlVc(value);
            case 156:
                this.setPnNeSgVc(value);
            case 157:
                this.setPnNePlVc(value);
            case 158:
                this.setNoMaSgVc(value);
            case 159:
                this.setNoMaPlVc(value);
            case 160:
                this.setNoFeSgVc(value);
            case 161:
                this.setNoFePlVc(value);
            case 162:
                this.setNoNeSgVc(value);
            case 163:
                this.setNoNePlVc(value);
            case 164:
                this.setAjMaSgVc(value);
            case 165:
                this.setAjMaPlVc(value);
            case 166:
                this.setAjFeSgVc(value);
            case 167:
                this.setAjFePlVc(value);
            case 168:
                this.setAjNeSgVc(value);
            case 169:
                this.setAjNePlVc(value);

        }
    }

    @Override
    public String toString() {
        String s = "";
        s = s.concat(AtDfMaSgNm + " ");
        s = s.concat(AtDfMaSgGe + " ");
        s = s.concat(AtDfMaSgAc + " ");
        s = s.concat(AtDfMaPlNm + " ");
        s = s.concat(AtDfMaPlGe + " ");
        s = s.concat(AtDfMaPlAc + " ");
        s = s.concat(AtDfFeSgNm + " ");
        s = s.concat(AtDfFeSgGe + " ");
        s = s.concat(AtDfFeSgAc + " ");
        s = s.concat(AtDfFePlNm + " ");
        s = s.concat(AtDfFePlGe + " ");
        s = s.concat(AtDfFePlAc + " ");
        s = s.concat(AtDfNeSgNm + " ");
        s = s.concat(AtDfNeSgGe + " ");
        s = s.concat(AtDfNeSgAc + " ");
        s = s.concat(AtDfNePlNm + " ");
        s = s.concat(AtDfNePlGe + " ");
        s = s.concat(AtDfNePlAc + " ");
        s = s.concat(AtIdMaSgNm + " ");
        s = s.concat(AtIdMaSgGe + " ");
        s = s.concat(AtIdMaSgAc + " ");
        s = s.concat(AtIdMaPlNm + " ");
        s = s.concat(AtIdMaPlGe + " ");
        s = s.concat(AtIdMaPlAc + " ");
        s = s.concat(AtIdFeSgNm + " ");
        s = s.concat(AtIdFeSgGe + " ");
        s = s.concat(AtIdFeSgAc + " ");
        s = s.concat(AtIdNeSgNm + " ");
        s = s.concat(AtIdNeSgGe + " ");
        s = s.concat(AtIdNeSgAc + " ");
        s = s.concat(NoMaSgNm + " ");
        s = s.concat(NoMaSgGe + " ");
        s = s.concat(NoMaSgAc + " ");
        s = s.concat(NoMaPlNm + " ");
        s = s.concat(NoMaPlGe + " ");
        s = s.concat(NoMaPlAc + " ");
        s = s.concat(NoFeSgNm + " ");
        s = s.concat(NoFeSgGe + " ");
        s = s.concat(NoFeSgAc + " ");
        s = s.concat(NoFePlNm + " ");
        s = s.concat(NoFePlGe + " ");
        s = s.concat(NoFePlAc + " ");
        s = s.concat(NoNeSgNm + " ");
        s = s.concat(NoNeSgGe + " ");
        s = s.concat(NoNeSgAc + " ");
        s = s.concat(NoNePlNm + " ");
        s = s.concat(NoNePlGe + " ");
        s = s.concat(NoNePlAc + " ");
        s = s.concat(AjMaSgNm + " ");
        s = s.concat(AjMaSgGe + " ");
        s = s.concat(AjMaSgAc + " ");
        s = s.concat(AjMaPlNm + " ");
        s = s.concat(AjMaPlGe + " ");
        s = s.concat(AjMaPlAc + " ");
        s = s.concat(AjFeSgNm + " ");
        s = s.concat(AjFeSgGe + " ");
        s = s.concat(AjFeSgAc + " ");
        s = s.concat(AjFePlNm + " ");
        s = s.concat(AjFePlGe + " ");
        s = s.concat(AjFePlAc + " ");
        s = s.concat(AjNeSgNm + " ");
        s = s.concat(AjNeSgGe + " ");
        s = s.concat(AjNeSgAc + " ");
        s = s.concat(AjNePlNm + " ");
        s = s.concat(AjNePlGe + " ");
        s = s.concat(AjNePlAc + " ");
        s = s.concat(PnIc + " ");
        s = s.concat(PnSgNm + " ");
        s = s.concat(PnSgGe + " ");
        s = s.concat(PnSgAc + " ");
        s = s.concat(PnPlNm + " ");
        s = s.concat(PnPlGe + " ");
        s = s.concat(PnPlAc + " ");
        s = s.concat(PnMaSgNm + " ");
        s = s.concat(PnMaSgGe + " ");
        s = s.concat(PnMaSgAc + " ");
        s = s.concat(PnMaPlNm + " ");
        s = s.concat(PnMaPlGe + " ");
        s = s.concat(PnMaPlAc + " ");
        s = s.concat(PnFeSgNm + " ");
        s = s.concat(PnFeSgGe + " ");
        s = s.concat(PnFeSgAc + " ");
        s = s.concat(PnFePlNm + " ");
        s = s.concat(PnFePlGe + " ");
        s = s.concat(PnFePlAc + " ");
        s = s.concat(PnNeSgNm + " ");
        s = s.concat(PnNeSgGe + " ");
        s = s.concat(PnNeSgAc + " ");
        s = s.concat(PnNePlNm + " ");
        s = s.concat(PnNePlGe + " ");
        s = s.concat(PnNePlAc + " ");
        s = s.concat(NmCd + " ");
        s = s.concat(AtPpFePlAc + " ");
        s = s.concat(AtPpFeSgAc + " ");
        s = s.concat(AtPpMaPlAc + " ");
        s = s.concat(AtPpMaSgAc + " ");
        s = s.concat(AtPpNePlAc + " ");
        s = s.concat(AtPpNeSgAc + " ");
        s = s.concat(AtPpNePlNm + " ");
        s = s.concat(AtPpNeSgNm + " ");
        s = s.concat(VbMnPrPlAv + " ");
        s = s.concat(VbMnPaPlAv + " ");
        s = s.concat(VbMnXxPlAv + " ");
        s = s.concat(VbMnPrSgAv + " ");
        s = s.concat(VbMnPaSgAv + " ");
        s = s.concat(VbMnXxSgAv + " ");
        s = s.concat(VbMnPrPlPv + " ");
        s = s.concat(VbMnPaPlPv + " ");
        s = s.concat(VbMnXxPlPv + " ");
        s = s.concat(VbMnPrSgPv + " ");
        s = s.concat(VbMnPaSgPv + " ");
        s = s.concat(VbMnXxSgPv + " ");
        s = s.concat(VbMnNfAv + " ");
        s = s.concat(VbMnNfPv + " ");
        s = s.concat(VbPp + " ");
        s = s.concat(RgOt + " ");
        s = s.concat(Ad + " ");
        s = s.concat(AsPp + " ");
        s = s.concat(Cj + " ");
        s = s.concat(Pt + " ");
        s = s.concat(Pu + " ");
        s = s.concat(RgSy + " ");
        s = s.concat(RgAb + " ");
        s = s.concat(RgAn + " ");
        s = s.concat(RgFw + " ");
        s = s.concat(AtPpFePlGe + " ");
        s = s.concat(AtPpFeSgGe + " ");
        s = s.concat(AtPpMaPlGe + " ");
        s = s.concat(AtPpMaSgGe + " ");
        s = s.concat(AtPpNePlGe + " ");
        s = s.concat(AtPpNeSgGe + " ");
        s = s.concat(AtPpFePlNm + " ");
        s = s.concat(AtPpFeSgNm + " ");
        s = s.concat(AtPpMaPlNm + " ");
        s = s.concat(AtPpMaSgNm + " ");
        s = s.concat(AtPpFePlVc + " ");
        s = s.concat(AtPpFeSgVc + " ");
        s = s.concat(AtPpMaPlVc + " ");
        s = s.concat(AtPpMaSgVc + " ");
        s = s.concat(AtPpNePlVc + " ");
        s = s.concat(AtPpNeSgVc + " ");
        s = s.concat(AtIdFeSgVc + " ");
        s = s.concat(AtIdMaSgVc + " ");
        s = s.concat(AtIdNeSgVc + " ");
        s = s.concat(AtDfFePlVc + " ");
        s = s.concat(AtDfFeSgVc + " ");
        s = s.concat(AtDfMaPlVc + " ");
        s = s.concat(AtDfMaSgVc + " ");
        s = s.concat(AtDfNePlVc + " ");
        s = s.concat(AtDfNeSgVc + " ");
        s = s.concat(PnSgVc + " ");
        s = s.concat(PnPlVc + " ");
        s = s.concat(PnMaSgVc + " ");
        s = s.concat(PnMaPlVc + " ");
        s = s.concat(PnFeSgVc + " ");
        s = s.concat(PnFePlVc + " ");
        s = s.concat(PnNeSgVc + " ");
        s = s.concat(PnNePlVc + " ");
        s = s.concat(NoMaSgVc + " ");
        s = s.concat(NoMaPlVc + " ");
        s = s.concat(NoFeSgVc + " ");
        s = s.concat(NoFePlVc + " ");
        s = s.concat(NoNeSgVc + " ");
        s = s.concat(NoNePlVc + " ");
        s = s.concat(AjMaSgVc + " ");
        s = s.concat(AjMaPlVc + " ");
        s = s.concat(AjFeSgVc + " ");
        s = s.concat(AjFePlVc + " ");
        s = s.concat(AjNeSgVc + " ");
        s = s.concat(AjNePlVc + " ");


        //s = s.concat(Integer.toString(ambiguity));
        return s;
    }
}
