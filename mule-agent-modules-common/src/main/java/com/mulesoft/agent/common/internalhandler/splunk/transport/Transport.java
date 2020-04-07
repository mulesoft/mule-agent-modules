/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.mulesoft.agent.handlers.exception.InitializationException;

import java.util.Collection;

/**
 * @author Walter Poch
 *         created on 10/23/15
 * @param <T> Message type.
 */
public interface Transport<T>
{
    void init() throws InitializationException;

    boolean send(final Collection<T> messages);

    void dispose();
}
