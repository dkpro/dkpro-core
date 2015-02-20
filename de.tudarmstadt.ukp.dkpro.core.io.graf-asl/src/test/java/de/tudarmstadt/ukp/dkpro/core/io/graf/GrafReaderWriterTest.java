package de.tudarmstadt.ukp.dkpro.core.io.graf;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class GrafReaderWriterTest
{
    @Ignore("Doesn't work yet...")
    @Test
    public void test()
        throws Exception
    {
        String header = "<resourceHeader xmlns=\"http://www.xces.org/ns/GrAF/1.0/\">\n" + 
                "   <resourceDesc>\n" + 
                "       <annotationSpaces>\n" + 
                "           <annotationSpace xml:id=\"pos\" pid=\"http://dummy1/\"/>\n" + 
                "           <annotationSpace xml:id=\"type\" pid=\"http://dummy2/\"/>\n" + 
                "       </annotationSpaces>\n" + 
                "   </resourceDesc>\n" + 
                "</resourceHeader>";
        
        FileUtils.writeStringToFile(new File("target/header.xml"), header);
        
        testRoundTrip(GrafReader.class, GrafWriter.class, "reference/example1.txt.xml");
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
