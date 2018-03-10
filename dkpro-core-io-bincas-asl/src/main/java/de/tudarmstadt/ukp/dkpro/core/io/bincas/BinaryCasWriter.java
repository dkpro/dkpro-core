/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.bincas;

import static org.apache.uima.cas.SerialFormat.BINARY;
import static org.apache.uima.cas.SerialFormat.COMPRESSED;
import static org.apache.uima.cas.SerialFormat.COMPRESSED_FILTERED;
import static org.apache.uima.cas.SerialFormat.COMPRESSED_FILTERED_TS;
import static org.apache.uima.cas.SerialFormat.COMPRESSED_FILTERED_TSI;
import static org.apache.uima.cas.SerialFormat.COMPRESSED_TSI;
import static org.apache.uima.cas.SerialFormat.SERIALIZED;
import static org.apache.uima.cas.SerialFormat.SERIALIZED_TSI;
import static org.apache.uima.cas.impl.Serialization.serializeCASMgr;
import static org.apache.uima.cas.impl.Serialization.serializeWithCompression;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.SerialFormat;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASMgrSerializer;
import org.apache.uima.cas.impl.CASSerializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasIOUtils;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

/**
 * <p>
 * Write CAS in one of the UIMA binary formats.
 * </p>
 * 
 * <p>
 * All the supported formats except <code>6+</code> can also be loaded and saved via the UIMA
 * {@link CasIOUtils}.
 * </p>
 * 
 * <table>
 * <caption>Supported formats</caption>
 * <tr>
 * <th>Format</th>
 * <th>Description</th>
 * <th>Type system on load</th>
 * <th>CAS Addresses preserved</th>
 * </tr>
 * <tr>
 * <td><code>SERIALIZED</code> or <code>S</code></td>
 * <td>CAS structures are dumped to disc as they are using Java serialization ({@link CASSerializer}
 * ). Because these structures are pre-allocated in memory at larger sizes than what is actually
 * required, files in this format may be larger than necessary. However, the CAS addresses of
 * feature structures are preserved in this format. When the data is loaded back into a CAS, it must
 * have been initialized with the same type system as the original CAS.</td>
 * <td>must be the same</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <td><code>SERIALIZED_TSI</code> or <code>S+</code></td>
 * <td>CAS structures are dumped to disc as they are using Java serialization as in form 0, but now
 * using the {@link CASCompleteSerializer} which includes CAS metadata like type system and index
 * repositories.</td>
 * <td>is reinitialized</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <td><code>BINARY</code> or 0</td>
 * <td>CAS structures are dumped to disc as they are using Java serialization ({@link CASSerializer}
 * ). This is basically the same as format {@code S} but includes a UIMA header and can be read
 * using {@link org.apache.uima.cas.impl.Serialization#deserializeCAS}.</td>
 * <td>must be the same</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <td><code>BINARY_TSI</code> or 0</td>
 * <td>The same as <code>BINARY_TSI</code>, except that the type system and index configuration are
 * also stored in the file. However, lenient loading or reinitalizing the CAS with this information
 * is presently not supported.</td>
 * <td>must be the same</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <td><code>COMPRESSED</code> or <code>4</code></td>
 * <td>UIMA binary serialization saving all feature structures (reachable or not). This format
 * internally uses gzip compression and a binary representation of the CAS, making it much more
 * efficient than format 0.</td>
 * <td>must be the same</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <td><code>COMPRESSED_FILTERED</code> or <code>6</code></td>
 * <td>UIMA binary serialization as format 4, but saving only reachable feature structures.</td>
 * <td>must be the same</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td>6+</td>
 * <td><b>This is a legacy format specific to DKPro Core.</b> Since UIMA 2.9.0,
 * <code>COMPRESSED_FILTERED_TSI</code> is supported and should be used instead of this format. UIMA
 * binary serialization as format 6, but also contains the type system definition. This allows the
 * {@link BinaryCasReader} to load data leniently into a CAS that has been initialized with a
 * different type system.</td>
 * <td>lenient loading</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td><code>COMPRESSED_FILTERED_TS</code></td>
 * <td>Same as <code>COMPRESSED_FILTERED</code>, but also contains the type system definition. This
 * allows the {@link BinaryCasReader} to load data leniently into a CAS that has been initialized
 * with a different type system.</td>
 * <td>lenient loading</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td><code>COMPRESSED_FILTERED_TSI</code></td>
 * <td><b>Default</b>. UIMA binary serialization as format 6, but also contains the type system
 * definition and index definitions. This allows the {@link BinaryCasReader} to load data leniently
 * into a CAS that has been initialized with a different type system.</td>
 * <td>lenient loading</td>
 * <td>no</td>
 * </tr>
 * </table>
 * 
 * @see <a href="http://uima.apache.org/d/uimaj-2.9.0/references.html#ugr.ref.compress">Compressed
 *      Binary CASes</a>
 */
