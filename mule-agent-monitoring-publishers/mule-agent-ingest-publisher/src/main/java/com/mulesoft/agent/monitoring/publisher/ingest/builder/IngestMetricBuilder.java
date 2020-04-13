/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.builder;

import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;

import javax.inject.Singleton;

/**
 * Builds an IngesMetric from a MetricSample.
 */
@Singleton
public class IngestMetricBuilder
{

    public IngestMetric build(MetricSample sample)
    {
        return new IngestMetric()
                .withTime(sample.getDate())
                .withMax(sample.getMax())
                .withMin(sample.getMin())
                .withSum(sample.getSum())
                .withAvg(sample.getAvg())
                .withCount(sample.getCount());
    }

}
