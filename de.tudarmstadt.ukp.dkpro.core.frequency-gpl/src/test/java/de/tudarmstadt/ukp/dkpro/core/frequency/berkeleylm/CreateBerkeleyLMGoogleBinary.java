package de.tudarmstadt.ukp.dkpro.core.frequency.berkeleylm;

import java.io.IOException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.util.Logger;

public class CreateBerkeleyLMGoogleBinary
{

    
    public static void main(String[] args) throws IOException
    {
        String path = DKProContext.getContext().getWorkspace("berkeley_lm").getAbsolutePath();    
        run(path + "/en", "target/blm_en.ser");
        run(path + "/de", "target/blm_de.ser");
    }

    private static void run(String path, String outFile) {
        Logger.setGlobalLogger(new Logger.SystemLogger(System.out, System.err));
        Logger.startTrack("Reading Lm File " + path + " . . . ");
        final NgramLanguageModel<String> lm = LmReaders.readLmFromGoogleNgramDir(path, true);
        Logger.endTrack();
        Logger.startTrack("Writing to file " + outFile + " . . . ");
        LmReaders.writeLmBinary(lm, outFile);
        Logger.endTrack();
        
    }
}