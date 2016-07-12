package com.mulesoft.agent.monitoring.publisher.ingest.builder;

import com.google.common.collect.Sets;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * Builds the body of an application metric post body from the 3 metrics it contains.
 */
@Singleton
public class IngestApplicationMetricPostBodyBuilder {

    public IngestApplicationMetricPostBody build(IngestMetric messageCount, IngestMetric responseTime, IngestMetric errorCount)
    {
        return this.build(Sets.newHashSet(messageCount), Sets.newHashSet(responseTime), Sets.newHashSet(errorCount));
    }

    public IngestApplicationMetricPostBody build(Set<IngestMetric> messageCount, Set<IngestMetric> responseTime, Set<IngestMetric> errorCount)
    {
        return new IngestApplicationMetricPostBody(messageCount, responseTime, errorCount);
    }

}
