/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.api.AnypointMonitoringIngestAPIClient;
import com.mulesoft.agent.monitoring.publisher.api.resource.targets.id.model.IdPOSTBody;
import com.mulesoft.agent.services.OnOffSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to a running Nagios instance.
 * Utilizes the NCSA protocol.
 * </p>
 */
@Named("mule.agent.ingest.metrics.internal.handler")
@Singleton
public class IngestMonitorPublisher extends BufferedHandler<List<Metric>>
{

    private final static Logger LOGGER = LoggerFactory.getLogger(IngestMonitorPublisher.class);

    @Configurable("http://0.0.0.0:8070")
    String ingestEndpoint;

    @Configurable("1")
    String apiVersion;

    @Configurable("6c001e57-aa67-431e-b5cf-8ad1145c5f30")
    String organizationId;

    @Configurable("7a964cfd-5cda-47b6-bcfe-817fa0f00362")
    String environmentId;

    @Configurable("asdqwe")
    String targetId;

    @Configurable("true")
    protected boolean enabled;

    @Override
    protected boolean canHandle(List<Metric> metrics)
    {
        return true;
    }

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
    }



    private IdPOSTBody processMetrics(Collection<List<Metric>> collection)
    {
        return new MetricTransformer().transform(collection);
    }

    private boolean send(IdPOSTBody body)
    {
        try
        {
            AnypointMonitoringIngestAPIClient
                    .create(ingestEndpoint, apiVersion, organizationId, environmentId)
                    .targets.id(targetId).post(body);
            LOGGER.info("Published metrics to Ingest successfully");
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Could not publish metrics to Ingest: ", e);
            return false;
        }
    }

    @Override
    protected boolean flush(Collection<List<Metric>> collection)
    {
        IdPOSTBody body = processMetrics(collection);
        LOGGER.info(String.format("publishing %s to ingest api.", body));
        return send(body);
    }
}
