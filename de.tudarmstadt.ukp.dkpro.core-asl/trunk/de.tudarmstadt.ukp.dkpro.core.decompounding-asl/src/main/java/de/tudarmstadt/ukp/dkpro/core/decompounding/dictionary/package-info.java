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
 *******************************************************************************/

/**
 * This package contains dictionary classes.
 * Currently you have to options. You can work with
 * your own dictionary or with popular IGerman98 Dictionary
 * which is part of nearly all spell checkers.
 * 
 * If you want to use your own dictionary you have to create a
 * file that contains your words. Each word in one line.
 * Than you can use the {@see SimpleDictionary} class.
 * 
 * If you want to use the IGerman98 dictionary you can use 
 * the {@see IGerman98Dictionary}. The files are located
 * under /src/main/resources/de_DE.dic and /src/main/resources/de_DE.aff
 * 
 * Additional this package contains the {@see LinkingMorphemes} class.
 * This is a simple dictionary and hold all possible morphemes.
 * Currently the file /src/main/resources/linkingMorphemes.txt is used.
 * 
 * If you want to code you own dictionary use the {@see IDictionary}
 * interface.
 */
package de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary;