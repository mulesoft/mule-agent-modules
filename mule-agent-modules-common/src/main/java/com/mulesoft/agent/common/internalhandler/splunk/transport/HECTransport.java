/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.HECTransportConfig;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Transport which connects to Splunk using HTTP Event Collector for sending the events.
 *
 * @author Walter Poch
 *         created on 10/23/15
 * @see <a href="http://dev.splunk.com/view/event-collector/SP-CAAAE6M">Introduction to Splunk HTTP Event Collector</a>
 * </p>
 * @param <T> Message type.
 */
public class HECTransport<T> extends AbstractTransport<T>
{
    private static final Logger LOGGER = LogManager.getLogger(HECTransport.class);
    private static final int CONNECTION_TIMEOUT = 10 * 1000; //10 sec of timeout
    private static final String HEC_PATH = "/services/collector";

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
            try (Socket socket = new Socket())
            {
                socket.connect(new InetSocketAddress(this.config.getHost(), this.config.getPort()), CONNECTION_TIMEOUT);
            }
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
                String serialized = serialize(wrappedMessage);

                if (StringUtils.isNotBlank(serialized))
                {
                    sb.append(serialized);
                }
            }

            // Use the Async library because it's already a dependency and manages the SSL Certificate validation

            AsyncHttpClientConfig httpClientConfig = new AsyncHttpClientConfig.Builder()
                    .setAcceptAnyCertificate(this.config.getAcceptAnyCertificate())
                    .build();

            Response response;
            try (AsyncHttpClient asyncHttpClient = new AsyncHttpClient(httpClientConfig))
            {
                response = asyncHttpClient.preparePost(url.toString())
                    .addHeader("Authorization", "Splunk " + this.config.getToken())
                    .setBody(sb.toString())
                    .execute()
                    .get();
            }

            if (response.getStatusCode() != HttpURLConnection.HTTP_OK)
            {
                LOGGER.error("The Splunk server didn't accept the request. Sending {} events. Error: {} - {}",
                        messages.size(), response.getStatusCode(), response.getStatusText());
                return false;
            }

            return true;
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

    /**
     * HEC Message definition.
     * @param <T> Event type.
     */
    static class HECMessage<T>
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

        HECMessage(T event, String source, String sourceType, String index, String host)
        {
            this.event = event;
            this.source = source;
            this.sourceType = sourceType;
            this.index = index;
            this.host = host;
        }

        @Override
        public String toString()
        {
            return "HECMessage{"
                    + "host='" + host + '\''
                    + ", source='" + source + '\''
                    + ", sourceType='" + sourceType + '\''
                    + ", index='" + index + '\''
                    + ", event=" + event
                    + '}';
        }
    }
}

