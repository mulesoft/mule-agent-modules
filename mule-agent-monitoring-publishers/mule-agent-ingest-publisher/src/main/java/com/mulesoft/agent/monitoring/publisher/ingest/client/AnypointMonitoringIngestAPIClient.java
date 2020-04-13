/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */


package com.mulesoft.agent.monitoring.publisher.ingest.client;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.agent.clients.AuthenticationProxyClient;
import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestMetric;
import org.asynchttpclient.Response;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Monitoring Ingest API Client.
 */
public final class AnypointMonitoringIngestAPIClient
{

    private static final Logger LOGGER = LogManager.getLogger(AnypointMonitoringIngestAPIClient.class);

    /**
     * There is a header on the request used by the authentication proxy to know which application is making the request.
     * That header's name is held in this constant.
     */
    private static final String APPLICATION_NAME_HEADER = "X-APPLICATION-NAME";

    /**
     * Request path to post target metrics.
     */
    private final String targetMetricsPath;

    /**
     * Request path to post application metrics.
     */
    private final String applicationMetricsPath;

    /**
     * Authentication proxy client.
     */
    private final AuthenticationProxyClient authProxyClient;

    /**
     * Create a monitoring client for the given API version and the Authentication Proxy client.
     *
     * @param apiVersion Monitoring API version.
     * @param authProxyClient Authentication proxy client.
     */
    private AnypointMonitoringIngestAPIClient(String apiVersion, AuthenticationProxyClient authProxyClient)
    {
        this.targetMetricsPath = String.format("/monitoring/ingest/api/v%s/targets", apiVersion);
        this.applicationMetricsPath  = String.format("/monitoring/ingest/api/v%s/applications", apiVersion);
        this.authProxyClient = authProxyClient;
    }

    /**
     * Static method for creating monitoring api clients.
     *
     * @param apiVersion Monitoring API version.
     * @param authProxyClient Authentication proxy client.
     * @return A new monitoring ingest API client.
     */
    public static AnypointMonitoringIngestAPIClient create(String apiVersion, AuthenticationProxyClient authProxyClient)
    {
        return new AnypointMonitoringIngestAPIClient(apiVersion, authProxyClient);
    }

    /**
     * Publish target metrics to Ingest API.
     *
     * @param body Request body.
     * @return http response.
     */
    public Response postTargetMetrics(final Map<String, Set<IngestMetric>> body)
    {
        Response httpResponse = this.authProxyClient.post(this.targetMetricsPath, body);

        if (javax.ws.rs.core.Response.Status.Family.familyOf(httpResponse.getStatusCode()) != javax.ws.rs.core.Response.Status.Family.SUCCESSFUL)
        {
            LOGGER.warn("Post of target metrics failed with status " + httpResponse.getStatusCode());
            if (LOGGER.isDebugEnabled())
            {
                try
                {
                    LOGGER.debug("Post of target metrics failed with status " + httpResponse.getStatusCode() + ", response body: " + httpResponse.getResponseBody(Charset.forName("UTF-8")));
                }
                catch (Throwable e)
                {
                    LOGGER.warn(String.format("Could not read response body. cause: %s - %s", e.getClass().getSimpleName(), ExceptionUtils.getRootCauseMessage(e)));
                    LOGGER.debug("Could not read response body from post target metrics response.", e);
                }
            }
        }

        return httpResponse;
    }

    /**
     * Publish application metrics to Ingest API.
     *
     * @param applicationName Name of the application.
     * @param body Request body.
     * @return http response.
     */
    public Response postApplicationMetrics(final String applicationName, final IngestApplicationMetricPostBody body)
    {
        HashMap<CharSequence, Collection<String>> headers = Maps.newHashMap();
        headers.put(APPLICATION_NAME_HEADER, Lists.newArrayList(applicationName));
        Response httpResponse = this.authProxyClient.post(this.applicationMetricsPath, body, headers);

        if (javax.ws.rs.core.Response.Status.Family.familyOf(httpResponse.getStatusCode()) != javax.ws.rs.core.Response.Status.Family.SUCCESSFUL)
        {
            LOGGER.warn(String.format("Post of application metrics for %s failed with status %d", applicationName, httpResponse.getStatusCode()));
            if (LOGGER.isDebugEnabled())
            {
                try
                {
                    LOGGER.debug(String.format("Post of application metrics for %s failed with status %d, response body: %s", applicationName, httpResponse.getStatusCode(), httpResponse.getResponseBody(Charset.forName("UTF-8"))));
                }
                catch (Throwable e)
                {
                    LOGGER.warn(String.format("Could not read response body. cause: %s - %s", e.getClass().getSimpleName(), ExceptionUtils.getRootCauseMessage(e)));
                    LOGGER.debug(String.format("Could not read response body from post application metrics for %s response.", applicationName), e);
                }
            }
        }

        return httpResponse;
    }

}
