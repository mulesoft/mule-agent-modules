/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.log;

import javax.inject.Named;
import javax.inject.Singleton;

import com.mulesoft.agent.common.internalhandler.AbstractLogInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent;

/**
 * <p>
 * The Log Internal handler will store all the Http Events produced from the
 * Mule API Gateway in a configurable log file with a rolling file policy.
 * </p>
 */
@Singleton
@Named("mule.agent.gw.http.handler.log")
public class GatewayHttpEventsLogInternalHandler extends AbstractLogInternalHandler<AnalyticsHttpEvent>
{
    /**
     * <p>
     * The name of the file to write to.
     * If the file, or any of its parent directories, do not exist, they will be created.
     * </p>
     */
    @Configurable(value = "$MULE_HOME/logs/gw-http-events.log", type = Type.DYNAMIC)
    String fileName;

    /**
     * <p>
     * The pattern of the file name of the archived log file.
     * It will accept both a date/time pattern compatible with SimpleDateFormat and/or
     * a %i which represents an integer counter.
     * </p>
     */
    @Configurable(value = "$MULE_HOME/logs/gw-http-events-%d{yyyy-dd-MM}-%i.log", type = Type.DYNAMIC)
    String filePattern;

    @Override
    protected String getFileName()
    {
        return this.fileName;
    }

    @Override
    protected String getFilePattern()
    {
        return this.filePattern;
    }
}
