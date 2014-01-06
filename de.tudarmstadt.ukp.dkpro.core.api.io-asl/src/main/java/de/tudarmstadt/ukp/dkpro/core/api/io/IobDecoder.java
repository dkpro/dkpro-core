package de.tudarmstadt.ukp.dkpro.core.api.io;

import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;

public class IobDecoder
{
    private CAS cas;
    private Feature chunkValue;

    private MappingProvider mappingProvider;

    private boolean internTags = true;
    
    private String openChunk;
    private int start;
    private int end;
    

    public IobDecoder(CAS aCas, Feature aChunkValue, MappingProvider aMappingProvider)
    {
        super();
        cas = aCas;
        chunkValue = aChunkValue;
        mappingProvider = aMappingProvider;
    }

    public void setInternTags(boolean aInternTags)
    {
        internTags = aInternTags;
    }
    
    public void decode(List<? extends AnnotationFS> aTokens, String[] aChunkTags)
    {
        int i = 0;
        for (AnnotationFS token : aTokens) {
            // System.out.printf("%s %s %n", token.getCoveredText(), aChunkTags[i]);
            String fields[] = aChunkTags[i].split("-");
            String flag = fields.length == 2 ? fields[0] : "NONE";
            String chunk = fields.length == 2 ? fields[1] : fields[0];

            // Start of a new hunk
            if (!chunk.equals(openChunk) || "B".equals(flag)) {
                if (openChunk != null) {
                    // End of previous chunk
                    chunkComplete();
                }

                openChunk = chunk;
                start = token.getBegin();
            }

            // Record how much of the chunk we have seen so far
            end = token.getEnd();
            
            i++;
        }
        
        // End of processing signal
        chunkComplete();
    }
    
    private void chunkComplete()
    {
        if (openChunk != null) {
            Type chunkType = mappingProvider.getTagType(openChunk);
            AnnotationFS chunk = cas.createAnnotation(chunkType, start, end);
            chunk.setStringValue(chunkValue, internTags ? openChunk.intern() :
                openChunk);
            cas.addFsToIndexes(chunk);
            openChunk = null;
        }
    }
}