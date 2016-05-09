/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.HECTransportConfig;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * Transport which connects to Splunk using HTTP Event Collector for sending the events.
 *
 * @author Walter Poch
 *         created on 10/23/15
 * @see <a href="http://dev.splunk.com/view/event-collector/SP-CAAAE6M">Introduction to Splunk HTTP Event Collector</a>
 * </p>
 */
public class HECTransport<T> extends AbstractTransport<T>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(HECTransport.class);
    private final static int CONNECTION_TIMEOUT = 10 * 1000; //10 sec of timeout
    private final static String HEC_PATH = "/services/collector";

    private HECTransportConfig config;
    private String host;
    private URL url;

    public HECTransport(HECTransportConfig config, ObjectMapper objectMapper)
    {
        super(objectMapper);
        this.config = config;
    }

    @Override
    public void init() throws InitializationException
    {
        try
        {
            this.url = new URL(this.config.getScheme().getValue(), this.config.getHost(), this.config.getPort(), HEC_PATH);


            LOGGER.debug("Connecting to the Splunk server: %s:%s.", this.config.getHost(), this.config.getPort());
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(this.config.getHost(), this.config.getPort()), CONNECTION_TIMEOUT);
            socket.close();
            LOGGER.debug("Successfully connected to the Splunk server.");

            if (this.host == null)
            {
                try
                {
                    this.host = InetAddress.getLocalHost().toString();
                }
                catch (UnknownHostException e)
                {
                    LOGGER.warn("The host couldn't be calculated.", e);
                }
            }
        }
        catch (Exception e)
        {
            throw new InitializationException(
                    "There was an error connecting to the Splunk server. Please review your settings.", e);
        }
    }

    @Override
    public boolean send(final Collection<T> messages)
    {
        try
        {
            StringBuilder sb = new StringBuilder();

            for (T message : messages)
            {
                HECMessage wrappedMessage = new HECMessage(message, this.config.getSource(),
                        this.config.getSourceType(), this.config.getIndex(), this.host);
                String serialized = this.getObjectMapper().writeValueAsString(wrappedMessage) + LINE_BREAKER;
                sb.append(serialized);
            }

            // Use the Async library because it's already a dependency and manages the SSL Certificate validation
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            Response response = asyncHttpClient.preparePost(url.toString())
                    .addHeader("Authorization", "Splunk " + this.config.getToken())
                    .setBody(sb.toString())
                    .execute()
                    .get();
            asyncHttpClient.close();

            if (response.getStatusCode() != HttpURLConnection.HTTP_OK)
            {
                LOGGER.error("The Splunk server didn't accept the request. Sending {} events. Error: {} - {}",
                        messages.size(), response.getStatusCode(), response.getStatusText());
                return false;
            }

            return true;
        }
        catch (IOException e)
        {
            LOGGER.error("There was an error sending the events to the Splunk instance.", e);
            return false;
        }
        catch (InterruptedException e)
        {
            LOGGER.error("There was an error retrieving the response.", e);
            return false;
        }
        catch (ExecutionException e)
        {
            LOGGER.error("There was an error executing the request.", e);
            return false;
        }
    }

    @Override
    public void dispose()
    {
    }

    private class HECMessage<T>
    {
        @JsonProperty("host")
        private String host;
        @JsonProperty("source")
        private String source;
        @JsonProperty("sourcetype")
        private String sourceType;
        @JsonProperty("index")
        private String index;
        @JsonProperty("event")
        private T event;

        public HECMessage(T event, String source, String sourceType, String index, String host)
        {
            this.event = event;
            this.source = source;
            this.sourceType = sourceType;
            this.index = index;
            this.host = host;
        }
    }
}

