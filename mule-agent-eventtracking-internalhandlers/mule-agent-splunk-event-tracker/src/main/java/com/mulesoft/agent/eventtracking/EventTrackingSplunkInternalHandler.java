package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.splunk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

@Named("mule.agent.tracking.handler.splunk")
@Singleton
public class EventTrackingSplunkInternalHandler extends BufferedHandler<AgentTrackingNotification> {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventTrackingSplunkInternalHandler.class);

    @Configurable(type = Type.DYNAMIC)
    String user;

    @Configurable(type = Type.DYNAMIC)
    String pass;

    @Configurable(type = Type.DYNAMIC)
    String host;

    @Configurable(value = "8089", type = Type.DYNAMIC)
    int port;

    @Configurable(value="https",type = Type.DYNAMIC)
    String scheme;

    @Configurable(value="main",type = Type.DYNAMIC)
    String splunkIndexName;

    @Configurable(value="mule",type = Type.DYNAMIC)
    String splunkSource;

    @Configurable(value="_json",type = Type.DYNAMIC)
    String splunkSourceType;


    private Service service;
    private Index index;


    @PostConfigure
    public void postConfigurable(){
        super.postConfigurable();

        ServiceArgs loginArgs = new ServiceArgs();
        loginArgs.setUsername(this.user);
        loginArgs.setPassword(this.pass);
        loginArgs.setHost(this.host);
        loginArgs.setPort(this.port);
        loginArgs.setScheme(this.scheme);

        service = Service.connect(loginArgs);
        index = service.getIndexes().get(this.splunkIndexName);
        if(index == null){
            index = service.getIndexes().create(this.splunkIndexName);
        }
    }

    @Override
    protected boolean canHandle(AgentTrackingNotification message) {
        return true;
    }

    @Override
    protected boolean flush(final Collection<AgentTrackingNotification> messages) {
        LOGGER.trace(String.format("Flushing %s notifications.", messages.size()));

        try {

            /**
             * http://dev.splunk.com/view/java-sdk/SP-CAAAEJ2#add2index
             * Says to use the attachWith() method, but it didn't accept parameters
             * in order to specify the sourcetype, host, etc...
             * That's why we use the common attach() method.
             */
            Socket socket = null;
            OutputStream output = null;
            try {
                Args args = new Args();
                //args.put("host","test");
                args.put("source",this.splunkSource);
                args.put("sourcetype",this.splunkSourceType);
                socket = index.attach(args);
                output = socket.getOutputStream();
                for (AgentTrackingNotification notification : messages) {
                    LOGGER.trace("Flushing Notification: " + notification);
                    output.write((toString(notification)).getBytes("UTF8"));
                }
                output.flush();
                return true;
            } finally {
                if (output != null) { output.close(); }
                if (socket != null) { socket.close(); }
            }
        } catch (IOException e) {
            LOGGER.error("There was an error sending the notifications to the Splunk instance.", e);
            return false;
        }
    }
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private String toString(AgentTrackingNotification notification){
            return "{ " +
                    "\"timestamp\": \"" + dateFormat.format(new Date(notification.getTimestamp())) + "\", " +
                    "\"application\": \"" + notification.getApplication()+ "\", " +
                    "\"notificationType\": \"" + notification.getNotificationType() + "\", " +
                    "\"action\": \"" + notification.getAction() + "\", " +
                    "\"resourceIdentifier\": \"" + notification.getResourceIdentifier() + "\", " +
                    "\"source\": \"" + notification.getSource() + "\", " +
                    "\"muleMessage\": \"" + notification.getMuleMessage() + "\", " +
                    "\"path\": \"" + notification.getPath() + "\", " +
                    "\"annotations\": \"" + notification.getAnnotations().size() + "\", " +
                    "\"muleMessageId\": \"" + notification.getMuleMessageId() + "\"" +
                    " }\r\n";

    }
}
