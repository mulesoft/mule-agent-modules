/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Sets;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.api.AnypointMonitoringIngestAPIClient;
import com.mulesoft.agent.monitoring.publisher.resource.targets.id.model.CpuUsage;
import com.mulesoft.agent.monitoring.publisher.resource.targets.id.model.IdPOSTBody;
import com.mulesoft.agent.monitoring.publisher.resource.targets.id.model.MemoryTotal;
import com.mulesoft.agent.monitoring.publisher.resource.targets.id.model.MemoryUsage;
import com.mulesoft.agent.services.OnOffSwitch;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

    @Configurable("0.0.0.0")
    String ingestServer;

    @Configurable("8070")
    int ingestPort;

    @Configurable("1.0")
    String apiVersion;

    @Configurable("6c001e57-aa67-431e-b5cf-8ad1145c5f30")
    String organizationId;

    @Configurable("7a964cfd-5cda-47b6-bcfe-817fa0f00362")
    String environmentId;

    @Configurable("asdqwe")
    String applicationId;

    @Configurable("true")
    protected boolean enabled;

    @Override
    protected boolean canHandle(List<Metric> metrics)
    {
        return true;
    }

    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException {
        if(this.enabledSwitch == null) {
            this.enabledSwitch = OnOffSwitch.newNullSwitch(this.enabled);
            if (this.buffer == null) {
                this.buffer = new BufferConfiguration();
                this.buffer.setType(BufferType.MEMORY);
                this.buffer.setRetryCount(1);
                this.buffer.setFlushFrequency(10000L);
                this.buffer.setMaximumCapacity(30);
            }
        }
        LOGGER.info(ToStringBuilder.reflectionToString(this.buffer));
        LOGGER.info(String.format("enabled: %s", String.valueOf(this.enabledSwitch.isEnabled())));
    }

    private IdPOSTBody processMetrics(Collection<List<Metric>> collection)
    {
        Set<CpuUsage> cpuUsage = Sets.newHashSet();
        Set<MemoryUsage> memoryUsage = Sets.newHashSet();
        Set< MemoryTotal> memoryTotal = Sets.newHashSet();
        for (List<Metric> metrics : collection) {
            for (Metric metric : metrics) {
                Double value = metric.getValue().doubleValue();
                Date date = new Date(metric.getTimestamp());
                if (metric.getName().contains("java.lang:type=OperatingSystem:CPU")) {
                    CpuUsage usage = new CpuUsage(date, value, value, value, value, 1d);
                    LOGGER.info(String.format("Registered cpu usage: %s", usage.toString()));
                    cpuUsage.add(usage);
                } else if (metric.getName().contains("java.lang:type=Memory:heap used")) {
                    MemoryUsage usage = new MemoryUsage(date, value, value, value, value, 1d);
                    LOGGER.info(String.format("Registered memory usage: %s", usage.toString()));
                    memoryUsage.add(usage);
                } else if (metric.getName().contains("java.lang:type=Memory:heap total")) {
                    MemoryTotal total = new MemoryTotal(date, value, value, value, value, 1d);
                    LOGGER.info(String.format("Registered memory total: %s", total.toString()));
                    memoryTotal.add(total);
                }
            }
        }
        return new IdPOSTBody(null, cpuUsage, memoryUsage, memoryTotal);
    }

    private boolean send(IdPOSTBody body)
    {
        try {
            String endpoint = String.format("http://%s:%s", ingestServer, String.valueOf(ingestPort));
            LOGGER.info(String.format("Sending %s to Ingest Api at endpoint: %s", body.toString(), endpoint));
            AnypointMonitoringIngestAPIClient
                    .create(endpoint, apiVersion, organizationId, environmentId)
                    .targets.id(applicationId).post(body);
            LOGGER.info("It all went fine :D");
            return true;
        } catch (Exception e) {
            LOGGER.error("oops... something went wrong: ", e);
            return false;
        }
    }

    @Override
    protected boolean flush(Collection<List<Metric>> collection)
    {
        IdPOSTBody body = processMetrics(collection);
        return send(body);
    }
}