@ResourceMetaData(name = "UIMA Binary CAS Writer")
@MimeTypeCapability({ MimeTypes.APPLICATION_X_UIMA_BINARY })
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class BinaryCasWriter
    extends JCasFileWriter_ImplBase
{
    public static final String AUTO = "AUTO";
    
    /**
     * Location to write the type system to. The type system is saved using Java serialization, it
     * is not saved as a XML type system description. We recommend to use the name
     * {@code typesystem.ser}.
     * <br>
     * The {@link #PARAM_COMPRESSION} parameter has no effect on the
     * type system. Instead, if the type system file should be compressed or not is detected from
     * the file name extension (e.g. ".gz").
     * <br>
     * If this parameter is set, the type system and index repository are no longer serialized into
     * the same file as the test of the CAS. The {@link SerializedCasReader} can currently not
     * read such files. Use this only if you really know what you are doing.
     * <br>
     * This parameter has no effect if formats S+ or 6+ are used as the type system information
     * is embedded in each individual file. Otherwise, it is recommended that this parameter be
     * set unless some other mechanism is used to initialize the CAS with the same type system and
     * index repository during reading that was used during writing.
     */
    public static final String PARAM_TYPE_SYSTEM_LOCATION = "typeSystemLocation";
    @ConfigurationParameter(name = PARAM_TYPE_SYSTEM_LOCATION, mandatory = false)
    private String typeSystemLocation;

    public static final String PARAM_FORMAT = "format";
    @ConfigurationParameter(name = PARAM_FORMAT, mandatory = true, defaultValue = "COMPRESSED_FILTERED_TSI")
    private String format;

    /**
     * The file extension. If this is set to {@link AUTO}, then the extension will be chosen based
     * on the default extension specified by the UIMA {@link SerialFormat} class. However, this
     * only works when using the new long format names (e.g. <code>COMPRESSED_FILTERED_TSI</code>).
     * When using the old short names (e.g. <code>6</code>), the default extension <i>.bin</i> is
     * used.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = AUTO)
    private String filenameExtension;

    private boolean typeSystemWritten;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        if (AUTO.equals(filenameExtension)) {
            try {
                filenameExtension = SerialFormat.valueOf(format).getDefaultFileExtension();
            }
            catch (IllegalArgumentException e) {
                filenameExtension = ".bin";
            }
        }
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try (NamedOutputStream docOS = getOutputStream(aJCas, filenameExtension)) {
            if ("S".equals(format) || SERIALIZED.toString().equals(format)) {
                // Java-serialized CAS without type system
                getLogger().debug("Writing CAS to [" + docOS + "]");
                //                 CASSerializer serializer = new CASSerializer();
//                 serializer.addCAS(aJCas.getCasImpl());
//                 ObjectOutputStream objOS = new ObjectOutputStream(docOS);
//                 objOS.writeObject(serializer);
//                 objOS.flush();
                CasIOUtils.save(aJCas.getCas(), docOS, SERIALIZED);
            }
            else if ("S+".equals(format) || SERIALIZED_TSI.toString().equals(format)) {
                // Java-serialized CAS with type system
//                ObjectOutputStream objOS = new ObjectOutputStream(docOS);
//                CASCompleteSerializer serializer = serializeCASComplete(aJCas.getCasImpl());
//                objOS.writeObject(serializer);
//                objOS.flush();
                CasIOUtils.save(aJCas.getCas(), docOS, SERIALIZED_TSI);
                typeSystemWritten = true; // Embedded type system
            }
            else if ("0".equals(format) || BINARY.toString().equals(format)) {
                // Java-serialized CAS without type system
                // serializeCAS(aJCas.getCas(), docOS);
                CasIOUtils.save(aJCas.getCas(), docOS, BINARY);
            }
            else if (BINARY.toString().equals(format)) {
                // Java-serialized CAS without type system
                CasIOUtils.save(aJCas.getCas(), docOS, SerialFormat.BINARY_TSI);
            }
            else if ("4".equals(format) || COMPRESSED.toString().equals(format)) {
                // Binary compressed CAS without type system (form 4)
                // serializeWithCompression(aJCas.getCas(), docOS);
                CasIOUtils.save(aJCas.getCas(), docOS, COMPRESSED);
            }
            else if (COMPRESSED_TSI.toString().equals(format)) {
                CasIOUtils.save(aJCas.getCas(), docOS, COMPRESSED_TSI);
            }
            else if (format.equals("6") || COMPRESSED_FILTERED.toString().equals(format)) {
                CasIOUtils.save(aJCas.getCas(), docOS, COMPRESSED_FILTERED);
            }
            else if (COMPRESSED_FILTERED_TS.toString().equals(format)) {
                CasIOUtils.save(aJCas.getCas(), docOS, COMPRESSED_FILTERED_TS);
            }
            else if (COMPRESSED_FILTERED_TSI.toString().equals(format)) {
                CasIOUtils.save(aJCas.getCas(), docOS, COMPRESSED_FILTERED_TSI);
            }
            else if (format.equals("6+")) {
                // LEGACY ... with embedded Java-serialized type system DKPro-Style
                writeHeader(docOS);
                writeTypeSystem(aJCas, docOS);
                typeSystemWritten = true; // Embedded type system
                serializeWithCompression(aJCas.getCas(), docOS, (TypeSystem) null);
            }
            else {
                throw new IllegalArgumentException("Unknown format [" + format
                        + "]. Must be S, S+, 0, 4, 6, or 6+");
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        // To support writing to ZIPs, the type system must be written separately from the CAS data
        try {
            if (typeSystemLocation != null && !typeSystemWritten) {
                writeTypeSystem(aJCas);
                typeSystemWritten = true;
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void writeTypeSystem(JCas aJCas)
        throws IOException
    {
        // If the type system location is an absolute file system location, write it there,
        // otherwise use the default storage which places the file relative to the target location
        if (!typeSystemLocation.startsWith(JAR_PREFIX)
                && new File(typeSystemLocation).isAbsolute()) {
            try (OutputStream typeOS = CompressionUtils
                    .getOutputStream(new File(typeSystemLocation))) {
                getLogger().debug("Writing type system to [" + typeSystemLocation + "]");
                writeTypeSystem(aJCas, typeOS);
            }
        }
        else {
            try (NamedOutputStream typeOS = getOutputStream(typeSystemLocation, "")) {
                getLogger().debug("Writing type system to [" + typeOS + "]");
                writeTypeSystem(aJCas, typeOS);
            }
        }
    }
   
    private void writeHeader(OutputStream aOS)
        throws IOException
    {
        byte[] header = new byte[] { 'D', 'K', 'P', 'r', 'o', '1' };
        DataOutputStream dataOS = new DataOutputStream(aOS);
        dataOS.write(header);
        dataOS.flush();
    }

    private void writeTypeSystem(JCas aJCas, OutputStream aOS)
        throws IOException
    {
        ObjectOutputStream typeOS = new ObjectOutputStream(aOS);
        CASMgrSerializer casMgrSerializer = serializeCASMgr(aJCas.getCasImpl());
        typeOS.writeObject(casMgrSerializer);
        typeOS.flush();
    }
}
