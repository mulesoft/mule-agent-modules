/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport.config;

/**
 * @author Walter Poch
 *         created on 10/23/15
 */
public enum HttpScheme
{
    HTTP("http"),
    HTTPS("https");

    private String value;

    HttpScheme(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
