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
package org.dkpro.core.api.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * CAS-configurable provider produces a resource from a stream instead of an URL. The provider
 * implementation does not have to care about opening/closing the stream.
 * 
 * @param <M>
 *            the type of resource to produce.
 */
public abstract class CasConfigurableStreamProviderBase<M>
    extends CasConfigurableProviderBase<M>
{
    @Override
    protected M produceResource(URL aUrl) throws IOException
    {
        try (InputStream is = aUrl.openStream()) {
            return produceResource(is);
        }
        catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }

    protected abstract M produceResource(InputStream aStream) throws Exception;
}
