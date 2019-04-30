/*
 * Copyright 2007-2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Apache UIMA
 * Copyright 2006, 2011 The Apache Software Foundation
 *
 * This product includes software developed at
 * The Apache Software Foundation (http://www.apache.org/).
 *
 * Portions of UIMA were originally developed by
 * International Business Machines Corporation and are
 * licensed to the Apache Software Foundation under the
 * "Software Grant License Agreement", informally known as the
 * "IBM UIMA License Agreement".
 * Copyright (c) 2003, 2006 IBM Corporation.
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.stanfordnlp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UIMARuntimeException;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.ByteArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.DoubleArrayFS;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.FloatArrayFS;
import org.apache.uima.cas.IntArrayFS;
import org.apache.uima.cas.LongArrayFS;
import org.apache.uima.cas.ShortArrayFS;
import org.apache.uima.cas.SofaFS;
import org.apache.uima.cas.StringArrayFS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.LowLevelCAS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Utility class for doing deep copies of FeatureStructures from one CAS to another. To handle cases
 * where the source CAS has multiple references to the same FS, you can create one instance of
 * CasCopier and use it to copy multiple FeatureStructures. The CasCopier will remember previously
 * copied FeatureStructures, so if you later copy another FS that has a reference to a previously
 * copied FS, it will not duplicate the multiply-referenced FS.
 */
public class CasCopier
{
    private final CAS mSrcCas;
    private final CAS mDestCas;
    private final LowLevelCAS mLowLevelDestCas;
    private final Feature mDestSofaFeature;

    private final Map<FeatureStructure, FeatureStructure> mFsMap = 
            new HashMap<FeatureStructure, FeatureStructure>();

    private List<Annotation> batchCopyAnnoList = null;

