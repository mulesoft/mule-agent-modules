/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport.config;

import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;

/**
 * @author Walter Poch
 *         created on 10/23/15
 */
public abstract class AbstractSplunkConfig
{
    @Configurable(type = Type.DYNAMIC)
    private String host;
    @Configurable(type = Type.DYNAMIC)
    private int port;

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    @Override
    public String toString()
    {
        return "AbstractSplunkConfig{"
                + "host='" + host + '\''
                + ", port=" + port
                + '}';
    }

}
