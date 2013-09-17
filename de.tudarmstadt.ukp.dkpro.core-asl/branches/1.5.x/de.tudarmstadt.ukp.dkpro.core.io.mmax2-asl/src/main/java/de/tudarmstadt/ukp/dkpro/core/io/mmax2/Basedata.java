/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.io.mmax2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Basedata {

	private final Map<Integer, String> ids = new HashMap<Integer, String>();

	private final List<String[]> words = new ArrayList<String[]>();

	private int id = 0;

	public void append(String term, int startOffset) {
		String[] word = new String[2];
		word[0] = "word_" + id;
		word[1] = term;
		ids.put(startOffset, word[0]);
		words.add(word);
		id++;
	}

	public String getId(int offset) {
		return ids.get(offset);
	}

	public String[] getWord(int offset) {
		return words.get(offset);
	}

	public void save(File file) throws MmaxWriterException {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder builder = factory.newDocumentBuilder();
    		Document doc = builder.newDocument();
    		Element list = doc.createElement("words");

    		for (String[] word : words) {
    			Element cur = doc.createElement("word");
    			cur.setAttribute("id", word[0]);
    			cur.appendChild(doc.createTextNode(word[1]));
    			list.appendChild(cur);
    		}
    		doc.appendChild(list);

    		DOMSource source = new DOMSource(doc);
    		StreamResult result = new StreamResult(new FileOutputStream(file));
    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
     	    transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
     	    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "words.dtd");
     	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
     	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
     	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
     	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

     	   transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            throw new MmaxWriterException(e);
        } catch (FileNotFoundException e) {
            throw new MmaxWriterException(e);
        } catch (TransformerConfigurationException e) {
            throw new MmaxWriterException(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new MmaxWriterException(e);
        } catch (TransformerException e) {
            throw new MmaxWriterException(e);
        }
	}

	public void save(String filename) throws MmaxWriterException {
		save(new File(filename));
	}
}