package com.mulesoft.agent.monitoring.publisher.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.clients.AuthenticationProxyClient;
import com.mulesoft.agent.clients.AuthenticationProxyFactory;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.configuration.common.AuthenticationProxyConfiguration;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.agent.handlers.internal.buffer.DiscardingMessageBufferConfigurationFactory;
import com.mulesoft.agent.monitoring.publisher.ingest.client.AnypointMonitoringIngestAPIClient;
import com.mulesoft.agent.services.OnOffSwitch;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Collection;

/**
 * <p>
 * Abstract monitoring metrics publisher.
 * </p>
 */
public abstract class IngestMonitorPublisher<T> extends BufferedHandler<T>
{

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SZ";

    /**
     * Factory to create the {@link AuthenticationProxyClient}.
     */
    @Inject
    private AuthenticationProxyFactory authenticationProxyFactory;

    /**
     * Authentication proxy configuration.
     */
    @Configurable("{}")
    private AuthenticationProxyConfiguration authenticationProxy;

    /**
     * Monitoring API version.
     */
    @Configurable("1")
    private String apiVersion;

    /**
     * Whether this publisher is enabled.
     */
    @Configurable("true")
    private Boolean enabled;

    /**
     * Monitoring Ingest Client
     */
    protected AnypointMonitoringIngestAPIClient client;

    /**
     * Enabled Switch
     */
    protected OnOffSwitch enabledSwitch;

    /**
     * Manually set this publisher as enabled/disabled.
     *
     * @throws AgentEnableOperationException when an exception is thrown while trying to enable the publisher.
     */
    @Override
    public void enable(boolean state)
            throws AgentEnableOperationException
    {
        this.enabledSwitch.switchTo(state);
    }

    /**
     * @return Whether this publisher is enabled or not.
     */
    @Override
    public boolean isEnabled()
    {
        return this.enabledSwitch.isEnabled();
    }

    /**
     * Initialization code to be run after configuration.
     *
     * @throws AgentEnableOperationException when an exception is thrown while trying to enable the publisher.
     */
    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException
    {
        if (this.enabledSwitch == null)
        {
            this.enabledSwitch = OnOffSwitch.newNullSwitch(this.enabled);
        }
    }

    /**
     * Initialization process.
     *
     * @throws InitializationException when configuration is invalid.
     */
    @Override
    public void initialize() throws InitializationException
    {
        if (!this.checkConfiguration())
        {
            throw new InitializationException("Could not initialize ingest monitor publisher. Its configuration is invalid.");
        }

        ObjectMapper objectMapper = new ObjectMapper()
                .setDateFormat(new SimpleDateFormat(DATE_FORMAT))
                // to allow serialization of "empty" POJOs (no properties to serialize)
                // (without this setting, an exception is thrown in those cases)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        AuthenticationProxyClient authProxyClient = authenticationProxyFactory.create(authenticationProxy, objectMapper);
        this.client = AnypointMonitoringIngestAPIClient.create(apiVersion, authProxyClient);
        super.initialize();
    }

    /**
     * Lazily initialize buffer configuration.
     *
     * @return buffer configuration.
     */
    @Override
    public BufferConfiguration getBuffer()
    {
        if (this.buffer == null)
        {
            this.buffer = new DiscardingMessageBufferConfigurationFactory().create(60000L, 1000, BufferType.MEMORY, null);
        }
        return this.buffer;
    }

    /**
     * Publisher configuration validation.
     *
     * @return whether the configuration is valid or not.
     */
    private boolean checkConfiguration()
    {
        return this.authenticationProxy != null &&
                this.authenticationProxy.getSecurity() != null &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getKeyStoreAlias()) &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getKeyStoreAliasPassword()) &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getKeyStoreFile()) &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getKeyStorePassword()) &&
                StringUtils.isNotBlank(this.authenticationProxy.getSecurity().getTrustStoreFile()) &&
                this.authenticationProxy.getEndpoint() != null;
    }

    /**
     * @param collection Buffer contents.
     * @return Whether the process ended successfully or not.
     */
    protected abstract boolean send(Collection<T> collection);

    /**
     * @return true.
     */
    @Override
    protected boolean canHandle(T metrics)
    {
        return true;
    }

    /**
     * @param collection Buffer contents.
     * @return Whether the process ended successfully or not.
     */
    @Override
    protected boolean flush(Collection<T> collection)
    {
        return send(collection);
    }

    /**
     * <p>
     * Checks whether the provided HTTP status code belongs to the Success family.
     * </p>
     *
     * @param statusCode The status code to check.
     * @return true if the error code is between 200 and 300.
     */
    protected boolean isSuccessStatusCode(int statusCode)
    {
        return statusCode >= 200 && statusCode < 300;
    }
}
