/*
 *  (c) 2003-2016 MuleSoft, Inc. This software is protected under international copyright
 *  law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 *  (or other master license agreement) separately entered into in writing between you and
 *  MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.common.internalhandler.splunk.transport.config.HECTransportConfig;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class HECTransportTest
{
    @Test
    public void givenAnExceptionSerializingTheTransportReturnsNull() throws JsonProcessingException
    {
        ObjectMapper mapper = mock(ObjectMapper.class);
        doThrow(IndexOutOfBoundsException.class).when(mapper).writeValueAsString(any());

        HECTransport transport = new HECTransport(new HECTransportConfig(), mapper);

        String serialized = transport.serialize(new HECTransport.HECMessage<>(new AgentTrackingNotification.TrackingNotificationBuilder().build(), null, null, null, null));
        assertNull(serialized);
    }

    @Test
    public void toStringOfHECMessageDoesntThrowNPE()
    {
        String toString = new HECTransport.HECMessage<>(new AgentTrackingNotification.TrackingNotificationBuilder().build(), null, null, null, null).toString();
        assertNotNull(toString);
    }
}
