
/* First created by JCasGen Wed Jan 25 16:39:50 CET 2012 */
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Represents a revision in Wikipedia. Updated by JCasGen Wed Jan 25 16:39:50
 * CET 2012 XML source:
 * /home/daxenberger/workspace/de.tudarmstadt.ukp.dkpro.core
 * .io.jwpl-asl/src/main/resources/desc/type/WikipediaRevision.xml
 * 
 * @generated
 */
public class WikipediaRevision
	extends Annotation
{
	/**
	 * @generated
	 * @ordered
	 */
	public final static int typeIndexID = JCasRegistry
			.register(WikipediaRevision.class);
	/**
	 * @generated
	 * @ordered
	 */
	public final static int type = typeIndexID;

	/** @generated */
	@Override
	public int getTypeIndexID()
	{
		return typeIndexID;
	}

	/**
	 * Never called. Disable default constructor
	 * 
	 * @generated
	 */
	protected WikipediaRevision()
	{/* intentionally empty block */
	}

	/**
	 * Internal - constructor used by generator
	 * 
	 * @generated
	 */
	public WikipediaRevision(int addr, TOP_Type type)
	{
		super(addr, type);
		readObject();
	}

	/** @generated */
	public WikipediaRevision(JCas jcas)
	{
		super(jcas);
		readObject();
	}

	/** @generated */
	public WikipediaRevision(JCas jcas, int begin, int end)
	{
		super(jcas);
		setBegin(begin);
		setEnd(end);
		readObject();
	}

	/**
	 * <!-- begin-user-doc --> Write your own initialization here <!--
	 * end-user-doc -->
	 * 
	 * @generated modifiable
	 */
	private void readObject()
	{/* default - does nothing empty block */
	}

	// *--------------*
	// * Feature: revisionId

	/**
	 * getter for revisionId - gets The ID of the revision.
	 * 
	 * @generated
	 */
	public int getRevisionId()
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_revisionId == null) {
			jcasType.jcas
					.throwFeatMissing("revisionId",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		return jcasType.ll_cas.ll_getIntValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_revisionId);
	}

	/**
	 * setter for revisionId - sets The ID of the revision.
	 * 
	 * @generated
	 */
	public void setRevisionId(int v)
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_revisionId == null) {
			jcasType.jcas
					.throwFeatMissing("revisionId",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		jcasType.ll_cas.ll_setIntValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_revisionId, v);
	}

	// *--------------*
	// * Feature: pageId

	/**
	 * getter for pageId - gets The pageId of the Wikipedia page of this
	 * revision.
	 * 
	 * @generated
	 */
	public int getPageId()
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_pageId == null) {
			jcasType.jcas
					.throwFeatMissing("pageId",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		return jcasType.ll_cas.ll_getIntValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_pageId);
	}

	/**
	 * setter for pageId - sets The pageId of the Wikipedia page of this
	 * revision.
	 * 
	 * @generated
	 */
	public void setPageId(int v)
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_pageId == null) {
			jcasType.jcas
					.throwFeatMissing("pageId",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		jcasType.ll_cas.ll_setIntValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_pageId, v);
	}

	// *--------------*
	// * Feature: contributorName

	/**
	 * getter for contributorName - gets The username of the user/contributor
	 * who edited this revision.
	 * 
	 * @generated
	 */
	public String getContributorName()
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_contributorName == null) {
			jcasType.jcas
					.throwFeatMissing("contributorName",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		return jcasType.ll_cas
				.ll_getStringValue(
						addr,
						((WikipediaRevision_Type) jcasType).casFeatCode_contributorName);
	}

	/**
	 * setter for contributorName - sets The username of the user/contributor
	 * who edited this revision.
	 * 
	 * @generated
	 */
	public void setContributorName(String v)
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_contributorName == null) {
			jcasType.jcas
					.throwFeatMissing("contributorName",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		jcasType.ll_cas
				.ll_setStringValue(
						addr,
						((WikipediaRevision_Type) jcasType).casFeatCode_contributorName,
						v);
	}

	// *--------------*
	// * Feature: comment

	/**
	 * getter for comment - gets The comment that the editor entered for this
	 * revision.
	 * 
	 * @generated
	 */
	public String getComment()
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_comment == null) {
			jcasType.jcas
					.throwFeatMissing("comment",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		return jcasType.ll_cas.ll_getStringValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_comment);
	}

	/**
	 * setter for comment - sets The comment that the editor entered for this
	 * revision.
	 * 
	 * @generated
	 */
	public void setComment(String v)
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_comment == null) {
			jcasType.jcas
					.throwFeatMissing("comment",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		jcasType.ll_cas.ll_setStringValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_comment, v);
	}

	// *--------------*
	// * Feature: contributorId

	/**
	 * getter for contributorId - gets The userId of the user/contributor who
	 * created this revision
	 * 
	 * @generated
	 */
	public int getContributorId()
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_contributorId == null) {
			jcasType.jcas
					.throwFeatMissing("contributorId",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		return jcasType.ll_cas.ll_getIntValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_contributorId);
	}

	/**
	 * setter for contributorId - sets The userId of the user/contributor who
	 * created this revision
	 * 
	 * @generated
	 */
	public void setContributorId(int v)
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_contributorId == null) {
			jcasType.jcas
					.throwFeatMissing("contributorId",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		jcasType.ll_cas.ll_setIntValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_contributorId,
				v);
	}

	// *--------------*
	// * Feature: timestamp

	/**
	 * getter for timestamp - gets The timestamp of the revision, given in
	 * milliseconds since the standard base time (January 1, 1970, 00:00:00 GMT)
	 * 
	 * @generated
	 */
	public long getTimestamp()
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_timestamp == null) {
			jcasType.jcas
					.throwFeatMissing("timestamp",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		return jcasType.ll_cas.ll_getLongValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_timestamp);
	}

	/**
	 * setter for timestamp - sets The timestamp of the revision, given in
	 * milliseconds since the standard base time (January 1, 1970, 00:00:00 GMT)
	 * 
	 * @generated
	 */
	public void setTimestamp(long v)
	{
		if (WikipediaRevision_Type.featOkTst
				&& ((WikipediaRevision_Type) jcasType).casFeat_timestamp == null) {
			jcasType.jcas
					.throwFeatMissing("timestamp",
							"de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
		}
		jcasType.ll_cas.ll_setLongValue(addr,
				((WikipediaRevision_Type) jcasType).casFeatCode_timestamp, v);
	}
}
