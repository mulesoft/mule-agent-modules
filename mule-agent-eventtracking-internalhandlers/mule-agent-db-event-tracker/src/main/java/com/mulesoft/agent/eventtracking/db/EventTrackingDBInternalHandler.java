package com.mulesoft.agent.eventtracking.db;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.uuid.Generators;
import com.mulesoft.agent.common.internalhandler.AbstractDBInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private final static Logger LOGGER = LogManager.getLogger(EventTrackingDBInternalHandler.class);

    /**
     * <p>
     * Table name in which the Mule agent will store the events.
     * Default: 'MULE_EVENTS'
     * </p>
     */
    @Configurable("MULE_EVENTS")
    public String eventsTable;

    /**
     * <p>
     * Table name in which the Mule agent will store the annotations associated to the main event.
     * Default: 'MULE_EVENTS_ANNOTATIONS'
     * </p>
     */
    @Configurable("MULE_EVENTS_ANNOTATIONS")
    public String annotationsTable;

    /**
     * <p>
     * Table name in which the Mule agent will store the custom business events associated to the main event.
     * Default: 'MULE_EVENTS_BUSINESS'
     * </p>
     */
    @Configurable("MULE_EVENTS_BUSINESS")
    public String businessTable;

    @Override
    protected void insert (Connection connection, Collection<AgentTrackingNotification> notifications)
            throws SQLException
    {
        PreparedStatement eventInsert = connection.prepareStatement(String.format("INSERT INTO %s (id, action, application, mule_message, mule_message_id, notification_type, path, resource_identifier, timestamp, source) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)", eventsTable));
        PreparedStatement annotationsInsert = connection.prepareStatement(String.format("INSERT INTO %s (id, event_id, annotation_type, annotation_value) " +
                "VALUES (?,?,?,?)", annotationsTable));
        PreparedStatement businessInsert = connection.prepareStatement(String.format("INSERT INTO %s (id, event_id, business_key, business_value) " +
                "VALUES (?,?,?,?)", businessTable));

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

    private UUID insertEvent (PreparedStatement statement, AgentTrackingNotification notification)
            throws SQLException
    {
        UUID id = Generators.timeBasedGenerator().generate();

        statement.setString(1, id.toString());
        statement.setString(2, notification.getAction());
        statement.setString(3, notification.getApplication());
        statement.setString(4, notification.getMuleMessage());
        statement.setString(5, notification.getNotificationType());
        statement.setString(6, notification.getPath());
        statement.setString(7, notification.getResourceIdentifier());
        statement.setLong(8, notification.getTimestamp());
        statement.setString(9, notification.getSource());

        statement.addBatch();

        return id;
    }

    private void insertAnnotations (PreparedStatement statement, UUID eventId, List<Annotation> annotations)
            throws SQLException
    {
        if (annotations == null)
        {
            return;
        }

        for (Annotation annotation : annotations)
        {
            statement.setString(1, Generators.timeBasedGenerator().generate().toString());
            statement.setString(2, eventId.toString());
            statement.setString(3, annotation.annotationType().toString());
            statement.setString(4, annotation.toString());

            statement.addBatch();
        }
    }

    private void insertBusinessEvents (PreparedStatement statement, UUID eventId, Map<String, String> businessEvents)
            throws SQLException
    {
        if (businessEvents == null)
        {
            return;
        }

        for (String key : businessEvents.keySet())
        {
            statement.setString(1, Generators.timeBasedGenerator().generate().toString());
            statement.setString(2, eventId.toString());
            statement.setString(3, key);
            statement.setString(4, businessEvents.get(key));

            statement.addBatch();
        }
    }
}
