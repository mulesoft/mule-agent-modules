package com.mulesoft.agent.eventtracker.cloudhub.source;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.mulesoft.agent.domain.tracking.FlowSourceEvent;
import com.mulesoft.agent.eventtracker.cloudhub.S3StorageException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.ch.client.proxy.s3.S3FileStorageProxy;

public class CloudhubS3ReplayStoreTest
{

    private S3FileStorageProxy s3;

    CloudhubS3ReplayStore store;

    @Before
    public void setUp() throws Exception
    {
        System.setProperty("s3.tracking.accessKey", "accessKey");
        System.setProperty("s3.tracking.secretKey", "secretKey");
        System.setProperty("s3.tracking.bucket", "bucket");
        System.setProperty("application.id", "appId");
        CloudhubS3Configuration s3Config = new CloudhubS3Configuration();
        s3Config.setEncryptionKey("encryptionKey");
        s3Config.setConnectionTimeout(3000);
        s3Config.setSocketTimeout(5000);
        s3Config.setMaxConnections(60);
        store = new CloudhubS3ReplayStore(s3Config);
        store.init();
        s3 = spy(store.getS3());
        store.setS3(s3);

        S3Object s3Object = mock(S3Object.class);
        when(s3Object.getObjectContent()).thenReturn(createEventStream());
        doReturn(s3Object).when(s3).getFileVersion("bucket", "ch-appName-appId/messageId/flowName", null);
        doReturn(null).when(s3).getFileVersionMetadata(anyString(), anyString(), anyString());
        doReturn(null).when(s3).putFileVersion(anyString(), anyString(), any(InputStream.class),
                any(ObjectMetadata.class));
    }

    @Test
    public void testGetFlowEvent()
    {
        FlowSourceEvent event = store.getFlowSourceEvent("appName", "flowName", "messageId",
                Thread.currentThread().getContextClassLoader());

        assertEquals("appName", event.getApplicationName());
        assertEquals("flowName", event.getFlowName());
        assertEquals("messageId", event.getMessageId());
        assertEquals("payload", event.getPayload());
        assertEquals(Collections.emptyMap(), event.getInvocationProperties());
        assertEquals(Collections.emptyMap(), event.getInboundProperties());
        assertEquals(Collections.emptyMap(), event.getOutboundProperties());
        assertEquals(Collections.emptyMap(), event.getSessionProperties());
    }

    @Test
    public void testGetFlowEventNull()
    {
        assertNull(store.getFlowSourceEvent("a", "f", "m", Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void testPutFlowEvent() throws IOException, S3StorageException
    {
        ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ByteArrayInputStream> streamCaptor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
        ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);

        store.putFlowSourceEvent(Arrays.asList(createEvent(), createEvent()));

        verify(s3, times(2)).putFileVersion(bucketCaptor.capture(), pathCaptor.capture(), streamCaptor.capture(),
                metadataCaptor.capture());

        assertEquals("bucket", bucketCaptor.getAllValues().get(0));
        assertEquals("bucket", bucketCaptor.getAllValues().get(1));
        assertEquals("ch-appName-appId/messageId/flowName", pathCaptor.getAllValues().get(0));
        assertEquals("ch-appName-appId/messageId/flowName", pathCaptor.getAllValues().get(1));
        byte[] bytes = FlowSourceStreamCodec.toBytes(createEvent());
        assertArrayEquals(bytes, IOUtils.toByteArray(streamCaptor.getAllValues().get(0)));
        assertArrayEquals(bytes, IOUtils.toByteArray(streamCaptor.getAllValues().get(1)));
        assertEquals(bytes.length, metadataCaptor.getAllValues().get(0).getContentLength());
        assertEquals(bytes.length, metadataCaptor.getAllValues().get(1).getContentLength());
    }

    @Test
    public void testPutFlowEventDuplicate() throws S3StorageException
    {
        doReturn(null).doReturn(new ObjectMetadata()).when(s3).getFileVersionMetadata(anyString(), anyString(),
                anyString());
        store.putFlowSourceEvent(Arrays.asList(createEvent(), createEvent()));

        verify(s3, times(1)).putFileVersion(anyString(), anyString(), any(InputStream.class),
                any(ObjectMetadata.class));
    }

    @Test(expected = S3StorageException.class)
    public void testPutFlowEventExcepted() throws S3StorageException
    {
        doThrow(Exception.class).when(s3).putFileVersion(anyString(), anyString(), any(InputStream.class),
                any(ObjectMetadata.class));
        store.putFlowSourceEvent(Arrays.asList(createEvent(), createEvent()));
    }

    @Test(expected = InitializationException.class)
    public void testInitFailedNoAccessKey() throws InitializationException
    {
        System.clearProperty("s3.tracking.accessKey");
        store.init();
    }

    @Test(expected = InitializationException.class)
    public void testInitFailedNoSecretKey() throws InitializationException
    {
        System.clearProperty("s3.tracking.secretKey");
        store.init();
    }

    @Test(expected = InitializationException.class)
    public void testInitFailedNoBucket() throws InitializationException
    {
        System.clearProperty("s3.tracking.bucket");
        store.init();
    }

    @Test(expected = NullPointerException.class)
    public void testInitFailedNoAppId() throws InitializationException
    {
        System.clearProperty("application.id");
        store.init();
    }

    private S3ObjectInputStream createEventStream()
    {
        return new S3ObjectInputStream(new ByteArrayInputStream(FlowSourceStreamCodec.toBytes(createEvent())), null);
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
