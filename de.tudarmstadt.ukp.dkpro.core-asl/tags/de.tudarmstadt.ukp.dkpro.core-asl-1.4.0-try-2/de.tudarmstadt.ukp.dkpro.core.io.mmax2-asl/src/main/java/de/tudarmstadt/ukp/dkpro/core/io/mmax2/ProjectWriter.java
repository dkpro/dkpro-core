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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;

public class ProjectWriter {

	private static final String BASEDATA_PATH      = "Basedata"       + File.separator;
//	private static final String CUSTOMIZATION_PATH = "Customizations" + File.separator;
	private static final String MARKABLE_PATH      = "Markables"      + File.separator;
//	private static final String SCHEME_PATH        = "Schemes"        + File.separator;
//	private static final String STYLE_PATH         = "Styles"         + File.separator;

	private static final String RESOURCE_PATH = "resource/mmax/";

    private File basedataPath;
//	private File customizationPath;
	private File markablePath;
//	private File schemePath;
//	private File stylePath;

	private File path;

	public ProjectWriter(String pathStr) {
		this.path = new File(pathStr);

        basedataPath      = new File(path.getPath(), BASEDATA_PATH);
//		customizationPath = new File(path.getPath(), CUSTOMIZATION_PATH);
//		schemePath        = new File(path.getPath(), SCHEME_PATH);
//		stylePath         = new File(path.getPath(), STYLE_PATH);
		markablePath      = new File(path.getPath(), MARKABLE_PATH);
	}

	public void addProject(String filename, Basedata base, Graph graph,
			String level) throws TransformerConfigurationException,
			ParserConfigurationException, TransformerException,
			TransformerFactoryConfigurationError, SAXException, IOException, MMAXWriterException {

        base.save(new File(basedataPath, filename + ".xml"));
		createMMAXFile(filename);
		augmentProject(level);
		createMarkableFile(filename, base, graph, level);
	}

	private void augmentProject(String level)
			throws ParserConfigurationException, SAXException, IOException,
			TransformerConfigurationException, TransformerException,
			TransformerFactoryConfigurationError {

        Document compath = loadXML(new File(path, "common_paths.xml"));
		Element annotations = (Element) compath.getElementsByTagName("annotations").item(0);
		for (int i = 0; i < 10; i++) {
			String name = level + ">0." + i;
			String file = level + "_" + i;
			NodeList annos = annotations.getElementsByTagName("level");
			boolean found = false;
			for (int j = 0; j < annos.getLength(); j++) {
				if (annos.item(j).getAttributes().getNamedItem("name").getTextContent().equals(name)) {
					found = true;
					break;
				}
			}
			if (!found) {
				createEmpty("_" + file + ".xml");
				Element anno = compath.createElement("level");
				anno.setAttribute("name", name);
				anno.setAttribute("schemefile", "cohesion.xml");
				anno.setAttribute("customization_file", "cohesion_customization.xml");
				anno.appendChild(compath.createTextNode("$_" + file + ".xml"));
				annotations.appendChild(anno);
			}
		}
		saveXML(compath, new File(path, "common_paths.xml"), null, null);
	}

	private void copy(File source, File target) throws IOException {
        if (source.isFile() && !target.exists()) {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
			int i;
			while ((i = in.read()) != -1) {
				out.write(i);
			}
			in.close();
			out.close();
			System.out.println(target.getName() + " copied.");
		}
		if (source.isDirectory()) {
            target.mkdirs();
			File[] files = source.listFiles();
			for (File file : files) {
				copy(file, new File(target, file.getName()));
			}
		}
	}

	private void createEmpty(String append) throws IOException {
		File[] files = path.listFiles();
		for (File file : files) {
			String filename = file.getName();
			if (file.isFile() && filename.endsWith(".mmax")) {
				filename = filename.substring(0, filename.lastIndexOf("."));
				copy(new File(markablePath, "empty.xml"), new File(markablePath, filename + append));
			}
		}
	}

	@SuppressWarnings(value = "unchecked")
	private void createMarkableFile(String filename, Basedata base,
			Graph graph, String level) throws ParserConfigurationException,
			TransformerConfigurationException, TransformerException,
			TransformerFactoryConfigurationError, IOException {

        for (int i = 0; i < 10; i++) {
			double threshold = new Double(i) / 10;

			String MARKABLEID = "markable_";

			Document doc = createXML();
			Element toplevel = doc.createElement("markables");
			toplevel.setAttribute("xmlns", "de.tudarmstadt.dkpro/NameSpaces/cohesion");
			Set<Vertex> vertices = graph.getVertices();
			for (Vertex vertex : vertices) {
				Element elem = doc.createElement("markable");

				String cohNeighb = "";

				String cohSucc = "";
				Set<Edge> succs = vertex.getOutEdges();
				for (Edge succ : succs) {
					double comp = (Double) succ.getUserDatum("weight");
					if (comp > threshold) {
						String pointer = MARKABLEID + ((Vertex) succ.getEndpoints().getSecond()).getUserDatum("id") + ";";
						cohSucc += pointer;
						cohNeighb += pointer;
					}
				}
				Set<Edge> prevs = vertex.getInEdges();
				for (Edge prev : prevs) {
					double comp = (Double) prev.getUserDatum("weight");
					if (comp > threshold) {
						String pointer = MARKABLEID + ((Vertex) prev.getEndpoints().getFirst()).getUserDatum("id") + ";";
						cohNeighb += pointer;
					}
				}

				elem.setAttribute("id", MARKABLEID + vertex.getUserDatum("id"));
				elem.setAttribute("span", base.getId(((Token) vertex.getUserDatum("token")).getBegin()));
				elem.setAttribute("cohNeighb", cohNeighb);
				elem.setAttribute("cohSucc", cohSucc);
				elem.setAttribute("mmax_level", level + ">0." + i);
				toplevel.appendChild(elem);
			}
			doc.appendChild(toplevel);
			saveXML(doc, new File(markablePath, filename + "_" + level + "_" + i + ".xml"), OutputKeys.DOCTYPE_SYSTEM, "markables.dtd");
		}
	}

	private void createMMAXFile(String filename)
			throws TransformerConfigurationException, TransformerException,
			TransformerFactoryConfigurationError, ParserConfigurationException,
			IOException {

        Document doc = createXML();
		Element toplevel = doc.createElement("mmax_project");

		Element elem = doc.createElement("words");
		elem.appendChild(doc.createTextNode(filename + ".xml"));
		toplevel.appendChild(elem);

		doc.appendChild(toplevel);
		saveXML(doc, new File(path, filename + ".mmax"), null, null);
	}

	private Document createXML() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.newDocument();
	}

	public void initialize() throws IOException {
        try {
            copy(new File(RESOURCE_PATH), path);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
	}

	private Document loadXML(File file) throws ParserConfigurationException,
			SAXException, IOException {

        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return parser.parse(file);
	}

	private void saveXML(Document doc, File file, String outputkey,
			String doctype) throws TransformerConfigurationException,
			TransformerException, TransformerFactoryConfigurationError,
			IOException {

        DOMSource source = new DOMSource(doc);
		FileOutputStream stream = new FileOutputStream(file);
		StreamResult result = new StreamResult(stream);
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		if (doctype != null) {
			trans.setOutputProperty(outputkey, doctype);
		}
		trans.transform(source, result);
		stream.close();
	}
}
