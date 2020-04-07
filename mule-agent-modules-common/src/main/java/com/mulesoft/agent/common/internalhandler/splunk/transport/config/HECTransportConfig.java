/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport.config;

import com.mulesoft.agent.exception.AgentConfigurationException;
import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.Type;
import com.splunk.SSLSecurityProtocol;
import org.apache.commons.lang.StringUtils;

/**
 * @author Walter Poch
 *         created on 10/23/15
 */
public class HECTransportConfig extends HttpBasedSplunkConfig
{
    @Configurable(type = Type.DYNAMIC)
    private String token;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    @Override
    public String toString()
    {
        return "HECTransportConfig{"
                + "token='" + token + '\''
                + "} " + super.toString();
    }

    /**
     * HecTransportConfig builder.
     */
    public static class Builder
    {
        private HECTransportConfig config = new HECTransportConfig();

        public Builder(AbstractSplunkInternalHandler internalHandler) throws AgentConfigurationException
        {
            try
            {
                config.setToken(internalHandler.getToken());
                config.setHost(internalHandler.getHost());
                config.setPort(internalHandler.getPort());
                config.setScheme(HttpScheme.valueOf(internalHandler.getScheme().toUpperCase()));
                config.setSslSecurityProtocol(SSLSecurityProtocol.valueOf(internalHandler.getSslSecurityProtocol()));
                config.setIndex(internalHandler.getSplunkIndexName());
                config.setSource(internalHandler.getSplunkSource());
                config.setSourceType(internalHandler.getSplunkSourceType());
                config.setAcceptAnyCertificate(internalHandler.getAcceptAnyCertificate());
            }
            catch (Exception ex)
            {
                throw new AgentConfigurationException("There was an error reading the Splunk Configuration.", ex);
            }

            if (StringUtils.isEmpty(this.config.getHost())
                    || StringUtils.isEmpty(this.config.getToken())
                    || this.config.getScheme() == null
                    || this.config.getPort() < 1
                    || StringUtils.isEmpty(this.config.getIndex())
                    || StringUtils.isEmpty(this.config.getSource())
                    || this.config.getAcceptAnyCertificate() == null
                    || StringUtils.isEmpty(this.config.getSourceType()))
            {
                throw new AgentConfigurationException("Please review configuration; "
                        + "you must configure the following properties: token and host.");
            }
        }

        public HECTransportConfig build()
        {
            return this.config;
        }
    }
}
