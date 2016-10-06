/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.lxf.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.lxf.internal.model.LxfGraph;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;

public class DKProRunScript
{

    private static final String TOOL_OPTION = "tool-class";
    private static final String[] params = new String[] { ComponentParameters.PARAM_VARIANT,
            ComponentParameters.PARAM_WRITE_CONSTITUENT, ComponentParameters.PARAM_WRITE_DEPENDENCY,
            ComponentParameters.PARAM_READ_POS, ComponentParameters.PARAM_WRITE_POS,
            ComponentParameters.PARAM_NUM_THREADS, StanfordParser.PARAM_KEEP_PUNCTUATION,
            StanfordParser.PARAM_MODE };

    public static Options options()
    {
        Options options = new Options();

        Option option = new Option(null, TOOL_OPTION, true, "");
        option.setRequired(true);
        options.addOption(option);

        option = new Option(null, ComponentParameters.PARAM_LANGUAGE, true, "");
        option.setRequired(true);
        options.addOption(option);

        for (String p : params) {
            option = new Option(null, p, true, "");
            option.setRequired(false);
            options.addOption(option);
        }

        return options;
    }

    public static void main(String[] args)
        throws Exception
    {

        Options options = options();

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        if (args.length == 0) {
            formatter.printHelp(
                    "DKProScript --tool-class <tool> --language <en/no> [input] [output]?",
                    options);
            return;
        }

        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("DKProScript", options);

            System.exit(1);
            return;
        }

        if (cmd.getArgs().length < 1) {
            System.err.println("The script requires input");
            formatter.printHelp("DKProScript", options);
            System.exit(1);
            return;
        }

        String language = cmd.getOptionValue(ComponentParameters.PARAM_LANGUAGE);
        String inputFile = cmd.getArgs()[0];
        File in = new File(inputFile);
        if (!in.exists()) {
            System.err.println("Input file \'" + inputFile + "' does not exist!");
            System.exit(1);
        }

        String outputFile = null;
        File out = null;
        if (cmd.getArgs().length > 1) {
            outputFile = cmd.getArgs()[1];
            out = new File(outputFile);
        }
        AnalysisEngine tool = createEngineFromCommand(cmd);
        run(in, out, tool, cmd.getOptionValue(TOOL_OPTION), language);

    }

    public static AnalysisEngine createEngineFromCommand(CommandLine cmd)
        throws Exception
    {
        String tool = cmd.getOptionValue(TOOL_OPTION);
        tool = tool.replace("_", ".");
        @SuppressWarnings("unchecked")
        Class<AnalysisComponent> c = (Class<AnalysisComponent>) Class.forName(tool);

        AnalysisEngineDescription description = null;

        List<Object> paramsList = Lists.newArrayList();

        if (cmd.hasOption(ComponentParameters.PARAM_VARIANT)) {
            paramsList.add(ComponentParameters.PARAM_VARIANT);
            String value = cmd.getOptionValue(ComponentParameters.PARAM_VARIANT);
            paramsList.add(value);
        }

        for (String p : params) {
            if (cmd.hasOption(p)) {
                String value = cmd.getOptionValue(p);

                Object o = readBooleanValue(value);
                paramsList.add(p);
                paramsList.add(o);

                if (p.equalsIgnoreCase(ComponentParameters.PARAM_READ_POS)) {
                    Boolean v = (Boolean) o;
                    if (!cmd.hasOption(ComponentParameters.PARAM_WRITE_POS)) {
                        paramsList.add(ComponentParameters.PARAM_WRITE_POS);
                        paramsList.add(!v);
                    }
                    else {
                        Boolean vs = (Boolean) readBooleanValue(
                                ComponentParameters.PARAM_WRITE_POS);
                        if (vs == v) {
                            System.err.println(ComponentParameters.PARAM_WRITE_POS + " and "
                                    + ComponentParameters.PARAM_READ_POS
                                    + "  must have oposite values");
                            System.exit(1);
                        }
                    }
                }
                if (p.equalsIgnoreCase(ComponentParameters.PARAM_WRITE_POS)) {
                    Boolean v = (Boolean) o;
                    if (!cmd.hasOption(ComponentParameters.PARAM_READ_POS)) {
                        paramsList.add(ComponentParameters.PARAM_READ_POS);
                        paramsList.add(!((Boolean) o));
                    }
                    else {
                        Boolean vs = (Boolean) readBooleanValue(ComponentParameters.PARAM_READ_POS);
                        if (vs == v) {
                            System.err.println(ComponentParameters.PARAM_WRITE_POS + " and "
                                    + ComponentParameters.PARAM_READ_POS
                                    + "  must have oposite values");
                            System.exit(1);
                        }

                    }
                }
            }
        }
        description = AnalysisEngineFactory.createEngineDescription(c, paramsList.toArray());

        return AnalysisEngineFactory.createEngine(description);
    }

    private static Object readBooleanValue(String value)
    {
        Object o = value;
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
            o = Boolean.parseBoolean(value);
        if (value.equalsIgnoreCase("1"))
            o = Boolean.TRUE;
        if (value.equalsIgnoreCase("0"))
            o = Boolean.FALSE;
        return o;
    }

    public static void run(File in, File out, AnalysisEngine engine, String toolid, String language)
        throws IOException, UIMAException
    {

        ObjectMapper mapper = new ObjectMapper();
        // Deserialize LXF
        LxfGraph inLxf;
        InputStream is = new FileInputStream(in);
        inLxf = mapper.readValue(is, LxfGraph.class);

        // Convert LXF to CAS
        JCas jcas = JCasFactory.createJCas();

        if (language != null)
            jcas.setDocumentLanguage(language);

        Lxf2DKPro.convert(inLxf, jcas);
        jcas.getCasImpl().createMarker();

        engine.process(jcas);

        // Convert CAS to LXF
        LxfGraph outLxf = new LxfGraph();
        DKPro2Lxf.convert(jcas, inLxf, outLxf, DKPro2Lxf.createIdMap(toolid, inLxf));
        mapper.setSerializationInclusion(Include.NON_NULL);
        if (out == null)
            mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, outLxf);
        else
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, outLxf);

        engine.destroy();
        jcas.reset();
        jcas = null;
        System.gc();
    }

}
