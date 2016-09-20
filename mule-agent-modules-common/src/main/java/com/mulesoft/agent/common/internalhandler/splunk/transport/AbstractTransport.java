/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * @author Walter Poch
 *         created on 10/27/15
 */
public abstract class AbstractTransport<T> implements Transport<T>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTransport.class);
    protected final static String CHARSET = "UTF-8";
    protected final static String LINE_BREAKER = "\r\n";

    private final ObjectMapper objectMapper;

    protected AbstractTransport(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    /**
     * <p>
     * Serializes the given message to be used on the transport layer.
     * </p>
     *
     * @param message The tracking message to serialize.
     * @return A JSON string that represents the serialized message.
     */
    protected String serialize(Object message)
    {
        try
        {
            return this.objectMapper.writeValueAsString(message) + LINE_BREAKER;
        }
        catch (Throwable t)
        {
            // Return null so will be excluded from the batch
            LOGGER.warn("Couldn't serialize the message: {}, discarding it. Error: {}", message, t.getMessage());
            LOGGER.debug("Exception: ", t);
            return null;
        }
    }

    /**
     * <p>
     *     Writes a serialized the message to the given Output Stream.
     * </p>
     * @param message The tracking message to serialize on the Output Stream.
     * @param outputStream The output stream to write the message to.
     */
    protected void serializeTo(T message, OutputStream outputStream)
    {
        try
        {
            String serializer = serialize(message);
            if (StringUtils.isNotBlank(serializer))
            {
                outputStream.write(serializer.getBytes(CHARSET));
                outputStream.flush();
            }
        }
        catch (Throwable t)
        {
            LOGGER.warn("Couldn't write the message: {} to the output stream, discarding it. Error: {}", message, t.getMessage());
            LOGGER.debug("Exception: ", t);
        }
    }
}
