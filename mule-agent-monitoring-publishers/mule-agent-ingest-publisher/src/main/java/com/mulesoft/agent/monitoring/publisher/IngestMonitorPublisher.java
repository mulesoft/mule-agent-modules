package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.clients.AuthenticationProxyClient;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.common.AuthenticationProxyConfiguration;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.agent.handlers.internal.buffer.DiscardingMessageBufferConfigurationFactory;
import com.mulesoft.agent.handlers.internal.client.DefaultAuthenticationProxyClient;
import com.mulesoft.agent.monitoring.publisher.ingest.AnypointMonitoringIngestAPIClient;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestMetricBuilder;
import com.mulesoft.agent.services.OnOffSwitch;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by sebastianvinci on 5/30/16.
 */
public abstract class IngestMonitorPublisher<T> extends BufferedHandler<T>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(IngestMonitorPublisher.class);

    @Configurable("{}")
    private AuthenticationProxyConfiguration authenticationProxy;

    @Configurable("1")
    private String apiVersion;

    @Configurable("true")
    private Boolean enabled;

    @Inject
    protected IngestMetricBuilder metricBuilder;

    protected AnypointMonitoringIngestAPIClient client;

    protected OnOffSwitch enabledSwitch;

    @Override
    public void enable(boolean state)
            throws AgentEnableOperationException
    {
        this.enabledSwitch.switchTo(state);
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabledSwitch.isEnabled();
    }

    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException
    {
        if (this.enabledSwitch == null)
        {
            this.enabledSwitch = OnOffSwitch.newNullSwitch(this.enabled);
        }
    }

    @Override
    public void initialize() throws InitializationException
    {
        if (!this.checkConfiguration()) {
            throw new InitializationException("Could not initialize ingest monitor publisher. Its configuration is invalid.");
        }
        AuthenticationProxyClient authProxyClient = DefaultAuthenticationProxyClient.create(authenticationProxy);
        this.client = AnypointMonitoringIngestAPIClient.create(apiVersion, authProxyClient);
        super.initialize();
    }

    @Override
    public BufferConfiguration getBuffer()
    {
        if (this.buffer == null)
        {
            this.buffer = new DiscardingMessageBufferConfigurationFactory().create(60000L, 100, BufferType.MEMORY, null);
        }
        return this.buffer;
    }

    private boolean checkConfiguration()
    {
        return this.authenticationProxy != null &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getKeyStoreAlias()) &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getKeyStoreAliasPassword()) &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getKeyStoreFile()) &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getKeyStorePassword()) &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getTrustStoreFile()) &&
                this.authenticationProxy.getEndpoint() != null;
    }

    protected abstract boolean send(Collection<T> collection);

    @Override
    protected boolean canHandle(T metrics)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<T> collection)
    {
        return send(collection);
    }
}
