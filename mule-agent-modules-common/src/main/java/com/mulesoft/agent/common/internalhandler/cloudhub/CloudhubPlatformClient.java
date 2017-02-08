package com.mulesoft.agent.common.internalhandler.cloudhub;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

import com.amazonaws.util.EC2MetadataUtils;
import com.google.common.base.Preconditions;

@Singleton
public class CloudhubPlatformClient {

    private final String instanceId;
    private final CloseableHttpClient httpClient;

    private static final String PLATFORM_HOST = System.getProperty("platform.services.endpoint");
    private static final String CH_API_TOKEN = System.getProperty("ion.api.token");
    private static final String STATS_MEMORY_PATH = "%s/agentstats/memory/%s";
    private static final String STATS_MESSAGES_PATH = "%s/agentstats/messages/%s";
    private static final int CONNECTION_TIMEOUT_MILLI = 5000;
    private static final int SOCKET_TIMEOUT_MILLI = 10000;

    private static final CloudhubPlatformClient INSTANCE = new CloudhubPlatformClient();
    private static final Logger logger = LogManager.getLogger(CloudhubPlatformClient.class);

    private CloudhubPlatformClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectTimeout(CONNECTION_TIMEOUT_MILLI)
                                                   .setSocketTimeout(SOCKET_TIMEOUT_MILLI)
                                                   .build();
        this.httpClient = HttpClients.custom()
                                     .setDefaultRequestConfig(requestConfig)
                                     .build();

        // check aws-java-sdk dependency
        this.instanceId = EC2MetadataUtils.getInstanceId();
        Preconditions.checkNotNull(PLATFORM_HOST);
        Preconditions.checkNotNull(CH_API_TOKEN);
    }

    private static CloseableHttpClient initClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectTimeout(CONNECTION_TIMEOUT_MILLI)
                                                   .setSocketTimeout(SOCKET_TIMEOUT_MILLI)
                                                   .build();
        return HttpClients.custom()
                          .setDefaultRequestConfig(requestConfig)
                          .build();
    }

    public static CloudhubPlatformClient getClient() {
        return INSTANCE;
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
                instanceId);
        HttpPost req = new HttpPost(endpoint);

        StringEntity entity = null;
        try {
            entity = new StringEntity(rawEntity);
        } catch (UnsupportedEncodingException e) { // ignore
        }

        req.setHeader("Content-type", "application/json");
        req.setHeader("X-ION-Authenticate", CH_API_TOKEN);
        req.setEntity(entity);

        try (CloseableHttpResponse res = httpClient.execute(req)) {
            if (res.getStatusLine().getStatusCode() != 200) {
                logger.error("Error sending metrics to platform, got status code {}",
                        res.getStatusLine().getStatusCode());
            }

            EntityUtils.consumeQuietly(res.getEntity());
        } catch (IOException e) {
            logger.error("Exception trying to call platform", e);
        }
    }
}
