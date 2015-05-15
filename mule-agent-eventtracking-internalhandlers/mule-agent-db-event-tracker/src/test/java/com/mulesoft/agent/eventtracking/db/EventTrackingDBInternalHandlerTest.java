package com.mulesoft.agent.eventtracking.db;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class EventTrackingDBInternalHandlerTest
{

    @Test
    public void test () throws SQLException, ClassNotFoundException
    {
        EventTrackingDBInternalHandler handler = new EventTrackingDBInternalHandler();
        handler.driver = System.getProperty("driver");
        handler.jdbcUrl = System.getProperty("jdbcUrl");
        handler.user = System.getProperty("user");
        handler.pass = System.getProperty("pass");
        handler.table = System.getProperty("table");
        handler.postConfigurable();

        Connection conn = getConnection(handler);
        clearTable(conn, handler);
        List<AgentTrackingNotification> notifications = createNotifications();
        for (AgentTrackingNotification notification : notifications)
        {
            handler.handle(notification);
        }
        long insertedRecords = countRecords(conn, handler);

        Assert.assertEquals(notifications.size(), insertedRecords);
        conn.close();
    }

    private long countRecords (Connection connection, EventTrackingDBInternalHandler agent) throws SQLException
    {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + agent.table);
        rs.next();
        long count = rs.getLong(1);
        rs.close();
        st.close();
        return count;
    }

    private void clearTable (Connection connection, EventTrackingDBInternalHandler agent) throws SQLException
    {
        Statement st = connection.createStatement();
        st.execute("DELETE FROM " + agent.table);
        st.close();
    }

    private Connection getConnection (EventTrackingDBInternalHandler agent) throws ClassNotFoundException, SQLException
    {
        Class.forName(agent.driver);
        return DriverManager.getConnection(agent.jdbcUrl, agent.user, agent.pass);
    }

    private List<AgentTrackingNotification> createNotifications ()
    {
        List<AgentTrackingNotification> list = new ArrayList<AgentTrackingNotification>();
        for (int i = 0; i < 10; i++)
        {
            list.add(new AgentTrackingNotification.TrackingNotificationBuilder()
                    .action("TEST " + i)
                    .annotations(new ArrayList<Annotation>())
                    .build());
        }
        return list;
    }
}