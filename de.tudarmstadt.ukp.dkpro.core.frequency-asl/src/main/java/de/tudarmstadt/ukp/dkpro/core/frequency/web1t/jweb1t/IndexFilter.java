/*
 * Copyright 2007 FBK-irst http://www.itc.it/)
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
import java.io.FileFilter;

/**
 * TO DO
 *
 * @author 	Claudio Giuliano
 * @version %I%, %G%
 * @since		1.0
 */
class IndexFilter implements FileFilter
{
	
	// Accept all directories and all .test files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            if (f.getName().startsWith(".")) {
                return false;
            }
            return true;
        }
		
		String extension = getExtension(f.getName());
		if (extension != null) {
			if (!extension.toLowerCase().equals("idx")) {
				return true;
			} else {
				return false;
			}
		}
		
		return false;
	}
	
	// The description of this filter
	public String getDescription() {
		return "Just Test File";
	}
	
	// The description of this filter
	private String getExtension(String name) {
		int i = name.lastIndexOf('.') + 1;
		
		if ((i == -1) && (i == name.length()))
			return "";
		
		return name.substring(i);
	}  
} // end class
