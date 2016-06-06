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
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.clients.AuthenticationProxyClient;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.common.SecurityConfiguration;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.handlers.exception.InitializationException;
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
     * The security configuration to be used to open a connection with the Authentication Proxy.
     * </p>
     */
    @Configurable("{}")
    private SecurityConfiguration security;

    /**
     * <p>
     * The URL of the Authentication Proxy.
     * </p>
     */
    @Configurable("https://localhost:8083")
    private String authProxyEndpoint;

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
        authProxyClient = DefaultAuthenticationProxyClient.create(authProxyEndpoint, security);
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
            Collection<List<AgentTrackingNotification>> groupedNotifications = groupByApplication(notifications);
            for (List<AgentTrackingNotification> applicationNotifications : groupedNotifications) {
                Map<String, Object> headers = new HashMap<>();
                headers.put("X-APPLICATION-NAME", applicationNotifications.get(0).getApplication());
                String serializedEvents = getMapper().writeValueAsString(applicationNotifications);
                authProxyClient.put("/insight/api/v1/", serializedEvents, headers);
            }
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Could not send tracking event to the Analytics service", e);
            return false;
        }
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
