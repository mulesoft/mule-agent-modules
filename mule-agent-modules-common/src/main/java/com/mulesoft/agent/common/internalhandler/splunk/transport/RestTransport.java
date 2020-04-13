/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.HttpScheme;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.RestTransportConfig;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.splunk.Args;
import com.splunk.Index;
import com.splunk.SSLSecurityProtocol;
import com.splunk.Service;
import com.splunk.ServiceArgs;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;

/**
 * <p>
 * Transport which connects to the Splunk REST API for sending the events.
 *
 * @param <T> Messages type.
 * @author Walter Poch
 *         created on 10/23/15
 * @see <a href="http://dev.splunk.com/view/java-sdk/SP-CAAAEJ2#add2index">Splunk SDK - To add data directly to an index</a>
 * </p>
 */
public class RestTransport<T> extends AbstractTransport<T>
{
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(RestTransport.class);

    /**
     * http://dev.splunk.com/view/java-sdk/SP-CAAAECX
     * By default, the token is valid for one hour, but is refreshed every time you make a call to Splunk.
     */
    private static final int TOKEN_EXPIRATION_MS = 55 * 60 * 1000; // I use 55 minutes, instead of 60 as a safe net.
    private long lastConnection;
    private Service service;
    private Index index;

    private RestTransportConfig config;


    public RestTransport(RestTransportConfig config, ObjectMapper objectMapper)
    {
        super(objectMapper);
        this.config = config;
    }

    @Override
    public void init() throws InitializationException
    {
        try
        {
            LOGGER.debug("Connecting to the Splunk server: {}:{}.", this.config.getHost(), this.config.getPort());
            ServiceArgs loginArgs = new ServiceArgs();
            loginArgs.setUsername(this.config.getUser());
            loginArgs.setPassword(this.config.getPass());
            loginArgs.setHost(this.config.getHost());
            loginArgs.setPort(this.config.getPort());
            loginArgs.setScheme(this.config.getScheme().getValue());
            if (this.config.getScheme().equals(HttpScheme.HTTPS))
            {
                SSLSecurityProtocol protocol = this.config.getSslSecurityProtocol();
                Service.setSslSecurityProtocol(protocol);
            }
            this.service = Service.connect(loginArgs);
            this.lastConnection = System.currentTimeMillis();
            LOGGER.debug("Successfully connected to the Splunk server.");
        }
        catch (Exception e)
        {
            throw new InitializationException("There was an error connecting to the Splunk server. Please review your settings.", e);
        }

        try
        {
            LOGGER.debug("Retrieving the Splunk index: {}", this.config.getIndex());
            this.index = service.getIndexes().get(this.config.getIndex());
            if (index == null)
            {
                LOGGER.warn("Creating the index: {}", this.config.getIndex());
                this.index = service.getIndexes().create(this.config.getIndex());
                if (this.index == null)
                {
                    throw new InitializationException(String.format("Couldn't create the Splunk index: {}", this.config.getIndex()));
                }
                LOGGER.debug("Splunk index: {}, created successfully.", this.config.getIndex());
            }
        }
        catch (Exception e)
        {
            throw new InitializationException("There was an error obtaining the Splunk index.", e);
        }
    }

    @Override
    public boolean send(final Collection<T> messages)
    {
        if (this.service == null || this.index == null)
        {
            LOGGER.debug("The Splunk service isn't initializated correctly.");
            return false;
        }
        try
        {
            // Check if the authentication token, isn't expired
            if ((System.currentTimeMillis() - this.lastConnection) >= TOKEN_EXPIRATION_MS)
            {
                LOGGER.info("Refreshing the session token.");
                service.login();
            }
            /**
             * http://dev.splunk.com/view/java-sdk/SP-CAAAEJ2#add2index
             * Says to use the attachWith() method, but it didn't accept parameters
             * in order to specify the sourcetype, host, etc...
             * That's why we use the common attach() method.
             */
            Socket socket = null;
            OutputStream output = null;
            try
            {
                Args args = new Args();
                args.put("source", this.config.getSource());
                args.put("sourcetype", this.config.getSourceType());
                socket = index.attach(args);
                output = socket.getOutputStream();
                for (T message : messages)
                {
                    serializeTo(message, output);
                }
                this.lastConnection = System.currentTimeMillis();
                return true;
            }
            catch (IOException e)
            {
                LOGGER.error("There was an error sending the notifications to the Splunk instance.", e);
                return false;
            }
            finally
            {
                if (output != null)
                {
                    output.close();
                }
                if (socket != null)
                {
                    socket.close();
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.error("There was an error closing the communication to the Splunk instance.", e);
            return false;
        }
    }

    @Override
    public void dispose()
    {
        if (service != null)
        {
            service.logout();
        }
    }
}
