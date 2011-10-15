/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;

/**
 * 
 * Adapted from NLTK.
 * 
 * License: ASL 2.0
 * 
 * @author zesch
 *
 */
public abstract class BaseCorpus implements Corpus {

    public final static String WORKSPACE = "dkpro_teaching";
    
	private File corpusPath;
	private List<File> files;
	protected final String languageCode;
    protected final String name;
	
	protected BaseCorpus(File path, String languageCode, String name) throws IOException {
		initialize(path);
		this.languageCode = languageCode;
		this.name = name;
	}

    protected BaseCorpus(String corpusName, String languageCode) throws IOException {
        
        initialize(
                new File(
                        DKProContext.getContext().
                           getWorkspace(WORKSPACE).
                           getAbsolutePath() + "/corpora/" + corpusName + "/"
                )
        );
        
        this.languageCode = languageCode;
        this.name = corpusName;
    }

    private void initialize(File path) throws IOException {
        
        if (!path.canRead() || !path.isDirectory()) {
            throw new IOException("Cannot read corpus from: " + path.getAbsolutePath());
        }

        this.corpusPath = path; 
        this.files = Arrays.asList(path.listFiles());
    }
    

	protected File getCorpusPath() {
		return corpusPath;
	}

	protected void setCorpusPath(File corpusPath) {
		this.corpusPath = corpusPath;
	}

	protected List<File> getFiles() {
		return files;
	}

	protected void setFiles(List<File> files) {
		this.files = files;
	}

    @Override
    public String getLanguage()
    {
        return this.languageCode;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
}