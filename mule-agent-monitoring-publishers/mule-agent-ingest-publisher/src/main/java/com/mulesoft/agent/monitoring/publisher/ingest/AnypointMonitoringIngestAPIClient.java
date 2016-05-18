
package com.mulesoft.agent.monitoring.publisher.ingest;

import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Monitoring Ingest API Client
 */
public class AnypointMonitoringIngestAPIClient
{

    private static final Logger LOGGER = LoggerFactory.getLogger(AnypointMonitoringIngestAPIClient.class);

    private final String baseUrl;
    private final Client client;

    private AnypointMonitoringIngestAPIClient(String baseUrl)
    {
        this.baseUrl = baseUrl;
        this.client = ClientBuilder.newClient();
    }

    private AnypointMonitoringIngestAPIClient(String endpoint, String apiVersion, String organizationId, String environmentId)
    {
        this(String.format("%s/monitoring/ingest/api/v%s/organizations/%s/environments/%s", endpoint, apiVersion, organizationId, environmentId));
    }

    public static AnypointMonitoringIngestAPIClient create(String endpoint, String apiVersion, String organizationId, String environmentId)
    {
        return new AnypointMonitoringIngestAPIClient(endpoint, apiVersion, organizationId, environmentId);
    }

    private <T> void doPost(final String url, final Entity<T> json) {
        final Response response = this.client
                .target(url)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(json);
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL)
        {
            Response.StatusType statusInfo = response.getStatusInfo();
            throw new RuntimeException("(" + statusInfo.getFamily()+ ") " + statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase());
        }
    }

    public void postTargetMetrics(final String id, final IngestTargetMetricPostBody body) {
        final String url = this.baseUrl + "/targets/" + id;
        LOGGER.info(String.format("Sending %s to %s...", body.toString(), url));
        Entity<IngestTargetMetricPostBody> json = Entity.json(body);
        this.doPost(url, json);
    }

    public void postApplicationMetrics(final String id, final IngestApplicationMetricPostBody body) {
        final String url = this.baseUrl + "/applications/" + id;
        Entity<IngestApplicationMetricPostBody> json = Entity.json(body);
        LOGGER.info(String.format("Sending %s to %s...", body.toString(), url));
        this.doPost(url, json);
    }

}
