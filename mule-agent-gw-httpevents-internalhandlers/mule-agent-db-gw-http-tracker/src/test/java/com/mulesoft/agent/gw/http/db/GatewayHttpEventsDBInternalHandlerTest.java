/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.module.client.model.HttpEvent;
import com.mulesoft.module.client.model.HttpEventBuilder;
import com.mulesoft.module.client.model.PolicyViolation;
import com.mulesoft.module.client.model.PolicyViolationOutcome;
import com.mulesoft.module.client.model.RequestDisposition;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GatewayHttpEventsDBInternalHandlerTest
{

    @Test
    public void testMysql()
            throws SQLException, ClassNotFoundException, AgentEnableOperationException
    {
        GatewayHttpEventsDBInternalHandler handler = new GatewayHttpEventsDBInternalHandler();
        handler.setDriver(System.getProperty("rdbms.mysql.driver"));
        handler.setJdbcUrl(System.getProperty("rdbms.mysql.jdbcUrl"));
        handler.setUser(System.getProperty("rdbms.mysql.user"));
        handler.setPass(System.getProperty("rdbms.mysql.pass"));
        handler.apiAnalyticsTable = System.getProperty("rdbms.mysql.apiAnalyticsTable");
        handler.postConfigurable();
        handler.enable(true);

        Connection conn = getConnection(handler);
        clearTable(conn, handler);
        List<HttpEvent> metrics = createNotifications();
        for (HttpEvent notification : metrics)
        {
            handler.handle(notification);
        }

        Assert.assertEquals(metrics.size(), countRecords(conn, handler.apiAnalyticsTable));

        conn.close();
    }

    @Test
    public void testOracle()
            throws SQLException, ClassNotFoundException, AgentEnableOperationException
    {
        GatewayHttpEventsDBInternalHandler handler = new GatewayHttpEventsDBInternalHandler();
        handler.setDriver(System.getProperty("rdbms.oracle.driver"));
        handler.setJdbcUrl(System.getProperty("rdbms.oracle.jdbcUrl"));
        handler.setUser(System.getProperty("rdbms.oracle.user"));
        handler.setPass(System.getProperty("rdbms.oracle.pass"));
        handler.apiAnalyticsTable = System.getProperty("rdbms.oracle.apiAnalyticsTable");
        handler.postConfigurable();
        handler.enable(true);

        Connection conn = getConnection(handler);
        clearTable(conn, handler);
        List<HttpEvent> metrics = createNotifications();
        for (HttpEvent notification : metrics)
        {
            handler.handle(notification);
        }

        Assert.assertEquals(metrics.size(), countRecords(conn, handler.apiAnalyticsTable));

        conn.close();
    }

    private long countRecords(Connection connection, String table)
            throws SQLException
    {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
        rs.next();
        long count = rs.getLong(1);
        rs.close();
        st.close();
        return count;
    }

    private void clearTable(Connection connection, GatewayHttpEventsDBInternalHandler internalHandler)
            throws SQLException
    {
        Statement st = connection.createStatement();
        st.execute("DELETE FROM " + internalHandler.apiAnalyticsTable);
        st.close();
    }

    private Connection getConnection(GatewayHttpEventsDBInternalHandler agent)
            throws ClassNotFoundException, SQLException
    {
        Class.forName(agent.getDriver());
        return DriverManager.getConnection(agent.getJdbcUrl(), agent.getUser(), agent.getPass());
    }

    private List<HttpEvent> createNotifications()
    {
        List<HttpEvent> list = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            HttpEvent event = HttpEventBuilder.create()
                    .setApiId(4605)
                    .setApiName("zTest Proxy")
                    .setApiVersion("Rest")
                    .setApiVersionId(46672)
                    .setClientIp("127.0.0.1")
                    .setEventId("8a0e3d60-7cfc-11e5-82f4-0a0027000000")
                    .setOrgId("66310c16-bce5-43c4-b978-5945ed2f99c5")
                    .setPath("/gateway/proxy/apikit/items ")
                    .setReceivedTs("2015-10-27T19:46:19.447-03:00")
                    .setRepliedTs("2015-10-27T19:46:19.532-03:00")
                    .setRequestBytes(-1)
                    .setResponseBytes(132)
                    .setStatusCode(200)
                    .setUserAgent(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36 ")
                    .setVerb("GET")
                    .build();

            event.setRequestDisposition(RequestDisposition.PROCESSED);

            if (i % 2 == 0)
            {
                event.setPolicyViolation(PolicyViolation.create(111, "Max req # time", PolicyViolationOutcome.ERROR));
            }

            // Properties not set by the builder (!)
            event.setRepliedTs("2015-10-27T19:46:19.532-03:00");

            list.add(event);
        }
        return list;
    }
}
