/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;

/**
 * @author Walter Poch
 *         created on 10/27/15
 * @param <T> Message type.
 */
public abstract class AbstractTransport<T> implements Transport<T>
{
    private static final Logger LOGGER = LogManager.getLogger(AbstractTransport.class);
    protected static final String CHARSET = "UTF-8";
    protected static final String LINE_BREAKER = "\r\n";

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
            LOGGER.warn("Couldn't serialize the message, discarding it. Error: {}", ExceptionUtils.getRootCauseMessage(t));
            LOGGER.debug("Message: {}", message);
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
