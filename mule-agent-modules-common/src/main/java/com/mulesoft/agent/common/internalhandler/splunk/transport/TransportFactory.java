/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.mulesoft.agent.AgentConfigurationException;

/**
 * @author Walter Poch
 *         created on 10/28/15
 * @param <T> Message type.
 */
public interface TransportFactory<T>
{
    Transport<T> create() throws AgentConfigurationException;
}
