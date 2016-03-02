/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.castransformation.internal;

import java.util.Iterator;

import org.apache.uima.UIMARuntimeException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.SofaFS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSIndexRepositoryImpl;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.FeatureStructureImpl;
import org.apache.uima.cas.impl.LowLevelIterator;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.internal.util.Int2IntHashMap;
import org.apache.uima.internal.util.IntVector;
import org.apache.uima.internal.util.PositiveIntSet;
import org.apache.uima.internal.util.PositiveIntSet_impl;

/**
 * Utility class for doing deep copies of FeatureStructures from one CAS to another. To handle cases
 * where the source CAS has multiple references to the same FS, you can create one instance of
 * CasCopier and use it to copy multiple FeatureStructures. The CasCopier will remember previously
 * copied FeatureStructures, so if you later copy another FS that has a reference to a previously
 * copied FS, it will not duplicate the multiply-referenced FS.
 * 
 * This class makes use of CASImpl methods, but is only passed CAS objects, which may be 
 * CAS Wrappers.  To make this more feasible, the implementors of CAS Wrappers need to implement
 * the method  getLowLevelCas() which should return a reference to the underlying CAS which can be 
 * successfully cast to a CASImpl.
 * 
 * The source and target CASs must be separate CASs (that is, not two views of the same CAS), with
 * one exception:
 * 
 *   If the CopyCasView API is being used, and the target View name is different from the source view name,
 *   
 * 
 */
public class CasCopier {
  
  private static final int FRC_SKIP = 0;  // is the default, must be 0
  private static final int FRC_STRING = 1;
  private static final int FRC_LONG = 2;
  private static final int FRC_DOUBLE = 3;
  private static final int FRC_INT_LIKE = 4;
  private static final int FRC_REF = 5;
  
  private static final int K_SRC_FEAT_OFFSET = 0;
  private static final int K_TGT_FEAT_CODE = 1;
  
  private class TypeInfo {
    final int[] codesAndOffsets;  // indexed with count * 2
    final byte[] frc;
    final int tgtTypeCode;
        
    TypeInfo(int srcTypeCode) {    

      if (tgtTsi == srcTsi) {
        tgtTypeCode = srcTypeCode;
      } else {
        Type srcType = srcTsi.ll_getTypeForCode(srcTypeCode);
        Type tgtType = tgtTsi.getType(srcType.getName());
        if (tgtType == null) {
          // If in lenient mode, do not act on this FS. Instead just
          // return (null) to the caller and let the caller deal with this case.
          if (lenient) {
            tgtTypeCode = 0;
          } else {
            throw new UIMARuntimeException(UIMARuntimeException.TYPE_NOT_FOUND_DURING_CAS_COPY,
                new Object[] { srcType.getName() });
          }
        } else {
          tgtTypeCode = tgtTsi.ll_getCodeForType(tgtType);
        }
      }
                  
      int[] srcFeatCodes = srcTsi.ll_getAppropriateFeatures(srcTypeCode);
      int arrayLength = srcFeatCodes.length << 1;
      
      codesAndOffsets = new int[arrayLength];
      frc = new byte[srcFeatCodes.length];

      if (srcTsi == tgtTsi) {
        for (int i = 0; i < srcFeatCodes.length; i++) {
          final int srcFeatCode = srcFeatCodes[i];
          Feature srcFeat = srcTsi.ll_getFeatureForCode(srcFeatCode);
          setRangeClass((TypeImpl) srcFeat.getRange(), i);
          final int i2 = i << 1;
          codesAndOffsets[i2 + K_SRC_FEAT_OFFSET] = originalSrcCasImpl.getFeatureOffset(srcFeatCode);
          codesAndOffsets[i2 + K_TGT_FEAT_CODE] = srcFeatCodes[i];
        }
      } else {        
        for (int i = 0; i < srcFeatCodes.length; i++) { 
         final int srcFeatCode = srcFeatCodes[i];
         Feature srcFeat = srcTsi.ll_getFeatureForCode(srcFeatCode);
          String srcFeatName = srcFeat.getName();
          Feature tgtFeat = tgtTsi.getFeatureByFullName(srcFeatName);
          if (tgtFeat == null) {
            // If in lenient mode, ignore this feature and move on to the next
            // feature in this FS (if one exists)
            if (lenient) {
              continue; // Ignore this feature in the source CAS since it doesn't exist in
                        // in the target CAS.
            } else {
              throw new UIMARuntimeException(UIMARuntimeException.FEATURE_NOT_FOUND_DURING_CAS_COPY,
                  new Object[] { srcFeatName });
            }
          } else {
            final int i2 = i << 1;
            int tgtFeatCode = ((FeatureImpl)tgtFeat).getCode();
            codesAndOffsets[i2 + K_SRC_FEAT_OFFSET] = originalSrcCasImpl.getFeatureOffset(srcFeatCode);
            codesAndOffsets[i2 + K_TGT_FEAT_CODE] = tgtFeatCode;
          }

          TypeImpl srcRangeType = (TypeImpl) srcFeat.getRange();
          
          // verify range types of features have the same name
          if (!srcRangeType.getName().equals(
               tgtFeat.getRange().getName())) {
            throw new UIMARuntimeException(UIMARuntimeException.COPY_CAS_RANGE_TYPE_NAMES_NOT_EQUAL, 
                new Object[] {srcFeatName, srcFeat.getRange().getName(), tgtFeat.getRange().getName()});
          }
          
          setRangeClass(srcRangeType, i);
        }
      }        
    }
    
