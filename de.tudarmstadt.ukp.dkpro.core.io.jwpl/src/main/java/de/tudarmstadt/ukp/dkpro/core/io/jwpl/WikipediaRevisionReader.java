package de.tudarmstadt.ukp.dkpro.core.io.jwpl;

import java.io.IOException;
import java.sql.Timestamp;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.jwpl.util.WikiUtils;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;

/**
 * Reads all revisions of all articles.
 * 
 * @author zesch
 *
 */
public class WikipediaRevisionReader extends WikipediaRevisionReaderBase
{

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        // if hasNext() is true, a next timestamp should be available
        Timestamp timestamp = timestampIter.next();
        
        try {
            Revision revision = this.revisionEncoder.getRevision(currentArticle.getPageId(), timestamp);

            String text = "";
            if (outputPlainText) {
                text = WikiUtils.cleanText(
                        StringEscapeUtils.unescapeHtml(revision.getRevisionText())
                );
            }
            else {
                text = revision.getRevisionText();
            }
            jcas.setDocumentText(text);
            
            addDocumentMetaData(jcas, currentArticle.getPageId(), revision.getRevisionID());
            addRevisionAnnotation(jcas, revision);
        }
        catch (WikiApiException e) {
            throw new CollectionException(e);
        }
    }
}