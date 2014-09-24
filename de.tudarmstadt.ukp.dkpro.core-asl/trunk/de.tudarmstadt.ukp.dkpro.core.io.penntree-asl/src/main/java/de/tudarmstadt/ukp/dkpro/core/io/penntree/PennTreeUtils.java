/*******************************************************************************
 * Copyright 2012
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

import static java.util.Collections.singletonList;
import static org.apache.uima.fit.util.FSCollectionFactory.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.uima.cas.FeatureStructure;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class PennTreeUtils
{
    private static final Map<String, String> ESCAPE = new HashMap<String, String>();
    private static final Map<String, String> UNESCAPE = new HashMap<String, String>();

    static {
        ESCAPE.put("(", "-LRB-");
        ESCAPE.put(")", "-RRB-");
    }

    static {
        UNESCAPE.put("-LRB-", "(");
        UNESCAPE.put("-RRB-", ")");
    }

    public static PennTreeNode convertPennTree(Constituent aConstituent)
    {
        PennTreeNode node = new PennTreeNode();
        if (aConstituent.getSyntacticFunction() != null) {
            node.setLabel(aConstituent.getConstituentType() + '-'
                    + aConstituent.getSyntacticFunction());
        }
        else {
            node.setLabel(aConstituent.getConstituentType());
        }

        List<PennTreeNode> children = new ArrayList<PennTreeNode>();
        for (FeatureStructure c : create(aConstituent.getChildren())) {
            if (c instanceof Constituent) {
                children.add(convertPennTree((Constituent) c));
            }

            if (c instanceof Token) {
                Token t = (Token) c;

                PennTreeNode term = new PennTreeNode();
                term.setLabel(escapeToken(t.getCoveredText()));

                PennTreeNode preterm = new PennTreeNode();
                preterm.setLabel(t.getPos().getPosValue());
                preterm.setChildren(singletonList(term));

                children.add(preterm);
            }
        }

        node.setChildren(children);
        return node;
    }
    
    public static String escapeToken(String aToken)
    {
        String value = ESCAPE.get(aToken);
        return value == null ? aToken : value;
    }

    public static String unescapeToken(String aToken)
    {
        String value = UNESCAPE.get(aToken);
        return value == null ? aToken : value;
    }

    public static String toText(PennTreeNode aNode)
    {
        StringBuilder buf = new StringBuilder();
        toText(buf, aNode);
        return buf.toString();
    }

    private static void toText(StringBuilder aBuffer, PennTreeNode aNode)
    {
        if (aNode.isTerminal()) {
            if (aBuffer.length() > 0) {
                aBuffer.append(" ");
            }
            aBuffer.append(unescapeToken(aNode.getLabel()));
        }
        else {
            for (PennTreeNode n : aNode.getChildren()) {
                toText(aBuffer, n);
            }
        }
    }

    public static PennTreeNode selectDfs(PennTreeNode aNode, int aIndex)
    {
        return dfs(aIndex, new MutableInt(0), aNode);
    }

    private static PennTreeNode dfs(int aTarget, MutableInt aIndex, PennTreeNode aNode)
    {
        if (aTarget == aIndex.intValue()) {
            return aNode;
        }

        for (PennTreeNode n : aNode.getChildren()) {
            aIndex.increment();
            PennTreeNode r = dfs(aTarget, aIndex, n);
            if (r != null) {
                return r;
            }
        }

        return null;
    }

    public static PennTreeNode parsePennTree(String aTree)
    {
        StringTokenizer st = new StringTokenizer(aTree, "() ", true);

        PennTreeNode root = null;
        Stack<PennTreeNode> stack = new Stack<PennTreeNode>();
        boolean seenLabel = false;

        while (st.hasMoreTokens()) {
            String t = st.nextToken().trim();
            if (t.length() == 0) {
                // Skip
            }
            else if ("(".equals(t)) {
                PennTreeNode n = new PennTreeNode();
                stack.push(n);
                if (root == null) {
                    root = n;
                }
                seenLabel = false;
            }
            else if (")".equals(t)) {
                PennTreeNode n = stack.pop();
                if (!stack.isEmpty()) {
                    PennTreeNode p = stack.peek();
                    p.addChild(n);
                }
            }
            else if (seenLabel) {
                // If the node has two labels, its a leaf, add a new terminal node then.
                PennTreeNode p = stack.peek();
                PennTreeNode n = new PennTreeNode();
                n.setLabel(t);
                p.addChild(n);
            }
            else {
                PennTreeNode n = stack.peek();
                n.setLabel(t);
                seenLabel = true;
            }
        }

        return root;
    }

    public static String toPennTree(PennTreeNode aNode)
    {
        StringBuilder sb = new StringBuilder();
        toPennTree(sb, aNode, -1);
        return sb.toString().trim();
    }
    
    public static String toPrettyPennTree(PennTreeNode aNode)
    {
        StringBuilder sb = new StringBuilder();
        toPennTree(sb, aNode, 0);
        return sb.toString().trim();
    }

    private static void toPennTree(StringBuilder aSb, PennTreeNode aNode, int aLevel)
    {
        boolean indentationEnabled = aLevel >= 0;
        
        // This is a "(Label Token)"
        if (aNode.isPreTerminal()) {
            aSb.append('(');
            aSb.append(aNode.getLabel());
            aSb.append(' ');
            aSb.append(aNode.getChildren().get(0).getLabel());
            aSb.append(')');
        }
        else {
            if (indentationEnabled) {
                aSb.append(StringUtils.repeat(" ", aLevel * 2));
            }

            aSb.append('(');
            aSb.append(aNode.getLabel());
            
            PennTreeNode prevChild = null;
            Iterator<PennTreeNode> i = aNode.getChildren().iterator();
            while (i.hasNext()) {
                PennTreeNode child = i.next();
                if (indentationEnabled && !child.isPreTerminal()) {
                    aSb.append('\n');
                }
                else if (indentationEnabled && prevChild != null && !prevChild.isPreTerminal()) {
                    aSb.append('\n');
                    aSb.append(StringUtils.repeat(" ", (aLevel+1) * 2));
                }
                else {
                    aSb.append(' ');
                }
                toPennTree(aSb, child, indentationEnabled ? aLevel + 1 : -1);
                prevChild = child;
            }
            
            aSb.append(')');
        }
    }
    
    /**
     * Remove trailing or leading whitespace from the annotation.
     */
    public static void trim(CharSequence aText, int[] aSpan)
    {
        int begin = aSpan[0];
        int end = aSpan[1]-1;

        CharSequence data = aText;
        while (
                (begin < (data.length()-1))
                && trimChar(data.charAt(begin))
        ) {
            begin ++;
        }
        while (
                (end > 0)
                && trimChar(data.charAt(end))
        ) {
            end --;
        }

        end++;

        aSpan[0] = begin;
        aSpan[1] = end;
    }

    public static boolean isEmpty(int aBegin, int aEnd)
    {
        return aBegin >= aEnd;
    }

    public static boolean trimChar(final char aChar)
    {
        switch (aChar) {
        case '\n':     return true; // Line break
        case '\r':     return true; // Carriage return
        case '\t':     return true; // Tab
        case '\u200E': return true; // LEFT-TO-RIGHT MARK
        case '\u200F': return true; // RIGHT-TO-LEFT MARK
        case '\u2028': return true; // LINE SEPARATOR
        case '\u2029': return true; // PARAGRAPH SEPARATOR
        default:
            return  Character.isWhitespace(aChar);
        }
    }

    public static List<PennTreeNode> getPreTerminals(PennTreeNode aNode)
    {
        List<PennTreeNode> preTerminals = new ArrayList<>();
        getPreTerminals(aNode, preTerminals);
        return preTerminals;
    }
    
    private static void getPreTerminals(PennTreeNode aNode, List<PennTreeNode> aList)
    {
        if (aNode.isPreTerminal()) {
            aList.add(aNode);
        }
        else {
            for (PennTreeNode n : aNode.getChildren()) {
                getPreTerminals(n, aList);
            }
        }
    }
}
