/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.trim;
import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.unescapeToken;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.uima.fit.util.FSCollectionFactory.createFSArray;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

public class PennTreeToJCasConverter
{
    private static final String ROOT = "ROOT";
    private static final String NONE = "-NONE-";

    private boolean writeTracesToText;
    private boolean createPosTags;
    private boolean internTags;
    private String rootLabel = ROOT;
    
    private MappingProvider posMappingProvider;
    private MappingProvider constituentMappingProvider;
    
    public PennTreeToJCasConverter(MappingProvider aPosMappingProvider,
            MappingProvider aConstituentMappingProvider)
    {
        posMappingProvider = aPosMappingProvider;
        constituentMappingProvider = aConstituentMappingProvider;
    }

    public boolean isWriteTracesToText()
    {
        return writeTracesToText;
    }

    public void setWriteTracesToText(boolean aWriteTracesToText)
    {
        writeTracesToText = aWriteTracesToText;
    }

    public boolean isCreatePosTags()
    {
        return createPosTags;
    }

    public void setCreatePosTags(boolean aCreatePosTags)
    {
        createPosTags = aCreatePosTags;
    }

    public boolean isInternTags()
    {
        return internTags;
    }

    public void setInternTags(boolean aInternTags)
    {
        internTags = aInternTags;
    }

    public String getRootLabel()
    {
        return rootLabel;
    }

    public void setRootLabel(String aRootLabel)
    {
        rootLabel = aRootLabel;
    }

    public Constituent convertPennTree(JCas aJCas, StringBuilder aText, PennTreeNode aNode)
    {
        return convertPennTree(aJCas, aText, aNode, null, true);
    }
    
    private Constituent convertPennTree(JCas aJCas, StringBuilder aText, PennTreeNode aNode,
            Constituent aParent, boolean aBOS)
    {
        boolean bos = aBOS;
        Constituent constituent = null;
        Constituent parent = aParent;
        boolean generatedParent = false;
        
        // Do we need to insert an artificial ROOT node?
        if (aParent == null) {
            // Case 2: no root node:        (S
            if (!rootLabel.equals(aNode.getLabel()) && !isBlank(aNode.getLabel())) {
                constituent = createConstituent(aJCas, aNode.getLabel());
                
                parent = new ROOT(aJCas);
                parent.setConstituentType(ROOT);
                parent.setChildren(createFSArray(aJCas, new Constituent[] { constituent }));
                generatedParent = true;
                
                constituent.setParent(parent);
            }
            // Case 1: unlabeled root node: ( (S...
            // Case 3: labeled root node:   (ROOT (S...
            else {
                constituent = new ROOT(aJCas);
                constituent.setConstituentType(ROOT);
            }
        }
        else {
            constituent = createConstituent(aJCas, aNode.getLabel());
        }
        
        constituent.setBegin(aText.length());
        
        List<Annotation> children = new ArrayList<Annotation>();
        for (PennTreeNode c : aNode.getChildren()) {
            if (c.isPreTerminal()) {
                // Do not read traces into the CAS, at least not as tokens
                if (!writeTracesToText && NONE.equals(c.getLabel())) {
                    continue;
                }
                
                // Add space between tokens with inside sentence. Do not add token at the beginning
                // of the sentence, even if we append into a larger document.
                if (!bos) {
                    aText.append(' ');
                }
                
                // Add to the document test
                int begin = aText.length();
                aText.append(unescapeToken(c.getChildren().get(0).getLabel()));
                int end = aText.length();

                // only add POS to index if we want POS-tagging
                POS posAnno = null;
                if (createPosTags) {
                    if (posMappingProvider != null) {
                        Type posTag = posMappingProvider.getTagType(c.getLabel());
                        posAnno = (POS) aJCas.getCas().createAnnotation(posTag, begin, end);
                    }
                    else {
                        posAnno = new POS(aJCas, begin, end);
                    }
                    posAnno.setPosValue(internTags ? c.getLabel().intern() : c.getLabel());
                    posAnno.addToIndexes();
                }
                
                Token token = new Token(aJCas, begin, end);
                token.setPos(posAnno);
                token.addToIndexes();
                
                children.add(token);
            }
            else {
                children.add(convertPennTree(aJCas, aText, c, constituent, bos));
            }
            bos = false;
        }

        constituent.setEnd(aText.length());
        
        int[] offsets = {constituent.getBegin(), constituent.getEnd()};
        trim(aText, offsets);
        constituent.setBegin(offsets[0]);
        constituent.setEnd(offsets[1]);
        constituent.setChildren(createFSArray(aJCas, children));
        constituent.addToIndexes();
        
        // We we created an additional ROOT node, then we need to set its offsets as well
        if (generatedParent) {
            parent.setBegin(constituent.getBegin());
            parent.setEnd(constituent.getEnd());
            parent.addToIndexes();
        }
        
        return constituent;        
    }

