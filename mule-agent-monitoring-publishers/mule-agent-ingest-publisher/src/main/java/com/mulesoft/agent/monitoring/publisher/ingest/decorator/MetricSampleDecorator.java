package com.mulesoft.agent.monitoring.publisher.ingest.decorator;

import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;

import java.util.Date;

/**
 * Basic metric sample decorator meant to be extended to convert the values of a MetricSample from one unit to another.
 */
public abstract class MetricSampleDecorator implements MetricSample
{

    private MetricSample sample;

    public MetricSampleDecorator(MetricSample sample)
    {
        this.sample = sample;
    }

    /**
     * Custom function that will convert the given value to the wanted unit.
     *
     * @param value Value to be converted.
     * @return Converted value.
     */
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

    @Override
    public String toString()
    {
        return "MetricSampleDecorator{" +
                "sample=" + sample +
                '}';
    }

}
