/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.api.AnypointMonitoringIngestAPIClient;
import com.mulesoft.agent.monitoring.publisher.resource.targets.id.model.IdPOSTBody;
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


    @Override
    protected boolean canHandle(List<Metric> metrics)
    {
        String endpoint = "http://arm-mon-ingest-int.dev.cloudhub.io";
        String apiVersion = "1.0";
        String organizationId = "6c001e57-aa67-431e-b5cf-8ad1145c5f30";
        String environmentId = "7a964cfd-5cda-47b6-bcfe-817fa0f00362";
        AnypointMonitoringIngestAPIClient client = AnypointMonitoringIngestAPIClient.create(endpoint, apiVersion, organizationId, environmentId);
        client.targets.id("application-id").post(new IdPOSTBody());
        return true;
    }

    @Override
    protected boolean flush(Collection<List<Metric>> collection)
    {
        return false;
    }
}
