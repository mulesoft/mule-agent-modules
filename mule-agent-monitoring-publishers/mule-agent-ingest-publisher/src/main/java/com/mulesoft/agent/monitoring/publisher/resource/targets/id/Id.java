
package com.mulesoft.agent.monitoring.publisher.resource.targets.id;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import com.mulesoft.agent.monitoring.publisher.resource.targets.id.model.IdPOSTBody;

public class Id {

    private String _baseUrl;
    private Client client;

    public Id(String baseUrl, Client client, String uriParam) {
        _baseUrl = (baseUrl +("/"+ uriParam));
        this.client = client;
    }

    private Client getClient() {
        return this.client;
    }

    private String getBaseUri() {
        return _baseUrl;
    }

    /**
     * Creates aggregated metric measurements for multiple targets.
     * 
     * 
     */
    public void post(IdPOSTBody body) {
        WebTarget target = this.client.target(getBaseUri());
        final javax.ws.rs.client.Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = invocationBuilder.post(Entity.json(body));
        if (response.getStatusInfo().getFamily()!= Family.SUCCESSFUL) {
            Response.StatusType statusInfo = response.getStatusInfo();
            throw new RuntimeException(((((("("+ statusInfo.getFamily())+") ")+ statusInfo.getStatusCode())+" ")+ statusInfo.getReasonPhrase()));
        }
    }

}
