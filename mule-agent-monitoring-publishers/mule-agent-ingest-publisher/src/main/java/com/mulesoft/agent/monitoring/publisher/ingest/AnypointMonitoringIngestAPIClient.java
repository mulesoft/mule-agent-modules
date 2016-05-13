
package com.mulesoft.agent.monitoring.publisher.ingest;

import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetricPostBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    public void postMetrics(final String id, final IngestMetricPostBody body) {
        final String url = this.baseUrl + "/targets/" + id;
        LOGGER.info(String.format("Sending %s to %s...", body.toString(), url));
        final Response response = this.client
                .target(url)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(body));
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL)
        {
            Response.StatusType statusInfo = response.getStatusInfo();
            LOGGER.error(String.format("Failed to send %s to %s. Response status: %s", body.toString(), url, statusInfo.getReasonPhrase()));
            throw new RuntimeException(((((("("+ statusInfo.getFamily())+") ")+ statusInfo.getStatusCode())+" ")+ statusInfo.getReasonPhrase()));
        }
        LOGGER.info(String.format("Successfully sent %s to %s!", body.toString(), url));
    }

}
