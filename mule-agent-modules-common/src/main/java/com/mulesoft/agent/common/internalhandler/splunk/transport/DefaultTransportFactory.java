/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.mulesoft.agent.exception.AgentConfigurationException;
import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.HECTransportConfig;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.RestTransportConfig;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.TCPTransportConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;


/**
 * <p>
 * Factory to create the specific transports.
 * In this version in order to maintain backward compatibility uses a set of checks on the Splunk Internal Handler fields.
 * </p>
 * @param <T> Message type.
 *
 * @author Walter Poch
 *         created on 10/28/15
 * @since 1.3.0
 */
public class DefaultTransportFactory<T extends Serializable> implements TransportFactory<T>
{
    private static final Logger LOGGER = LogManager.getLogger(DefaultTransportFactory.class);

    private final AbstractSplunkInternalHandler<T> internalHandler;

    public DefaultTransportFactory(AbstractSplunkInternalHandler<T> internalHandler)
    {
        this.internalHandler = internalHandler;
    }

    @Override
    public Transport<T> create() throws AgentConfigurationException
    {
        if (StringUtils.isNotBlank(this.internalHandler.getToken()))
        {
            HECTransportConfig config = new HECTransportConfig.Builder(this.internalHandler).build();
            LOGGER.debug("Creating the a HECTransport with the settings: " + config);
            return new HECTransport<T>(config, this.internalHandler.getObjectMapper());
        }

        if (this.internalHandler.getScheme().equals("tcp"))
        {
            TCPTransportConfig config = new TCPTransportConfig.Builder(this.internalHandler).build();
            LOGGER.debug("Creating the a TCPTransport with the settings: " + config);
            return new TCPTransport<T>(config, this.internalHandler.getObjectMapper());
        }

        // Default old behavior, REST
        RestTransportConfig config = new RestTransportConfig.Builder(this.internalHandler).build();
        LOGGER.debug("Creating the a RestTransport with the settings: " + config);
        return new RestTransport<T>(config, this.internalHandler.getObjectMapper());
    }
}
