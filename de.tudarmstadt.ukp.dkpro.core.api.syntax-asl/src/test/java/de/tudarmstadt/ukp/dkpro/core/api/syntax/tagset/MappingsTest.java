package de.tudarmstadt.ukp.dkpro.core.api.syntax.tagset;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;

public class MappingsTest
{
    @Test
    public void testMappings() throws Exception
    {
        Collection<File> files = FileUtils.listFiles(
                new File("src/main/resources/de/tudarmstadt/ukp/dkpro/core/api/syntax/tagset"),
                new WildcardFileFilter("*.map"),
                TrueFileFilter.TRUE);
        
        for (File file : files) {
            boolean failure = false;
            System.out.printf("== %s ==%n", file.getName());
            MappingProvider mappingProvider = new MappingProvider();
            mappingProvider.setDefault(MappingProvider.LOCATION, file.toURI().toURL().toString());
            mappingProvider.configure();
            Map<String, String> mapping = mappingProvider.getResource();
            for (Entry<String, String> entry : mapping.entrySet()) {
                try {
                    Class.forName(entry.getValue());
                }
                catch (Throwable e) {
                    System.out.printf("%s FAILED: %s %n", entry.getValue(), e.getMessage());
                    failure = true;
                }
            }
            assertFalse(failure);
        }
    }
}
