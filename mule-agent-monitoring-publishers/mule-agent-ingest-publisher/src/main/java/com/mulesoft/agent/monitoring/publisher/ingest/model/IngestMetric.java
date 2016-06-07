package com.mulesoft.agent.monitoring.publisher.ingest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mulesoft.agent.monitoring.publisher.model.MetricSample;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "time",
        "min",
        "max",
        "sum",
        "avg",
        "count"
})
public class IngestMetric
{

    @JsonProperty("time")
    private Date time;

    @JsonProperty("min")
    private Double min;

    @JsonProperty("max")
    private Double max;

    @JsonProperty("sum")
    private Double sum;

    @JsonProperty("avg")
    private Double avg;

    @JsonProperty("count")
    private Double count;

    /**
     * No args constructor for use in serialization
     *
     */
    public IngestMetric()
    {
    }

    /**
     *
     * @param min Minimum value
     * @param avg Average value
     * @param max Maximum value
     * @param count Quantity of samples
     * @param sum Sum of all samples
     * @param time Sample's date
     */
    public IngestMetric(Date time, Double min, Double max, Double sum, Double avg, Double count)
    {
        this.time = time;
        this.min = min;
        this.max = max;
        this.sum = sum;
        this.avg = avg;
        this.count = count;
    }

    @JsonProperty("time")
    public Date getTime()
    {
        return time;
    }

    @JsonProperty("time")
    public void setTime(Date time)
    {
        this.time = time;
    }

    public IngestMetric withTime(Date time)
    {
        this.time = time;
        return this;
    }

    @JsonProperty("min")
    public Double getMin()
    {
        return min;
    }

    @JsonProperty("min")
    public void setMin(Double min)
    {
        this.min = min;
    }

    public IngestMetric withMin(Double min)
    {
        this.min = min;
        return this;
    }

    @JsonProperty("max")
    public Double getMax()
    {
        return max;
    }

    @JsonProperty("max")
    public void setMax(Double max)
    {
        this.max = max;
    }

    public IngestMetric withMax(Double max)
    {
        this.max = max;
        return this;
    }

    @JsonProperty("sum")
    public Double getSum()
    {
        return sum;
    }

    @JsonProperty("sum")
    public void setSum(Double sum)
    {
        this.sum = sum;
    }

    public IngestMetric withSum(Double sum)
    {
        this.sum = sum;
        return this;
    }

    @JsonProperty("avg")
    public Double getAvg()
    {
        return avg;
    }

    @JsonProperty("avg")
    public void setAvg(Double avg)
    {
        this.avg = avg;
    }

    public IngestMetric withAvg(Double avg)
    {
        this.avg = avg;
        return this;
    }

    @JsonProperty("count")
    public Double getCount()
    {
        return count;
    }

    @JsonProperty("count")
    public void setCount(Double count)
    {
        this.count = count;
    }

    public IngestMetric withCount(Double count)
    {
        this.count = count;
        return this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(time).append(min).append(max).append(sum).append(avg).append(count).toHashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof IngestMetric) == false)
        {
            return false;
        }
        IngestMetric rhs = ((IngestMetric) other);
        return new EqualsBuilder().append(time, rhs.time).append(min, rhs.min).append(max, rhs.max).append(sum, rhs.sum).append(avg, rhs.avg).append(count, rhs.count).isEquals();
    }

}
