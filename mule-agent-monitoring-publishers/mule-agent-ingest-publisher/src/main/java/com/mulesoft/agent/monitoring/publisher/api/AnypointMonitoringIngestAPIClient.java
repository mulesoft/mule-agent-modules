
package com.mulesoft.agent.monitoring.publisher.api;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import com.mulesoft.agent.monitoring.publisher.resource.applications.Applications;
import com.mulesoft.agent.monitoring.publisher.resource.targets.Targets;

public class AnypointMonitoringIngestAPIClient {

    private String _baseUrl;
    public final Applications applications;
    public final Targets targets;

    public AnypointMonitoringIngestAPIClient(String baseUrl) {
        _baseUrl = baseUrl;
        applications = new Applications(getBaseUri(), getClient());
        targets = new Targets(getBaseUri(), getClient());
    }

    public AnypointMonitoringIngestAPIClient(String endpoint, String apiVersion, String organizationId, String environmentId) {
        this(String.format("%s/monitoring/api/v%s/organizations/%s/environments/%s", endpoint, apiVersion, organizationId, environmentId));
    }

    private Client getClient() {
        return ClientBuilder.newClient();
    }

    protected String getBaseUri() {
        return _baseUrl;
    }

    public static AnypointMonitoringIngestAPIClient create(String baseUrl) {
        return new AnypointMonitoringIngestAPIClient(baseUrl);
    }

    public static AnypointMonitoringIngestAPIClient create(String endpoint, String apiVersion, String organizationId, String environmentId) {
        return new AnypointMonitoringIngestAPIClient(endpoint, apiVersion, organizationId, environmentId);
    }

}
