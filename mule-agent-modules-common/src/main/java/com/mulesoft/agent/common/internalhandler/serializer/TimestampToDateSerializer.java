package com.mulesoft.agent.common.internalhandler.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * Serializer to transform timestamps to java.util.Date.
 */
public class TimestampToDateSerializer extends JsonSerializer<Long>
{
    @Override
    public void serialize(Long value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException
    {
        Date date = new Date(value);
        jgen.writeObject(date);
    }
}
