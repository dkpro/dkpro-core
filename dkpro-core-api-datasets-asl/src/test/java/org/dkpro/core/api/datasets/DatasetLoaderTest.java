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
package org.dkpro.core.api.datasets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Disabled("Normally we do not run this")
public class DatasetLoaderTest
{
    @Test
    public void testUniversalDependencyTreebankV1_3(@TempDir File tempDir)
        throws Exception
    {
        List<Dataset> dss = new DatasetLoader(tempDir)
                .loadUniversalDependencyTreebankV1_3();
        for (Dataset ds : dss) {
            assertDatasetOk(ds);
        }
    }
    private void assertDatasetOk(Dataset ds)
    {
        assertThat(ds.getName()).isNotNull();
        assertThat(ds.getLanguage()).isNotNull();
        Split split = ds.getDefaultSplit();
        assertNullOrExists(split.getTestFiles());
        assertNullOrExists(split.getTestFiles());
        assertNullOrExists(split.getDevelopmentFiles());
        assertNullOrExists(ds.getLicenseFiles());
    }

    private void assertNullOrExists(File... aFiles)
    {
        if (aFiles != null) {
            for (File f : aFiles) {
                assertThat(f).exists();
            }
        }
    }
}
