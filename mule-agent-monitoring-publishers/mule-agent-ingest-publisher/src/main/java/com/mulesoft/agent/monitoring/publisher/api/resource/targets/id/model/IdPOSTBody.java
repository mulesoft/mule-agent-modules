
package com.mulesoft.agent.monitoring.publisher.api.resource.targets.id.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "cpu-usage",
    "memory-usage",
    "memory-total"
})
public class IdPOSTBody
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("cpu-usage")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> cpuUsage = new LinkedHashSet<IngestMetric>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("memory-usage")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> memoryUsage = new LinkedHashSet<IngestMetric>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("memory-total")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> memoryTotal = new LinkedHashSet<IngestMetric>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdPOSTBody()
    {
    }

    /**
     * 
     * @param cpuUsage
     * @param memoryUsage
     * @param memoryTotal
     * @param id
     */
    public IdPOSTBody(Set<IngestMetric> cpuUsage, Set<IngestMetric> memoryUsage, Set<IngestMetric> memoryTotal)
    {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.memoryTotal = memoryTotal;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The cpuUsage
     */
    @JsonProperty("cpu-usage")
    public Set<IngestMetric> getCpuUsage()
    {
        return cpuUsage;
    }

    /**
     * 
     * (Required)
     * 
     * @param cpuUsage
     *     The cpu-usage
     */
    @JsonProperty("cpu-usage")
    public void setCpuUsage(Set<IngestMetric> cpuUsage)
    {
        this.cpuUsage = cpuUsage;
    }

    public IdPOSTBody withCpuUsage(Set<IngestMetric> cpuUsage)
    {
        this.cpuUsage = cpuUsage;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The memoryUsage
     */
    @JsonProperty("memory-usage")
    public Set<IngestMetric> getMemoryUsage()
    {
        return memoryUsage;
    }

    /**
     * 
     * (Required)
     * 
     * @param memoryUsage
     *     The memory-usage
     */
    @JsonProperty("memory-usage")
    public void setMemoryUsage(Set<IngestMetric> memoryUsage)
    {
        this.memoryUsage = memoryUsage;
    }

    public IdPOSTBody withMemoryUsage(Set<IngestMetric> memoryUsage)
    {
        this.memoryUsage = memoryUsage;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The memoryTotal
     */
    @JsonProperty("memory-total")
    public Set<IngestMetric> getMemoryTotal()
    {
        return memoryTotal;
    }

    /**
     * 
     * (Required)
     * 
     * @param memoryTotal
     *     The memory-total
     */
    @JsonProperty("memory-total")
    public void setMemoryTotal(Set<IngestMetric> memoryTotal)
    {
        this.memoryTotal = memoryTotal;
    }

    public IdPOSTBody withMemoryTotal(Set<IngestMetric> memoryTotal)
    {
        this.memoryTotal = memoryTotal;
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

    public IdPOSTBody withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(cpuUsage).append(memoryUsage).append(memoryTotal).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof IdPOSTBody) == false)
        {
            return false;
        }
        IdPOSTBody rhs = ((IdPOSTBody) other);
        return new EqualsBuilder().append(cpuUsage, rhs.cpuUsage).append(memoryUsage, rhs.memoryUsage).append(memoryTotal, rhs.memoryTotal).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
