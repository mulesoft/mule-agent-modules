package com.mulesoft.agent.eventtracker.cloudhub.source;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.tracking.FlowSourceEvent;
import com.mulesoft.agent.eventtracker.cloudhub.S3StorageException;
import com.mulesoft.agent.handlers.exception.InitializationException;

/**
 * An internal handler that stores {@link FlowSourceEvent}, which has non-empty payload, to S3.
 */
@Singleton
@Named("mule.agent.tracking.handler.cloudhub.source")
public class FlowSourceCloudhubInternalHandler extends BufferedHandler<FlowSourceEvent>
{

    private static final Logger LOGGER = LogManager.getLogger(FlowSourceCloudhubInternalHandler.class);

    @Inject
    private CloudhubS3ReplayStore replayStore;

    FlowSourceCloudhubInternalHandler()
    {
    }

    FlowSourceCloudhubInternalHandler(CloudhubS3ReplayStore replayStore)
    {
        this.replayStore = replayStore;
    }

    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();
        if (replayStore == null)
        {
            throw new InitializationException(
                    "Cannot Initilaize Cloudhub Flow Source Tracking handler without Cloudhub S3 Replay Store");
        }
        LOGGER.info("Initialized Cloudhub Flow Source Tracking handler");
    }

    @Override
    protected boolean canHandle(FlowSourceEvent message)
    {
        return message.getPayload() != null;
    }

    @Override
    protected boolean flush(Collection<FlowSourceEvent> messages)
    {
        LOGGER.trace("Flushing tracking payloads");
        try
        {
            replayStore.putFlowSourceEvent(messages);
            LOGGER.trace("Flushed tracking payloads");
            return true;
        }
        catch (S3StorageException ex)
        {
            LOGGER.warn("Cloud not send payload to s3. Error: {}", ExceptionUtils.getRootCauseMessage(ex));
            LOGGER.debug(ex);
            return false;
        }
    }

}
