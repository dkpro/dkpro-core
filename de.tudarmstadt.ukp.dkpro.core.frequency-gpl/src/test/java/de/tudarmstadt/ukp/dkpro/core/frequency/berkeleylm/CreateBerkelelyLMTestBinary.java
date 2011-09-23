package de.tudarmstadt.ukp.dkpro.core.frequency.berkeleylm;

import java.io.IOException;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.util.Logger;

public class CreateBerkelelyLMTestBinary
{

    
    public static void main(String[] args) throws IOException
    {
        run("src/test/resources/googledir/", "target/test.ser");
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