/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracking.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.internalhandler.serializer.mixin.AgentTrackingNotificationMixin;
import com.mulesoft.agent.common.internalhandler.splunk.transport.Transport;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.agent.handlers.internal.buffer.DiscardingMessageBufferConfigurationFactory;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * The Event Tracking Http Internal handler will push all event tracking
 * notifications produced from the Mule ESB to the configured service.
 * </p>
 */
@Singleton
@Named("mule.agent.tracking.handler.http")
public class EventTrackingHttpInternalHandler extends BufferedHandler<AgentTrackingNotification>
{
    private final static Logger LOGGER = LogManager.getLogger();

    private ObjectMapper objectMapper;
    private Transport<AgentTrackingNotification> transport;

    /**
     * <p>
     * IP or hostname of the service where the notification will be pushed.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String host;

    /**
     * <p>
     * Service connection port.
     * Default: 8080
     * </p>
     */
    @Configurable(value = "8080", type = Type.DYNAMIC)
    public int port;

    /**
     * <p>
     *  Path where the service is listening.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String path;

    /**
     * <p>
     * Scheme of connection to the service (http, https).
     * Default: https
     * </p>
     */
    @Configurable(value = "https", type = Type.DYNAMIC)
    public String scheme;

    /**
     * <p>
     * The source used on the events sent to the service.
     * Default: mule
     * </p>
     */
    @Configurable(value = "mule", type = Type.DYNAMIC)
    public String source;

    @Override
    protected boolean canHandle(AgentTrackingNotification message)
    {
        return true;
    }

    @Override
    protected boolean flush(final Collection<AgentTrackingNotification> messages)
    {
        LOGGER.debug("Flushing %s notifications.", messages.size());

        if (this.transport == null)
        {
            throw new NullPointerException("The Http transport isn't initialized.");
        }

        boolean succeeded = this.transport.send(messages);

        if (succeeded)
        {
            LOGGER.debug("Flushed %s notifications.", messages.size());
        }

        return succeeded;
    }

    @Override
    public void initialize() throws InitializationException
    {
        LOGGER.debug("Configuring the Http Internal Handler with values: " + this.toString());

        if (this.objectMapper == null)
        {
            this.objectMapper = new ObjectMapper();
            this.objectMapper.addMixInAnnotations(AgentTrackingNotification.class, AgentTrackingNotificationMixin.class);
        }

        if (this.transport != null)
        {
            LOGGER.debug("Disposing the previous Http transport");
            this.transport.dispose();
        }

        LOGGER.debug("Creating a new Http transport");
        this.transport = new EventTrackingHttpInternalHandler.HttpTransport();
        this.transport.init();

        LOGGER.debug("Successfully configured the Http Internal Handler.");
        
        super.initialize();
    }

    @Override
    public BufferConfiguration getBuffer()
    {
        if (this.buffer == null)
        {
            this.buffer = new DiscardingMessageBufferConfigurationFactory().create(60000L, 1000, BufferType.MEMORY, null);
        }
        return this.buffer;
    }

    @Override
    public String toString()
    {
        return "EventTrackingHttpInternalHandler{" +
                ", host='" + host + '\'' +
                ", port=" + port  +
                ", path='" + path + '\'' +
                ", scheme='" + scheme + '\'' +
                ", source='" + source + '\''+
                '}';
    }


    private class HttpTransport implements Transport<AgentTrackingNotification>
    {
        private AsyncHttpClient httpClient;
        private URL url;

        private final static int CONNECTION_TIMEOUT = 10 * 1000;

        @Override
        public void init() throws InitializationException
        {
            try
            {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
                socket.close();

                this.url = new URL(scheme, host, port, path);

                AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder()
                        .setConnectTimeout(CONNECTION_TIMEOUT);

                AsyncHttpClientConfig asyncHttpClientConfig = builder.build();
                httpClient = new AsyncHttpClient(asyncHttpClientConfig);
            }
            catch (Exception e)
            {
                LOGGER.error("Error initializing the HTTP Event Host: {} - Port: {}. Error: {}",
                        host, port, ExceptionUtils.getRootCauseMessage(e));
                LOGGER.debug(e);

                throw new InitializationException(
                        "There was an error connecting to the service. Please review your settings.", e);
            }
        }

        @Override
        public boolean send(final Collection<AgentTrackingNotification> messages)
        {
            EventTrackingHttpInternalHandler.HttpMessage<AgentTrackingNotification> httpMessage = new EventTrackingHttpInternalHandler.HttpMessage<>(messages, source);
            String serialized = serialize(httpMessage);

            try
            {
                Response response = httpClient.preparePost(url.toString())
                        .addHeader("Content-type", "application/json")
                        .setBody(serialized)
                        .execute()
                        .get();

                if (response.getStatusCode() != HttpURLConnection.HTTP_OK)
                {
                    LOGGER.error("The service  didn't accept the request. Sending {} events. Error: {} - {}",
                            messages.size(), response.getStatusCode(), response.getStatusText());
                    LOGGER.debug(messages);

                    return false;
                }

                return true;
            }
            catch (InterruptedException e)
            {
                LOGGER.error("Error sending messages. Error: {}", host, port, ExceptionUtils.getRootCauseMessage(e));
                LOGGER.debug(e);
                return false;
            }
            catch (ExecutionException e)
            {
                LOGGER.error("Error sending messages. Error: {}", host, port, ExceptionUtils.getRootCauseMessage(e));
                LOGGER.debug(e);

                return false;
            }
        }

        @Override
        public void dispose()
        {
            if (httpClient != null)
            {
                httpClient.close();
            }
        }

        private String serialize(Object message)
        {
            try
            {
                return objectMapper.writeValueAsString(message);
            }
            catch (JsonProcessingException e)
            {
                LOGGER.error("Error converting event tracking notification to json: {}", message.getClass(), ExceptionUtils.getRootCauseMessage(e));
                LOGGER.debug(e);

                throw new RuntimeException(e);
            }
        }
    }

    static class HttpMessage<T>
    {
        @JsonProperty("source")
        private String source;

        @JsonProperty("data")
        private Collection<T> events;

        public HttpMessage(Collection<T> events, String source)
        {
            this.events = events;
            this.source = source;
        }
    }

}
