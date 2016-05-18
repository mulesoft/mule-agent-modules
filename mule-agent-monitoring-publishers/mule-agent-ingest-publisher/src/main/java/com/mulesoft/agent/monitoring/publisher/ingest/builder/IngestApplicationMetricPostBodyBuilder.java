package com.mulesoft.agent.monitoring.publisher.ingest.builder;

import com.google.common.collect.Sets;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Builds the body of an application metric post body from the 3 metrics it contains.
 */
@Singleton
public class IngestApplicationMetricPostBodyBuilder {

    public IngestApplicationMetricPostBody build(IngestMetric messageCount, IngestMetric responseTime, IngestMetric errorCount)
    {
        return new IngestApplicationMetricPostBody(
                Sets.newHashSet(messageCount),
                Sets.newHashSet(responseTime),
                Sets.newHashSet(errorCount)
        );
    }

}
