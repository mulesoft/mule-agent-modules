/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.model.api;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents the metrics of a Flow.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "message-count",
        "response-time",
        "error-count"
})
public class IngestFlowMetrics
{

    @JsonProperty("message-count")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> messageCount = new LinkedHashSet<IngestMetric>();

    @JsonProperty("response-time")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> responseTime = new LinkedHashSet<IngestMetric>();

    @JsonProperty("error-count")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<IngestMetric> errorCount = new LinkedHashSet<IngestMetric>();

    /**
     * No args constructor for use in serialization.
     */
    public IngestFlowMetrics()
    {
    }

    /**
     *
     * @param messageCount Samples for message count.
     * @param responseTime Samples for response time.
     * @param errorCount Samples for error count.
     */
    public IngestFlowMetrics(Set<IngestMetric> messageCount, Set<IngestMetric> responseTime, Set<IngestMetric> errorCount)
    {
        this.messageCount = messageCount;
        this.responseTime = responseTime;
        this.errorCount = errorCount;
    }

    @JsonProperty("message-count")
    public Set<IngestMetric> getMessageCount()
    {
        return messageCount;
    }

    @JsonProperty("message-count")
    public void setMessageCount(Set<IngestMetric> messageCount)
    {
        this.messageCount = messageCount;
    }

    @JsonProperty("response-time")
    public Set<IngestMetric> getResponseTime()
    {
        return responseTime;
    }

    @JsonProperty("response-time")
    public void setResponseTime(Set<IngestMetric> responseTime)
    {
        this.responseTime = responseTime;
    }

    @JsonProperty("error-count")
    public Set<IngestMetric> getErrorCount()
    {
        return errorCount;
    }

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

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(messageCount).append(responseTime).append(errorCount).toHashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if (!(other instanceof IngestFlowMetrics))
        {
            return false;
        }
        IngestFlowMetrics rhs = ((IngestFlowMetrics) other);
        return new EqualsBuilder().append(messageCount, rhs.messageCount).append(responseTime, rhs.responseTime).append(errorCount, rhs.errorCount).isEquals();
    }

}
