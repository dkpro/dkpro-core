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
package org.dkpro.core.castransformation.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.uima.cas.CAS;
import org.dkpro.core.api.transform.alignment.AlignedString;
import org.dkpro.core.castransformation.ApplyChangesAnnotator;
import org.dkpro.core.castransformation.Backmapper;

/**
 * Use to smuggle the alignment state from the {@link ApplyChangesAnnotator} to the
 * {@link Backmapper}.
 *
 * @since 1.1.0
 */
public class AlignmentStorage
{
    private static AlignmentStorage instance;

    private Map<CAS, Map<Key, AlignedString>> mmap;

    {
        // WeakHashMap is not threadsafe, so we need to wrap it, because it will likely be
        // invoked by many concurrent pipeline threads.
        mmap = Collections.synchronizedMap(new WeakHashMap<CAS, Map<Key, AlignedString>>());
    }

    public static synchronized AlignmentStorage getInstance()
    {
        if (instance == null) {
            instance = new AlignmentStorage();
        }
        return instance;
    }

    public AlignedString get(final CAS aCas, final String from, final String to)
    {
        Map<Key, AlignedString> map = mmap.get(aCas);
        if (map == null) {
            return null;
        }
        return map.get(new Key(from, to));
    }

    public void put(final CAS aCas, final String from, final String to, final AlignedString aAs)
    {
        Map<Key, AlignedString> map = mmap.computeIfAbsent(aCas, k -> new HashMap<>());
        // No reason to keep the internal map synchronized because it's specific to the CAS, and
        // it is assumed that two different pipeline threads in any execution environment never
        // manipulate the same CAS instance simultaneously.
        //System.out.println("Adding from [" + from + "] to [" + to + "] on [" + aCas.hashCode()
        //        + "]");
        map.put(new Key(from, to), aAs);

    }

    private static class Key
    {
        final String from;
        final String to;

        public Key(final String aFrom, final String aTo)
        {
            from = aFrom;
            to = aTo;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((from == null) ? 0 : from.hashCode());
            result = prime * result + ((to == null) ? 0 : to.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (from == null) {
                if (other.from != null) {
                    return false;
                }
            }
            else if (!from.equals(other.from)) {
                return false;
            }
            if (to == null) {
                if (other.to != null) {
                    return false;
                }
            }
            else if (!to.equals(other.to)) {
                return false;
            }
            return true;
        }
    }
}
