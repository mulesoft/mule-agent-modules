/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.decorator;

import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;

/**
 * MetricSample decorator that converts the values of a sample to percentages (0.9 to 90).
 */
public class PercentageMetricSampleDecorator extends MetricSampleDecorator
{

    private static final int PERCENTAGE = 100;

    public PercentageMetricSampleDecorator(MetricSample sample)
    {
        super(sample);
    }

    @Override
    protected Double processValue(Double value)
    {
        return value * PERCENTAGE;
    }

    @Override
    public String toString()
    {
        return "PercentageMetricSampleDecorator{} " + super.toString();
    }

}
