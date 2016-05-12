
package com.mulesoft.agent.monitoring.publisher.api.resource.targets;

import javax.ws.rs.client.Client;
import com.mulesoft.agent.monitoring.publisher.api.resource.targets.id.Id;

public class Targets
{

    private String _baseUrl;
    private Client client;

    public Targets(String baseUrl, Client client)
    {
        _baseUrl = (baseUrl +"/targets");
        this.client = client;
    }

    private Client getClient()
    {
        return this.client;
    }

    private String getBaseUri()
    {
        return _baseUrl;
    }

    public final Id id(String id)
    {
        return new Id(getBaseUri(), getClient(), id);
    }

}
