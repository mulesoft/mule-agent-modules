package com.mulesoft.agent.common.internalhandler.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for object mapper factories.
 */
public interface ObjectMapperFactory
{
    ObjectMapper create();
}
