package com.mulesoft.agent.monitoring.publisher.ingest.decorator;

import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;

/**
 * MetricSample decorator that converts the values of a sample to percentages (0.9 to 90).
 */
public class PercentageMetricSampleDecorator extends MetricSampleDecorator
{

    public PercentageMetricSampleDecorator(MetricSample sample)
    {
        super(sample);
    }

    @Override
    protected Double processValue(Double value)
    {
        return value * 100;
    }

}
