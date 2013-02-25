/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model.DfModel;

/**
 * Serialization and deserialization methods.
 * 
 * @author Mateusz Parzonka
 * 
 */
public class TfidfUtils
{

	/**
	 * Serializes the DfStore at outputPath.
	 *
	 * @throws Exception
	 */
	public static void writeDfModel(DfModel dfModel, String path)
		throws Exception
	{
		serialize(dfModel, path);
	}

	/**
	 * Reads a {@link DfStore} from disk.
	 *
	 * @return
	 * @throws Exception
	 * @throws ResourceInitializationException
	 */
	public static DfModel getDfModel(String path)
		throws Exception
	{
		return deserialize(path);
	}

	public static void serialize(Object object, String fileName)
		throws Exception
	{
		File file = new File(fileName);
		if (!file.exists())
			FileUtils.touch(file);
		if (file.isDirectory()) {
			throw new IOException("A directory with that name exists!");
		}
		ObjectOutputStream objOut;
		objOut = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(file)));
		objOut.writeObject(object);
		objOut.flush();
		objOut.close();

	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(String filePath)
		throws Exception
	{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				new File(filePath)));
		return (T) in.readObject();
	}
}