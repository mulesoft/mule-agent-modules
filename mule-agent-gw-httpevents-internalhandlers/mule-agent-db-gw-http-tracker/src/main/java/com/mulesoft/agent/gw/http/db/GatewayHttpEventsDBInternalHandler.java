/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.UUID;
import javax.inject.Named;
import javax.inject.Singleton;

import com.fasterxml.uuid.Generators;
import com.mulesoft.agent.common.internalhandler.AbstractDBInternalHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.module.client.model.HttpEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOGGER = LogManager.getLogger(GatewayHttpEventsDBInternalHandler.class);

    /**
     * <p>
     * Table name in which the Mule agent will store the events.
     * Default: 'MULE_API_ANALYTICS'
     * </p>
     */
    @Configurable("MULE_API_ANALYTICS")
    String apiAnalyticsTable;

    @Override
    protected void insert(Connection connection, Collection<HttpEvent> notifications)
            throws SQLException
    {
        PreparedStatement eventInsert = connection.prepareStatement(String.format(""
                + "INSERT INTO %s (id, api_id, api_name, api_version, api_version_id, application_name, client_id, "
                + "client_ip, event_id, host_id, org_id, path, policy_violation_policy_id, policy_violation_policy_name, "
                + "policy_violation_outcome, received_ts, replied_ts, request_bytes, request_disposition, response_bytes, "
                + "status_code, transaction_id, user_agent, verb) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", apiAnalyticsTable));

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

        int parameterIndex = 1;
        statement.setString(parameterIndex++, id.toString());
        statement.setInt(parameterIndex++, event.getApiId());
        statement.setString(parameterIndex++, event.getApiName());
        statement.setString(parameterIndex++, event.getApiVersion());
        statement.setInt(parameterIndex++, event.getApiVersionId());
        statement.setString(parameterIndex++, event.getApplicationName());
        statement.setString(parameterIndex++, event.getClientId());
        statement.setString(parameterIndex++, event.getClientIp());
        statement.setString(parameterIndex++, event.getEventId());
        statement.setString(parameterIndex++, event.getHostId());
        statement.setString(parameterIndex++, event.getOrgId());
        statement.setString(parameterIndex++, event.getPath());

        if (event.getPolicyViolation() == null)
        {
            statement.setNull(parameterIndex++, Types.INTEGER);
            statement.setNull(parameterIndex++, Types.VARCHAR);
            statement.setNull(parameterIndex++, Types.VARCHAR);
        }
        else
        {
            statement.setInt(parameterIndex++, event.getPolicyViolation().getPolicyId());
            statement.setString(parameterIndex++, "");
            statement.setString(parameterIndex++, event.getPolicyViolation().getOutcome().getName());
        }


        statement.setString(parameterIndex++, event.getReceivedTs());
        statement.setString(parameterIndex++, event.getRepliedTs());
        statement.setInt(parameterIndex++, event.getRequestBytes());
        statement.setString(parameterIndex++, event.getRequestDisposition().getName());
        statement.setInt(parameterIndex++, event.getResponseBytes());
        statement.setInt(parameterIndex++, event.getStatusCode());
        statement.setString(parameterIndex++, event.getTransactionId());
        statement.setString(parameterIndex++, event.getUserAgent());
        statement.setString(parameterIndex, event.getVerb());

        statement.addBatch();

        return id;
    }
}
