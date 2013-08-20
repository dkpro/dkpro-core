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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.annotation.markables.MarkablePointer;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Writes the CAS into a MMAX2 project.
 * The MMAX2 project can then be used to annotate the annotations further.
 *
 * All the settings (which annotations should be included,
 * how they should be treated in the MMAX2 project, etc.)
 * are to be made in the empty (but fully configured) MMAX2 project that is then
 * filled with the data from the CAS.
 * This requires some knowledge of MMAX2.
 *
 * @author zesch
 * @author ferschke
 *
 */
// TODO add parameter for path to MMAX project, as this needs to be adapted to the task anyway
public class MmaxWriter {
	private final Log log = LogFactory.getLog(getClass());

    public enum StartUpMode {
        visible,
        inactive
    }

    public static final String BASEDATA_PATH      = "Basedata"       + File.separator;
    public static final String CUSTOMIZATION_PATH = "Customizations" + File.separator;
    public static final String MARKABLE_PATH      = "Markables"      + File.separator;
    public static final String SCHEME_PATH        = "Schemes"        + File.separator;
    public static final String STYLE_PATH         = "Styles"         + File.separator;

    // The path to the source mmax project template
    private static final String SOURCE_PATH_STRING = "resource/mmax/";

    private File basedataPath;
    private File customizationPath;
    private File markablePath;
    private File schemePath;
    private File stylePath;

    private Basedata basedata;

    // The path to the mmax project
    private File projectPath;

    // the common_paths.xml file
    private File commonPathsFile;

    private MMAX2Discourse discourse;

    public MmaxWriter(File outputPath) throws MmaxWriterException {
        this.projectPath = outputPath;

        this.basedata = new Basedata();

        commonPathsFile = new File(this.projectPath, "common_paths.xml");

        if (!commonPathsFile.exists()) {
            try {
                copy(new File(SOURCE_PATH_STRING), projectPath);
            } catch (IOException e) {
                throw new MmaxWriterException(e);
            }
        }

        basedataPath      = new File(projectPath.getPath(), BASEDATA_PATH);
        customizationPath = new File(projectPath.getPath(), CUSTOMIZATION_PATH);
        schemePath        = new File(projectPath.getPath(), SCHEME_PATH);
        stylePath         = new File(projectPath.getPath(), STYLE_PATH);
        markablePath      = new File(projectPath.getPath(), MARKABLE_PATH);
    }

    public void clearFiles(){
    	for(File f : basedataPath.listFiles()){
    		if(!f.getName().endsWith(".dtd")) {
    			log.info("Deleting: "+f.getPath().toString());
				f.delete();
			}
    	}
    	for(File f : projectPath.listFiles()){
    		if(f.getName().endsWith(".mmax")) {
    			log.info("Deleting: "+f.getPath().toString());
				f.delete();
			}
    	}
    	for(File f : markablePath.listFiles()){
    		if(!f.getName().endsWith(".dtd")) {
    			log.info("Deleting: "+f.getPath().toString());
    			f.delete();
			}
    	}
    	for(File f : projectPath.listFiles()){
    		if(f.isDirectory()&&!
    				(f.getName().equals(basedataPath.getName())
    						||f.getName().equals(customizationPath.getName())
    						||f.getName().equals(schemePath.getName())
    						||f.getName().equals(stylePath.getName())
    						||f.getName().equals(markablePath.getName())
    				)
    			){
				log.info("Deleting:"+f.getPath().toString());
    			deleteDir(f);
    		}
    	}
    }

    public void clearBasedata() {
        this.basedata = new Basedata();
    }

    public void appendBasedata(String term, int startOffset) {
        basedata.append(term, startOffset);
    }

    public String getBasedataId(int offset) {
        return basedata.getId(offset);
    }

    public String createMMAXFile(String mmaxFilename) throws MmaxWriterException {

        log.info("Writing Basedata");
        File basedataFile = new File(basedataPath, mmaxFilename + ".xml");
        basedata.save(basedataFile);

        Document doc = createXML();
        Element toplevel = doc.createElement("mmax_project");

        Element elem = doc.createElement("words");
        elem.appendChild(doc.createTextNode(mmaxFilename + ".xml"));
        toplevel.appendChild(elem);

        File mmaxFile = new File(projectPath, mmaxFilename + ".mmax");
        doc.appendChild(toplevel);
        saveXML(doc, mmaxFile, null, null);

        // load the current discourse
        loadDiscourse(mmaxFile.getPath());

        return basedataFile.getName();
    }

