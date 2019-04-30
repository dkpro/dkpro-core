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
package org.dkpro.core.testing;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import org.dkpro.core.testing.validation.checks.Check;

public class TestOptions
{
    Set<Class<? extends Check>> skippedChecks = new HashSet<>();
    BiConsumer<File, File> resultAssertor;
    
    public TestOptions skipCheck(Class<? extends Check> aCheck)
    {
        skippedChecks.add(aCheck);
        return this;
    }
    
    public TestOptions resultAssertor(BiConsumer<File, File> aResultComparator)
    {
        resultAssertor = aResultComparator;
        return this;
    }
}
