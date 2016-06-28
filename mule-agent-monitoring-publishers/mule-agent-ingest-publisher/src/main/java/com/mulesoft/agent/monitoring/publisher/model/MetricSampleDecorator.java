package com.mulesoft.agent.monitoring.publisher.model;

import java.util.Date;

/**
 * Created by svinci on 6/28/16.
 */
public abstract class MetricSampleDecorator implements MetricSample
{

    private MetricSample sample;

    public MetricSampleDecorator(MetricSample sample)
    {
        this.sample = sample;
    }

    protected abstract Double processValue(Double value);

    @Override
    public Double getMin()
    {
        Double min = sample.getMin();
        return min != null ? this.processValue(min) : null;
    }

    @Override
    public Double getMax()
    {
        Double max = sample.getMax();
        return max != null ? this.processValue(max) : null;
    }

    @Override
    public Double getSum()
    {
        Double sum = sample.getSum();
        return sum != null ? this.processValue(sum) : null;
    }

    @Override
    public Double getAvg()
    {
        Double avg = sample.getAvg();
        return avg != null ? this.processValue(avg) : null;
    }

    @Override
    public Date getDate() {
        return this.sample.getDate();
    }

    @Override
    public Double getCount() {
        return this.sample.getCount();
    }
}
