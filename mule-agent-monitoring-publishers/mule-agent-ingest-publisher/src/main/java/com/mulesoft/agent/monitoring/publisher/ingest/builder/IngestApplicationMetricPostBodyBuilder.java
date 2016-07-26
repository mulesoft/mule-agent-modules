package com.mulesoft.agent.monitoring.publisher.ingest.builder;

import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;

import javax.inject.Singleton;
import java.util.Set;

/**
 * Builds the body of an application metric post body from the 3 metrics it contains.
 */
@Singleton
public class IngestApplicationMetricPostBodyBuilder {

    public IngestApplicationMetricPostBody build(Set<IngestMetric> messageCount, Set<IngestMetric> responseTime, Set<IngestMetric> errorCount)
    {
        return new IngestApplicationMetricPostBody(messageCount, responseTime, errorCount);
    }

}
