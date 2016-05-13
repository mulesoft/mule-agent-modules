
package com.mulesoft.agent.monitoring.publisher.ingest.model;

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
        "message-count",
        "response-time",
        "error-count"
})
public class IngestApplicationMetricPostBody
{

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("message-count")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> messageCount = new LinkedHashSet<IngestMetric>();
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("response-time")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> responseTime = new LinkedHashSet<IngestMetric>();
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("error-count")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> errorCount = new LinkedHashSet<IngestMetric>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public IngestApplicationMetricPostBody()
    {
    }

    /**
     *
     * @param messageCount
     * @param responseTime
     * @param errorCount
     * @param id
     */
    public IngestApplicationMetricPostBody(Set<IngestMetric> messageCount, Set<IngestMetric> responseTime, Set<IngestMetric> errorCount)
    {
        this.messageCount = messageCount;
        this.responseTime = responseTime;
        this.errorCount = errorCount;
    }

    /**
     *
     * (Required)
     *
     * @return
     *     The messageCount
     */
    @JsonProperty("message-count")
    public Set<IngestMetric> getMessageCount()
    {
        return messageCount;
    }

    /**
     *
     * (Required)
     *
     * @param messageCount
     *     The message-count
     */
    @JsonProperty("message-count")
    public void setMessageCount(Set<IngestMetric> messageCount)
    {
        this.messageCount = messageCount;
    }

    /**
     *
     * (Required)
     *
     * @return
     *     The responseTime
     */
    @JsonProperty("response-time")
    public Set<IngestMetric> getResponseTime()
    {
        return responseTime;
    }

    /**
     *
     * (Required)
     *
     * @param responseTime
     *     The response-time
     */
    @JsonProperty("response-time")
    public void setResponseTime(Set<IngestMetric> responseTime)
    {
        this.responseTime = responseTime;
    }

    /**
     *
     * (Required)
     *
     * @return
     *     The errorCount
     */
    @JsonProperty("error-count")
    public Set<IngestMetric> getErrorCount()
    {
        return errorCount;
    }

    /**
     *
     * (Required)
     *
     * @param errorCount
     *     The error-count
     */
    @JsonProperty("error-count")
    public void setErrorCount(Set<IngestMetric> errorCount)
    {
        this.errorCount = errorCount;
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

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(messageCount).append(responseTime).append(errorCount).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof IngestApplicationMetricPostBody) == false)
        {
            return false;
        }
        IngestApplicationMetricPostBody rhs = ((IngestApplicationMetricPostBody) other);
        return new EqualsBuilder().append(messageCount, rhs.messageCount).append(responseTime, rhs.responseTime).append(errorCount, rhs.errorCount).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
