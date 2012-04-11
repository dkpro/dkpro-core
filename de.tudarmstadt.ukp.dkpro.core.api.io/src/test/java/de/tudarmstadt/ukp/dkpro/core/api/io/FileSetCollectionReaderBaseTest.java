package de.tudarmstadt.ukp.dkpro.core.api.io;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.IOException;

import org.apache.tools.ant.types.resources.FileResource;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class FileSetCollectionReaderBaseTest
{
    @Test
    public void testBaseUri()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(DummyReader.class,
                createTypeSystemDescription(), FileSetCollectionReaderBase.PARAM_PATH,
                "src/main/java/de/tudarmstadt/ukp/", FileSetCollectionReaderBase.PARAM_PATTERNS,
                new String[] { "[+]**/*.java" });

        checkBaseUri(reader);
    }

    public void checkBaseUri(CollectionReader aReader)
        throws Exception
    {
        CAS cas = CasCreationUtils.createCas(aReader.getProcessingResourceMetaData());
        while (aReader.hasNext()) {
            aReader.getNext(cas);

            DocumentMetaData meta = DocumentMetaData.get(cas);
            String baseUri = meta.getDocumentBaseUri();
            assertTrue(baseUri.endsWith("src/main/java/de/tudarmstadt/ukp/"));

            cas.reset();
        }
        cas.release();
    }

    public static final class DummyReader
        extends FileSetCollectionReaderBase
    {
        @Override
        public void getNext(CAS aCAS)
            throws IOException, CollectionException
        {
            FileResource res = nextFile();
            initCas(aCAS, res);
        }

    }
}
