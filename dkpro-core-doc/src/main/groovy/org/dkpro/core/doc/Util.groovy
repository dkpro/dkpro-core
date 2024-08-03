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
package org.dkpro.core.doc

import java.text.BreakIterator

class Util {
    static def editOnGithub(url)
    {
        """
        ++++
        <div style="float:right">
          <a href="${url}">Edit on GitHub</a>
        </div>
        ++++
        """.stripIndent()
    }
    
    /*
    static def tagsetLink(name)
    {
        return "<<tagset-reference.adoc#tagset-${ id },${ name }>>
    }
    */
    
    static def typeLink(name)
    {
        if (name.startsWith('uima.cas.')) {
          return name.substring(9)
        }
        if (name.startsWith('uima.tcas.')) {
          return "${name.tokenize('.')[-1]}"
        }
        if (!name.contains('.')) {
          throw new IllegalArgumentException("Type names must have a package: ${name}")
        }
        else {
          return "<<typesystem-reference.adoc#type-${name},${name.tokenize('.')[-1]}>>"
        }
    }

    static def engineLink(name)
    {
        return "<<component-reference.adoc#engine-${name},${name}>>"
    }
    
    static def modelLink(model)
    {
        return "<<model-reference.adoc#model-${model.@artifactId},${model.@shortArtifactId}>>"
    }
    
    static def modelLink(model, title)
    {
        return "<<model-reference.adoc#model-${model.@artifactId},${title}>>"
    }

    static def formatLink(format)
    {
        return "<<format-reference.adoc#format-${ format.name },${ format.name }>>"
    }

    static def preparePassthrough(description)
    {
        if (description) {
            if (
                !description.contains('<p>') &&
                !description.contains('<div>') &&
                !description.contains('<ol>') &&
                !description.contains('<ul>') &&
                !description.contains('<table>')
            ) {
                description = "<p>${description}</p>"
            }
            if (!description.startsWith('<p>') && description.contains('<p>')) {
                def i = description.indexOf('<p>')
                description = "<p>${description[0..i-1]}</p>${description[i..-1]}"
            }
            description = "<div class='paragraph'>${description}</div>"
        }
        return description
    }
    
    static def shortDesc(description) {
        if (description) {
            BreakIterator tokenizer = BreakIterator.getSentenceInstance(Locale.US)
            tokenizer.setText(description)
            def start = tokenizer.first()
            def end = tokenizer.next()
            if (start > -1 && end > -1) {
                description = description.substring(start, end)
            }
            description = description
                // Remove HTML tags in tables
                .replaceAll(/<.+?>/, '') 
                // Make sure the text doesn't close the pass-through block
                .replaceAll(']', '{endsb}')
                .trim()
        }
        return description ? "pass:[${description}]" : '__No description__'
    }
}