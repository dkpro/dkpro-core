/*
 * This class is based on FileSearch.java in
 * the jWeb1T library by FBK-irst http://www.itc.it/
 * The read(long m) method has been changed to be UTF-8 conform.
 *
 * Original licence information:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.frequency.web1t.jweb1t;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 * @version %I%, %G%
 * @since		1.0
 */
public class FileSearch implements Search
{
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named <code>FileSearch</code>.
	 */
	static Logger logger = Logger.getLogger(FileSearch.class.getName());

	private final RandomAccessFile raf;

	public FileSearch(File file) throws IOException
	{
		raf = new RandomAccessFile(file, "r");

	} // end constructor

	//
	public void close() throws IOException
	{
		raf.close();
	} // end close

	//
	@Override
	public long getFreq(String t) throws IOException
	{
		//logger.info("searching '" + t + "' in " + file.getName());
		long s = 0;
		long m = raf.length();
		long e = raf.length();
		int loop = 0;
		while (e > (s + 1))
		{
			++loop;
			//logger.info("loop: " + loop);
			m = s + ((e - s) / 2);
			//logger.debug(s + ", [" + m + "], " + e);
			NGram n = read(m);
			if (n == null)
			{
				//logger.info("loop: " + loop);
				return 0;
			}

			int c = t.compareTo(n.s);

			if (c == 0)
			{
				//logger.debug(t + " == " + n.s + " (" + c + ")");
				//logger.info("loops: " + loop);
				return n.f;
			}
			else if (c > 0)
			{
				//logger.debug(t + " > " + n.s + " (" + c + ")");
				s = m;
			}
			else
			{
				//logger.debug(t + " < " + n.s + " (" + c + ")");
				e = m;
			}

		} // end while

		//logger.info("loops: " + loop);
		return 0;
 	} // end getFreq

	//
	public NGram read(long m) throws IOException
	{
		long s = m - 50;
		if (s < 0) {
			s = 0;
		}
		long e = m + 50;
		if (e > raf.length()) {
			e = raf.length();
		}

		int len = (int) (e - s);
		int nm = (int) (m - s);

		//logger.debug("nm = " + nm);
		//logger.debug("len = " + len);

		raf.seek(s);
		byte[] array = new byte[len];

		raf.read(array);

		int i = nm;

		//Go back to the beginning of the line
		while ((i >= 0) && ((char) array[i]) != '\n')
		{
			i--;
		}

		//remember line start position
		int ns = i + 1;

		i = nm + 1;

		//go to end of line
		while ((i < array.length) && ((char) array[i]) != '\n')
		{
			i++;
		}

		//remember line end position
		int ne = i;

		//copy the bytes for the current line to a new byte[]
		byte[] curLine = new byte[ne-ns];
		int index = 0;
		for (int j=ns;j<ne;j++)
		{
			curLine[index++]=array[j];
		}

		//convert the curLine-byte[] to UTF-8 String
		String lineAsString = new String(curLine, "UTF-8");

		if (lineAsString.length() == 0) {
			return null;
		}

		return new NGram(lineAsString);

	}


}