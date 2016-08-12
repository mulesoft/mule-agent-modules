/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracker.analytics;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.clients.AuthenticationProxyClient;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.common.AuthenticationProxyConfiguration;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.agent.handlers.internal.buffer.DiscardingMessageBufferConfigurationFactory;
import com.mulesoft.agent.handlers.internal.client.DefaultAuthenticationProxyClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Internal handler that pushes tracking event to the Analytics service via the Authentication
 * Proxy.
 * </p>
 */
@Singleton
@Named("mule.agent.tracking.handler.analytics")
public class EventTrackingAnalyticsInternalHandler extends BufferedHandler<AgentTrackingNotification> {

    /**
     * <p>
     * Logger to be used to log information about this class.
     * </p>
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventTrackingAnalyticsInternalHandler.class);

    /**
     * <p>
     * The {@link ObjectMapper} to be used to serialize the events in the format received by Analytics.
     * </p>
     */
    private ObjectMapper objectMapper;

    /**
     * <p>
     * The configuration to be used to open a connection with the Authentication Proxy.
     * </p>
     */
    @Configurable("{}")
    private AuthenticationProxyConfiguration authenticationProxy;

    /**
     * <p>
     * Whether the handler is enabled or not.
     * </p>
     */
    @Configurable("true")
    protected boolean enabled;

    /**
     * <p>
     * The client to be used to connect to the Authentication Proxy.
     * </p>
     */
    private AuthenticationProxyClient authProxyClient;

    @Override
    public void initialize() throws InitializationException {
        super.initialize();
        authProxyClient = DefaultAuthenticationProxyClient.create(authenticationProxy, getMapper());
    }

    @Override
    protected boolean canHandle(AgentTrackingNotification agentTrackingNotification)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> notifications)
    {
        try
        {
            boolean fullSuccess = true;
            Collection<List<AgentTrackingNotification>> groupedNotifications = groupByApplication(notifications);
            for (List<AgentTrackingNotification> applicationNotifications : groupedNotifications) {
                String applicationName = applicationNotifications.get(0).getApplication();
                Map<String, Collection<String>> headers = new HashMap<>();
                headers.put("X-APPLICATION-NAME", Lists.newArrayList(applicationName));
                try
                {
                    authProxyClient.put("/insight/ingest/api/v1/", applicationNotifications, headers);
                }
                catch (Exception e)
                {
                    fullSuccess = false;
                    LOGGER.warn("Could not send tracking event to the Analytics service for application " + applicationName);
                    LOGGER.debug("Could not send tracking event to the Analytics service for application " + applicationName, e);
                }
            }
            return fullSuccess;
        }
        catch (Exception e)
        {
            LOGGER.warn("Could not send tracking event to the Analytics service");
            LOGGER.debug("Could not send tracking event to the Analytics service", e);
            return false;
        }
    }

    @Override
    public BufferConfiguration getBuffer()
    {
        if (this.buffer == null)
        {
            this.buffer = new DiscardingMessageBufferConfigurationFactory().create(10000L, 5000, BufferType.MEMORY, null);
        }
        return this.buffer;
    }

    /**
     * <p>
     * Groups events by the application that triggered them.
     * </p>
     * @param events The list of triggered events.
     * @return A collection of lists of events. Each list contains all events for one application.
     */
    private Collection<List<AgentTrackingNotification>> groupByApplication(Collection<AgentTrackingNotification> events) {
        Map<String, List<AgentTrackingNotification>> groupedEvents = new HashMap<>();
        for (AgentTrackingNotification event : events) {
            if (!groupedEvents.containsKey(event.getApplication())) {
                groupedEvents.put(event.getApplication(), new LinkedList<AgentTrackingNotification>());
            }
            groupedEvents.get(event.getApplication()).add(event);
        }
        return groupedEvents.values();
    }

    /**
     * <p>
     * Retrieves the {@link ObjectMapper} to be used to serialize the events in the format received by
     * the Analytics service.
     * The mapper is created only once and the same one is retrieved when it is requested multiple times.
     * </p>
     * @return The initialized mapper.
     */
    private ObjectMapper getMapper()
    {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();

            SimpleModule serializationModule = new SimpleModule("SerializationModule", new Version(1, 0, 0, null, null, null));

            serializationModule.addSerializer(new AnalyticsEventSerializer());
            objectMapper.registerModule(serializationModule);
        }

        return objectMapper;
    }
}
