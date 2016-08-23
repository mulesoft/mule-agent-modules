package com.mulesoft.agent.monitoring.publisher.model;

/**
 * Created by svinci on 6/28/16.
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
