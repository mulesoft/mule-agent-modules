package com.mulesoft.agent.eventtracker.cloudhub.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;

/**
 * Classloader for a serialized {@link com.mulesoft.agent.domain.tracking.FlowSourceEvent.FlowSourceEvent} from S3.
 */
class MultipleClassLoaderObjectInputStream extends ClassLoaderObjectInputStream
{

    MultipleClassLoaderObjectInputStream(ClassLoader classLoader, InputStream inputStream)
            throws IOException, StreamCorruptedException
    {
        super(classLoader, inputStream);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException
    {
        try
        {
            return super.resolveClass(objectStreamClass);
        }
        catch (ClassNotFoundException e)
        {
            return Class.forName(objectStreamClass.getName(), false, Thread.currentThread().getContextClassLoader());
        }
    }
}
