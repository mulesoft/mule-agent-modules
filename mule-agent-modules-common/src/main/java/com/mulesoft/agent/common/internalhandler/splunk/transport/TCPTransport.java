/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.TCPTransportConfig;
import com.mulesoft.agent.handlers.exception.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;

/**
 * <p>
 *     Transport which connects to Splunk using TCP for sending the events.
 *     @see <a href="http://dev.splunk.com/view/java-sdk/SP-CAAAEJ2#add2tcpinput">Splunk SDK - To add data directly to a TCP input</a>
 * </p>
 * @author Walter Poch
 *         created on 10/23/15
 */
public class TCPTransport<T> extends AbstractTransport<T>
{
    private final static Logger LOGGER = LogManager.getLogger(TCPTransport.class);
    private final static int CONNECTION_TIMEOUT = 10 * 1000; //10 sec of timeout

    private TCPTransportConfig config;

    public TCPTransport(TCPTransportConfig config, ObjectMapper objectMapper)
    {
        super(objectMapper);
        this.config = config;
    }

    @Override
    public void init() throws InitializationException
    {
        try
        {
            LOGGER.debug("Connecting to the Splunk server: %s:%s.", this.config.getHost(), this.config.getPort());
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(this.config.getHost(), this.config.getPort()), CONNECTION_TIMEOUT);
            socket.close();
            LOGGER.debug("Successfully connected to the Splunk server.");
        }
        catch (Exception e)
        {
            throw new InitializationException("There was an error connecting to the Splunk server. Please review your settings.", e);
        }
    }

    @Override
    public boolean send(final Collection<T> messages)
    {
        try
        {
            Socket socket = null;
            OutputStream output = null;
            try
            {
                socket = new Socket();
                socket.connect(new InetSocketAddress(this.config.getHost(), this.config.getPort()), CONNECTION_TIMEOUT);
                output = socket.getOutputStream();
                for (T message : messages)
                {
                    String serialized = this.getObjectMapper().writeValueAsString(message) + LINE_BREAKER;
                    output.write(serialized.getBytes(CHARSET));
                    output.flush();
                }
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
    }
}
