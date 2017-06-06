package com.mulesoft.agent.common.internalhandler;

import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.exception.AgentConfigurationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferExhaustedAction;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.internalhandler.serializer.DefaultObjectMapperFactory;
import com.mulesoft.agent.common.internalhandler.splunk.transport.DefaultTransportFactory;
import com.mulesoft.agent.common.internalhandler.splunk.transport.Transport;
import com.mulesoft.agent.common.internalhandler.splunk.transport.TransportFactory;
import com.mulesoft.agent.configuration.Configurable;
import java.io.Serializable;

import com.mulesoft.agent.configuration.Password;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.handlers.exception.InitializationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract splunk internal handler.
 * @param <T> Message type.
 */
public abstract class AbstractSplunkInternalHandler<T extends Serializable> extends BufferedHandler<T>
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int DEFAULT_BUFFER_RETRY_COUNT = 3;
    private static final long DEFAULT_BUFFER_FLUSH_FREQUENCY = 10000L;
    private static final int DEFAULT_BUFFER_MAXIMUM_CAPACITY = 1000;
    private static final boolean DEFAULT_DISCARD_ON_FAILURE = false;

    private TransportFactory<T> transportFactory = new DefaultTransportFactory<>(this);
    private ObjectMapper objectMapper;
    private Transport<T> transport;

    /**
     * <p>
     * Username to connect to Splunk.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    private String user;

    /**
     * <p>
     * The password of the user to connect to Splunk.
     * </p>
     */
    @Password
    @Configurable(type = Type.DYNAMIC)
    private String pass;

    /**
     * <p>
     * The token to use on the HTTP Event Collector mode.
     * </p>
     */
    @Password
    @Configurable(type = Type.DYNAMIC)
    private String token;

    /**
     * <p>
     * IP or hostname of the server where Splunk is running.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    private String host;

    /**
     * <p>
     * Splunk connection port.
     * Default: 8089
     * </p>
     */
    @Configurable(value = "8089", type = Type.DYNAMIC)
    private int port;

    /**
     * <p>
     * Scheme of connection to the Splunk port (http, https, tcp).
     * Default: https
     * </p>
     */
    @Configurable(value = "https", type = Type.DYNAMIC)
    private String scheme;

    /**
     * <p>
     * SSL Security Protocol to use in the https connection.
     * Default: TLSv1.2
     * </p>
     */
    @Configurable(value = "TLSv1_2", type = Type.DYNAMIC)
    private String sslSecurityProtocol;

    /**
     * <p>
     * Splunk index name where all the events will be sent.
     * If the user has the rights, and the index doesn't exists, then the internal handler will create it.
     * Default: main
     * </p>
     */
    @Configurable(value = "main", type = Type.DYNAMIC)
    private String splunkIndexName;

    /**
     * <p>
     * The source used on the events sent to Splunk.
     * Default: mule
     * </p>
     */
    @Configurable(value = "mule", type = Type.DYNAMIC)
    private String splunkSource;

    /**
     * <p>
     * The sourcetype used on the events sent to Splunk.
     * Default: mule
     * </p>
     */
    @Configurable(value = "mule", type = Type.DYNAMIC)
    private String splunkSourceType;

    /**
     * <p>
     * Date format used to format the timestamp.
     * Default: yyyy-MM-dd'T'HH:mm:ss.SZ
     * </p>
     */
    @Configurable(value = "yyyy-MM-dd'T'HH:mm:ss.SZ", type = Type.DYNAMIC)
    private String dateFormatPattern;

    /**
     * <p>
     * Determines if the Splunk internal handlers will accept connections
     * to any Splunk server by ignoring the SSL certificate.
     * It is enabled by default just to support backward compatibility.
     * </p>
     */
    @Configurable(value = "true", type = Type.DYNAMIC)
    private Boolean acceptAnyCertificate;

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

        acceptAnyCertificate = acceptAnyCertificate == null ? true : acceptAnyCertificate;

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
            defaultBuffer.setRetryCount(DEFAULT_BUFFER_RETRY_COUNT);
            defaultBuffer.setFlushFrequency(DEFAULT_BUFFER_FLUSH_FREQUENCY);
            defaultBuffer.setMaximumCapacity(DEFAULT_BUFFER_MAXIMUM_CAPACITY);
            defaultBuffer.setDiscardMessagesOnFlushFailure(DEFAULT_DISCARD_ON_FAILURE);
            defaultBuffer.setWhenExhausted(BufferExhaustedAction.FLUSH);
            return defaultBuffer;
        }
    }

    public void setTransportFactory(TransportFactory<T> transportFactory)
    {
        this.transportFactory = transportFactory;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public void setTransport(Transport<T> transport)
    {
        this.transport = transport;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }

    public void setSslSecurityProtocol(String sslSecurityProtocol)
    {
        this.sslSecurityProtocol = sslSecurityProtocol;
    }

    public void setSplunkIndexName(String splunkIndexName)
    {
        this.splunkIndexName = splunkIndexName;
    }

    public void setSplunkSource(String splunkSource)
    {
        this.splunkSource = splunkSource;
    }

    public void setSplunkSourceType(String splunkSourceType)
    {
        this.splunkSourceType = splunkSourceType;
    }

    public void setDateFormatPattern(String dateFormatPattern)
    {
        this.dateFormatPattern = dateFormatPattern;
    }

    public void setAcceptAnyCertificate(Boolean acceptAnyCertificate)
    {
        this.acceptAnyCertificate = acceptAnyCertificate;
    }

    public TransportFactory<T> getTransportFactory()
    {
        return transportFactory;
    }

    public Transport<T> getTransport()
    {
        return transport;
    }

    public String getUser()
    {
        return user;
    }

    public String getPass()
    {
        return pass;
    }

    public String getToken()
    {
        return token;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getScheme()
    {
        return scheme;
    }

    public String getSslSecurityProtocol()
    {
        return sslSecurityProtocol;
    }

    public String getSplunkIndexName()
    {
        return splunkIndexName;
    }

    public String getSplunkSource()
    {
        return splunkSource;
    }

    public String getSplunkSourceType()
    {
        return splunkSourceType;
    }

    public String getDateFormatPattern()
    {
        return dateFormatPattern;
    }

    public Boolean getAcceptAnyCertificate()
    {
        return acceptAnyCertificate;
    }

    @Override
    public String toString()
    {
        return "AbstractSplunkInternalHandler{"
                + "user='" + user + '\''
                + ", host='" + host + '\''
                + ", token='" + token + '\''
                + ", port=" + port
                + ", scheme='" + scheme + '\''
                + ", sslSecurityProtocol='" + sslSecurityProtocol + '\''
                + ", splunkIndexName='" + splunkIndexName + '\''
                + ", splunkSource='" + splunkSource + '\''
                + ", splunkSourceType='" + splunkSourceType + '\''
                + ", dateFormatPattern='" + dateFormatPattern + '\''
                + '}';
    }
}
