package com.mulesoft.agent.common.internalhandler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferExhaustedAction;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.internalhandler.splunk.transport.Transport;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public abstract class HttpCustomInternalHandler<T> extends BufferedHandler<T>
{
    private final static Logger LOGGER = LogManager.getLogger();

    private ObjectMapper objectMapper;
    private Transport<T> transport;

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
    protected boolean canHandle(T message)
    {
        return true;
    }

    protected boolean flush(final Collection<T> messages)
    {
        LOGGER.debug("Flushing %s notifications.", messages.size());

        if (this.transport == null)
        {
            throw new NullPointerException("The Http custom publisher transport isn't initialized.");
        }

        boolean succeeded = this.transport.send(messages);

        if (succeeded)
        {
            LOGGER.debug("Flushed %s notifications.", messages.size());
        }

        return succeeded;
    }

    public void initialize() throws InitializationException
    {
        LOGGER.debug("Configuring the Http custom publisher Internal Handler with values: " + this.toString());

        if (this.objectMapper == null)
        {
            this.objectMapper = new ObjectMapper();
        }

        this.transport = new HttpTransport();
        this.transport.init();

        LOGGER.debug("Successfully configured the Http Custom Internal Handler.");
    }

    public ObjectMapper getObjectMapper()
    {
        return this.objectMapper;
    }

    @Override
    public BufferConfiguration getBuffer()
    {
        if (this.buffer != null)
        {
            return this.buffer;
        }

        BufferConfiguration defaultBuffer = new BufferConfiguration();
        defaultBuffer.setType(BufferType.MEMORY);
        defaultBuffer.setRetryCount(3);
        defaultBuffer.setFlushFrequency(10000L);
        defaultBuffer.setMaximumCapacity(1000);
        defaultBuffer.setDiscardMessagesOnFlushFailure(false);
        defaultBuffer.setWhenExhausted(BufferExhaustedAction.FLUSH);

        return this.buffer;
    }

    @Override
    public String toString()
    {
        return "HttpCustomInternalHandler{" +
                ", host='" + host + '\'' +
                ", port=" + port  +
                ", path='" + path + '\'' +
                ", scheme='" + scheme + '\'' +
                ", source='" + source + '\''+
                '}';
    }


    private class HttpTransport implements Transport<T>
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
                throw new InitializationException(
                        "There was an error connecting to the service. Please review your settings.", e);
            }
        }

        @Override
        public boolean send(final Collection<T> messages)
        {
            HttpMessage<T> httpMessage = new HttpMessage<>(messages, source);
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

        private String serialize(Object message)
        {
            try
            {
                return objectMapper.writeValueAsString(message);
            }
            catch (JsonProcessingException e)
            {
                LOGGER.error("Error converting to json", e);
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
