package com.mulesoft.agent.eventtracker.cloudhub.source;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;
import java.util.Collections;

import org.junit.Test;

import com.mulesoft.agent.domain.tracking.FlowSourceEvent;

public class MultipleClassLoaderObjectInputStreamTest
{

    private ObjectStreamClass objectStreamClass = mock(ObjectStreamClass.class);

    @Test(expected = ClassNotFoundException.class)
    public void testFailure() throws IOException, ClassNotFoundException
    {

        when(objectStreamClass.getName()).thenReturn("any");

        try (MultipleClassLoaderObjectInputStream loader = new MultipleClassLoaderObjectInputStream(new ClassLoader()
        {
            @Override
            public Class<?> loadClass(String s) throws ClassNotFoundException
            {
                throw new ClassNotFoundException();
            }
        }, new ByteArrayInputStream(FlowSourceStreamCodec.toBytes(createEvent()))))
        {
            loader.resolveClass(objectStreamClass);
        }
    }

    private FlowSourceEvent createEvent()
    {
        FlowSourceEvent event = new FlowSourceEvent();
        event.setApplicationName("appName");
        event.setFlowName("flowName");
        event.setMessageId("messageId");
        event.setPayload("payload");
        event.setInvocationProperties(Collections.<String, Object> emptyMap());
        event.setInboundProperties(Collections.<String, Object> emptyMap());
        event.setOutboundProperties(Collections.<String, Object> emptyMap());
        event.setSessionProperties(Collections.<String, Object> emptyMap());
        return event;
    }

}
