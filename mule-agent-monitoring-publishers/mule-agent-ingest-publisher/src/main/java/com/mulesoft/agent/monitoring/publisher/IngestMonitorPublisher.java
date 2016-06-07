package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.clients.AuthenticationProxyClient;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.common.SecurityConfiguration;
import com.mulesoft.agent.handlers.internal.client.DefaultAuthenticationProxyClient;
import com.mulesoft.agent.monitoring.publisher.ingest.AnypointMonitoringIngestAPIClient;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestMetricBuilder;
import com.mulesoft.agent.services.OnOffSwitch;
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
    private SecurityConfiguration securityConfiguration;

    @Configurable("http://localhost:8088")
    private String authProxyEndpoint;

    @Configurable("1")
    private String apiVersion;

    @Configurable("true")
    private Boolean enabled;

    @Inject
    protected IngestMetricBuilder metricBuilder;

    protected AnypointMonitoringIngestAPIClient client;

    @Override
    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException
    {
        if(this.enabledSwitch == null)
        {
            this.enabledSwitch = OnOffSwitch.newNullSwitch(this.enabled);
            if (this.buffer == null)
            {
                this.buffer = new BufferConfiguration();
                this.buffer.setType(BufferType.MEMORY);
                this.buffer.setRetryCount(1);
                this.buffer.setFlushFrequency(60000L);
                this.buffer.setMaximumCapacity(100);
            }
        }
        AuthenticationProxyClient authProxyClient = DefaultAuthenticationProxyClient.create(authProxyEndpoint, securityConfiguration);
        this.client = AnypointMonitoringIngestAPIClient.create(apiVersion, authProxyClient);
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
        LOGGER.info("publishing metrics to ingest api.");
        return send(collection);
    }
}