    /**
     * Creates a new CasCopier that can be used to copy FeatureStructures from one CAS to another.
     * Note that if you are merging data from multiple CASes, you must create a new CasCopier for
     * each source CAS.
     *
     * @param aSrcCas
     *            the CAS to copy from.
     * @param aDestCas
     *            the CAS to copy into.
     */
    public CasCopier(CAS aSrcCas, CAS aDestCas)
    {
        mSrcCas = aSrcCas;
        mDestCas = aDestCas;
        mLowLevelDestCas = aDestCas.getLowLevelCAS();
        mDestSofaFeature = aDestCas.getTypeSystem()
                .getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);
    }

    /**
     * Does a complete deep copy of one CAS into another CAS. The contents of each view in the
     * source CAS will be copied to the same-named view in the destination CAS. If the view does not
     * already exist it will be created. All FeatureStructures that are indexed in a view in the
     * source CAS will become indexed in the same-named view in the destination CAS.
     *
     * @param aSrcCas
     *            the CAS to copy from
     * @param aDestCas
     *            the CAS to copy to
     * @param aCopySofa
     *            if true, the sofa data and mimeType of each view will be copied. If false they
     *            will not.
     */
    public static void copyCas(CAS aSrcCas, CAS aDestCas, boolean aCopySofa)
    {
        CasCopier copier = new CasCopier(aSrcCas, aDestCas);

        Iterator<SofaFS> sofaIter = aSrcCas.getSofaIterator();
        while (sofaIter.hasNext()) {
            SofaFS sofa = sofaIter.next();
            CAS view = aSrcCas.getView(sofa);
            copier.copyCasView(view, aCopySofa);
        }
    }

    /**
     * Does a deep copy of the contents of one CAS View into another CAS. If a view with the same
     * name as <code>aSrcCasView</code> exists in the destination CAS, then it will be the target of
     * the copy. Otherwise, a new view will be created with that name and will become the target of
     * the copy. All FeatureStructures that are indexed in the source CAS view will become indexed
     * in the target view.
     *
     * @param aSrcCasView
     *            the CAS to copy from
     * @param aCopySofa
     *            if true, the sofa data and mimeType will be copied. If false they will not.
     */
    public void copyCasView(CAS aSrcCasView, boolean aCopySofa)
    {
        // get or create the target view
        CAS targetView = getOrCreateView(mDestCas, aSrcCasView.getViewName());

        if (aCopySofa) {
            // can't copy the SofaFS - just copy the sofa data and mime type
            String sofaMime = aSrcCasView.getSofa().getSofaMime();
            if (aSrcCasView.getDocumentText() != null) {
                targetView.setSofaDataString(aSrcCasView.getDocumentText(), sofaMime);
            }
            else if (aSrcCasView.getSofaDataURI() != null) {
                targetView.setSofaDataURI(aSrcCasView.getSofaDataURI(), sofaMime);
            }
            else if (aSrcCasView.getSofaDataArray() != null) {
                targetView.setSofaDataArray(copyFs(aSrcCasView.getSofaDataArray()), sofaMime);
            }
        }

        // now copy indexed FS, but keep track so we don't index anything more
        // than once
        Set<FeatureStructure> indexedFs = new HashSet<FeatureStructure>();
        Iterator<FSIndex<FeatureStructure>> indexes = aSrcCasView.getIndexRepository().getIndexes();
        while (indexes.hasNext()) {
            FSIndex<FeatureStructure> index = indexes.next();
            Iterator<FeatureStructure> iter = index.iterator();
            while (iter.hasNext()) {
                FeatureStructure fs = iter.next();
                if (!indexedFs.contains(fs)) {
                    FeatureStructure copyOfFs = copyFs(fs);
                    // check for annotations with null Sofa reference - this can
                    // happen if the annotations were created with the Low Level
                    // CAS API. If the Sofa reference isn't set, attempting to
                    // add the FS to the indexes will fail.
                    if (fs instanceof AnnotationFS) {
                        FeatureStructure sofa = ((AnnotationFS) copyOfFs)
                                .getFeatureValue(mDestSofaFeature);
                        if (sofa == null) {
                            copyOfFs.setFeatureValue(mDestSofaFeature, targetView.getSofa());
                        }
                    }
                    // also don't index the DocumentAnnotation (it's indexed by
                    // default)
                    if (!isDocumentAnnotation(copyOfFs)) {
                        targetView.addFsToIndexes(copyOfFs);
                    }
                    indexedFs.add(fs);
                }
            }
        }
    }

    /**
     * Copies an FS from the source CAS to the destination CAS. Also copies any referenced FS,
     * except that previously copied FS will not be copied again.
     *
     * @param aFS
     *            the FS to copy. Must be contained within the source CAS.
     * @return the copy of <code>aFS</code> in the target CAS.
     */
    public FeatureStructure copyFs(FeatureStructure aFS)
    {
        // FS must be in the source CAS
        assert ((CASImpl) aFS.getCAS()).getBaseCAS() == ((CASImpl) mSrcCas).getBaseCAS();

        // check if we already copied this FS
        FeatureStructure copy = mFsMap.get(aFS);
        if (copy != null) {
            return copy;
        }

        // get the type of the FS
        Type srcType = aFS.getType();

        // Certain types need to be handled specially

        // Sofa - cannot be created by normal methods. Instead, we return the
        // Sofa with the
        // same Sofa ID in the target CAS. If it does not exist it will be
        // created.
        if (aFS instanceof SofaFS) {
            String sofaId = ((SofaFS) aFS).getSofaID();
            return getOrCreateView(mDestCas, sofaId).getSofa();
        }

        // DocumentAnnotation - instead of creating a new instance, reuse the
        // automatically created
        // instance in the destination view.
        if (isDocumentAnnotation(aFS)) {
            String viewName = ((AnnotationFS) aFS).getView().getViewName();
            CAS destView = mDestCas.getView(viewName);
            FeatureStructure destDocAnnot = destView.getDocumentAnnotation();
            if (destDocAnnot != null) {
                copyFeatures(aFS, destDocAnnot);
            }
            return destDocAnnot;
        }

        // Arrays - need to be created a populated differently than "normal" FS
        if (aFS.getType().isArray()) {
            copy = copyArray(aFS);
            mFsMap.put(aFS, copy);
            return copy;
        }

        // create a new FS of the same type in the target CAS
        Type destType;
        if (mDestCas.getTypeSystem() == mSrcCas.getTypeSystem()) {
            // optimize type lookup if type systems are identical
            destType = srcType;
        }
        else {
            destType = mDestCas.getTypeSystem().getType(srcType.getName());
        }
        if (destType == null) {
            throw new UIMARuntimeException(UIMARuntimeException.TYPE_NOT_FOUND_DURING_CAS_COPY,
                    new Object[] { srcType.getName() });
        }
        // We need to use the LowLevel CAS interface to create the FS, because
        // the usual CAS.createFS() call doesn't allow us to create subtypes of
        // AnnotationBase from a base CAS. In any case we don't need the Sofa
        // reference to be automatically set because we'll set it manually when
        // in the copyFeatures method.
        int typeCode = mLowLevelDestCas.ll_getTypeSystem().ll_getCodeForType(destType);
        int destFsAddr = mLowLevelDestCas.ll_createFS(typeCode);
        FeatureStructure destFs = mDestCas.getLowLevelCAS().ll_getFSForRef(destFsAddr);

        // add to map so we don't try to copy this more than once
        mFsMap.put(aFS, destFs);

        copyFeatures(aFS, destFs);
        return destFs;
    }

    /**
     * Copy feature values from one FS to another. For reference-valued features, this does a deep
     * copy.
     *
     * <p>
     * NOTE:<br/>
     * Different behavior as the original CasCopier!!!
     * </P>
     *
     * <strong>_IF_ there is no appropriate alternative present in the destination CAS. If the
     * destination CAS contains an annotation that is identical to the referenced annotation in the
     * sourceCas except it has a different ID, then it won't be deep copied, but the reference-id in
     * the annotation will be updated.</strong>
     *
     * @param aSrcFS
     *            FeatureStructure to copy from
     * @param aDestFS
     *            FeatureStructure to copy to
     */
    private void copyFeatures(FeatureStructure aSrcFS, FeatureStructure aDestFS)
    {
        // set feature values
        Type srcType = aSrcFS.getType();
        Type destType = aDestFS.getType();
        for (Feature srcFeat : srcType.getFeatures()) {
            Feature destFeat;
            if (destType == aSrcFS.getType()) {
                // sharing same type system, so destFeat == srcFeat
                destFeat = srcFeat;
            }
            else {
                // not sharing same type system, so do a name loop up in
                // destination type system
                destFeat = destType.getFeatureByBaseName(srcFeat.getShortName());
                if (destFeat == null) {
                    throw new UIMARuntimeException(
                            UIMARuntimeException.TYPE_NOT_FOUND_DURING_CAS_COPY,
                            new Object[] { srcFeat.getName() });
                }
            }

            // copy primitive values using their string representation
            // TODO: could be optimized but this code would be very messy if we
            // have to
            // enumerate all possible primitive types. Maybe LowLevel CAS API
            // could help?
            if (srcFeat.getRange().isPrimitive()) {
                aDestFS.setFeatureValueFromString(destFeat,
                        aSrcFS.getFeatureValueAsString(srcFeat));
            }
            else {
                // recursive copy
                FeatureStructure refFS = aSrcFS.getFeatureValue(srcFeat);
                if (refFS != null) {
                    boolean foundexisting = false;
                    // if yes, only update ID in the copied annotation
                    if (refFS instanceof Annotation) {
                        // Check if referenced annotation is among the
                        // annotations that have not YET been recovered
                        // Here we can check the Annotation-address (id),
                        // because we are recovering original instances,
                        // no recreated annotations.
                        if (!foundexisting && batchCopyAnnoList != null) {
                            for (Annotation curAnno : batchCopyAnnoList) {
                                if (curAnno.getAddress() == ((Annotation) refFS).getAddress()) {
                                    aDestFS.setFeatureValue(destFeat, copyFs(curAnno));
                                    foundexisting = true;
                                }
                            }
                        }
                        // If last check has been unsuccessful, check if
                        // the referenced annotation is a parser annotation that
                        // has been recreated during the deserialization of the
                        // transformed Stanford tree object.
                        // In this case we want to change the reference to the
                        // new annotation (which should already be present in the
                        // destinationFS)
                        if (!foundexisting) {
                            AnnotationIndex<AnnotationFS> annoIndex = aDestFS.getCAS()
                                    .getAnnotationIndex(refFS.getType());
                            FSIterator<AnnotationFS> it = annoIndex.iterator();
                            while (it.hasNext()) {
                                AnnotationFS fs = it.next();
                                // TODO Caution: the following check cannot identify
                                // reference targets if their span has changed
                                // during transformation, eg. due to some
                                // TSurgeon-operation
                                // We cannot compare annotation-ids here, because
                                // the we are dealing with different instances
                                if (fs.getBegin() == ((AnnotationFS) refFS).getBegin()
                                        && fs.getEnd() == ((AnnotationFS) refFS).getEnd()
                                        && fs.getView()
                                                .getViewName()
                                                .equals(((AnnotationFS) refFS).getView()
                                                        .getViewName())) {
                                    aDestFS.setFeatureValue(destFeat, fs);
                                    foundexisting = true;
                                }
                            }
                        }
                    }
                    // if referenced annotation has not been recreated and
                    // not been recovered from the standford tree nodes,
                    // copy annotation directly from the old CAS
                    if (!foundexisting) {
                        FeatureStructure copyRefFs = copyFs(refFS);
                        aDestFS.setFeatureValue(destFeat, copyRefFs);
                    }
                }
            }
        }
    }

    /**
     * Returns whether the given FS has already been copied using this CasCopier.
     * 
     * @param aFS
     *            a feature structure.
     * @return if the FS has already been copied.
     */
    public boolean alreadyCopied(FeatureStructure aFS)
    {
        return mFsMap.containsKey(aFS);
    }

    private FeatureStructure copyArray(FeatureStructure aSrcFs)
    {
        // TODO: there should be a way to do this without enumerating all the
        // array types!
        if (aSrcFs instanceof StringArrayFS) {
            StringArrayFS arrayFs = (StringArrayFS) aSrcFs;
            int len = arrayFs.size();
            StringArrayFS destFS = mDestCas.createStringArrayFS(len);
            for (int i = 0; i < len; i++) {
                destFS.set(i, arrayFs.get(i));
            }
            return destFS;
        }
        if (aSrcFs instanceof IntArrayFS) {
            IntArrayFS arrayFs = (IntArrayFS) aSrcFs;
            int len = arrayFs.size();
            IntArrayFS destFS = mDestCas.createIntArrayFS(len);
            for (int i = 0; i < len; i++) {
                destFS.set(i, arrayFs.get(i));
            }
            return destFS;
        }
        if (aSrcFs instanceof ByteArrayFS) {
            ByteArrayFS arrayFs = (ByteArrayFS) aSrcFs;
            int len = arrayFs.size();
            ByteArrayFS destFS = mDestCas.createByteArrayFS(len);
            for (int i = 0; i < len; i++) {
                destFS.set(i, arrayFs.get(i));
            }
            return destFS;
        }
        if (aSrcFs instanceof ShortArrayFS) {
            ShortArrayFS arrayFs = (ShortArrayFS) aSrcFs;
            int len = arrayFs.size();
            ShortArrayFS destFS = mDestCas.createShortArrayFS(len);
            for (int i = 0; i < len; i++) {
                destFS.set(i, arrayFs.get(i));
            }
            return destFS;
        }
        if (aSrcFs instanceof LongArrayFS) {
            LongArrayFS arrayFs = (LongArrayFS) aSrcFs;
            int len = arrayFs.size();
            LongArrayFS destFS = mDestCas.createLongArrayFS(len);
            for (int i = 0; i < len; i++) {
                destFS.set(i, arrayFs.get(i));
            }
            return destFS;
        }
        if (aSrcFs instanceof FloatArrayFS) {
            FloatArrayFS arrayFs = (FloatArrayFS) aSrcFs;
            int len = arrayFs.size();
            FloatArrayFS destFS = mDestCas.createFloatArrayFS(len);
            for (int i = 0; i < len; i++) {
                destFS.set(i, arrayFs.get(i));
            }
            return destFS;
        }
        if (aSrcFs instanceof DoubleArrayFS) {
            DoubleArrayFS arrayFs = (DoubleArrayFS) aSrcFs;
            int len = arrayFs.size();
            DoubleArrayFS destFS = mDestCas.createDoubleArrayFS(len);
            for (int i = 0; i < len; i++) {
                destFS.set(i, arrayFs.get(i));
            }
            return destFS;
        }
        if (aSrcFs instanceof BooleanArrayFS) {
            BooleanArrayFS arrayFs = (BooleanArrayFS) aSrcFs;
            int len = arrayFs.size();
            BooleanArrayFS destFS = mDestCas.createBooleanArrayFS(len);
            for (int i = 0; i < len; i++) {
                destFS.set(i, arrayFs.get(i));
            }
            return destFS;
        }
        if (aSrcFs instanceof ArrayFS) {
            ArrayFS arrayFs = (ArrayFS) aSrcFs;
            int len = arrayFs.size();
            ArrayFS destFS = mDestCas.createArrayFS(len);
            for (int i = 0; i < len; i++) {
                FeatureStructure srcElem = arrayFs.get(i);
                if (srcElem != null) {
                    FeatureStructure copyElem = copyFs(arrayFs.get(i));
                    destFS.set(i, copyElem);
                }
            }
            return destFS;
        }
        assert false; // the set of array types should be exhaustive, so we
        // should never get here
        return null;
    }

    /**
     * Gets the named view; if the view doesn't exist it will be created.
     */
    private static CAS getOrCreateView(CAS aCas, String aViewName)
    {
        // TODO: there should be some way to do this without the try...catch
        try {
            return aCas.getView(aViewName);
        }
        catch (CASRuntimeException e) {
            // create the view
            return aCas.createView(aViewName);
        }
    }

    /**
     * Determines whether the given FS is the DocumentAnnotation for its view. This is more than
     * just a type check; we actually check if it is the one "special" DocumentAnnotation that
     * CAS.getDocumentAnnotation() would return.
     */
    private static boolean isDocumentAnnotation(FeatureStructure aFS)
    {
        return (aFS instanceof AnnotationFS)
                && aFS.equals(((AnnotationFS) aFS).getView().getDocumentAnnotation());
    }

    /**
     * Performs batch-copying of Annotations (could also be generalized to FeatureStructures)
     *
     * While copying the annotations, the whole batch is held in a class attribute. This way, we can
     * cope with referenced annotations that have not been copied/recovered yet.
     *
     * @param annoList
     *            the list of annotations that is to be batch-copied
     * @return the list of copied annotations
     */
    public List<Annotation> batchCopyAnnotations(List<Annotation> annoList)
    {

        batchCopyAnnoList = annoList;
        List<Annotation> returnList = new ArrayList<Annotation>();

        for (Annotation anno : batchCopyAnnoList) {
            returnList.add((Annotation) copyFs(anno));
        }

        // reset list
        batchCopyAnnoList = null;

        return returnList;

    }
}
