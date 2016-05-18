package com.mulesoft.agent.monitoring.publisher.ingest.builder;

import com.google.common.collect.Sets;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;

import javax.inject.Singleton;

/**
 * Builds the body of a target metric post body from the 3 metrics it contains.
 */
@Singleton
public class IngestTargetMetricPostBodyBuilder {

    public IngestTargetMetricPostBody build(IngestMetric cpuUsage, IngestMetric memoryUsage, IngestMetric memoryTotal)
    {
        return new IngestTargetMetricPostBody(
                Sets.newHashSet(cpuUsage),
                Sets.newHashSet(memoryUsage),
                Sets.newHashSet(memoryTotal)
        );
    }

}
