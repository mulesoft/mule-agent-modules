package com.mulesoft.agent.eventtracker.cloudhub.tracking;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.eventtracker.cloudhub.InsightClient;
import com.mulesoft.agent.eventtracker.cloudhub.InsightEvent;
import com.mulesoft.agent.eventtracker.cloudhub.InsightException;

public class EventTrackingCloudhubInternalHandlerTest
{

    private InsightClient insightClient;

    EventTrackingCloudhubInternalHandler handler;

    @Before
    public void setUp() throws Exception
    {
        insightClient = mock(InsightClient.class);
        handler = new EventTrackingCloudhubInternalHandler(insightClient);
    }

    @Test
    public void testFlush() throws InsightException
    {
        assertTrue(handler.flush(mockNotifications()));
        verify(insightClient, times(1)).sendInsight(anyListOf(InsightEvent.class));
    }

    @Test
    public void testFlushFailed() throws InsightException
    {
        doThrow(InsightException.class).when(insightClient).sendInsight(anyListOf(InsightEvent.class));
        assertFalse(handler.flush(mockNotifications()));
        verify(insightClient, times(1)).sendInsight(anyListOf(InsightEvent.class));
    }

    private Collection<AgentTrackingNotification> mockNotifications()
    {
        AgentTrackingNotification notification = mock(AgentTrackingNotification.class);
        return Arrays.asList(notification);
    }

}
