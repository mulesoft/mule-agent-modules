package com.mulesoft.agent.eventtracker.cloudhub.source;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.mulesoft.agent.domain.tracking.FlowSourceEvent;
import com.mulesoft.agent.eventtracker.cloudhub.S3StorageException;
import com.mulesoft.agent.handlers.exception.InitializationException;

public class FlowSourceCloudhubInternalHandlerTest
{

    private CloudhubS3ReplayStore replayStore;
    private FlowSourceEvent message;
    private Object payload;

    FlowSourceCloudhubInternalHandler handler;

    @Before
    public void setUp() throws Exception
    {
        replayStore = mock(CloudhubS3ReplayStore.class);
        message = mock(FlowSourceEvent.class);
        payload = mock(Object.class);
        handler = new FlowSourceCloudhubInternalHandler(replayStore);

        when(message.getPayload()).thenReturn(payload);
    }

    @Test
    public void testCanHandle()
    {
        assertTrue(handler.canHandle(message));
    }

    @Test
    public void testCanNotHandleEmptyPayload()
    {
        when(message.getPayload()).thenReturn(null);
        assertFalse(handler.canHandle(message));
    }

    @Test
    public void testFlush() throws S3StorageException
    {
        assertTrue(handler.flush(Arrays.asList(message)));
        verify(replayStore, times(1)).putFlowSourceEvent(Arrays.asList(message));
    }

    @Test
    public void testFlushFailed() throws S3StorageException
    {
        doThrow(S3StorageException.class).when(replayStore).putFlowSourceEvent(anyListOf(FlowSourceEvent.class));
        assertFalse(handler.flush(Arrays.asList(message)));
    }

    @Test
    public void testInitialize() throws InitializationException
    {
        handler.initialize();
    }

    @Test(expected = InitializationException.class)
    public void testInitializeNoReplayStore() throws InitializationException
    {
        handler = new FlowSourceCloudhubInternalHandler(null);
        handler.initialize();
    }
}
