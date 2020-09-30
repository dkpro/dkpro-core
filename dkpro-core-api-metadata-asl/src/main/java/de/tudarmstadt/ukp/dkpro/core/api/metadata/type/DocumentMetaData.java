/*
 * Copyright 2017
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
/* Apache UIMA v3 - First created by JCasGen Fri Sep 08 09:51:28 EEST 2017 */

package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.DocumentAnnotation;




/** <p>The DocumentMetaData annotation stores information about a single processed
document. There can only be one of these annotations per CAS. The annotation is
created by readers and contains information to uniquely identify the document from
which a CAS was created. Writer components use this information when determining
under which filename a CAS is stored.<p>

<p>There are two principle ways of identifying a document:<p>

<ul>
<li><b>collection id / document id:</b> this simple system identifies a document
  within a collection. The ID of the collection and the document are each
  simple strings without any further semantics such as e.g. a hierarchy. For
  this reason, this identification scheme is not well suited to preserve
  information about directory structures.</li>

<li><b>document base URI / document URI:</b> this system identifies a document using
  a URI. The base URI is used to derive the relative path of the document with
  respect to the base location from where it has been read. E.g. if the base
  URI is <code>file:/texts</code> and the document URI is <code>file:/texts/english/text1.txt</code>, then the relativ
  path of the document is <code>english/text1.txt</code>. This
  information is used by writers to recreate the directory structure found
  under the base location in the target location.</li>
</ul>

<p>It is possible and indeed common for a writer to initialize both systems of
identification. If both systems are present, most writers default to using the
URI-based systems. However, most writers also allow forcing the use of the ID-based
systems.</p>

<p>In addition to the features given here, there is a <i>language</i> feature inherited from UIMA's DocumentAnnotation. DKPro Core components expect a two letter ISO
639-1 language code there.</p>
 * Updated by JCasGen Fri Sep 08 09:51:28 EEST 2017
 * XML source: /Users/bluefire/git/dkpro-core/dkpro-core-api-metadata-asl/target/jcasgen/typesystem.xml
 * @generated */
