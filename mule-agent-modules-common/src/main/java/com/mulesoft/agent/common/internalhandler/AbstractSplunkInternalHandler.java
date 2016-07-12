package com.mulesoft.agent.common.internalhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.AgentConfigurationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferExhaustedAction;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.internalhandler.serializer.DefaultObjectMapperFactory;
import com.mulesoft.agent.common.internalhandler.splunk.transport.DefaultTransportFactory;
import com.mulesoft.agent.common.internalhandler.splunk.transport.Transport;
import com.mulesoft.agent.common.internalhandler.splunk.transport.TransportFactory;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Password;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.handlers.exception.InitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

public abstract class AbstractSplunkInternalHandler<T> extends BufferedHandler<T>
{
    private final static Logger LOGGER = LogManager.getLogger();

    private TransportFactory<T> transportFactory = new DefaultTransportFactory<>(this);
    private ObjectMapper objectMapper;
    private Transport<T> transport;

    /**
     * <p>
     * Username to connect to Splunk.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String user;

    /**
     * <p>
     * The password of the user to connect to Splunk.
     * </p>
     */
    @Password
    @Configurable(type = Type.DYNAMIC)
    public String pass;

    /**
     * <p>
     * The token to use on the HTTP Event Collector mode.
     * </p>
     */
    @Password
    @Configurable(type = Type.DYNAMIC)
    public String token;

    /**
     * <p>
     * IP or hostname of the server where Splunk is running.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String host;

    /**
     * <p>
     * Splunk connection port.
     * Default: 8089
     * </p>
     */
    @Configurable(value = "8089", type = Type.DYNAMIC)
    public int port;

    /**
     * <p>
     * Scheme of connection to the Splunk port (http, https, tcp).
     * Default: https
     * </p>
     */
    @Configurable(value = "https", type = Type.DYNAMIC)
    public String scheme;

    /**
     * <p>
     * SSL Security Protocol to use in the https connection.
     * Default: TLSv1.2
     * </p>
     */
    @Configurable(value = "TLSv1_2", type = Type.DYNAMIC)
    public String sslSecurityProtocol;

    /**
     * <p>
     * Splunk index name where all the events will be sent.
     * If the user has the rights, and the index doesn't exists, then the internal handler will create it.
     * Default: main
     * </p>
     */
    @Configurable(value = "main", type = Type.DYNAMIC)
    public String splunkIndexName;

    /**
     * <p>
     * The source used on the events sent to Splunk.
     * Default: mule
     * </p>
     */
    @Configurable(value = "mule", type = Type.DYNAMIC)
    public String splunkSource;

    /**
     * <p>
     * The sourcetype used on the events sent to Splunk.
     * Default: mule
     * </p>
     */
    @Configurable(value = "mule", type = Type.DYNAMIC)
    public String splunkSourceType;

    /**
     * <p>
     * Date format used to format the timestamp.
     * Default: yyyy-MM-dd'T'HH:mm:ss.SZ
     * </p>
     */
    @Configurable(value = "yyyy-MM-dd'T'HH:mm:ss.SZ", type = Type.DYNAMIC)
    public String dateFormatPattern;

    @Override
    protected boolean canHandle(T message)
    {
        return true;
    }

    @Override
    protected boolean flush(final Collection<T> messages)
    {
        LOGGER.debug("Flushing %s notifications.", messages.size());

        if (this.transport == null)
        {
            throw new NullPointerException("The Splunk transport isn't initialized.");
        }

        boolean succeeded = this.transport.send(messages);

        if (succeeded)
        {
            LOGGER.debug("Flushed %s notifications.", messages.size());
        }

        return succeeded;
    }

    public ObjectMapper getObjectMapper()
    {
        return this.objectMapper;
    }

    @Override
    public void initialize() throws InitializationException
    {
        LOGGER.debug("Configuring the Splunk Internal Handler with values: " + this.toString());

        if (this.transport != null)
        {
            LOGGER.debug("Disposing the previous Splunk transport");
            this.transport.dispose();
        }

        if (this.objectMapper == null)
        {
            this.objectMapper = new DefaultObjectMapperFactory(this.dateFormatPattern).create();
        }

        try
        {
            LOGGER.debug("Creating a new Splunk transport");
            this.transport = this.transportFactory.create();
            LOGGER.debug("Initializing the Splunk transport: " + this.transport);
            this.transport.init();
        }
        catch (AgentConfigurationException e)
        {
            throw new InitializationException("There was an error configuring the Internal Handler", e);
        }

        LOGGER.debug("Successfully configured the Common Splunk Internal Handler.");
        super.initialize();
    }

    @Override
    public BufferConfiguration getBuffer()
    {
        if (buffer != null)
        {
            return buffer;
        }
        else
        {
            BufferConfiguration defaultBuffer = new BufferConfiguration();
            defaultBuffer.setType(BufferType.MEMORY);
            defaultBuffer.setRetryCount(3);
            defaultBuffer.setFlushFrequency(10000l);
            defaultBuffer.setMaximumCapacity(5000);
            defaultBuffer.setDiscardMessagesOnFlushFailure(false);
            defaultBuffer.setWhenExhausted(BufferExhaustedAction.FLUSH);
            return defaultBuffer;
        }
    }

    @Override
    public String toString()
    {
        return "AbstractSplunkInternalHandler{" +
                "user='" + user + '\'' +
                ", host='" + host + '\'' +
                ", token='" + token + '\'' +
                ", port=" + port +
                ", scheme='" + scheme + '\'' +
                ", sslSecurityProtocol='" + sslSecurityProtocol + '\'' +
                ", splunkIndexName='" + splunkIndexName + '\'' +
                ", splunkSource='" + splunkSource + '\'' +
                ", splunkSourceType='" + splunkSourceType + '\'' +
                ", dateFormatPattern='" + dateFormatPattern + '\'' +
                '}';
    }
}