    private void loadDiscourse(String infile) {
    	log.info("Loading discourse from: "+infile);
    	discourse = MMAX2Discourse.buildDiscourse(infile, commonPathsFile.getPath());
    }

    public void registerMarkableLevel(String levelname, String schemeFilename, String customizationFilename) throws MmaxWriterException {

        Document compath = loadXML(commonPathsFile);

        Element annotations = (Element) compath.getElementsByTagName("annotations").item(0);
        NodeList annos = annotations.getElementsByTagName("level");
        boolean found = false;
        for (int j = 0; j < annos.getLength(); j++) {
            if (annos.item(j).getAttributes().getNamedItem("name").getTextContent().equals(levelname)) {
                found = true;
                System.err.println("Level " + levelname + " already exists.");
                break;
            }
        }

        // if the level is not already present in the commons_path file, add it
        if (!found) {
            Element anno = compath.createElement("level");
            anno.setAttribute("name", levelname);
            anno.setAttribute("schemefile", schemeFilename);
            anno.setAttribute("customization_file", customizationFilename);
            anno.appendChild(compath.createTextNode("$_" + levelname + ".xml"));
            annotations.appendChild(anno);
        }

        saveXML(compath, commonPathsFile, null, null);
    }

    private MarkableLevel getMarkableLevel(String levelName) {
        return discourse.getMarkableLevelByName(levelName, false);
    }

    public Markable addMarkable(Node node, String id, MarkableLevel level) {
        return new Markable(node, id, null, null, level);
    }

    public Markable addMarkable(String levelName, String[] ids, HashMap attributes) throws MmaxWriterException {
        MarkableLevel level = getMarkableLevel(levelName);
        if (level == null) {
            throw new MmaxWriterException("Could not get level: " + levelName);
        }
        Markable markable = level.addMarkable(ids, attributes);
        return markable;
    }

    public MarkablePointer addMarkablePointer(Markable m1, Markable m2) {
        return null;
    }

    public void saveMarkables(String levelName) {
        getMarkableLevel(levelName).saveMarkables("");
    }

    private Document loadXML(File file) throws MmaxWriterException {
        DocumentBuilder parser;
        Document doc;
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = parser.parse(file);
        } catch (ParserConfigurationException e) {
            throw new MmaxWriterException(e);
        } catch (SAXException e) {
            throw new MmaxWriterException(e);
        } catch (IOException e) {
            throw new MmaxWriterException(e);
        }
        return doc;
    }

    private void saveXML(Document doc, File file, String outputkey, String doctype) throws MmaxWriterException {

        try {
            DOMSource source = new DOMSource(doc);
            FileOutputStream stream = new FileOutputStream(file);
            StreamResult result = new StreamResult(stream);
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            if (doctype != null) {
                trans.setOutputProperty(outputkey, doctype);
            }
            trans.transform(source, result);
            stream.close();
        } catch (FileNotFoundException e) {
            throw new MmaxWriterException(e);
        } catch (TransformerConfigurationException e) {
            throw new MmaxWriterException(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new MmaxWriterException(e);
        } catch (TransformerException e) {
            throw new MmaxWriterException(e);
        } catch (IOException e) {
            throw new MmaxWriterException(e);
        }
    }

    public Document createXML() throws MmaxWriterException  {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new MmaxWriterException(e);
        }
        return builder.newDocument();
    }

    /**
     * Copies a source folder/file to a target folder/file. Used to duplicate the template project and template files.
     * @param source
     * @param target
     * @throws IOException
     */
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
            log.trace(target.getName() + " copied.");
        }
        if (source.isDirectory()) {
            target.mkdirs();
            File[] files = source.listFiles();
            for (File file : files) {
                if (!file.getName().endsWith(".svn")) { // do not copy svn files!
                    copy(file, new File(target, file.getName()));
                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
	     if (dir.isDirectory()) {
	         for (File element : dir.listFiles()) {
	             boolean success = deleteDir(element);
	             if (!success) {
	                 return false;
	             }
	         }
	     }
	     return dir.delete();
	 }
}
