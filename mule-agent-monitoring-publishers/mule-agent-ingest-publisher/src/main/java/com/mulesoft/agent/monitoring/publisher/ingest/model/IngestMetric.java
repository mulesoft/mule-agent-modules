package com.mulesoft.agent.monitoring.publisher.ingest.model;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("time")
    private Date time;
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("min")
    private Double min;
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("max")
    private Double max;
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("sum")
    private Double sum;
    @JsonProperty("avg")
    private Double avg;
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("count")
    private Double count;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public IngestMetric()
    {
    }

    /**
     *
     * @param min
     * @param avg
     * @param max
     * @param count
     * @param sum
     * @param time
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

    /**
     *
     * (Required)
     *
     * @return
     *     The time
     */
    @JsonProperty("time")
    public Date getTime()
    {
        return time;
    }

    /**
     *
     * (Required)
     *
     * @param time
     *     The time
     */
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

    /**
     *
     * (Required)
     *
     * @return
     *     The min
     */
    @JsonProperty("min")
    public Double getMin()
    {
        return min;
    }

    /**
     *
     * (Required)
     *
     * @param min
     *     The min
     */
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

    /**
     *
     * (Required)
     *
     * @return
     *     The max
     */
    @JsonProperty("max")
    public Double getMax()
    {
        return max;
    }

    /**
     *
     * (Required)
     *
     * @param max
     *     The max
     */
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

    /**
     *
     * (Required)
     *
     * @return
     *     The sum
     */
    @JsonProperty("sum")
    public Double getSum()
    {
        return sum;
    }

    /**
     *
     * (Required)
     *
     * @param sum
     *     The sum
     */
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

    /**
     *
     * @return
     *     The avg
     */
    @JsonProperty("avg")
    public Double getAvg()
    {
        return avg;
    }

    /**
     *
     * @param avg
     *     The avg
     */
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

    /**
     *
     * (Required)
     *
     * @return
     *     The count
     */
    @JsonProperty("count")
    public Double getCount()
    {
        return count;
    }

    /**
     *
     * (Required)
     *
     * @param count
     *     The count
     */
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

    public IngestMetric withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(time).append(min).append(max).append(sum).append(avg).append(count).append(additionalProperties).toHashCode();
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
        return new EqualsBuilder().append(time, rhs.time).append(min, rhs.min).append(max, rhs.max).append(sum, rhs.sum).append(avg, rhs.avg).append(count, rhs.count).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
