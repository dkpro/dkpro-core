/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.testing.validation;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

public class CasValidatorComponent
    extends JCasConsumer_ImplBase
{
    private static CasValidator validator = CasValidator.createWithAllChecks();

    /**
     * If set to true component will throw an exception when cas contains errors.
     */
    public static final String PARAM_STRICT_CHECK = "strictCheck";
    @ConfigurationParameter(name = PARAM_STRICT_CHECK, mandatory = false, defaultValue = "false")
    protected boolean strictCheck;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        getLogger().info("CasValidation process started.");
        List<Message> messages = validator.analyze(aJCas);
        getLogger().info("CasValidation process finished.");
        if (messages.isEmpty()) {
            getLogger().info("CasValidation was successful!");
        }
        else {
            getLogger().warn("CasValidator found " + messages.size() + " issues.");
            for (Message message : messages) {
                switch (message.level) {
                case INFO:
                    getLogger().info(message.message);
                    break;
                case ERROR:
                    getLogger().error(message.message);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected value of message.level ["
                            + message.level + "] encountered!");
                }
            }

            if (strictCheck) {
                throw new IllegalArgumentException(
                        messages.size() + " errors were found in the jcas. You should fix them "
                                + "to be able to run the pipeline.");
            }
        }
    }
}
