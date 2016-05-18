package com.mulesoft.agent.monitoring.publisher.model;

import com.mulesoft.agent.domain.monitoring.Metric;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *      This class represents a sample in one moment of a group of metrics.
 * </p>
 */
public class MetricSample
{

    private final Date date;
    private final Double min;
    private final Double max;
    private final Double sum;
    private final Double avg;
    private final Double count;

    public MetricSample(Date date, List<Metric> sample)
    {
        this.date = date;
        Double min = null;
        Double max = null;
        Double sum = 0d;
        Double count = 0d;

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
            }
        }
        this.max = max;
        this.min = min;
        this.sum = sum;
        this.avg = count > 0 ? sum / count : 0d;
        this.count = count;
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

}
