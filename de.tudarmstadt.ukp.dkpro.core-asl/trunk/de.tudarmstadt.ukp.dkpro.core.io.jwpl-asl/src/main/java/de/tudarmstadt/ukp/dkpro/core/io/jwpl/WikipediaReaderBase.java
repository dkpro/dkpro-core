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
package de.tudarmstadt.ukp.dkpro.core.io.jwpl;

import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;

/**
 * Abstract base class for all Wikipedia readers.
 *
 * @author zesch
 *
 */
public abstract class WikipediaReaderBase extends JCasCollectionReader_ImplBase
{

    /** The host server. */
    public static final String PARAM_HOST = "Host";
    @ConfigurationParameter(name = PARAM_HOST, mandatory=true)
    private String host;

    /** The name of the database. */
    public static final String PARAM_DB = "Database";
    @ConfigurationParameter(name = PARAM_DB, mandatory=true)
    private String db;

    /** The username of the database account. */
    public static final String PARAM_USER = "User";
    @ConfigurationParameter(name = PARAM_USER, mandatory=true)
    private String user;

    /** The password of the database account. */
    public static final String PARAM_PASSWORD = "Password";
    @ConfigurationParameter(name = PARAM_PASSWORD, mandatory=true)
    private String password;

    /** The language of the Wikipedia that should be connected to. */
    public static final String PARAM_LANGUAGE = "Language";
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory=true)
    private Language language;

    /** Sets whether the database configuration should be stored in the CAS,
     *  so that annotators down the pipeline can access additional data. */
    public static final String PARAM_CREATE_DATABASE_CONFIG_ANNOTATION = "CreateDBAnno";
    @ConfigurationParameter(name = PARAM_CREATE_DATABASE_CONFIG_ANNOTATION, mandatory=true, defaultValue="false")
    private boolean createDbAnno;

    protected DatabaseConfiguration dbconfig;

    protected Wikipedia wiki;


    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        dbconfig = new DatabaseConfiguration(
                host,
                db,
                user,
                password,
                language
        );

        try {
            this.wiki = new Wikipedia(dbconfig);
        }
        catch (WikiInitializationException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
	public void getNext(JCas jcas) throws IOException, CollectionException
	{
    	if(createDbAnno){
        	DBConfig dbconfiganno = new DBConfig(jcas);
        	dbconfiganno.setHost(host);
        	dbconfiganno.setPassword(password);
        	dbconfiganno.setDB(db);
        	dbconfiganno.setUser(user);
        	dbconfiganno.setLanguage(language.toString());
        	dbconfiganno.addToIndexes();
    	}
    }

    @Override
    public abstract Progress[] getProgress();

}
