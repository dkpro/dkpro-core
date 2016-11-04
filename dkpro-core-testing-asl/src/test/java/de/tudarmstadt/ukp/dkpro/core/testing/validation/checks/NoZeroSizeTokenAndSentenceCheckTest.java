package de.tudarmstadt.ukp.dkpro.core.testing.validation.checks;

import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.ERROR;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.CasValidator;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public class NoZeroSizeTokenAndSentenceCheckTest
{
    @Test
    public void testZeroLengthToken()
        throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("test");
        new Token(jcas, 1, 1).addToIndexes();

        CasValidator validator = new CasValidator(PosAttachedToTokenCheck.class);
        List<Message> messages = validator.analyze(jcas);

        messages.forEach(m -> System.out.println(m));

        assertTrue(messages.stream().anyMatch(m -> m.level == ERROR));
    }

    @Test
    public void testZeroLengthSentence()
        throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("test");
        new Sentence(jcas, 3,3).addToIndexes();

        CasValidator validator = new CasValidator(PosAttachedToTokenCheck.class);
        List<Message> messages = validator.analyze(jcas);

        messages.forEach(m -> System.out.println(m));

        assertTrue(messages.stream().anyMatch(m -> m.level == ERROR));
    }

    @Test
    public void testCorrectCas()
        throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("test");
        new Sentence(jcas, 0,4).addToIndexes();
        new Token(jcas, 1, 2).addToIndexes();

        CasValidator validator = new CasValidator(PosAttachedToTokenCheck.class);
        List<Message> messages = validator.analyze(jcas);

        messages.forEach(m -> System.out.println(m));

        assertTrue(messages.stream().anyMatch(m -> m.level == ERROR));
    }
}
