/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.db;

import com.fasterxml.uuid.Generators;
import com.mulesoft.agent.common.internalhandler.AbstractDBInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.module.client.model.HttpEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.UUID;

/**
 * <p>
 * The DB Internal handler will store all the HTTP API Analytics produced from the
 * Mule API Gateway in a configurable database.
 * </p>
 */
@Named("mule.agent.gw.http.handler.database")
@Singleton
public class GatewayHttpEventsDBInternalHandler extends AbstractDBInternalHandler<HttpEvent>
{

    private final static Logger LOGGER = LoggerFactory.getLogger(GatewayHttpEventsDBInternalHandler.class);

    /**
     * <p>
     * Table name in which the Mule agent will store the events.
     * Default: 'MULE_API_ANALYTICS'
     * </p>
     */
    @Configurable("MULE_API_ANALYTICS")
    public String apiAnalyticsTable;

    @Override
    protected void insert(Connection connection, Collection<HttpEvent> notifications)
            throws SQLException
    {
        PreparedStatement eventInsert = connection.prepareStatement(String.format("" +
                "INSERT INTO %s (id, api_id, api_name, api_version, api_version_id, application_name, client_id, " +
                "client_ip, event_id, host_id, org_id, path, policy_violation_policy_id, policy_violation_policy_name, " +
                "policy_violation_outcome, received_ts, replied_ts, request_bytes, request_disposition, response_bytes, " +
                "status_code, transaction_id, user_agent, verb) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", apiAnalyticsTable));

        for (HttpEvent notification : notifications)
        {
            LOGGER.debug("Inserting notification: " + notification);

            insertEvent(eventInsert, notification);
        }

        eventInsert.executeBatch();
    }

    private UUID insertEvent(PreparedStatement statement, HttpEvent event)
            throws SQLException
    {
        UUID id = Generators.timeBasedGenerator().generate();

        statement.setString(1, id.toString());
        statement.setInt(2, event.getApiId());
        statement.setString(3, event.getApiName());
        statement.setString(4, event.getApiVersion());
        statement.setInt(5, event.getApiVersionId());
        statement.setString(6, event.getApplicationName());
        statement.setString(7, event.getClientId());
        statement.setString(8, event.getClientIp());
        statement.setString(9, event.getEventId());
        statement.setString(10, event.getHostId());
        statement.setString(11, event.getOrgId());
        statement.setString(12, event.getPath());

        if (event.getPolicyViolation() == null)
        {
            statement.setNull(13, Types.INTEGER);
            statement.setNull(14, Types.VARCHAR);
            statement.setNull(15, Types.VARCHAR);
        }
        else
        {
            statement.setInt(13, event.getPolicyViolation().getPolicyId());
            // TODO: Once GW team add the getter to the PolicyViolation policyName field we will persist this value
            statement.setString(14, "");
            statement.setString(15, event.getPolicyViolation().getOutcome().getName());
        }


        statement.setString(16, event.getReceivedTs());
        statement.setString(17, event.getRepliedTs());
        statement.setInt(18, event.getRequestBytes());
        statement.setString(19, event.getRequestDisposition().getName());
        statement.setInt(20, event.getResponseBytes());
        statement.setInt(21, event.getStatusCode());
        statement.setString(22, event.getTransactionId());
        statement.setString(23, event.getUserAgent());
        statement.setString(24, event.getVerb());

        statement.addBatch();

        return id;
    }
}
