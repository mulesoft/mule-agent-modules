
package com.mulesoft.agent.monitoring.publisher.resource.applications.id.model;

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
public class IdPOSTBody {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message-count")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<MessageCount> messageCount = new LinkedHashSet<MessageCount>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("response-time")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseTime> responseTime = new LinkedHashSet<ResponseTime>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("error-count")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ErrorCount> errorCount = new LinkedHashSet<ErrorCount>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdPOSTBody() {
    }

    /**
     * 
     * @param messageCount
     * @param responseTime
     * @param errorCount
     */
    public IdPOSTBody(Set<MessageCount> messageCount, Set<ResponseTime> responseTime, Set<ErrorCount> errorCount) {
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
    public Set<MessageCount> getMessageCount() {
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
    public void setMessageCount(Set<MessageCount> messageCount) {
        this.messageCount = messageCount;
    }

    public IdPOSTBody withMessageCount(Set<MessageCount> messageCount) {
        this.messageCount = messageCount;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The responseTime
     */
    @JsonProperty("response-time")
    public Set<ResponseTime> getResponseTime() {
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
    public void setResponseTime(Set<ResponseTime> responseTime) {
        this.responseTime = responseTime;
    }

    public IdPOSTBody withResponseTime(Set<ResponseTime> responseTime) {
        this.responseTime = responseTime;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     * @return
     *     The errorCount
     */
    @JsonProperty("error-count")
    public Set<ErrorCount> getErrorCount() {
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
    public void setErrorCount(Set<ErrorCount> errorCount) {
        this.errorCount = errorCount;
    }

    public IdPOSTBody withErrorCount(Set<ErrorCount> errorCount) {
        this.errorCount = errorCount;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public IdPOSTBody withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(messageCount).append(responseTime).append(errorCount).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdPOSTBody) == false) {
            return false;
        }
        IdPOSTBody rhs = ((IdPOSTBody) other);
        return new EqualsBuilder().append(messageCount, rhs.messageCount).append(responseTime, rhs.responseTime).append(errorCount, rhs.errorCount).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
