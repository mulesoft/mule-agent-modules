package com.mulesoft.agent.eventtracker.cloudhub.tracking;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.eventtracker.cloudhub.InsightClient;
import com.mulesoft.agent.eventtracker.cloudhub.InsightEvent;
import com.mulesoft.agent.eventtracker.cloudhub.InsightException;
import com.mulesoft.agent.eventtracker.cloudhub.InsightSendingException;
import com.mulesoft.agent.eventtracker.cloudhub.InsightSerializationException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

/**
 * A client that puts {@link InsightEvent} to Analytics Insight via Cloudhub Platform.
 */
@Named("client.cloudhub.insight")
@Singleton
public class CloudhubInsightClient implements InsightClient
{

    private static final Logger LOGGER = LogManager.getLogger(CloudhubInsightClient.class);

    private static final String INSIGHTS_PATH_FORMAT = "%s/v2/tracking/organizations/%s/environments/%s/applications/%s/workers/%s/events";

    private static final String APP_ID_PROPERTY = "application.id";
    private static final String APP_TOKEN_PROPERTY = "ion.api.token";
    private static final String CS_ORG_ID_PROPERTY = "csorganization.id";
    private static final String ENV_ID_PROPERTY = "environment.id";
    private static final String SERVER_ID_PROPERTY = "server.id";
    private static final String PLATFORM_HOST_PROPERTY = "platform.services.endpoint";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Configurable("5000")
    private int connectionTimeoutMilli;
    @Configurable("60000")
    private int requestTimeoutMilli;

    private AsyncHttpClient httpClient;

    private String appId;
    private String appToken;
    private String insightPath;

    CloudhubInsightClient()
    {
    }

    CloudhubInsightClient(int connectionTimeoutMilli, int requestTimeoutMilli)
    {
        this.connectionTimeoutMilli = connectionTimeoutMilli;
        this.requestTimeoutMilli = requestTimeoutMilli;
    }

    @PostConfigure
    public void init() throws InitializationException
    {
        appId = System.getProperty(APP_ID_PROPERTY);
        appToken = System.getProperty(APP_TOKEN_PROPERTY);
        String orgId = System.getProperty(CS_ORG_ID_PROPERTY);
        String envId = System.getProperty(ENV_ID_PROPERTY);
        String workerId = System.getProperty(SERVER_ID_PROPERTY);
        String platformHost = System.getProperty(PLATFORM_HOST_PROPERTY);
        if (appId == null || appToken == null || orgId == null || envId == null || workerId == null
                || platformHost == null)
        {
            throw new InitializationException("Missing application meta info for insights!");
        }
        insightPath = String.format(INSIGHTS_PATH_FORMAT, platformHost, orgId, envId, appId, workerId);

        Builder builder = new Builder().setCompressionEnforced(true)
                .setConnectTimeout(connectionTimeoutMilli)
                .setRequestTimeout(requestTimeoutMilli);
        httpClient = new AsyncHttpClient(builder.build());
        LOGGER.info("Initialized Cloudhub Insight Client");
    }

    @Override
    public void sendInsight(Collection<InsightEvent> events) throws InsightException
    {
        try
        {
            Request request = new RequestBuilder("PUT").setUrl(insightPath)
                    .addHeader("Content-Type", "application/json; charset=UTF-8")
                    .addHeader("X-ION-Authenticate", appToken)
                    .addHeader("X-ION-Application", appId)
                    .setBody(MAPPER.writeValueAsString(events))
                    .setBodyEncoding("UTF-8")
                    .build();
            LOGGER.trace("Sending insight events to cloudhub platform");
            Response response = httpClient.executeRequest(request).get();
            if (response.getStatusCode() != HttpURLConnection.HTTP_OK)
            {
                throw new InsightSendingException(String.format(
                        "Error sending tracking events to platform, status code: %s", response.getStatusCode()));
            }
            LOGGER.trace("Sent insight events to cloudhub platform");
        }
        catch (JsonProcessingException e)
        {
            throw new InsightSerializationException("Could not convert insights events to json.", e);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new InsightSendingException("Could not send request to Cloudhub Platform service.", e);
        }
    }

    AsyncHttpClient getHttpClient()
    {
        return httpClient;
    }

    void setHttpClient(AsyncHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }
}
