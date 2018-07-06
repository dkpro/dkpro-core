/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.testing.validation;

public class Message
{
    public static enum Level
    {
        INFO, ERROR
    }

    public final Message.Level level;
    public final Class<?> source;
    public final String message;

    public Message(Object aSource, Message.Level aLevel, String aMessage)
    {
        this(aSource, aLevel, "%s", aMessage);
    }

    public Message(Object aSource, Message.Level aLevel, String aFormat, Object... aValues)
    {
        source = aSource != null ? aSource.getClass() : null;
        level = aLevel;
        message = String.format(aFormat, aValues);
    }

    @Override
    public String toString()
    {
        return String.format("[%s] %s", source != null ? source.getSimpleName() : "<unknown>",
                message);
    }
}
