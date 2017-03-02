package com.mulesoft.agent.eventtracking.db;

import com.fasterxml.uuid.Generators;
import com.mulesoft.agent.common.internalhandler.AbstractDBInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * The DB Internal handler will store all
 * the Event Notifications produced from the Mule ESB flows in a configurable database.
 * </p>
 */
@Named("mule.agent.tracking.handler.database")
@Singleton
public class EventTrackingDBInternalHandler extends AbstractDBInternalHandler<AgentTrackingNotification>
{

    private static final Logger LOGGER = LogManager.getLogger(EventTrackingDBInternalHandler.class);

    /**
     * <p>
     * Table name in which the Mule agent will store the events.
     * Default: 'MULE_EVENTS'
     * </p>
     */
    @Configurable("MULE_EVENTS")
    String eventsTable;

    /**
     * <p>
     * Table name in which the Mule agent will store the annotations associated to the main event.
     * Default: 'MULE_EVENTS_ANNOTATIONS'
     * </p>
     */
    @Configurable("MULE_EVENTS_ANNOTATIONS")
    String annotationsTable;

    /**
     * <p>
     * Table name in which the Mule agent will store the custom business events associated to the main event.
     * Default: 'MULE_EVENTS_BUSINESS'
     * </p>
     */
    @Configurable("MULE_EVENTS_BUSINESS")
    String businessTable;

    @Override
    protected void insert(Connection connection, Collection<AgentTrackingNotification> notifications)
            throws SQLException
    {
        PreparedStatement eventInsert = connection.prepareStatement(String.format("INSERT INTO %s (id, action, application, mule_message, mule_message_id, notification_type, path, resource_identifier, timestamp, source) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)", eventsTable));
        PreparedStatement annotationsInsert = connection.prepareStatement(String.format("INSERT INTO %s (id, event_id, annotation_type, annotation_value) "
                + "VALUES (?,?,?,?)", annotationsTable));
        PreparedStatement businessInsert = connection.prepareStatement(String.format("INSERT INTO %s (id, event_id, business_key, business_value) "
                + "VALUES (?,?,?,?)", businessTable));

        for (AgentTrackingNotification notification : notifications)
        {
            LOGGER.trace("Inserting notification: " + notification);

            UUID eventId = insertEvent(eventInsert, notification);
            insertAnnotations(annotationsInsert, eventId, notification.getAnnotations());
            insertBusinessEvents(businessInsert, eventId, notification.getCustomEventProperties());
        }

        eventInsert.executeBatch();
        annotationsInsert.executeBatch();
        businessInsert.executeBatch();
    }

    private UUID insertEvent(PreparedStatement statement, AgentTrackingNotification notification)
            throws SQLException
    {
        UUID id = Generators.timeBasedGenerator().generate();

        int parameterIndex = 1;
        statement.setString(parameterIndex++, id.toString());
        statement.setString(parameterIndex++, notification.getAction());
        statement.setString(parameterIndex++, notification.getApplication());
        statement.setString(parameterIndex++, notification.getMuleMessage());
        statement.setString(parameterIndex++, notification.getMuleMessageId());
        statement.setString(parameterIndex++, notification.getNotificationType());
        statement.setString(parameterIndex++, notification.getPath());
        statement.setString(parameterIndex++, notification.getResourceIdentifier());
        statement.setLong(parameterIndex++, notification.getTimestamp());
        statement.setString(parameterIndex, notification.getSource());

        statement.addBatch();

        return id;
    }

    private void insertAnnotations(PreparedStatement statement, UUID eventId, List<Annotation> annotations)
            throws SQLException
    {
        if (annotations == null)
        {
            return;
        }

        for (Annotation annotation : annotations)
        {
            int parameterIndex = 1;
            statement.setString(parameterIndex++, Generators.timeBasedGenerator().generate().toString());
            statement.setString(parameterIndex++, eventId.toString());
            statement.setString(parameterIndex++, annotation.annotationType().toString());
            statement.setString(parameterIndex, annotation.toString());

            statement.addBatch();
        }
    }

    private void insertBusinessEvents(PreparedStatement statement, UUID eventId, Map<String, String> businessEvents)
            throws SQLException
    {
        if (businessEvents == null)
        {
            return;
        }

        for (String key : businessEvents.keySet())
        {
            int parameterIndex = 1;
            statement.setString(parameterIndex++, Generators.timeBasedGenerator().generate().toString());
            statement.setString(parameterIndex++, eventId.toString());
            statement.setString(parameterIndex++, key);
            statement.setString(parameterIndex, businessEvents.get(key));

            statement.addBatch();
        }
    }
}
