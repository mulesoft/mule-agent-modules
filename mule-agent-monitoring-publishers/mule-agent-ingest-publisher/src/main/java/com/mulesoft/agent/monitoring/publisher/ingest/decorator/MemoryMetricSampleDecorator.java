package com.mulesoft.agent.monitoring.publisher.ingest.decorator;

import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;

/**
 * MetricSample decorator that converts the values of a sample from bytes to megabytes.
 */
public class MemoryMetricSampleDecorator extends MetricSampleDecorator
{

    public MemoryMetricSampleDecorator(DefaultMetricSample sample)
    {
        super(sample);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Double processValue(Double value)
    {
        return value / (1024 * 1024);
    }

}
