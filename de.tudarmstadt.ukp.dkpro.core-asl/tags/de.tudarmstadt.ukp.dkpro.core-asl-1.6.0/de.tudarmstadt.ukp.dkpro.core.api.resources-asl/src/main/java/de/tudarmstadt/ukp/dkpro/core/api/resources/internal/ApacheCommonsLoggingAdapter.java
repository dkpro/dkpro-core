/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.api.resources.internal;

import org.apache.commons.logging.Log;
import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.Message;

public class ApacheCommonsLoggingAdapter
    extends AbstractMessageLogger
{
    private final Log logger;

    public ApacheCommonsLoggingAdapter(Log aLogger)
    {
        logger = aLogger;
    }
    
    @Override
    public void log(String msg, int level)
    {
        switch (level) {
        case Message.MSG_DEBUG:
            debug(msg);
            break;
        case Message.MSG_VERBOSE:
            verbose(msg);
            break;
        case Message.MSG_INFO:
            info(msg);
            break;
        case Message.MSG_WARN:
            warn(msg);
            break;
        case Message.MSG_ERR:
            error(msg);
            break;
        default:
            break;
        }
    }

    @Override
    public void rawlog(String msg, int level)
    {
        log(msg, level);
    }

    @Override
    public void debug(String msg)
    {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }

    @Override
    public void verbose(String msg)
    {
        debug(msg);
    }

    @Override
    public void info(String msg)
    {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }

    @Override
    public void warn(String msg)
    {
        if(logger.isWarnEnabled()){
            logger.warn(msg);
        }
    }

    @Override
    public void error(String msg)
    {
        if(logger.isErrorEnabled()){
            logger.error(msg);
        }
    }

    @Override
    protected void doProgress()
    {
    }

    @Override
    protected void doEndProgress(String msg)
    {
    }

}
