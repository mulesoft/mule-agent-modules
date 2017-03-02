package com.mulesoft.agent.eventtracking.log;

import javax.inject.Named;
import javax.inject.Singleton;

import com.mulesoft.agent.common.internalhandler.AbstractLogInternalHandler;
import com.mulesoft.agent.common.internalhandler.serializer.mixin.AgentTrackingNotificationMixin;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.handlers.exception.InitializationException;

/**
 * <p>
 * The Log Internal handler will store all the Event Notifications produced from the
 * Mule ESB flows in a configurable log file with a rolling file policy.
 * </p>
 */
@Singleton
@Named("mule.agent.tracking.handler.log")
public class EventTrackingLogInternalHandler extends AbstractLogInternalHandler<AgentTrackingNotification>
{
    /**
     * <p>
     * The name of the file to write to.
     * If the file, or any of its parent directories, do not exist, they will be created.
     * </p>
     */
    @Configurable(value = "$MULE_HOME/logs/events.log", type = Type.DYNAMIC)
    String fileName;

    /**
     * <p>
     * The pattern of the file name of the archived log file.
     * It will accept both a date/time pattern compatible with SimpleDateFormat and/or
     * a %i which represents an integer counter.
     * </p>
     */
    @Configurable(value = "$MULE_HOME/logs/events-%d{yyyy-dd-MM}-%i.log", type = Type.DYNAMIC)
    String filePattern;

    @Override
    protected String getFileName()
    {
        return this.fileName;
    }

    @Override
    protected String getFilePattern()
    {
        return this.filePattern;
    }

    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();

        this.getObjectMapper().addMixInAnnotations(AgentTrackingNotification.class, AgentTrackingNotificationMixin.class);
    }
}
