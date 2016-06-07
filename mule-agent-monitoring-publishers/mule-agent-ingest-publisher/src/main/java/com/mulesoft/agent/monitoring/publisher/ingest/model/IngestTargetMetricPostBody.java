
package com.mulesoft.agent.monitoring.publisher.ingest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents the body of the post to publish target metrics to the monitoring ingest API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "cpu-usage",
    "memory-usage",
    "memory-total"
})
public class IngestTargetMetricPostBody
{

    @JsonProperty("cpu-usage")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> cpuUsage = new LinkedHashSet<IngestMetric>();

    @JsonProperty("memory-usage")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> memoryUsage = new LinkedHashSet<IngestMetric>();

    @JsonProperty("memory-total")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> memoryTotal = new LinkedHashSet<IngestMetric>();

    /**
     * No args constructor for use in serialization
     *
     */
    public IngestTargetMetricPostBody()
    {
    }

    /**
     *
     * @param cpuUsage Samples for CPU Usage
     * @param memoryUsage Samples for Memory Usage
     * @param memoryTotal Samples for Memory Total
     */
    public IngestTargetMetricPostBody(Set<IngestMetric> cpuUsage, Set<IngestMetric> memoryUsage, Set<IngestMetric> memoryTotal)
    {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.memoryTotal = memoryTotal;
    }

    @JsonProperty("cpu-usage")
    public Set<IngestMetric> getCpuUsage()
    {
        return cpuUsage;
    }

    @JsonProperty("cpu-usage")
    public void setCpuUsage(Set<IngestMetric> cpuUsage)
    {
        this.cpuUsage = cpuUsage;
    }

    @JsonProperty("memory-usage")
    public Set<IngestMetric> getMemoryUsage()
    {
        return memoryUsage;
    }

    @JsonProperty("memory-usage")
    public void setMemoryUsage(Set<IngestMetric> memoryUsage)
    {
        this.memoryUsage = memoryUsage;
    }

    @JsonProperty("memory-total")
    public Set<IngestMetric> getMemoryTotal()
    {
        return memoryTotal;
    }

    @JsonProperty("memory-total")
    public void setMemoryTotal(Set<IngestMetric> memoryTotal)
    {
        this.memoryTotal = memoryTotal;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(cpuUsage).append(memoryUsage).append(memoryTotal).toHashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof IngestTargetMetricPostBody) == false)
        {
            return false;
        }
        IngestTargetMetricPostBody rhs = ((IngestTargetMetricPostBody) other);
        return new EqualsBuilder().append(cpuUsage, rhs.cpuUsage).append(memoryUsage, rhs.memoryUsage).append(memoryTotal, rhs.memoryTotal).isEquals();
    }

}
