/*
 * The jsoup code-base (include source and compiled packages) are distributed under the open source 
 * MIT license as described below.
 * 
 * The MIT License
 * Copyright © 2009 - 2016 Jonathan Hedley (jonathan@hedley.net)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.dkpro.core.io.html.internal;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Methods in this class were copied from JSoup. They were private and could not be accessed.
 */
public final class JSoupUtil
{
    /*
     * org.jsoup.nodes.TextNode.lastCharIsWhitespace(StringBuilder)
     */
    public static boolean lastCharIsWhitespace(StringBuilder sb) {
        return sb.length() != 0 && sb.charAt(sb.length() - 1) == ' ';
    }
    
    /*
     * org.jsoup.nodes.Element.appendNormalisedText(StringBuilder, TextNode)
     */
    public static void appendNormalisedText(StringBuilder accum, TextNode textNode) {
        String text = textNode.getWholeText();

        if (preserveWhitespace(textNode.parentNode())) {
            accum.append(text);
        }
        else {
            StringUtil.appendNormalisedWhitespace(accum, text, lastCharIsWhitespace(accum));
        }
    }
    
    /*
     * org.jsoup.nodes.Element.preserveWhitespace(Node)
     */
    public static boolean preserveWhitespace(Node node) {
        // looks only at this element and one level up, to prevent recursion & needless stack
        // searches
        if (node != null && node instanceof Element) {
            Element element = (Element) node;
            return element.tag().preserveWhitespace() ||
                element.parent() != null && element.parent().tag().preserveWhitespace();
        }
        return false;
    }
}
