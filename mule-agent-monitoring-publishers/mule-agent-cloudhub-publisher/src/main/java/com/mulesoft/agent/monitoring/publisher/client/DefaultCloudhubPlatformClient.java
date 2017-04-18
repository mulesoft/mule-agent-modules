package com.mulesoft.agent.monitoring.publisher.client;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.monitoring.publisher.factory.MemorySnapshotFactory.MemorySnapshot;
import com.mulesoft.agent.monitoring.publisher.factory.MuleMessageSnapshotFactory.MuleMessageSnapshot;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default HTTP client implementation for Cloudhub Platform services.
 */
@Named("client.cloudhub")
@Singleton
public class DefaultCloudhubPlatformClient implements CloudhubPlatformClient
{

    private static final String STATS_MEMORY_PATH = "%s/agentstats/memory/%s";
    private static final String STATS_MESSAGES_PATH = "%s/agentstats/messages/%s";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LogManager.getLogger(DefaultCloudhubPlatformClient.class);

    // these properties are set by CloudhubPropertiesCoreExtension when mule starts on a worker
    private static final String PLATFORM_HOST = System.getProperty("platform.services.endpoint");
    private static final String CH_API_TOKEN = System.getProperty("ion.api.token");
    private static final String AWS_INSTANCE_ID = System.getProperty("server.id");
    private static final String CH_APP_ID = System.getProperty("application.id");

    @Configurable("5000")
    private int connectionTimeoutMilli;
    @Configurable("5000")
    private int readTimeoutMilli;

    private AsyncHttpClient httpClient;

    @PostConfigure
    public void init()
    {
        Preconditions.checkNotNull(PLATFORM_HOST);
        Preconditions.checkNotNull(CH_API_TOKEN);
        Preconditions.checkNotNull(AWS_INSTANCE_ID);
        Preconditions.checkNotNull(CH_APP_ID);

        AsyncHttpClientConfig builder = new Builder()
                .setConnectTimeout(connectionTimeoutMilli)
                .setReadTimeout(readTimeoutMilli)
                .build();
        httpClient = new AsyncHttpClient(builder);
    }

    @Override
    public boolean sendMemoryStats(MemorySnapshot snapshot)
    {
        return doPost(serialize(snapshot), STATS_MEMORY_PATH);
    }

    @Override
    public boolean sendMessagesStats(MuleMessageSnapshot snapshot)
    {
        return doPost(serialize(snapshot), STATS_MESSAGES_PATH);
    }

    private String serialize(Object snapshot)
    {
        try
        {
            return MAPPER.writeValueAsString(snapshot);
        }
        catch (JsonProcessingException e)
        {
            LOGGER.error("Error converting to json", e);
            throw new RuntimeException(e);
        }
    }

    private boolean doPost(String body, String path)
    {
        String endpoint = String.format(path, PLATFORM_HOST, AWS_INSTANCE_ID);
        Request req = new RequestBuilder()
                .setMethod("POST")
                .setUrl(endpoint)
                .setBody(body)
                .setBodyEncoding(StandardCharsets.UTF_8.name())
                .addHeader("Content-type", "application/json")
                .addHeader("X-ION-Authenticate", CH_API_TOKEN)
                .addHeader("X-ION-Application", CH_APP_ID)
                .build();
        try
        {
            Response res = httpClient.executeRequest(req).get();
            if (res.getStatusCode() != HttpURLConnection.HTTP_OK)
            {
                LOGGER.warn("Error sending metrics to platform, status code: {} - path: {}", res.getStatusCode(), endpoint);
                return false;
            }
            return true;
        }
        catch (InterruptedException | ExecutionException e)
        {
            LOGGER.warn("Could not send request to Cloudhub Platform service", e);
            return false;
        }
    }
}
