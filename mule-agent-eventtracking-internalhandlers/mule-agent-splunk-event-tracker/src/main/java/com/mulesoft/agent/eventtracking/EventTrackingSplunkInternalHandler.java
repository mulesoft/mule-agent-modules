package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.splunk.Args;
import com.splunk.Index;
import com.splunk.Service;
import com.splunk.ServiceArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Date;

@Named("mule.agent.tracking.handler.splunk")
@Singleton
public class EventTrackingSplunkInternalHandler extends BufferedHandler<AgentTrackingNotification>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(EventTrackingSplunkInternalHandler.class);

    @Configurable(type = Type.DYNAMIC)
    String user;

    @Configurable(type = Type.DYNAMIC)
    String pass;

    @Configurable(type = Type.DYNAMIC)
    String host;

    @Configurable(value = "8089", type = Type.DYNAMIC)
    int port;

    @Configurable(value = "https", type = Type.DYNAMIC)
    String scheme;

    @Configurable(value = "main", type = Type.DYNAMIC)
    String splunkIndexName;

    @Configurable(value = "mule", type = Type.DYNAMIC)
    String splunkSource;

    @Configurable(value = "_json", type = Type.DYNAMIC)
    String splunkSourceType;

    @Inject
    InternalHandlerSerializer<String> serializer;

    private Service service;
    private Index index;
    private boolean isConfigured;

    /**
     * http://dev.splunk.com/view/java-sdk/SP-CAAAECX
     * By default, the token is valid for one hour, but is refreshed every time you make a call to splunkd.
     */
    private final int TOKEN_EXPIRATION_MINUTES = 60;
    private Date lastConnection;


    @PostConfigure
    public void postConfigurable ()
    {
        super.postConfigurable();
        LOGGER.trace("Configuring the mule.agent.tracking.handler.splunk internal handler...");
        isConfigured = false;

        if (isNullOrWhiteSpace(this.host)
                || this.port > 0
                || isNullOrWhiteSpace(this.user)
                || isNullOrWhiteSpace(this.pass)
                || isNullOrWhiteSpace(this.scheme)
                || isNullOrWhiteSpace(this.splunkIndexName)
                || isNullOrWhiteSpace(this.splunkSource)
                || isNullOrWhiteSpace(this.splunkSourceType))
        {
            LOGGER.error("Please review the EventTrackingSplunkInternalHandler (mule.agent.tracking.handler.splunk) configuration; " +
                    "You must configure the following properties: user, pass and host.");
            isConfigured = false;
            return;
        }

        if (serializer == null)
        {
            serializer = new JsonInternalHandlerSerializer();
        }

        try
        {
            LOGGER.info(String.format("Connecting to the Splunk server: %s:%s.", this.host, this.port));
            ServiceArgs loginArgs = new ServiceArgs();
            loginArgs.setUsername(this.user);
            loginArgs.setPassword(this.pass);
            loginArgs.setHost(this.host);
            loginArgs.setPort(this.port);
            loginArgs.setScheme(this.scheme);
            service = Service.connect(loginArgs);
            lastConnection = new Date();
            LOGGER.info("Successfully connected to the Splunk server.");
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error connecting to the Splunk server. Please review your settings.", e);
            isConfigured = false;
            return;
        }

        try
        {
            LOGGER.info(String.format("Retrieving the Splunk index: %s", this.splunkIndexName));
            index = service.getIndexes().get(this.splunkIndexName);
            if (index == null)
            {
                LOGGER.warn(String.format("Creating the index: %s", this.splunkIndexName));
                index = service.getIndexes().create(this.splunkIndexName);
                if (index == null)
                {
                    throw new Exception(String.format("Couldn't create the Splunk index: %s",
                            this.splunkIndexName));
                }
                LOGGER.info(String.format("Splunk index: %s, created successfully.", this.splunkIndexName));
            }
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error obtaining the Splunk index.", e);
            isConfigured = false;
            return;
        }

        isConfigured = true;
        LOGGER.info("Successfully configured the mule.agent.tracking.handler.splunk internal handler ");
    }

    @Override
    protected boolean canHandle (AgentTrackingNotification message)
    {
        return isConfigured;
    }

    @Override
    protected boolean flush (final Collection<AgentTrackingNotification> messages)
    {
        LOGGER.trace(String.format("Flushing %s notifications.", messages.size()));

        try
        {
            // Check if the authentication token, isn't expired
            if ((new Date().getTime() - lastConnection.getTime()) / 60000 >= TOKEN_EXPIRATION_MINUTES)
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
                args.put("source", this.splunkSource);
                args.put("sourcetype", this.splunkSourceType);
                socket = index.attach(args);
                output = socket.getOutputStream();
                for (AgentTrackingNotification notification : messages)
                {
                    String serialized = serializer.serialize(notification);
                    output.write(serialized.getBytes("UTF8"));
                }
                output.flush();
                lastConnection = new Date();
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

    public static boolean isNullOrWhiteSpace (String a)
    {
        return a == null || (a.length() > 0 && a.trim().length() <= 0);
    }
}
