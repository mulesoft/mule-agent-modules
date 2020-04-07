/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.db;

import static com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent.builder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent;
import com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent.Builder;
import com.mulesoft.mule.runtime.gw.api.analytics.PolicyViolation;
import com.mulesoft.mule.runtime.gw.api.analytics.PolicyViolationOutcome;
import com.mulesoft.mule.runtime.gw.api.analytics.RequestDisposition;

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
        List<AnalyticsHttpEvent> metrics = createNotifications();
        for (AnalyticsHttpEvent notification : metrics)
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
        List<AnalyticsHttpEvent> metrics = createNotifications();
        for (AnalyticsHttpEvent notification : metrics)
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

    private List<AnalyticsHttpEvent> createNotifications()
    {
        List<AnalyticsHttpEvent> list = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            Builder eventBuilder =
                builder()
                    .withApiId(4605)
                    .withApiName("zTest Proxy")
                    .withApiVersion("Rest")
                    .withApiVersionId(46672L)
                    .withClientIp("127.0.0.1")
                    .withEventId("8a0e3d60-7cfc-11e5-82f4-0a0027000000")
                    .withOrgId("66310c16-bce5-43c4-b978-5945ed2f99c5")
                    .withPath("/gateway/proxy/apikit/items ")
                    .withReceivedTs("2015-10-27T19:46:19.447-03:00")
                    .withRepliedTs("2015-10-27T19:46:19.532-03:00")
                    .withRequestBytes(-1)
                    .withResponseBytes(132)
                    .withStatusCode(200)
                    .withUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36 ")
                    .withVerb("GET")
                    .withRequestDisposition(RequestDisposition.PROCESSED);

            if (i % 2 == 0)
            {
                PolicyViolation violation = PolicyViolation.builder()
                    .withPolicyId("111")
                    .withPolicyName("Max req # time")
                    .withOutcome(PolicyViolationOutcome.ERROR)
                    .build();

                eventBuilder.withPolicyViolation(violation);
            }

            // Properties not set by the builder (!)
            eventBuilder.withRepliedTs("2015-10-27T19:46:19.532-03:00");

            list.add(eventBuilder.build());
        }
        return list;
    }
}
