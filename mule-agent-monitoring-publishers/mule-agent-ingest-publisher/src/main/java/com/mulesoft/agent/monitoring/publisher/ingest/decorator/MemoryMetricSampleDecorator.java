/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.decorator;

import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;

/**
 * MetricSample decorator that converts the values of a sample from bytes to megabytes.
 */
public class MemoryMetricSampleDecorator extends MetricSampleDecorator
{
    private static final long MB_DIVISOR = 1048576L;

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
        return value / MB_DIVISOR;
    }

    @Override
    public String toString()
    {
        return "MemoryMetricSampleDecorator{} " + super.toString();
    }
}
