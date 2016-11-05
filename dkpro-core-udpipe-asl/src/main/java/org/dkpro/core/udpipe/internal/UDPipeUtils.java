package org.dkpro.core.udpipe.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class UDPipeUtils
{
    private static boolean initialized = false;
    
    public static void init() throws IOException
    {
        if (initialized) {
            return;
        }

        PlatformDetector pd = new PlatformDetector();
        
        String platform = pd.getPlatformId();

        File binFolder = ResourceUtils.getClasspathAsFolder(
                "classpath*:org/dkpro/core/udpipe/lib/lib/bin/bin-" + platform, true);
        
        // See https://github.com/ufal/udpipe/issues/10
        String libname = System.mapLibraryName("udpipe_java");
        Files.copy(new File(binFolder, libname).toPath(),
                Paths.get(libname), StandardCopyOption.REPLACE_EXISTING);
    }
}
