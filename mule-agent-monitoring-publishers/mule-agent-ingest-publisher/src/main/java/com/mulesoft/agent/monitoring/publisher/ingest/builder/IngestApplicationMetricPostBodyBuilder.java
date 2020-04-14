/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.builder;

import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestFlowMetrics;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

/**
 * Builds the body of an application metric post body from the 3 metrics it contains.
 */
@Singleton
public class IngestApplicationMetricPostBodyBuilder
{

    public IngestApplicationMetricPostBody build(Set<IngestMetric> messageCount, Set<IngestMetric> responseTime, Set<IngestMetric> errorCount, Map<String, IngestFlowMetrics> flows)
    {
        return new IngestApplicationMetricPostBody(messageCount, responseTime, errorCount, flows);
    }

}
