/*******************************************************************************
 * Copyright 2015
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.testing.validation;

import static java.util.Arrays.asList;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.uima.jcas.JCas;
import org.reflections.Reflections;

import de.tudarmstadt.ukp.dkpro.core.testing.validation.checks.Check;

public class CasValidator
{
    private Set<Class<? extends Check>> checks = new LinkedHashSet<>();

    public CasValidator()
    {
        // Nothing to do
    }
    
    @SafeVarargs
    public CasValidator(Class<? extends Check>... aChecks)
    {
        setChecks(aChecks);
    }
    
    public void setChecks(Collection<Class<? extends Check>> aChecks)
    {
        checks = new LinkedHashSet<>();
        if (aChecks != null) {
            checks.addAll(aChecks);
        }
    }

    public void setChecks(Class<? extends Check>... aChecks)
    {
        checks = new LinkedHashSet<>();
        if (aChecks != null) {
            checks.addAll(asList(aChecks));
        }
    }

    public List<Message> analyze(JCas aCas)
    {
        List<Message> messages = new ArrayList<>();
        analyze(aCas, messages);
        return messages;
    }

    private boolean analyze(JCas aCas, List<Message> aMessages)
    {
        boolean ok = true;
        for (Class<? extends Check> checkClass : checks) {
            try {
                Check check = checkClass.newInstance();
                ok &= check.check(aCas, aMessages);
            }
            catch (InstantiationException | IllegalAccessException e) {
                aMessages.add(new Message(this, Message.Level.ERROR,
                        "Cannot instantiate [%s]: %s", checkClass.getSimpleName(), ExceptionUtils
                                .getRootCauseMessage(e)));
            }
        }

        return ok;
    }
    
    public static CasValidator createWithAllChecks()
    {
        Reflections reflections = new Reflections(Check.class.getPackage().getName());
        CasValidator validator = new CasValidator();
        validator.setChecks(reflections.getSubTypesOf(Check.class).stream()
                .filter(c -> !Modifier.isAbstract(c.getModifiers())).collect(Collectors.toList()));
        return validator;
    }

    public void addCheck(Class<? extends Check> aExtra)
    {
        checks.add(aExtra);
    }

    public void removeCheck(Class<? extends Check> aCheck)
    {
        checks.remove(aCheck);
    }
}
