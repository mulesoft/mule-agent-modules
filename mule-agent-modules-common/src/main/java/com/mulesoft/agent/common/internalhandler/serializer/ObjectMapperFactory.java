/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for object mapper factories.
 */
public interface ObjectMapperFactory
{
    ObjectMapper create();
}
