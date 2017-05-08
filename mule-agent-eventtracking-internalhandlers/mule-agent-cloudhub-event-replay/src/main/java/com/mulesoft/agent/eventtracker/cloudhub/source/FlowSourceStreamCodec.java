package com.mulesoft.agent.eventtracker.cloudhub.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Optional;
import com.mulesoft.agent.domain.tracking.FlowSourceEvent;

/**
 * Serialize and deserialize {@link FlowSourceEvent} for S3 storage.
 */
final class FlowSourceStreamCodec
{
    private FlowSourceStreamCodec()
    {
    }

    public static byte[] toBytes(FlowSourceEvent event)
    {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos))
        {
            out.writeObject(wrapAnyInputStreams(event.getInboundProperties()));
            out.writeObject(wrapAnyInputStreams(event.getInvocationProperties()));
            out.writeObject(wrapAnyInputStreams(event.getOutboundProperties()));
            out.writeObject(wrapAnyInputStreams(event.getSessionProperties()));
            out.writeObject(Optional.fromNullable(wrapIfInputStream(event.getPayload())));
            return bos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static FlowSourceEvent fromStreamAndClose(InputStream in, ClassLoader classLoader)
    {
        try (MultipleClassLoaderObjectInputStream oin = new MultipleClassLoaderObjectInputStream(classLoader, in))
        {
            FlowSourceEvent event = new FlowSourceEvent();
            event.setInboundProperties(unwrapAnyInputStreams(readMap(oin)));
            event.setInvocationProperties(unwrapAnyInputStreams(readMap(oin)));
            event.setOutboundProperties(unwrapAnyInputStreams(readMap(oin)));
            event.setSessionProperties(unwrapAnyInputStreams(readMap(oin)));
            event.setPayload(unwrapIfInputStream(Optional.class.cast(oin.readObject()).orNull()));
            return event;
        }
        catch (IOException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> wrapAnyInputStreams(Map<String, Object> map)
    {
        Map<String, Object> mutable = new LinkedHashMap<>();
        for (Entry<String, Object> entry : map.entrySet())
        {
            mutable.put(entry.getKey(), wrapIfInputStream(entry.getValue()));
        }
        return mutable;
    }

    private static Map<String, Object> unwrapAnyInputStreams(Map<String, Object> map)
    {
        for (Entry<String, Object> entry : map.entrySet())
        {
            entry.setValue(unwrapIfInputStream(entry.getValue()));
        }
        return map;
    }

    private static Object wrapIfInputStream(Object in)
    {
        if (in == null)
        {
            return null;
        }
        if (in instanceof InputStream)
        {
            try
            {
                return new InputStreamHolder(IOUtils.toByteArray(InputStream.class.cast(in)));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return in;
    }

    private static Object unwrapIfInputStream(Object in)
    {
        if (in == null)
        {
            return null;
        }
        if (in instanceof InputStreamHolder)
        {
            return new ByteArrayInputStream(InputStreamHolder.class.cast(in).bytes);
        }
        return in;
    }

    /**
     * Internal holder to wrap {@link InputStream} when storing in S3.
     */
    private static final class InputStreamHolder implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final byte[] bytes;

        private InputStreamHolder(byte[] bytes)
        {
            if (bytes == null)
            {
                throw new NullPointerException("Bytes cannot be null");
            }
            this.bytes = bytes;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readMap(MultipleClassLoaderObjectInputStream oin)
            throws IOException, ClassNotFoundException
    {
        return (Map<String, Object>) oin.readObject();
    }

}
