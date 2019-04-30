/*
 * Copyright 2007-2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.io.tgrep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionMethod;
import org.dkpro.core.api.resources.RuntimeProvider;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import eu.openminted.share.annotations.api.Parameters;

/**
 * TGrep2 corpus file writer. Requires {@link PennTree}s to be annotated before.
 */
@ResourceMetaData(name = "TGrep2 Writer")
@MimeTypeCapability({MimeTypes.APPLICATION_X_TGREP2})
@Parameters(
        exclude = { 
                TGrepWriter.PARAM_TARGET_LOCATION  })
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree"})
public class TGrepWriter
    extends JCasAnnotator_ImplBase
{
    /**
     * Path to which the output is written.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File outputPath;

    /**
     * Set this parameter to true if you want to add a comment to each PennTree which is written to
     * the output files. The comment is of the form {@code documentId,beginOffset,endOffset}.
     */
    public static final String PARAM_WRITE_COMMENTS = "writeComments";
    @ConfigurationParameter(name = PARAM_WRITE_COMMENTS, mandatory = true, defaultValue = "true")
    private boolean writeComments;

    /**
     * Set this parameter to true if you want to encode directly into the tgrep2 binary format.
     */
    public static final String PARAM_WRITE_T2C = "writeT2c";
    @ConfigurationParameter(name = PARAM_WRITE_T2C, mandatory = true, defaultValue = "true")
    private boolean writeT2c;

    /**
     * Method to compress the tgrep file (only used if PARAM_WRITE_T2C is true). Only NONE, GZIP and
     * BZIP2 are supported.
     *
     * @see CompressionMethod
     */
    public static final String PARAM_COMPRESSION = "compression";
    @ConfigurationParameter(name = PARAM_COMPRESSION, mandatory = true, defaultValue = "NONE")
    private CompressionMethod compression;

    /**
     * If true, silently drops malformed Penn Trees instead of throwing an exception.
     */
    public static final String PARAM_DROP_MALFORMED_TREES = "dropMalformedTrees";
    @ConfigurationParameter(name = PARAM_DROP_MALFORMED_TREES, mandatory = true, defaultValue = "false")
    private boolean dropMalformedTrees;

    private static final String EXT_CORPUS = ".txt";
    private static final String EXT_BINARY = ".t2c";

    private Map<String, PrintWriter> writers;
    private File tgrep2File;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        if (compression != CompressionMethod.NONE && compression != CompressionMethod.GZIP
                && compression != CompressionMethod.BZIP2) {
            throw new ResourceInitializationException(new IllegalArgumentException(
                    "Only gzip and bzip2 compression are supported by TGrep2, but [" + compression
                            + "] was specified."));
        }

        try {
            FileUtils.forceMkdir(outputPath);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        writers = new HashMap<String, PrintWriter>();
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        String filename;
        String collectionId;
        String documentId;

        try {
            DocumentMetaData meta = DocumentMetaData.get(aJCas);
            collectionId = meta.getCollectionId();
            documentId = meta.getDocumentId();
        }
        catch (IllegalArgumentException e) {
            getLogger().warn("No DocumentMetaData found.");
            collectionId = "defaultCollectionId";
            documentId = "defaultDocumentId";
        }

        // if the collectionId contains inconvenient characters, remove them for the filename
        // filename = collectionId;
        filename = collectionId.replaceAll("\\W", "");

        try {
            PrintWriter pw = writers.get(filename);
            if (pw == null) {
                pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(
                        outputPath, filename + EXT_CORPUS)), "UTF-8"));
                writers.put(filename, pw);
            }

            for (PennTree pt : JCasUtil.select(aJCas, PennTree.class)) {
                String tree = StringUtils.normalizeSpace(pt.getPennTree());
                // detect and handle malformed trees
                if (!isTermiteFree(tree)) {
                    if (dropMalformedTrees) {
                        getLogger().warn("Dropping malformed tree: [" + tree + "].");
                        continue;
                    }
                    else {
                        throw new AnalysisEngineProcessException(new IllegalArgumentException(
                                "Found malformed tree: [" + tree + "]."));
                    }
                }
                // write comments and trees
                if (writeComments) {
                    pw.printf("# %s,%d,%d\n", documentId, pt.getBegin(), pt.getEnd());
                }
                pw.printf("%s\n", tree);
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Check if a given Penn tree will be rejected by TGrep2.
     *
     * @param aTree
     *            the Penn tree to check
     * @return true if aTree is fit for use with Tgrep2, false otherwise
     */
    private boolean isTermiteFree(String aTree)
    {
        int bracketCount = 0;
        boolean justOpened = false;

        if (aTree.isEmpty() || aTree.charAt(0) != '(') {
            return false;
        }

        for (int idx = 0; idx < aTree.length(); idx++) {
            char c = aTree.charAt(idx);
            switch (c) {
            case '(':
                bracketCount++;
                if (justOpened) {
                    // "((" is illegal, also with spaces in between
                    return false;
                }
                justOpened = true;
                break;
            case ' ':
                break;
            case ')':
                bracketCount--;
                if (justOpened) {
                    // "()" is illegal, also with spaces in between
                    return false;
                }
                if (bracketCount < 0) {
                    // more closing than opening brackets at any point are illegal
                    return false;
                }
                justOpened = false;
                break;
            default:
                justOpened = false;
                break;
            }
        }
        // if not all brackets are closed, the next sentence is thought to be part of this one
        // we consider these cases as illegal, as the files are usually built one sentence/line
        return bracketCount == 0;
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        for (PrintWriter pw : writers.values()) {
            IOUtils.closeQuietly(pw);
        }

        if (writeT2c) {
            RuntimeProvider runtime = new RuntimeProvider(
                    "classpath:/de/tudarmstadt/ukp/dkpro/core/io/tgrep/bin/");
            try {
                tgrep2File = runtime.getFile("tgrep2");
                for (String filename : writers.keySet()) {
                    writeTgrepBinary(filename);
                }
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
            finally {
                runtime.uninstall();
            }
        }
    }

    /**
     * Produces a TGrep2 binary corpus file.
     *
     * @param aFilename
     *            the name of the file from which a corpus file shall be created, without extension
     * @throws IOException
     *             if the employed tgrep2 process is interrupted or if it reports an error
     */
    private void writeTgrepBinary(String aFilename)
        throws IOException
    {
        List<String> cmd = new ArrayList<String>();
        cmd.add(tgrep2File.getAbsolutePath());
        if (writeComments) {
            // enable writing comments
            cmd.add("-C");
        }
        // specify corpus
        cmd.add("-p");
        cmd.add(new File(outputPath, aFilename + EXT_CORPUS).getAbsolutePath());
        cmd.add(new File(outputPath, aFilename + EXT_BINARY + compression.getExtension())
                .getAbsolutePath());

        getLogger().info("Running tgrep2 command: [" + StringUtils.join(cmd, " ") + "].");

        Process tgrepProcess = null;
        try {
            tgrepProcess = new ProcessBuilder(cmd).start();
            tgrepProcess.waitFor();
        }
        catch (InterruptedException e) {
            throw new IOException();
        }
        finally {
            if (tgrepProcess != null) {
                InputStream stderr = tgrepProcess.getErrorStream();
                if (stderr.available() > 0) {
                    byte[] data = new byte[stderr.available()];
                    stderr.read(data);
                    String error = new String(data, "UTF-8");
                    getLogger().error(error);
                    throw new IOException(error);
                }
            }
        }
    }
}
