package com.mulesoft.agent.common.internalhandlers;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.builders.MapMessageBuilder;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.Type;
import com.splunk.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.MapMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractSplunkInternalHandler<T> extends BufferedHandler<T>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractSplunkInternalHandler.class);

    private Service service;
    private Index index;
    private MapMessageBuilder messageBuilder;
    private Layout<? extends Serializable> layout;
    private LogEventFactory factory = new DefaultLogEventFactory();
    private String className = this.getClass().getName();

    /**
     * http://dev.splunk.com/view/java-sdk/SP-CAAAECX
     * By default, the token is valid for one hour, but is refreshed every time you make a call to Splunk.
     */
    private final int TOKEN_EXPIRATION_MS = 55 * 60 * 1000; // I use 55 minutes, instead of 60 as a safe net.
    private long lastConnection;

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
    @Configurable(type = Type.DYNAMIC)
    public String pass;

    /**
     * <p>
     * IP or hostname of the server where Splunk is running.
     * </p>
     */
    @Configurable(type = Type.DYNAMIC)
    public String host;

    /**
     * <p>
     * Splunk management port.
     * Default: 8089
     * </p>
     */
    @Configurable(value = "8089", type = Type.DYNAMIC)
    public int port;

    /**
     * <p>
     * Scheme of connection to the Splunk management port.
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
     * Default: _json
     * </p>
     */
    @Configurable(value = "_json", type = Type.DYNAMIC)
    public String splunkSourceType;

    /**
     * <p>
     * Date format used to format the timestamp.
     * Default: yyyy-MM-dd'T'HH:mm:ss.SZ
     * </p>
     */
    @Configurable(value = "yyyy-MM-dd'T'HH:mm:ss.SZ", type = Type.DYNAMIC)
    public String dateFormatPattern;


    private MapMessage createMapMessage (T message)
    {
        MapMessage mapMessage = this.messageBuilder.build(message);
        // Append additional properties
        Map<String, String> augmentation = augmentMapMessage(message);
        if (augmentation != null && !augmentation.isEmpty())
        {
            mapMessage.putAll(augmentation);
        }
        return mapMessage;
    }

    protected Map<String, String> augmentMapMessage (T message)
    {
        return null;
    }

    protected String getPattern ()
    {
        return this.messageBuilder.getDefaultPattern();
    }

    protected String getTimestampGetterName ()
    {
        return null;
    }

    protected abstract MapMessageBuilder getMessageBuilder ();

    @Override
    protected boolean canHandle (T message)
    {
        return true;
    }

    @Override
    protected boolean flush (final Collection<T> messages)
    {
        LOGGER.trace(String.format("Flushing %s notifications.", messages.size()));

        try
        {
            // Check if the authentication token, isn't expired
            if ((System.currentTimeMillis() - this.lastConnection) >= TOKEN_EXPIRATION_MS)
            {
                LOGGER.trace("Refreshing the session token.");
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
                args.put("source", this.splunkSource);
                args.put("sourcetype", this.splunkSourceType);
                socket = index.attach(args);
                output = socket.getOutputStream();
                for (T message : messages)
                {
                    MapMessage mapMessage = createMapMessage(message);
                    LogEvent event = factory.createEvent(className, null, className, Level.INFO, mapMessage, null, null);
                    output.write(layout.toByteArray(event));
                }
                output.flush();
                this.lastConnection = System.currentTimeMillis();
                LOGGER.trace(String.format("Flushed %s notifications.", messages.size()));
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
    public void postConfigurable () throws AgentEnableOperationException
    {
        super.postConfigurable();
        LOGGER.trace("Configuring the AbstractSplunkInternalHandler internal handler...");
        this.layout = null;

        if (StringUtils.isEmpty(this.host)
                || StringUtils.isEmpty(this.user)
                || StringUtils.isEmpty(this.pass)
                || StringUtils.isEmpty(this.scheme)
                || StringUtils.isEmpty(this.splunkIndexName)
                || StringUtils.isEmpty(this.splunkSource)
                || StringUtils.isEmpty(this.splunkSourceType))
        {
            LOGGER.error("Please review the EventTrackingSplunkInternalHandler (mule.agent.tracking.handler.splunk) configuration; " +
                    "You must configure at least the following properties: user, pass and host.");
            enable(false);
            return;
        }

        try
        {
            LOGGER.trace(String.format("Connecting to the Splunk server: %s:%s.", this.host, this.port));
            ServiceArgs loginArgs = new ServiceArgs();
            loginArgs.setUsername(this.user);
            loginArgs.setPassword(this.pass);
            loginArgs.setHost(this.host);
            loginArgs.setPort(this.port);
            loginArgs.setScheme(this.scheme);
            if (this.scheme.equalsIgnoreCase("https"))
            {
                SSLSecurityProtocol protocol = SSLSecurityProtocol.valueOf(this.sslSecurityProtocol);
                Service.setSslSecurityProtocol(protocol);
            }
            this.service = Service.connect(loginArgs);
            this.lastConnection = System.currentTimeMillis();
            LOGGER.trace("Successfully connected to the Splunk server.");
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error connecting to the Splunk server. Please review your settings.", e);
            enable(false);
            return;
        }

        try
        {
            LOGGER.trace(String.format("Retrieving the Splunk index: %s", this.splunkIndexName));
            this.index = service.getIndexes().get(this.splunkIndexName);
            if (index == null)
            {
                LOGGER.warn(String.format("Creating the index: %s", this.splunkIndexName));
                this.index = service.getIndexes().create(this.splunkIndexName);
                if (this.index == null)
                {
                    throw new Exception(String.format("Couldn't create the Splunk index: %s", this.splunkIndexName));
                }
                LOGGER.trace(String.format("Splunk index: %s, created successfully.", this.splunkIndexName));
            }
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error obtaining the Splunk index.", e);
            enable(false);
            return;
        }

        this.messageBuilder = getMessageBuilder();

        try
        {
            this.layout = PatternLayout.createLayout(this.getPattern(), null, null, null, true, true, null, null);
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error creating the pattern.", e);
            enable(false);
        }
        
        LOGGER.trace("Successfully configured the AbstractSplunkInternalHandler internal handler.");
    }
}
