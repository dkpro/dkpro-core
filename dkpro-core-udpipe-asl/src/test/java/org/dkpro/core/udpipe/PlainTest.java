package org.dkpro.core.udpipe;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Test;

import cz.cuni.mff.ufal.udpipe.InputFormat;
import cz.cuni.mff.ufal.udpipe.Model;
import cz.cuni.mff.ufal.udpipe.Sentence;
import cz.cuni.mff.ufal.udpipe.Words;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class PlainTest
{
    @Test
    public void test()
        throws Exception
    {
        File binFolder = ResourceUtils
                .getClasspathAsFolder("classpath*:org/dkpro/core/udpipe/lib/lib/bin/bin-osx-x86_64", true);
        Files.copy(new File(binFolder, "libudpipe_java.dylib").toPath(),
                Paths.get("libudpipe_java.dylib"), StandardCopyOption.REPLACE_EXISTING);

        Model model = Model.load(
                "/Users/bluefire/git/dkpro-core/dkpro-core-udpipe-asl/target/download/models/english-ud-1.2-160523.udpipe");

        InputFormat inputFormat = model.newTokenizer(Model.getDEFAULT());
        inputFormat.setText("This is a test");

        Sentence sentence = new Sentence();
        while (inputFormat.nextSentence(sentence)) {
            Words words = sentence.getWords();
            for (int i = 0; i < words.size(); i++) {
                System.out.println(words.get(i).getForm());
            }
        }
    }
}
