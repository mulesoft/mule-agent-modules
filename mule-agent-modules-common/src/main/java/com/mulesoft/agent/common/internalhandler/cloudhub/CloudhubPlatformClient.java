package com.mulesoft.agent.common.internalhandler.cloudhub;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

/**
 * Simple HTTP client for talking to Cloudhub Platform services.
 */
@Singleton
public class CloudhubPlatformClient {

    private CloseableHttpClient httpClient;

    // these properties are set by CloudhubPropertiesCoreExtension when mule starts on a worker
    private static final String PLATFORM_HOST =
            System.getProperty("platform.services.endpoint");
    private static final String CH_API_TOKEN =
            System.getProperty("ion.api.token");
    private static final String AWS_INSTANCE_ID =
            System.getProperty("server.id");

    private static final String STATS_MEMORY_PATH = "%s/agentstats/memory/%s";
    private static final String STATS_MESSAGES_PATH =
            "%s/agentstats/messages/%s";
    private static final int CONNECTION_TIMEOUT_MILLI = 5000;
    private static final int SOCKET_TIMEOUT_MILLI = 10000;

    private static final Logger LOGGER = LogManager.getLogger(
            CloudhubPlatformClient.class);

    @PostConstruct
    public void init() {
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectTimeout(CONNECTION_TIMEOUT_MILLI)
                                                   .setSocketTimeout(SOCKET_TIMEOUT_MILLI)
                                                   .build();
        httpClient = HttpClients.custom()
                                .setDefaultRequestConfig(requestConfig)
                                .build();
        Preconditions.checkNotNull(PLATFORM_HOST);
        Preconditions.checkNotNull(CH_API_TOKEN);
        Preconditions.checkNotNull(AWS_INSTANCE_ID);
    }

    public void sendMemoryStats(String rawEntity) {
        doPost(rawEntity, STATS_MEMORY_PATH);
    }

    public void sendMessagesStats(String rawEntity) {
        doPost(rawEntity, STATS_MESSAGES_PATH);
    }

    private void doPost(String rawEntity, String path) {
        String endpoint = String.format(path,
                PLATFORM_HOST,
                AWS_INSTANCE_ID);
        HttpPost req = new HttpPost(endpoint);
        StringEntity entity = new StringEntity(rawEntity,
                StandardCharsets.UTF_8);

        req.setHeader("Content-type", "application/json");
        req.setHeader("X-ION-Authenticate", CH_API_TOKEN);
        req.setEntity(entity);

        try (CloseableHttpResponse res = httpClient.execute(req)) {
            if (res.getStatusLine().getStatusCode() != 200) {
                LOGGER.warn("Error sending metrics to platform," +
                                " got status code {}",
                        res.getStatusLine().getStatusCode());
            }
            EntityUtils.consumeQuietly(res.getEntity());
        } catch (IOException e) {
            LOGGER.warn("Could not send request to CloudHub Platform" +
                    " service", e);
        }
    }
}
