package de.tudarmstadt.ukp.dkpro.core.io.gate;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2000Reader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class GateXmlWriterTest
{
    @Test
    public void oneWay()
        throws Exception
    {
        testOneWay(Conll2000Reader.class, GateXmlWriter.class,
                "conll/2000/chunk2000_ref.xml", "conll/2000/chunk2000_test.conll");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