public class DocumentMetaData extends DocumentAnnotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DocumentMetaData.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
 
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public final static String _FeatName_documentTitle = "documentTitle";
  public final static String _FeatName_documentId = "documentId";
  public final static String _FeatName_documentUri = "documentUri";
  public final static String _FeatName_collectionId = "collectionId";
  public final static String _FeatName_documentBaseUri = "documentBaseUri";
  public final static String _FeatName_isLastSegment = "isLastSegment";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_documentTitle = TypeSystemImpl.createCallSite(DocumentMetaData.class, "documentTitle");
  private final static MethodHandle _FH_documentTitle = _FC_documentTitle.dynamicInvoker();
  private final static CallSite _FC_documentId = TypeSystemImpl.createCallSite(DocumentMetaData.class, "documentId");
  private final static MethodHandle _FH_documentId = _FC_documentId.dynamicInvoker();
  private final static CallSite _FC_documentUri = TypeSystemImpl.createCallSite(DocumentMetaData.class, "documentUri");
  private final static MethodHandle _FH_documentUri = _FC_documentUri.dynamicInvoker();
  private final static CallSite _FC_collectionId = TypeSystemImpl.createCallSite(DocumentMetaData.class, "collectionId");
  private final static MethodHandle _FH_collectionId = _FC_collectionId.dynamicInvoker();
  private final static CallSite _FC_documentBaseUri = TypeSystemImpl.createCallSite(DocumentMetaData.class, "documentBaseUri");
  private final static MethodHandle _FH_documentBaseUri = _FC_documentBaseUri.dynamicInvoker();
  private final static CallSite _FC_isLastSegment = TypeSystemImpl.createCallSite(DocumentMetaData.class, "isLastSegment");
  private final static MethodHandle _FH_isLastSegment = _FC_isLastSegment.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentMetaData() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public DocumentMetaData(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DocumentMetaData(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DocumentMetaData(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: documentTitle

  /** getter for documentTitle - gets The human readable title of the document.
   * @generated
   * @return value of the feature 
   */
  public String getDocumentTitle() { return _getStringValueNc(wrapGetIntCatchException(_FH_documentTitle));}
    
  /** setter for documentTitle - sets The human readable title of the document. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentTitle(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_documentTitle), v);
  }    
    
   
    
  //*--------------*
  //* Feature: documentId

  /** getter for documentId - gets The id of the document.
   * @generated
   * @return value of the feature 
   */
  public String getDocumentId() { return _getStringValueNc(wrapGetIntCatchException(_FH_documentId));}
    
  /** setter for documentId - sets The id of the document. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentId(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_documentId), v);
  }    
    
   
    
  //*--------------*
  //* Feature: documentUri

  /** getter for documentUri - gets The URI of the document.
   * @generated
   * @return value of the feature 
   */
  public String getDocumentUri() { return _getStringValueNc(wrapGetIntCatchException(_FH_documentUri));}
    
  /** setter for documentUri - sets The URI of the document. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentUri(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_documentUri), v);
  }    
    
   
    
  //*--------------*
  //* Feature: collectionId

  /** getter for collectionId - gets The ID of the whole document collection.
   * @generated
   * @return value of the feature 
   */
  public String getCollectionId() { return _getStringValueNc(wrapGetIntCatchException(_FH_collectionId));}
    
  /** setter for collectionId - sets The ID of the whole document collection. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setCollectionId(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_collectionId), v);
  }    
    
   
    
  //*--------------*
  //* Feature: documentBaseUri

  /** getter for documentBaseUri - gets Base URI of the document.
   * @generated
   * @return value of the feature 
   */
  public String getDocumentBaseUri() { return _getStringValueNc(wrapGetIntCatchException(_FH_documentBaseUri));}
    
  /** setter for documentBaseUri - sets Base URI of the document. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentBaseUri(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_documentBaseUri), v);
  }    
    
   
    
  //*--------------*
  //* Feature: isLastSegment

  /** getter for isLastSegment - gets CAS de-multipliers need to know whether a CAS is the
            last multiplied segment.
            Thus CAS multipliers should set this field to true for the last CAS
            they produce.
   * @generated
   * @return value of the feature 
   */
  public boolean getIsLastSegment() { return _getBooleanValueNc(wrapGetIntCatchException(_FH_isLastSegment));}
    
  /** setter for isLastSegment - sets CAS de-multipliers need to know whether a CAS is the
            last multiplied segment.
            Thus CAS multipliers should set this field to true for the last CAS
            they produce. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setIsLastSegment(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_isLastSegment), v);
  }    
        	
    /**
     * Create a new {@link DocumentMetaData} annotation in the given CAS. The meta data fields can
     * then be set on the returned object.
     *
     * @param aCas
     *            the CAS to create the meta data for.
     * @return a {@link DocumentMetaData} annotation that has already been added to the CAS indexes.
     * @throws IllegalStateException
     *             if there is already a {@link DocumentMetaData} annotation
     * @throws CASException
     *             if the JCas cannot be accessed from the provided CAS.
     */
	public static DocumentMetaData create(final CAS aCas)
		throws IllegalStateException, CASException
	{
		try {
			get(aCas);
			throw new IllegalStateException("CAS already contains DocumentMetaData");
		}
		catch (IllegalArgumentException e) {
			DocumentMetaData docMetaData = new DocumentMetaData(aCas.getJCas());
			initDocumentMetaData(docMetaData);
			return docMetaData;
		}
	}

    /**
     * Create a new {@link DocumentMetaData} annotation in the given CAS. The meta data fields can
     * then be set on the returned object.
     *
     * @param aJcas
     *            the CAS to create the meta data for.
     * @return a {@link DocumentMetaData} annotation that has already been added to the CAS indexes.
     * @throws IllegalStateException
     *             if there is already a {@link DocumentMetaData} annotation
     */
	public static DocumentMetaData create(final JCas aJcas)
		throws IllegalStateException
	{
		try {
			get(aJcas);
			throw new IllegalStateException("CAS already contains DocumentMetaData");
		}
		catch (IllegalArgumentException e) {
			DocumentMetaData docMetaData = new DocumentMetaData(aJcas);
			initDocumentMetaData(docMetaData);
			return docMetaData;
		}
	}

	private static DocumentMetaData initDocumentMetaData(DocumentMetaData aMetaData)
	{
		// If there is already a DocumentAnnotation copy it's information and delete it
		DocumentAnnotation da = getDocumentAnnotation(aMetaData.getView());
		if (da != null) {
			aMetaData.setLanguage(da.getLanguage());
			aMetaData.setBegin(da.getBegin());
			aMetaData.setEnd(da.getEnd());
			da.removeFromIndexes();
		}
		else if (aMetaData.getView().getDocumentText() != null) {
			aMetaData.setBegin(0);
			aMetaData.setEnd(aMetaData.getView().getDocumentText().length());
		}

		aMetaData.addToIndexes();
		return aMetaData;
	}

	/**
	 * Copy the {@link DocumentMetaData} annotation from one view to another.
	 *
	 * @param aSourceView
	 *            the source.
	 * @param aTargetView
	 *            the target.
	 */
	public static void copy(final JCas aSourceView, final JCas aTargetView)
	{
	    // First get the DMD then create. In case the get fails, we do not create.
        DocumentMetaData dmd = get(aSourceView);
		DocumentMetaData docMetaData = create(aTargetView);
		docMetaData.setCollectionId(dmd.getCollectionId());
		docMetaData.setDocumentBaseUri(dmd.getDocumentBaseUri());
		docMetaData.setDocumentId(dmd.getDocumentId());
		docMetaData.setDocumentTitle(dmd.getDocumentTitle());
		docMetaData.setDocumentUri(dmd.getDocumentUri());
		docMetaData.setIsLastSegment(dmd.getIsLastSegment());
		docMetaData.setLanguage(dmd.getLanguage());
	}

	/**
	 * Get the {@link DocumentMetaData} from the CAS.
	 * @param aCas a CAS.
	 * @return the {@link DocumentMetaData} from the CAS.
	 * @throws IllegalArgumentException
	 *             if no {@link DocumentMetaData} exists in the CAS.
	 */
	public static DocumentMetaData get(final CAS aCas)
	{
		FSIterator<FeatureStructure> iterator = aCas.getIndexRepository().getAllIndexedFS(
				CasUtil.getType(aCas, DocumentMetaData.class));

		if (!iterator.hasNext()) {
			throw new IllegalArgumentException(new Throwable("CAS does not contain any "
					+ DocumentMetaData.class.getName()));
		}

		DocumentMetaData result = (DocumentMetaData) iterator.next();

		if (iterator.hasNext()) {
			throw new IllegalArgumentException(new Throwable("CAS contains more than one "
					+ DocumentMetaData.class.getName()));
		}

		return result;
	}

    /**
     * Get the {@link DocumentAnnotation} from the CAS if it already exists.
     * 
     * @param aCas
     *            a CAS.
     * @return the {@link DocumentAnnotation} from the CAS.
     */
	private static DocumentAnnotation getDocumentAnnotation(final CAS aCas)
	{
		FSIterator<FeatureStructure> iterator = aCas.getIndexRepository().getAllIndexedFS(
				CasUtil.getType(aCas, DocumentAnnotation.class));

		if (!iterator.hasNext()) {
			return null;
		}

		DocumentAnnotation result = (DocumentAnnotation) iterator.next();

		if (iterator.hasNext()) {
			throw new IllegalArgumentException(new Throwable("CAS contains more than one "
					+ DocumentAnnotation.class.getName()));
		}

		return result;
	}

    /**
     * Get the {@link DocumentMetaData} from the CAS.
     *
     * @param aJCas
     *            the JCas.
     * @return the {@link DocumentMetaData} from the CAS.
     * @throws IllegalArgumentException
     *             if no {@link DocumentMetaData} exists in the CAS.
     */
	public static DocumentMetaData get(final JCas aJCas)
	{
		return get(aJCas.getCas());
	}
}
