package com.mulesoft.agent.eventtracker.cloudhub.source;

import java.io.ByteArrayInputStream;
import java.util.Collection;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.tracking.FlowSourceEvent;
import com.mulesoft.agent.eventtracker.cloudhub.S3StorageException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.agent.tracking.ReplayStore;
import com.mulesoft.ch.client.proxy.s3.S3FileStorageProxy;
import com.mulesoft.ch.client.proxy.s3.S3FileStorageProxyImpl;
import com.mulesoft.ion.store.tracking.keyprovider.AWSEncryptedKeyProvider;

/**
 * Cloudhub implementation of {@link ReplayStore} that stores and retrieves {@link FlowSourceEvent}.
 */
@Singleton
@Named("mule.agent.tracking.replay.cloudhub.s3")
public class CloudhubS3ReplayStore implements ReplayStore
{

    private static final Logger LOGGER = LogManager.getLogger(CloudhubS3ReplayStore.class);

    private static final String S3_BUCKET_KEY = "s3.tracking.bucket";
    private static final String APPLICTION_ID_KEY = "application.id";

    @Configurable("{}")
    private CloudhubS3Configuration cloudhubS3;

    private S3FileStorageProxy s3;

    private String bucket;

    private S3FlowSourceNameFactory nameFactory;

    CloudhubS3ReplayStore()
    {
    }

    CloudhubS3ReplayStore(CloudhubS3Configuration cloudhubS3)
    {
        this.cloudhubS3 = cloudhubS3;
    }

    @PostConfigure
    public void init() throws InitializationException
    {
        AWSEncryptedKeyProvider keyProvider = new AWSEncryptedKeyProvider(cloudhubS3.getEncryptionKey());
        String accessKey = keyProvider.getAccessKey();
        String secretKey = keyProvider.getSecretKey();
        bucket = System.getProperty(S3_BUCKET_KEY);
        if (accessKey == null || secretKey == null || bucket == null)
        {
            throw new InitializationException("Missing S3 configurations!");
        }
        nameFactory = new S3FlowSourceNameFactory(System.getProperty(APPLICTION_ID_KEY));
        this.s3 = new S3FileStorageProxyImpl();
        s3.init(accessKey, secretKey, cloudhubS3.getConnectionTimeout(), cloudhubS3.getSocketTimeout(),
                cloudhubS3.getMaxConnections());
        LOGGER.info("Initialized Cloudhub S3 replay store");
    }

    @Override
    public FlowSourceEvent getFlowSourceEvent(String appName, String flowName, String transactionId,
            ClassLoader appClassLoader)
    {
        S3Object inputStream = s3.getFileVersion(bucket, objectName(appName, transactionId, flowName), null);
        if (inputStream == null)
        {
            return null;
        }
        FlowSourceEvent event = FlowSourceStreamCodec.fromStreamAndClose(inputStream.getObjectContent(),
                appClassLoader);
        if (event != null)
        {
            event.setApplicationName(appName);
            event.setFlowName(flowName);
            event.setMessageId(transactionId);
        }
        return event;
    }

    void putFlowSourceEvent(Collection<FlowSourceEvent> events) throws S3StorageException
    {
        for (FlowSourceEvent event : events)
        {
            try
            {
                putFlowSourceEvent(event);
            }
            catch (Exception e)
            {
                throw new S3StorageException(String.format("Error storing the event: app: %s - flow: %s - message: %s",
                        event.getApplicationName(), event.getFlowName(), event.getMessageId()), e);
            }
        }
    }

    void putFlowSourceEvent(FlowSourceEvent event)
    {
        LOGGER.debug("check event {}", event.getMessageId());
        String path = objectName(event);
        ObjectMetadata value = s3.getFileVersionMetadata(bucket, path, null);
        if (value == null)
        {
            LOGGER.debug("putting event {} to s3", event.getMessageId());
            byte[] bytes = FlowSourceStreamCodec.toBytes(event);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            s3.putFileVersion(bucket, path, new ByteArrayInputStream(bytes), metadata);
            LOGGER.debug("put event {} to s3", event.getMessageId());
        }
    }

    private String objectName(FlowSourceEvent event)
    {
        return objectName(event.getApplicationName(), event.getMessageId(), event.getFlowName());
    }

    private String objectName(String appName, String messageId, String flowName)
    {
        return nameFactory.build(appName, messageId, flowName);
    }

    S3FileStorageProxy getS3()
    {
        return s3;
    }

    void setS3(S3FileStorageProxy s3)
    {
        this.s3 = s3;
    }
}
