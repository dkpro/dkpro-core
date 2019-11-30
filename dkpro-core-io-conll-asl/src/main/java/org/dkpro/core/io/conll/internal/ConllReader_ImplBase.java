package org.dkpro.core.io.conll.internal;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

/**
 * Abstract base class for CoNLL format readers.
 */
public abstract class ConllReader_ImplBase
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Trim field values.
     */
    public static final String PARAM_TRIM_FIELDS = "trimFields";
    @ConfigurationParameter(name = PARAM_TRIM_FIELDS, mandatory = true, defaultValue = "true")
    protected boolean trimFields;

    protected String cleanTag(String aField)
    {
        if (aField == null) {
            return null;
        }
        
        return trim(aField).intern();
    }

    protected String trim(String aField)
    {
        if (aField == null || !trimFields) {
            return aField;
        }
        
        return aField.trim();
    }
    
    protected Dependency makeDependency(JCas aJCas, int govId, int depId, String label,
            String flavor, Int2ObjectMap<Token> tokens, String[] word)
    {
        Dependency rel;

        if (govId == 0) {
            rel = new ROOT(aJCas);
            rel.setGovernor(tokens.get(depId));
            rel.setDependent(tokens.get(depId));
        }
        else {
            rel = new Dependency(aJCas);
            rel.setGovernor(tokens.get(govId));
            rel.setDependent(tokens.get(depId));
        }

        rel.setDependencyType(label);
        rel.setFlavor(flavor);
        rel.setBegin(rel.getDependent().getBegin());
        rel.setEnd(rel.getDependent().getEnd());
        rel.addToIndexes();

        return rel;
    }
}
