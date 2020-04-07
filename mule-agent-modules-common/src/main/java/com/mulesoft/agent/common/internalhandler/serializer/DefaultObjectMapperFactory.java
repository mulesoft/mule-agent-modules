/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * Factory for default object mapper configuration.
 */
public class DefaultObjectMapperFactory implements ObjectMapperFactory
{
    private final String dateFormatPattern;

    public DefaultObjectMapperFactory(String dateFormatPattern)
    {
        this.dateFormatPattern = dateFormatPattern;
    }

    public ObjectMapper create()
    {
        ObjectMapper om = new ObjectMapper()
                .setDateFormat(new SimpleDateFormat(this.dateFormatPattern))
                // to allow serialization of "empty" POJOs (no properties to serialize)
                // (without this setting, an exception is thrown in those cases)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        om.getFactory().setCharacterEscapes(new CustomCharacterEscapes());

        return om;
    }
}
