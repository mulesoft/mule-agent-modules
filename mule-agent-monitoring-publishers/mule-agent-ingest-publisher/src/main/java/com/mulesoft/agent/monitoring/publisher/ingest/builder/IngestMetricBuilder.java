package com.mulesoft.agent.monitoring.publisher.ingest.builder;

import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.model.MetricSample;

import javax.inject.Singleton;

/**
 * Builds an IngesMetric from a MetricSample.
 */
@Singleton
public class IngestMetricBuilder {

    public IngestMetric build(MetricSample sample) {
        return new IngestMetric()
                .withTime(sample.getDate())
                .withMax(sample.getMax())
                .withMin(sample.getMin())
                .withSum(sample.getSum())
                .withAvg(sample.getAvg())
                .withCount(sample.getCount());
    }

}
