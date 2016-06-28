package com.mulesoft.agent.monitoring.publisher.model;

/**
 * Created by svinci on 6/28/16.
 */
public class MemoryMetricSampleDecorator extends MetricSampleDecorator
{

    public MemoryMetricSampleDecorator(DefaultMetricSample sample)
    {
        super(sample);
    }

    @Override
    protected Double processValue(Double value)
    {
        return value / (1024 * 1024 * 1024);
    }

}
