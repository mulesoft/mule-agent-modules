package com.mulesoft.agent.eventtracker.cloudhub.source;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.mulesoft.agent.domain.tracking.FlowSourceEvent;

public class FlowSourceStreamCodecTest
{

    Map<String, String> payload = Collections.singletonMap("key", "value");

    @Test
    public void testTransform() throws IOException
    {
        byte[] bytes = FlowSourceStreamCodec.toBytes(createEvent(payload));
        InputStream in = new ByteArrayInputStream(bytes);
        System.gc();

        FlowSourceEvent event = FlowSourceStreamCodec.fromStreamAndClose(in,
                Thread.currentThread().getContextClassLoader());
        assertNull(event.getApplicationName());
        assertNull(event.getFlowName());
        assertNull(event.getMessageId());
        assertEquals(payload, event.getPayload());
        assertEquals(Collections.singletonMap("iv", "value"), event.getInvocationProperties());
        assertEquals(null, event.getInboundProperties().get("in"));
        assertArrayEquals("properties".getBytes(),
                IOUtils.toByteArray((InputStream) event.getOutboundProperties().get("ou")));
        assertTrue(event.getSessionProperties().isEmpty());
    }

    @Test
    public void testEmptyPayload()
    {
        byte[] bytes = FlowSourceStreamCodec.toBytes(createEvent(null));
        InputStream in = new ByteArrayInputStream(bytes);
        System.gc();

        FlowSourceEvent event = FlowSourceStreamCodec.fromStreamAndClose(in,
                Thread.currentThread().getContextClassLoader());

        assertNull(event.getPayload());
    }

    @Test
    public void testNotSerializable()
    {
        try
        {
            FlowSourceStreamCodec.toBytes(createEvent(new Object()));
        }
        catch (RuntimeException e)
        {
            assertEquals(NotSerializableException.class, e.getCause().getClass());
        }
    }

    private FlowSourceEvent createEvent(Object payload)
    {
        FlowSourceEvent event = new FlowSourceEvent();
        event.setApplicationName("appName");
        event.setFlowName("flowName");
        event.setMessageId("messageId");
        event.setPayload(payload);
        event.setInvocationProperties(Collections.singletonMap("iv", (Object) "value"));
        event.setInboundProperties(Collections.singletonMap("in", null));
        event.setOutboundProperties(
                Collections.singletonMap("ou", (Object) new ByteArrayInputStream("properties".getBytes())));
        event.setSessionProperties(Collections.<String, Object> emptyMap());
        return event;
    }

}
