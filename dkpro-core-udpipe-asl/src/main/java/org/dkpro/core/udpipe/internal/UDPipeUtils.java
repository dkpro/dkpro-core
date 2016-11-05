package org.dkpro.core.udpipe.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public class UDPipeUtils
{
    private static boolean initialized = false;
    
    public static void init() throws IOException
    {
        if (initialized) {
            return;
        }

        RuntimeProvider runtimeProvider = new RuntimeProvider(
                "classpath:/org/dkpro/core/udpipe/bin/");
        
        // See https://github.com/ufal/udpipe/issues/10
        String libname = System.mapLibraryName("udpipe_java");
        Files.copy(runtimeProvider.getFile(libname).toPath(),
                Paths.get(libname), StandardCopyOption.REPLACE_EXISTING);
    }
}
