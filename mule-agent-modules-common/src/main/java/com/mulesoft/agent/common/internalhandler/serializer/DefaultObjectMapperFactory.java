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
