package com.mulesoft.agent.monitoring.publisher.model;

import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;

/**
 * Convenience model to clarify ingest request by application classification.
 */
public class IngestApplicationMetric {

    private final String applicationName;
    private final IngestApplicationMetricPostBody body;

    public IngestApplicationMetric(String applicationName, IngestApplicationMetricPostBody body) {
        this.applicationName = applicationName;
        this.body = body;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public IngestApplicationMetricPostBody getBody() {
        return body;
    }
}