    void setRangeClass(TypeImpl srcRangeType, int i) {
      if (srcTsi.ll_subsumes(srcStringTypeCode, srcRangeType.getCode())) {
        frc[i] = FRC_STRING;
      } else if (srcRangeType == srcTsi.intType || 
          srcRangeType == srcTsi.floatType ||
          srcRangeType == srcTsi.booleanType ||
          srcRangeType == srcTsi.byteType ||
          srcRangeType == srcTsi.shortType) {
        frc[i] = FRC_INT_LIKE;
      } else if (srcRangeType == srcTsi.longType) {
        frc[i] = FRC_LONG;
      } else if (srcRangeType == srcTsi.doubleType) {
        frc[i] = FRC_DOUBLE;
      } else {
        frc[i] = FRC_REF;
      }
    }
  }
    
  private final TypeInfo[] tInfoArray;
  
  // these next are called original, as they are the views used to create the CasCopier instance
  private final CAS originalSrcCas;
  private final CAS originalTgtCas;
  // these next are the CASImpls of these
  private final CASImpl originalSrcCasImpl;
  private final CASImpl originalTgtCasImpl;
  
  // these next 2 are like the above, but for explicit view copying
  
  private CASImpl srcCasViewImpl;
  private CASImpl tgtCasViewImpl;
  
  private String srcViewName;  // these are used when the view name is changed
  private String tgtViewName;  // this is the corresponding target view name for the source view name
  
  private final TypeSystemImpl srcTsi;
  private final TypeSystemImpl tgtTsi;
  
  private final TypeImpl srcStringType;
  private final int srcStringTypeCode;
  
  /**
   * true if the copyCasView api was used, and the target view name corresponding to the source view name is changed
   */
  private boolean isChangeViewName = false;
  
  private int srcCasDocumentAnnotation = 0;
//  /**
//   * The source view name - may be null if the view is of the base CAS
//   */
//  private String mSrcCasViewName;
//  /**
//   * The target view name - not used unless doing a view copy 
//   * Allows copying a view to another CAS under a different name
//   */
//  private String mTgtCasViewName;
  
  final private Feature mDestSofaFeature;
  final private int mDestSofaFeatureCode;
  final private int srcSofaTypeCode;
  
  final private boolean lenient; //true: ignore feature structures and features that are not defined in the destination CAS

  /**
   * key is source FS, value is target FS 
   * Target not set for DocumentAnnotation or SofaFSs
   * Target not set if lenient specified and src type isn't in target
   */
  final private Int2IntHashMap mFsMap = new Int2IntHashMap();
  
  /**
   * feature structures whose slots need copying are put on this list, together with their source
   * as pairs of ints.
   *   First int is the target Cas ref
   *   Second int is the source Cas ref
   * List is operated as a stack, from the end, for efficiency
   */
  private IntVector fsToDo = new IntVector();


  /**
   * Creates a new CasCopier that can be used to copy FeatureStructures from one CAS to another.
   * Note that if you are merging data from multiple CASes, you must create a new CasCopier
   * for each source CAS.
   * 
   * Note: If the feature structure and/or feature is not defined in the type system of
   *       the destination CAS, the copy will fail (in other words, the lenient setting is false,
   *       by default).
   *       
   * @param aSrcCas
   *          the CAS to copy from.
   * @param aDestCas
   *          the CAS to copy into.
   */
  public CasCopier(CAS aSrcCas, CAS aDestCas) {
    this(aSrcCas, aDestCas, false);
  }

  /**
   * Creates a new CasCopier that can be used to copy FeatureStructures from one CAS to another.
   * Note that if you are merging data from multiple CASes, you must create a new CasCopier
   * for each source CAS. This version of the constructor supports a "lenient copy" option. When set,
   * the CAS copy function will ignore (not attempt to copy) FSs and features not defined in the type system
   * of the destination CAS, rather than throwing an exception.
   * 
   * @param aSrcCas
   *          the CAS to copy from.
   * @param aDestCas
   *          the CAS to copy into.
   * @param lenient
   *          ignore FSs and features not defined in the type system of the destination CAS
   */
  public CasCopier(CAS aSrcCas, CAS aDestCas, boolean lenient) {

    originalSrcCas = aSrcCas;
    originalTgtCas = aDestCas;
    
    originalSrcCasImpl = (CASImpl) aSrcCas.getLowLevelCAS(); 
    originalTgtCasImpl = (CASImpl) aDestCas.getLowLevelCAS(); 
    
    srcTsi = originalSrcCasImpl.getTypeSystemImpl();
    tgtTsi = originalTgtCasImpl.getTypeSystemImpl();
    
    tInfoArray = new TypeInfo[srcTsi.getLargestTypeCode() + 1];
    
    srcStringType = srcTsi.stringType;
    srcStringTypeCode = srcStringType.getCode();
    
    mDestSofaFeature = aDestCas.getTypeSystem().getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);
    mDestSofaFeatureCode = ((FeatureImpl)mDestSofaFeature).getCode();
    srcSofaTypeCode = originalSrcCasImpl.getTypeSystemImpl().sofaType.getCode();
    this.lenient = lenient;
    