    public Constituent convertPennTree(Sentence aSentence, PennTreeNode aNode)
    {
        JCas jcas;
        try {
            jcas = aSentence.getCAS().getJCas();
        }
        catch (CASException e) {
            throw new IllegalStateException(e);
        }
            
        List<Token> tokens = selectCovered(Token.class, aSentence);
        List<PennTreeNode> preTerminalNodes = PennTreeUtils.getPreTerminals(aNode);
        Map<PennTreeNode, Token> tokenMap = new HashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            tokenMap.put(preTerminalNodes.get(i), tokens.get(i));
        }
        
        return convertPennTree(jcas, aNode, null, tokenMap);
    }
    

    private Constituent convertPennTree(JCas aJCas, PennTreeNode aNode,
            Constituent aParent, Map<PennTreeNode, Token> aTokenMap)
    {
        Constituent constituent = null;
        Constituent parent = aParent;
        boolean generatedParent = false;
        
        // Do we need to insert an artificial ROOT node?
        if (aParent == null) {
            // Case 2: no root node:        (S
            if (!rootLabel.equals(aNode.getLabel()) && !isBlank(aNode.getLabel())) {
                constituent = createConstituent(aJCas, aNode.getLabel());
                
                parent = new ROOT(aJCas);
                parent.setConstituentType(ROOT);
                parent.setChildren(createFSArray(aJCas, new Constituent[] { constituent }));
                generatedParent = true;
                
                constituent.setParent(parent);
            }
            // Case 1: unlabeled root node: ( (S...
            // Case 3: labeled root node:   (ROOT (S...
            else {
                constituent = new ROOT(aJCas);
                constituent.setConstituentType(ROOT);
            }
        }
        else {
            constituent = createConstituent(aJCas, aNode.getLabel());
        }
        
        List<Annotation> children = new ArrayList<Annotation>();
        for (PennTreeNode c : aNode.getChildren()) {
            if (c.isPreTerminal()) {
                Token token = aTokenMap.get(c);
                children.add(token);
            }
            else {
                children.add(convertPennTree(aJCas, c, constituent, aTokenMap));
            }
        }

        constituent.setBegin(children.get(0).getBegin());
        constituent.setEnd(children.get(children.size()-1).getEnd());
        constituent.setChildren(createFSArray(aJCas, children));
        constituent.addToIndexes();
        
        // We we created an additional ROOT node, then we need to set its offsets as well
        if (generatedParent) {
            parent.setBegin(constituent.getBegin());
            parent.setEnd(constituent.getEnd());
            parent.addToIndexes();
        }
        
        return constituent;        
    }

    private Constituent createConstituent(JCas aJCas, String aLabel)
    {
        if (NONE.equals(aLabel)) {
            return new Constituent(aJCas);
        }

        String[] label = aLabel.split("-");
        Constituent constituentAnno;
        if (constituentMappingProvider != null) {
            Type constituentTag = constituentMappingProvider.getTagType(label[0]);
            // We just set a dummy value for the offsets here. These need to be fixed when we know the
            // children and before addToIndexes() is called.
            constituentAnno = (Constituent) aJCas.getCas().createAnnotation(constituentTag, 0, 0);
        }
        else {
            constituentAnno = new Constituent(aJCas, 0, 0);
        }
        
        constituentAnno.setConstituentType(label[0]);
        
        if (label.length >= 2) {
            constituentAnno.setSyntacticFunction(label[1]);
        }
        
        return constituentAnno;
    }
}
