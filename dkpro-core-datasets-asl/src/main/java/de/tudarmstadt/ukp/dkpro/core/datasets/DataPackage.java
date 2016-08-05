/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.datasets;

public class DataPackage
{
    public static final DataPackage LICENSE_CC_BY_4_0 = new DataPackage.Builder()
            .url("https://creativecommons.org/licenses/by/4.0/legalcode.txt")
            .sha1("1167f0e28fe2db01e38e883aaf1e749fb09f9ceb").target("LICENSE.txt").build();

    public static final DataPackage LICENSE_CC_BY_SA_3_0 = new DataPackage.Builder()
            .url("https://creativecommons.org/licenses/by-sa/3.0/legalcode.txt")
            .sha1("fb41626a3005c2b6e14b8b3f5d9d0b19b5faaa51").target("LICENSE.txt").build();
    
    private String url;
    private String target;
    private String sha1;
    private String md5;
    private Callback action;

    public DataPackage(Builder aBuilder)
    {
        url = aBuilder.url;
        target = aBuilder.target;
        sha1 = aBuilder.sha1;
        md5 = aBuilder.md5;
        action = aBuilder.action;
    }

    public String getUrl()
    {
        return url;
    }

    public String getTarget()
    {
        return target;
    }

    public String getSha1()
    {
        return sha1;
    }

    public String getMd5()
    {
        return md5;
    }
    
    public Callback getPostAction()
    {
        return action;
    }

    public static class Builder
    {
        private String url;
        private String target;
        private String sha1;
        private String md5;
        private Callback action;

        public Builder url(String aUrl)
        {
            url = aUrl;
            return this;
        }

        public Builder target(String aTarget)
        {
            target = aTarget;
            return this;
        }

        public Builder sha1(String aSha1)
        {
            sha1 = aSha1;
            return this;
        }

        public Builder md5(String aMd5)
        {
            md5 = aMd5;
            return this;
        }
        
        public Builder postAction(Callback aAction)
        {
            action = aAction;
            return this;
        }

        public DataPackage build()
        {
            return new DataPackage(this);
        }
    }
    
    @FunctionalInterface
    public static interface Callback
    {
        void run(DataPackage aPackage)
            throws Exception;
    }
}