    // the next is to support the style of use where
    //   an instance of this copier is made, corresponding to two views in the same CAS
    //   or corresponding to two views in different CASs
    //   and then individual FeatureStructures are copied using copyFS(...)
    
    srcCasViewImpl = (CASImpl) originalSrcCas.getLowLevelCAS();
    tgtCasViewImpl = (CASImpl) originalTgtCas.getLowLevelCAS();
    
    srcViewName = srcCasViewImpl.getViewName();
    tgtViewName = tgtCasViewImpl.getViewName();
    
    if (srcViewName == null) {
      isChangeViewName = (tgtViewName == null) ? false : true;
    } else {
      isChangeViewName = !srcViewName.equals(tgtViewName);
    }
  }
  

  
  /**
   * Does a complete deep copy of one CAS into another CAS.  The contents of each view
   * in the source CAS will be copied to the same-named view in the destination CAS.  If
   * the view does not already exist it will be created.  All FeatureStructures that are
   * indexed in a view in the source CAS will become indexed in the same-named view in the
   * destination CAS.
   * 
   * Note: If the feature structure and/or feature is not defined in the type system of
   *       the destination CAS, the copy will fail (in other words, the lenient setting is false,
   *       by default).
   *
   * @param aSrcCas
   *          the CAS to copy from
   * @param aDestCas
   *          the CAS to copy to
   * @param aCopySofa
   *          if true, the sofa data and mimeType of each view will be copied. If false they will not.
   */  
  public static void copyCas(CAS aSrcCas, CAS aDestCas, boolean aCopySofa) {
   copyCas(aSrcCas, aDestCas, aCopySofa, false);
  }

  /**
   * Does a complete deep copy of one CAS into another CAS.  The contents of each view
   * in the source CAS will be copied to the same-named view in the destination CAS.  If
   * the view does not already exist it will be created.  All FeatureStructures that are
   * indexed in a view in the source CAS will become indexed in the same-named view in the
   * destination CAS. This version of the method supports a "lenient copy" option. When set,
   * the CAS copy function will ignore (not attempt to copy) FSs and features not defined in the type system
   * of the destination CAS, rather than throwing an exception.
   *
   * @param aSrcCas
   *          the CAS to copy from
   * @param aDestCas
   *          the CAS to copy to; must be a completely different CAS than the source (that is, not an alternative "view" of the source)
   * @param aCopySofa
   *          if true, the sofa data and mimeType of each view will be copied. If false they will not.
   * @param lenient
   *          ignore FSs and features not defined in the type system of the destination CAS
   */
  public static void copyCas(CAS aSrcCas, CAS aDestCas, boolean aCopySofa, boolean lenient) {
    CasCopier copier = new CasCopier(aSrcCas, aDestCas, lenient);
    
    // oops, this misses the initial view if a sofa FS has not yet been created
//    Iterator<SofaFS> sofaIter = aSrcCas.getSofaIterator();
//    while (sofaIter.hasNext()) {
//      SofaFS sofa = sofaIter.next();
//      CAS view = aSrcCas.getView(sofa);
//      copier.copyCasView(view, aCopySofa);
//    }
    
    if (copier.originalSrcCasImpl.getBaseCAS() == copier.originalTgtCasImpl.getBaseCAS()) {
      throw new UIMARuntimeException(UIMARuntimeException.ILLEGAL_CAS_COPY_TO_SAME_CAS, null);
    }
    
    Iterator<CAS> viewIterator = aSrcCas.getViewIterator();
    while (viewIterator.hasNext()) {
      CAS view = viewIterator.next();
      copier.copyCasView(view, aCopySofa); 

    }
  }

  /**
   * Does a deep copy of the contents of one CAS View into another CAS's same-named-view
   * If the destination view already exists in the destination CAS,
   * then it will be the target of the copy.  Otherwise, a new view will be created with
   * that name and will become the target of the copy.  All FeatureStructures 
   * (except for those dropped because the target type system doesn't have the needed type) that are indexed 
   * in the source CAS view will become indexed in the target view.
   * Cross-view references may result in creating additional views in the destination CAS;
   * for these views, any Sofa data in the source is *not* copied.
   * 
   * @param aSrcCasView the CAS to copy from.  This must be a view in the src Cas set by the constructor
   * @param aCopySofa if true, the sofa data and mimeType will be copied. If false they will not.
   */
  public void copyCasView(CAS aSrcCasView, boolean aCopySofa) {
    copyCasViewDifferentCASs(aSrcCasView, getOrCreateView(originalTgtCas, aSrcCasView.getViewName()), aCopySofa);
  }
  
  /**
   * Does a deep copy of the contents of one CAS View into another CAS's same-named-view
   * If the destination view already exists in the destination CAS,
   * then it will be the target of the copy.  Otherwise, a new view will be created with
   * that name and will become the target of the copy.  All FeatureStructures 
   * (except for those dropped because the target type system doesn't have the needed type) that are indexed 
   * in the source CAS view will become indexed in the target view.
   * Cross-view references may result in creating additional views in the destination CAS;
   * for these views, any Sofa data in the source is *not* copied.  Any views created because
   * of cross-view references will have the same view name as in the source.
   *
   * @param aSrcCasViewName the name of the view in the source CAS to copy from
   * @param aCopySofa if true, the sofa data and mimeType will be copied. If false they will not.
   */
  public void copyCasView(String aSrcCasViewName, boolean aCopySofa) {
    copyCasView(getOrCreateView(originalSrcCas, aSrcCasViewName), aCopySofa);
  }
  
  /**
   * Does a deep copy of the contents of one CAS View into another CAS view,
   * with a possibly different name.
   * If the destination view already exists in the destination CAS,
   * then it will be the target of the copy.  Otherwise, a new view will be created with
   * that name and will become the target of the copy.  
   * All FeatureStructures 
   * (except for those dropped because the target type system doesn't have the needed type) that are indexed 
   * in the source CAS view will become indexed in the target view.
   * Cross-view references may result in creating additional views in the destination CAS;
   * for these views, any Sofa data in the source is *not* copied.  Any views created because
   * of cross-view references will have the same view name as in the source.
   * 
   * @param aSrcCasView The view in the source to copy from
   * @param aTgtCasViewName The name of the view in the destination CAS to copy into
   * @param aCopySofa if true, the sofa data and mimeType will be copied. If false they will not.
   */
  public void copyCasView(CAS aSrcCasView, String aTgtCasViewName, boolean aCopySofa) {
    copyCasView(aSrcCasView, getOrCreateView(originalTgtCas, aTgtCasViewName), aCopySofa);
  }

  /**
   * Does a deep copy of the contents of one CAS View into another CAS view,
   * with a possibly different name.
   * All FeatureStructures 
   * (except for those dropped because the target type system doesn't have the needed type) that are indexed 
   * in the source CAS view will become indexed in the target view.
   * Cross-view references may result in creating additional views in the destination CAS;
   * for these views, any Sofa data in the source is *not* copied.  Any views created because
   * of cross-view references will have the same view name as in the source.
   * 
   * @param aSrcCasViewName The name of the view in the Source CAS to copy from
   * @param aTgtCasView The view in the destination CAS to copy into
   * @param aCopySofa if true, the sofa data and mimeType will be copied. If false they will not.
   */
  public void copyCasView(String aSrcCasViewName, CAS aTgtCasView, boolean aCopySofa) {
    copyCasView(getOrCreateView(originalSrcCas, aSrcCasViewName), aTgtCasView, aCopySofa);
  }

  private void copyCasViewDifferentCASs(CAS aSrcCasView, CAS aTgtCasView, boolean aCopySofa) {
    if (originalSrcCasImpl.getBaseCAS() == originalTgtCasImpl.getBaseCAS()) {
      throw new UIMARuntimeException(UIMARuntimeException.ILLEGAL_CAS_COPY_TO_SAME_CAS, null);
    }

    copyCasView(aSrcCasView, aTgtCasView, aCopySofa);
  }
  
  /**
   * Does a deep copy of the contents of one CAS View into another CAS view,
   * with a possibly different name.
   * All FeatureStructures 
   * (except for those dropped because the target type system doesn't have the needed type) that are indexed 
   * in the source CAS view will become indexed in the target view.
   * Cross-view references may result in creating additional views in the destination CAS;
   * for these views, any Sofa data in the source is *not* copied.  Any views created because
   * of cross-view references will have the same view name as in the source.
   * 
   * If the source and target views are both views of the same CAS, then Feature Structures
   * in the view are effectively "cloned", with the following change:
   *   Subtypes of AnnotationBase in the source whose sofaRef is for the source View are
   *   cloned with their sofaRefs changed to the new targetView. 
   * 
   * @param aSrcCasView
   *          the CAS view to copy from. This must be a view of the srcCas set in the constructor
   * @param aTgtCasView 
   *          the CAS view to copy to. This must be a view of the tgtCas set in the constructor
   * @param aCopySofa
   *          if true, the sofa data and mimeType will be copied. If false they will not.  
   *          If true and the sofa data is already set in the target, will throw CASRuntimeException        
   */
  public void copyCasView(CAS aSrcCasView, CAS aTgtCasView, boolean aCopySofa) {
        
    if (!casViewsInSameCas(aSrcCasView, originalSrcCas)) {
      throw new UIMARuntimeException(UIMARuntimeException.VIEW_NOT_PART_OF_CAS, new Object[] {"Source"});
    }
    if (!casViewsInSameCas(aTgtCasView, originalTgtCas)) {
      throw new UIMARuntimeException(UIMARuntimeException.VIEW_NOT_PART_OF_CAS, new Object[] {"Destination"});
    }
    
//    mSrcCasViewName = aSrcCasView.getViewName(); 
//    mTgtCasViewName = aTgtCasView.getViewName();

    srcCasViewImpl = (CASImpl) aSrcCasView.getLowLevelCAS();
    tgtCasViewImpl = (CASImpl) aTgtCasView.getLowLevelCAS();
    
    srcViewName = srcCasViewImpl.getViewName();
    tgtViewName = tgtCasViewImpl.getViewName();
    isChangeViewName = !srcViewName.equals(tgtViewName);

    if ((aSrcCasView == srcCasViewImpl.getBaseCAS()) || (aTgtCasView == tgtCasViewImpl.getBaseCAS())) {
      throw new UIMARuntimeException(UIMARuntimeException.UNSUPPORTED_CAS_COPY_TO_OR_FROM_BASE_CAS, null);
    }
    
    srcCasDocumentAnnotation = 0;  // each view needs to get this once
    
//    mLowLevelDestCas = aTgtCasView.getLowLevelCAS();
//    mLowLevelSrcCas = aSrcCasView.getLowLevelCAS();
    
    // The top level sofa associated with this view is copied (or not)
    
    if (aCopySofa) {
      // can't copy the SofaFS - just copy the sofa data and mime type
      SofaFS sofa = srcCasViewImpl.getSofa();
      if (null != sofa) {
        // if the sofa doesn't exist in the target, these calls will create it
        //  (view can exist without Sofa, at least for the initial view)
        String sofaMime = sofa.getSofaMime();
        if (srcCasViewImpl.getDocumentText() != null) {
          aTgtCasView.setSofaDataString(srcCasViewImpl.getDocumentText(), sofaMime);
        } else if (srcCasViewImpl.getSofaDataURI() != null) {
          aTgtCasView.setSofaDataURI(srcCasViewImpl.getSofaDataURI(), sofaMime);
        } else if (srcCasViewImpl.getSofaDataArray() != null) {
          aTgtCasView.setSofaDataArray(copyFs2Fs(srcCasViewImpl.getSofaDataArray()), sofaMime);
        }
      }
    }

    // now copy indexed FS, but keep track so we don't index anything more than once
    //   Note: mFsMap might be used for this, but it doesn't index several kinds of FSs
    //         see the javadoc for this field for details
    // NOTE: FeatureStructure hashcode / equals use the int "address" of the FS in the heap.
    
    final PositiveIntSet indexedFs = new PositiveIntSet_impl();
    
    // The indexFs set starts out "cleared", but 
    // we don't clear the cas copier instance map "mFsMap" here, in order to skip actually copying the
    //   FSs when doing a full CAS copy with multiple views - the 2nd and subsequent
    //   views don't copy, but they do index.
    
    LowLevelIterator it = ((FSIndexRepositoryImpl)(srcCasViewImpl.getIndexRepository())).ll_getAllIndexedFS(srcTsi.getTopType());

    while (it.isValid()) {
      final int fs = it.ll_get();
      it.moveToNext();
//    Iterator<LowLevelIndex> indexes = srcCasViewImpl.getIndexRepository().ll_getIndexes();
//    while (indexes.hasNext()) {
//      LowLevelIndex index = indexes.next();
//      LowLevelIterator iter = index.ll_iterator();
//      while (iter.isValid()) {
//        final int fs = iter.ll_get();
//        iter.moveToNext();
      if (!indexedFs.contains(fs)) {
        final int copyOfFs = copyFs2(fs);
        // If the lenient option is used, it's possible that no FS was
        // created (e.g., FS is not defined in the target CAS. So ignore
        // this FS in the source CAS and move on to the next FS.
        if (lenient && copyOfFs == 0) {
          continue; // Move to the next FS in the source CAS
        }
        // otherwise, won't be null (error thrown instead)

        // check for annotations with null Sofa reference - this can happen
        // if the annotations were created with the Low Level CAS API. If the
        // Sofa reference isn't set, attempting to add the FS to the indexes
        // will fail.
        if (originalSrcCasImpl.isSubtypeOfAnnotationBaseType(originalSrcCasImpl.getTypeCode(fs))) {
          int sofaRef = tgtCasViewImpl.ll_getRefValue(copyOfFs, mDestSofaFeatureCode);
          if (0 == sofaRef) {
            tgtCasViewImpl.ll_setRefValue(copyOfFs, mDestSofaFeatureCode, tgtCasViewImpl.getSofaRef());
          }
        }

        // also don't index the DocumentAnnotation (it's indexed by default)
        if (!isDocumentAnnotation(fs)) {
          tgtCasViewImpl.ll_getIndexRepository().ll_addFS(copyOfFs);
        }
        indexedFs.add(fs);
      }
    }
  }

  /**
   * For long lists, and other structures, the straight-forward impl with recursion can
   * nest too deep, causing a Java failure - out of stack space.
   * 
   * This is a non-recursive impl, making use of an aux object: featureStructuresWithSlotsToSet to
   * hold copied FSs whose slots need to be scanned and set with values.
   * 
   * The main loop dequeues one element, and copies the features.
   * 
   * The copying of a FS copies the FS without setting the slots; instead it queues the
   * copied FS together with its source instance on featureStructuresWithSlotsToSet 
   * for later processing.
   * 
   */
  
  /**
   * Copy 1 feature structure from the originalSrcCas to a new Cas.  No indexing of the new FS is done.
   * If the FS has been copied previously (using this CasCopier instance) the 
   * same identical copy will be returned rather than making another copy.
   * 
   * View handling: ignores the view of the targetCas
   * 
   * @param aFS the Feature Structure to copy
   * @return a deep copy of the Feature Structure - any referred to FSs will also be copied.
   */
  
  public FeatureStructure copyFs(FeatureStructure aFS) {
    if (null == srcCasViewImpl) {
      srcCasViewImpl = originalSrcCasImpl;
    }
    if (null == tgtCasViewImpl) {
      tgtCasViewImpl = originalTgtCasImpl;
    }
    
    // safety - insure DocumentAnnotation is tested.
    srcCasDocumentAnnotation = 0;  
    return copyFs2Fs(aFS);
  }
  
  /**
   * Copy one FS from the src CAS to the tgt CAS
   *   View context:
   *     The caller must set the srcCasViewImpl and the tgtCasViewImpl
   *     
   * @param aFS a Feature Structure reference in the originalSrcCas
   * @return a Feature Structure reference in the originalTgtCas
   */
  private int copyFs2(int aFS) {
    
    int copy = copyFsInner(aFS);  // doesn't copy the slot values, but enqueues them
    while (fsToDo.size() > 0) {
      int copyToFillSlots = fsToDo.remove(fsToDo.size()-1);
      int srcToFillSlots = fsToDo.remove(fsToDo.size()-1);
      copyFeatures(srcToFillSlots, copyToFillSlots);   
    }
    return copy;
  }
  
  private FeatureStructure copyFs2Fs(FeatureStructure fs) {
    return originalTgtCasImpl.ll_getFSForRef(copyFs2(((FeatureStructureImpl)fs).getAddress()));
  }

  /**
   * Copies a FS from the source CAS to the destination CAS. Also copies any referenced FSs, except
   * that previously copied FS will not be copied again.
   * 
   * @param aFS
   *          the FS to copy. Must be contained within the source CAS.
   * @return the copy of <code>aFS</code> in the target CAS.
   */
  private int copyFsInner(int aFS) {
    // FS must be in the source CAS
    // this test must be done by the caller if wanted.
//    assert (casViewsInSameCas(aFS.getCAS(), originalSrcCas));

    // check if we already copied this FS
    int copy = mFsMap.get(aFS);
    if (copy != 0) {
        return copy;
    }

    // get the type of the FS
    final int srcTypeCode = originalSrcCasImpl.ll_getFSRefType(aFS);
    final Type srcType = srcTsi.ll_getTypeForCode(srcTypeCode);
    
    // Certain types need to be handled specially

    // Sofa - cannot be created by normal methods. Instead, we return the Sofa with the
    // same Sofa ID in the target CAS. If it does not exist it will be created.
    if (srcTypeCode == srcSofaTypeCode) {
      String destSofaId = getDestSofaId(srcCasViewImpl.ll_getSofaID(aFS));
      // note: not put into the mFsMap, because each view needs a separate copy
      return ((CASImpl)getOrCreateView(originalTgtCas, destSofaId)).getSofaRef();
    }

    // DocumentAnnotation - instead of creating a new instance, reuse the automatically created
    // instance in the destination view.
    if (isDocumentAnnotation(aFS)) {
      String destViewName = getDestSofaId(srcCasViewImpl.ll_getSofaID(srcCasViewImpl.getSofaFeat(aFS)));

      // the DocumentAnnotation could be indexed in a different view than the one being copied
      //   if it was ref'd for the 1st time from a cross-indexed fs
      // Note: The view might not exist in the target
      //   but this is unlikely.  To have this case this would require
      //   indexing some other feature structure in this view, which, in turn,
      //   has a reference to the DocumentAnnotation FS belonging to another view
      CASImpl destView = (CASImpl) getOrCreateView(originalTgtCas, destViewName);
      int destDocAnnot = destView.ll_getDocumentAnnotation();
      if (destDocAnnot == 0) {
        destDocAnnot = destView.ll_createDocumentAnnotationNoIndex(0, 0);
        copyFeatures(aFS, destDocAnnot);
        ((FSIndexRepositoryImpl)(destView.getIndexRepository())).addFS(destDocAnnot);
      } else {
        AutoCloseable ac = tgtCasViewImpl.protectIndexes();
        try {
          copyFeatures(aFS, destDocAnnot);
        } finally {
          try {
            ac.close();
          } catch (Exception e) {
          }
        }
      }
      // note note put into mFsMap, because each view needs a separate copy
      return destDocAnnot;
    }

    // Arrays - need to be created a populated differently than "normal" FS
    if (srcType.isArray()) {
      copy = copyArray(aFS);
      mFsMap.put(aFS, copy);
      return copy;
    }
    
    final TypeInfo tInfo = getTypeInfo(srcTypeCode);
    final int tgtTypeCode = tInfo.tgtTypeCode;
    if (tgtTypeCode == 0) {
      return 0; // not in target, no FS to create
    }
    // We need to use the LowLevel CAS interface to create the FS, because the usual
    // CAS.createFS() call doesn't allow us to create subtypes of AnnotationBase from
    // a base CAS. In any case we don't need the Sofa reference to be automatically
    // set because we'll set it manually when in the copyFeatures method.
    
    int tgtFsAddr = tgtCasViewImpl.ll_createFS(tgtTypeCode);

    // add to map so we don't try to copy this more than once
    mFsMap.put(aFS, tgtFsAddr);

    fsToDo.add(aFS); // order important
    fsToDo.add(tgtFsAddr);
    return tgtFsAddr;
  }
  
  /**
   * There are two cases for getting target sofa name from the source one, depending on whether or not
   * the API which allows specifying a different target view name for the source view name, is in use.
   * 
   * If so, then whenever the source sofa name is that src view name, replace it in the target with the 
   * specified different target view name.
   *     
   * @param id
   * @return id unless the id matches the source view name, and that name is being changed
   */
  private String getDestSofaId(String id) {
    return (isChangeViewName && id.equals(srcViewName)) ? tgtViewName : id;
  }
  
  private TypeInfo getTypeInfo(int srcTypeCode) {
    TypeInfo tInfo = tInfoArray[srcTypeCode];
    if (tInfo == null) {
      return tInfoArray[srcTypeCode] = new TypeInfo(srcTypeCode);
    }
    return tInfo;
  }
  
  /**
   * Copy feature values from one FS to another. For reference-valued features, this does a deep
   * copy.
   * 
   * @param srcFS
   *          FeatureStructure to copy from
   * @param tgtFS
   *          FeatureStructure to copy to, which must not be in the index (index corruption checks skipped)
   */
  private void copyFeatures(int srcFS, int tgtFS) {
    // set feature values
    
    final int srcTypeCode = srcCasViewImpl.getTypeCode(srcFS);
    
    final TypeInfo tInfo = getTypeInfo(srcTypeCode);
        
    tgtCasViewImpl.setCacheNotInIndex(tgtFS);
        
    for (int i = 0; i < tInfo.codesAndOffsets.length; i = i + 2) {
      final int tgtFeatCode = tInfo.codesAndOffsets[i + K_TGT_FEAT_CODE];
      if (0 == tgtFeatCode) {
        continue; 
      }
      final int srcFeatOffset = tInfo.codesAndOffsets[i + K_SRC_FEAT_OFFSET];
      switch (tInfo.frc[i >> 1]) {
      case FRC_SKIP:
        break;
      case FRC_STRING:
        // need feature code to check subtype constraints
        tgtCasViewImpl.ll_setStringValue(tgtFS, tgtFeatCode, srcCasViewImpl.ll_getStringValueFeatOffset(srcFS, srcFeatOffset));
        break;
      case FRC_INT_LIKE:
        tgtCasViewImpl.ll_setIntValue(tgtFS, tgtFeatCode, srcCasViewImpl.ll_getIntValueFeatOffset(srcFS, srcFeatOffset));
        break;
      case FRC_LONG:
        tgtCasViewImpl.ll_setLongValue(tgtFS,  tgtFeatCode,  srcCasViewImpl.ll_getLongValueFeatOffset(srcFS,  srcFeatOffset));
        break;
      case FRC_DOUBLE:
        tgtCasViewImpl.ll_setDoubleValue(tgtFS,  tgtFeatCode,  srcCasViewImpl.ll_getDoubleValueFeatOffset(srcFS,  srcFeatOffset));
        break;
      case FRC_REF:
        int refFS = srcCasViewImpl.ll_getRefValueFeatOffset(srcFS, srcFeatOffset);
        if (refFS != 0) {
          int copyRefFs = copyFsInner(refFS);
          tgtCasViewImpl.ll_setRefValue(tgtFS, tgtFeatCode, copyRefFs);
        }
        break;
      default:
        throw new UIMARuntimeException();  // internal error
      }      
    }
  }
  
  /**
   * Note: if lenient is in effect, this method will return false for
   * FSs which are not copied because the target doesn't have that type.
   * It also returns false for sofa FSs and the documentAnnotation FS.
   * @param aFS a feature structure
   * @return true if the given FS has already been copied using this CasCopier.
   */
  public boolean alreadyCopied(FeatureStructure aFS) {
    return alreadyCopied(((FeatureStructureImpl)aFS).getAddress());
  }
  
  /**
   * Note: if lenient is in effect, this method will return false for
   * FSs which are not copied because the target doesn't have that type.
   * It also returns false for sofa FSs and the documentAnnotation FS.
   * @param aFS a feature structure
   * @return true if the given FS has already been copied using this CasCopier.
   */
  public boolean alreadyCopied(int aFS) {
    return mFsMap.get(aFS) != 0;
  }

  /**
   * @param arrayFS
   * @return a copy of the array
   */
  private int copyArray(int srcFS) {
    // TODO: there should be a way to do this without enumerating all the array types!
    
    final int len = srcCasViewImpl.ll_getArraySize(srcFS);
    
    int srcTypeCode = srcCasViewImpl.getTypeCode(srcFS);
    
    if (srcTypeCode == TypeSystemImpl.stringArrayTypeCode) {
      final int tgtFS = tgtCasViewImpl.ll_createArray(TypeSystemImpl.stringArrayTypeCode, len);
      for (int i = 0; i < len; i++) {
        tgtCasViewImpl.ll_setStringArrayValue(tgtFS, i, srcCasViewImpl.ll_getStringArrayValue(srcFS, i));
      }
      return tgtFS;
    }
    
    if (srcTypeCode == TypeSystemImpl.intArrayTypeCode) {     
      final int tgtFS = tgtCasViewImpl.ll_createArray(TypeSystemImpl.intArrayTypeCode, len);
      for (int i = 0; i < len; i++) {
        tgtCasViewImpl.ll_setIntArrayValue(tgtFS, i,  srcCasViewImpl.ll_getIntArrayValue(srcFS,  i));
      }
      return tgtFS;
    }

    if (srcTypeCode == TypeSystemImpl.floatArrayTypeCode) {     
      final int tgtFS = tgtCasViewImpl.ll_createArray(TypeSystemImpl.floatArrayTypeCode, len);
      for (int i = 0; i < len; i++) {
        tgtCasViewImpl.ll_setFloatArrayValue(tgtFS, i,  srcCasViewImpl.ll_getFloatArrayValue(srcFS, i));
      }
      return tgtFS;
    }

    if (srcTypeCode == TypeSystemImpl.fsArrayTypeCode) {     
      final int tgtFS = tgtCasViewImpl.ll_createArray(TypeSystemImpl.fsArrayTypeCode, len);
      for (int i = 0; i < len; i++) {
        int srcItem = srcCasViewImpl.ll_getRefArrayValue(srcFS, i);
        tgtCasViewImpl.ll_setRefArrayValue(tgtFS, i, (srcItem == 0) ? 0 : copyFsInner(srcItem));
      }
      return tgtFS;
    }

    if (srcTypeCode == TypeSystemImpl.byteArrayTypeCode) {
      final int tgtFS = tgtCasViewImpl.ll_createByteArray(len);
      for (int i = 0; i < len; i++) {
        tgtCasViewImpl.ll_setByteArrayValue(tgtFS,  i, srcCasViewImpl.ll_getByteArrayValue(srcFS, i));
      }
      return tgtFS;      
    }
    
    if (srcTypeCode == TypeSystemImpl.shortArrayTypeCode) {
      final int tgtFS = tgtCasViewImpl.ll_createShortArray(len);
      for (int i = 0; i < len; i++) {
        tgtCasViewImpl.ll_setShortArrayValue(tgtFS,  i, srcCasViewImpl.ll_getShortArrayValue(srcFS, i));
      }
      return tgtFS;      
    }

    if (srcTypeCode == TypeSystemImpl.longArrayTypeCode) {
      final int tgtFS = tgtCasViewImpl.ll_createLongArray(len);
      for (int i = 0; i < len; i++) {
        tgtCasViewImpl.ll_setLongArrayValue(tgtFS,  i, srcCasViewImpl.ll_getLongArrayValue(srcFS, i));
      }
      return tgtFS;      
    }
    
    if (srcTypeCode == TypeSystemImpl.doubleArrayTypeCode) {
      final int tgtFS = tgtCasViewImpl.ll_createDoubleArray(len);
      for (int i = 0; i < len; i++) {
        tgtCasViewImpl.ll_setDoubleArrayValue(tgtFS,  i, srcCasViewImpl.ll_getDoubleArrayValue(srcFS, i));
      }
      return tgtFS;      
    }

    if (srcTypeCode == TypeSystemImpl.booleanArrayTypeCode) {
      final int tgtFS = tgtCasViewImpl.ll_createBooleanArray(len);
      for (int i = 0; i < len; i++) {
        tgtCasViewImpl.ll_setBooleanArrayValue(tgtFS,  i, srcCasViewImpl.ll_getBooleanArrayValue(srcFS, i));
      }
      return tgtFS;      
    }
    
    assert false; // the set of array types should be exhaustive, so we should never get here
    return 0;
  }
  
  /**
   * Gets the named view; if the view doesn't exist it will be created.
   */
  private static CASImpl getOrCreateView(CAS aCas, String aViewName) {
    //TODO: there should be some way to do this without the try...catch
    try { // throws if view doesn't exist
      return (CASImpl) aCas.getView(aViewName).getLowLevelCAS(); 
    }
    catch(CASRuntimeException e) {
      //create the view
      return (CASImpl) aCas.createView(aViewName).getLowLevelCAS(); 
    }
  }  
  
  /**
   * Determines whether the given FS is the DocumentAnnotation in the srcCasView.  
   * This is more than just a type check; we actually check if it is the one "special"
   * DocumentAnnotation that CAS.getDocumentAnnotation() would return.
   */
  private boolean isDocumentAnnotation(int aFS) {
    if (srcCasDocumentAnnotation == 0) {
      int docFs = srcCasViewImpl.ll_getDocumentAnnotation();
      srcCasDocumentAnnotation = (docFs == 0) ? -1 : docFs; 
    }
    return aFS == srcCasDocumentAnnotation;
  }
  
  /**
   * Change from https://issues.apache.org/jira/browse/UIMA-3112 :
   *   requires that the CAS returned from the getLowLevelCAS() be castable to CASImpl
   * @param c1 -
   * @param c2 -
   * @return true if both views are in the same CAS (e.g., they have the same base CAS)
   */
  private boolean casViewsInSameCas(CAS c1, CAS c2) {
    if (null == c1 || null == c2) {
      return false;
    }

    CASImpl ci1 = (CASImpl) c1.getLowLevelCAS();
    CASImpl ci2 = (CASImpl) c2.getLowLevelCAS();
    
    return ci1.getBaseCAS() == ci2.getBaseCAS();
  }
  
  public FeatureStructure getCopy(FeatureStructure aFS) {
      int addr = mFsMap.get(originalSrcCas.getLowLevelCAS().ll_getFSRef(aFS));
      return addr != 0 ? originalTgtCas.getLowLevelCAS().ll_getFSForRef(addr) : null;
  }
  public int getCopy(int aFS) {
      return mFsMap.get(aFS);
  }
}
