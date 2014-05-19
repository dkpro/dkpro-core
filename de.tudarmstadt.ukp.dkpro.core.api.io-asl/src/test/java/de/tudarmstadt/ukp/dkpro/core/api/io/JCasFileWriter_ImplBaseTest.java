package de.tudarmstadt.ukp.dkpro.core.api.io;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class JCasFileWriter_ImplBaseTest
{
    @Test
    public void writeToZip() throws Exception
    {
        AnalysisEngine ae = createEngine(DummyWriter.class,
                DummyWriter.PARAM_TARGET_LOCATION, "jar:file:target/out.zip");
        JCas jcas = JCasFactory.createJCas();
        ae.process(jcas);
        ae.process(jcas);
        ae.process(jcas);
        ae.collectionProcessComplete();
    }

    @Test
    public void writeToZip2() throws Exception
    {
        AnalysisEngine ae = createEngine(DummyWriter.class,
                DummyWriter.PARAM_TARGET_LOCATION, "jar:file:target/out2.zip!test");
        JCas jcas = JCasFactory.createJCas();
        ae.process(jcas);
        ae.process(jcas);
        ae.process(jcas);
        ae.collectionProcessComplete();
    }

    public static final class DummyWriter
        extends JCasFileWriter_ImplBase
    {
        private int count = 0;
        
        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            Writer docOS = null;
            try {
                docOS = new OutputStreamWriter(getOutputStream("file-" + count, ".txt"), "UTF-8");
                docOS.write("This is the file " + count);
                count++;
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
            finally {
                closeQuietly(docOS);
            }
        }
    }
}
