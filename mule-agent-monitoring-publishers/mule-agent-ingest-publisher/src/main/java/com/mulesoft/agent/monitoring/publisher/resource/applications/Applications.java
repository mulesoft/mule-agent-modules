
package com.mulesoft.agent.monitoring.publisher.resource.applications;

import javax.ws.rs.client.Client;
import com.mulesoft.agent.monitoring.publisher.resource.applications.id.Id;

public class Applications {

    private String _baseUrl;
    private Client client;

    public Applications(String baseUrl, Client client) {
        _baseUrl = (baseUrl +"/applications");
        this.client = client;
    }

    private Client getClient() {
        return this.client;
    }

    private String getBaseUri() {
        return _baseUrl;
    }

    public final Id id(String id) {
        return new Id(getBaseUri(), getClient(), id);
    }

}
