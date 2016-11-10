package com.mulesoft.agent.monitoring.publisher.ingest.model;

import com.mulesoft.agent.domain.monitoring.Metric;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *      This class represents a sample in one moment of a group of metrics.
 * </p>
 */
public class DefaultMetricSample implements MetricSample
{

    private final Date date;
    private final Double min;
    private final Double max;
    private final Double sum;
    private final Double avg;
    private final Double count;

    public DefaultMetricSample(Date date, Double min, Double max, Double sum, Double avg, Double count) {
        this.date = date;
        this.min = min;
        this.max = max;
        this.sum = sum;
        this.avg = avg;
        this.count = count;
    }

    public DefaultMetricSample(List<Metric> sample)
    {
        Double min = null;
        Double max = null;
        Double sum = 0d;
        Double count = 0d;
        Date date = null;

        if (sample != null)
        {
            for (Metric metric : sample)
            {
                if (metric == null || metric.getValue() == null)
                {
                    continue;
                }
                double value = metric.getValue().doubleValue();
                if (max == null || max < value)
                {
                    max = value;
                }
                if (min == null || min > value)
                {
                    min = value;
                }
                sum += value;
                count += 1;
                if (date == null || date.getTime() < metric.getTimestamp())
                {
                    date = new Date(metric.getTimestamp());
                }
            }
        }
        this.max = max;
        this.min = min;
        this.sum = sum;
        this.avg = count > 0 ? sum / count : 0d;
        this.count = count;
        this.date = date != null ? date : new Date();
    }

    public Date getDate()
    {
        return this.date;
    }

    public Double getMin()
    {
        return min;
    }

    public Double getMax()
    {
        return max;
    }

    public Double getSum()
    {
        return sum;
    }

    public Double getAvg()
    {
        return avg;
    }

    public Double getCount()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return "DefaultMetricSample{" +
                "date=" + date +
                ", min=" + min +
                ", max=" + max +
                ", sum=" + sum +
                ", avg=" + avg +
                ", count=" + count +
                '}';
    }
}
