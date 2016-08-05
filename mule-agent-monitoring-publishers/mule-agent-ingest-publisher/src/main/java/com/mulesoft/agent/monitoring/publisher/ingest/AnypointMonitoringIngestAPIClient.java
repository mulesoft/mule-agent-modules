
package com.mulesoft.agent.monitoring.publisher.ingest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.agent.clients.AuthenticationProxyClient;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;

/**
 * Monitoring Ingest API Client
 */
public class AnypointMonitoringIngestAPIClient
{

    private static final Logger LOGGER = LoggerFactory.getLogger(AnypointMonitoringIngestAPIClient.class);
    private static final String APPLICATION_NAME_HEADER = "X-APPLICATION-NAME";

    private final String targetMetricsPath;
    private final String applicationMetricsPath;

    private final AuthenticationProxyClient authProxyClient;

    private AnypointMonitoringIngestAPIClient(String apiVersion, AuthenticationProxyClient authProxyClient)
    {
        this.targetMetricsPath = String.format("/monitoring/ingest/api/v%s/targets", apiVersion);
        this.applicationMetricsPath  = String.format("/monitoring/ingest/api/v%s/applications", apiVersion);
        this.authProxyClient = authProxyClient;
    }

    public static AnypointMonitoringIngestAPIClient create(String apiVersion, AuthenticationProxyClient authProxyClient)
    {
        return new AnypointMonitoringIngestAPIClient(apiVersion, authProxyClient);
    }

    public int postTargetMetrics(final IngestTargetMetricPostBody body)
    {
        try
        {
            return this.authProxyClient.post(this.targetMetricsPath, body).getStatusCode();
        }
        catch (Exception e)
        {
            LOGGER.warn("Could not publish target, cause: " + e.getMessage());
            LOGGER.debug("ERROR: ", e);
            return 500;
        }
    }

    public int postApplicationMetrics(final String applicationName, final IngestApplicationMetricPostBody body)
    {
        HashMap<String, Collection<String>> headers = Maps.newHashMap();
        headers.put(APPLICATION_NAME_HEADER, Lists.newArrayList(applicationName));
        try
        {
            return this.authProxyClient.post(this.applicationMetricsPath, body, headers).getStatusCode();
        }
        catch (Exception e)
        {
            LOGGER.warn("Could not publish metrics for application " + applicationName + ", cause: " + e.getMessage());
            LOGGER.debug("ERROR: ", e);
            return 500;
        }
    }

}
