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
package de.tudarmstadt.ukp.dkpro.core.api.lexmorph.pos;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public final class POSUtils
{
    
    public static final String POS_TYPE_PREFIX = "POS_";
    
    private POSUtils() {
        // nothing here
    }
    
    public static void assignCoarseValue(POS pos) {
        
        if(pos == null) {
            return;
        }
        
        String shortName = pos.getType().getShortName();
        if(!StringUtils.equals(pos.getType().getName(), POS.class.getName())) {
        
            if(!shortName.startsWith(POS_TYPE_PREFIX)){
                throw new IllegalArgumentException("The type " +shortName+ "of the given POS annotation does not fulfill the convention of starting with prefix '"+POS_TYPE_PREFIX+"'");
            }
            pos.setCoarseValue(shortName.substring(POS_TYPE_PREFIX.length()).intern());
        }
    }
    
}
