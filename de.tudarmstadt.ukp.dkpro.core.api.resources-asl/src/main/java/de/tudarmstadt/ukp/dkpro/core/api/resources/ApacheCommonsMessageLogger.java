package de.tudarmstadt.ukp.dkpro.core.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.Message;

public class ApacheCommonsMessageLogger
    extends AbstractMessageLogger
{

    private final Log logger = LogFactory.getLog(getClass());

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
