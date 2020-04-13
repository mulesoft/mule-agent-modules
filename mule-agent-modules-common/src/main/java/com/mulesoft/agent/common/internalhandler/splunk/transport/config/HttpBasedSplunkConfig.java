/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport.config;

import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.splunk.SSLSecurityProtocol;

/**
 * @author Walter Poch
 *         created on 10/23/15
 */
public abstract class HttpBasedSplunkConfig extends AbstractSplunkConfig
{

    @Configurable(value = "https", type = Type.DYNAMIC)
    private HttpScheme scheme;
    @Configurable(value = "TLSv1_2", type = Type.DYNAMIC)
    private SSLSecurityProtocol sslSecurityProtocol;
    @Configurable(value = "main", type = Type.DYNAMIC)
    private String index;
    @Configurable(value = "mule", type = Type.DYNAMIC)
    private String source;
    @Configurable(value = "mule", type = Type.DYNAMIC)
    private String sourceType;
    @Configurable(value = "true", type = Type.DYNAMIC)
    private Boolean acceptAnyCertificate;

    public HttpScheme getScheme()
    {
        return scheme;
    }

    public void setScheme(HttpScheme scheme)
    {
        this.scheme = scheme;
    }

    public SSLSecurityProtocol getSslSecurityProtocol()
    {
        return sslSecurityProtocol;
    }

    public void setSslSecurityProtocol(SSLSecurityProtocol sslSecurityProtocol)
    {
        this.sslSecurityProtocol = sslSecurityProtocol;
    }

    public String getIndex()
    {
        return index;
    }

    public void setIndex(String index)
    {
        this.index = index;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public void setAcceptAnyCertificate(Boolean acceptAnyCertificate)
    {
        this.acceptAnyCertificate = acceptAnyCertificate;
    }

    public Boolean getAcceptAnyCertificate()
    {
        return acceptAnyCertificate;
    }

    @Override
    public String toString()
    {
        return "HttpBasedSplunkConfig{"
                + "scheme=" + scheme
                + ", sslSecurityProtocol=" + sslSecurityProtocol
                + ", index='" + index + '\''
                + ", source='" + source + '\''
                + ", sourceType='" + sourceType + '\''
                + "} " + super.toString();
    }

}
