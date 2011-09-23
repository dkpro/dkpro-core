package de.tudarmstadt.ukp.dkpro.core.io.aclanthology;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.File;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class AclArcReaderTest
{
    @Test
    public void aclArcReaderTest()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(
                AclAnthologyReader.class,
                ResourceCollectionReaderBase.PARAM_PATH,
                        new File("src/test/resources/acl/").getAbsolutePath(),
                ResourceCollectionReaderBase.PARAM_PATTERNS,
                        new String[] {ResourceCollectionReaderBase.INCLUDE_PREFIX + "**/*.txt" }
        );

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            System.out.println(md.getDocumentUri());

            if (i < 2) {
                System.out.println(jcas.getDocumentText());
                System.out.println();
                System.out.println();
            }

            i++;
        }
        assertEquals(10, i);
    }
}