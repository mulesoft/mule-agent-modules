/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

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
